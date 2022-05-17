package com.solarwinds.msp.ncentral.eventproduction.publisher.relay.grpc;

import com.solarwinds.dto.Tuple;
import com.solarwinds.msp.ncentral.common.time.TimeService;
import com.solarwinds.msp.ncentral.eventproduction.api.service.publisher.CircuitBreaker;
import com.solarwinds.msp.ncentral.eventproduction.api.service.publisher.CircuitBreakerResult;
import com.solarwinds.msp.ncentral.eventproduction.api.service.tracking.EventingControlService;
import com.solarwinds.msp.ncentral.eventproduction.publisher.relay.configuration.MspRelayConfigurationService;
import com.solarwinds.msp.relay.Relay;
import com.solarwinds.util.BooleanWaitingObserver;
import com.solarwinds.util.concurrent.InterruptibleRunnable;
import com.solarwinds.util.function.Condition;
import com.solarwinds.util.function.ConditionWaitParameters;

import org.assertj.core.api.SoftAssertions;
import org.jooq.lambda.Unchecked;
import org.jooq.lambda.fi.lang.CheckedRunnable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockingDetails;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.VoidAnswer1;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.PrimitiveIterator;
import java.util.Random;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BooleanSupplier;
import java.util.function.IntFunction;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.AdditionalAnswers.answerVoid;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockingDetails;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SuppressWarnings("unchecked")
@ExtendWith(MockitoExtension.class)
class RequestBatcherTest {

    private static final Duration BATCH_SEND_INTERVAL = Duration.ofMillis(1_000L);
    private static final int BATCH_MAXIMUM_SIZE = 5;
    private static final String BUSINESS_APPLICATIONS_ID_1 = "BizApps-ID-1";
    private static final String BUSINESS_APPLICATIONS_ID_2 = "BizApps-ID-2";
    private static final String BUSINESS_APPLICATIONS_CUSTOMER_PREFIX = "Bizapps-Customer-";
    private static final String BUSINESS_APPLICATIONS_CUSTOMER_1 = "Bizapps-Customer-1";
    private static final String BUSINESS_APPLICATIONS_CUSTOMER_2 = "Bizapps-Customer-2";
    private static final int INVALID_VALUE = 0;
    private static final long FUTURE_TIMEOUT = 1_000;
    private static final ConditionWaitParameters CONDITION_WAIT_PARAMETERS = ConditionWaitParameters.newBuilder()
            .withPauseBetweenRetries(Duration.ofMillis(1L))
            .withMaximumWait(Duration.ofSeconds(10L))
            .build();

    private static final Logger logger = LoggerFactory.getLogger(RequestBatcherTest.class);
    private static final Random random = new Random();

    private RequestBatcher requestBatcher;

    @Mock
    private MspRelayConfigurationService mspRelayConfigurationServiceMock;
    @Mock
    private BatchSender batchSenderMock;
    @Mock
    private CircuitBreaker circuitBreakerMock;
    @Mock
    private EventingControlService eventingControlServiceMock;
    @Mock
    private TimeService timeServiceMock;
    private final BooleanWaitingObserver hasWaitServiceStarted = new BooleanWaitingObserver();
    private final BooleanWaitingObserver canWaitServiceQuit = new BooleanWaitingObserver();

    @Captor
    private ArgumentCaptor<Tuple<Batch, CompletableFuture<Relay.PublishResponse>>> publishRequestArgumentCaptor;

    @BeforeEach
    void setUp() {
        requestBatcher = new RequestBatcher(mspRelayConfigurationServiceMock, batchSenderMock, circuitBreakerMock,
                eventingControlServiceMock, timeServiceMock);
    }

    @AfterEach
    void tearDown() {
        requestBatcher.close();
    }

    @Test
    void onEventingStart_initializes_circuitBreaker_and_starts_RequestBatcher() throws InterruptedException {
        RequestBatcher requestBatcherStartedByListenerCall =
                new RequestBatcher(mspRelayConfigurationServiceMock, batchSenderMock, circuitBreakerMock,
                        eventingControlServiceMock, timeServiceMock);
        verify(eventingControlServiceMock).addStartupListenerOrExecuteStartup(requestBatcherStartedByListenerCall);
        requestBatcherStartedByListenerCall.onEventingStart();

        assertThat(requestBatcherStartedByListenerCall.isStarted()).isTrue();
        verify(circuitBreakerMock).initialize();
    }

