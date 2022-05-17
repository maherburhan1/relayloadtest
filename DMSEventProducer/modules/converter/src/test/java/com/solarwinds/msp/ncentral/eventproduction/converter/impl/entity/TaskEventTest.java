package com.solarwinds.msp.ncentral.eventproduction.converter.impl.entity;

import com.google.protobuf.GeneratedMessageV3;
import com.google.protobuf.Timestamp;

import com.solarwinds.msp.ncentral.eventproduction.api.entity.Event;
import com.solarwinds.msp.ncentral.eventproduction.api.entity.EventType;
import com.solarwinds.msp.ncentral.eventproduction.converter.EventParser;
import com.solarwinds.msp.ncentral.proto.entity.ActionOuterClass;
import com.solarwinds.msp.ncentral.proto.entity.MspContextOuterClass;
import com.solarwinds.msp.ncentral.proto.entity.tasks.TaskOuterClass;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

class TaskEventTest {

    private static Stream<Arguments> parseRecordTest() {
        EventParserTestCases testCases = new EventParserTestCases();
        TaskOuterClass.Task expectedResult;
        Event incomingEvent;
        String parsingAssertMessage;

        Map<String, String> taskEntity = new HashMap<>();
        taskEntity.put("lastupdated", "2019-02-21T13:51:52.068Z");
        taskEntity.put("taskid", "30185527");

        //<editor-fold desc="Valid task event">
        parsingAssertMessage = "Valid task event";
        Map<String, String> taskEntity1 = new HashMap<>(taskEntity);
        taskEntity1.put("usedefault_resetonnotif", "true");
        taskEntity1.put("transitiontime", "2019-02-20T15:51:34.750Z");
        taskEntity1.put("lasttransitiontime", "2019-02-20T15:51:05.553Z");
        taskEntity1.put("lasttaskstatus", "12");
        taskEntity1.put("taskident", "sda2");
        taskEntity1.put("aggregationdata",
                "1550689410766:1550689679078:1550690087450:1550690289928:\t3:3:3:3:|104044768:104044768:104044768:104044768:|2625232:2625244:2625412:2625416:|96127660:96127648:96127480:96127476:|7:7:7:7:|");
        taskEntity1.put("uistate", "3");
        taskEntity1.put("taskstate", "3");
        taskEntity1.put("timetostale", "30");
        taskEntity1.put("ismanagedtask", "true");
        taskEntity1.put("taskstatus", "12");
        taskEntity1.put("applianceid", "1");
        taskEntity1.put("aggregationtime", "2019-02-20T19:00:00Z");
        taskEntity1.put("created", "2019-02-20T15:51:05.207Z");
        taskEntity1.put("lasttaskstate", "1");
        taskEntity1.put("isconfigrequired", "false");
        taskEntity1.put("generate_notification", "true");
        taskEntity1.put("deviceid", "1");
        taskEntity1.put("tasknote", "Auto-assigned central server self-monitoring task.");
        taskEntity1.put("deleted", "false");
        taskEntity1.put("lastscantime", "2019-02-20T19:18:09.928Z");
        taskEntity1.put("customerid", "1");
        taskEntity1.put("lastupdate", "2019-02-20T19:18:09.928Z");
        taskEntity1.put("serviceid", "113");
        taskEntity1.put("serviceitemid", "39480");

        incomingEvent = Event.builder()
                .entityDataTypes(Collections.EMPTY_MAP)
                .bizappsCustomerId("aaea6111-b13f-462b-9385-4f3baa7f0ccc")
                .ncentralServerGuid("225ed5bb-9e8e-4ad5-aa8c-7a018dc5ffa5")
                .eventingConfigurationCustomerId(1)
                .professionalModeLicenseType("Per Device")
                .eventType(EventType.INSERT)
                .entityType("appliancetask")
                .entity(taskEntity1)
                .newValues(Collections.emptyMap())
                .build();

        expectedResult = TaskOuterClass.Task.newBuilder()
                .setTaskId(30185527)
                .setDeviceId(1)
                .setDeleted(false)
                .setName("sda2")
                .setNote("Auto-assigned central server self-monitoring task.")
                .setStatusId(12)
                .setStateId(3)
                .setServiceInstanceId(113)
                .setCreated(Timestamp.newBuilder().setSeconds(1550677865).setNanos(207000000).build())
                .setLastUpdated(Timestamp.newBuilder().setSeconds(1550757112).setNanos(68000000).build())
                .setAction(ActionOuterClass.Action.ADD)
                .setContext(MspContextOuterClass.MspContext.newBuilder()
                        .setBizAppsCustomerId("aaea6111-b13f-462b-9385-4f3baa7f0ccc")
                        .setSystemGuid("225ed5bb-9e8e-4ad5-aa8c-7a018dc5ffa5")
                        .build())
                .build();

        testCases.setTestCase(incomingEvent, expectedResult, parsingAssertMessage);
        //</editor-fold>

        //<editor-fold desc="Task event missing task id">
        parsingAssertMessage = "Task event missing task id";
        Map<String, String> taskEntity2 = new HashMap<>(taskEntity);
        taskEntity2.remove("taskid");
        incomingEvent = Event.builder()
                .entityDataTypes(Collections.EMPTY_MAP)
                .bizappsCustomerId("aaea6111-b13f-462b-9385-4f3baa7f0ccc")
                .ncentralServerGuid("225ed5bb-9e8e-4ad5-aa8c-7a018dc5ffa5")
                .eventingConfigurationCustomerId(1)
                .professionalModeLicenseType("Per Device")
                .eventType(EventType.INSERT)
                .entityType("appliancetask")
                .entity(taskEntity2)
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