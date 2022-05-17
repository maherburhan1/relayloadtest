package com.solarwinds.msp.ncentral.eventproduction.converter.impl.entity;

import com.google.protobuf.GeneratedMessageV3;
import com.google.protobuf.Timestamp;

import com.solarwinds.msp.ncentral.eventproduction.api.entity.Event;
import com.solarwinds.msp.ncentral.eventproduction.api.entity.EventType;
import com.solarwinds.msp.ncentral.eventproduction.converter.EventParser;
import com.solarwinds.msp.ncentral.proto.entity.ActionOuterClass;
import com.solarwinds.msp.ncentral.proto.entity.MspContextOuterClass;
import com.solarwinds.msp.ncentral.proto.entity.tasks.PatchActionOuterClass;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

class PatchActionEventTest {

    private static Stream<Arguments> parseRecordTest() {
        EventParserTestCases testCases = new EventParserTestCases();
        PatchActionOuterClass.PatchAction expectedResult;
        Event incomingEvent;
        String parsingAssertMessage;

        Map<String, String> patchActionEntity = new HashMap<>();
        patchActionEntity.put("date", "2019-02-21T13:51:52.068Z");
        patchActionEntity.put("lastupdated", "2019-02-21T13:51:52.068Z");
        patchActionEntity.put("deviceid", "1454030117");

        //<editor-fold desc="Valid patch action approval event">
        parsingAssertMessage = "Valid patch action approval event";
        Map<String, String> patchActionEntity1 = new HashMap<>(patchActionEntity);
        patchActionEntity1.put("patchguid", "07609d43-d518-4e77-856e-d1b316d1b8a8");
        patchActionEntity1.put("status", "Approved for Install");

        incomingEvent = Event.builder()
                .entityDataTypes(Collections.EMPTY_MAP)
                .bizappsCustomerId("aaea6111-b13f-462b-9385-4f3baa7f0ccc")
                .ncentralServerGuid("225ed5bb-9e8e-4ad5-aa8c-7a018dc5ffa5")
                .eventingConfigurationCustomerId(1)
                .professionalModeLicenseType("Per Device")
                .eventType(EventType.INSERT)
                .entityType("patchapprovallog")
                .entity(patchActionEntity1)
                .newValues(Collections.emptyMap())
                .build();

        expectedResult = PatchActionOuterClass.PatchAction.newBuilder()
                .setDeviceId(1454030117)
                .setPatchId("07609d43-d518-4e77-856e-d1b316d1b8a8")
                .setActionStatus("Approved for Install")
                .setActionType(PatchActionOuterClass.PatchAction.ActionType.APPROVAL)
                .setActionDate(Timestamp.newBuilder().setSeconds(1550757112).setNanos(68000000).build())
                .setLastUpdated(Timestamp.newBuilder().setSeconds(1550757112).setNanos(68000000).build())
                .setAction(ActionOuterClass.Action.ADD)
                .setContext(MspContextOuterClass.MspContext.newBuilder()
                        .setBizAppsCustomerId("aaea6111-b13f-462b-9385-4f3baa7f0ccc")
                        .setSystemGuid("225ed5bb-9e8e-4ad5-aa8c-7a018dc5ffa5")
                        .build())
                .build();

        testCases.setTestCase(incomingEvent, expectedResult, parsingAssertMessage);
        //</editor-fold>

        //<editor-fold desc="Valid patch action install event">
        parsingAssertMessage = "Valid patch action install event";
        Map<String, String> patchActionEntity2 = new HashMap<>(patchActionEntity);
        patchActionEntity2.put("deleted", "false");
        patchActionEntity2.put("updateid", "07609d43-d518-4e77-856e-d1b316d1b8a8");
        patchActionEntity2.put("installationresult", "Installed");
        patchActionEntity2.put("installeddate", "2018-04-04 00:00:00.000Z");

        incomingEvent = Event.builder()
                .entityDataTypes(Collections.EMPTY_MAP)
                .bizappsCustomerId("aaea6111-b13f-462b-9385-4f3baa7f0ccc")
                .ncentralServerGuid("225ed5bb-9e8e-4ad5-aa8c-7a018dc5ffa5")
                .eventingConfigurationCustomerId(1)
                .professionalModeLicenseType("Per Device")
                .eventType(EventType.INSERT)
                .entityType("cim_patch")
                .entity(patchActionEntity2)
                .newValues(Collections.emptyMap())
                .build();

        expectedResult = PatchActionOuterClass.PatchAction.newBuilder()
                .setDeviceId(1454030117)
                .setPatchId("07609d43-d518-4e77-856e-d1b316d1b8a8")
                .setActionStatus("Installed")
                .setActionDate(Timestamp.newBuilder().setSeconds(1522800000).build())
                .setActionType(PatchActionOuterClass.PatchAction.ActionType.INSTALLATION)
                .setLastUpdated(Timestamp.newBuilder().setSeconds(1550757112).setNanos(68000000).build())
                .setAction(ActionOuterClass.Action.ADD)
                .setContext(MspContextOuterClass.MspContext.newBuilder()
                        .setBizAppsCustomerId("aaea6111-b13f-462b-9385-4f3baa7f0ccc")
                        .setSystemGuid("225ed5bb-9e8e-4ad5-aa8c-7a018dc5ffa5")
                        .build())
                .build();

        testCases.setTestCase(incomingEvent, expectedResult, parsingAssertMessage);
        //</editor-fold>

        //<editor-fold desc="patch action event missing device id">
        parsingAssertMessage = "Patch action event missing device id";
        Map<String, String> patchActionEntiry3 = new HashMap<>(patchActionEntity);
        patchActionEntiry3.remove("deviceid");
        incomingEvent = Event.builder()
                .entityDataTypes(Collections.EMPTY_MAP)
                .bizappsCustomerId("aaea6111-b13f-462b-9385-4f3baa7f0ccc")
                .ncentralServerGuid("225ed5bb-9e8e-4ad5-aa8c-7a018dc5ffa5")
                .eventingConfigurationCustomerId(1)
                .professionalModeLicenseType("Per Device")
                .eventType(EventType.INSERT)
                .entityType("cim_patch")
                .entity(patchActionEntiry3)
                .newValues(Collections.emptyMap())
                .build();

        testCases.setTestCase(incomingEvent, parsingAssertMessage);
        //</editor-fold>

        //<editor-fold desc="patch action event missing patch id">
        parsingAssertMessage = "Patch action event missing patch id";
        Map<String, String> patchActionEntiry4 = new HashMap<>(patchActionEntity);
        incomingEvent = Event.builder()
                .entityDataTypes(Collections.EMPTY_MAP)
                .bizappsCustomerId("aaea6111-b13f-462b-9385-4f3baa7f0ccc")
                .ncentralServerGuid("225ed5bb-9e8e-4ad5-aa8c-7a018dc5ffa5")
                .eventingConfigurationCustomerId(1)
                .professionalModeLicenseType("Per Device")
                .eventType(EventType.INSERT)
                .entityType("cim_patch")
                .entity(patchActionEntiry4)
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