package com.solarwinds.msp.ncentral.eventproduction.publisher.relay.grpc;

import com.solarwinds.msp.relay.Relay;

/**
 * Wrapper of {@link Relay.PublishRequest.Builder} representing batch of multiple request messages.
 */
public class Batch {
    private final Relay.PublishRequest.Builder requestBuilder;

    public Batch(Relay.Context context) { requestBuilder = Relay.PublishRequest.newBuilder().setContext(context); }

    public void addMessage(Relay.RequestMessage requestMessage) {
        requestBuilder.addMessages(requestMessage);
    }

    public int getMessagesCount() {
        return requestBuilder.getMessagesCount();
    }

    /**
     * Gets the bizapps customer ID associated with this batch of message requests.
     *
     * @return String representing bizapps customer ID.
     */
    public String getBizappsCustomerId() { return requestBuilder.getContext().getBizappsCustomerId(); }

    public Relay.PublishRequest buildRequest() {
        return requestBuilder.build();
    }
}