    @Test
    void start_uses_defaults_when_configuration_is_missing() throws Exception {
        startBatcher();

        assertThat(requestBatcher.isStarted()).isTrue();
        assertThat(requestBatcher.getBatchSendInterval()).isEqualTo(RequestBatcher.DEFAULT_BATCH_SEND_INTERVAL);
        assertThat(requestBatcher.getBatchMaximumSize()).isEqualTo(RequestBatcher.DEFAULT_BATCH_MAXIMUM_SIZE);

        verify(mspRelayConfigurationServiceMock).getBatchSendInterval();
        verify(mspRelayConfigurationServiceMock).getBatchMaximumSize();
    }

    @Test
    void start_uses_configuration_when_present() throws Exception {
        when(mspRelayConfigurationServiceMock.getBatchSendInterval()).thenReturn(Optional.of(BATCH_SEND_INTERVAL));
        when(mspRelayConfigurationServiceMock.getBatchMaximumSize()).thenReturn(OptionalInt.of(BATCH_MAXIMUM_SIZE));

        startBatcher();

        assertThat(requestBatcher.isStarted()).isTrue();
        assertThat(requestBatcher.getBatchSendInterval()).isEqualTo(BATCH_SEND_INTERVAL);
        assertThat(requestBatcher.getBatchMaximumSize()).isEqualTo(BATCH_MAXIMUM_SIZE);
    }

    @Test
    void startInvalidBatchSendInterval() {
        when(mspRelayConfigurationServiceMock.getBatchSendInterval()).thenReturn(getDuration(INVALID_VALUE));
        assertThrows(IllegalArgumentException.class, () -> requestBatcher.start());
    }

    @Test
    void startInvalidBatchMaximumSize() {
        when(mspRelayConfigurationServiceMock.getBatchMaximumSize()).thenReturn(OptionalInt.of(INVALID_VALUE));
        assertThrows(IllegalArgumentException.class, () -> requestBatcher.start());
        verify(mspRelayConfigurationServiceMock).getBatchSendInterval();
    }

    @Test
    void close() throws Exception {
        startBatcher();
        requestBatcher.close();
        assertThat(requestBatcher.isStarted()).isFalse();
    }

    // ----------------------------------------------------------------------------------------------------------------
    // addToBatch method tests
    // ----------------------------------------------------------------------------------------------------------------

    @Test
    void addToBatchWhenBatcherNotStarted() {
        assertThrows(IllegalStateException.class, () -> requestBatcher.addToBatch(null, null));
    }

    @Test
    void addToBatch_adds_messages_to_existing_batch() throws Exception {
        mockCircuitBreakerAndStartBatcher();

        final Relay.Context context = createContext();
        final List<Relay.RequestMessage> requestMessages = addMessagesToBatch(context, BATCH_MAXIMUM_SIZE - 1);

        processCurrentBatchesAndThenPause();
        verifyThatSendFunctionWasInvoked(parameters(context, requestMessages));
    }

    @Test
    void addToBatch_adds_messages_to_existing_context_specific_batch() throws Exception {
        mockCircuitBreakerAndStartBatcher();

        final Relay.Context context1 = createContext(BUSINESS_APPLICATIONS_ID_1);
        final List<Relay.RequestMessage> requestMessages1 = addMessagesToBatch(context1, BATCH_MAXIMUM_SIZE - 1);
        final Relay.Context context2 = createContext(BUSINESS_APPLICATIONS_ID_2);
        final List<Relay.RequestMessage> requestMessages2 = addMessagesToBatch(context2, BATCH_MAXIMUM_SIZE - 1);

        processCurrentBatchesAndThenPause();
        verifyThatSendFunctionWasInvoked(parameters(context1, requestMessages1),
                parameters(context2, requestMessages2));
    }

