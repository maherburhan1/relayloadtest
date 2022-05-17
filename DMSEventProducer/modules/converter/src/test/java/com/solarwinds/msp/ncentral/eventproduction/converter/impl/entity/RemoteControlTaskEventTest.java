package com.solarwinds.msp.ncentral.eventproduction.converter.impl.entity;

import com.google.protobuf.GeneratedMessageV3;
import com.google.protobuf.Timestamp;

import com.solarwinds.msp.ncentral.eventproduction.api.entity.Event;
import com.solarwinds.msp.ncentral.eventproduction.api.entity.EventType;
import com.solarwinds.msp.ncentral.eventproduction.converter.EventParser;
import com.solarwinds.msp.ncentral.proto.entity.ActionOuterClass;
import com.solarwinds.msp.ncentral.proto.entity.MspContextOuterClass;
import com.solarwinds.msp.ncentral.proto.entity.tasks.RemoteControlTaskOuterClass;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

class RemoteControlTaskEventTest {

    private static Stream<Arguments> parseRecordTest() {
        EventParserTestCases testCases = new EventParserTestCases();
        RemoteControlTaskOuterClass.RemoteControlTask expectedResult;
        Event incomingEvent;
        String parsingAssertMessage;

        Map<String, String> entity = new HashMap<>();
        entity.put("lastupdated", "2019-02-21T13:51:52.068Z");
        entity.put("terminated", "2019-02-21T13:51:50.068Z");
        entity.put("created", "2019-02-21T13:51:45.068Z");
        entity.put("client_created_time", "2019-06-17T15:56:43.376Z");
        entity.put("remotecontrolauditlogid", "217189");
        entity.put("remotecontroltaskid", "1698129204");
        entity.put("isattended", "f");
        entity.put("customerid", "1002");
        entity.put("deviceid", "655056688");
        entity.put("targeturi", "192.168.1.1");
        entity.put("userid", "1322468698");
        entity.put("email", "email@company.com");
        entity.put("username", "Somebody Somefirst Somelastname");
        entity.put("method", "MSPAnywhere");
        entity.put("terminatedby", "");
        entity.put("notes", "Terminated by User");
        entity.put("deleted", "f");
        entity.put("sourceuri", "96.23.212.11");
        entity.put("rc_session_uuid", "5cb450e2-b35d-4b77-affe-306ad0b22ba2");
        entity.put("rc_session_state", "FINISHED");
        entity.put("error", "");
        entity.put("tunnel_setup_start_time", "");
        entity.put("tunnel_setup_end_time", "");
        entity.put("duration", "313116");
        entity.put("tunnel_type", "");
        entity.put("tunnel_target_uri", "");
        entity.put("tunnel_source_uri", "");
        entity.put("available_tunnel_types", "");
        entity.put("device_name", "11DXHT2");
        entity.put("deviceclass", "Workstations - Windows");
        entity.put("tech_userid", "1322468698");
        entity.put("tech_username", "Somebody Somefirst Somelastname");

        //<editor-fold desc="Valid remote control task event">
        parsingAssertMessage = "Valid remote control task event";
        Map<String, String> entity1 = new HashMap<>(entity);

        incomingEvent = Event.builder()
                .entityDataTypes(Collections.EMPTY_MAP)
                .bizappsCustomerId("aaea6111-b13f-462b-9385-4f3baa7f0ccc")
                .ncentralServerGuid("225ed5bb-9e8e-4ad5-aa8c-7a018dc5ffa5")
                .eventingConfigurationCustomerId(1)
                .professionalModeLicenseType("Per Device")
                .eventType(EventType.INSERT)
                .entityType("remotecontroltaskauditlog")
                .entity(entity1)
                .newValues(Collections.emptyMap())
                .build();

        expectedResult = RemoteControlTaskOuterClass.RemoteControlTask.newBuilder()
                .setRemoteControlTaskId(1698129204)
                .setClientId(1002)
                .setDeviceId(655056688)
                .setUserId(1322468698)
                .setEmail("email@company.com")
                .setTargetUri("192.168.1.1")
                .setSourceUri("96.23.212.11")
                .setMethod("MSPAnywhere")
                .setCreated(Timestamp.newBuilder().setSeconds(1550757105).setNanos(68000000).build())
                .setTerminated(Timestamp.newBuilder().setSeconds(1550757110).setNanos(68000000).build())
                .setNotes("Terminated by User")
                .setIsAttended(false)
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

        //<editor-fold desc="Remote control task event missing UserId">
        parsingAssertMessage = "Remote control task event missing UserId";
        Map<String, String> entity2 = new HashMap<>(entity1);
        entity2.remove("userid");
        incomingEvent = Event.builder()
                .entityDataTypes(Collections.EMPTY_MAP)
                .bizappsCustomerId("aaea6111-b13f-462b-9385-4f3baa7f0ccc")
                .ncentralServerGuid("225ed5bb-9e8e-4ad5-aa8c-7a018dc5ffa5")
                .eventingConfigurationCustomerId(1)
                .professionalModeLicenseType("Per Device")
                .eventType(EventType.INSERT)
                .entityType("remotecontroltaskauditlog")
                .entity(entity2)
                .newValues(Collections.emptyMap())
                .build();

        testCases.setTestCase(incomingEvent, parsingAssertMessage);
        //</editor-fold>

        //<editor-fold desc="Remote control task event missing method">
        parsingAssertMessage = "Remote control task event missing method";
        Map<String, String> entity3 = new HashMap<>(entity1);
        entity3.remove("method");
        incomingEvent = Event.builder()
                .entityDataTypes(Collections.EMPTY_MAP)
                .bizappsCustomerId("aaea6111-b13f-462b-9385-4f3baa7f0ccc")
                .ncentralServerGuid("225ed5bb-9e8e-4ad5-aa8c-7a018dc5ffa5")
                .eventingConfigurationCustomerId(1)
                .professionalModeLicenseType("Per Device")
                .eventType(EventType.INSERT)
                .entityType("remotecontroltaskauditlog")
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