package com.solarwinds.msp.ncentral.eventproduction.publisher.relay.grpc;

import com.nable.logging.Logger;
import com.nable.logging.Loggers;
import com.solarwinds.dto.Tuple;
import com.solarwinds.msp.ncentral.eventproduction.api.entity.EventStatistic;
import com.solarwinds.msp.ncentral.eventproduction.api.service.statistics.EventStatistics;
import com.solarwinds.msp.ncentral.eventproduction.api.service.tracking.EventingControlService;
import com.solarwinds.msp.ncentral.eventproduction.api.service.tracking.EventingStartupListener;
import com.solarwinds.msp.ncentral.eventproduction.publisher.relay.configuration.MspRelayConfigurationService;
import com.solarwinds.msp.relay.Relay;
import com.solarwinds.util.function.TracingFunction;

import org.springframework.stereotype.Component;

import java.net.URISyntaxException;
import java.rmi.RemoteException;
import java.time.Duration;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;

/**
 * This class represents the Batch Sender. It handles the actual sending of batches to the remote MSP Relay server and
 * receives response(s). It also handles communication errors and provides the exponential back-off mechanism for all
 * event delivery error types.
 */
@Component
public class BatchSender implements EventingStartupListener, Observer {

    private static final Logger logger = Loggers.EVENT_PRODUCER;

    private static final String FUNCTION_NAME = "MSP-Relay::Publish";
    private static final Duration DEFAULT_WAIT_TIME_FOR_RESPONSE_AFTER_SENDING = Duration.ofMillis(15_000L);

    private final PublisherProvider publisherProvider;
    private final EventStatistics eventStatistics;
    private final MspRelayConfigurationService mspRelayConfigurationService;

    private final Map<String, Function<Relay.PublishRequest, Future<Relay.PublishResponse>>> sendFunctionMappings =
            new ConcurrentHashMap<>();
    private Duration waitTimeForResponseAfterSending;

    /**
     * Creates an instance of this class with the specified parameters.
     */
    public BatchSender(PublisherProvider publisherProvider, EventStatistics eventStatistics,
            MspRelayConfigurationService mspRelayConfigurationService, EventingControlService eventingControlService) {
        this.publisherProvider = publisherProvider;
        this.eventStatistics = eventStatistics;
        this.mspRelayConfigurationService = mspRelayConfigurationService;

        eventingControlService.addStartupListenerOrExecuteStartup(this);
        publisherProvider.addObserver(this);
    }

    @Override
    public void onEventingStart() {
        waitTimeForResponseAfterSending = mspRelayConfigurationService.getWaitTimeForResponseAfterSending()
                .orElse(DEFAULT_WAIT_TIME_FOR_RESPONSE_AFTER_SENDING);
        logger.info("Component {} initialized.", this.getClass().getSimpleName());
    }

    /**
     * Sends the batch to the MSP Relay server and handles the result - completes the provided future.
     *
     * @param batchTuple the batch to send along with future to be completed with result or error.
     * @throws InterruptedException if interrupted while waiting
     * @throws Exception if any error occurs (for example if no response from the MSP Relay server is received).
     */
    public void sendBatch(Tuple<Batch, CompletableFuture<Relay.PublishResponse>> batchTuple)
            throws InterruptedException {
        Batch batch = batchTuple.getFirst();
        try {
            Function<Relay.PublishRequest, Future<Relay.PublishResponse>> sendFunction =
                    getSendFunction(batch.getBizappsCustomerId());
            Relay.PublishResponse publishResponse = sendBatch(batch, sendFunction);
            batchTuple.getSecond().complete(publishResponse);

            eventStatistics.addStatistic(EventStatistic.builder()
                    .statisticType(EventStatistic.StatisticType.PROCESSING_DATA)
                    .statisticSubType(EventStatistic.StatisticSubType.SUCCESSFULLY_SENT_TO_RELAY)
                    .statisticValue(batch.getMessagesCount())
                    .build());
        } catch (InterruptedException interruptedException) {
            throw interruptedException;
        } catch (RemoteException | URISyntaxException | ExecutionException | TimeoutException | RuntimeException exception) {
            // if there is any error complete the Future with the exception
            batchTuple.getSecond().completeExceptionally(exception);

            final String hashCode = String.valueOf(batch.hashCode());
            logger.error("Cannot send the batch [{}] to the MSP Relay.", hashCode, exception);

            // throw the exception so the CircuitBreaker can react on it
            throw new RuntimeException(exception);
        }
    }

    private Function<Relay.PublishRequest, Future<Relay.PublishResponse>> getSendFunction(String bizappsCustomerId)
            throws RemoteException, URISyntaxException {
        if (!initializedForConfiguration(bizappsCustomerId)) {
            createNewSendFunctionForConfiguration(bizappsCustomerId);
        }
        return sendFunctionMappings.get(bizappsCustomerId);
    }

    private synchronized void createNewSendFunctionForConfiguration(String bizappsCustomerId)
            throws RemoteException, URISyntaxException {
        try {
            final FuturePublisher publisher = publisherProvider.getFuturePublisher(bizappsCustomerId);
            sendFunctionMappings.put(bizappsCustomerId,
                    new TracingFunction<>(publisher::publish, logger, FUNCTION_NAME));
        } catch (RemoteException | URISyntaxException exception) {
            logger.error("The Batch Sender encountered an issue while trying to create a Publisher.", exception);
            throw exception;
        }
    }

    private Relay.PublishResponse sendBatch(Batch batch,
            Function<Relay.PublishRequest, Future<Relay.PublishResponse>> sendFunction)
            throws InterruptedException, ExecutionException, TimeoutException {
        Relay.PublishRequest publishRequest = batch.buildRequest();

        logger.debug("Sending the batch [{}] to the MSP Relay as the request [{}].", batch.hashCode(),
                publishRequest.hashCode());
        Relay.PublishResponse publishResponse = sendFunction.apply(publishRequest)
                .get(waitTimeForResponseAfterSending.toMillis(), TimeUnit.MILLISECONDS);
        logger.debug("Received the response [{}] from the MSP Relay for the batch [{}].", publishResponse.hashCode(),
                batch.hashCode());

        return publishResponse;
    }

    private synchronized boolean initializedForConfiguration(String bizappsCustomerId) {
        return sendFunctionMappings.containsKey(bizappsCustomerId);
    }

    @Override
    public void update(Observable o, Object arg) {
        String bizappsCustomerId = (String) arg;
        removeSendFunctionForConfiguration(bizappsCustomerId);
    }

    private synchronized void removeSendFunctionForConfiguration(String bizappsCustomerId) {
        sendFunctionMappings.remove(bizappsCustomerId);
    }
}