package com.solarwinds.msp.ncentral.eventproduction.converter.impl.entity;

import com.google.protobuf.GeneratedMessageV3;
import com.google.protobuf.Timestamp;

import com.solarwinds.msp.ncentral.eventproduction.api.entity.Event;
import com.solarwinds.msp.ncentral.eventproduction.api.entity.EventType;
import com.solarwinds.msp.ncentral.eventproduction.converter.EventParser;
import com.solarwinds.msp.ncentral.proto.entity.ActionOuterClass;
import com.solarwinds.msp.ncentral.proto.entity.MspContextOuterClass;
import com.solarwinds.msp.ncentral.proto.entity.tasks.ManagementTaskTypeOuterClass;
import com.solarwinds.msp.ncentral.proto.entity.tasks.RemoteExecutionTaskOuterClass;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

class RemoteExecutionTaskEventTest {

    private static Stream<Arguments> parseRecordTest() {
        EventParserTestCases testCases = new EventParserTestCases();
        RemoteExecutionTaskOuterClass.RemoteExecutionTask expectedResult;
        Event incomingEvent;
        String parsingAssertMessage;

        Map<String, String> entity = new HashMap<>();
        entity.put("lastupdated", "2019-02-21T13:51:52.068Z");
        entity.put("remoteexecutiontaskid", "1143031030");
        entity.put("remoteexecutionitemid", "91");
        entity.put("applianceid", "1824172396");
        entity.put("customerid", "261");
        entity.put("isconfigrequired", "f");
        entity.put("tasktype", "AVDefenderQuickScan");
        entity.put("parentremoteexecutiontaskid", "");
        entity.put("status", "Completed");
        entity.put("statuschangetimestamp", "2019-06-17T12:02:57.754-04:00");
        entity.put("useagent", "t");
        entity.put("sotask", "f");
        entity.put("profiletask", "t");
        entity.put("profileid", "412187519");
        entity.put("task_configitem_uuid", "ea048854-a044-42a9-80d6-f60855f9aeaf");
        entity.put("deviceid", "1251379553");
        entity.put("name", "AV Defender Quick Scan - 12:00");
        entity.put("hasrecurring", "t");
        entity.put("hasoncenow", "f");
        entity.put("hasoncelater", "f");
        entity.put("useprobe", "t");
        entity.put("min_version", "8.1.0.0");
        entity.put("expiry_time", "");
        entity.put("last_run_start_time", "2019-02-21T13:51:42.068Z");
        entity.put("last_run_finish_time", "2019-02-21T13:51:52.068Z");
        entity.put("last_run_return_code", "0");
        entity.put("last_run_limited_output", "");
        entity.put("last_run_output_capture_file", "");
        entity.put("deleted", "f");
        entity.put("statusmessage",
                "Elapsed time: 2 Minutes, Scanned:56968, Infected:0, Resolved:0, Deleted:0 Suspicious:0");
        entity.put("enabled", "t");
        entity.put("remotecontroltaskid", "");
        entity.put("reactivetask", "f");

        //<editor-fold desc="Valid remote execution task event">
        parsingAssertMessage = "Valid remote execution task event";
        Map<String, String> entity1 = new HashMap<>(entity);

        incomingEvent = Event.builder()
                .entityDataTypes(Collections.EMPTY_MAP)
                .bizappsCustomerId("aaea6111-b13f-462b-9385-4f3baa7f0ccc")
                .ncentralServerGuid("225ed5bb-9e8e-4ad5-aa8c-7a018dc5ffa5")
                .eventingConfigurationCustomerId(1)
                .professionalModeLicenseType("Per Device")
                .eventType(EventType.INSERT)
                .entityType("remoteexecutiontask")
                .entity(entity1)
                .newValues(Collections.emptyMap())
                .build();

        expectedResult = RemoteExecutionTaskOuterClass.RemoteExecutionTask.newBuilder()
                .setClientId(261)
                .setDeviceId(1251379553)
                .setRemoteExecutionItemId(91)
                .setName("AV Defender Quick Scan - 12:00")
                .setLastRunStartTime(Timestamp.newBuilder().setSeconds(1550757102).setNanos(68000000).build())
                .setLastRunFinishTime(Timestamp.newBuilder().setSeconds(1550757112).setNanos(68000000).build())
                .setManagementTaskType(ManagementTaskTypeOuterClass.ManagementTaskType.newBuilder()
                        .setName("AVDefenderQuickScan")
                        .build())
                .setName("AV Defender Quick Scan - 12:00")
                .setIsSuccessfulTask(true)
                .setRemoteExecutionTaskId(1143031030)
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

        //<editor-fold desc="Remote execution task event unsuccessful">
        parsingAssertMessage = "Remote execution task event unsuccessful";
        Map<String, String> entity2 = new HashMap<>(entity1);
        entity2.replace("last_run_return_code", "12");
        RemoteExecutionTaskOuterClass.RemoteExecutionTask expectedResult2 =
                RemoteExecutionTaskOuterClass.RemoteExecutionTask.newBuilder()
                        .mergeFrom(expectedResult)
                        .setIsSuccessfulTask(false)
                        .build();
        incomingEvent = Event.builder()
                .entityDataTypes(Collections.EMPTY_MAP)
                .bizappsCustomerId("aaea6111-b13f-462b-9385-4f3baa7f0ccc")
                .ncentralServerGuid("225ed5bb-9e8e-4ad5-aa8c-7a018dc5ffa5")
                .eventingConfigurationCustomerId(1)
                .professionalModeLicenseType("Per Device")
                .eventType(EventType.INSERT)
                .entityType("remoteexecutiontask")
                .entity(entity2)
                .newValues(Collections.emptyMap())
                .build();

        testCases.setTestCase(incomingEvent, expectedResult2, parsingAssertMessage);
        //</editor-fold>

        //<editor-fold desc="Remote execution task event missing remoteexecutiontaskid">
        parsingAssertMessage = "Remote execution task event missing remoteexecutiontaskid";
        Map<String, String> entity3 = new HashMap<>(entity1);
        entity3.remove("remoteexecutiontaskid");
        incomingEvent = Event.builder()
                .entityDataTypes(Collections.EMPTY_MAP)
                .bizappsCustomerId("aaea6111-b13f-462b-9385-4f3baa7f0ccc")
                .ncentralServerGuid("225ed5bb-9e8e-4ad5-aa8c-7a018dc5ffa5")
                .eventingConfigurationCustomerId(1)
                .professionalModeLicenseType("Per Device")
                .eventType(EventType.INSERT)
                .entityType("remoteexecutiontask")
                .entity(entity3)
                .newValues(Collections.emptyMap())
                .build();

        testCases.setTestCase(incomingEvent, parsingAssertMessage);
        //</editor-fold>

        //<editor-fold desc="Remote execution task event missing remoteexecutionitemid">
        parsingAssertMessage = "Remote execution task event missing remoteexecutionitemid";
        Map<String, String> entity4 = new HashMap<>(entity1);
        entity4.remove("remoteexecutionitemid");
        incomingEvent = Event.builder()
                .entityDataTypes(Collections.EMPTY_MAP)
                .bizappsCustomerId("aaea6111-b13f-462b-9385-4f3baa7f0ccc")
                .ncentralServerGuid("225ed5bb-9e8e-4ad5-aa8c-7a018dc5ffa5")
                .eventingConfigurationCustomerId(1)
                .professionalModeLicenseType("Per Device")
                .eventType(EventType.INSERT)
                .entityType("remoteexecutiontask")
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