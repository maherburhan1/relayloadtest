package com.solarwinds.msp.ncentral.eventproduction.controller.impl;

import com.google.protobuf.MessageLite;

import com.solarwinds.msp.ncentral.eventproduction.api.service.persistence.TimestampedEvent;
import com.solarwinds.msp.ncentral.eventproduction.api.service.publisher.PublishingContext;
import com.solarwinds.msp.ncentral.eventproduction.api.service.tracking.EventingConfigurationChange;
import com.solarwinds.msp.ncentral.eventproduction.api.service.tracking.EventingControlService;
import com.solarwinds.util.function.Condition;
import com.solarwinds.util.function.ConditionWaitParameters;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.BooleanSupplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SuppressWarnings("unchecked")
@ExtendWith(MockitoExtension.class)
class PersistedEventSendBufferBufferingSendingEligibilityTest {

    private static final int CUSTOMER_ID = 12;
    private PersistedEventSendBuffer<MessageLite> persistedEventSendBuffer;

    private static final ConditionWaitParameters CONDITION_WAIT_PARAMETERS = ConditionWaitParameters.newBuilder()
            .withPauseBetweenRetries(Duration.ofMillis(20L))
            .withMaximumWait(Duration.ofSeconds(5L))
            .build();

    @Mock
    private EventingControlService eventingControlService;

    @BeforeEach
    void setup() {
        persistedEventSendBuffer = new PersistedEventSendBuffer<>(eventingControlService);
        persistedEventSendBuffer.onEventingStart();
        verify(eventingControlService).addStartupListenerOrExecuteStartup(persistedEventSendBuffer);
    }

    @Test
    void enabledEvent_processed_before_disabled_event() throws InterruptedException {
        TimestampedEvent<MessageLite> enabledEventMock = mock(TimestampedEvent.class);
        TimestampedEvent<MessageLite> disabledEventMock = mock(TimestampedEvent.class);
        when(eventingControlService.isEventEligibleForSend(enabledEventMock)).thenReturn(true);
        when(eventingControlService.isEventEligibleForSend(disabledEventMock)).thenReturn(false);

        persistedEventSendBuffer.addFirst(disabledEventMock);
        persistedEventSendBuffer.addFirst(enabledEventMock);

        assertThat(persistedEventSendBuffer.takeFirst()).isSameAs(enabledEventMock);
    }

    @Test
    void disabledEvent_blocksOnTakeFirst() throws TimeoutException, InterruptedException {
        TimestampedEvent<MessageLite> disabledEventMock = mock(TimestampedEvent.class);
        when(eventingControlService.isEventEligibleForSend(disabledEventMock)).thenReturn(false);

        persistedEventSendBuffer.addFirst(disabledEventMock);
        assertTakeFirstBlocksWithoutAnyResultReturned();
    }

    private void assertTakeFirstBlocksWithoutAnyResultReturned() throws TimeoutException, InterruptedException {
        final ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(1);
        ThreadAwareCallable<TimestampedEvent> threadAwareCallable = getThreadAwareCallableForTakeFirst();
        final Future<TimestampedEvent> eventFuture = executor.submit(threadAwareCallable);

        waitUntilThreadGetsToWaitingState(threadAwareCallable);
        assertThrows(TimeoutException.class, () -> eventFuture.get(0, TimeUnit.SECONDS));
    }

    @Test
    void clear_clears_all_buffers() {
        TimestampedEvent<MessageLite> enabledEventMock = mock(TimestampedEvent.class);
        TimestampedEvent<MessageLite> disabledEventMock = mock(TimestampedEvent.class);
        when(eventingControlService.isEventEligibleForSend(enabledEventMock)).thenReturn(true);
        when(eventingControlService.isEventEligibleForSend(disabledEventMock)).thenReturn(false);

        persistedEventSendBuffer.addLast(disabledEventMock);
        persistedEventSendBuffer.addLast(enabledEventMock);

        persistedEventSendBuffer.clear();

        assertThat(persistedEventSendBuffer.currentCount()).isEqualTo(0L);
    }

    @Test
    void update_buffer_reorder() throws InterruptedException, TimeoutException {
        TimestampedEvent<MessageLite> enabledEventMock = mock(TimestampedEvent.class);
        TimestampedEvent<MessageLite> disabledEventMock = mock(TimestampedEvent.class);
        when(eventingControlService.isEventEligibleForSend(enabledEventMock)).thenReturn(true);
        when(eventingControlService.isEventEligibleForSend(disabledEventMock)).thenReturn(false);

        persistedEventSendBuffer.addLast(disabledEventMock);
        persistedEventSendBuffer.addLast(enabledEventMock);

        when(eventingControlService.isEventEligibleForSend(enabledEventMock)).thenReturn(false);
        when(eventingControlService.isEventEligibleForSend(disabledEventMock)).thenReturn(true);

        persistedEventSendBuffer.update(null, new EventingConfigurationChange().setSendingConfigurationChanged());

        assertThat(persistedEventSendBuffer.takeFirst()).isSameAs(disabledEventMock);
        assertTakeFirstBlocksWithoutAnyResultReturned();
    }

