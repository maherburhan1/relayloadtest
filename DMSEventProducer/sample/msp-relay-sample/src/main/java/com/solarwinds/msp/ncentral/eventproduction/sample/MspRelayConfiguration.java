package com.solarwinds.msp.ncentral.eventproduction.sample;

import com.solarwinds.msp.relay.Relay;

/**
 * This class represents the MSP Relay Server Behaviour Configuration.
 */
public class MspRelayConfiguration {

    public static final String PREFIX_RESPONSE_STATUS = "response_status";
    public static final String PREFIX_REQUEST_DELAY = "request_delay";
    public static final String RESPONSE_STATUS_PUBLISH_ERROR = "PUBLISH_ERROR";

    private Relay.ResponseMessage.ResponseStatus responseStatus = Relay.ResponseMessage.ResponseStatus.OK;
    private long requestDelayMilliseconds = 0L;

    public Relay.ResponseMessage.ResponseStatus getResponseStatus() {
        return responseStatus;
    }

    public void setResponseStatus(Relay.ResponseMessage.ResponseStatus responseStatus) {
        this.responseStatus = responseStatus;
    }

    public long getRequestDelayMilliseconds() {
        return requestDelayMilliseconds;
    }

    public void setRequestDelayMilliseconds(long requestDelayMilliseconds) {
        this.requestDelayMilliseconds = requestDelayMilliseconds;
    }

    @Override
    public String toString() {
        return String.format("%s: responseStatus = %s, requestDelay = %s", getClass().getSimpleName(), responseStatus,
                requestDelayMilliseconds);
    }
}
