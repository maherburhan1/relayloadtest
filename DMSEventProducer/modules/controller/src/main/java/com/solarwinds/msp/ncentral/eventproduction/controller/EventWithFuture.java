package com.solarwinds.msp.ncentral.eventproduction.controller;

import com.solarwinds.msp.ncentral.eventproduction.api.service.persistence.TimestampedEvent;
import com.solarwinds.msp.ncentral.eventproduction.api.service.publisher.PublishedEventInfo;

import java.util.concurrent.Future;

public class EventWithFuture<T> extends TimestampedEvent<T> {
    private Future<PublishedEventInfo> future;

    public EventWithFuture(TimestampedEvent eventVal, Future<PublishedEventInfo> futureVal) {
        super(eventVal);
        future = futureVal;
    }

    public Future<PublishedEventInfo> getFuture() {
        return future;
    }

    public void setFuture(Future<PublishedEventInfo> future) {
        this.future = future;
    }

    @Override
    public boolean equals(Object compareObj) {
        if (!(compareObj instanceof EventWithFuture)) {
            return false;
        }

        EventWithFuture compareEvent = (EventWithFuture) compareObj;
        if (future != null) {
            return ((future.equals(compareEvent.future)) &&
                    (super.equals(compareEvent)));
        }
        return ((compareEvent.future != null) &&
                (super.equals(compareEvent)));
    }
}