    @Test
    void addToBatch_sends_request_when_maximum_messages_count_per_batch_is_reached() throws Exception {
        mockCircuitBreakerAndStartBatcher();

        final Relay.Context context = createContext();
        final List<Relay.RequestMessage> requestMessages = createRequestMessages(BATCH_MAXIMUM_SIZE);
        addMessagesToBatch(context, requestMessages);

        processCurrentBatchesAndThenPause();
        verifyThatSendFunctionWasInvoked(parameters(context, requestMessages));
    }

    @Test
    void addToBatch_sends_request_per_context_when_maximum_messages_count_per_batch_is_reached() throws Exception {
        mockCircuitBreakerAndStartBatcher();

        final Relay.Context context1 = createContext(BUSINESS_APPLICATIONS_ID_1);
        final List<Relay.RequestMessage> requestMessages1 = createRequestMessages(BATCH_MAXIMUM_SIZE);
        final Relay.Context context2 = createContext(BUSINESS_APPLICATIONS_ID_2);
        final List<Relay.RequestMessage> requestMessages2 = createRequestMessages(BATCH_MAXIMUM_SIZE);

        addMessagesToBatch(context1, requestMessages1.subList(0, BATCH_MAXIMUM_SIZE - 1));
        addMessagesToBatch(context2, requestMessages2.subList(0, BATCH_MAXIMUM_SIZE - 1));
        addMessagesToBatch(context1, requestMessages1.subList(BATCH_MAXIMUM_SIZE - 1, BATCH_MAXIMUM_SIZE));
        addMessagesToBatch(context2, requestMessages2.subList(BATCH_MAXIMUM_SIZE - 1, BATCH_MAXIMUM_SIZE));

        processCurrentBatchesAndThenPause();
        verifyThatSendFunctionWasInvoked(parameters(context1, requestMessages1),
                parameters(context2, requestMessages2));
    }

    @Test
    void addToBatch_always_sends_request_when_maximum_messages_count_per_batch_is_reached_even_if_sendFunction_blocks()
            throws Exception {
        mockCircuitBreakerAndStartBatcher();

        final CountDownLatch sendFunctionBlocker = new CountDownLatch(1);
        mockSendFunctionToInvokeCallbackAndThenReturnOkResponse(sendFunctionBlocker::await);

        final Relay.Context context = createContext();
        final List<Relay.RequestMessage> requestMessages1 = createRequestMessages(BATCH_MAXIMUM_SIZE);
        addMessagesToBatch(context, requestMessages1);
        processCurrentBatchesAndThenPause();

        final List<Relay.RequestMessage> requestMessages2 = createRequestMessages(BATCH_MAXIMUM_SIZE);
        addMessagesToBatch(context, requestMessages2);
        processCurrentBatchesAndThenPause();

        final List<Relay.RequestMessage> requestMessages3 = createRequestMessages(BATCH_MAXIMUM_SIZE);
        addMessagesToBatch(context, requestMessages3);
        processCurrentBatchesAndThenPause();

        sendFunctionBlocker.countDown();
        //processCurrentBatches();
        //closeBatcher();

        verifyThatSendFunctionWasInvoked(parameters(context, requestMessages1), parameters(context, requestMessages2),
                parameters(context, requestMessages3));
    }

    @Test
    void addToBatch_returns_future_specific_for_message() throws Exception {
        mockCircuitBreakerAndStartBatcher();
        mockSendFunctionToReturnOkResponse();

        final List<Relay.RequestMessage> requestMessages = createRequestMessages(BATCH_MAXIMUM_SIZE);
        final List<Future<Relay.ResponseMessage>> responseMessagesFutures =
                addMessagesToBatch(createContext(), requestMessages);

        processCurrentBatchesAndThenPause();
        assertThat(responseMessagesFutures).extracting(
                future -> future.get(FUTURE_TIMEOUT, TimeUnit.MILLISECONDS).getId())
                .containsSequence(
                        requestMessages.stream().map(Relay.RequestMessage::getId).collect(Collectors.toList()));
    }

