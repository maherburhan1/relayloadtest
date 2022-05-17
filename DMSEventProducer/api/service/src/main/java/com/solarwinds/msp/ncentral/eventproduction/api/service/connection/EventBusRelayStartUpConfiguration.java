package com.solarwinds.msp.ncentral.eventproduction.api.service.connection;

/**
 * Request event bus relay host configuration from KeyBox on start up.
 */
public interface EventBusRelayStartUpConfiguration {

    /**
     * Verifies that event bus relay configuration is present for each bizAppCustomerId in the system. If not then it
     * invokes KeyBox to request relay host information for each bizAppCustomerId.
     */
    void requestMissingEventBusRelayConfiguration();
}