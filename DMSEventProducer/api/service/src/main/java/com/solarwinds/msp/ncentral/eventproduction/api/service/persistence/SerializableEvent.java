package com.solarwinds.msp.ncentral.eventproduction.api.service.persistence;

import java.io.Serializable;
import java.util.Objects;

/**
 * Utility class marked as serializable to aid in persisting event data to file in the QueuedEventStore.
 */
public class SerializableEvent implements Serializable {
    private byte[] eventData;
    private String eventType;
    private byte[] timestamp;
    private byte[] publishingContext;

    private SerializableEvent() { }

    /**
     * @return The serialized bytes of event data.
     */
    public byte[] getEventData() {
        return eventData;
    }

    /**
     * @return The event type.
     */
    public String getEventType() {
        return eventType;
    }

    /**
     * @return The event's timestamp.
     */
    public byte[] getTimestamp() {
        return timestamp;
    }

    /**
     * @return The serialized bytes of publishing context.
     */
    public byte[] getPublishingContext() {
        return publishingContext;
    }

    /**
     * @return a Builder instance responsible for creating a SerializableEvent object.
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder for {@link SerializableEvent} instances. Not thread-safe.
     */
    public static class Builder {
        private byte[] eventData;
        private String eventType;
        private byte[] timestamp;
        private byte[] publishingContext;

        private Builder() {
        }

        /**
         * @param eventData serialized event data.
         * @return This {@link Builder} instance after setting build parameters.
         */
        public Builder withEventData(byte[] eventData) {
            this.eventData = eventData;
            return this;
        }

        /**
         * @param eventType the type of this serialized event.
         * @return This {@link Builder} instance after setting build parameters.
         */
        public Builder withEventType(String eventType) {
            this.eventType = eventType;
            return this;
        }

        /**
         * @param timestamp the timestamp to serialize.
         * @return This {@link Builder} instance after setting build parameters.
         */
        public Builder withTimestamp(byte[] timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        /**
         * @param publishingContext the serialized publishing context.
         * @return This {@link Builder} instance after setting build parameters.
         */
        public Builder withPublishingContext(byte[] publishingContext) {
            this.publishingContext = publishingContext;
            return this;
        }

        /**
         * After setting fields with provided methods, this method will construct a {@link SerializableEvent}.
         *
         * @return the constructed {@link SerializableEvent} object.
         */
        public SerializableEvent build() {
            SerializableEvent serializableEvent = new SerializableEvent();
            serializableEvent.eventData = Objects.requireNonNull(eventData, "eventData must not be null");
            serializableEvent.eventType = Objects.requireNonNull(eventType, "eventType must not be null");
            serializableEvent.timestamp = Objects.requireNonNull(timestamp, "timestamp must not be null");
            serializableEvent.publishingContext =
                    Objects.requireNonNull(publishingContext, "publishingContext must not be null");
            return serializableEvent;
        }
    }

}
