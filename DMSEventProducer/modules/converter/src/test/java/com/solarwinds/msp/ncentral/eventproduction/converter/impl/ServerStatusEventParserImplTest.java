package com.solarwinds.msp.ncentral.eventproduction.converter.impl;

import com.solarwinds.msp.ncentral.eventproduction.api.entity.EventHighWaterMark;
import com.solarwinds.msp.ncentral.eventproduction.api.entity.ServerStatusEvent;
import com.solarwinds.msp.ncentral.eventproduction.api.entity.ServerStatusEventType;
import com.solarwinds.msp.ncentral.eventproduction.api.entity.Tools;
import com.solarwinds.msp.ncentral.proto.entity.MspContextOuterClass.MspContext;
import com.solarwinds.msp.ncentral.proto.entity.MspSourceSystemEventOuterClass.MspSourceSystemEvent;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.ZonedDateTime;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
public class ServerStatusEventParserImplTest {

    private static final String BIZAPPS_CUSTOMER_ID = "1111";
    private static final String NCENTRAL_SERVER_GUID = "9999";
    private static final String ENTITY_NAME = "entity";
    private static final String TABLE_NAME = "table";
    private static final int EVENTING_CONFIGURATION_CUSTOMER_ID = 1;
    private static final int CUSTOMER_ID = 50;
    @InjectMocks
    private ServerStatusEventParserImpl parser;

    @Test
    public void parse() {
        final ZonedDateTime now = ZonedDateTime.now();
        final ServerStatusEvent sourceEvent = ServerStatusEvent.builder()
                .eventType(ServerStatusEventType.EVENTING_FRESH_START)
                .eventTime(now)
                .bizappsCustomerId(BIZAPPS_CUSTOMER_ID)
                .ncentralServerGuid(NCENTRAL_SERVER_GUID)
                .eventingConfigurationCustomerId(EVENTING_CONFIGURATION_CUSTOMER_ID)
                .highWaterMarks(Collections.singletonList(EventHighWaterMark.builder()
                        .entityName(ENTITY_NAME)
                        .tableName(TABLE_NAME)
                        .customerId(CUSTOMER_ID)
                        .lastProcessed(now)
                        .build()))
                .build();

        final MspSourceSystemEvent expectedConvertedEvent = MspSourceSystemEvent.newBuilder()
                .setContext(MspContext.newBuilder()
                        .setBizAppsCustomerId(BIZAPPS_CUSTOMER_ID)
                        .setSystemGuid(NCENTRAL_SERVER_GUID)
                        .build())
                .setEventType(MspSourceSystemEvent.EventType.EVENTING_FRESH_START)
                .setEventTime(Tools.toTimestamp(now))
                .addAllEventHighWaterMark(Collections.singletonList(MspSourceSystemEvent.eventHighWaterMark.newBuilder()
                        .setEntityName(ENTITY_NAME)
                        .setLastProcessed(Tools.toTimestamp(now))
                        .build()))
                .build();

        final MspSourceSystemEvent convertedEvent = parser.parse(sourceEvent);

        assertThat(convertedEvent).isEqualTo(expectedConvertedEvent);
    }
}
