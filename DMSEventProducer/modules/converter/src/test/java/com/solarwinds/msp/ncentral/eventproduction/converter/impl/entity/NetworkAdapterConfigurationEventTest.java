package com.solarwinds.msp.ncentral.eventproduction.converter.impl.entity;

import com.google.protobuf.GeneratedMessageV3;
import com.google.protobuf.Timestamp;

import com.solarwinds.msp.ncentral.eventproduction.api.entity.Event;
import com.solarwinds.msp.ncentral.eventproduction.api.entity.EventType;
import com.solarwinds.msp.ncentral.eventproduction.converter.EventParser;
import com.solarwinds.msp.ncentral.proto.entity.ActionOuterClass;
import com.solarwinds.msp.ncentral.proto.entity.MspContextOuterClass;
import com.solarwinds.msp.ncentral.proto.entity.asset.NetworkAdapterConfigurationOuterClass;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

class NetworkAdapterConfigurationEventTest {
    private static Stream<Arguments> parseRecordTest() {
        EventParserTestCases testCases = new EventParserTestCases();
        NetworkAdapterConfigurationOuterClass.NetworkAdapterConfiguration expectedResult;
        Event incomingEvent;
        String parsingAssertMessage;

        Map<String, String> entity = new HashMap<>();
        entity.put("lastupdated", "2019-02-21T13:51:52.068Z");
        entity.put("deviceid", "1454030117");

        //<editor-fold desc="Valid network adapter configuration event">
        parsingAssertMessage = "Valid network adapter configuration event";
        Map<String, String> entity1 = new HashMap<>(entity);
        entity1.put("deleted", "false");
        entity1.put("macaddress", "ab:cd:ef:gh:12:34");
        entity1.put("adapterid", "3");
        entity1.put("description", "hyper - v virtual ethernet adapter");
        entity1.put("contenthash", "4e7 b1e23");
        entity1.put("adaptertype", "");
        entity1.put("ipaddress", "1.1.1.1");
        entity1.put("hostname", "test.local");
        entity1.put("gateway", "10.10.10.1, fe80::ca9c:1d ff:feb9:c861");
        entity1.put("dnsserver", "10.10.10 .10, 10.11.11.11");
        entity1.put("dhcpserver", "10.10.10.10");

        incomingEvent = Event.builder()
                .entityDataTypes(Collections.EMPTY_MAP)
                .bizappsCustomerId("aaea6111-b13f-462b-9385-4f3baa7f0ccc")
                .ncentralServerGuid("225ed5bb-9e8e-4ad5-aa8c-7a018dc5ffa5")
                .eventingConfigurationCustomerId(1)
                .professionalModeLicenseType("Per Device")
                .eventType(EventType.INSERT)
                .entityType("cim_networkadapterconfiguration")
                .entity(entity1)
                .newValues(Collections.emptyMap())
                .build();

        expectedResult = NetworkAdapterConfigurationOuterClass.NetworkAdapterConfiguration.newBuilder()
                .setDeviceId(1454030117)
                .setDeleted(false)
                .setDescription("hyper - v virtual ethernet adapter")
                .setMacAddress("ab:cd:ef:gh:12:34")
                .setIpAddress("1.1.1.1")
                .setHostName("test.local")
                .setLastUpdated(Timestamp.newBuilder().setSeconds(1550757112).setNanos(68000000).build())
                .setAction(ActionOuterClass.Action.ADD)
                .setContext(MspContextOuterClass.MspContext.newBuilder()
                        .setBizAppsCustomerId("aaea6111-b13f-462b-9385-4f3baa7f0ccc")
                        .setSystemGuid("225ed5bb-9e8e-4ad5-aa8c-7a018dc5ffa5")
                        .build())
                .build();

        testCases.setTestCase(incomingEvent, expectedResult, parsingAssertMessage);
        //</editor-fold>

        //<editor-fold desc="Network adapter configuration event missing device id">
        parsingAssertMessage = "Network adapter configuration event missing device id";
        Map<String, String> entity2 = new HashMap<>(entity1);
        entity2.remove("deviceid");
        incomingEvent = Event.builder()
                .entityDataTypes(Collections.EMPTY_MAP)
                .bizappsCustomerId("aaea6111-b13f-462b-9385-4f3baa7f0ccc")
                .ncentralServerGuid("225ed5bb-9e8e-4ad5-aa8c-7a018dc5ffa5")
                .eventingConfigurationCustomerId(1)
                .professionalModeLicenseType("Per Device")
                .eventType(EventType.INSERT)
                .entityType("cim_networkadapterconfiguration")
                .entity(entity2)
                .newValues(Collections.emptyMap())
                .build();

        testCases.setTestCase(incomingEvent, parsingAssertMessage);
        //</editor-fold>

        //<editor-fold desc="Network adapter configuration event missing adapter id">
        parsingAssertMessage = "Network adapter configuration event missing adapter id";
        Map<String, String> entity3 = new HashMap<>(entity1);
        entity3.remove("macaddress");
        incomingEvent = Event.builder()
                .entityDataTypes(Collections.EMPTY_MAP)
                .bizappsCustomerId("aaea6111-b13f-462b-9385-4f3baa7f0ccc")
                .ncentralServerGuid("225ed5bb-9e8e-4ad5-aa8c-7a018dc5ffa5")
                .eventingConfigurationCustomerId(1)
                .professionalModeLicenseType("Per Device")
                .eventType(EventType.INSERT)
                .entityType("cim_networkadapterconfiguration")
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