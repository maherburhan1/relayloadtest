package com.solarwinds.msp.ncentral.eventproduction.controller.impl;

import com.google.protobuf.MessageLite;

import com.solarwinds.msp.ncentral.eventproduction.api.service.persistence.TimestampedEvent;
import com.solarwinds.msp.ncentral.eventproduction.api.service.tracking.EventingControlService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SuppressWarnings("unchecked")
@ExtendWith(MockitoExtension.class)
public class PersistedEventSendBufferTest {

    private PersistedEventSendBuffer<MessageLite> persistedEventSendBuffer;
    private TimestampedEvent<MessageLite> eventMock1;
    private TimestampedEvent<MessageLite> eventMock2;

    @Mock
    private EventingControlService eventingControlServiceMock;

    @BeforeEach
    void setup() {
        persistedEventSendBuffer = new PersistedEventSendBuffer<>(eventingControlServiceMock);
        persistedEventSendBuffer.onEventingStart();
        verify(eventingControlServiceMock).addStartupListenerOrExecuteStartup(persistedEventSendBuffer);
        persistedEventSendBuffer.addLast(eventMock1 = mockEventAndEnableForSend());
        persistedEventSendBuffer.addLast(eventMock2 = mockEventAndEnableForSend());
    }

    private TimestampedEvent<MessageLite> mockEventAndEnableForSend() {
        TimestampedEvent<MessageLite> mockedEvent = mock(TimestampedEvent.class);
        when(eventingControlServiceMock.isEventEligibleForSend(mockedEvent)).thenReturn(true);
        return mockedEvent;
    }

    @Test
    void memory_queue_size_is_calculated() throws InterruptedException {
        assertThat(persistedEventSendBuffer.currentCount()).isEqualTo(2);
        persistedEventSendBuffer.takeFirst();
        persistedEventSendBuffer.takeFirst();
        assertThat(persistedEventSendBuffer.currentCount()).isEqualTo(0);

        long sizeBefore = persistedEventSendBuffer.currentByteSize();
        persistedEventSendBuffer.addLast(eventMock1 = mockEventAndEnableForSend());
        persistedEventSendBuffer.addLast(eventMock2 = mockEventAndEnableForSend());

        // Size should be greater after adding elements
        long sizeAfter = persistedEventSendBuffer.currentByteSize();
        assertThat(sizeAfter).isGreaterThan(sizeBefore);

        persistedEventSendBuffer.takeFirst();
        assertThat(persistedEventSendBuffer.currentByteSize()).isLessThan(sizeAfter);
    }

    @Test
    void takeFirst_returns_and_removes_the_first_element() throws InterruptedException {
        assertThat(persistedEventSendBuffer.takeFirst()).isSameAs(eventMock1);
        assertThat(persistedEventSendBuffer.takeFirst()).isSameAs(eventMock2);
    }

    @Test
    void takeFirst_waits_until_element_is_available() throws InterruptedException, ExecutionException {
        assertThat(persistedEventSendBuffer.takeFirst()).isSameAs(eventMock1);
        assertThat(persistedEventSendBuffer.takeFirst()).isSameAs(eventMock2);

        AtomicBoolean isInExecution = new AtomicBoolean(false);
        Future<TimestampedEvent> futureTimestampedEvent = Executors.newSingleThreadExecutor().submit(() -> {
            isInExecution.set(true);
            return persistedEventSendBuffer.takeFirst();
        });
        do {
            Thread.sleep(500); // give it some time to run
        } while (!isInExecution.get());
        assertThat(futureTimestampedEvent.isDone()).isFalse();

        TimestampedEvent<MessageLite> newEventMock = mockEventAndEnableForSend();
        persistedEventSendBuffer.addLast(newEventMock);
        assertThat(futureTimestampedEvent.get()).isSameAs(newEventMock);
    }

    @Test
    void addFirst_adds_element_at_the_front() throws InterruptedException {
        TimestampedEvent<MessageLite> firstEventMock = mockEventAndEnableForSend();
        persistedEventSendBuffer.addFirst(firstEventMock);
        assertThat(persistedEventSendBuffer.takeFirst()).isSameAs(firstEventMock);
    }

    @Test
    void addLast_adds_element_at_the_end() throws InterruptedException {
        TimestampedEvent<MessageLite> lastEventMock = mockEventAndEnableForSend();
        persistedEventSendBuffer.addLast(lastEventMock);

        persistedEventSendBuffer.takeFirst();
        persistedEventSendBuffer.takeFirst();
        assertThat(persistedEventSendBuffer.takeFirst()).isSameAs(lastEventMock);
    }

    @Test
    void addLast_sets_timestamp_for_persistence_in_order_and_does_not_modify_persistence_timestamp_if_set()
            throws InterruptedException {
        persistedEventSendBuffer.takeFirst();
        persistedEventSendBuffer.takeFirst();

        ArgumentCaptor<Long> longArgumentCaptor = ArgumentCaptor.forClass(Long.class);

        verify(eventMock1).setTimestampForPersistence(longArgumentCaptor.capture());
        final Long eventMock1TimestampForPersistence = longArgumentCaptor.getValue();
        when(eventMock1.getTimestampForPersistence()).thenReturn(eventMock1TimestampForPersistence);

        verify(eventMock2).setTimestampForPersistence(longArgumentCaptor.capture());
        final Long eventMock2TimestampForPersistence = longArgumentCaptor.getValue();

        assertThat(eventMock1TimestampForPersistence).isLessThan(eventMock2TimestampForPersistence);

        persistedEventSendBuffer.addLast(eventMock1);
        persistedEventSendBuffer.takeFirst();

        verify(eventMock1).setTimestampForPersistence(longArgumentCaptor.capture());

        assertThat(longArgumentCaptor.getValue()).isEqualTo(eventMock1TimestampForPersistence);
    }

}