package com.solarwinds.msp.ncentral.eventproduction.api.service.notification;

/**
 * Types of MSP Event Producer email notifications.
 */
public enum EventNotificationType {

    /**
     * N-central is able to successfully send events again.
     */
    EVENTS_SUCCESSFULLY_SENT,
    /**
     * The file buffer has filled and buffering is going to be terminated.
     */
    FILE_QUEUE_CAPACITY_REACHED,
    /**
     * The memory queue has reached its capacity and will now overflow to the file queue.
     */
    MEMORY_QUEUE_CAPACITY_REACHED,
    /**
     * The memory queue has reached a stable state again.
     */
    MEMORY_QUEUE_STABLE,
    /**
     * N-central has been unable to successfully send events for a certain period of time (configurable in the
     * database).
     */
    UNABLE_TO_SEND_EVENTS
}
