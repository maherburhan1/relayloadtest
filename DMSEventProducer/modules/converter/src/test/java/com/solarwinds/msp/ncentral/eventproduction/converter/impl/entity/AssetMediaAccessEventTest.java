package com.solarwinds.msp.ncentral.eventproduction.converter.impl.entity;

import com.google.protobuf.GeneratedMessageV3;
import com.google.protobuf.Timestamp;

import com.solarwinds.msp.ncentral.eventproduction.api.entity.Event;
import com.solarwinds.msp.ncentral.eventproduction.api.entity.EventType;
import com.solarwinds.msp.ncentral.eventproduction.converter.EventParser;
import com.solarwinds.msp.ncentral.proto.entity.ActionOuterClass;
import com.solarwinds.msp.ncentral.proto.entity.MspContextOuterClass;
import com.solarwinds.msp.ncentral.proto.entity.asset.AssetMediaAccessOuterClass;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

class AssetMediaAccessEventTest {

    private static Stream<Arguments> parseRecordTest() {
        EventParserTestCases testCases = new EventParserTestCases();
        AssetMediaAccessOuterClass.AssetMediaAccess expectedResult;
        Event incomingEvent;
        String parsingAssertMessage;

        Map<String, String> assetMediaAccessEntity = new HashMap<>();
        assetMediaAccessEntity.put("deviceid", "1907480434");
        assetMediaAccessEntity.put("lastupdated", "2019-02-21T13:51:52.068Z");
        assetMediaAccessEntity.put("deleted", "false");
        assetMediaAccessEntity.put("uniqueid", "\\\\.\\PHYSICALDRIVE1");
        assetMediaAccessEntity.put("mediatype", "External hard disk media");
        assetMediaAccessEntity.put("capacity", "500105249280");
        assetMediaAccessEntity.put("capabilities", "3, 4");
        assetMediaAccessEntity.put("modelnumber", "null");
        assetMediaAccessEntity.put("serialnumber", "null");
        assetMediaAccessEntity.put("drive_name", "null");
        assetMediaAccessEntity.put("vendor", "null");

        parsingAssertMessage = "Valid asset media access event";
        incomingEvent = Event.builder()
                .entityDataTypes(Collections.EMPTY_MAP)
                .bizappsCustomerId("aaea6111-b13f-462b-9385-4f3baa7f0ccc")
                .ncentralServerGuid("225ed5bb-9e8e-4ad5-aa8c-7a018dc5ffa5")
                .eventingConfigurationCustomerId(1)
                .professionalModeLicenseType("Per Device")
                .eventType(EventType.INSERT)
                .entityType("cim_mediaaccessdevice")
                .entity(assetMediaAccessEntity)
                .newValues(Collections.emptyMap())
                .build();

        expectedResult = AssetMediaAccessOuterClass.AssetMediaAccess.newBuilder()
                .setSizeMb(476937.53173828125)
                .setDeviceId(1907480434)
                .setDescription("\\\\.\\PHYSICALDRIVE1")
                .setMediaType("External hard disk media")
                .setLastUpdated(Timestamp.newBuilder().setSeconds(1550757112).setNanos(68000000).build())
                .setAction(ActionOuterClass.Action.ADD)
                .setContext(MspContextOuterClass.MspContext.newBuilder()
                        .setBizAppsCustomerId("aaea6111-b13f-462b-9385-4f3baa7f0ccc")
                        .setSystemGuid("225ed5bb-9e8e-4ad5-aa8c-7a018dc5ffa5")
                        .build())
                .build();

        testCases.setTestCase(incomingEvent, expectedResult, parsingAssertMessage);

        parsingAssertMessage = "Asset media access event with null device id";
        Map<String, String> assetMediaAccessEntity2 = new HashMap<>(assetMediaAccessEntity);
        assetMediaAccessEntity2.replace("deviceid", null);
        incomingEvent = Event.builder()
                .entityDataTypes(Collections.EMPTY_MAP)
                .bizappsCustomerId("aaea6111-b13f-462b-9385-4f3baa7f0ccc")
                .ncentralServerGuid("225ed5bb-9e8e-4ad5-aa8c-7a018dc5ffa5")
                .eventingConfigurationCustomerId(1)
                .professionalModeLicenseType("Per Device")
                .eventType(EventType.INSERT)
                .entityType("cim_mediaaccessdevice")
                .entity(assetMediaAccessEntity2)
                .newValues(Collections.emptyMap())
                .build();

        testCases.setTestCase(incomingEvent, parsingAssertMessage);

        parsingAssertMessage = "Asset media access event with null lastupdated";
        Map<String, String> assetMediaAccessEntity3 = new HashMap<>(assetMediaAccessEntity);
        assetMediaAccessEntity3.replace("lastupdated", null);
        incomingEvent = Event.builder()
                .entityDataTypes(Collections.EMPTY_MAP)
                .bizappsCustomerId("aaea6111-b13f-462b-9385-4f3baa7f0ccc")
                .ncentralServerGuid("225ed5bb-9e8e-4ad5-aa8c-7a018dc5ffa5")
                .eventingConfigurationCustomerId(1)
                .professionalModeLicenseType("Per Device")
                .eventType(EventType.INSERT)
                .entityType("cim_mediaaccessdevice")
                .entity(assetMediaAccessEntity3)
                .newValues(Collections.emptyMap())
                .build();

        testCases.setTestCase(incomingEvent, parsingAssertMessage);

        parsingAssertMessage = "Asset media access event with non number capacity";
        Map<String, String> assetMediaAccessEntity4 = new HashMap<>(assetMediaAccessEntity);
        assetMediaAccessEntity4.replace("capacity", "ABC123");
        incomingEvent = Event.builder()
                .entityDataTypes(Collections.EMPTY_MAP)
                .bizappsCustomerId("aaea6111-b13f-462b-9385-4f3baa7f0ccc")
                .ncentralServerGuid("225ed5bb-9e8e-4ad5-aa8c-7a018dc5ffa5")
                .eventingConfigurationCustomerId(1)
                .professionalModeLicenseType("Per Device")
                .eventType(EventType.INSERT)
                .entityType("cim_mediaaccessdevice")
                .entity(assetMediaAccessEntity4)
                .newValues(Collections.emptyMap())
                .build();
        expectedResult = AssetMediaAccessOuterClass.AssetMediaAccess.newBuilder()
                .setDeviceId(1907480434)
                .setDescription("\\\\.\\PHYSICALDRIVE1")
                .setMediaType("External hard disk media")
                .setLastUpdated(Timestamp.newBuilder().setSeconds(1550757112).setNanos(68000000).build())
                .setAction(ActionOuterClass.Action.ADD)
                .setContext(MspContextOuterClass.MspContext.newBuilder()
                        .setBizAppsCustomerId("aaea6111-b13f-462b-9385-4f3baa7f0ccc")
                        .setSystemGuid("225ed5bb-9e8e-4ad5-aa8c-7a018dc5ffa5")
                        .build())
                .build();

        testCases.setTestCase(incomingEvent, expectedResult, parsingAssertMessage);

        return testCases.toArguments();
    }

    @ParameterizedTest(name = "Run {index}: {3}")
    @MethodSource
    void parseRecordTest(EventParser<GeneratedMessageV3> eventParser, Event incomingEvent,
            List<GeneratedMessageV3> expectedResult, String assertMessage) {
        Assertions.assertEquals(expectedResult, eventParser.parse(incomingEvent), assertMessage);
    }
}