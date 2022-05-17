package com.solarwinds.msp.ncentral.eventproduction.publisher.relay.grpc;

import com.nable.logging.Logger;
import com.nable.logging.Loggers;
import com.solarwinds.dto.Tuple;
import com.solarwinds.msp.ncentral.common.time.TimeService;
import com.solarwinds.msp.ncentral.eventproduction.api.service.publisher.CircuitBreaker;
import com.solarwinds.msp.ncentral.eventproduction.api.service.tracking.EventingControlService;
import com.solarwinds.msp.ncentral.eventproduction.api.service.tracking.EventingStartupListener;
import com.solarwinds.msp.ncentral.eventproduction.publisher.relay.configuration.MspRelayConfigurationService;
import com.solarwinds.msp.relay.Relay;

import org.springframework.scheduling.concurrent.CustomizableThreadFactory;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;

import javax.annotation.concurrent.ThreadSafe;

/**
 * Accumulates messages ({@link Relay.RequestMessage}) with the same request context ({@link Relay.Context}) and sends
 * them once per given time period or when given amount of messages is accumulated (what happens first).
 */
@ThreadSafe
@Component
public class RequestBatcher implements AutoCloseable, EventingStartupListener {

    public static final int DEFAULT_BATCH_MAXIMUM_SIZE = 50;
    public static final Duration DEFAULT_BATCH_SEND_INTERVAL = Duration.ofMillis(1_000L);
    public static final int NUMBER_OF_CONCURRENTLY_SENT_BATCHES = 10;
    private static final int NUMBER_OF_MAIN_THREADS = 1;

    private static final String THREAD_NAME_PREFIX = "RequestBatcher-";
    private static final String NEGATIVE_OR_ZERO_ERROR = "The %s value cannot be negative or zero.";

    private static final Logger logger = Loggers.EVENT_PRODUCER;

    private final BatchSender batchSender;
    private final CircuitBreaker circuitBreaker;
    private final MspRelayConfigurationService mspRelayConfigurationService;
    private final TimeService timeService;

    private final Map<Relay.Context, Tuple<Batch, CompletableFuture<Relay.PublishResponse>>> batches =
            new ConcurrentHashMap<>();
    private final Map<Relay.Context, CompletableFuture<Tuple<Batch, CompletableFuture<Relay.PublishResponse>>>>
            futureBatches = new ConcurrentHashMap<>();

    private final AtomicBoolean isStarted = new AtomicBoolean(false);
    private final Semaphore mainProcessingPermit = new Semaphore(NUMBER_OF_MAIN_THREADS, true);
    private final Lock batchProcessingPermit = new ReentrantLock(true);
    private final Semaphore batchesCreationPermits = new Semaphore(NUMBER_OF_CONCURRENTLY_SENT_BATCHES, true);

    private final AtomicReference<Duration> batchSendInterval = new AtomicReference<>();
    private final AtomicInteger batchMaximumSize = new AtomicInteger();
    private final AtomicReference<ExecutorService> executorService = new AtomicReference<>();

    /**
     * Creates an instance of this class with the specified parameters.
     */
    public RequestBatcher(MspRelayConfigurationService mspRelayConfigurationService, BatchSender batchSender,
            CircuitBreaker circuitBreaker, EventingControlService eventingControlService, TimeService timeService) {
        this.mspRelayConfigurationService = mspRelayConfigurationService;
        this.batchSender = batchSender;
        this.circuitBreaker = circuitBreaker;
        this.timeService = timeService;

        eventingControlService.addStartupListenerOrExecuteStartup(this);
    }

    @Override
    public void onEventingStart() {
        if (!isStarted.get()) {
            circuitBreaker.initialize();
            try {
                start();
                logger.info("Component {} initialized.", this.getClass().getSimpleName());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException(e);
            }
        } else {
            logger.debug("Component {} is already initialized, skipping repeated initialization.",
                    this.getClass().getSimpleName());
        }
    }

    boolean isStarted() {
        return isStarted.get();
    }

    Duration getBatchSendInterval() {
        return batchSendInterval.get();
    }

    int getBatchMaximumSize() {
        return batchMaximumSize.get();
    }

