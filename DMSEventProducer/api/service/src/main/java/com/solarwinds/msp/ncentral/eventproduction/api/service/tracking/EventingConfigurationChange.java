package com.solarwinds.msp.ncentral.eventproduction.api.service.tracking;

import java.util.OptionalInt;

/**
 * Representation of what has changed in eventing configuration.
 */
public class EventingConfigurationChange {

    private boolean sendingConfigurationChanged;
    private OptionalInt removeEventsForCustomer = OptionalInt.empty();

    public boolean isSendingConfigurationChanged() {
        return sendingConfigurationChanged;
    }

    /**
     * @return OptionalInt containing customer ID if any events should be removed for this customer, {@link
     * OptionalInt#empty()} if none should be removed
     */
    public OptionalInt getRemoveEventsForCustomer() {
        return removeEventsForCustomer;
    }

    /**
     * Sets sending configuration changed to true.
     *
     * @return updated state of object this call was invoked on
     */
    public EventingConfigurationChange setSendingConfigurationChanged() {
        this.sendingConfigurationChanged = true;
        return this;
    }

    /**
     * Sets info that events for given customer should be removed.
     *
     * @param customerId customer for who is configured eventing and whose events should be removed
     * @return updated state of object this call was invoked on
     */
    public EventingConfigurationChange setRemoveEventsForCustomer(int customerId) {
        this.removeEventsForCustomer = OptionalInt.of(customerId);
        return this;
    }

}
