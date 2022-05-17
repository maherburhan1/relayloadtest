package com.solarwinds.msp.ncentral.eventproduction.api.service.tracking;

import com.solarwinds.util.NullChecker;

/**
 * Tracking of state for tables using eventing.
 */
public class EventTableStateChange {

    private int customerId;
    private String tableName;
    private EventTableScrapingState scrapingState;

    private EventTableStateChange() {}

    public int getCustomerId() {
        return customerId;
    }

    public String getTableName() {
        return tableName;
    }

    public EventTableScrapingState getScrapingState() {
        return scrapingState;
    }

    /**
     * @return {@link EventTableStateChangeBuilder} builder for creating {@link EventTableStateChange} instances.
     */
    public static EventTableStateChangeBuilder builder() {
        return new EventTableStateChangeBuilder();
    }

    /**
     * Builder class for {@link EventTableStateChange} instances.
     */
    public static final class EventTableStateChangeBuilder {
        private String tableName;
        private int customerId;
        private EventTableScrapingState scrapingState;

        private EventTableStateChangeBuilder() {}

        public EventTableStateChangeBuilder tableName(String tableName) {
            this.tableName = tableName;
            return this;
        }

        public EventTableStateChangeBuilder customerId(int customerId) {
            this.customerId = customerId;
            return this;
        }

        public EventTableStateChangeBuilder scrapingState(EventTableScrapingState scrapingState) {
            this.scrapingState = scrapingState;
            return this;
        }

        public EventTableStateChange build() {
            EventTableStateChange stateChange = new EventTableStateChange();
            stateChange.customerId = NullChecker.check(customerId, "customerId");
            stateChange.tableName = NullChecker.check(tableName, "tableName");
            stateChange.scrapingState = NullChecker.check(scrapingState, "scrapingState");
            return stateChange;
        }
    }

}
