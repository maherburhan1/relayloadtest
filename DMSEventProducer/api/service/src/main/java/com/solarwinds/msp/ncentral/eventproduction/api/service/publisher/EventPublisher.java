package com.solarwinds.msp.ncentral.eventproduction.api.service.publisher;

import java.util.Optional;
import java.util.concurrent.Future;

/**
 * Event publisher API.
 *
 * @param <T> event type
 */
public interface EventPublisher<T> {
    /**
     * Publishes an event.
     *
     * @param event {@link T} event instance to publish
     * @param publishingContext with additional information needed to publish the event
     * @return {@link Future} with information on the published event. Return empty {@link Optional} when event cannot
     * be published.
     * @throws InterruptedException if interrupted while waiting
     */
    Optional<Future<PublishedEventInfo>> publish(T event, PublishingContext publishingContext)
            throws InterruptedException;
}
