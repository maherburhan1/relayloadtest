package com.solarwinds.msp.ncentral.eventproduction.publisher.relay;

import com.nable.logging.Logger;
import com.nable.logging.Loggers;
import com.solarwinds.enumeration.event.EventRelayType;
import com.solarwinds.msp.ncentral.eventproduction.api.service.pinging.EventHealthCheck;
import com.solarwinds.msp.ncentral.eventproduction.api.service.pinging.HealthCheckInfo;
import com.solarwinds.msp.ncentral.eventproduction.api.service.tracking.EventingControlService;
import com.solarwinds.msp.ncentral.eventproduction.api.service.tracking.EventingStartupListener;
import com.solarwinds.msp.ncentral.eventproduction.publisher.relay.grpc.PublisherProvider;
import com.solarwinds.util.function.TracingFunction;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.URISyntaxException;
import java.rmi.RemoteException;
import java.util.Optional;

import io.grpc.health.v1.HealthCheckRequest;
import io.grpc.health.v1.HealthCheckResponse;
import io.grpc.health.v1.HealthGrpc;

/**
 * Responsible for sending a GRPC health check to the MSP Relay and determining if it is ready to receive requests.
 */
@Component
public class MspRelayHealthCheck implements EventHealthCheck, EventingStartupListener {

    private static final Logger logger = Loggers.EVENT_PRODUCER;
    private static final String FUNCTION_NAME = "MSP-Relay::Check";
    private static final HealthCheckInfo COULD_NOT_PING_RESPONSE = new MspRelayHealthCheckInfo(false);

    private final PublisherProvider publisherProvider;
    private HealthCheckRequest.Builder healthRequestBuilder;

    @Autowired
    public MspRelayHealthCheck(PublisherProvider publisherProvider, EventingControlService eventingControlService) {
        this.publisherProvider = publisherProvider;
        eventingControlService.addStartupListenerOrExecuteStartup(this);
    }

    @Override
    public void onEventingStart() {
        if (healthRequestBuilder == null) {
            healthRequestBuilder = HealthCheckRequest.newBuilder();
        }
        logger.info("Component {} initialized.", this.getClass().getSimpleName());
    }

    @Override
    public HealthCheckInfo ping(String bizappsCustomerId, EventRelayType relayType) {
        Optional<TracingFunction<HealthCheckRequest, HealthCheckResponse>> sendFunction =
                createTracingFunction(bizappsCustomerId, relayType);
        HealthCheckRequest healthCheckRequest = healthRequestBuilder.build();
        logger.debug("Sending the health check request [{}] to the MSP Relay for BizApps customer [{}].",
                healthCheckRequest.hashCode(), bizappsCustomerId);

        final HealthCheckResponse healthCheckResponse;
        if (sendFunction.isPresent()) {
            healthCheckResponse = sendFunction.get().apply(healthCheckRequest);
            logger.debug("Received the health check response [{}] from the MSP Relay for BizApps customer [{}].",
                    healthCheckResponse.hashCode(), bizappsCustomerId);
            return new MspRelayHealthCheckInfo(healthCheckResponse);
        } else {
            logger.warn("Could not send the health check request [{}] to the MSP Relay for [{}].",
                    healthCheckRequest.hashCode(), bizappsCustomerId.hashCode());
        }

        return COULD_NOT_PING_RESPONSE;
    }

    private Optional<TracingFunction<HealthCheckRequest, HealthCheckResponse>> createTracingFunction(
            String bizappsCustomerId, EventRelayType relayType) {
        try {
            HealthGrpc.HealthBlockingStub healthBlockingStub =
                    publisherProvider.getHealthBlockingStub(bizappsCustomerId, relayType);
            return Optional.of(new TracingFunction<>(healthBlockingStub::check, Loggers.EVENT_PRODUCER, FUNCTION_NAME));
        } catch (RemoteException | URISyntaxException e) {
            logger.error("An error was encountered while trying to retrieve the HealthCheck stub", e);
        }

        return Optional.empty();
    }
}