    @Test
    void addToBatch_returns_future_that_fails_when_specific_messageId_is_missing_in_response() throws Exception {
        mockCircuitBreakerAndStartBatcher();
        final List<Relay.RequestMessage> requestMessages = createRequestMessages(BATCH_MAXIMUM_SIZE);
        final Relay.PublishResponse publishResponse = createResponse(requestMessages, id -> id + 123456);
        doAnswer(invocation -> {
            final Tuple<Batch, CompletableFuture<Relay.PublishResponse>> batchTuple = invocation.getArgument(0);
            batchTuple.getSecond().complete(publishResponse);
            return null;
        }).when(batchSenderMock).sendBatch(any(Tuple.class));

        final List<Future<Relay.ResponseMessage>> responseMessagesFutures =
                addMessagesToBatch(createContext(), requestMessages);

        processCurrentBatchesAndThenPause();

        final PrimitiveIterator.OfInt messageIds =
                requestMessages.stream().mapToInt(Relay.RequestMessage::getId).iterator();
        final SoftAssertions softly = new SoftAssertions();
        responseMessagesFutures.forEach(responseMessageFuture -> softly.assertThatThrownBy(responseMessageFuture::get)
                .isInstanceOf(ExecutionException.class)
                .hasCauseInstanceOf(IllegalArgumentException.class)
                .hasStackTraceContaining(String.format(
                        "Message [%d] of type [%s] was not found in the response [%d] from the MSP Relay.",
                        messageIds.next(), "Test", publishResponse.hashCode())));
        softly.assertAll();
    }

    @Test
    void batches_are_sent_periodically_after_start_is_called() throws Exception {
        mockCircuitBreakerAndStartBatcher();

        final Relay.Context context1 = createContext(BUSINESS_APPLICATIONS_CUSTOMER_1);
        final List<Relay.RequestMessage> requestMessages1 = createRequestMessages(BATCH_MAXIMUM_SIZE - 1);
        addMessagesToBatch(context1, requestMessages1);

        processCurrentBatchesAndThenPause();
        verifyThatSendFunctionWasInvoked(parameters(context1, requestMessages1));

        final Relay.Context context2 = createContext(BUSINESS_APPLICATIONS_CUSTOMER_2);
        final List<Relay.RequestMessage> requestMessages2 = createRequestMessages(BATCH_MAXIMUM_SIZE - 2);
        addMessagesToBatch(context2, requestMessages2);

        processCurrentBatchesAndThenPause();
        verifyThatSendFunctionWasInvoked(parameters(context1, requestMessages1),
                parameters(context2, requestMessages2));
    }

    @Test
    void all_batches_are_sent_periodically_after_start_is_called() throws Exception {
        mockCircuitBreakerAndStartBatcher();

        final Relay.Context context1 = createContext(BUSINESS_APPLICATIONS_CUSTOMER_1);
        final List<Relay.RequestMessage> requestMessages1 = createRequestMessages(BATCH_MAXIMUM_SIZE - 1);
        final Relay.Context context2 = createContext(BUSINESS_APPLICATIONS_CUSTOMER_2);
        final List<Relay.RequestMessage> requestMessages2 = createRequestMessages(BATCH_MAXIMUM_SIZE - 2);

        addMessagesToBatch(context1, requestMessages1);
        addMessagesToBatch(context2, requestMessages2);

        processCurrentBatchesAndThenPause();
        verifyThatSendFunctionWasInvoked(parameters(context1, requestMessages1),
                parameters(context2, requestMessages2));
    }

