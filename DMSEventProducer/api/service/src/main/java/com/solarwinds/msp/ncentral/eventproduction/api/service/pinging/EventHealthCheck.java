package com.solarwinds.msp.ncentral.eventproduction.api.service.pinging;

import com.solarwinds.enumeration.event.EventRelayType;

/**
 * Interface definition for checking the health of the MSP Relay.
 */
public interface EventHealthCheck {

    /**
     * Sends a GRPC Health Check request to the MSP Relay to check connectivity state.
     *
     * @param bizappsCustomerId The Bizapps Customer ID to use for pinging MSP Relay.
     * @param relayType type of the relay.
     * @return Data object class that has interpreted the Health Check response from MSP Relay.
     */
    HealthCheckInfo ping(String bizappsCustomerId, EventRelayType relayType);
}
