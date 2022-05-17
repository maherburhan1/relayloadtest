package com.solarwinds.msp.ncentral.eventproduction.api.entity;

import com.solarwinds.constants.GeneralConstants;
import com.solarwinds.util.NullChecker;

import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * System control events for reporting server state to relay.
 */
public class ServerStatusEvent implements EventWithContext {

    private String ncentralServerGuid;
    private Optional<String> bizappsCustomerId;
    private ServerStatusEventType eventType;
    private ZonedDateTime eventTime;
    private List<EventHighWaterMark> highWaterMarks;
    private int eventingConfigurationCustomerId;
    private boolean directSend;

    private ServerStatusEvent() {}

    public String getNcentralServerGuid() {
        return ncentralServerGuid;
    }

    public Optional<String> getBizappsCustomerId() {
        return bizappsCustomerId;
    }

    public String getEntityType() {
        return GeneralConstants.EMPTY_STRING;
    }

    public ServerStatusEventType getEventType() {
        return eventType;
    }

    public ZonedDateTime getEventTime() {
        return eventTime;
    }

    public List<EventHighWaterMark> getHighWaterMarks() {
        return highWaterMarks;
    }

    public int getEventingConfigurationCustomerId() {return eventingConfigurationCustomerId;}

    public boolean isDirectSend() {
        return directSend;
    }

    /**
     * @return {@link Event.EventBuilder} builder for creating {@link Event} instances.
     */
    public static ServerStatusEvent.EventBuilder builder() {
        return new ServerStatusEvent.EventBuilder();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ServerStatusEvent event = (ServerStatusEvent) o;
        return ncentralServerGuid.equals(event.ncentralServerGuid) && bizappsCustomerId.equals(event.bizappsCustomerId)
                && eventType.equals(event.eventType) && eventTime.equals(event.eventTime) && highWaterMarks.equals(
                event.highWaterMarks) && directSend == event.directSend
                && eventingConfigurationCustomerId == event.eventingConfigurationCustomerId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(ncentralServerGuid, bizappsCustomerId, eventType, eventTime, highWaterMarks, directSend,
                eventingConfigurationCustomerId);
    }

    @Override
    public String toString() {
        return "Event{" + "ncentralServerGuid='" + ncentralServerGuid + '\'' + ", bizappsCustomerId="
                + bizappsCustomerId + '\'' + ", eventType=" + eventType + '\'' + ", eventTime=" + eventTime
                + ", highWatermarks=" + highWaterMarks + ", directSend=" + directSend
                + ", eventingConfigurationCustomerId=" + eventingConfigurationCustomerId + '}';
    }

    /**
     * Builder class for {@link Event} instances.
     */
    public static final class EventBuilder {
        private String ncentralServerGuid;
        private String bizappsCustomerId;
        private ServerStatusEventType eventType;
        private ZonedDateTime eventTime;
        private List<EventHighWaterMark> highWaterMarks;
        private Integer eventingConfigurationCustomerId;
        private boolean directSend;

        private EventBuilder() {}

        public EventBuilder ncentralServerGuid(String ncentralServerGuid) {
            this.ncentralServerGuid = ncentralServerGuid;
            return this;
        }

        public EventBuilder bizappsCustomerId(String bizappsCustomerId) {
            this.bizappsCustomerId = bizappsCustomerId;
            return this;
        }

        public EventBuilder eventTime(ZonedDateTime eventTime) {
            this.eventTime = eventTime;
            return this;
        }

        public EventBuilder eventType(ServerStatusEventType eventType) {
            this.eventType = eventType;
            return this;
        }

        public EventBuilder highWaterMarks(List<EventHighWaterMark> highWaterMarks) {
            this.highWaterMarks = highWaterMarks;
            return this;
        }

        public EventBuilder eventingConfigurationCustomerId(Integer eventingConfigurationCustomerId) {
            this.eventingConfigurationCustomerId = eventingConfigurationCustomerId;
            return this;
        }

        public EventBuilder directSend(boolean directSend) {
            this.directSend = directSend;
            return this;
        }

        public ServerStatusEvent build() {
            ServerStatusEvent event = new ServerStatusEvent();
            event.ncentralServerGuid = NullChecker.check(ncentralServerGuid, "ncentralServerGuid");
            event.bizappsCustomerId = Optional.ofNullable(bizappsCustomerId);
            event.eventType = NullChecker.check(eventType, "eventType");
            event.eventTime = NullChecker.check(eventTime, "eventTime");
            event.eventingConfigurationCustomerId =
                    NullChecker.check(eventingConfigurationCustomerId, "eventingConfigurationCustomerId");

            event.highWaterMarks = highWaterMarks == null ? Collections.emptyList() : highWaterMarks;
            event.directSend = directSend;

            return event;
        }
    }
}
