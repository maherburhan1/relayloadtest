package com.solarwinds.msp.ncentral.eventproduction.converter.impl.entity;

import com.google.protobuf.GeneratedMessageV3;
import com.google.protobuf.Timestamp;

import com.solarwinds.msp.ncentral.eventproduction.api.entity.Event;
import com.solarwinds.msp.ncentral.eventproduction.api.entity.EventType;
import com.solarwinds.msp.ncentral.eventproduction.converter.EventParser;
import com.solarwinds.msp.ncentral.proto.entity.ActionOuterClass;
import com.solarwinds.msp.ncentral.proto.entity.MspContextOuterClass;
import com.solarwinds.msp.ncentral.proto.entity.tasks.AvSecurityOuterClass;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

class AvSecurityEventTest {

    private static Stream<Arguments> parseRecordTest() {
        EventParserTestCases testCases = new EventParserTestCases();
        AvSecurityOuterClass.AvSecurity expectedResult;

        Event incomingEvent;
        String parsingAssertMessage;

        Map<String, String> avSecurityEntity = new HashMap<>();
        avSecurityEntity.put("taskid", "1907480434");
        avSecurityEntity.put("scantime", "2019-02-21T13:51:52.068Z");
        avSecurityEntity.put("lastupdated", "2019-02-21T13:51:52.068Z");
        avSecurityEntity.put("state", "3");

        parsingAssertMessage = "Valid AV Security Center event";
        avSecurityEntity.put("wmi20165_displayname", "AV Security Center");
        avSecurityEntity.put("wmi20165_uptodate", "true");
        avSecurityEntity.put("wmi20165_scanningenabled", "true");
        avSecurityEntity.put("wmi20165_versionnumber", "1.2.3");
        incomingEvent = Event.builder()
                .entityDataTypes(Collections.emptyMap())
                .bizappsCustomerId("aaea6111-b13f-462b-9385-4f3baa7f0ccc")
                .ncentralServerGuid("225ed5bb-9e8e-4ad5-aa8c-7a018dc5ffa5")
                .eventingConfigurationCustomerId(1)
                .professionalModeLicenseType("Per Device")
                .eventType(EventType.INSERT)
                .entityType("data20165_detailed")
                .entity(avSecurityEntity)
                .newValues(Collections.emptyMap())
                .build();

        expectedResult = AvSecurityOuterClass.AvSecurity.newBuilder()
                .setName("AV Security Center")
                .setTaskId(1907480434)
                .setStateId(3)
                .setDefinitionsUpToDate(true)
                .setScanningEnabled(true)
                .setVersion("1.2.3")
                .setScanTime(Timestamp.newBuilder().setSeconds(1550757112).setNanos(68000000).build())
                .setLastUpdated(Timestamp.newBuilder().setSeconds(1550757112).setNanos(68000000).build())
                .setAction(ActionOuterClass.Action.ADD)
                .setContext(MspContextOuterClass.MspContext.newBuilder()
                        .setBizAppsCustomerId("aaea6111-b13f-462b-9385-4f3baa7f0ccc")
                        .setSystemGuid("225ed5bb-9e8e-4ad5-aa8c-7a018dc5ffa5")
                        .build())
                .build();

        testCases.setTestCase(incomingEvent, expectedResult, parsingAssertMessage);

        parsingAssertMessage = "AV Security event missing taskid";
        Map<String, String> avSecurityEntity1 = new HashMap<>(avSecurityEntity);
        avSecurityEntity1.put("wmi20165_displayname", "AV Security Center");
        avSecurityEntity1.put("wmi20165_uptodate", "true");
        avSecurityEntity1.put("wmi20165_scanningenabled", "true");
        avSecurityEntity1.put("wmi20165_versionnumber", "1.2.3");
        avSecurityEntity1.replace("taskid", null);
        incomingEvent = Event.builder()
                .entityDataTypes(Collections.emptyMap())
                .bizappsCustomerId("aaea6111-b13f-462b-9385-4f3baa7f0ccc")
                .ncentralServerGuid("225ed5bb-9e8e-4ad5-aa8c-7a018dc5ffa5")
                .eventingConfigurationCustomerId(1)
                .professionalModeLicenseType("Per Device")
                .eventType(EventType.INSERT)
                .entityType("data20165_detailed")
                .entity(avSecurityEntity1)
                .newValues(Collections.emptyMap())
                .build();

        testCases.setTestCase(incomingEvent, parsingAssertMessage);

        parsingAssertMessage = "AV Security event missing product name";
        Map<String, String> avSecurityEntityMissingName = new HashMap<>(avSecurityEntity);
        avSecurityEntityMissingName.put("wmi20165_displayname", "");
        avSecurityEntityMissingName.put("wmi20165_uptodate", "true");
        avSecurityEntityMissingName.put("wmi20165_scanningenabled", "true");
        avSecurityEntityMissingName.put("wmi20165_versionnumber", "1.2.3");
        incomingEvent = Event.builder()
                .entityDataTypes(Collections.emptyMap())
                .bizappsCustomerId("aaea6111-b13f-462b-9385-4f3baa7f0ccc")
                .ncentralServerGuid("225ed5bb-9e8e-4ad5-aa8c-7a018dc5ffa5")
                .eventingConfigurationCustomerId(1)
                .professionalModeLicenseType("Per Device")
                .eventType(EventType.INSERT)
                .entityType("data20165_detailed")
                .entity(avSecurityEntityMissingName)
                .newValues(Collections.emptyMap())
                .build();

        testCases.setTestCase(incomingEvent, parsingAssertMessage);

        parsingAssertMessage = "Valid AV Defender event";
        Map<String, String> avSecurityEntity2 = new HashMap<>(avSecurityEntity);
        avSecurityEntity2.put("name", "AV Defender");
        avSecurityEntity2.put("avd_signature_age", "1");
        avSecurityEntity2.put("avd_protection_state", "1");
        avSecurityEntity2.put("avd_days_from_last_scan", "2");
        avSecurityEntity2.put("avd_product_version", "1.2.3");
        incomingEvent = Event.builder()
                .entityDataTypes(Collections.emptyMap())
                .bizappsCustomerId("aaea6111-b13f-462b-9385-4f3baa7f0ccc")
                .ncentralServerGuid("225ed5bb-9e8e-4ad5-aa8c-7a018dc5ffa5")
                .eventingConfigurationCustomerId(1)
                .professionalModeLicenseType("Per Device")
                .eventType(EventType.INSERT)
                .entityType("dataavdefenderstatus_detailed")
                .entity(avSecurityEntity2)
                .newValues(Collections.emptyMap())
                .build();
        expectedResult = AvSecurityOuterClass.AvSecurity.newBuilder()
                .setName("AV Defender")
                .setTaskId(1907480434)
                .setStateId(3)
                .setDefinitionsUpToDate(true)
                .setScanningEnabled(true)
                .setDefinitionsAge(1)
                .setDaysFromLastScan(2)
                .setVersion("1.2.3")
                .setScanTime(Timestamp.newBuilder().setSeconds(1550757112).setNanos(68000000).build())
                .setLastUpdated(Timestamp.newBuilder().setSeconds(1550757112).setNanos(68000000).build())
                .setAction(ActionOuterClass.Action.ADD)
                .setContext(MspContextOuterClass.MspContext.newBuilder()
                        .setBizAppsCustomerId("aaea6111-b13f-462b-9385-4f3baa7f0ccc")
                        .setSystemGuid("225ed5bb-9e8e-4ad5-aa8c-7a018dc5ffa5")
                        .build())
                .build();
        testCases.setTestCase(incomingEvent, expectedResult, parsingAssertMessage);

        parsingAssertMessage = "Valid Endpoint Security Manager event";
        Map<String, String> avSecurityEntity3 = new HashMap<>(avSecurityEntity);
        avSecurityEntity3.put("name", "Endpoint Security Manager");
        avSecurityEntity3.put("es_deffileage", "1");
        avSecurityEntity3.put("es_protectionstatus", "1");
        avSecurityEntity3.put("es_daysfromlastscan", "2");
        avSecurityEntity3.put("es_agentversion", "1.2.3");
        incomingEvent = Event.builder()
                .entityDataTypes(Collections.emptyMap())
                .bizappsCustomerId("aaea6111-b13f-462b-9385-4f3baa7f0ccc")
                .ncentralServerGuid("225ed5bb-9e8e-4ad5-aa8c-7a018dc5ffa5")
                .eventingConfigurationCustomerId(1)
                .professionalModeLicenseType("Per Device")
                .eventType(EventType.INSERT)
                .entityType("dataendpointsecuritystatus_detailed")
                .entity(avSecurityEntity3)
                .newValues(Collections.emptyMap())
                .build();
        expectedResult = AvSecurityOuterClass.AvSecurity.newBuilder()
                .setName("Endpoint Security Manager")
                .setTaskId(1907480434)
                .setStateId(3)
                .setDefinitionsUpToDate(true)
                .setScanningEnabled(true)
                .setDefinitionsAge(1)
                .setDaysFromLastScan(2)
                .setVersion("1.2.3")
                .setScanTime(Timestamp.newBuilder().setSeconds(1550757112).setNanos(68000000).build())
                .setLastUpdated(Timestamp.newBuilder().setSeconds(1550757112).setNanos(68000000).build())
                .setAction(ActionOuterClass.Action.ADD)
                .setContext(MspContextOuterClass.MspContext.newBuilder()
                        .setBizAppsCustomerId("aaea6111-b13f-462b-9385-4f3baa7f0ccc")
                        .setSystemGuid("225ed5bb-9e8e-4ad5-aa8c-7a018dc5ffa5")
                        .build())
                .build();
        testCases.setTestCase(incomingEvent, expectedResult, parsingAssertMessage);

        parsingAssertMessage = "Valid AV Defender (unmanaged) event";
        Map<String, String> avSecurityEntity4 = new HashMap<>(avSecurityEntity);
        avSecurityEntity4.put("name", "AV Defender (unmanaged)");
        avSecurityEntity4.put("bt_definitions_up_to_date", "yes");
        avSecurityEntity4.put("signature_age", "1");
        avSecurityEntity4.put("bt_scanning_enabled", "yes");
        avSecurityEntity4.put("bt_days_from_last_scan", "2");
        avSecurityEntity4.put("product_status_update_updatesigam", "1.2.3");
        avSecurityEntity4.put("bt_agent_version", "");
        incomingEvent = Event.builder()
                .entityDataTypes(Collections.emptyMap())
                .bizappsCustomerId("aaea6111-b13f-462b-9385-4f3baa7f0ccc")
                .ncentralServerGuid("225ed5bb-9e8e-4ad5-aa8c-7a018dc5ffa5")
                .eventingConfigurationCustomerId(1)
                .professionalModeLicenseType("Per Device")
                .eventType(EventType.INSERT)
                .entityType("databitdefenderstatus_detailed")
                .entity(avSecurityEntity4)
                .newValues(Collections.emptyMap())
                .build();
        expectedResult = AvSecurityOuterClass.AvSecurity.newBuilder()
                .setName("AV Defender (unmanaged)")
                .setTaskId(1907480434)
                .setStateId(3)
                .setDefinitionsUpToDate(true)
                .setScanningEnabled(true)
                .setDefinitionsAge(1)
                .setDaysFromLastScan(2)
                .setVersion("1.2.3")
                .setScanTime(Timestamp.newBuilder().setSeconds(1550757112).setNanos(68000000).build())
                .setLastUpdated(Timestamp.newBuilder().setSeconds(1550757112).setNanos(68000000).build())
                .setAction(ActionOuterClass.Action.ADD)
                .setContext(MspContextOuterClass.MspContext.newBuilder()
                        .setBizAppsCustomerId("aaea6111-b13f-462b-9385-4f3baa7f0ccc")
                        .setSystemGuid("225ed5bb-9e8e-4ad5-aa8c-7a018dc5ffa5")
                        .build())
                .build();
        testCases.setTestCase(incomingEvent, expectedResult, parsingAssertMessage);

        return testCases.toArguments();
    }

    @ParameterizedTest(name = "Run {index}: {3}")
    @MethodSource
    void parseRecordTest(EventParser<GeneratedMessageV3> eventParser, Event incomingEvent,
            List<com.google.protobuf.GeneratedMessageV3> expectedResult, String assertMessage) {
        Assertions.assertEquals(expectedResult, eventParser.parse(incomingEvent), assertMessage);
    }
}
