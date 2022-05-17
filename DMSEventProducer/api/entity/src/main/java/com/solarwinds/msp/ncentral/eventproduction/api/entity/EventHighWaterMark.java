package com.solarwinds.msp.ncentral.eventproduction.api.entity;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Objects;

/**
 * Entity representing high water mark object data.
 */
public class EventHighWaterMark {
    private int customerId;
    private String tableName;
    private String entityName;
    private ZonedDateTime lastProcessed;
    private boolean justThisCustomer;

    private EventHighWaterMark() {}

    public int getCustomerId() { return customerId; }

    public String getTableName() {
        return tableName;
    }

    public String getEntityName() {
        return entityName;
    }

    public ZonedDateTime getLastProcessed() {
        return lastProcessed;
    }

    public boolean isJustThisCustomer() { return justThisCustomer; }

    @Override
    public String toString() {
        return "EventHighWaterMark{" + "customerId=" + customerId + ", tableName='" + tableName + '\''
                + ", entityName='" + entityName + '\'' + ", lastProcessed=" + lastProcessed + ", justThisCustomer="
                + justThisCustomer + '}';
    }

    /**
     * @return {@link HighWaterMarkBuilder} builder for creating {@link EventHighWaterMark} instances.
     */
    public static HighWaterMarkBuilder builder() {
        return new HighWaterMarkBuilder();
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if ((object instanceof EventHighWaterMark) == false) {
            return false;
        }
        EventHighWaterMark event = (EventHighWaterMark) object;
        return new EqualsBuilder().append(customerId, event.customerId)
                .append(tableName, event.tableName)
                .append(entityName, event.entityName)
                .append(lastProcessed, event.lastProcessed)
                .append(justThisCustomer, event.justThisCustomer)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(customerId)
                .append(tableName)
                .append(entityName)
                .append(lastProcessed)
                .append(justThisCustomer)
                .toHashCode();
    }

    /**
     * Builder class for {@link EventHighWaterMark} instances.
     */
    public static final class HighWaterMarkBuilder {
        private int customerId;
        private String tableName;
        private ZonedDateTime lastProcessed;
        private String entityName;
        private boolean justThisCustomer;

        private HighWaterMarkBuilder() {}

        public HighWaterMarkBuilder customerId(int customerId) {
            this.customerId = customerId;
            return this;
        }

        public HighWaterMarkBuilder tableName(String tableName) {
            this.tableName = tableName;
            return this;
        }

        public HighWaterMarkBuilder lastProcessed(ZonedDateTime lastProcessed) {
            if (lastProcessed.withZoneSameInstant(ZoneId.of("UTC")).isAfter(Instant.EPOCH.atZone(ZoneOffset.UTC))) {
                this.lastProcessed = lastProcessed;
            } else {
                this.lastProcessed = Instant.EPOCH.atZone(ZoneOffset.UTC);
            }
            return this;
        }

        public HighWaterMarkBuilder entityName(String entityName) {
            this.entityName = entityName;
            return this;
        }

        public HighWaterMarkBuilder justThisCustomer(boolean justThisCustomer) {
            this.justThisCustomer = justThisCustomer;
            return this;
        }

        public EventHighWaterMark build() {
            EventHighWaterMark highWaterMark = new EventHighWaterMark();
            highWaterMark.customerId = customerId;
            highWaterMark.tableName = Objects.requireNonNull(tableName, "tableName cannot be null");
            highWaterMark.lastProcessed = lastProcessed;
            highWaterMark.entityName = entityName;
            highWaterMark.justThisCustomer = justThisCustomer;
            return highWaterMark;
        }
    }
}
