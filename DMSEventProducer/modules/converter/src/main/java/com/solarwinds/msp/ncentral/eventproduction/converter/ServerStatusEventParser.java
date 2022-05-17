package com.solarwinds.msp.ncentral.eventproduction.converter;

import com.solarwinds.msp.ncentral.eventproduction.api.entity.ServerStatusEvent;

import java.util.List;

/**
 * Parser of {@link ServerStatusEvent} object's string data into {@link T} objects instances.
 *
 * @param <T> base type of parsing results
 */
public interface ServerStatusEventParser<T> {
    /**
     * Parses {@link ServerStatusEvent} object into a {@link List} of {@code T} object instances.
     *
     * @param event {@link ServerStatusEvent} object
     * @return event protobuf {@code T} object instance
     */
    T parse(ServerStatusEvent event);

}