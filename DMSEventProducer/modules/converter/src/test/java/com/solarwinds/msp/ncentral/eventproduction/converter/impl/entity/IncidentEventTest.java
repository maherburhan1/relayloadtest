package com.solarwinds.msp.ncentral.eventproduction.converter.impl.entity;

import com.google.protobuf.GeneratedMessageV3;
import com.google.protobuf.Timestamp;

import com.solarwinds.msp.ncentral.eventproduction.api.entity.Event;
import com.solarwinds.msp.ncentral.eventproduction.api.entity.EventType;
import com.solarwinds.msp.ncentral.eventproduction.converter.EventParser;
import com.solarwinds.msp.ncentral.proto.entity.ActionOuterClass;
import com.solarwinds.msp.ncentral.proto.entity.MspContextOuterClass;
import com.solarwinds.msp.ncentral.proto.entity.tasks.IncidentOuterClass;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

class IncidentEventTest {

    private static Stream<Arguments> parseRecordTest() {
        EventParserTestCases testCases = new EventParserTestCases();
        IncidentOuterClass.Incident expectedResult;
        Event incomingEvent;
        String parsingAssertMessage;

        Map<String, String> entity = new HashMap<>();
        entity.put("lastupdated", "2019-02-21T13:51:52.068Z");

        //<editor-fold desc="Valid incident event">
        parsingAssertMessage = "Valid incident event";
        Map<String, String> entity1 = new HashMap<>(entity);
        entity1.put("incidentid", "892624986");
        entity1.put("antid", "876111475");
        entity1.put("profileid", "1639850061");
        entity1.put("iscorrelatedprofile", "f");
        entity1.put("triggerid", "266689939");
        entity1.put("customerid", "1482");
        entity1.put("timeopened", "2019-02-21T13:51:52.068Z");
        entity1.put("severity", "Failed");
        entity1.put("timetoescalation1", "");
        entity1.put("timetoescalation2", "");
        entity1.put("timetoacknowledgement1", "");
        entity1.put("timetoacknowledgement2", "");
        entity1.put("timetoacknowledgement3", "");
        entity1.put("timeclosed", "");
        entity1.put("currentstatus", "OPEN");
        entity1.put("deleted", "f");

        incomingEvent = Event.builder()
                .entityDataTypes(Collections.EMPTY_MAP)
                .bizappsCustomerId("aaea6111-b13f-462b-9385-4f3baa7f0ccc")
                .ncentralServerGuid("225ed5bb-9e8e-4ad5-aa8c-7a018dc5ffa5")
                .eventingConfigurationCustomerId(1)
                .professionalModeLicenseType("Per Device")
                .eventType(EventType.INSERT)
                .entityType("rpt_incidentlog")
                .entity(entity1)
                .newValues(Collections.emptyMap())
                .build();

        expectedResult = IncidentOuterClass.Incident.newBuilder()
                .setProfileId(1639850061)
                .setIsCorrelatedProfile(false)
                .setTriggerId(266689939)
                .setCurrentStatus("OPEN")
                .setClientId(1482)
                .setSeverity("Failed")
                .setIncidentId(892624986)
                .setTimeOpened(Timestamp.newBuilder().setSeconds(1550757112).setNanos(68000000).build())
                .setDeleted(false)
                .setLastUpdated(Timestamp.newBuilder().setSeconds(1550757112).setNanos(68000000).build())
                .setAction(ActionOuterClass.Action.ADD)
                .setContext(MspContextOuterClass.MspContext.newBuilder()
                        .setBizAppsCustomerId("aaea6111-b13f-462b-9385-4f3baa7f0ccc")
                        .setSystemGuid("225ed5bb-9e8e-4ad5-aa8c-7a018dc5ffa5")
                        .build())
                .build();

        testCases.setTestCase(incomingEvent, expectedResult, parsingAssertMessage);
        //</editor-fold>

        //<editor-fold desc="Incident  missing incidentid">
        parsingAssertMessage = "Incident  missing incidentid";
        Map<String, String> entity2 = new HashMap<>(entity1);
        entity2.remove("incidentid");
        incomingEvent = Event.builder()
                .entityDataTypes(Collections.EMPTY_MAP)
                .bizappsCustomerId("aaea6111-b13f-462b-9385-4f3baa7f0ccc")
                .ncentralServerGuid("225ed5bb-9e8e-4ad5-aa8c-7a018dc5ffa5")
                .eventingConfigurationCustomerId(1)
                .professionalModeLicenseType("Per Device")
                .eventType(EventType.INSERT)
                .entityType("rpt_incidentlog")
                .entity(entity2)
                .newValues(Collections.emptyMap())
                .build();

        testCases.setTestCase(incomingEvent, parsingAssertMessage);
        //</editor-fold>

        //<editor-fold desc="Incident missing customerid">
        parsingAssertMessage = "Incident missing customerid";
        Map<String, String> entity3 = new HashMap<>(entity1);
        entity3.remove("customerid");
        incomingEvent = Event.builder()
                .entityDataTypes(Collections.EMPTY_MAP)
                .bizappsCustomerId("aaea6111-b13f-462b-9385-4f3baa7f0ccc")
                .ncentralServerGuid("225ed5bb-9e8e-4ad5-aa8c-7a018dc5ffa5")
                .eventingConfigurationCustomerId(1)
                .professionalModeLicenseType("Per Device")
                .eventType(EventType.INSERT)
                .entityType("rpt_incidentlog")
                .entity(entity3)
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