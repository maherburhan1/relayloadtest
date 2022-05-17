package com.solarwinds.msp.ncentral.eventproduction.api.service.publisher;

import com.solarwinds.util.NullChecker;

import java.io.Serializable;
import java.util.Optional;

/**
 * Context information for an event publishing.
 */
public class PublishingContext implements Serializable {
    private String systemGuid;
    private String bizappsCustomerId;
    private int eventingConfigurationCustomerId;
    private String entityType;
    private boolean skipBuffer;

    private PublishingContext() {}

    /**
     * @return System GUID of the event origin
     */
    public String getSystemGuid() {
        return systemGuid;
    }

    /**
     * @return BizApps customer ID of the event origin
     */
    public Optional<String> getBizappsCustomerId() {
        return Optional.ofNullable(bizappsCustomerId);
    }

    /**
     * @return the eventing configuration customerId
     */
    public int getEventingConfigurationCustomerId() {
        return eventingConfigurationCustomerId;
    }

    /**
     * @return the entityType
     */
    public String getEntityType() {
        return entityType;
    }

    public boolean isSkipBuffer() {return skipBuffer;}

    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder for {@link PublishingContext} instances. Not thread-safe.
     */
    public static class Builder {
        private String systemGuid;
        private String bizappsCustomerId;
        private Integer eventingConfigurationCustomerId;
        private String entityType;
        private boolean skipBuffer;

        private Builder() {}

        public Builder withSystemGuid(String systemGuid) {
            this.systemGuid = systemGuid;
            return this;
        }

        public Builder withBizappsCustomerId(String bizappsCustomerId) {
            this.bizappsCustomerId = bizappsCustomerId;
            return this;
        }

        public Builder withEventingConfigurationCustomerId(Integer eventingConfigurationCustomerId) {
            this.eventingConfigurationCustomerId = eventingConfigurationCustomerId;
            return this;
        }

        public Builder withEntityType(String entityType) {
            this.entityType = entityType;
            return this;
        }

        public Builder withSkipBuffer(boolean skipBuffer) {
            this.skipBuffer = skipBuffer;
            return this;
        }

        public PublishingContext build() {
            PublishingContext publishingContext = buildCommon();
            publishingContext.entityType = NullChecker.check(entityType, "entityType");
            return publishingContext;
        }

        public PublishingContext buildForServerStatusEvent() {
            PublishingContext publishingContext = buildCommon();
            publishingContext.entityType = entityType;
            return publishingContext;
        }

        private PublishingContext buildCommon() {
            PublishingContext publishingContext = new PublishingContext();
            publishingContext.systemGuid = NullChecker.check(systemGuid, "systemGuid");
            publishingContext.bizappsCustomerId = bizappsCustomerId;
            publishingContext.eventingConfigurationCustomerId =
                    NullChecker.check(eventingConfigurationCustomerId, "eventingConfigurationCustomerId");
            publishingContext.skipBuffer = skipBuffer;
            return publishingContext;
        }
    }
}
