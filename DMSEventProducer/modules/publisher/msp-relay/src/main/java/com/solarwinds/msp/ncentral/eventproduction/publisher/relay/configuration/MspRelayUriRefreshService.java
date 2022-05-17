package com.solarwinds.msp.ncentral.eventproduction.publisher.relay.configuration;

import java.util.Observable;

/**
 * Service for retrieving the most recent MSP EventBus Relay URI for specified Business Applications Customer ID.
 */
public abstract class MspRelayUriRefreshService extends Observable {

    /**
     * Requests the latest MSP EventBus Relay URI. If the URI has been updated, then all observers are notified of this
     * change using the {@link MspRelayConfigurationChange} object instance.
     *
     * @param businessApplicationsCustomerId the Business Applications Customer ID to retrieve configuration for.
     */
    public abstract void requestEventBusRelayUri(String businessApplicationsCustomerId);
}