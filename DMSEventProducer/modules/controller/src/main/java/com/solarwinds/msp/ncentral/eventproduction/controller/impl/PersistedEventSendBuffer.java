package com.solarwinds.msp.ncentral.eventproduction.controller.impl;

import com.google.protobuf.MessageLite;

import com.nable.logging.Logger;
import com.nable.logging.Loggers;
import com.solarwinds.msp.ncentral.eventproduction.api.service.persistence.TimestampedEvent;
import com.solarwinds.msp.ncentral.eventproduction.api.service.tracking.EventingConfigurationChange;
import com.solarwinds.msp.ncentral.eventproduction.api.service.tracking.EventingControlService;
import com.solarwinds.msp.ncentral.eventproduction.api.service.tracking.EventingStartupListener;

import org.springframework.stereotype.Component;

import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * Buffer for sending events that persists the buffered events as well.
 *
 * @param <T> the buffer element type
 */
@Component
public class PersistedEventSendBuffer<T extends MessageLite> implements Observer, EventingStartupListener {

    private static final Logger logger = Loggers.EVENT_PRODUCER;

    private BlockingDeque<TimestampedEvent<T>> bufferForSend = new LinkedBlockingDeque<>();
    private BlockingDeque<TimestampedEvent<T>> bufferForWait = new LinkedBlockingDeque<>();

    private EventingControlService eventingControlService;

    /**
     * Creates a {@link PersistedEventSendBuffer}
     */
    public PersistedEventSendBuffer(EventingControlService eventingControlService) {
        this.eventingControlService = eventingControlService;
        this.eventingControlService.addObserver(this);
        this.eventingControlService.addStartupListenerOrExecuteStartup(this);
    }

    @Override
    public void onEventingStart() {
        bufferForSend.clear();
        bufferForWait.clear();
        logger.info("Component {} initialized.", this.getClass().getSimpleName());
    }

    /**
     * Inserts the specified element at the front of this buffer. This method will not take the memory size limitations
     * into consideration in order to prevent blocking. NOTE: we purposefully ignore the memory queue capacity when
     * adding an element back to the FRONT of the queue.
     *
     * @param event the {@link TimestampedEvent} element to add to the front of queue.
     */
    public void addFirst(TimestampedEvent<T> event) {
        if (eventingControlService.isEventEligibleForSend(event)) {
            bufferForSend.addFirst(event);
        } else {
            bufferForWait.addFirst(event);
        }
    }

    /**
     * Inserts the specified element at the end of this buffer.
     *
     * @param event the {@link TimestampedEvent} element to add to the end of queue.
     */
    public void addLast(TimestampedEvent<T> event) {
        if (event.getTimestampForPersistence() == 0) {
            event.setTimestampForPersistence(System.nanoTime());
        }
        if (eventingControlService.isEventEligibleForSend(event)) {
            bufferForSend.addLast(event);
        } else {
            bufferForWait.addLast(event);
        }
    }

    /**
     * Retrieves and removes the first element of this buffer, waiting if necessary until an element becomes available.
     * Elements are retrieved from the in-memory buffer first, then from the persistence.
     *
     * @return The head of the buffer
     * @throws InterruptedException if interrupted while waiting
     */
    public TimestampedEvent<T> takeFirst() throws InterruptedException {
        return bufferForSend.takeFirst();
    }

    /**
     * Clears out all data from the memory buffer.
     */
    public void clear() {
        bufferForSend.clear();
        bufferForWait.clear();
    }

    /**
     * Gets the current count of records in the memory queue.
     *
     * @return Long representing the current record count;
     */
    public long currentCount() {
        return (long) bufferForSend.size() + bufferForWait.size();
    }

    /**
     * Gets the current amount of memory in use by the memory queue in bytes.
     *
     * @return Long representing the current byte usage of the memory queue.
     */
    public long currentByteSize() {
        final long AVERAGE_TIMESTAMPED_EVENT_SIZE_IN_BYTES = 1_000L;
        return currentCount() * AVERAGE_TIMESTAMPED_EVENT_SIZE_IN_BYTES;
    }

    /**
     * Checks current status of eventing from {@link EventingControlService} and performs following:
     *
     * <ol>
     * <li> If buffering configuration has changed, remove from buffers any items that are not enabled for buffering
     * anymore.</li>
     * <li> If sending configuration has changed, recheck buffers and place enabled events for processing.</li>
     * </ol>
     */
    @Override
    public void update(Observable observable, Object argument) {
        if (argument instanceof EventingConfigurationChange) {
            EventingConfigurationChange configurationChange = (EventingConfigurationChange) argument;
            configurationChange.getRemoveEventsForCustomer().ifPresent(this::removeBufferedEventsForCustomer);
            if (configurationChange.isSendingConfigurationChanged()) {
                reorderQueue();
            }
        }
    }

    private void removeBufferedEventsForCustomer(int customerId) {
        bufferForWait.stream()
                .filter(event -> event.getPublishingContext().getEventingConfigurationCustomerId() == customerId)
                .forEach(bufferForWait::removeFirstOccurrence);
        bufferForSend.stream()
                .filter(event -> event.getPublishingContext().getEventingConfigurationCustomerId() == customerId)
                .forEach(bufferForSend::removeFirstOccurrence);
    }

    private void reorderQueue() {
        bufferForWait.stream().filter(event -> eventingControlService.isEventEligibleForSend(event)).forEach(event -> {
            bufferForWait.removeFirstOccurrence(event);
            bufferForSend.addLast(event);
        });

        bufferForSend.stream().filter(event -> !eventingControlService.isEventEligibleForSend(event)).forEach(event -> {
            bufferForSend.removeFirstOccurrence(event);
            bufferForWait.addLast(event);
        });
    }
}