    @Test
    void addToBatchAndCheckThatConcurrentSendingIsLimited() throws TimeoutException, InterruptedException {
        mockCircuitBreakerAndStartBatcher();

        // Mock the sending function so it blocks
        final AtomicInteger batchSendCounter = new AtomicInteger(0);
        final CyclicBarrier sendFunctionBlocker = mockSendFunctionToBlock(() -> {
            batchSendCounter.incrementAndGet();
            synchronized (batchSendCounter) {
                batchSendCounter.notifyAll();
            }
        });

        final int numberOfBatches = RequestBatcher.NUMBER_OF_CONCURRENTLY_SENT_BATCHES * 4 + 1;
        final ExecutorService executorService = Executors.newFixedThreadPool(numberOfBatches);
        final AtomicInteger batchCreationCounter = new AtomicInteger(0);

        // Launch concurrent creation of more batches than can be created and sent at one moment
        for (int idSuffix = 1; idSuffix <= numberOfBatches; idSuffix++) {
            final Relay.Context context = createContext(BUSINESS_APPLICATIONS_CUSTOMER_PREFIX + idSuffix);
            executorService.submit(() -> {
                // this method can block until there is available permit from the semaphore
                addMessagesToBatch(context, BATCH_MAXIMUM_SIZE);
                batchCreationCounter.incrementAndGet();
                synchronized (batchCreationCounter) {
                    batchCreationCounter.notifyAll();
                }
            });
        }

        // Wait until maximum possible count of batches is created. Other threads are blocked on semaphore
        new Condition(() -> batchCreationCounter.get() == RequestBatcher.NUMBER_OF_CONCURRENTLY_SENT_BATCHES,
                CONDITION_WAIT_PARAMETERS).await(batchCreationCounter);

        // Unblock processing of batches which passes all current batches to sending and unblocks creation of next batches
        processCurrentBatchesAndThenPause();
        new Condition(() -> batchCreationCounter.get() == RequestBatcher.NUMBER_OF_CONCURRENTLY_SENT_BATCHES * 2,
                CONDITION_WAIT_PARAMETERS).await(batchCreationCounter);

        // At this point there should be exactly:
        // - number of batches in process of sending - blocked = RequestBatcher.NUMBER_OF_CONCURRENTLY_SENT_BATCHES
        // - no batch is sent yet
        new Condition(
                () -> sendFunctionBlocker.getNumberWaiting() == RequestBatcher.NUMBER_OF_CONCURRENTLY_SENT_BATCHES,
                CONDITION_WAIT_PARAMETERS).await(sendFunctionBlocker);
        assertThat(batchSendCounter.get()).isEqualTo(0);

        do {
            final int batchSendCounterLast = batchSendCounter.get();
            final int batchCreationCounterLast = batchCreationCounter.get();

            // Unblock sending of batches
            sendFunctionBlocker.reset();

            // There should be next RequestBatcher.NUMBER_OF_CONCURRENTLY_SENT_BATCHES batches sent after unblocking sending
            final int nextExpectedBatchSendCounter = Math.min(numberOfBatches,
                    batchSendCounterLast + RequestBatcher.NUMBER_OF_CONCURRENTLY_SENT_BATCHES);
            new Condition(() -> batchSendCounter.get() == nextExpectedBatchSendCounter,
                    CONDITION_WAIT_PARAMETERS).await(batchSendCounter);

            // Unblock processing of batches
            processCurrentBatchesAndThenPause();

            // There should be next RequestBatcher.NUMBER_OF_CONCURRENTLY_SENT_BATCHES batches passed to send after unblocking processing
            final int nextExpectedSendFunctionWaitingCount = Math.min(numberOfBatches - nextExpectedBatchSendCounter,
                    nextExpectedBatchSendCounter - batchSendCounterLast);
            new Condition(() -> sendFunctionBlocker.getNumberWaiting() == nextExpectedSendFunctionWaitingCount,
                    CONDITION_WAIT_PARAMETERS).await(sendFunctionBlocker);

            // There should be next RequestBatcher.NUMBER_OF_CONCURRENTLY_SENT_BATCHES batches created as well
            final int nextExpectedBatchCreationCounter = Math.min(numberOfBatches,
                    batchCreationCounterLast + RequestBatcher.NUMBER_OF_CONCURRENTLY_SENT_BATCHES);
            new Condition(() -> batchCreationCounter.get() == nextExpectedBatchCreationCounter,
                    CONDITION_WAIT_PARAMETERS).await(batchCreationCounter);

            logger.debug("Created={}, Sent={}", batchCreationCounter.get(), batchSendCounter.get());
        } while (batchSendCounter.get() != numberOfBatches);

        logger.debug("Created={}, Sent={}. Finished.", batchCreationCounter.get(), batchSendCounter.get());

        // at this point there should be exactly:
        // number of created batches = all
        // all batches should be sent because nothing is blocked
        assertThat(batchCreationCounter.get()).isEqualTo(numberOfBatches);
        assertThat(batchSendCounter.get()).isGreaterThanOrEqualTo(numberOfBatches);
    }

