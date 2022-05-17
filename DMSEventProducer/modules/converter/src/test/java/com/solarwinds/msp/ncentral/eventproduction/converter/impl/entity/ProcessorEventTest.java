package com.solarwinds.msp.ncentral.eventproduction.converter.impl.entity;

import com.google.protobuf.GeneratedMessageV3;
import com.google.protobuf.Timestamp;

import com.solarwinds.msp.ncentral.eventproduction.api.entity.Event;
import com.solarwinds.msp.ncentral.eventproduction.api.entity.EventType;
import com.solarwinds.msp.ncentral.eventproduction.converter.EventParser;
import com.solarwinds.msp.ncentral.proto.entity.ActionOuterClass;
import com.solarwinds.msp.ncentral.proto.entity.MspContextOuterClass;
import com.solarwinds.msp.ncentral.proto.entity.asset.ProcessorOuterClass;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

class ProcessorEventTest {
    private static Stream<Arguments> parseRecordTest() {
        EventParserTestCases testCases = new EventParserTestCases();
        ProcessorOuterClass.Processor expectedResult;
        Event incomingEvent;
        String parsingAssertMessage;

        Map<String, String> entity = new HashMap<>();
        entity.put("lastupdated", "2019-02-21T13:51:52.068Z");
        entity.put("deviceid", "1454030117");

        //<editor-fold desc="Valid processor event">
        parsingAssertMessage = "Valid processor event";
        Map<String, String> entity1 = new HashMap<>(entity);
        entity1.put("deleted", "false");
        entity1.put("cpuid", "CPU0");
        entity1.put("description", "x86 Family 6 Model 30 Stepping 5");
        entity1.put("maxclockspeed", "2930");
        entity1.put("name", "Intel(R) Core(TM) i7 CPU 870 @ 2.93GHz");
        entity1.put("processorid", "");
        entity1.put("processortype", "3");
        entity1.put("cpusnmpindex", "");
        entity1.put("architecture", null);
        entity1.put("contenthash", "16b22409");
        entity1.put("numbercpucores", "2");
        entity1.put("vendor", "Intel");

        incomingEvent = Event.builder()
                .entityDataTypes(Collections.EMPTY_MAP)
                .bizappsCustomerId("aaea6111-b13f-462b-9385-4f3baa7f0ccc")
                .ncentralServerGuid("225ed5bb-9e8e-4ad5-aa8c-7a018dc5ffa5")
                .eventingConfigurationCustomerId(1)
                .professionalModeLicenseType("Per Device")
                .eventType(EventType.INSERT)
                .entityType("cim_processor")
                .entity(entity1)
                .newValues(Collections.emptyMap())
                .build();

        expectedResult = ProcessorOuterClass.Processor.newBuilder()
                .setDeviceId(1454030117)
                .setDeleted(false)
                .setCpuCoresCount(2)
                .setCpuId("CPU0")
                .setDescription("x86 Family 6 Model 30 Stepping 5")
                .setMaxClockSpeedMegahertz(2930)
                .setName("Intel(R) Core(TM) i7 CPU 870 @ 2.93GHz")
                .setProcessorType(3)
                .setVendor("Intel")
                .setLastUpdated(Timestamp.newBuilder().setSeconds(1550757112).setNanos(68000000).build())
                .setAction(ActionOuterClass.Action.ADD)
                .setContext(MspContextOuterClass.MspContext.newBuilder()
                        .setBizAppsCustomerId("aaea6111-b13f-462b-9385-4f3baa7f0ccc")
                        .setSystemGuid("225ed5bb-9e8e-4ad5-aa8c-7a018dc5ffa5")
                        .build())
                .build();

        testCases.setTestCase(incomingEvent, expectedResult, parsingAssertMessage);
        //</editor-fold>

        //<editor-fold desc="Processor event missing device id">
        parsingAssertMessage = "Processor event missing device id";
        Map<String, String> entity2 = new HashMap<>(entity1);
        entity2.remove("deviceid");
        incomingEvent = Event.builder()
                .entityDataTypes(Collections.EMPTY_MAP)
                .bizappsCustomerId("aaea6111-b13f-462b-9385-4f3baa7f0ccc")
                .ncentralServerGuid("225ed5bb-9e8e-4ad5-aa8c-7a018dc5ffa5")
                .eventingConfigurationCustomerId(1)
                .professionalModeLicenseType("Per Device")
                .eventType(EventType.INSERT)
                .entityType("cim_processor")
                .entity(entity2)
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