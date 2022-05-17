package com.solarwinds.msp.ncentral.eventproduction.converter.impl.entity;

import com.google.protobuf.GeneratedMessageV3;
import com.google.protobuf.Timestamp;

import com.solarwinds.msp.ncentral.eventproduction.api.entity.Event;
import com.solarwinds.msp.ncentral.eventproduction.api.entity.EventType;
import com.solarwinds.msp.ncentral.eventproduction.converter.EventParser;
import com.solarwinds.msp.ncentral.proto.entity.ActionOuterClass;
import com.solarwinds.msp.ncentral.proto.entity.MspContextOuterClass;
import com.solarwinds.msp.ncentral.proto.entity.tasks.AvSecurityThreatOuterClass;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

class AvSecurityThreatEventTest {

    private static Stream<Arguments> parseRecordTest() {
        EventParserTestCases testCases = new EventParserTestCases();
        AvSecurityThreatOuterClass.AvSecurityThreat expectedResult;
        Event incomingEvent;
        String parsingAssertMessage;

        Map<String, String> entity = new HashMap<>();
        entity.put("lastupdated", "2019-02-21T13:51:52.068Z");

        //<editor-fold desc="Valid AV security threat event">
        parsingAssertMessage = "Valid AV security threat event";
        Map<String, String> entity1 = new HashMap<>(entity);
        entity1.put("deviceid", "1355964618");
        entity1.put("event_time", "2019-02-21T13:51:52.068Z");
        entity1.put("sequence", "230752");
        entity1.put("maltype", "1");
        entity1.put("threattype", "0");
        entity1.put("threatname", "Generic.Application.CoinMiner.1.4.ACB1673");
        entity1.put("object", "C:\\Windows\\System32\\winlogui.exe");
        entity1.put("path", "C:\\Windows\\System32\\winlogui.exe");
        entity1.put("state", "2");
        entity1.put("quarid", "");
        entity1.put("action", "3");
        entity1.put("eventid", "77946");

        incomingEvent = Event.builder()
                .entityDataTypes(Collections.EMPTY_MAP)
                .bizappsCustomerId("aaea6111-b13f-462b-9385-4f3baa7f0ccc")
                .ncentralServerGuid("225ed5bb-9e8e-4ad5-aa8c-7a018dc5ffa5")
                .eventingConfigurationCustomerId(1)
                .professionalModeLicenseType("Per Device")
                .eventType(EventType.INSERT)
                .entityType("eventdata_malware")
                .entity(entity1)
                .newValues(Collections.emptyMap())
                .build();

        expectedResult = AvSecurityThreatOuterClass.AvSecurityThreat.newBuilder()
                .setDeviceId(1355964618)
                .setActionIds("3")
                .setAvSolutionName("AV Defender")
                .setEventTime(Timestamp.newBuilder().setSeconds(1550757112).setNanos(68000000).build())
                .setMalwareTypeId(1)
                .setSequence(230752)
                .setStateId(2)
                .setThreatName("Generic.Application.CoinMiner.1.4.ACB1673")
                .setThreatTypeId(0)
                .setLastUpdated(Timestamp.newBuilder().setSeconds(1550757112).setNanos(68000000).build())
                .setAction(ActionOuterClass.Action.ADD)
                .setContext(MspContextOuterClass.MspContext.newBuilder()
                        .setBizAppsCustomerId("aaea6111-b13f-462b-9385-4f3baa7f0ccc")
                        .setSystemGuid("225ed5bb-9e8e-4ad5-aa8c-7a018dc5ffa5")
                        .build())
                .build();

        testCases.setTestCase(incomingEvent, expectedResult, parsingAssertMessage);
        //</editor-fold>

        //<editor-fold desc="AV security threat missing device id">
        parsingAssertMessage = "AV security threat missing device id";
        Map<String, String> entity2 = new HashMap<>(entity);
        entity2.remove("deviceid");
        incomingEvent = Event.builder()
                .entityDataTypes(Collections.EMPTY_MAP)
                .bizappsCustomerId("aaea6111-b13f-462b-9385-4f3baa7f0ccc")
                .ncentralServerGuid("225ed5bb-9e8e-4ad5-aa8c-7a018dc5ffa5")
                .eventingConfigurationCustomerId(1)
                .professionalModeLicenseType("Per Device")
                .eventType(EventType.INSERT)
                .entityType("eventdata_malware")
                .entity(entity2)
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