    @Test
    void addToBatch_throws_IllegalStateException_after_close_is_called() throws Exception {
        startBatcher();
        closeBatcher();

        assertThatThrownBy(() -> addMessagesToBatch(createContext(), createRequestMessages(1))).isInstanceOf(
                IllegalStateException.class);
    }

    @Test
    void batches_are_dropped_when_close_is_called() throws Exception {
        startBatcher();
        addMessagesToBatch(createContext(BUSINESS_APPLICATIONS_CUSTOMER_1), 1);

        closeBatcher();
        mockCircuitBreakerAndStartBatcher();

        final Relay.Context context2 = createContext(BUSINESS_APPLICATIONS_CUSTOMER_2);
        final List<Relay.RequestMessage> requestMessages2 = addMessagesToBatch(context2, 1);
        processCurrentBatchesAndThenPause();

        verifyThatSendFunctionWasInvoked(parameters(context2, requestMessages2));
    }

    // ----------------------------------------------------------------------------------------------------------------
    // Private methods
    // ----------------------------------------------------------------------------------------------------------------

    private void mockCircuitBreakerAndStartBatcher() throws InterruptedException, TimeoutException {
        mockCircuitBreaker();
        startBatcher();
    }

    private void mockCircuitBreaker() throws InterruptedException {
        CircuitBreakerResult circuitBreakerResultMock = mock(CircuitBreakerResult.class);
        doAnswer(invocation -> {
            InterruptibleRunnable command = invocation.getArgument(0);
            command.run();
            return circuitBreakerResultMock;
        }).when(circuitBreakerMock).execute(any(InterruptibleRunnable.class));
    }

    private void startBatcher() throws InterruptedException, TimeoutException {
        mockWaitService();
        requestBatcher.start();
        waitUntilWaitServiceIsCalled();
    }

    private void mockWaitService() throws InterruptedException {
        doAnswer(invocation -> {
            try {
                canWaitServiceQuit.update(null, false);
                hasWaitServiceStarted.update(null, true);
                canWaitServiceQuit.waitUntilBecomes(true, CONDITION_WAIT_PARAMETERS);
            } finally {
                hasWaitServiceStarted.update(null, false);
            }
            return null;
        }).when(timeServiceMock).wait(eq(requestBatcher), eq(BATCH_SEND_INTERVAL));
    }

    private void waitUntilWaitServiceIsCalled() throws InterruptedException, TimeoutException {
        hasWaitServiceStarted.waitUntilBecomes(true, CONDITION_WAIT_PARAMETERS);
        hasWaitServiceStarted.update(null, false);
    }

    private void closeBatcher() {
        requestBatcher.close();
        unblockWaitService();
    }

    private static Optional<Duration> getDuration(long value) {
        return Optional.of(Duration.of(value, ChronoUnit.MILLIS));
    }

    private static Relay.Context createContext() {
        return createContext(BUSINESS_APPLICATIONS_ID_1);
    }

    private static Relay.Context createContext(String businessApplicationsId) {
        return Relay.Context.newBuilder().setBizappsCustomerId(businessApplicationsId).build();
    }

    private List<Relay.RequestMessage> addMessagesToBatch(Relay.Context context, int numberOfMessages) {
        final List<Relay.RequestMessage> requestMessages = createRequestMessages(numberOfMessages);
        addMessagesToBatch(context, requestMessages);
        return requestMessages;
    }

    private List<Relay.RequestMessage> createRequestMessages(int numberOfMessages) {
        int firstMessageIdNonZero = random.nextInt();
        if (firstMessageIdNonZero == 0) {
            firstMessageIdNonZero = 123;
        }
        return IntStream.range(firstMessageIdNonZero, firstMessageIdNonZero + numberOfMessages)
                .mapToObj(messageId -> Relay.RequestMessage.newBuilder().setId(messageId).setEventType("Test").build())
                .collect(Collectors.toList());
    }

    private List<Future<Relay.ResponseMessage>> addMessagesToBatch(Relay.Context context,
            List<Relay.RequestMessage> requestMessages) {
        return requestMessages.stream()
                .map(Unchecked.function(requestMessage -> requestBatcher.addToBatch(context, requestMessage)))
                .collect(Collectors.toList());
    }

