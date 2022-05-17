package com.solarwinds.msp.ncentral.eventproduction.converter.impl.entity;

import com.google.protobuf.GeneratedMessageV3;
import com.google.protobuf.Timestamp;

import com.solarwinds.msp.ncentral.eventproduction.api.entity.Event;
import com.solarwinds.msp.ncentral.eventproduction.api.entity.EventType;
import com.solarwinds.msp.ncentral.eventproduction.converter.EventParser;
import com.solarwinds.msp.ncentral.proto.entity.ActionOuterClass;
import com.solarwinds.msp.ncentral.proto.entity.MspContextOuterClass;
import com.solarwinds.msp.ncentral.proto.entity.asset.MappedDriveOuterClass;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class MappedDriveEventTest {

    private static final String BIZAPPS_CUSTOMER_ID = "aaea6111-b13f-462b-9385-4f3baa7f0ccc";
    private static final boolean DELETED = true;
    private static final int DEVICE_ID = 12345;
    private static final String DRIVE_NAME = "Drive name";
    private static final String REMOTE_PATH = "Remote path";
    private static final String NCENTRAL_SERVER_GUID = "225ed5bb-9e8e-4ad5-aa8c-7a018dc5ffa5";
    private static final Integer EVENTING_CONFIG_CUSTOMER_ID = 1;

    private static Stream<Arguments> parseRecordTest() {
        EventParserTestCases testCases = new EventParserTestCases();

        final Map<String, String> validEntity = new HashMap<>();
        validEntity.put("deviceid", String.valueOf(DEVICE_ID));
        validEntity.put("deleted", String.valueOf(DELETED));
        validEntity.put("drivename", DRIVE_NAME);
        validEntity.put("lastupdated", "2019-02-21T13:51:53.068Z");
        validEntity.put("remotepath", REMOTE_PATH);

        //<editor-fold desc="Valid MappedDrive event">
        {
            String parsingAssertMessage = "Valid MappedDrive event";
            Map<String, String> entity = new HashMap<>(validEntity);

            MappedDriveOuterClass.MappedDrive expectedResult = MappedDriveOuterClass.MappedDrive.newBuilder()
                    .setAction(ActionOuterClass.Action.ADD)
                    .setContext(MspContextOuterClass.MspContext.newBuilder()
                            .setBizAppsCustomerId(BIZAPPS_CUSTOMER_ID)
                            .setSystemGuid(NCENTRAL_SERVER_GUID)
                            .build())
                    .setDeleted(DELETED)
                    .setDeviceId(DEVICE_ID)
                    .setDriveName(DRIVE_NAME)
                    .setLastUpdated(Timestamp.newBuilder().setSeconds(1550757113).setNanos(68000000).build())
                    .setRemotePath(REMOTE_PATH)
                    .build();

            testCases.setTestCase(createEventForInsert(entity), expectedResult, parsingAssertMessage);
        }
        //</editor-fold>

        //<editor-fold desc="MappedDrive event missing device id">
        {
            String parsingAssertMessage = "MappedDrive event missing device id";
            Map<String, String> entity = new HashMap<>(validEntity);
            entity.remove("deviceid");

            testCases.setTestCase(createEventForInsert(entity), parsingAssertMessage);
        }
        //</editor-fold>

        //<editor-fold desc="MappedDrive event missing drive name">
        {
            String parsingAssertMessage = "MappedDrive event missing drive name";
            Map<String, String> entity = new HashMap<>(validEntity);
            entity.remove("drivename");

            testCases.setTestCase(createEventForInsert(entity), parsingAssertMessage);
        }
        //</editor-fold>

        //<editor-fold desc="MappedDrive update event without changed data">
        {
            String parsingAssertMessage = "MappedDrive update event without changed data";
            Map<String, String> entity = new HashMap<>(validEntity);

            testCases.setTestCase(createEvent(entity, Collections.emptyMap(), EventType.UPDATE), parsingAssertMessage);
        }
        //</editor-fold>

        return testCases.toArguments();
    }

    private static Event createEventForInsert(Map<String, String> entity) {
        return createEvent(entity, entity, EventType.INSERT);
    }

    private static Event createEvent(Map<String, String> entity, Map<String, String> newValues, EventType eventType) {
        return Event.builder()
                .entityDataTypes(Collections.EMPTY_MAP)
                .bizappsCustomerId(BIZAPPS_CUSTOMER_ID)
                .ncentralServerGuid(NCENTRAL_SERVER_GUID)
                .eventingConfigurationCustomerId(EVENTING_CONFIG_CUSTOMER_ID)
                .professionalModeLicenseType("Per Device")
                .eventType(eventType)
                .entityType("cim_mappeddrive")
                .entity(entity)
                .newValues(newValues)
                .build();
    }

    @ParameterizedTest(name = "Run {index}: {3}")
    @MethodSource
    void parseRecordTest(EventParser<GeneratedMessageV3> eventParser, Event incomingEvent,
            List<com.google.protobuf.GeneratedMessageV3> expectedResult, String assertMessage) {
        assertThat(eventParser.parse(incomingEvent)).isEqualTo(expectedResult).describedAs(assertMessage);
    }
}