package com.solarwinds.msp.ncentral.eventproduction.publisher.relay;

import com.solarwinds.msp.ncentral.eventproduction.api.service.pinging.HealthCheckInfo;

import io.grpc.health.v1.HealthCheckResponse;

/**
 * Accepts the response from the GRPC Health Check request and interprets the data.
 */
public class MspRelayHealthCheckInfo implements HealthCheckInfo {

    private final boolean isHealthy;

    public MspRelayHealthCheckInfo(HealthCheckResponse response) {
        this(response.getStatus().equals(HealthCheckResponse.ServingStatus.SERVING));
    }

    public MspRelayHealthCheckInfo(boolean isHealthy) {
        this.isHealthy = isHealthy;
    }

    @Override
    public boolean isHealthy() {
        return isHealthy;
    }
}