    private void processCurrentBatchesAndThenPause() throws InterruptedException, TimeoutException {
        unblockWaitService();
        waitUntilWaitServiceIsCalled();
    }

    private void unblockWaitService() {
        canWaitServiceQuit.update(null, true);
    }

    private Tuple<Relay.Context, List<Relay.RequestMessage>> parameters(Relay.Context context,
            List<Relay.RequestMessage> requestMessages) {
        return new Tuple<>(context, requestMessages);
    }

    private void verifyThatSendFunctionWasInvoked(
            Tuple<Relay.Context, List<Relay.RequestMessage>>... contextMessageTuples) throws Exception {
        MockingDetails batchSenderMockDetails = mockingDetails(batchSenderMock);
        BooleanSupplier isBatchSenderInvocationCountAsExpected =
                () -> batchSenderMockDetails.getInvocations().size() == contextMessageTuples.length;
        new Condition(isBatchSenderInvocationCountAsExpected, CONDITION_WAIT_PARAMETERS).await();

        verify(batchSenderMock, times(contextMessageTuples.length)).sendBatch(publishRequestArgumentCaptor.capture());
        assertThat(publishRequestArgumentCaptor.getAllValues()).extracting("first.requestBuilder.context",
                "first.requestBuilder.messagesList")
                .contains(Stream.of(contextMessageTuples)
                        .map(item -> org.assertj.core.groups.Tuple.tuple(item.getFirst(), item.getSecond()))
                        .toArray(org.assertj.core.groups.Tuple[]::new));
    }

    private void mockSendFunctionToInvokeCallbackAndThenReturnOkResponse(CheckedRunnable callback)
            throws InterruptedException {
        doAnswer(answerVoid((VoidAnswer1<Tuple<Batch, CompletableFuture<Relay.PublishResponse>>>) batchTuple -> {
            try {
                logger.debug("Thread {}: Send function mock is calling callback.run().",
                        Thread.currentThread().getName());
                callback.run();

                logger.debug("Thread {}: Send function mock is creating response and completing the future.",
                        Thread.currentThread().getName());
                final List<Relay.RequestMessage> messagesList = batchTuple.getFirst().buildRequest().getMessagesList();
                batchTuple.getSecond().complete(createResponse(messagesList, id -> id));
            } catch (Exception exception) {
                logger.error("Thread {}: Send function mock cannot create response for the given argument '{}'.",
                        Thread.currentThread().getName(), batchTuple, exception);
                // try our best to complete the future
                batchTuple.getSecond().completeExceptionally(exception);
            }
        })).when(batchSenderMock).sendBatch(any(Tuple.class));
    }

    private void mockSendFunctionToReturnOkResponse() throws Exception {
        mockSendFunctionToInvokeCallbackAndThenReturnOkResponse(() -> {
        });
    }

    private static Relay.PublishResponse createResponse(List<Relay.RequestMessage> requestMessages,
            IntFunction<Integer> messageIdMapper) {
        return Relay.PublishResponse.newBuilder()
                .addAllResponseMessages(requestMessages.stream()
                        .map(requestMessage -> Relay.ResponseMessage.newBuilder()
                                .setId(messageIdMapper.apply(requestMessage.getId()))
                                .build())
                        .collect(Collectors.toList()))
                .build();
    }

    private CyclicBarrier mockSendFunctionToBlock(Runnable commandToRunWhenUnblocked) throws InterruptedException {
        final CyclicBarrier sendFunctionBlocker = new CyclicBarrier(Integer.MAX_VALUE);
        mockSendFunctionToInvokeCallbackAndThenReturnOkResponse(() -> {
            try {
                logger.debug("Thread {}: Send function mock is calling CyclicBarrier.await().",
                        Thread.currentThread().getName());
                synchronized (sendFunctionBlocker) {
                    sendFunctionBlocker.notifyAll();
                }
                sendFunctionBlocker.await();
            } catch (BrokenBarrierException e) {
                // expected due to sendFunctionBlocker.reset()
            }
            commandToRunWhenUnblocked.run();
        });
        return sendFunctionBlocker;
    }
}
