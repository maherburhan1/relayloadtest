package com.solarwinds.msp.ncentral.eventproduction.api.service.persistence;

import com.solarwinds.msp.ncentral.eventproduction.api.service.publisher.PublishingContext;
import com.solarwinds.util.NullChecker;

import java.time.ZonedDateTime;
import java.util.Objects;

/**
 * Data holder class for persisting event data along with the timestamp it was published at, and the context for
 * publishing. Also contains logic for tracking the count of send retries when send has failed.
 *
 * @param <T> the event type.
 */
public class TimestampedEvent<T> {

    private static final String NAME_EVENT = "event";
    private static final String NAME_TIMESTAMP = "timestamp";
    private static final String NAME_PUBLISHING_CONTEXT = "publishingContext";

    private final T event;
    private final ZonedDateTime timestamp;
    private final PublishingContext publishingContext;
    private int sendRetryCount;
    private long timestampForPersistence;

    /**
     * Creates a new instance of the TimeStamped event data holder object. Will initialize the send retry count to 0.
     *
     * @param event Event data of generic type.
     * @param timestamp The timestamp the event was published at.
     * @param publishingContext Context for publishing the event (BizAppsID, etc.)
     */
    public TimestampedEvent(T event, ZonedDateTime timestamp, PublishingContext publishingContext) {
        this.event = NullChecker.check(event, NAME_EVENT);
        this.timestamp = NullChecker.check(timestamp, NAME_TIMESTAMP);
        this.publishingContext = NullChecker.check(publishingContext, NAME_PUBLISHING_CONTEXT);
        this.sendRetryCount = 0;
    }

    /**
     * Creates a new instance of the TimeStamped event data holder object.
     *
     * @param event Event data of generic type.
     * @param timestamp The timestamp the event was published at.
     * @param publishingContext Context for publishing the event (BizAppsID, etc.)
     * @param sendRetryCount The number of times this event has failed to send.
     */
    public TimestampedEvent(T event, ZonedDateTime timestamp, PublishingContext publishingContext, int sendRetryCount) {
        this.event = NullChecker.check(event, NAME_EVENT);
        this.timestamp = NullChecker.check(timestamp, NAME_TIMESTAMP);
        this.publishingContext = NullChecker.check(publishingContext, NAME_PUBLISHING_CONTEXT);
        this.sendRetryCount = sendRetryCount;
    }

    /**
     * Creates a new copy of TimeStamped event data holder object from the provided object.
     *
     * @param timestampedEvent The source object to create a new instance of.
     */
    public TimestampedEvent(TimestampedEvent<T> timestampedEvent) {
        this(timestampedEvent.getEvent(), timestampedEvent.getTimestamp(), timestampedEvent.getPublishingContext(),
                timestampedEvent.getSendRetryCount());
    }

    public T getEvent() {
        return event;
    }

    public ZonedDateTime getTimestamp() {
        return timestamp;
    }

    public PublishingContext getPublishingContext() {
        return publishingContext;
    }

    public int getSendRetryCount() {
        return sendRetryCount;
    }

    public void setSendRetryCount(int sendRetryCount) {
        this.sendRetryCount = sendRetryCount;
    }

    public long getTimestampForPersistence() {
        return timestampForPersistence;
    }

    public void setTimestampForPersistence(long timestampForPersistence) {
        this.timestampForPersistence = timestampForPersistence;
    }

    /**
     * Increase the send retry count by 1 and returns new value. It handles the integer overflow issue. Therefore the
     * valid new value is within the range [1, {@link Integer#MAX_VALUE}].
     *
     * @return The send retry count increased by 1.
     */
    public int incrementRetryCount() {
        if (sendRetryCount == Integer.MAX_VALUE || sendRetryCount < 0) {
            sendRetryCount = 0;
        }
        sendRetryCount++;
        return sendRetryCount;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }
        TimestampedEvent<?> that = (TimestampedEvent<?>) other;
        return Objects.equals(event, that.event) && Objects.equals(timestamp, that.timestamp) && Objects.equals(
                publishingContext, that.publishingContext) && timestampForPersistence == that.timestampForPersistence;
    }

    @Override
    public int hashCode() {
        return Objects.hash(event, timestamp, publishingContext, timestampForPersistence);
    }

}
