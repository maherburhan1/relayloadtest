package com.solarwinds.msp.ncentral.eventproduction.publisher.relay;

import com.solarwinds.msp.ncentral.eventproduction.api.service.publisher.PublishedEventInfo;
import com.solarwinds.msp.relay.Relay.ResponseMessage;

import static com.solarwinds.msp.relay.Relay.ResponseMessage.ResponseStatus.OK;

public class MspRelayPublishedEventInfo implements PublishedEventInfo {

    private final boolean isSuccess;
    private final String info;

    public MspRelayPublishedEventInfo(ResponseMessage responseMessage) {
        isSuccess = (OK.equals(responseMessage.getStatus()));
        info = String.format("responseMessage=[%s]", responseMessage.toString());
    }

    @Override
    public boolean isSuccess() {
        return isSuccess;
    }

    @Override
    public String getInfo() {
        return info;
    }
}
