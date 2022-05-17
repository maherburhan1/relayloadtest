package com.solarwinds.msp.ncentral.eventproduction.api.service.pinging;

/**
 * Interface definition of the data holder object that interprets GRPC Health Check requests to the MSP Relay.
 */
public interface HealthCheckInfo {

    /**
     * Indicates whether the MSP Relay is ready to receive published events.
     *
     * @return Boolean indicating whether the Server's health is deemed OK and can receive requests.
     */
    boolean isHealthy();
}
