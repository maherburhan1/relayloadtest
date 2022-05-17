package com.solarwinds.msp.ncentral.eventproduction.converter.impl.entity;

import com.google.protobuf.GeneratedMessageV3;
import com.google.protobuf.Timestamp;

import com.solarwinds.msp.ncentral.eventproduction.api.entity.Event;
import com.solarwinds.msp.ncentral.eventproduction.api.entity.EventType;
import com.solarwinds.msp.ncentral.eventproduction.converter.EventParser;
import com.solarwinds.msp.ncentral.proto.entity.ActionOuterClass;
import com.solarwinds.msp.ncentral.proto.entity.MspContextOuterClass;
import com.solarwinds.msp.ncentral.proto.entity.entity.ServiceOuterClass;
import com.solarwinds.msp.ncentral.proto.entity.entity.ServiceTypeOuterClass;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

class ServiceEventTest {

    private static Stream<Arguments> parseRecordTest() {
        EventParserTestCases testCases = new EventParserTestCases();
        ServiceOuterClass.Service expectedResult;
        Event incomingEvent;
        String parsingAssertMessage;

        Map<String, String> serviceEntity = new HashMap<>();
        serviceEntity.put("lastupdated", "2019-02-21T13:51:52.068Z");
        serviceEntity.put("serviceitemid", "220");

        //<editor-fold desc="Valid service event">
        parsingAssertMessage = "Valid service event";
        Map<String, String> serviceEntity1 = new HashMap<>(serviceEntity);
        serviceEntity1.put("description", "Patch Status v2");
        serviceEntity1.put("displayname", "Patch Status v2");
        serviceEntity1.put("deleted", "false");
        serviceEntity1.put("isavailability", "false");
        serviceEntity1.put("customerid", "50");
        serviceEntity1.put("serviceitemtypeid", "1");
        serviceEntity1.put("displaylabel", "AMP-based MDS Check");
        serviceEntity1.put("version", "4.5.0.2");
        serviceEntity1.put("customserviceitem", "false");

        incomingEvent = Event.builder()
                .entityDataTypes(Collections.EMPTY_MAP)
                .bizappsCustomerId("aaea6111-b13f-462b-9385-4f3baa7f0ccc")
                .ncentralServerGuid("225ed5bb-9e8e-4ad5-aa8c-7a018dc5ffa5")
                .eventingConfigurationCustomerId(1)
                .professionalModeLicenseType("Per Device")
                .eventType(EventType.INSERT)
                .entityType("serviceitem")
                .entity(serviceEntity1)
                .newValues(Collections.emptyMap())
                .build();

        expectedResult = ServiceOuterClass.Service.newBuilder()
                .setServiceId(220)
                .setName("Patch Status v2")
                .setDescription("Patch Status v2")
                .setDeleted(false)
                .setLastUpdated(Timestamp.newBuilder().setSeconds(1550757112).setNanos(68000000).build())
                .setClientId(50)
                .setIsAvailability(false)
                .setServiceType(ServiceTypeOuterClass.ServiceType.newBuilder().setServiceTypeId(1).build())
                .setAction(ActionOuterClass.Action.ADD)
                .setContext(MspContextOuterClass.MspContext.newBuilder()
                        .setBizAppsCustomerId("aaea6111-b13f-462b-9385-4f3baa7f0ccc")
                        .setSystemGuid("225ed5bb-9e8e-4ad5-aa8c-7a018dc5ffa5")
                        .build())
                .build();

        testCases.setTestCase(incomingEvent, expectedResult, parsingAssertMessage);
        //</editor-fold>

        //<editor-fold desc="Service event missing service id">
        parsingAssertMessage = "Service event missing service id";
        Map<String, String> serviceEntity2 = new HashMap<>(serviceEntity);
        serviceEntity2.remove("serviceitemid");
        incomingEvent = Event.builder()
                .entityDataTypes(Collections.EMPTY_MAP)
                .bizappsCustomerId("aaea6111-b13f-462b-9385-4f3baa7f0ccc")
                .ncentralServerGuid("225ed5bb-9e8e-4ad5-aa8c-7a018dc5ffa5")
                .eventingConfigurationCustomerId(1)
                .professionalModeLicenseType("Per Device")
                .eventType(EventType.INSERT)
                .entityType("serviceitem")
                .entity(serviceEntity2)
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