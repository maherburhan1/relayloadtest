package com.solarwinds.msp.ncentral.eventproduction.converter.impl.entity;

import com.google.protobuf.GeneratedMessageV3;
import com.google.protobuf.Timestamp;

import com.solarwinds.msp.ncentral.eventproduction.api.entity.Event;
import com.solarwinds.msp.ncentral.eventproduction.api.entity.EventType;
import com.solarwinds.msp.ncentral.eventproduction.converter.EventParser;
import com.solarwinds.msp.ncentral.proto.entity.ActionOuterClass;
import com.solarwinds.msp.ncentral.proto.entity.MspContextOuterClass;
import com.solarwinds.msp.ncentral.proto.entity.tasks.BandwidthOuterClass;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

class BandwidthEventTest {

    private static Stream<Arguments> parseRecordTest() {
        EventParserTestCases testCases = new EventParserTestCases();
        BandwidthOuterClass.Bandwidth expectedResult;
        Event incomingEvent;
        String parsingAssertMessage;

        Map<String, String> bandwidthEntiry = new HashMap<>();
        bandwidthEntiry.put("taskid", "1907480434");
        bandwidthEntiry.put("scantime", "2019-02-21T13:51:52.068Z");
        bandwidthEntiry.put("lastupdated", "2019-02-21T13:51:52.068Z");

        //<editor-fold desc="Valid bandwidth event">
        parsingAssertMessage = "Valid bandwidth event";
        Map<String, String> bandwidthEntiry1 = new HashMap<>(bandwidthEntiry);
        bandwidthEntiry1.put("snmp400_bitsinpersec", "10000");
        bandwidthEntiry1.put("snmp400_bitsoutpersec", "1000");
        bandwidthEntiry1.put("snmp400_util", "20");
        bandwidthEntiry1.put("errormessge", "");
        bandwidthEntiry1.put("state", "3");
        incomingEvent = Event.builder()
                .entityDataTypes(Collections.EMPTY_MAP)
                .bizappsCustomerId("aaea6111-b13f-462b-9385-4f3baa7f0ccc")
                .ncentralServerGuid("225ed5bb-9e8e-4ad5-aa8c-7a018dc5ffa5")
                .eventingConfigurationCustomerId(1)
                .professionalModeLicenseType("Per Device")
                .eventType(EventType.INSERT)
                .entityType("datatraffic_detailed")
                .entity(bandwidthEntiry1)
                .newValues(Collections.emptyMap())
                .build();

        expectedResult = BandwidthOuterClass.Bandwidth.newBuilder()
                .setTaskId(1907480434)
                .setStateId(3)
                .setBitsInPerSec(10000)
                .setBitsOutPerSec(1000)
                .setUtilizationPercentage(20)
                .setErrorMessage("")
                .setScanTime(Timestamp.newBuilder().setSeconds(1550757112).setNanos(68000000).build())
                .setLastUpdated(Timestamp.newBuilder().setSeconds(1550757112).setNanos(68000000).build())
                .setAction(ActionOuterClass.Action.ADD)
                .setContext(MspContextOuterClass.MspContext.newBuilder()
                        .setBizAppsCustomerId("aaea6111-b13f-462b-9385-4f3baa7f0ccc")
                        .setSystemGuid("225ed5bb-9e8e-4ad5-aa8c-7a018dc5ffa5")
                        .build())
                .build();

        testCases.setTestCase(incomingEvent, expectedResult, parsingAssertMessage);
        //</editor-fold>

        //<editor-fold desc="Bandwidth event missing taskid">
        parsingAssertMessage = "Bandwidth event missing taskid";
        Map<String, String> bandwidthEntiry2 = new HashMap<>(bandwidthEntiry);
        bandwidthEntiry2.remove("taskid");
        incomingEvent = Event.builder()
                .entityDataTypes(Collections.EMPTY_MAP)
                .bizappsCustomerId("aaea6111-b13f-462b-9385-4f3baa7f0ccc")
                .ncentralServerGuid("225ed5bb-9e8e-4ad5-aa8c-7a018dc5ffa5")
                .eventingConfigurationCustomerId(1)
                .professionalModeLicenseType("Per Device")
                .eventType(EventType.INSERT)
                .entityType("datatraffic_detailed")
                .entity(bandwidthEntiry2)
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