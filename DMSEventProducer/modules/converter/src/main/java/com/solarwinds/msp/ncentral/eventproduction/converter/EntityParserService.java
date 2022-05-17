package com.solarwinds.msp.ncentral.eventproduction.converter;

import com.google.protobuf.GeneratedMessageV3;

import com.solarwinds.msp.ncentral.eventproduction.api.entity.Event;

import java.util.List;
import java.util.Optional;

/**
 * This interface represents the business logic related to parsing of event entities/messages.
 *
 * @param <MESSAGE> the Message/Entity type.
 */
public interface EntityParserService<MESSAGE extends GeneratedMessageV3> {

    /**
     * Gets a new instance of the class that extends the {@link GeneratedMessageV3} class specified by the class name.
     *
     * @param className the class name to instantiate.
     * @return The new instance of the {@link GeneratedMessageV3} sub-class, or {@code Optional.empty()} if the instance
     * cannot be created.
     */
    Optional<MESSAGE> getNewInstance(String className);

    /**
     * Adds additional logic to the existing processed entity/message instance passed in.
     *
     * @param event the {@link Event} with data to be processed.
     * @param messageEntity the existing processed entity (message that extends {@link GeneratedMessageV3}).
     * @return The {@link List} of entities/messages - updated instances for the specified {@link Event}, or {@code
     * Optional.empty()} if it cannot find any parser or an error during parsing occurs.
     */
    Optional<List<MESSAGE>> parseRecord(Event event, MESSAGE messageEntity);
}
