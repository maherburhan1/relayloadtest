package com.solarwinds.msp.ncentral.eventproduction.sample;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.grpc.health.v1.HealthCheckRequest;
import io.grpc.health.v1.HealthCheckResponse;
import io.grpc.health.v1.HealthCheckResponse.ServingStatus;
import io.grpc.health.v1.HealthGrpc;
import io.grpc.stub.StreamObserver;

/**
 * This class represents the MSP Relay Health Service that simply log the received request and returns a response with
 * status {@link ServingStatus#SERVING}.
 */
final class MspRelayHealthService extends HealthGrpc.HealthImplBase {

    private static final Logger logger = LoggerFactory.getLogger(MspRelayHealthService.class.getName());

    MspRelayHealthService() {
        logger.info(this.getClass().toString() + " has been initialized.");
    }

    @Override
    public void check(HealthCheckRequest request, StreamObserver<HealthCheckResponse> responseObserver) {
        sendHealthCheckResponseOk(request, responseObserver);
    }

    @Override
    public void watch(HealthCheckRequest request, final StreamObserver<HealthCheckResponse> responseObserver) {
        sendHealthCheckResponseOk(request, responseObserver);
    }

    private void sendHealthCheckResponseOk(HealthCheckRequest request,
            StreamObserver<HealthCheckResponse> responseObserver) {
        logger.info("Received HealthCheckRequest: [\n{}]\n", request.toString().replaceAll("(?m)^", "\t"));
        final HealthCheckResponse response = HealthCheckResponse.newBuilder().setStatus(ServingStatus.SERVING).build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}
