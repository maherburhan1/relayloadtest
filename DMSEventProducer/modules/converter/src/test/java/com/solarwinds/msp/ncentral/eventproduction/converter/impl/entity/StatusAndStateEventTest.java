package com.solarwinds.msp.ncentral.eventproduction.converter.impl.entity;

import com.google.protobuf.GeneratedMessageV3;
import com.google.protobuf.Timestamp;

import com.solarwinds.msp.ncentral.eventproduction.api.entity.Event;
import com.solarwinds.msp.ncentral.eventproduction.api.entity.EventType;
import com.solarwinds.msp.ncentral.eventproduction.converter.EventParser;
import com.solarwinds.msp.ncentral.proto.entity.ActionOuterClass;
import com.solarwinds.msp.ncentral.proto.entity.MspContextOuterClass;
import com.solarwinds.msp.ncentral.proto.entity.entity.DeviceOuterClass;
import com.solarwinds.msp.ncentral.proto.entity.entity.StateOuterClass;
import com.solarwinds.msp.ncentral.proto.entity.entity.StatusOuterClass;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class StatusAndStateEventTest {

    private static Stream<Arguments> parseRecordTest() {
        EventParserTestCases testCases = new EventParserTestCases();
        DeviceOuterClass.Device expectedResult;
        Event incomingEvent;
        String parsingAssertMessage;

        //<editor-fold desc="Valid taskstatestatuspriority events">
        parsingAssertMessage = "Valid taskstatestatuspriority events";
        Map<String, String> entity = new HashMap<>();

        entity.put("taskstatus", "12");
        entity.put("statusdesc", "Task Monitoring - Normal");
        entity.put("taskstate", "5");
        entity.put("statedesc", "Failed");
        entity.put("deleted", "false");
        entity.put("lastupdated", "2019-02-21T13:51:52.068Z");

        incomingEvent = Event.builder()
                .entityDataTypes(Collections.EMPTY_MAP)
                .bizappsCustomerId("aaea6111-b13f-462b-9385-4f3baa7f0ccc")
                .ncentralServerGuid("225ed5bb-9e8e-4ad5-aa8c-7a018dc5ffa5")
                .eventingConfigurationCustomerId(1)
                .professionalModeLicenseType("Per Device")
                .eventType(EventType.INSERT)
                .entityType("taskstatestatuspriority")
                .entity(entity)
                .newValues(Collections.emptyMap())
                .build();

        StatusOuterClass.Status expectedResultStatus = StatusOuterClass.Status.newBuilder()
                .setStatusId(12)
                .setName("Task Monitoring - Normal")
                .setDeleted(false)
                .setLastUpdated(Timestamp.newBuilder().setSeconds(1550757112).setNanos(68000000).build())
                .setAction(ActionOuterClass.Action.ADD)
                .setContext(MspContextOuterClass.MspContext.newBuilder()
                        .setBizAppsCustomerId("aaea6111-b13f-462b-9385-4f3baa7f0ccc")
                        .setSystemGuid("225ed5bb-9e8e-4ad5-aa8c-7a018dc5ffa5")
                        .build())
                .build();
        StateOuterClass.State expectedResultState = StateOuterClass.State.newBuilder()
                .setStateId(5)
                .setName("Failed")
                .setDeleted(false)
                .setLastUpdated(Timestamp.newBuilder().setSeconds(1550757112).setNanos(68000000).build())
                .setAction(ActionOuterClass.Action.ADD)
                .setContext(MspContextOuterClass.MspContext.newBuilder()
                        .setBizAppsCustomerId("aaea6111-b13f-462b-9385-4f3baa7f0ccc")
                        .setSystemGuid("225ed5bb-9e8e-4ad5-aa8c-7a018dc5ffa5")
                        .build())
                .build();

        testCases.setTestCase(incomingEvent, Arrays.asList(expectedResultStatus, expectedResultState),
                parsingAssertMessage);
        //</editor-fold>

        return testCases.toArguments();
    }

    @ParameterizedTest(name = "Run {index}: {3}")
    @MethodSource
    void parseRecordTest(EventParser<GeneratedMessageV3> eventParser, Event incomingEvent,
            List<GeneratedMessageV3> expectedResult, String assertMessage) {
        assertThat(eventParser.parse(incomingEvent)).containsExactlyInAnyOrderElementsOf(expectedResult)
                .describedAs(assertMessage);
    }
}