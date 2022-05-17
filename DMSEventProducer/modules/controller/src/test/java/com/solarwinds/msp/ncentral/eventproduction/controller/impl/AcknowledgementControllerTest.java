package com.solarwinds.msp.ncentral.eventproduction.controller.impl;

import com.google.protobuf.MessageLite;

import com.solarwinds.msp.ncentral.eventproduction.api.entity.EventHighWaterMark;
import com.solarwinds.msp.ncentral.eventproduction.api.service.failure.FailureNotificationService;
import com.solarwinds.msp.ncentral.eventproduction.api.service.publisher.CircuitBreaker;
import com.solarwinds.msp.ncentral.eventproduction.api.service.publisher.PublishedEventInfo;
import com.solarwinds.msp.ncentral.eventproduction.api.service.publisher.PublishingContext;
import com.solarwinds.msp.ncentral.eventproduction.api.service.statistics.EventStatistics;
import com.solarwinds.msp.ncentral.eventproduction.api.service.tracking.ComponentStatusService;
import com.solarwinds.msp.ncentral.eventproduction.api.service.tracking.EventTracker;
import com.solarwinds.msp.ncentral.eventproduction.api.service.tracking.EventingConfigurationChange;
import com.solarwinds.msp.ncentral.eventproduction.api.service.tracking.EventingControlService;
import com.solarwinds.msp.ncentral.eventproduction.controller.EventControllerConfiguration;
import com.solarwinds.msp.ncentral.eventproduction.controller.EventWithFuture;
import com.solarwinds.util.BooleanWaitingObserver;
import com.solarwinds.util.IntegerWaitingObserver;
import com.solarwinds.util.function.ConditionWaitParameters;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.rmi.RemoteException;
import java.time.Duration;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Observable;
import java.util.Observer;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AcknowledgementControllerTest {

    private static final Duration MAXIMUM_WAIT_TIME_AFTER_ERROR = Duration.ofMillis(50L);
    private static final Duration WAIT_TIME_FOR_RESPONSE_AFTER_SENDING = Duration.ofMillis(50L);
    private static final String ENTITY_TYPE = "TEST_ENTITY";
    private static final int CUSTOMER_ID_1 = 100;
    private static final int CUSTOMER_ID_2 = 200;
    private static final String BIZAPPS_CUSTOMER_ID = "BIZAPPS-CUSTOMER-1";
    private static final ZonedDateTime TIME_STAMP = ZonedDateTime.now(ZoneOffset.UTC);

    private static final ConditionWaitParameters CONDITION_WAIT_PARAMETERS = ConditionWaitParameters.newBuilder()
            .withPauseBetweenRetries(Duration.ofMillis(20L))
            .withMaximumWait(Duration.ofSeconds(10L))
            .build();

    @Mock
    private CircuitBreaker circuitBreakerMock;
    @Mock
    private EventControllerConfiguration eventControllerConfigurationMock;
    @Mock
    private EventingControlService eventingControlServiceMock;
    @Mock
    private EventTracker eventTrackerMock;
    @Mock
    private EventFailuresMonitor eventFailuresMonitorMock;
    @Mock
    private EventStatistics eventStatisticsMock;
    @Mock
    private ComponentStatusService componentStatusServiceMock;
    @Mock
    private FailureNotificationService failureNotificationServiceMock;
    @InjectMocks
    private AcknowledgementController acknowledgementController;

    private BooleanWaitingObserver controllerStateObserver;

    @Mock
    private Observer observerMock1;
    @Mock
    private Observer observerMock2;
    @Captor
    private ArgumentCaptor<EventHighWaterMark> eventHighWaterMarkArgumentCaptor;

    @BeforeEach
    void setup() {
        when(eventControllerConfigurationMock.getMaximumWaitTimeAfterError()).thenReturn(
                Optional.of(MAXIMUM_WAIT_TIME_AFTER_ERROR));
        when(eventControllerConfigurationMock.getWaitTimeForResponseAfterSending()).thenReturn(
                Optional.of(WAIT_TIME_FOR_RESPONSE_AFTER_SENDING));
        acknowledgementController.onEventingStart();
    }

    @Test
    public void constructor_adds_itself_as_EventingControlService_observer() {
        verify(eventingControlServiceMock).addObserver(acknowledgementController);
    }

    @Test
    public void constructor_adds_itself_as_EventingControlService_eventingStartupListener() {
        verify(eventingControlServiceMock).addStartupListenerOrExecuteStartup(acknowledgementController);
    }

    @Test
    void observers_are_called_with_pendingEventsCount_after_acknowledgeEvent_is_called() throws InterruptedException {
        acknowledgementController.addObserver(observerMock1);
        acknowledgementController.addObserver(observerMock2);

        final EventWithFuture eventWithFuture = mock(EventWithFuture.class);
        when(eventWithFuture.getPublishingContext()).thenReturn(mock(PublishingContext.class));

        for (int eventsCount = 1; eventsCount <= 10; eventsCount++) {
            acknowledgementController.acknowledgeEvent(eventWithFuture, mock(Runnable.class));

            verifyObserverInvocation(observerMock1, eventsCount);
            verifyObserverInvocation(observerMock2, eventsCount);
        }
    }

    private void verifyObserverInvocation(Observer observer, int pendingEventsCount) {
        verify(observer).update(same(acknowledgementController), eq(pendingEventsCount));
    }

    @Test
    void componentStatusService_setRunning_is_invoked_with_true_after_start_is_called()
            throws TimeoutException, InterruptedException {
        startControllerAndWaitUntilItStartsUp();
        verify(componentStatusServiceMock).setRunning(eq(AcknowledgementController.class), eq(true));
    }

    private void startControllerAndWaitUntilItStartsUp() throws InterruptedException, TimeoutException {
        startController();
        controllerStateObserver.waitUntilBecomes(true, CONDITION_WAIT_PARAMETERS);
    }

    private void startController() {
        createAndRegisterControllerStateObserver();
        acknowledgementController.start();
    }

    private void createAndRegisterControllerStateObserver() {
        controllerStateObserver = new BooleanWaitingObserver();
        doAnswer(invocation -> {
            controllerStateObserver.update(null, invocation.getArguments()[1]);
            return null;
        }).when(componentStatusServiceMock).setRunning(any(), anyBoolean());
    }

    @Test
    void eventingStatusTrackingService_setAcknowledgementControllerRunning_is_invoked_with_false_after_close_is_called()
            throws TimeoutException, InterruptedException {
        startControllerAndWaitUntilItStartsUp();
        closeController();
        verify(componentStatusServiceMock).setRunning(eq(AcknowledgementController.class), eq(false));
    }

    private void closeController() throws InterruptedException, TimeoutException {
        acknowledgementController.close();
        controllerStateObserver.waitUntilBecomes(false, CONDITION_WAIT_PARAMETERS);
    }

    @Test
    void observers_are_called_with_pendingEventsCount_after_event_is_acknowledged()
            throws InterruptedException, TimeoutException, ExecutionException {
        final int numberOfEvents = 10;
        for (int eventsCount = 1; eventsCount <= numberOfEvents; eventsCount++) {
            acknowledgementController.acknowledgeEvent(createEventWithFutureWithPublishingSuccess(),
                    mock(Runnable.class));
        }

        acknowledgementController.addObserver(observerMock1);
        acknowledgementController.addObserver(observerMock2);

        startControllerAndProcessAcknowledgementsAndStopIt();

        for (int eventsCount = numberOfEvents - 1; eventsCount >= 0; eventsCount--) {
            verifyObserverInvocation(observerMock1, eventsCount);
            verifyObserverInvocation(observerMock2, eventsCount);
        }
        verify(circuitBreakerMock, times(numberOfEvents)).handleSuccess();
    }

    @Test
    void observers_are_called_when_acknowledgementController_is_restarted()
            throws InterruptedException, TimeoutException, ExecutionException {
        final int numberOfEvents = 10;
        for (int eventsCount = 1; eventsCount <= numberOfEvents; eventsCount++) {
            acknowledgementController.acknowledgeEvent(createEventWithFutureWithPublishingSuccess(),
                    mock(Runnable.class));
        }
        startControllerAndProcessAcknowledgementsAndStopIt();

        for (int eventsCount = 1; eventsCount <= numberOfEvents; eventsCount++) {
            acknowledgementController.acknowledgeEvent(createEventWithFutureWithPublishingSuccess(),
                    mock(Runnable.class));
        }
        acknowledgementController.addObserver(observerMock1);
        acknowledgementController.addObserver(observerMock2);
        startControllerAndProcessAcknowledgementsAndStopIt();

        for (int eventsCount = numberOfEvents - 1; eventsCount >= 0; eventsCount--) {
            verifyObserverInvocation(observerMock1, eventsCount);
            verifyObserverInvocation(observerMock2, eventsCount);
        }
        verify(circuitBreakerMock, times(numberOfEvents * 2)).handleSuccess();
    }

    private void startControllerAndProcessAcknowledgementsAndStopIt() throws InterruptedException, TimeoutException {
        IntegerWaitingObserver pendingAcknowledgementsCount = new IntegerWaitingObserver();
        acknowledgementController.addObserver(pendingAcknowledgementsCount);

        startControllerAndWaitUntilItStartsUp();
        pendingAcknowledgementsCount.waitUntilBecomes(0, CONDITION_WAIT_PARAMETERS);
        closeController();

        acknowledgementController.deleteObserver(pendingAcknowledgementsCount);
    }

    private static EventWithFuture createEventWithFutureWithPublishingSuccess()
            throws InterruptedException, ExecutionException, TimeoutException {
        PublishedEventInfo publishedEventInfoMock = mockPublishedEventInfo(true);
        EventWithFuture eventWithFutureMock = createEventWithFutureMock(publishedEventInfoMock);
        return mockEventWithFutureWithPublishingSuccessData(eventWithFutureMock);
    }

    private static EventWithFuture createEventWithFutureWithPublishingSuccess(
            Future<PublishedEventInfo> publishedEventInfoFuture) {
        EventWithFuture eventWithFutureMock = createEventWithFutureMock(publishedEventInfoFuture);
        return mockEventWithFutureWithPublishingSuccessData(eventWithFutureMock);
    }

    private static EventWithFuture mockEventWithFutureWithPublishingSuccessData(EventWithFuture eventWithFutureMock) {
        MessageLite messageLiteMock = mockProtobufMessage(eventWithFutureMock);
        when(messageLiteMock.toByteArray()).thenReturn(new byte[] {1, 2, 3});

        PublishingContext publishingContextMock = mockPublishingContext(eventWithFutureMock, CUSTOMER_ID_1);
        when(publishingContextMock.getEntityType()).thenReturn(ENTITY_TYPE);

        when(eventWithFutureMock.getTimestamp()).thenReturn(TIME_STAMP);

        return eventWithFutureMock;
    }

    private static PublishedEventInfo mockPublishedEventInfo(boolean isSuccess) {
        PublishedEventInfo publishedEventInfoMock = mock(PublishedEventInfo.class);
        when(publishedEventInfoMock.isSuccess()).thenReturn(isSuccess);
        return publishedEventInfoMock;
    }

    private static EventWithFuture createEventWithFutureMock()
            throws InterruptedException, ExecutionException, TimeoutException {
        return createEventWithFutureMock(mock(PublishedEventInfo.class));
    }

    private static EventWithFuture createEventWithFutureMock(PublishedEventInfo publishedEventInfo)
            throws InterruptedException, ExecutionException, TimeoutException {
        Future<PublishedEventInfo> publishedEventInfoFutureMock = mock(Future.class);
        when(publishedEventInfoFutureMock.get(anyLong(), any())).thenReturn(publishedEventInfo);
        EventWithFuture eventWithFutureMock = mock(EventWithFuture.class);
        when(eventWithFutureMock.getFuture()).thenReturn(publishedEventInfoFutureMock);
        return eventWithFutureMock;
    }

    private static EventWithFuture createEventWithFutureMock(Future<PublishedEventInfo> publishedEventInfoFuture) {
        EventWithFuture eventWithFutureMock = mock(EventWithFuture.class);
        when(eventWithFutureMock.getFuture()).thenReturn(publishedEventInfoFuture);
        return eventWithFutureMock;
    }

    private static MessageLite mockProtobufMessage(EventWithFuture eventWithFutureMock) {
        MessageLite messageLiteMock = mock(MessageLite.class);
        when(eventWithFutureMock.getEvent()).thenReturn(messageLiteMock);
        return messageLiteMock;
    }

    private static PublishingContext mockPublishingContext(EventWithFuture eventWithFutureMock, int customerId) {
        PublishingContext publishingContextMock = mock(PublishingContext.class);
        when(eventWithFutureMock.getPublishingContext()).thenReturn(publishingContextMock);
        when(publishingContextMock.getEventingConfigurationCustomerId()).thenReturn(customerId);
        return publishingContextMock;
    }

    @Test
    void controller_waits_certain_timeout_for_event_acknowledgement()
            throws InterruptedException, ExecutionException, TimeoutException {
        EventWithFuture eventWithFuture = createEventWithFutureWithPublishingSuccess();
        acknowledgementController.acknowledgeEvent(eventWithFuture, mock(Runnable.class));

        startControllerAndProcessAcknowledgementsAndStopIt();

        verify(eventWithFuture.getFuture()).get(
                eq(2 * (MAXIMUM_WAIT_TIME_AFTER_ERROR.toMillis() + WAIT_TIME_FOR_RESPONSE_AFTER_SENDING.toMillis())),
                eq(TimeUnit.MILLISECONDS));
        verify(circuitBreakerMock).handleSuccess();
    }

    @Test
    void eventTracker_is_called_after_event_acknowledgement()
            throws InterruptedException, ExecutionException, TimeoutException, RemoteException {
        acknowledgementController.acknowledgeEvent(createEventWithFutureWithPublishingSuccess(), mock(Runnable.class));

        startControllerAndProcessAcknowledgementsAndStopIt();

        verify(eventTrackerMock).trackEventTimestamp(eventHighWaterMarkArgumentCaptor.capture());
        assertThat(eventHighWaterMarkArgumentCaptor.getValue()).isNotNull();
        EventHighWaterMark highWaterMark = eventHighWaterMarkArgumentCaptor.getValue();
        assertThat(highWaterMark.getCustomerId()).isEqualTo(CUSTOMER_ID_1);
        assertThat(highWaterMark.getTableName()).isEqualTo(ENTITY_TYPE);
        assertThat(highWaterMark.getLastProcessed()).isNotNull();
        verify(circuitBreakerMock).handleSuccess();
    }

    @ParameterizedTest
    @MethodSource
    void controller_calls_failureCommand_on_event_acknowledgement_timeout_and_error(Throwable throwable)
            throws InterruptedException, ExecutionException, TimeoutException {
        Runnable onAcknowledgeFailureCommand = mock(Runnable.class);
        when(eventingControlServiceMock.isEventingEnabledForCustomer(CUSTOMER_ID_1)).thenReturn(true);
        EventWithFuture eventWithFutureMock = createEventWithFutureThrowingException(throwable, CUSTOMER_ID_1);

        acknowledgementController.acknowledgeEvent(eventWithFutureMock, onAcknowledgeFailureCommand);
        startControllerAndProcessAcknowledgementsAndStopIt();

        verify(circuitBreakerMock).handleFailure();
        verify(onAcknowledgeFailureCommand).run();
    }

    static Stream<Throwable> controller_calls_failureCommand_on_event_acknowledgement_timeout_and_error() {
        return Stream.of(new TimeoutException("Timeout"),
                new ExecutionException("ExecutionException", new RuntimeException()));
    }

    private static EventWithFuture createEventWithFutureThrowingException(Throwable throwable, int customerId)
            throws InterruptedException, ExecutionException, TimeoutException {
        EventWithFuture eventWithFutureMock = createEventWithFutureMock();
        when(eventWithFutureMock.getFuture().get(anyLong(), any())).thenThrow(throwable);

        mockProtobufMessage(eventWithFutureMock);

        mockPublishingContext(eventWithFutureMock, customerId);

        return eventWithFutureMock;
    }

    @Test
    void controller_calls_failureNotificationService_on_ExecutionException()
            throws InterruptedException, ExecutionException, TimeoutException {
        EventWithFuture eventWithFutureMock = createEventWithFutureMock();
        mockProtobufMessage(eventWithFutureMock);
        PublishingContext publishingContextMock = mockPublishingContext(eventWithFutureMock, CUSTOMER_ID_1);
        when(publishingContextMock.getBizappsCustomerId()).thenReturn(Optional.of(BIZAPPS_CUSTOMER_ID));
        RuntimeException expectedCauseException = new RuntimeException("Cause exception message");
        when(eventWithFutureMock.getFuture().get(anyLong(), any())).thenThrow(
                new ExecutionException("ExecutionException", expectedCauseException));
        Runnable onAcknowledgeFailureCommandMock = mock(Runnable.class);

        acknowledgementController.acknowledgeEvent(eventWithFutureMock, onAcknowledgeFailureCommandMock);
        startControllerAndProcessAcknowledgementsAndStopIt();

        verify(failureNotificationServiceMock).registerException(eq(BIZAPPS_CUSTOMER_ID), same(expectedCauseException));
    }

    @Test
    void controller_doesNotCall_failureNotificationService_on_ExecutionExceptionWithoutCause()
            throws InterruptedException, ExecutionException, TimeoutException {
        EventWithFuture eventWithFutureMock = createEventWithFutureMock();
        mockProtobufMessage(eventWithFutureMock);
        mockPublishingContext(eventWithFutureMock, CUSTOMER_ID_1);
        when(eventWithFutureMock.getFuture().get(anyLong(), any())).thenThrow(
                new ExecutionException("ExecutionException", null));
        Runnable onAcknowledgeFailureCommandMock = mock(Runnable.class);

        acknowledgementController.acknowledgeEvent(eventWithFutureMock, onAcknowledgeFailureCommandMock);
        startControllerAndProcessAcknowledgementsAndStopIt();

        verifyNoInteractions(failureNotificationServiceMock);
    }

    @Test
    void controller_calls_failureCommand_for_publishedEventInfo_with_failure()
            throws InterruptedException, ExecutionException, TimeoutException {
        Runnable onAcknowledgeFailureCommandMock = mock(Runnable.class);
        when(eventingControlServiceMock.isEventingEnabledForCustomer(CUSTOMER_ID_1)).thenReturn(true);

        acknowledgementController.acknowledgeEvent(createEventWithFutureWithPublishingFailure(),
                onAcknowledgeFailureCommandMock);
        startControllerAndProcessAcknowledgementsAndStopIt();

        verify(circuitBreakerMock).handleFailure();
        verify(onAcknowledgeFailureCommandMock).run();
    }

    private static EventWithFuture createEventWithFutureWithPublishingFailure()
            throws InterruptedException, ExecutionException, TimeoutException {
        PublishedEventInfo publishedEventInfoMock = mockPublishedEventInfo(false);

        EventWithFuture eventWithFutureMock = createEventWithFutureMock(publishedEventInfoMock);

        mockProtobufMessage(eventWithFutureMock);

        mockPublishingContext(eventWithFutureMock, CUSTOMER_ID_1);

        return eventWithFutureMock;
    }

    @Test
    void processing_is_stopped_when_eventFutureGet_throws_InterruptedException()
            throws InterruptedException, TimeoutException, ExecutionException {
        EventWithFuture eventWithFuture = createEventWithFutureMock(mock(PublishedEventInfo.class));
        mockPublishingContext(eventWithFuture, CUSTOMER_ID_1);
        when(eventWithFuture.getFuture().get(anyLong(), any())).thenThrow(new InterruptedException());

        acknowledgementController.acknowledgeEvent(eventWithFuture, mock(Runnable.class));

        startController();
        controllerStateObserver.waitUntilBecomes(false, CONDITION_WAIT_PARAMETERS);
    }

    @Test
    void event_is_returnedBackToPendingEvents_when_eventFutureGet_throws_InterruptedException()
            throws InterruptedException, TimeoutException, ExecutionException {
        Future<PublishedEventInfo> publishedEventInfoFutureMock = mock(Future.class);
        EventWithFuture eventWithFutureMock = createEventWithFutureWithPublishingSuccess(publishedEventInfoFutureMock);
        when(publishedEventInfoFutureMock.get(anyLong(), any())).thenThrow(new InterruptedException())
                .thenReturn(mockPublishedEventInfo(true));

        acknowledgementController.acknowledgeEvent(eventWithFutureMock, mock(Runnable.class));
        acknowledgementController.addObserver(observerMock1);
        startController();

        controllerStateObserver.waitUntilBecomes(false, CONDITION_WAIT_PARAMETERS);
        verify(observerMock1, never()).update(any(), any());

        startControllerAndProcessAcknowledgementsAndStopIt();
        verifyObserverInvocation(observerMock1, 0);
    }

    @Test
    void pendingAcknowledgementsPerCustomerTable_counts_are_correct_after_enqueue_and_process()
            throws InterruptedException, ExecutionException, TimeoutException {
        acknowledgementController.acknowledgeEvent(createEventWithFutureWithPublishingSuccess(), mock(Runnable.class));

        assertThat(acknowledgementController.getPendingAcknowledgementsForCustomerTable(ENTITY_TYPE,
                CUSTOMER_ID_1)).isEqualTo(1);

        startControllerAndProcessAcknowledgementsAndStopIt();

        assertThat(acknowledgementController.getPendingAcknowledgementsForCustomerTable(ENTITY_TYPE,
                CUSTOMER_ID_1)).isEqualTo(0);
    }

    @Test
    void nothing_happens_when_eventing_is_disabled_for_customer_and_there_are_no_pending_events() {
        final EventingConfigurationChange configurationChange =
                new EventingConfigurationChange().setRemoveEventsForCustomer(CUSTOMER_ID_1);

        acknowledgementController.addObserver(observerMock1);

        acknowledgementController.update(mock(Observable.class), configurationChange);

        verifyNoInteractions(observerMock1);
        verify(eventFailuresMonitorMock).reset(CUSTOMER_ID_1);
    }

    @Test
    void correct_events_are_removed_when_eventing_is_disabled_for_customer() throws Exception {
        final EventingConfigurationChange configurationChange =
                new EventingConfigurationChange().setRemoveEventsForCustomer(CUSTOMER_ID_2);

        Future<PublishedEventInfo> publishedEventInfoFutureMock = mock(Future.class);

        final int numberOfEventsForCustomer = 5;
        for (int eventsCount = 1; eventsCount <= numberOfEventsForCustomer; eventsCount++) {
            acknowledgementController.acknowledgeEvent(createEventWithFutureWithPublishingSuccess(),
                    mock(Runnable.class));
            acknowledgementController.acknowledgeEvent(createEventWithFutureToBeDropped(publishedEventInfoFutureMock),
                    mock(Runnable.class));
        }

        acknowledgementController.addObserver(observerMock1);
        acknowledgementController.addObserver(observerMock2);

        acknowledgementController.update(mock(Observable.class), configurationChange);

        for (int eventsCount = 2 * numberOfEventsForCustomer - 1; eventsCount >= numberOfEventsForCustomer;
                eventsCount--) {
            verifyObserverInvocation(observerMock1, eventsCount);
            verifyObserverInvocation(observerMock2, eventsCount);
        }
        verify(eventFailuresMonitorMock).reset(CUSTOMER_ID_2);
        verify(publishedEventInfoFutureMock, times(numberOfEventsForCustomer)).cancel(false);

        startControllerAndProcessAcknowledgementsAndStopIt();

        for (int eventsCount = numberOfEventsForCustomer - 1; eventsCount >= 0; eventsCount--) {
            verifyObserverInvocation(observerMock1, eventsCount);
            verifyObserverInvocation(observerMock2, eventsCount);
        }
        verify(circuitBreakerMock, times(numberOfEventsForCustomer)).handleSuccess();
        verify(eventFailuresMonitorMock, times(numberOfEventsForCustomer)).processSuccess(CUSTOMER_ID_1);
    }

    private static EventWithFuture createEventWithFutureToBeDropped(
            Future<PublishedEventInfo> publishedEventInfoFutureMock) {
        EventWithFuture eventWithFutureMock = mock(EventWithFuture.class);
        when(eventWithFutureMock.getFuture()).thenReturn(publishedEventInfoFutureMock);

        mockProtobufMessage(eventWithFutureMock);

        mockPublishingContext(eventWithFutureMock, CUSTOMER_ID_2);

        return eventWithFutureMock;
    }

    @Test
    void acknowledgeEvent_throwsNullPointerException_forNullOnAcknowledgeFailureCommand() {
        assertThatNullPointerException().isThrownBy(
                () -> acknowledgementController.acknowledgeEvent(mock(EventWithFuture.class), null));
    }

    @Test
    void acknowledgeEvent_throwsNullPointerException_forNullEvent() {
        assertThatNullPointerException().isThrownBy(
                () -> acknowledgementController.acknowledgeEvent(null, mock(Runnable.class)));
    }
}