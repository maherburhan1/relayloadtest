package com.solarwinds.msp.ncentral.eventproduction.converter.impl.entity;

import com.google.protobuf.GeneratedMessageV3;

import com.solarwinds.msp.ncentral.eventproduction.api.entity.Event;

import java.util.List;

/**
 * This interface represents the Event/Message/Entity Parser.
 *
 * @param <MESSAGE> the Message/Entity type.
 */
@FunctionalInterface
public interface EntityParser<MESSAGE extends GeneratedMessageV3> {

    /**
     * Adds additional logic to the existing processed entity/message instance passed in.
     *
     * @param event the {@link Event} with data to be processed.
     * @param messageEntity the existing processed entity (message that extends {@link GeneratedMessageV3}).
     * @return The {@link List} of entities/messages - updated instances for the specified {@link Event}.
     */
    List<MESSAGE> parseRecord(Event event, MESSAGE messageEntity);
}
