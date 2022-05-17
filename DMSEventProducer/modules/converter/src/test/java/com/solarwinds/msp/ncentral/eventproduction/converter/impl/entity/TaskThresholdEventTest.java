package com.solarwinds.msp.ncentral.eventproduction.converter.impl.entity;

import com.google.protobuf.GeneratedMessageV3;

import com.solarwinds.msp.ncentral.eventproduction.api.entity.Event;
import com.solarwinds.msp.ncentral.eventproduction.api.entity.EventType;
import com.solarwinds.msp.ncentral.eventproduction.converter.EventParser;
import com.solarwinds.msp.ncentral.proto.entity.ActionOuterClass;
import com.solarwinds.msp.ncentral.proto.entity.MspContextOuterClass;
import com.solarwinds.msp.ncentral.proto.entity.tasks.TaskThresholdOuterClass;

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

class TaskThresholdEventTest {
    private static Stream<Arguments> parseRecordTest() {
        EventParserTestCases testCases = new EventParserTestCases();
        TaskThresholdOuterClass.TaskThreshold expectedResult;
        Event incomingEvent;
        String parsingAssertMessage;

        Map<String, String> entity = new HashMap<>();
        entity.put("scandetailid", "3561800");
        entity.put("taskid", "529324613");
        entity.put("valuelow", "0");
        entity.put("valuehigh", "300");
        entity.put("taskstate", "3");
        entity.put("monitoringtype", "Normal");
        entity.put("configurable", "t");
        entity.put("valuestring", "");
        entity.put("valuestringeval", "");
        entity.put("lastupdated", "2019-02-21T13:51:52.068Z");

        //<editor-fold desc="Valid task threshold event">
        parsingAssertMessage = "Valid task threshold event";
        Map<String, String> entity1 = new HashMap<>(entity);

        incomingEvent = Event.builder()
                .entityDataTypes(Collections.EMPTY_MAP)
                .bizappsCustomerId("aaea6111-b13f-462b-9385-4f3baa7f0ccc")
                .ncentralServerGuid("225ed5bb-9e8e-4ad5-aa8c-7a018dc5ffa5")
                .eventingConfigurationCustomerId(1)
                .professionalModeLicenseType("Per Device")
                .eventType(EventType.INSERT)
                .entityType("threshold")
                .entity(entity1)
                .newValues(Collections.emptyMap())
                .build();

        expectedResult = TaskThresholdOuterClass.TaskThreshold.newBuilder()
                .setTaskId(529324613)
                .setGenericServiceScanDetailId(3561800)
                .setMaxValue(300)
                .setMinValue(0)
                .setStateId(3)
                //.setLastUpdated(Timestamp.newBuilder().setSeconds(1550757112).setNanos(68000000).build())
                .setAction(ActionOuterClass.Action.ADD)
                .setContext(MspContextOuterClass.MspContext.newBuilder()
                        .setBizAppsCustomerId("aaea6111-b13f-462b-9385-4f3baa7f0ccc")
                        .setSystemGuid("225ed5bb-9e8e-4ad5-aa8c-7a018dc5ffa5")
                        .build())
                .build();

        testCases.setTestCase(incomingEvent, expectedResult, parsingAssertMessage);
        //</editor-fold>

        //<editor-fold desc="Task threshold event missing task id">
        parsingAssertMessage = "Task threshold event missing task id";
        Map<String, String> entity2 = new HashMap<>(entity1);
        entity2.remove("taskid");
        incomingEvent = Event.builder()
                .entityDataTypes(Collections.EMPTY_MAP)
                .bizappsCustomerId("aaea6111-b13f-462b-9385-4f3baa7f0ccc")
                .ncentralServerGuid("225ed5bb-9e8e-4ad5-aa8c-7a018dc5ffa5")
                .eventingConfigurationCustomerId(1)
                .professionalModeLicenseType("Per Device")
                .eventType(EventType.INSERT)
                .entityType("threshold")
                .entity(entity2)
                .newValues(Collections.emptyMap())
                .build();

        testCases.setTestCase(incomingEvent, parsingAssertMessage);
        //</editor-fold>

        //<editor-fold desc="Task threshold event missing scan detail id">
        parsingAssertMessage = "Task threshold event missing scan detail id";
        Map<String, String> entity3 = new HashMap<>(entity1);
        entity3.remove("scandetailid");
        incomingEvent = Event.builder()
                .entityDataTypes(Collections.EMPTY_MAP)
                .bizappsCustomerId("aaea6111-b13f-462b-9385-4f3baa7f0ccc")
                .ncentralServerGuid("225ed5bb-9e8e-4ad5-aa8c-7a018dc5ffa5")
                .eventingConfigurationCustomerId(1)
                .professionalModeLicenseType("Per Device")
                .eventType(EventType.INSERT)
                .entityType("threshold")
                .entity(entity3)
                .newValues(Collections.emptyMap())
                .build();

        testCases.setTestCase(incomingEvent, parsingAssertMessage);
        //</editor-fold>

        //<editor-fold desc="Task threshold event missing task state">
        parsingAssertMessage = "Task threshold event missing task state";
        Map<String, String> entity4 = new HashMap<>(entity1);
        entity4.remove("taskstate");
        incomingEvent = Event.builder()
                .entityDataTypes(Collections.EMPTY_MAP)
                .bizappsCustomerId("aaea6111-b13f-462b-9385-4f3baa7f0ccc")
                .ncentralServerGuid("225ed5bb-9e8e-4ad5-aa8c-7a018dc5ffa5")
                .eventingConfigurationCustomerId(1)
                .professionalModeLicenseType("Per Device")
                .eventType(EventType.INSERT)
                .entityType("threshold")
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
            List<TaskThresholdOuterClass.TaskThreshold> expectedResult, String assertMessage) {
        List<TaskThresholdOuterClass.TaskThreshold> result = eventParser.parse(incomingEvent)
                .stream()
                .map(e -> (TaskThresholdOuterClass.TaskThreshold) e)
                .map(TaskThresholdOuterClass.TaskThreshold::toBuilder)
                .map(TaskThresholdOuterClass.TaskThreshold.Builder::clearLastUpdated)
                .map(TaskThresholdOuterClass.TaskThreshold.Builder::build)
                .collect(Collectors.toList());

        Assertions.assertEquals(expectedResult, result, assertMessage);
    }
}