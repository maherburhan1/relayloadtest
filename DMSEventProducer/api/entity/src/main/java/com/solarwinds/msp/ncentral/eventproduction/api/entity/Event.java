package com.solarwinds.msp.ncentral.eventproduction.api.entity;

import com.solarwinds.util.NullChecker;
import com.solarwinds.util.time.ZonedDateTimeParser;

import org.apache.commons.lang3.BooleanUtils;

import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Entity representing event object data.
 */
public class Event implements EventWithContext {
    private String ncentralServerGuid;
    private Optional<String> bizappsCustomerId;
    private int eventingConfigurationCustomerId;
    private String professionalModeLicenseType;
    private EventType eventType;
    private String entityType;
    private Map<String, String> entity;
    private Map<String, String> newValues;
    private Map<String, String> entityDataTypes;
    private boolean exportEnabled;
    private boolean directSend;

    private Event() {}

    public String getNcentralServerGuid() {
        return ncentralServerGuid;
    }

    public Optional<String> getBizappsCustomerId() {
        return bizappsCustomerId;
    }

    public int getEventingConfigurationCustomerId() {
        return eventingConfigurationCustomerId;
    }

    public String getProfessionalModeLicenseType() {
        return professionalModeLicenseType;
    }

    public EventType getEventType() {
        return eventType;
    }

    public String getEntityType() {
        return entityType;
    }

    public Map<String, String> getEntity() {
        return entity;
    }

    public Map<String, String> getNewValues() {
        return newValues;
    }

    public Map<String, String> getEntityDataTypes() {
        return entityDataTypes;
    }

    public boolean isDirectSend() { return directSend; }

    public boolean isExportEnabled() {return exportEnabled;}

    /**
     * @return {@link EventBuilder} builder for creating {@link Event} instances.
     */
    public static EventBuilder builder() {
        return new EventBuilder();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Event event = (Event) o;
        return ncentralServerGuid.equals(event.ncentralServerGuid) && bizappsCustomerId.equals(event.bizappsCustomerId)
                && eventingConfigurationCustomerId == event.eventingConfigurationCustomerId && entity.equals(
                event.entity) && newValues.equals(event.newValues) && entityDataTypes.equals(event.entityDataTypes)
                && eventType.equals(event.eventType) && directSend == event.directSend
                && exportEnabled == event.exportEnabled;
    }

    @Override
    public int hashCode() {
        return Objects.hash(ncentralServerGuid, bizappsCustomerId, eventingConfigurationCustomerId,
                professionalModeLicenseType, entity, newValues, entityDataTypes, eventType, directSend, exportEnabled);
    }

    @Override
    public String toString() {
        return "Event{" + "ncentralServerGuid='" + ncentralServerGuid + '\'' + ", bizappsCustomerId="
                + bizappsCustomerId + ", eventingConfigurationCustomerId=" + eventingConfigurationCustomerId
                + ", professionalModeLicenseType='" + professionalModeLicenseType + '\'' + ", eventType=" + eventType
                + ", entityType='" + entityType + '\'' + ", entity=" + entity + ", newValues=" + newValues
                + ", entityDataTypes=" + entityDataTypes + ", directSend=" + directSend + ", exportEnabled="
                + exportEnabled + '}';
    }

    public ZonedDateTime getUpdateTimestamp() {
        return ZonedDateTimeParser.parseDateTime(entity.get("lastupdated")).orElse(ZonedDateTime.now());
    }

    /**
     * Builder class for {@link Event} instances.
     */
    public static final class EventBuilder {
        private String ncentralServerGuid;
        private String bizappsCustomerId;
        private Integer eventingConfigurationCustomerId;
        private String professionalModeLicenseType;
        private EventType eventType;
        private String entityType;
        private Map<String, String> entity;
        private Map<String, String> newValues;
        private Map<String, String> entityDataTypes;
        private Boolean exportEnabled;
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

        public EventBuilder eventingConfigurationCustomerId(Integer eventingConfigurationCustomerId) {
            this.eventingConfigurationCustomerId = eventingConfigurationCustomerId;
            return this;
        }

        public EventBuilder professionalModeLicenseType(String professionalModeLicenseType) {
            this.professionalModeLicenseType = professionalModeLicenseType;
            return this;
        }

        public EventBuilder eventType(EventType eventType) {
            this.eventType = eventType;
            return this;
        }

        public EventBuilder entityType(String entityType) {
            this.entityType = entityType;
            return this;
        }

        public EventBuilder entity(Map<String, String> entity) {
            this.entity = entity;
            return this;
        }

        public EventBuilder newValues(Map<String, String> newValues) {
            this.newValues = newValues;
            return this;
        }

        public EventBuilder entityDataTypes(Map<String, String> entityDataTypes) {
            this.entityDataTypes = entityDataTypes;
            return this;
        }

        public EventBuilder directSend(boolean directSend) {
            this.directSend = directSend;
            return this;
        }

        public EventBuilder exportEnabled(Boolean exportEnabled) {
            this.exportEnabled = exportEnabled;
            return this;
        }

        public EventBuilder copyFrom(Event other) {
            ncentralServerGuid = other.ncentralServerGuid;
            bizappsCustomerId = other.bizappsCustomerId.orElse(null);
            eventingConfigurationCustomerId = other.eventingConfigurationCustomerId;
            professionalModeLicenseType = other.professionalModeLicenseType;
            eventType = other.eventType;
            entityType = other.entityType;
            entity = other.entity;
            newValues = other.newValues;
            entityDataTypes = other.entityDataTypes;
            exportEnabled = other.exportEnabled;
            directSend = other.directSend;
            return this;
        }

        public Event build() {
            Event event = new Event();
            event.ncentralServerGuid = Objects.requireNonNull(ncentralServerGuid, "ncentralServerGuid cannot be null");
            event.bizappsCustomerId = Optional.ofNullable(bizappsCustomerId);
            event.eventingConfigurationCustomerId =
                    NullChecker.check(eventingConfigurationCustomerId, "eventingConfigurationCustomerId");
            event.professionalModeLicenseType =
                    Objects.requireNonNull(professionalModeLicenseType, "professionalModeLicenseType cannot be null");
            event.eventType = Objects.requireNonNull(eventType, "eventType cannot be null");
            event.entityType = Objects.requireNonNull(entityType, "entityType cannot be null");
            event.entity = Collections.unmodifiableMap(Objects.requireNonNull(entity, "entity cannot be null"));
            event.newValues =
                    Collections.unmodifiableMap(Objects.requireNonNull(newValues, "newValues cannot be null"));
            event.entityDataTypes = Collections.unmodifiableMap(
                    Objects.requireNonNull(entityDataTypes, "entity data types cannot be null"));
            event.directSend = directSend;
            event.exportEnabled = BooleanUtils.toBoolean(exportEnabled);
            return event;
        }
    }
}
