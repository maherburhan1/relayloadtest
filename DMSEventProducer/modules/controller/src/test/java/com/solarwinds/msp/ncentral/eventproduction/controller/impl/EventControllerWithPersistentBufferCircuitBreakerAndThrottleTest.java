package com.solarwinds.msp.ncentral.eventproduction.controller.impl;

import com.google.protobuf.MessageLite;

import com.solarwinds.msp.ncentral.eventproduction.api.service.persistence.TimestampedEvent;
import com.solarwinds.msp.ncentral.eventproduction.api.service.publisher.PublishingContext;
import com.solarwinds.msp.ncentral.eventproduction.api.service.statistics.EventStatistics;
import com.solarwinds.msp.ncentral.eventproduction.api.service.tracking.ComponentStatusService;
import com.solarwinds.msp.ncentral.eventproduction.api.service.tracking.EventingControlService;
import com.solarwinds.msp.ncentral.eventproduction.controller.EventBufferController;
import com.solarwinds.msp.ncentral.eventproduction.controller.EventControllerConfiguration;
import com.solarwinds.util.BooleanWaitingObserver;
import com.solarwinds.util.concurrent.InterruptibleRunnable;
import com.solarwinds.util.function.Condition;
import com.solarwinds.util.function.ConditionWaitParameters;

