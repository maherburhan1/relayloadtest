package com.solarwinds.msp.ncentral.eventproduction.controller;

import com.google.protobuf.MessageLite;

import com.solarwinds.msp.ncentral.common.time.TimeService;
import com.solarwinds.msp.ncentral.eventproduction.api.service.persistence.TimestampedEvent;
import com.solarwinds.msp.ncentral.eventproduction.api.service.publisher.PublishingContext;
import com.solarwinds.msp.ncentral.eventproduction.api.service.scraping.EventScraper;
import com.solarwinds.msp.ncentral.eventproduction.api.service.statistics.EventStatistics;
import com.solarwinds.msp.ncentral.eventproduction.api.service.tracking.ComponentStatusService;
import com.solarwinds.msp.ncentral.eventproduction.api.service.tracking.EventTableScrapingState;
import com.solarwinds.msp.ncentral.eventproduction.api.service.tracking.EventTableStateChange;
import com.solarwinds.msp.ncentral.eventproduction.api.service.tracking.EventingControlService;
import com.solarwinds.msp.ncentral.eventproduction.controller.impl.EventFailuresMonitor;
import com.solarwinds.msp.ncentral.eventproduction.controller.impl.PersistedEventSendBuffer;
import com.solarwinds.msp.ncentral.eventproduction.persistence.file.QueuedEventFileStore;
import com.solarwinds.msp.ncentral.proto.entity.entity.ClientOuterClass;
import com.solarwinds.util.BooleanWaitingObserver;
import com.solarwinds.util.function.ConditionWaitParameters;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.concurrent.TimeoutException;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EventBufferControllerTest {

    private static final String ENABLED_TABLE_NAME = "enabledTable";
    private static final int ENABLED_CUSTOMER_ID = 11;
    private final double MEMORY_SIZE_LIMIT_PERCENTAGE = 5.0;
    private final double FILE_SIZE_LIMIT_PERCENTAGE = 5.0;
    private final Duration RELAY_PING_WAIT_TIME = Duration.ofSeconds(1);
    private final int OPERATIONS_TO_TRIGGER_SIZE_CHECK =
            EventBufferController.NUMBER_OF_OPERATIONS_UNTIL_CAPACITY_CHECK;

    private static final ConditionWaitParameters CONDITION_WAIT_PARAMETERS = ConditionWaitParameters.newBuilder()
            .withPauseBetweenRetries(Duration.ofMillis(20L))
            .withMaximumWait(Duration.ofSeconds(10L))
            .build();

    @Mock
    private PersistedEventSendBuffer<MessageLite> memoryQueue;
    @Mock
    private QueuedEventFileStore<MessageLite> fileQueue;
    @Mock
    private EventFailuresMonitor eventFailuresMonitor;
    @Mock
    private EventStatistics eventStatistics;
    @Mock
    private EventControllerConfiguration eventControllerConfigurationMock;
    @Spy
    private EventingControlService eventingControlServiceSpy;
    @Mock
    private EventRelayPingService eventRelayPingService;
    @Mock
    private EventScraper eventScraperMock;
    @Mock
    private ComponentStatusService componentStatusServiceMock;
    @Mock
    private TimeService timeServiceMock;

    private EventBufferController<MessageLite> bufferController;

    private long AVERAGE_EVENT_BYTE_SIZE = 1_000L;
    private long memoryCount = 0;
    private long fileCount = 0;
    private BooleanWaitingObserver controllerStateObserver;

    @BeforeEach
    void setup() {
        updateBufferControllerObject(MEMORY_SIZE_LIMIT_PERCENTAGE, FILE_SIZE_LIMIT_PERCENTAGE, RELAY_PING_WAIT_TIME);

        eventingControlServiceSpy.updateStateForTable(EventTableStateChange.builder()
                .tableName(ENABLED_TABLE_NAME)
                .customerId(ENABLED_CUSTOMER_ID)
                .scrapingState(EventTableScrapingState.FINISHED)
                .build());
    }

    @AfterEach
    void tearDown() {
        fileQueue.close();
    }

    private void updateBufferControllerObject(double memoryPercentageSize, double filePercentageSize,
            Duration relayPingWait) {
        bufferController = new EventBufferController<>(memoryQueue, fileQueue, eventStatistics, eventFailuresMonitor,
                eventRelayPingService, eventControllerConfigurationMock, eventingControlServiceSpy,
                componentStatusServiceMock, eventScraperMock, timeServiceMock);
        when(eventControllerConfigurationMock.getMemoryBufferPercentageSize()).thenReturn(
                OptionalDouble.of(memoryPercentageSize));
        when(eventControllerConfigurationMock.getFileBufferPercentageSize()).thenReturn(
                OptionalDouble.of(filePercentageSize));
        when(eventControllerConfigurationMock.getBufferControllerWaitTimeBetweenRelayPings()).thenReturn(
                Optional.of(relayPingWait));
        bufferController.onEventingStart();
        verify(eventingControlServiceSpy).addStartupListenerOrExecuteStartup(bufferController);
    }

    private void setControllerToNoCapacity() {
        updateBufferControllerObject(0, 0, Duration.ZERO);
    }

    private void setControllerCustomBufferSizes(int memoryCapacityCount, int fileCapacityCount) {
        bufferController.overrideMemoryAndFileCapacity(AVERAGE_EVENT_BYTE_SIZE * memoryCapacityCount,
                AVERAGE_EVENT_BYTE_SIZE * fileCapacityCount);
    }

    private TimestampedEvent<MessageLite> generateEvent() {
        ClientOuterClass.Client eventData = ClientOuterClass.Client.newBuilder().build(); // Create an empty event
        ZonedDateTime timestamp = ZonedDateTime.now();
        PublishingContext publishingContext = PublishingContext.builder()
                .withBizappsCustomerId("BIZ_APPS_ID_1")
                .withSystemGuid("SYSTEM_GUID_2")
                .withEntityType(ENABLED_TABLE_NAME)
                .withEventingConfigurationCustomerId(ENABLED_CUSTOMER_ID)
                .build();

        return new TimestampedEvent<>(eventData, timestamp, publishingContext);
    }

    private void persistNEvents(int n) {
        for (int i = 0; i < n; i++) {
            bufferController.enqueue(generateEvent());
        }
    }

    private void createAndRegisterControllerStateObserver() {
        controllerStateObserver = new BooleanWaitingObserver();
        doAnswer(invocation -> {
            controllerStateObserver.update(null, invocation.getArguments()[1]);
            return null;
        }).when(componentStatusServiceMock).setRunning(eq(EventBufferController.class), anyBoolean());
    }

    private void mockMemoryQueueAdd() {
        doAnswer((Answer<Void>) invocationOnMock -> {
            memoryCount++;
            return null;
        }).when(memoryQueue).addLast(any());
    }

    private void mockMemoryQueueAddFirst() {
        doAnswer((Answer<Void>) invocationOnMock -> {
            memoryCount++;
            return null;
        }).when(memoryQueue).addFirst(any());
    }

    private void mockMemoryQueueRemove() throws InterruptedException {
        doAnswer((Answer<TimestampedEvent<MessageLite>>) invocationOnMock -> {
            memoryCount--;
            return generateEvent();
        }).when(memoryQueue).takeFirst();
    }

    private void mockMemoryQueueClear() {
        doAnswer((Answer<Void>) invocationOnMock -> {
            memoryCount = 0;
            return null;
        }).when(memoryQueue).clear();
    }

    private void mockFileQueueAdd() {
        doAnswer((Answer<Void>) invocationOnMock -> {
            fileCount++;
            return null;
        }).when(fileQueue).persist(any());
    }

    private void mockFileQueueRemove() {
        doAnswer((Answer<Optional<TimestampedEvent<MessageLite>>>) invocationOnMock -> {
            fileCount--;
            return Optional.of(generateEvent());
        }).when(fileQueue).remove();
    }

    private void mockFileQueueClear() {
        doAnswer((Answer<Void>) invocationOnMock -> {
            fileCount = 0;
            return null;
        }).when(fileQueue).clear();
    }

    @Test
    void add_elements_to_event_buffer_controller_stable_memory() {
        mockMemoryQueueAdd();
        when(memoryQueue.currentCount()).thenAnswer(invocation -> memoryCount);
        when(memoryQueue.currentByteSize()).thenAnswer(invocation -> memoryCount * AVERAGE_EVENT_BYTE_SIZE);

        persistNEvents(OPERATIONS_TO_TRIGGER_SIZE_CHECK);

        assertThat(bufferController.getMemoryBufferCurrentCount()).isEqualTo(OPERATIONS_TO_TRIGGER_SIZE_CHECK);
        assertThat(bufferController.getMemoryBufferCurrentByteSize()).isNotEqualTo(0);
        assertThat(bufferController.currentState()).isEqualTo(
                EventBufferController.BufferFlowStates.STABLE_FLOW_TO_MEMORY);
    }

    @Test
    void add_and_remove_elements_from_event_buffer_controller_stable_memory() throws InterruptedException {
        mockMemoryQueueAdd();
        mockMemoryQueueRemove();
        when(memoryQueue.currentCount()).thenAnswer(invocation -> memoryCount);

        persistNEvents(1);

        assertThat(bufferController.getMemoryBufferCurrentCount()).isEqualTo(1);
        bufferController.dequeue();
        assertThat(bufferController.getMemoryBufferCurrentCount()).isEqualTo(0);
    }

    @Test
    void add_event_to_front_of_queue() {
        mockMemoryQueueAdd();
        mockMemoryQueueAddFirst();
        when(memoryQueue.currentCount()).thenAnswer(invocation -> memoryCount);

        persistNEvents(3);
        bufferController.addFirst(generateEvent());

        assertThat(bufferController.getMemoryBufferCurrentCount()).isEqualTo(4);
    }

    @Test
    void add_elements_to_event_buffer_controller_unstable_overflow_memory() {
        setControllerToNoCapacity();
        mockMemoryQueueAdd();
        mockFileQueueAdd();
        when(memoryQueue.currentByteSize()).thenAnswer(invocation -> memoryCount * AVERAGE_EVENT_BYTE_SIZE);
        when(fileQueue.currentCount()).thenAnswer(invocation -> fileCount);

        persistNEvents(
                OPERATIONS_TO_TRIGGER_SIZE_CHECK); // Note: we only evaluate size every N element adds to the memory queue. Currently, this is set to 200.
        assertThat(bufferController.currentState()).isEqualTo(
                EventBufferController.BufferFlowStates.UNSTABLE_OVERFLOW_TO_FILE);
        assertThat(bufferController.getFileBufferCurrentCount()).isNotEqualTo(0);
    }

    @Test
    void add_elements_to_completely_full_buffers() throws TimeoutException, InterruptedException {
        setControllerToNoCapacity();
        mockMemoryQueueAdd();
        mockFileQueueAdd();
        mockMemoryQueueClear();
        mockFileQueueClear();
        when(memoryQueue.currentByteSize()).thenAnswer(invocation -> memoryCount * AVERAGE_EVENT_BYTE_SIZE);
        when(memoryQueue.currentCount()).thenAnswer(invocation -> memoryCount);
        when(fileQueue.currentByteSize()).thenAnswer(invocation -> fileCount * AVERAGE_EVENT_BYTE_SIZE);
        when(fileQueue.currentCount()).thenAnswer(invocation -> fileCount);
        when(eventRelayPingService.checkHealthOfAllRelays()).thenReturn(true);
        createAndRegisterControllerStateObserver();

        persistNEvents(OPERATIONS_TO_TRIGGER_SIZE_CHECK
                * 2); // Need to trigger 2 size checks since we want to overflow both memory and file.

        // Wait until the relay health check thread is finished to verify.
        controllerStateObserver.waitUntilBecomes(false, CONDITION_WAIT_PARAMETERS);

        // Termination should clear all buffers.
        assertThat(bufferController.getMemoryBufferCurrentCount()).isEqualTo(0);
        assertThat(bufferController.getFileBufferCurrentCount()).isEqualTo(0);
    }

    @Test
    void memory_can_be_reloaded_with_events_once_becoming_available_again()
            throws TimeoutException, InterruptedException {
        setControllerCustomBufferSizes(OPERATIONS_TO_TRIGGER_SIZE_CHECK - 1, OPERATIONS_TO_TRIGGER_SIZE_CHECK * 10);
        mockMemoryQueueAdd();
        mockMemoryQueueRemove();
        mockFileQueueAdd();
        mockFileQueueRemove();
        when(memoryQueue.currentByteSize()).thenAnswer(invocation -> memoryCount * AVERAGE_EVENT_BYTE_SIZE);
        when(fileQueue.currentByteSize()).thenAnswer(invocation -> fileCount * AVERAGE_EVENT_BYTE_SIZE);
        when(memoryQueue.currentCount()).thenAnswer(invocation -> memoryCount);
        when(fileQueue.currentCount()).thenAnswer(invocation -> fileCount);
        createAndRegisterControllerStateObserver();

        persistNEvents(OPERATIONS_TO_TRIGGER_SIZE_CHECK
                * 3); // Add enough events to overflow the small memory capacity to file.

        // At this point, memory should be at capacity and we should have overflowed to the file buffer.
        assertThat(bufferController.currentState()).isEqualTo(
                EventBufferController.BufferFlowStates.UNSTABLE_OVERFLOW_TO_FILE);
        assertThat(bufferController.getMemoryBufferCurrentCount()).isNotEqualTo(0);
        assertThat(bufferController.getFileBufferCurrentCount()).isNotEqualTo(0);

        long filePreviousCount =
                bufferController.getFileBufferCurrentCount(); // Get the current number of events in file queue
        for (int i = 0; i < OPERATIONS_TO_TRIGGER_SIZE_CHECK; i++) {
            // Remove memory queue files
            bufferController.dequeue();
        }

        // Wait until the event reloader thread is finished to verify.
        controllerStateObserver.waitUntilBecomes(false, CONDITION_WAIT_PARAMETERS);

        // The buffer state should still be in OVERFLOW since we have elements in the file queue still.
        assertThat(bufferController.currentState()).isEqualTo(
                EventBufferController.BufferFlowStates.UNSTABLE_OVERFLOW_TO_FILE);
        assertThat(bufferController.getFileBufferCurrentCount()).isLessThan(
                filePreviousCount); // Ensure that events have been reloaded into memory from file.
    }

    @Test
    void event_buffer_can_transition_back_to_stable_state_from_overflow()
            throws InterruptedException, TimeoutException {
        setControllerCustomBufferSizes(OPERATIONS_TO_TRIGGER_SIZE_CHECK * 2, OPERATIONS_TO_TRIGGER_SIZE_CHECK * 10);
        mockMemoryQueueAdd();
        mockMemoryQueueRemove();
        mockFileQueueAdd();
        mockFileQueueRemove();
        doAnswer((Answer<Boolean>) invocationOnMock -> fileCount == 0).when(fileQueue).isEmpty();
        when(memoryQueue.currentByteSize()).thenAnswer(invocation -> memoryCount * AVERAGE_EVENT_BYTE_SIZE);
        when(memoryQueue.currentCount()).thenAnswer(invocation -> memoryCount);
        when(fileQueue.currentByteSize()).thenAnswer(invocation -> fileCount * AVERAGE_EVENT_BYTE_SIZE);
        when(fileQueue.currentCount()).thenAnswer(invocation -> fileCount);
        createAndRegisterControllerStateObserver();

        int eventCount = OPERATIONS_TO_TRIGGER_SIZE_CHECK * 4;
        persistNEvents(eventCount);

        // Ensure we are currently overflowing to file.
        assertThat(bufferController.currentState()).isEqualTo(
                EventBufferController.BufferFlowStates.UNSTABLE_OVERFLOW_TO_FILE);

        int maximumIterations = eventCount * 2;
        int currentIterations = 0;

        // Note: Using an iteration counter to prevent infinite looping. There is an assertion to make sure we did
        // not break out of the while loop due to iteration limit.
        while (currentIterations < maximumIterations) {
            for (int i = 0; i <= OPERATIONS_TO_TRIGGER_SIZE_CHECK; i++) {
                if (bufferController.getMemoryBufferCurrentCount() > 0) {
                    bufferController.dequeue();
                }
                currentIterations++;

                // If the reloader thread is running, wait for it to complete.
                if (controllerStateObserver.getValue().isPresent()) {
                    controllerStateObserver.waitUntilBecomes(false, CONDITION_WAIT_PARAMETERS);
                    break;
                }
            }

            if (bufferController.currentState().equals(EventBufferController.BufferFlowStates.STABLE_FLOW_TO_MEMORY)) {
                break;
            }
        }
        // Assert that we did not break out of the while loop because of hitting iteration safety limit.
        assertThat(currentIterations).isLessThan(maximumIterations);

        // Ensure we are currently stable again after dequeuing.
        assertThat(bufferController.currentState()).isEqualTo(
                EventBufferController.BufferFlowStates.STABLE_FLOW_TO_MEMORY);
        assertThat(bufferController.getFileBufferCurrentCount()).isEqualTo(0);
    }

    @Test
    void event_buffer_pings_relay_when_buffers_terminate() throws TimeoutException, InterruptedException {
        setControllerToNoCapacity();
        mockMemoryQueueAdd();
        mockFileQueueAdd();
        when(memoryQueue.currentByteSize()).thenAnswer(invocation -> memoryCount * AVERAGE_EVENT_BYTE_SIZE);
        when(fileQueue.currentByteSize()).thenAnswer(invocation -> fileCount * AVERAGE_EVENT_BYTE_SIZE);
        when(eventRelayPingService.checkHealthOfAllRelays()).thenReturn(true);
        createAndRegisterControllerStateObserver();

        persistNEvents(OPERATIONS_TO_TRIGGER_SIZE_CHECK * 2);

        // Wait until the relay health check thread is finished to verify.
        controllerStateObserver.waitUntilBecomes(false, CONDITION_WAIT_PARAMETERS);

        // Verify that the controller pings the relay at least once while we are terminated.
        // Ping wait time is set to 1 second.
        verify(eventRelayPingService, atLeastOnce()).checkHealthOfAllRelays();
    }
}
