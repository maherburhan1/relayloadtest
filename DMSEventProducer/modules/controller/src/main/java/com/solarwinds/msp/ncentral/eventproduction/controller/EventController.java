package com.solarwinds.msp.ncentral.eventproduction.controller;

import com.solarwinds.msp.ncentral.eventproduction.api.service.publisher.PublishingContext;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Observer;

/**
 * Controller for publishing events.
 *
 * @param <T> event type
 */
public interface EventController<T> extends Observer {

    /**
     * Add events to be published into a send buffer, or, if publishingContext has skipBuffer flag ({@link
     * PublishingContext#isSkipBuffer()}), send event directly (and also use direct send for event send repeats).
     *
     * @param events the {@link List} of events to publish.
     * @param timestamp the timestamp of the events creation.
     * @param publishingContext the context information for events publishing.
     * @throws com.solarwinds.error.GenericRuntimeException if an error occurs in runtime.
     */
    void publishEvents(List<T> events, ZonedDateTime timestamp, PublishingContext publishingContext);
}
