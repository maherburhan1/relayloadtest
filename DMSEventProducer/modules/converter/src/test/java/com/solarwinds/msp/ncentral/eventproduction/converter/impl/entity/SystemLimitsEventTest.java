package com.solarwinds.msp.ncentral.eventproduction.converter.impl.entity;

import com.google.protobuf.GeneratedMessageV3;

import com.solarwinds.msp.ncentral.eventproduction.api.entity.Event;
import com.solarwinds.msp.ncentral.eventproduction.api.entity.EventType;
import com.solarwinds.msp.ncentral.eventproduction.converter.EventParser;
import com.solarwinds.msp.ncentral.proto.entity.ActionOuterClass;
import com.solarwinds.msp.ncentral.proto.entity.MspContextOuterClass;
import com.solarwinds.msp.ncentral.proto.entity.entity.SystemLimitsOuterClass;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class SystemLimitsEventTest {

    private static Stream<Arguments> parseRecordTest() {
        EventParserTestCases testCases = new EventParserTestCases();
        SystemLimitsOuterClass.SystemLimits expectedResult;
        Event incomingEvent;
        String parsingAssertMessage;

        Map<String, String> entity = new HashMap<>();
        entity.put("lastupdated", "2019-02-21T13:51:52.068Z");

        //<editor-fold desc="Valid system limits event">
        parsingAssertMessage = "Valid system limits event";
        Map<String, String> entity1 = new HashMap<>(entity);

        entity1.put("customerid", "101");
        entity1.put("shortdesc", "MaxCustomers");
        entity1.put("value", "999");

        incomingEvent = Event.builder()
                .entityDataTypes(Collections.EMPTY_MAP)
                .bizappsCustomerId("aaea6111-b13f-462b-9385-4f3baa7f0ccc")
                .ncentralServerGuid("225ed5bb-9e8e-4ad5-aa8c-7a018dc5ffa5")
                .eventingConfigurationCustomerId(1)
                .professionalModeLicenseType("Per Device")
                .eventType(EventType.INSERT)
                .entityType("systemlimits")
                .entity(entity1)
                .newValues(Collections.emptyMap())
                .build();

        expectedResult = SystemLimitsOuterClass.SystemLimits.newBuilder()
                .setClientId(101)
                .setShortDescription("MaxCustomers")
                .setValue(999)
                .setAction(ActionOuterClass.Action.ADD)
                .setContext(MspContextOuterClass.MspContext.newBuilder()
                        .setBizAppsCustomerId("aaea6111-b13f-462b-9385-4f3baa7f0ccc")
                        .setSystemGuid("225ed5bb-9e8e-4ad5-aa8c-7a018dc5ffa5")
                        .build())
                .build();

        testCases.setTestCase(incomingEvent, expectedResult, parsingAssertMessage);
        //</editor-fold>

        //<editor-fold desc="System limits event missing customer id">
        parsingAssertMessage = "System limits event missing customer id";
        Map<String, String> entity2 = new HashMap<>(entity);
        entity2.remove("customerid");
        incomingEvent = Event.builder()
                .entityDataTypes(Collections.EMPTY_MAP)
                .bizappsCustomerId("aaea6111-b13f-462b-9385-4f3baa7f0ccc")
                .ncentralServerGuid("225ed5bb-9e8e-4ad5-aa8c-7a018dc5ffa5")
                .eventingConfigurationCustomerId(1)
                .professionalModeLicenseType("Per Device")
                .eventType(EventType.INSERT)
                .entityType("systemlimits")
                .entity(entity2)
                .newValues(Collections.emptyMap())
                .build();

        testCases.setTestCase(incomingEvent, parsingAssertMessage);
        //</editor-fold>

        //<editor-fold desc="System limits event missing short description">
        parsingAssertMessage = "System limits event missing short description";
        Map<String, String> entity3 = new HashMap<>(entity);
        entity3.remove("shortdesc");
        incomingEvent = Event.builder()
                .entityDataTypes(Collections.EMPTY_MAP)
                .bizappsCustomerId("aaea6111-b13f-462b-9385-4f3baa7f0ccc")
                .ncentralServerGuid("225ed5bb-9e8e-4ad5-aa8c-7a018dc5ffa5")
                .eventingConfigurationCustomerId(1)
                .professionalModeLicenseType("Per Device")
                .eventType(EventType.INSERT)
                .entityType("systemlimits")
                .entity(entity2)
                .newValues(Collections.emptyMap())
                .build();

        testCases.setTestCase(incomingEvent, parsingAssertMessage);
        //</editor-fold>

        //<editor-fold desc="System limits event missing value">
        parsingAssertMessage = "System limits event missing value";
        Map<String, String> entity4 = new HashMap<>(entity);
        entity4.remove("value");
        incomingEvent = Event.builder()
                .entityDataTypes(Collections.EMPTY_MAP)
                .bizappsCustomerId("aaea6111-b13f-462b-9385-4f3baa7f0ccc")
                .ncentralServerGuid("225ed5bb-9e8e-4ad5-aa8c-7a018dc5ffa5")
                .eventingConfigurationCustomerId(1)
                .professionalModeLicenseType("Per Device")
                .eventType(EventType.INSERT)
                .entityType("systemlimits")
                .entity(entity4)
                .newValues(Collections.emptyMap())
                .build();

        testCases.setTestCase(incomingEvent, parsingAssertMessage);
        //</editor-fold>

        return testCases.toArguments();
    }

    @ParameterizedTest(name = "Run {index}: {3}")
    @MethodSource
    void parseRecordTest(EventParser<GeneratedMessageV3> eventParser, Event incomingEvent,
            List<SystemLimitsOuterClass.SystemLimits> expectedResult, String assertMessage) {
        List<SystemLimitsOuterClass.SystemLimits> result = eventParser.parse(incomingEvent)
                .stream()
                .map(e -> (SystemLimitsOuterClass.SystemLimits) e)
                .map(SystemLimitsOuterClass.SystemLimits::toBuilder)
                .map(SystemLimitsOuterClass.SystemLimits.Builder::clearLastUpdated)
                .map(SystemLimitsOuterClass.SystemLimits.Builder::build)
                .collect(Collectors.toList());

        Assertions.assertEquals(expectedResult, result, assertMessage);
    }
}