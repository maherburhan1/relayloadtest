package com.solarwinds.msp.ncentral.eventproduction.converter.impl.entity;

import com.google.protobuf.GeneratedMessageV3;
import com.google.protobuf.Timestamp;

import com.solarwinds.msp.ncentral.eventproduction.api.entity.Event;
import com.solarwinds.msp.ncentral.eventproduction.api.entity.EventType;
import com.solarwinds.msp.ncentral.eventproduction.converter.EventParser;
import com.solarwinds.msp.ncentral.proto.entity.ActionOuterClass;
import com.solarwinds.msp.ncentral.proto.entity.MspContextOuterClass;
import com.solarwinds.msp.ncentral.proto.entity.tasks.DeviceLoginsOuterClass;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

class DeviceLoginsEventTest {

    private static Stream<Arguments> parseRecordTest() {
        EventParserTestCases testCases = new EventParserTestCases();
        DeviceLoginsOuterClass.DeviceLogins expectedResult;
        Event incomingEvent;
        String parsingAssertMessage;

        Map<String, String> deviceLoginsEntity = new HashMap<>();
        deviceLoginsEntity.put("lastupdated", "2019-02-21T13:51:52.068Z");
        deviceLoginsEntity.put("deviceid", "30185527");

        //<editor-fold desc="Valid DeviceLogins event">
        parsingAssertMessage = "Valid DeviceLogins event";
        Map<String, String> deviceLoginsEntity1 = new HashMap<>(deviceLoginsEntity);
        deviceLoginsEntity1.put("username", "user1");
        deviceLoginsEntity1.put("userdomain", "domain1");
        deviceLoginsEntity1.put("deleted", "false");
        deviceLoginsEntity1.put("stillloggedin", "false");

        incomingEvent = Event.builder()
                .entityDataTypes(Collections.EMPTY_MAP)
                .bizappsCustomerId("aaea6111-b13f-462b-9385-4f3baa7f0ccc")
                .ncentralServerGuid("225ed5bb-9e8e-4ad5-aa8c-7a018dc5ffa5")
                .eventingConfigurationCustomerId(1)
                .professionalModeLicenseType("Per Device")
                .eventType(EventType.INSERT)
                .entityType("cim_custom_lastloggedinuser")
                .entity(deviceLoginsEntity1)
                .newValues(Collections.emptyMap())
                .build();

        expectedResult = DeviceLoginsOuterClass.DeviceLogins.newBuilder()
                .setDeviceId(30185527)
                .setDeleted(false)
                .setUserName("user1")
                .setDomain("domain1")
                .setStillLoggedIn(false)
                .setLastUpdated(Timestamp.newBuilder().setSeconds(1550757112).setNanos(68000000).build())
                .setAction(ActionOuterClass.Action.ADD)
                .setContext(MspContextOuterClass.MspContext.newBuilder()
                        .setBizAppsCustomerId("aaea6111-b13f-462b-9385-4f3baa7f0ccc")
                        .setSystemGuid("225ed5bb-9e8e-4ad5-aa8c-7a018dc5ffa5")
                        .build())
                .build();

        testCases.setTestCase(incomingEvent, expectedResult, parsingAssertMessage);
        //</editor-fold>

        //<editor-fold desc="DeviceLogins event missing device id">
        parsingAssertMessage = "DeviceLogins event missing device id";
        Map<String, String> deviceLoginsEntity2 = new HashMap<>(deviceLoginsEntity);
        deviceLoginsEntity2.remove("deviceid");
        incomingEvent = Event.builder()
                .entityDataTypes(Collections.EMPTY_MAP)
                .bizappsCustomerId("aaea6111-b13f-462b-9385-4f3baa7f0ccc")
                .ncentralServerGuid("225ed5bb-9e8e-4ad5-aa8c-7a018dc5ffa5")
                .eventingConfigurationCustomerId(1)
                .professionalModeLicenseType("Per Device")
                .eventType(EventType.INSERT)
                .entityType("cim_custom_lastloggedinuser")
                .entity(deviceLoginsEntity2)
                .newValues(Collections.emptyMap())
                .build();

        testCases.setTestCase(incomingEvent, parsingAssertMessage);
        //</editor-fold>

        return testCases.toArguments();
    }

    @ParameterizedTest(name = "Run {index}: {3}")
    @MethodSource
    void parseRecordTest(EventParser<GeneratedMessageV3> eventParser, Event incomingEvent,
            List<com.google.protobuf.GeneratedMessageV3> expectedResult, String assertMessage) {
        Assertions.assertEquals(expectedResult, eventParser.parse(incomingEvent), assertMessage);
    }
}