    /**
     * Starts processing batches of messages.
     *
     * @throws IllegalArgumentException if cannot get valid configuration options, e.g. batch send interval or batch
     * maximum size.
     * @throws RejectedExecutionException if the internal thread execution service cannot start the processing of
     * batches.
     * @throws InterruptedException if interrupted while waiting until for previous processing to finish
     */
    public synchronized void start() throws InterruptedException {
        if (!isStarted.get()) {
            logger.info("Message batches processing start requested.");
            configureBatching();
            waitForPreviousProcessingToFinishAndStartNewOne();
        }
    }

    private void configureBatching() {
        batchSendInterval.set(mspRelayConfigurationService.getBatchSendInterval().orElse(DEFAULT_BATCH_SEND_INTERVAL));
        if (batchSendInterval.get().toMillis() <= 0) {
            throw new IllegalArgumentException(String.format(NEGATIVE_OR_ZERO_ERROR, "batchSendIntervalMilliseconds"));
        }

        batchMaximumSize.set(mspRelayConfigurationService.getBatchMaximumSize().orElse(DEFAULT_BATCH_MAXIMUM_SIZE));
        if (batchMaximumSize.get() <= 0) {
            throw new IllegalArgumentException(String.format(NEGATIVE_OR_ZERO_ERROR, "batchMaximumSize"));
        }
    }

    private void waitForPreviousProcessingToFinishAndStartNewOne() throws InterruptedException {
        mainProcessingPermit.acquire();
        isStarted.set(true);

        executorService.set(Executors.newFixedThreadPool(NUMBER_OF_MAIN_THREADS + NUMBER_OF_CONCURRENTLY_SENT_BATCHES,
                new CustomizableThreadFactory(THREAD_NAME_PREFIX)));

        executorService.get().execute(() -> {
            try {
                processBatches();
            } finally {
                mainProcessingPermit.release();
            }
        });
    }

    @Override
    public synchronized void close() {
        if (isStarted.getAndSet(false)) {
            logger.info("Message batches processing stop requested.");
            executorService.get().shutdownNow();
            executorService.set(null);
            batches.clear();
        }
    }

    /**
     * Adds given {@link Relay.RequestMessage} to a batch for given {@link Relay.Context}.
     *
     * @param requestContext the context that represents a batch of messages to be sent at once.
     * @param requestMessage the message to be sent.
     * @return The {@link Future} answer for the message.
     * @throws InterruptedException if the thread was interrupted.
     */
    public Future<Relay.ResponseMessage> addToBatch(Relay.Context requestContext, Relay.RequestMessage requestMessage)
            throws InterruptedException {
        if (!isStarted.get()) {
            throw new IllegalStateException("Request batcher must be started.");
        }

        batchProcessingPermit.lock();
        try {
            final Tuple<Batch, CompletableFuture<Relay.PublishResponse>> batchTuple = getOrCreateBatch(requestContext);
            batchTuple.getFirst().addMessage(requestMessage);
            logger.debug("Message [{}] of type [{}] added to the MSP Relay batch [{}].", requestMessage.getId(),
                    requestMessage.getEventType(), batchTuple.getFirst().hashCode());

            final CompletableFuture<Relay.ResponseMessage> result =
                    batchTuple.getSecond().thenApply(createToResponseMessageMapper(requestMessage));

            // if the batch is full send it immediately
            if (batchTuple.getFirst().getMessagesCount() >= batchMaximumSize.get()) {
                processBatchAsynchronously(requestContext);
            }

            return result;
        } finally {
            batchProcessingPermit.unlock();
        }
    }

    private Tuple<Batch, CompletableFuture<Relay.PublishResponse>> getOrCreateBatch(Relay.Context requestContext)
            throws InterruptedException {
        Optional<Tuple<Batch, CompletableFuture<Relay.PublishResponse>>> batch = getExistingBatch(requestContext);
        if (batch.isPresent()) {
            return batch.get();
        } else {
            return createNewBatch(requestContext);
        }
    }

