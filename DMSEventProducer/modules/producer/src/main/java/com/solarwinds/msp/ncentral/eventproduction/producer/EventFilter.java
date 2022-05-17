package com.solarwinds.msp.ncentral.eventproduction.producer;

import com.solarwinds.msp.ncentral.eventproduction.api.entity.Event;

import java.util.function.Predicate;

/**
 * Determines if events having given customer ID and belonging to given entity name should be emitted .
 */
public interface EventFilter extends Predicate<Event> {

    /**
     * Determines if given event can be published.
     *
     * @param event to be published
     * @return {@code true} if the event can be published, {@code false} otherwise
     */
    @Override
    boolean test(Event event);
}