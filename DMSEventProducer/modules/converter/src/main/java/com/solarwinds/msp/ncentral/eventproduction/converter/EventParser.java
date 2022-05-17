package com.solarwinds.msp.ncentral.eventproduction.converter;

import com.solarwinds.msp.ncentral.eventproduction.api.entity.Event;

import java.util.List;

/**
 * Parser of {@link Event} object's string data into {@link T} objects instances.
 *
 * @param <T> base type of parsing results
 */
public interface EventParser<T> {
    /**
     * Parses {@link Event} object into a {@link List} of {@code T} object instances.
     *
     * @param event {@link Event} object
     * @return {@link List} of event protobuf {@code T} object instances
     */
    List<T> parse(Event event);

    /**
     * Parses {@link Event} object into a {@link List} of {@code T} object instances.
     *
     * @param event {@link Event} object
     * @param serviceProcessingRule {@link ServiceProcessingRules} Enumeration on how to parse event     *
     * @return {@link List} of event protobuf {@code T} object instances
     */
    List<T> parse(Event event, ServiceProcessingRules serviceProcessingRule);
}