    private Optional<Tuple<Batch, CompletableFuture<Relay.PublishResponse>>> getExistingBatch(
            Relay.Context requestContext) throws InterruptedException {
        Tuple<Batch, CompletableFuture<Relay.PublishResponse>> batch = batches.get(requestContext);
        if (batch != null) {
            return Optional.of(batch);
        }

        CompletableFuture<Tuple<Batch, CompletableFuture<Relay.PublishResponse>>> futureBatch =
                futureBatches.get(requestContext);
        if (futureBatch != null) {
            try {
                batchProcessingPermit.unlock();
                batch = futureBatch.get();
                batchProcessingPermit.lock();
                return Optional.of(batch);
            } catch (ExecutionException e) {
                throw new RuntimeException("Error getting batch", e);
            }
        }

        return Optional.empty();
    }

    private Tuple<Batch, CompletableFuture<Relay.PublishResponse>> createNewBatch(Relay.Context requestContext)
            throws InterruptedException {
        CompletableFuture<Tuple<Batch, CompletableFuture<Relay.PublishResponse>>> futureBatch =
                futureBatches.computeIfAbsent(requestContext, context -> new CompletableFuture<>());

        batchProcessingPermit.unlock(); // don't block batches modification and sending if the next call blocks
        batchesCreationPermits.acquire();
        batchProcessingPermit.lock();

        Tuple<Batch, CompletableFuture<Relay.PublishResponse>> batch = batches.computeIfAbsent(requestContext,
                context -> new Tuple<>(new Batch(context), new CompletableFuture<>()));
        futureBatch.complete(batch);
        futureBatches.remove(requestContext);

        return batch;
    }

    private static Function<Relay.PublishResponse, Relay.ResponseMessage> createToResponseMessageMapper(
            Relay.RequestMessage requestMessage) {
        return publishResponse -> publishResponse.getResponseMessagesList()
                .stream()
                .filter(responseMessage -> requestMessage.getId() == responseMessage.getId())
                .findFirst()
                .orElseThrow(() -> createResponseMessageNotFoundException(requestMessage, publishResponse));
    }

    private static IllegalArgumentException createResponseMessageNotFoundException(Relay.RequestMessage requestMessage,
            Relay.PublishResponse publishResponse) {
        return new IllegalArgumentException(
                String.format("Message [%d] of type [%s] was not found in the response [%d] from the MSP Relay.",
                        requestMessage.getId(), requestMessage.getEventType(), publishResponse.hashCode()));
    }

    private void processBatches() {
        logger.info("Message batches processing started.");

        try {
            while (isStarted.get() && !Thread.interrupted()) {
                batchProcessingPermit.lock();
                try {
                    batches.keySet().forEach(this::processBatchAsynchronously);
                } finally {
                    batchProcessingPermit.unlock();
                }

                timeService.wait(this, batchSendInterval.get());
            }
        } catch (InterruptedException e) {
            logger.info("Message batches processing interrupted.");
        } finally {
            if (isStarted.get()) {
                close();
            }
            logger.info("Message batches processing stopped.");
        }
    }

    private void processBatchAsynchronously(Relay.Context requestContext) {
        if (!isStarted.get()) {
            return;
        }

        final Tuple<Batch, CompletableFuture<Relay.PublishResponse>> batchTuple = batches.get(requestContext);
        if ((batchTuple == null) || (batchTuple.getFirst().getMessagesCount() == 0)) {
            return;
        }

        logger.debug("Scheduling the batch [{}] for sending to the MSP Relay.", batchTuple.getFirst().hashCode());
        batches.remove(requestContext);
        executorService.get().submit(getSendBatchRunnable(batchTuple));
    }

    private Runnable getSendBatchRunnable(Tuple<Batch, CompletableFuture<Relay.PublishResponse>> batchTuple) {
        return () -> {
            batchesCreationPermits.release();

            try {
                logger.debug("Passing the batch [{}] to the sender through the circuit breaker.",
                        batchTuple.getFirst().hashCode());
                circuitBreaker.execute(() -> batchSender.sendBatch(batchTuple));
            } catch (InterruptedException e) {
                logger.info("Processing of the batch [{}] interrupted in the circuit breaker.",
                        batchTuple.getFirst().hashCode());
            }
        };
    }
}
