package com.solarwinds.msp.ncentral.eventproduction.converter.impl.entity;

import com.google.protobuf.GeneratedMessageV3;
import com.google.protobuf.Timestamp;

import com.solarwinds.msp.ncentral.eventproduction.api.entity.Event;
import com.solarwinds.msp.ncentral.eventproduction.api.entity.EventType;
import com.solarwinds.msp.ncentral.eventproduction.converter.EventParser;
import com.solarwinds.msp.ncentral.proto.entity.ActionOuterClass;
import com.solarwinds.msp.ncentral.proto.entity.MspContextOuterClass;
import com.solarwinds.msp.ncentral.proto.entity.entity.ServiceInstanceOuterClass;
import com.solarwinds.msp.ncentral.proto.entity.entity.ServiceInstanceTypeOuterClass;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

class ServiceInstanceEventTest {

    private static Stream<Arguments> parseRecordTest() {
        EventParserTestCases testCases = new EventParserTestCases();
        ServiceInstanceOuterClass.ServiceInstance expectedResult;
        Event incomingEvent;
        String parsingAssertMessage;

        Map<String, String> entity = new HashMap<>();
        entity.put("lastupdated", "2019-02-21T13:51:52.068Z");

        //<editor-fold desc="Valid service instance event">
        parsingAssertMessage = "Valid service instance event";
        Map<String, String> entity1 = new HashMap<>(entity);

        entity1.put("deleted", "false");
        entity1.put("description", "Printer Status Test");
        entity1.put("customerid", "101");
        entity1.put("serviceid", "464817");
        entity1.put("displayname", "00 SNMP Printer Status Test");
        entity1.put("maxinstances", "100");
        entity1.put("displaystatus", "true");
        entity1.put("timetostale", "10000");
        entity1.put("moduletype", "Module");
        entity1.put("aggregatetasks", "false");
        entity1.put("schedulertype", "Cron-like Scheduler");
        entity1.put("minpollrate", "1");
        entity1.put("maxpollrate", "59");
        entity1.put("serviceinstancetype", "Single");
        entity1.put("servicetype", "SNMP");
        entity1.put("isgenericservice   ", "true");
        entity1.put("dashboardid", "");
        entity1.put("exportable", "true");
        entity1.put("version", "1.0.0.0");
        entity1.put("customservice", "true");
        entity1.put("help", "00 SNMP Printer Status Test");
        entity1.put("releasedependency  ", "1.0.0.0");
        entity1.put("serviceitemid", "30958");
        entity1.put("delimiter", "");
        entity1.put("displaylabel", "00 SNMP Printer Status Test");
        entity1.put("usesstockserviceitem", "false");

        incomingEvent = Event.builder()
                .entityDataTypes(Collections.EMPTY_MAP)
                .bizappsCustomerId("aaea6111-b13f-462b-9385-4f3baa7f0ccc")
                .ncentralServerGuid("225ed5bb-9e8e-4ad5-aa8c-7a018dc5ffa5")
                .eventingConfigurationCustomerId(1)
                .professionalModeLicenseType("Per Device")
                .eventType(EventType.INSERT)
                .entityType("service")
                .entity(entity1)
                .newValues(Collections.emptyMap())
                .build();

        expectedResult = ServiceInstanceOuterClass.ServiceInstance.newBuilder()
                .setDescription("Printer Status Test")
                .setName("00 SNMP Printer Status Test")
                .setServiceId(30958)
                .setServiceInstanceType(
                        ServiceInstanceTypeOuterClass.ServiceInstanceType.newBuilder().setName("SNMP").build())
                .setDeleted(false)
                .setServiceInstanceId(464817)
                .setLastUpdated(Timestamp.newBuilder().setSeconds(1550757112).setNanos(68000000).build())
                .setAction(ActionOuterClass.Action.ADD)
                .setContext(MspContextOuterClass.MspContext.newBuilder()
                        .setBizAppsCustomerId("aaea6111-b13f-462b-9385-4f3baa7f0ccc")
                        .setSystemGuid("225ed5bb-9e8e-4ad5-aa8c-7a018dc5ffa5")
                        .build())
                .build();

        testCases.setTestCase(incomingEvent, expectedResult, parsingAssertMessage);
        //</editor-fold>

        //<editor-fold desc="Service instance missing service id">
        parsingAssertMessage = "Service instance missing service id";
        Map<String, String> entity2 = new HashMap<>(entity);
        entity2.remove("serviceid");
        incomingEvent = Event.builder()
                .entityDataTypes(Collections.EMPTY_MAP)
                .bizappsCustomerId("aaea6111-b13f-462b-9385-4f3baa7f0ccc")
                .ncentralServerGuid("225ed5bb-9e8e-4ad5-aa8c-7a018dc5ffa5")
                .eventingConfigurationCustomerId(1)
                .professionalModeLicenseType("Per Device")
                .eventType(EventType.INSERT)
                .entityType("service")
                .entity(entity2)
                .newValues(Collections.emptyMap())
                .build();

        testCases.setTestCase(incomingEvent, parsingAssertMessage);
        //</editor-fold>

        //<editor-fold desc="Service instance missing display name">
        parsingAssertMessage = "Service instance missing display name";
        Map<String, String> entity3 = new HashMap<>(entity);
        entity3.remove("displayname");
        incomingEvent = Event.builder()
                .entityDataTypes(Collections.EMPTY_MAP)
                .bizappsCustomerId("aaea6111-b13f-462b-9385-4f3baa7f0ccc")
                .ncentralServerGuid("225ed5bb-9e8e-4ad5-aa8c-7a018dc5ffa5")
                .eventingConfigurationCustomerId(1)
                .professionalModeLicenseType("Per Device")
                .eventType(EventType.INSERT)
                .entityType("service")
                .entity(entity2)
                .newValues(Collections.emptyMap())
                .build();

        testCases.setTestCase(incomingEvent, parsingAssertMessage);
        //</editor-fold>

        //<editor-fold desc="Service instance missing service item id">
        parsingAssertMessage = "Service instance missing service item id";
        Map<String, String> entity4 = new HashMap<>(entity);
        entity4.remove("serviceitemid");
        incomingEvent = Event.builder()
                .entityDataTypes(Collections.EMPTY_MAP)
                .bizappsCustomerId("aaea6111-b13f-462b-9385-4f3baa7f0ccc")
                .ncentralServerGuid("225ed5bb-9e8e-4ad5-aa8c-7a018dc5ffa5")
                .eventingConfigurationCustomerId(1)
                .professionalModeLicenseType("Per Device")
                .eventType(EventType.INSERT)
                .entityType("service")
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
            List<com.google.protobuf.GeneratedMessageV3> expectedResult, String assertMessage) {
        Assertions.assertEquals(expectedResult, eventParser.parse(incomingEvent), assertMessage);
    }
}