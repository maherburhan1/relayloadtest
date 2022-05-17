package com.solarwinds.msp.ncentral.eventproduction.converter.impl.entity;

import com.google.protobuf.GeneratedMessageV3;
import com.google.protobuf.Timestamp;

import com.solarwinds.msp.ncentral.eventproduction.api.entity.Event;
import com.solarwinds.msp.ncentral.eventproduction.api.entity.EventType;
import com.solarwinds.msp.ncentral.eventproduction.converter.EventParser;
import com.solarwinds.msp.ncentral.proto.entity.ActionOuterClass;
import com.solarwinds.msp.ncentral.proto.entity.MspContextOuterClass;
import com.solarwinds.msp.ncentral.proto.entity.tasks.UtilizationOuterClass;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

class UtilizationEventTest {

    private static Stream<Arguments> parseRecordTest() {
        EventParserTestCases testCases = new EventParserTestCases();
        UtilizationOuterClass.Utilization expectedResult;
        UtilizationOuterClass.Utilization expectedResult2;
        Event incomingEvent;
        String parsingAssertMessage;

        Map<String, String> entity = new HashMap<>();
        entity.put("lastupdated", "2019-02-21T13:51:52.068Z");
        entity.put("scantime", "2019-02-21T13:51:52.068Z");
        entity.put("taskid", "1293848154");
        entity.put("datadelay", "21");
        entity.put("errormessage", "");
        entity.put("state", "3");

        //<editor-fold desc="Valid utilization event (memory)">
        parsingAssertMessage = "Valid utilization event (memory)";
        Map<String, String> entity1 = new HashMap<>(entity);
        entity1.put("memory_physicaltotal", "37748276");
        entity1.put("memory_physicalused", "22613792");
        entity1.put("memory_physicalfree", "15134484");
        entity1.put("memory_physicalusage", "60");
        entity1.put("memory_virtualtotal", "43253300");
        entity1.put("memory_virtualused", "23933496");
        entity1.put("memory_virtualfree", "19319804");
        entity1.put("memory_virtualusage", "55");
        entity1.put("memory_process1", "msseces (64 bit)");
        entity1.put("memory_process2", "sqlservr (64 bit)");
        entity1.put("memory_process3", "ReportingServicesService (64 bit)");
        entity1.put("memory_process4", "agent");
        entity1.put("memory_process5", "sqlservr (64 bit)");
        entity1.put("memory_process_pid1", "7448");
        entity1.put("memory_process_pid2", "2732");
        entity1.put("memory_process_pid3", "43700");
        entity1.put("memory_process_pid4", "16112");
        entity1.put("memory_process_pid5", "5732");
        entity1.put("memory_process_user1", "DCCSD118\\AMEOSC");
        entity1.put("memory_process_user2", "NT AUTHORITY\\SYSTEM");
        entity1.put("memory_process_user3", "NT AUTHORITY\\SYSTEM");
        entity1.put("memory_process_user4", "NT AUTHORITY\\SYSTEM");
        entity1.put("memory_process_user5", "NT SERVICE\\MSSQL$MICROSOFT##WID");
        entity1.put("memory_physical1", "933420");
        entity1.put("memory_physical2", "617468");
        entity1.put("memory_physical3", "504184");
        entity1.put("memory_physical4", "476916");
        entity1.put("memory_physical5", "333728");
        entity1.put("memory_virtual1", "2148622696");
        entity1.put("memory_virtual2", "54060784");
        entity1.put("memory_virtual3", "26362948");
        entity1.put("memory_virtual4", "1218660");
        entity1.put("memory_virtual5", "57657000");

        incomingEvent = Event.builder()
                .entityDataTypes(Collections.EMPTY_MAP)
                .bizappsCustomerId("aaea6111-b13f-462b-9385-4f3baa7f0ccc")
                .ncentralServerGuid("225ed5bb-9e8e-4ad5-aa8c-7a018dc5ffa5")
                .eventingConfigurationCustomerId(1)
                .professionalModeLicenseType("Per Device")
                .eventType(EventType.INSERT)
                .entityType("datamemory_detailed")
                .entity(entity1)
                .newValues(Collections.emptyMap())
                .build();

        expectedResult = UtilizationOuterClass.Utilization.newBuilder()
                .setTaskId(1293848154)
                .setScanTime(Timestamp.newBuilder().setSeconds(1550757112).setNanos(68000000).build())
                .setStateId(3)
                .setUsagePercent(55F)
                .setUnitsConsumed(23933496F)
                .setUnitsAvailable(19319804F)
                .setUnitsTotal(43253300F)
                .setUnitType("KB")
                .setUtilizationType(UtilizationOuterClass.Utilization.UtilizationType.VIRTUAL_MEMORY)
                .setLastUpdated(Timestamp.newBuilder().setSeconds(1550757112).setNanos(68000000).build())
                .setAction(ActionOuterClass.Action.ADD)
                .setContext(MspContextOuterClass.MspContext.newBuilder()
                        .setBizAppsCustomerId("aaea6111-b13f-462b-9385-4f3baa7f0ccc")
                        .setSystemGuid("225ed5bb-9e8e-4ad5-aa8c-7a018dc5ffa5")
                        .build())
                .build();

        expectedResult2 = UtilizationOuterClass.Utilization.newBuilder()
                .setTaskId(1293848154)
                .setScanTime(Timestamp.newBuilder().setSeconds(1550757112).setNanos(68000000).build())
                .setStateId(3)
                .setUsagePercent(60F)
                .setUnitsConsumed(22613792F)
                .setUnitsAvailable(15134484F)
                .setUnitsTotal(37748276F)
                .setUnitType("KB")
                .setUtilizationType(UtilizationOuterClass.Utilization.UtilizationType.PHYSICAL_MEMORY)
                .setLastUpdated(Timestamp.newBuilder().setSeconds(1550757112).setNanos(68000000).build())
                .setAction(ActionOuterClass.Action.ADD)
                .setContext(MspContextOuterClass.MspContext.newBuilder()
                        .setBizAppsCustomerId("aaea6111-b13f-462b-9385-4f3baa7f0ccc")
                        .setSystemGuid("225ed5bb-9e8e-4ad5-aa8c-7a018dc5ffa5")
                        .build())
                .build();

        List<UtilizationOuterClass.Utilization> expectedResults = new ArrayList<>();
        expectedResults.add(expectedResult2);
        expectedResults.add(expectedResult);
        testCases.setTestCase(incomingEvent, expectedResults, parsingAssertMessage);
        //</editor-fold>

        //<editor-fold desc="Utilization event missing task id">
        parsingAssertMessage = "Utilization event missing task id";
        Map<String, String> entity2 = new HashMap<>(entity1);
        entity2.remove("taskid");
        incomingEvent = Event.builder()
                .entityDataTypes(Collections.EMPTY_MAP)
                .bizappsCustomerId("aaea6111-b13f-462b-9385-4f3baa7f0ccc")
                .ncentralServerGuid("225ed5bb-9e8e-4ad5-aa8c-7a018dc5ffa5")
                .eventingConfigurationCustomerId(1)
                .professionalModeLicenseType("Per Device")
                .eventType(EventType.INSERT)
                .entityType("datamemory_detailed")
                .entity(entity2)
                .newValues(Collections.emptyMap())
                .build();

        testCases.setTestCase(incomingEvent, parsingAssertMessage);
        //</editor-fold>

        //<editor-fold desc="Valid utilization event (disk)">
        parsingAssertMessage = "Valid utilization event (disk)";
        Map<String, String> entity3 = new HashMap<>(entity);
        entity3.put("disk_total", "473762812");
        entity3.put("disk_used", "175148176");
        entity3.put("disk_free", "298614636");
        entity3.put("disk_usage", "37");

        incomingEvent = Event.builder()
                .entityDataTypes(Collections.EMPTY_MAP)
                .bizappsCustomerId("aaea6111-b13f-462b-9385-4f3baa7f0ccc")
                .ncentralServerGuid("225ed5bb-9e8e-4ad5-aa8c-7a018dc5ffa5")
                .eventingConfigurationCustomerId(1)
                .professionalModeLicenseType("Per Device")
                .eventType(EventType.INSERT)
                .entityType("datadisk_detailed")
                .entity(entity3)
                .newValues(Collections.emptyMap())
                .build();

        expectedResult = UtilizationOuterClass.Utilization.newBuilder()
                .setTaskId(1293848154)
                .setScanTime(Timestamp.newBuilder().setSeconds(1550757112).setNanos(68000000).build())
                .setStateId(3)
                .setUnitType("KB")
                .setUsagePercent(37F)
                .setUnitsConsumed(175148176F)
                .setUnitsAvailable(298614636F)
                .setUnitsTotal(473762812F)
                .setUtilizationType(UtilizationOuterClass.Utilization.UtilizationType.DISK)
                .setLastUpdated(Timestamp.newBuilder().setSeconds(1550757112).setNanos(68000000).build())
                .setAction(ActionOuterClass.Action.ADD)
                .setContext(MspContextOuterClass.MspContext.newBuilder()
                        .setBizAppsCustomerId("aaea6111-b13f-462b-9385-4f3baa7f0ccc")
                        .setSystemGuid("225ed5bb-9e8e-4ad5-aa8c-7a018dc5ffa5")
                        .build())
                .build();

        testCases.setTestCase(incomingEvent, expectedResult, parsingAssertMessage);
        //</editor-fold>

        //<editor-fold desc="Valid utilization event (cpu)">
        parsingAssertMessage = "Valid utilization event (cpu)";
        Map<String, String> entity4 = new HashMap<>(entity);
        entity4.put("cpu_usage", "2");
        entity4.put("top5_cpu_process1", "EPSecurityService");
        entity4.put("top5_cpu_process2", "System");
        entity4.put("top5_cpu_process3", "dwm");
        entity4.put("top5_cpu_process4", "BASupSrvcCnfg");
        entity1.put("top5_cpu_process5", "services");
        entity1.put("top5_pid_process1", "2096");
        entity1.put("top5_pid_process2", "4");
        entity1.put("top5_pid_process3", "5240");
        entity1.put("top5_pid_process4", "5904");
        entity1.put("top5_pid_process5", "712");
        entity1.put("top5_user_process1", "NT AUTHORITY\\SYSTEM");
        entity1.put("top5_user_process2", "NT AUTHORITY\\SYSTEM");
        entity1.put("top5_user_process3", "WIERSINT\\tkaufman");
        entity1.put("top5_user_process4", "WIERSINT\\tkaufman");
        entity1.put("top5_user_process5", "NT AUTHORITY\\SYSTEM");
        entity1.put("top5_cpu_usage1", "0");
        entity1.put("top5_cpu_usage2", "0");
        entity1.put("top5_cpu_usage3", "0");
        entity1.put("top5_cpu_usage4", "0");
        entity1.put("top5_cpu_usage5", "0");

        incomingEvent = Event.builder()
                .entityDataTypes(Collections.EMPTY_MAP)
                .bizappsCustomerId("aaea6111-b13f-462b-9385-4f3baa7f0ccc")
                .ncentralServerGuid("225ed5bb-9e8e-4ad5-aa8c-7a018dc5ffa5")
                .eventingConfigurationCustomerId(1)
                .professionalModeLicenseType("Per Device")
                .eventType(EventType.INSERT)
                .entityType("datacpu_detailed")
                .entity(entity4)
                .newValues(Collections.emptyMap())
                .build();

        expectedResult = UtilizationOuterClass.Utilization.newBuilder()
                .setTaskId(1293848154)
                .setScanTime(Timestamp.newBuilder().setSeconds(1550757112).setNanos(68000000).build())
                .setStateId(3)
                .setUnitsConsumed(2F)
                .setUnitType("Percent")
                .setUsagePercent(2F)
                .setUnitsAvailable(98F)
                .setUnitsTotal(100F)
                .setUtilizationType(UtilizationOuterClass.Utilization.UtilizationType.CPU)
                .setLastUpdated(Timestamp.newBuilder().setSeconds(1550757112).setNanos(68000000).build())
                .setAction(ActionOuterClass.Action.ADD)
                .setContext(MspContextOuterClass.MspContext.newBuilder()
                        .setBizAppsCustomerId("aaea6111-b13f-462b-9385-4f3baa7f0ccc")
                        .setSystemGuid("225ed5bb-9e8e-4ad5-aa8c-7a018dc5ffa5")
                        .build())
                .build();

        testCases.setTestCase(incomingEvent, expectedResult, parsingAssertMessage);
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