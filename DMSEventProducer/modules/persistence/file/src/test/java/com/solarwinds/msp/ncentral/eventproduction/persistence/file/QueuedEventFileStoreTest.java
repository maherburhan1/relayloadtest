package com.solarwinds.msp.ncentral.eventproduction.persistence.file;

import com.google.protobuf.MessageLite;

import com.solarwinds.msp.ncentral.eventproduction.api.service.persistence.TimestampedEvent;
import com.solarwinds.msp.ncentral.eventproduction.api.service.publisher.PublishingContext;
import com.solarwinds.msp.ncentral.eventproduction.api.service.tracking.EventingControlService;
import com.solarwinds.msp.ncentral.proto.entity.entity.ClientOuterClass;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.file.Path;
import java.time.ZonedDateTime;
import java.util.LinkedList;
import java.util.Optional;
import java.util.Queue;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class QueuedEventFileStoreTest {
    private QueuedEventFileStore<MessageLite> testStore;

    @Mock
    private EventingControlService eventingControlServiceMock;

    @BeforeEach
    void initializeStorage(@TempDir Path storageDirectory) {
        testStore = new QueuedEventFileStore<>(storageDirectory, eventingControlServiceMock);
        testStore.onEventingStart();
        verify(eventingControlServiceMock).addStartupListenerOrExecuteStartup(testStore);
    }

    @AfterEach
    void closeStorage() { testStore.close(); }

    private TimestampedEvent<MessageLite> generateEvent() {
        ClientOuterClass.Client eventData = ClientOuterClass.Client.newBuilder().build(); // Create an empty event
        ZonedDateTime timestamp = ZonedDateTime.now();
        PublishingContext publishingContext = PublishingContext.builder()
                .withBizappsCustomerId("BIZ_APPS_ID_1")
                .withSystemGuid("SYSTEM_GUID_2")
                .withEntityType("ENTITY_TYPE")
                .withEventingConfigurationCustomerId(1)
                .build();

        TimestampedEvent<MessageLite> newTestEvent = new TimestampedEvent<>(eventData, timestamp, publishingContext);
        return newTestEvent;
    }

    void persistNEvents(int persistCount) {
        // Write N events
        for (int i = 0; i < persistCount; i++) {
            testStore.persist(generateEvent());
        }
    }

    @Test
    void testPersistEvents() {
        // Test persisting 3 events
        persistNEvents(3);
    }

    @Test
    void testFileQueueSizeCalculation() {
        // Use high record count so that the level db store dumps its write buffer from memory to file.
        long previousSize = testStore.currentByteSize();
        persistNEvents(15000);
        assertEquals(15000, testStore.currentCount(), "File queue size count should be equal to 3");
        assertNotEquals(0, testStore.currentByteSize(), "Byte size of file queue should be non-zero after additions");
        assertTrue(testStore.currentByteSize() > previousSize, "Byte size should have increased after persistence");
    }

    @Test
    void testReadEvents() {
        // Write 3 events
        persistNEvents(3);

        // Now get an iterator and try to read them back in
        int messageCount = 0;

        while (!testStore.isEmpty()) {
            Optional<TimestampedEvent<MessageLite>> readEvent = testStore.remove();
            assertNotNull(readEvent, "Check dequeued event is not null");
            assertTrue(readEvent.isPresent(), "Check dequeued event is present");

            TimestampedEvent<MessageLite> eventData = readEvent.get();
            assertNotNull(eventData.getEvent(), "Read event data should not be null");
            assertNotNull(eventData.getTimestamp(), "Read timestamp should not be null");
            assertNotNull(eventData.getPublishingContext(), "Read publishing context should not be null");

            // Ensure it can convert to the correct generic entity type
            MessageLite entity = readEvent.get().getEvent();
            assertNotNull(entity, "Check converted event is not null");

            ++messageCount;
        }
        assertEquals(3, messageCount, "Read in message count");

        // Should be empty since we removed 3 events.
        assertTrue(testStore.isEmpty(), "Persistence file size after completely read");
    }

    @Test
    void testReadAfterRemoval() {
        // Write 3 events
        persistNEvents(3);

        // Now get an iterator and try to read one back in
        MessageLite readMessage = testStore.remove().map(TimestampedEvent::getEvent).orElse(null);
        assertNotNull(readMessage, "Check dequeued event is not null");

        // Ensure it can convert to the correct generic entity type
        MessageLite entity = readMessage;
        assertNotNull(entity, "Check converted event is not null");

        assertFalse(testStore.isEmpty(), "Should have a next");

        // Now get a new iterator and ensure it works from the correct location (after head removal).
        int messageCount = 0;
        while (!testStore.isEmpty()) {
            readMessage = testStore.remove().map(TimestampedEvent::getEvent).orElse(null);
            assertNotNull(readMessage, "Check dequeued event is not null");

            ++messageCount;
        }
        assertEquals(2, messageCount, "Read in message count");
    }

    @Test
    void testReadFromEmptyStore() {
        assertFalse(testStore.remove().isPresent(), "Reading message from empty store");
    }

    @Test
    void testReadPastLastEvent() {
        // Write 1 event
        persistNEvents(1);

        MessageLite readMessage = testStore.remove().map(TimestampedEvent::getEvent).orElse(null);
        assertNotNull(readMessage, "Check dequeued event is not null");

        // Now try to read past the end
        assertTrue(testStore.isEmpty(), "Check for end of events");
        assertFalse(testStore.remove().isPresent(), "Read past last event");
    }

    @Test
    void testClearEventStore() {
        // Write 3 events
        persistNEvents(3);
        assertFalse(testStore.isEmpty(), "Check event store is empty before clearing");

        testStore.clear();
        assertTrue(testStore.isEmpty(), "Check event store is empty after clearing");
    }

    @Test
    void testQueueOrdering() {
        // Write some events.
        Queue<ZonedDateTime> testQueue = new LinkedList<>();
        for (int i = 0; i < 5; i++) {
            TimestampedEvent<MessageLite> event = generateEvent();
            testQueue.add(event.getTimestamp());
            testStore.persist(event);
        }
        assertFalse(testStore.isEmpty(), "Check event store has elements");

        while (!testStore.isEmpty()) {
            // Pop off the oldest item in the queue
            Optional<TimestampedEvent<MessageLite>> readEvent = testStore.remove();
            assertNotNull(readEvent, "Check dequeued event is not null");
            assertTrue(readEvent.isPresent(), "Check dequeued event is present");

            // Get what the ordering should be from a queue object
            ZonedDateTime nextTimestamp = testQueue.remove();

            assertEquals(nextTimestamp, readEvent.get().getTimestamp(),
                    "Events should be ordered according to queue position");
        }

        assertTrue(testStore.isEmpty(), "Check event store is empty after clearing");
    }
}