import org.assertj.core.groups.Tuple;
import org.jooq.lambda.Unchecked;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.BooleanSupplier;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EventControllerWithPersistentBufferCircuitBreakerAndThrottleTest<EVENT extends TimestampedEvent<MessageLite>> {

    private static final ZonedDateTime TIME_STAMP = ZonedDateTime.now(ZoneOffset.UTC);

    private static final int MAXIMUM_EVENT_RETRIES_COUNT_ON_ACKNOWLEDGE_ERROR = 5;

    private static final String BUSINESS_APPLICATIONS_CUSTOMER_ID = "SomeId";
    private static final String SYSTEM_GUID = "SomeGuid";
    private static final String ENTITY_TYPE = "SomeType";
    private static final int EVENTING_CONFIGURATION_CUSTOMER_1 = 1;

    private static final ConditionWaitParameters CONDITION_WAIT_PARAMETERS = ConditionWaitParameters.newBuilder()
            .withPauseBetweenRetries(Duration.ofMillis(20L))
            .withMaximumWait(Duration.ofSeconds(2L))
            .build();

    private PublishingContext publishingContext;

    @Mock
    private EventBufferController<MessageLite> eventBufferControllerMock;
    private final BlockingDeque<EVENT> dequeForEventBufferControllerMock = new LinkedBlockingDeque<>();
    private final AtomicBoolean isEventBufferControllerMockInDequeueCall = new AtomicBoolean(false);
    @Mock
    private CircuitBreaker circuitBreakerMock;
    @Mock
    private Throttle throttleMock;
    @Mock
    private EventSender<MessageLite> eventSenderMock;
    @Mock
    private AcknowledgementController acknowledgementControllerMock;
    @Mock
    private EventSender<MessageLite>.Request requestMock;
    @Mock
    private EventFailuresMonitor eventFailuresMonitorMock;
    @Spy
    private EventingControlService eventingControlServiceSpy;
    @Mock
    private EventStatistics eventStatisticsMock;
    @Mock
    private ComponentStatusService componentStatusServiceMock;
    @Mock
    private EventControllerConfiguration eventControllerConfigurationMock;

    private BooleanWaitingObserver controllerStateObserver;

    private EventControllerWithPersistentBufferCircuitBreakerAndThrottle<MessageLite> eventController;

    @Mock
    private MessageLite messageLiteMock1;
    @Mock
    private MessageLite messageLiteMock2;
    @Captor
    private ArgumentCaptor<EVENT> timestampedEventArgumentCaptor;
    @Captor
    private ArgumentCaptor<InterruptibleRunnable> circuitBreakerExecuteArgumentCaptor;
    @Mock
    private CircuitBreaker.Result resultMock;
    @Captor
    private ArgumentCaptor<Runnable> resultOnFailureArgumentCaptor;
    @Captor
    private ArgumentCaptor<Runnable> requestOnAcknowledgeFailureArgumentCaptor;

    @BeforeEach
    void setUp() {
        eventController = new EventControllerWithPersistentBufferCircuitBreakerAndThrottle<>(eventBufferControllerMock,
                circuitBreakerMock, throttleMock, eventSenderMock, acknowledgementControllerMock,
                eventFailuresMonitorMock, eventingControlServiceSpy, eventStatisticsMock, componentStatusServiceMock,
                eventControllerConfigurationMock);
        verify(eventingControlServiceSpy).addStartupListenerOrExecuteStartup(eventController);
        when(eventControllerConfigurationMock.getMaximumEventRetriesCountOnAcknowledgementError()).thenReturn(
                OptionalInt.of(MAXIMUM_EVENT_RETRIES_COUNT_ON_ACKNOWLEDGE_ERROR));
        eventController.onEventingStart();
        publishingContext = PublishingContext.builder()
                .withBizappsCustomerId(BUSINESS_APPLICATIONS_CUSTOMER_ID)
                .withSystemGuid(SYSTEM_GUID)
                .withEntityType(ENTITY_TYPE)
                .withEventingConfigurationCustomerId(EVENTING_CONFIGURATION_CUSTOMER_1)
                .build();
    }

    @AfterEach
    void tearDown() {
        stopEventController();
    }

    @Test
    void eventController_observes_eventingStatusTracingService() {
        verify(eventingControlServiceSpy).addObserver(same(eventController));
    }

    @Test
    void publishEvents_adds_events_into_buffer() {
        List<MessageLite> events = Arrays.asList(messageLiteMock1, messageLiteMock2);
        eventController.publishEvents(events, TIME_STAMP, publishingContext);

        verify(eventBufferControllerMock, times(events.size())).enqueue(timestampedEventArgumentCaptor.capture());
        verifyThat_expectedEvents_wereCaptured(events, timestampedEventArgumentCaptor);
    }

    @Test
    void acknowledgeController_is_started_when_update_is_called_and_eventSending_is_enabled()
            throws TimeoutException, InterruptedException {
        startEventControllerAndWaitUntilItStartsUp();
        verify(eventFailuresMonitorMock).resetAll();
    }

    private void startEventControllerAndWaitUntilItStartsUp() throws TimeoutException, InterruptedException {
        createAndRegisterControllerStateObserver();
        startEventController();
        controllerStateObserver.waitUntilBecomes(true, CONDITION_WAIT_PARAMETERS);
    }

    private void startEventController() {
        when(eventingControlServiceSpy.getGlobalSendingEnabled()).thenReturn(true);
        eventController.update(null, null);
    }

    private void createAndRegisterControllerStateObserver() {
        controllerStateObserver = new BooleanWaitingObserver();
        doAnswer(invocation -> {
            controllerStateObserver.update(null, invocation.getArguments()[1]);
            return null;
        }).when(componentStatusServiceMock).setRunning(any(), anyBoolean());
    }

    @Test
    void eventFailuresMonitor_is_reset_when_update_is_called_and_eventSending_is_enabled()
            throws TimeoutException, InterruptedException {
        startEventControllerAndWaitUntilItStartsUp();
        verify(eventFailuresMonitorMock).resetAll();
    }

    @Test
    void componentStatusService_setRunning_is_invoked_with_true_when_update_is_called_and_eventSending_is_enabled()
            throws TimeoutException, InterruptedException {
        startEventControllerAndWaitUntilItStartsUp();
        verify(componentStatusServiceMock).setRunning(
                eq(EventControllerWithPersistentBufferCircuitBreakerAndThrottle.class), eq(true));
    }

    @Test
    void acknowledgeController_is_closed_when_update_is_called_and_eventSending_is_disabled()
            throws TimeoutException, InterruptedException {
        startEventControllerAndWaitUntilItStartsUp();
        stopEventControllerAndWaitUntilItStops();

        verify(acknowledgementControllerMock).close();
    }

    @Test
    void eventBufferController_is_closed_when_update_is_called_and_eventSending_is_disabled()
            throws TimeoutException, InterruptedException {
        startEventControllerAndWaitUntilItStartsUp();
        stopEventControllerAndWaitUntilItStops();

        verify(eventBufferControllerMock).close();
    }

    private void stopEventControllerAndWaitUntilItStops() throws TimeoutException, InterruptedException {
        createAndRegisterControllerStateObserver();
        stopEventController();
        controllerStateObserver.waitUntilBecomes(false, CONDITION_WAIT_PARAMETERS);
    }

    private void stopEventController() {
        when(eventingControlServiceSpy.getGlobalSendingEnabled()).thenReturn(false);
        eventController.update(null, null);
    }

    @Test
    void componentStatusService_setRunning_is_invoked_with_false_when_update_is_called_and_eventSending_is_enabled()
            throws TimeoutException, InterruptedException {
        startEventControllerAndWaitUntilItStartsUp();
        stopEventControllerAndWaitUntilItStops();

        verify(componentStatusServiceMock).setRunning(
                eq(EventControllerWithPersistentBufferCircuitBreakerAndThrottle.class), eq(false));
    }

    @Test
    void events_are_sent_through_circuitBreaker_when_throttle_is_open() throws Exception {
        mock_eventBufferControllerMock_addLast();
        mock_eventBufferControllerMock_takeFirst();

        List<MessageLite> events = Arrays.asList(messageLiteMock1, messageLiteMock2);
        eventController.publishEvents(events, TIME_STAMP, publishingContext);
        openThrottleAndCloseCircuitBreaker();

        startEventControllerAndWaitUntilAllEventsAreProcessedOrControllerStops();
        stopEventControllerAndWaitUntilItStops();

        verify(throttleMock, times(events.size() + 1)).isClosed();
        verifyNoMoreInteractions(throttleMock);
        verify(eventBufferControllerMock, times(events.size() + 1)).dequeue();
        verifyNoInteractions(eventSenderMock);

        verifyThat_circuitBreakerExecute_wasCalled_forAllEvents(events);
    }

    private void openThrottleAndCloseCircuitBreaker() throws Exception {
        when(throttleMock.isClosed()).thenReturn(false);
        openThrottleAndCloseCircuitBreakerWithoutMockingThrottleIsClosed();
    }

    private void openThrottleAndCloseCircuitBreakerWithoutMockingThrottleIsClosed() throws Exception {
        when(circuitBreakerMock.execute(any())).thenReturn(resultMock);
        when(eventSenderMock.forEvent(any())).thenReturn(requestMock);
        when(requestMock.onAcknowledgeFailure(any())).thenReturn(requestMock);
    }

    private void startEventControllerAndWaitUntilAllEventsAreProcessedOrControllerStops()
            throws InterruptedException, TimeoutException {
        startEventControllerAndWaitUntilItStartsUp();
        waitUntilAllEventsAreProcessedOrControllerStops();
    }

    private void waitUntilAllEventsAreProcessedOrControllerStops() throws InterruptedException, TimeoutException {
        BooleanSupplier areAllEventsProcessedOrControllerWasStopped =
                () -> (dequeForEventBufferControllerMock.isEmpty() && isEventBufferControllerMockInDequeueCall.get())
                        || (controllerStateObserver.getValue().isPresent() && (!controllerStateObserver.getValue()
                        .get()));
        new Condition(areAllEventsProcessedOrControllerWasStopped, CONDITION_WAIT_PARAMETERS).await();
    }

    private void verifyThat_circuitBreakerExecute_wasCalled_forAllEvents(List<MessageLite> events)
            throws InterruptedException {
        verify(circuitBreakerMock, times(events.size())).execute(circuitBreakerExecuteArgumentCaptor.capture());

        circuitBreakerExecuteArgumentCaptor.getAllValues().forEach(Unchecked.consumer(InterruptibleRunnable::run));
        verify(eventSenderMock, times(events.size())).forEvent(timestampedEventArgumentCaptor.capture());
        verifyThat_expectedEvents_wereCaptured(events, timestampedEventArgumentCaptor);
        verify(requestMock, times(events.size())).send();
    }

    private void verifyThat_circuitBreakerExecute_wasCalled_forAllEvents_n_times(List<MessageLite> events,
            int invocationCount) throws InterruptedException {
        verify(circuitBreakerMock, times(events.size() * invocationCount)).execute(
                circuitBreakerExecuteArgumentCaptor.capture());

        circuitBreakerExecuteArgumentCaptor.getAllValues().forEach(Unchecked.consumer(InterruptibleRunnable::run));
        verify(eventSenderMock, times(events.size() * invocationCount)).forEvent(
                timestampedEventArgumentCaptor.capture());
        verify(requestMock, times(events.size() * invocationCount)).send();
        List<MessageLite> capturedEvents = new ArrayList<>(events);
        for (int i = 1; i < invocationCount; i++) {
            capturedEvents.addAll(events);
        }
        verifyThat_expectedEvents_wereCaptured(capturedEvents, timestampedEventArgumentCaptor);
    }

    @Test
    void controller_stops_when_circuitBreaker_throws_InterruptedException()
            throws InterruptedException, TimeoutException {
        mock_eventBufferControllerMock_addLast();
        mock_eventBufferControllerMock_takeFirst();
        when(throttleMock.isClosed()).thenReturn(false);
        when(circuitBreakerMock.execute(any())).thenThrow(new InterruptedException());

        List<MessageLite> events = Arrays.asList(messageLiteMock1, messageLiteMock2);
        eventController.publishEvents(events, TIME_STAMP, publishingContext);

        createAndRegisterControllerStateObserver();
        startEventController();
        controllerStateObserver.waitUntilBecomes(false, CONDITION_WAIT_PARAMETERS);

        verify(throttleMock).isClosed();
        verifyNoMoreInteractions(throttleMock);
        verify(eventBufferControllerMock).dequeue();
        verify(circuitBreakerMock).execute(any());
        verifyNoInteractions(eventSenderMock);
    }

    @Test
    void event_is_added_back_to_buffer_when_send_fails() throws Exception {
        mock_eventBufferControllerMock_addFirst();
        mock_eventBufferControllerMock_addLast();
        mock_eventBufferControllerMock_takeFirst();

        List<MessageLite> events = Collections.singletonList(messageLiteMock1);
        eventController.publishEvents(events, TIME_STAMP, publishingContext);
        openThrottleAndCloseCircuitBreaker();

        startEventControllerAndWaitUntilAllEventsAreProcessedOrControllerStops();
        stopEventControllerAndWaitUntilItStops();

        verifyThat_circuitBreakerExecute_wasCalled_forAllEvents(events);

        verify(resultMock).onFailure(resultOnFailureArgumentCaptor.capture());
        resultOnFailureArgumentCaptor.getAllValues().forEach(Runnable::run);
        verify(eventFailuresMonitorMock).processFailure(EVENTING_CONFIGURATION_CUSTOMER_1);
        verifyThat_expectedEvents_wereCaptured(events, timestampedEventArgumentCaptor);
    }

    @Test
    void event_is_added_back_to_buffer_when_acknowledge_fails() throws Exception {
        mock_eventBufferControllerMock_addFirst();
        mock_eventBufferControllerMock_addLast();
        mock_eventBufferControllerMock_takeFirst();

        List<MessageLite> events = Collections.singletonList(messageLiteMock1);
        eventController.publishEvents(events, TIME_STAMP, publishingContext);
        openThrottleAndCloseCircuitBreaker();

        startEventControllerAndWaitUntilAllEventsAreProcessedOrControllerStops();
        stopEventControllerAndWaitUntilItStops();

        verifyThat_circuitBreakerExecute_wasCalled_forAllEvents(events);

        verify(requestMock).onAcknowledgeFailure(requestOnAcknowledgeFailureArgumentCaptor.capture());
        requestOnAcknowledgeFailureArgumentCaptor.getAllValues().forEach(Runnable::run);
        verify(eventFailuresMonitorMock).processFailure(EVENTING_CONFIGURATION_CUSTOMER_1);
        verifyThat_expectedEvents_wereCaptured(events, timestampedEventArgumentCaptor);
    }

    @Test
    void event_is_not_added_back_to_buffer_when_maximum_acknowledge_failures_count_is_reached() throws Exception {
        mock_eventBufferControllerMock_addFirst();
        mock_eventBufferControllerMock_addLast();
        mock_eventBufferControllerMock_takeFirst();

        List<MessageLite> events = Collections.singletonList(messageLiteMock1);
        eventController.publishEvents(events, TIME_STAMP, publishingContext);
        openThrottleAndCloseCircuitBreaker();

        startEventControllerAndWaitUntilAllEventsAreProcessedOrControllerStops();
        for (int retryCount = 0; retryCount <= MAXIMUM_EVENT_RETRIES_COUNT_ON_ACKNOWLEDGE_ERROR + 5; retryCount++) {
            verify(circuitBreakerMock, atLeastOnce()).execute(circuitBreakerExecuteArgumentCaptor.capture());
            circuitBreakerExecuteArgumentCaptor.getValue().run();
            verify(requestMock, atLeastOnce()).onAcknowledgeFailure(
                    requestOnAcknowledgeFailureArgumentCaptor.capture());

            requestOnAcknowledgeFailureArgumentCaptor.getValue().run();
            waitUntilAllEventsAreProcessedOrControllerStops();
        }
        stopEventControllerAndWaitUntilItStops();

        verify(eventBufferControllerMock, times(MAXIMUM_EVENT_RETRIES_COUNT_ON_ACKNOWLEDGE_ERROR)).addFirst(
                timestampedEventArgumentCaptor.capture());
        verifyThat_expectedEvents_wereCaptured(nCopies(MAXIMUM_EVENT_RETRIES_COUNT_ON_ACKNOWLEDGE_ERROR, events),
                timestampedEventArgumentCaptor);
    }

    private static <T> List<T> nCopies(int count, List<T> list) {
        return Stream.generate(() -> list).limit(count).flatMap(Collection::stream).collect(Collectors.toList());
    }

    @Test
    void waitUntilOpen_is_called_when_throttle_is_closed() throws InterruptedException, TimeoutException {
        AtomicBoolean isThrottleClosed = new AtomicBoolean(true);
        when(throttleMock.isClosed()).thenAnswer(p -> isThrottleClosed.get());
        doAnswer(p -> isThrottleClosed.getAndSet(false)).when(throttleMock).waitUntilOpen();

        mock_eventBufferControllerMock_takeFirst();

        startEventControllerAndWaitUntilAllEventsAreProcessedOrControllerStops();
        stopEventControllerAndWaitUntilItStops();

        verify(throttleMock, times(2)).isClosed();
        verify(throttleMock).waitUntilOpen();
        verify(eventBufferControllerMock).dequeue();
        verifyNoInteractions(eventSenderMock);
        verifyNoInteractions(circuitBreakerMock);
    }

    @Test
    void processing_is_stopped_when_throttleWaitUntilOpen_throws_InterruptedException()
            throws InterruptedException, TimeoutException {
        when(throttleMock.isClosed()).thenReturn(true);
        doThrow(new InterruptedException()).when(throttleMock).waitUntilOpen();

        createAndRegisterControllerStateObserver();
        startEventController();
        controllerStateObserver.waitUntilBecomes(false, CONDITION_WAIT_PARAMETERS);

        verify(throttleMock).isClosed();
        verify(throttleMock).waitUntilOpen();
    }

    @Test
    void processing_is_stopped_when_bufferDequeue_throws_InterruptedException()
            throws InterruptedException, TimeoutException {
        when(throttleMock.isClosed()).thenReturn(false);
        when(eventBufferControllerMock.dequeue()).thenThrow(new InterruptedException());

        createAndRegisterControllerStateObserver();
        startEventController();
        controllerStateObserver.waitUntilBecomes(false, CONDITION_WAIT_PARAMETERS);

        verify(throttleMock).isClosed();
        verify(eventBufferControllerMock).dequeue();
    }

    @Test
    void directSendEvent_not_added_to_buffer() throws InterruptedException {
        mock_direct_send_publishingContext();
        when(circuitBreakerMock.execute(any())).thenReturn(resultMock);

        List<MessageLite> events = Collections.singletonList(messageLiteMock1);
        eventController.publishEvents(events, TIME_STAMP, publishingContext);

        verify(throttleMock, times(events.size())).waitUntilOpen();
        verify(eventBufferControllerMock, never()).enqueue(any());
    }

    @Test
    void direct_events_are_sent_through_circuitBreaker_when_throttle_is_open() throws Exception {
        mock_direct_send_publishingContext();

        List<MessageLite> events = Arrays.asList(messageLiteMock1, messageLiteMock2);
        openThrottleAndCloseCircuitBreakerWithoutMockingThrottleIsClosed();
        eventController.publishEvents(events, TIME_STAMP, publishingContext);

        verify(throttleMock, times(events.size())).waitUntilOpen();
        verifyNoMoreInteractions(throttleMock);
        verify(eventBufferControllerMock, never()).dequeue();
        verifyNoInteractions(eventSenderMock);

        verifyThat_circuitBreakerExecute_wasCalled_forAllEvents(events);
    }

    @Test
    void direct_events_are_resent_directly_on_send_failure() throws Exception {
        mock_direct_send_publishingContext();

        List<MessageLite> events = Collections.singletonList(messageLiteMock1);
        openThrottleAndCloseCircuitBreakerWithoutMockingThrottleIsClosed();
        when(resultMock.callFailed()).thenReturn(true);
        eventController.publishEvents(events, TIME_STAMP, publishingContext);

        verify(throttleMock,
                times(1 + events.size() * MAXIMUM_EVENT_RETRIES_COUNT_ON_ACKNOWLEDGE_ERROR)).waitUntilOpen();
        // after initial send fails we will retry given number of times
        verify(resultMock, times(1 + MAXIMUM_EVENT_RETRIES_COUNT_ON_ACKNOWLEDGE_ERROR)).callFailed();
        resultOnFailureArgumentCaptor.getAllValues().forEach(Runnable::run);

        verifyThat_circuitBreakerExecute_wasCalled_forAllEvents_n_times(events, 6);
        verify(circuitBreakerMock, times(1 + MAXIMUM_EVENT_RETRIES_COUNT_ON_ACKNOWLEDGE_ERROR)).execute(any());
    }

    @Test
    void direct_events_are_resent_directly_on_acknowledgement_failure() throws Exception {
        mock_direct_send_publishingContext();

        List<MessageLite> events = Collections.singletonList(messageLiteMock1);
        openThrottleAndCloseCircuitBreakerWithoutMockingThrottleIsClosed();

        eventController.publishEvents(events, TIME_STAMP, publishingContext);

        verify(throttleMock, times(events.size())).waitUntilOpen();
        verifyThat_circuitBreakerExecute_wasCalled_forAllEvents(events);

        verify(requestMock).onAcknowledgeFailure(requestOnAcknowledgeFailureArgumentCaptor.capture());
        requestOnAcknowledgeFailureArgumentCaptor.getAllValues().forEach(Runnable::run);
        verifyThat_expectedEvents_wereCaptured(events, timestampedEventArgumentCaptor);
        verify(circuitBreakerMock, times(2)).execute(any());
    }

    private void mock_direct_send_publishingContext() {
        publishingContext = PublishingContext.builder()
                .withBizappsCustomerId(BUSINESS_APPLICATIONS_CUSTOMER_ID)
                .withSystemGuid(SYSTEM_GUID)
                .withEntityType(ENTITY_TYPE)
                .withEventingConfigurationCustomerId(EVENTING_CONFIGURATION_CUSTOMER_1)
                .withSkipBuffer(true)
                .build();
    }

    private void mock_eventBufferControllerMock_addFirst() {
        doAnswer(p -> {
            dequeForEventBufferControllerMock.addFirst(p.getArgument(0));
            return null;
        }).when(eventBufferControllerMock).addFirst(any());
    }

    private void mock_eventBufferControllerMock_addLast() {
        doAnswer(p -> {
            dequeForEventBufferControllerMock.addLast(p.getArgument(0));
            return null;
        }).when(eventBufferControllerMock).enqueue(any());
    }

    private void mock_eventBufferControllerMock_takeFirst() throws InterruptedException {
        Lock dequeueLock = new ReentrantLock();
        when(eventBufferControllerMock.dequeue()).thenAnswer(invocationOnMock -> {
            dequeueLock.lock();
            isEventBufferControllerMockInDequeueCall.set(true);
            try {
                return Optional.of(dequeForEventBufferControllerMock.takeFirst());
            } finally {
                isEventBufferControllerMockInDequeueCall.set(false);
                dequeueLock.unlock();
            }
        });
    }

    private void verifyThat_expectedEvents_wereCaptured(List<MessageLite> expectedEvents,
            ArgumentCaptor<EVENT> eventArgumentCaptor) {
        verifyThat_expectedEvents_wereCaptured(expectedEvents, eventArgumentCaptor, e -> e);
    }

    private <T> void verifyThat_expectedEvents_wereCaptured(List<MessageLite> expectedEvents,
            ArgumentCaptor<T> timestampedEventArgumentCaptor, Function<T, EVENT> toEventMapper) {
        assertThat(timestampedEventArgumentCaptor.getAllValues()).extracting(toEventMapper)
                .extracting(event -> tuple(event.getEvent(), event.getTimestamp(), event.getPublishingContext()))
                .containsExactly(expectedEvents.stream()
                        .map(event -> tuple(event, TIME_STAMP, publishingContext))
                        .toArray(Tuple[]::new));
    }
}
