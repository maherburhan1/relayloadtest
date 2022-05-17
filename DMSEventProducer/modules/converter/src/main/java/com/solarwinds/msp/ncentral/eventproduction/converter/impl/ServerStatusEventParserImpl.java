package com.solarwinds.msp.ncentral.eventproduction.converter.impl;

import com.google.protobuf.GeneratedMessageV3;

import com.nable.logging.Logger;
import com.nable.logging.Loggers;
import com.solarwinds.msp.ncentral.eventproduction.api.entity.ServerStatusEvent;
import com.solarwinds.msp.ncentral.eventproduction.api.entity.Tools;
import com.solarwinds.msp.ncentral.eventproduction.converter.ServerStatusEventParser;
import com.solarwinds.msp.ncentral.proto.entity.MspSourceSystemEventOuterClass.MspSourceSystemEvent;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Component;

/**
 * Parses {@link ServerStatusEvent} object's event data into Protocol Buffers objects - descendants of {@link
 * GeneratedMessageV3} class.
 */
@Component
public class ServerStatusEventParserImpl implements ServerStatusEventParser<MspSourceSystemEvent> {

    private static final Logger logger = Loggers.EVENT_PRODUCER;

    @Override
    public MspSourceSystemEvent parse(ServerStatusEvent event) {
        logger.debug("Accepted server status event for conversion: {}", event);

        final MspSourceSystemEvent.Builder builder = MspSourceSystemEvent.newBuilder();
        builder.setContext(Tools.getMspContext(event))
                .setEventType(MspSourceSystemEvent.EventType.valueOf(event.getEventType().toString()))
                .setEventTime(Tools.toTimestamp(event.getEventTime()));

        if (CollectionUtils.isNotEmpty(event.getHighWaterMarks())) {
            event.getHighWaterMarks()
                    .forEach(eventHighWaterMark -> builder.addEventHighWaterMark(MspSourceSystemEvent.eventHighWaterMark
                            .newBuilder()
                            .setLastProcessed(Tools.toTimestamp(eventHighWaterMark.getLastProcessed()))
                            .setEntityName(eventHighWaterMark.getEntityName())));
        }
        return builder.build();
    }
}
