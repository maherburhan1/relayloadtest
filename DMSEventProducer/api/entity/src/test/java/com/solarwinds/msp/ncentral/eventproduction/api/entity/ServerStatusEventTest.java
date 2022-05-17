package com.solarwinds.msp.ncentral.eventproduction.api.entity;

import org.junit.jupiter.api.Test;

import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ServerStatusEventTest {

    private static final String SERVER_GUID = "SERVER_GUID";
    private static final String TABLE_NAME = "TABLE_NAME";
    private static final String BIZAPPS_CUSTOMER_ID = "BIZAPPS_CUSTOMER_ID";
    private static final int EVENTING_CONFIGURATION_CUSTOMER_ID = 1;
    private final ZonedDateTime NOW = ZonedDateTime.now();
    private final List<EventHighWaterMark> EVENT_HIGH_WATER_MARKS =
            Collections.singletonList(EventHighWaterMark.builder().tableName(TABLE_NAME).build());

    @Test()
    void eventBuilder_checks_serverGuid_notNull() {
        final ServerStatusEvent.EventBuilder eventBuilder = ServerStatusEvent.builder()
                .highWaterMarks(null)
                .eventTime(NOW)
                .eventingConfigurationCustomerId(EVENTING_CONFIGURATION_CUSTOMER_ID)
                .eventType(ServerStatusEventType.UNKNOWN);

        assertThrows(NullPointerException.class, eventBuilder::build);
    }

    @Test()
    void eventBuilder_checks_eventType_notNull() {
        final ServerStatusEvent.EventBuilder eventBuilder = ServerStatusEvent.builder()
                .ncentralServerGuid(SERVER_GUID)
                .highWaterMarks(null)
                .eventTime(NOW)
                .eventingConfigurationCustomerId(EVENTING_CONFIGURATION_CUSTOMER_ID);

        assertThrows(NullPointerException.class, eventBuilder::build);
    }

    @Test()
    void eventBuilder_checks_eventTime_notNull() {
        final ServerStatusEvent.EventBuilder eventBuilder = ServerStatusEvent.builder()
                .ncentralServerGuid(SERVER_GUID)
                .highWaterMarks(null)
                .eventType(ServerStatusEventType.UNKNOWN)
                .eventingConfigurationCustomerId(EVENTING_CONFIGURATION_CUSTOMER_ID);

        assertThrows(NullPointerException.class, eventBuilder::build);
    }

    @Test()
    void eventBuilder_checks_eventingConfigurationCustomerId_notNull() {
        final ServerStatusEvent.EventBuilder eventBuilder = ServerStatusEvent.builder()
                .ncentralServerGuid(SERVER_GUID)
                .highWaterMarks(null)
                .eventType(ServerStatusEventType.UNKNOWN)
                .eventTime(NOW);

        assertThrows(NullPointerException.class, eventBuilder::build);
    }

    @Test()
    void eventBuilder_savesDataProperly_nullableFields_not_filled() {
        final ServerStatusEvent event = ServerStatusEvent.builder()
                .ncentralServerGuid(SERVER_GUID)
                .highWaterMarks(null)
                .eventTime(NOW)
                .eventType(ServerStatusEventType.UNKNOWN)
                .eventingConfigurationCustomerId(EVENTING_CONFIGURATION_CUSTOMER_ID)
                .build();

        assertThat(event.getEventTime()).isEqualTo(NOW);
        assertThat(event.getEventType()).isEqualTo(ServerStatusEventType.UNKNOWN);
        assertThat(event.getNcentralServerGuid()).isEqualTo(SERVER_GUID);
        assertThat(event.getEventingConfigurationCustomerId()).isEqualTo(EVENTING_CONFIGURATION_CUSTOMER_ID);
        assertThat(event.getBizappsCustomerId()).isEqualTo(Optional.empty());
        assertThat(event.getHighWaterMarks()).isEqualTo(Collections.emptyList());
    }

    @Test()
    void eventBuilder_savesDataProperly() {
        final ServerStatusEvent event = getFullyConfiguredEvent();

        assertThat(event.getEventTime()).isEqualTo(NOW);
        assertThat(event.getEventingConfigurationCustomerId()).isEqualTo(EVENTING_CONFIGURATION_CUSTOMER_ID);
        assertThat(event.getEventType()).isEqualTo(ServerStatusEventType.UNKNOWN);
        assertThat(event.getNcentralServerGuid()).isEqualTo(SERVER_GUID);
        assertThat(event.getBizappsCustomerId()).isEqualTo(Optional.of(BIZAPPS_CUSTOMER_ID));
        assertThat(event.getHighWaterMarks()).isEqualTo(EVENT_HIGH_WATER_MARKS);
    }

    @Test
    void testEventsEquality() {
        final ServerStatusEvent event1 = getFullyConfiguredEvent();
        final ServerStatusEvent event2 = getFullyConfiguredEvent();

        assertEquals(event1, event2);
        assertThat(event1.hashCode()).isEqualTo(event2.hashCode());
    }

    private ServerStatusEvent getFullyConfiguredEvent() {
        return ServerStatusEvent.builder()
                .ncentralServerGuid(SERVER_GUID)
                .bizappsCustomerId(BIZAPPS_CUSTOMER_ID)
                .highWaterMarks(EVENT_HIGH_WATER_MARKS)
                .eventTime(NOW)
                .eventType(ServerStatusEventType.UNKNOWN)
                .eventingConfigurationCustomerId(EVENTING_CONFIGURATION_CUSTOMER_ID)
                .build();
    }

}