    @Test
    void update_buffer_reorder_keeps_order_transfer_from_disabled_to_enabled() throws InterruptedException {
        TimestampedEvent<MessageLite> firstEvent = mock(TimestampedEvent.class);
        TimestampedEvent<MessageLite> secondEvent = mock(TimestampedEvent.class);
        when(eventingControlService.isEventEligibleForSend(firstEvent)).thenReturn(false);
        when(eventingControlService.isEventEligibleForSend(secondEvent)).thenReturn(false);

        persistedEventSendBuffer.addLast(firstEvent);
        persistedEventSendBuffer.addLast(secondEvent);

        when(eventingControlService.isEventEligibleForSend(firstEvent)).thenReturn(true);
        when(eventingControlService.isEventEligibleForSend(secondEvent)).thenReturn(true);

        persistedEventSendBuffer.update(null, new EventingConfigurationChange().setSendingConfigurationChanged());

        assertThat(persistedEventSendBuffer.takeFirst()).isSameAs(firstEvent);
        assertThat(persistedEventSendBuffer.takeFirst()).isSameAs(secondEvent);
    }

    @Test
    void update_buffer_reorder_keeps_order_transfer_from_enabled_to_disabled_and_back() throws InterruptedException {
        TimestampedEvent<MessageLite> firstEvent = mock(TimestampedEvent.class);
        TimestampedEvent<MessageLite> secondEvent = mock(TimestampedEvent.class);
        when(eventingControlService.isEventEligibleForSend(firstEvent)).thenReturn(true);
        when(eventingControlService.isEventEligibleForSend(secondEvent)).thenReturn(true);

        persistedEventSendBuffer.addLast(firstEvent);
        persistedEventSendBuffer.addLast(secondEvent);

        when(eventingControlService.isEventEligibleForSend(firstEvent)).thenReturn(false);
        when(eventingControlService.isEventEligibleForSend(secondEvent)).thenReturn(false);

        persistedEventSendBuffer.update(null, new EventingConfigurationChange().setSendingConfigurationChanged());

        when(eventingControlService.isEventEligibleForSend(firstEvent)).thenReturn(true);
        when(eventingControlService.isEventEligibleForSend(secondEvent)).thenReturn(true);

        persistedEventSendBuffer.update(null, new EventingConfigurationChange().setSendingConfigurationChanged());

        assertThat(persistedEventSendBuffer.takeFirst()).isSameAs(firstEvent);
        assertThat(persistedEventSendBuffer.takeFirst()).isSameAs(secondEvent);
    }

    @Test
    void update_buffer_removeBufferedForCustomer() throws TimeoutException, InterruptedException {
        TimestampedEvent<MessageLite> eventForWaitBuffer = mockEventWithContext(CUSTOMER_ID);
        TimestampedEvent<MessageLite> eventForSendBuffer = mockEventWithContext(CUSTOMER_ID);
        when(eventingControlService.isEventEligibleForSend(eventForWaitBuffer)).thenReturn(false);
        when(eventingControlService.isEventEligibleForSend(eventForSendBuffer)).thenReturn(true);

        persistedEventSendBuffer.addLast(eventForWaitBuffer);
        persistedEventSendBuffer.addLast(eventForSendBuffer);

        persistedEventSendBuffer.update(null,
                new EventingConfigurationChange().setRemoveEventsForCustomer(CUSTOMER_ID));

        assertTakeFirstBlocksWithoutAnyResultReturned();
    }

    private TimestampedEvent<MessageLite> mockEventWithContext(int customerId) {
        final TimestampedEvent event = mock(TimestampedEvent.class);
        PublishingContext publishingContext = mock(PublishingContext.class);
        when(publishingContext.getEventingConfigurationCustomerId()).thenReturn(customerId);
        when(event.getPublishingContext()).thenReturn(publishingContext);
        return event;
    }

    private ThreadAwareCallable<TimestampedEvent> getThreadAwareCallableForTakeFirst() {
        return new ThreadAwareCallable<>(() -> persistedEventSendBuffer.takeFirst());
    }

    private void waitUntilThreadGetsToWaitingState(ThreadAwareCallable callable)
            throws InterruptedException, TimeoutException {
        BooleanSupplier threadGetsToWaitingState =
                () -> (callable.getCurrentThread() != null && callable.getCurrentThread()
                        .getState()
                        .equals(Thread.State.WAITING));
        new Condition(threadGetsToWaitingState, CONDITION_WAIT_PARAMETERS).await();
    }

    private static class ThreadAwareCallable<T> implements Callable<T> {
        Thread currentThread;
        Callable<T> callable;

        ThreadAwareCallable(Callable callable) {
            this.callable = callable;
        }

        Thread getCurrentThread() {
            return currentThread;
        }

        @Override
        public T call() throws Exception {
            currentThread = Thread.currentThread();
            return callable.call();
        }
    }
}