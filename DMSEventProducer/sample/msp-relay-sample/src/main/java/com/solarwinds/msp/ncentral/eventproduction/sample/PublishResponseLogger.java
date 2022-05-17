package com.solarwinds.msp.ncentral.eventproduction.sample;

import com.solarwinds.msp.relay.PublisherGrpc;
import com.solarwinds.msp.relay.Relay;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletionException;
import java.util.stream.Collectors;

import io.grpc.stub.StreamObserver;

/**
 * This class represents the MSP Relay Publisher that simply log the received request and returns a response with
 * alternating status {@link Relay.ResponseMessage.ResponseStatus#OK} or {@link Relay.ResponseMessage.ResponseStatus#PUBLISH_ERROR}.
 */
public class PublishResponseLogger extends PublisherGrpc.PublisherImplBase {

    private static final Logger logger = LoggerFactory.getLogger(PublishResponseLogger.class);

    private volatile MspRelayConfiguration configuration = new MspRelayConfiguration();

    public MspRelayConfiguration getConfiguration() {
        return configuration;
    }

    public void setConfiguration(MspRelayConfiguration configuration) {
        if (configuration == null) {
            logger.warn("An attempt was made to set invalid configuration 'null'. Keeping the current one: {}",
                    this.configuration);
            return;
        }
        this.configuration = configuration;
    }

    @Override
    public void publish(Relay.PublishRequest request, StreamObserver<Relay.PublishResponse> responseObserver) {
        logger.info("Received request: [\n{}]\n", request.toString().replaceAll("(?m)^", "\t"));

        // Pause the request processing if required.
        if (configuration.getRequestDelayMilliseconds() > 0) {
            logger.info("Pausing request for {} milliseconds.", configuration.getRequestDelayMilliseconds());
            try {
                Thread.sleep(configuration.getRequestDelayMilliseconds());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new CompletionException("The processing of the request has been interrupted. No response.", e);
            }
        }

        // Create response with all response messages.
        final Relay.PublishResponse.Builder responseBuilder = Relay.PublishResponse.newBuilder();
        request.getMessagesList()
                .forEach(message -> responseBuilder.addResponseMessages(Relay.ResponseMessage.newBuilder()
                        .setId(message.getId())
                        .setStatus(configuration.getResponseStatus())
                        .build()));

        // Log the response.
        final String response = responseBuilder.getResponseMessagesList()
                .stream()
                .map(message -> String.format("[id:%s, status:%s]", message.getId(), message.getStatus()))
                .collect(Collectors.joining("\n\t"));
        logger.info("Sending response:\n\t{}", response);

        // Send the response.
        responseObserver.onNext(responseBuilder.build());
        responseObserver.onCompleted();
    }
}
