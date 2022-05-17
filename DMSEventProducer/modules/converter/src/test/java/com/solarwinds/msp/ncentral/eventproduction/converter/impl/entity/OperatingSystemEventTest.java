package com.solarwinds.msp.ncentral.eventproduction.converter.impl.entity;

import com.google.protobuf.GeneratedMessageV3;
import com.google.protobuf.Timestamp;

import com.solarwinds.msp.ncentral.eventproduction.api.entity.Event;
import com.solarwinds.msp.ncentral.eventproduction.api.entity.EventType;
import com.solarwinds.msp.ncentral.eventproduction.converter.EventParser;
import com.solarwinds.msp.ncentral.proto.entity.ActionOuterClass;
import com.solarwinds.msp.ncentral.proto.entity.MspContextOuterClass;
import com.solarwinds.msp.ncentral.proto.entity.asset.OperatingSystemOuterClass;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

class OperatingSystemEventTest {

    private static Stream<Arguments> parseRecordTest() {
        EventParserTestCases testCases = new EventParserTestCases();
        OperatingSystemOuterClass.OperatingSystem expectedResult;
        Event incomingEvent;
        String parsingAssertMessage;

        Map<String, String> operatingSystemEntity = new HashMap<>();
        operatingSystemEntity.put("lastupdated", "2019-02-21T13:51:52.068Z");
        operatingSystemEntity.put("deviceid", "1907480434");

        //<editor-fold desc="Valid operating system event">
        parsingAssertMessage = "Valid operating system event";
        Map<String, String> operatingSystemEntity1 = new HashMap<>(operatingSystemEntity);
        operatingSystemEntity1.put("supportedos", "Microsoft Windows Server 2016 Standard x64 Edition");
        operatingSystemEntity1.put("csdversion", "null");
        operatingSystemEntity1.put("reportedos", "Microsoft Windows Server 2019 Standard");
        operatingSystemEntity1.put("serialnumber", "00429-00521-79784-AA434");
        operatingSystemEntity1.put("servicepackmajor", "0");
        operatingSystemEntity1.put("servicepackminor", "0");
        operatingSystemEntity1.put("version", "10.0.17763");
        operatingSystemEntity1.put("lastbootuptime", "2017-05-09T22:13:14.491Z");
        operatingSystemEntity1.put("installdate", "2016-11-02T16:06:18.000Z");
        operatingSystemEntity1.put("timesystemup", "6402");
        operatingSystemEntity1.put("osarchitecture", "64-bit");
        operatingSystemEntity1.put("ostype", "18");
        operatingSystemEntity1.put("operatingsystemsku", "7");
        operatingSystemEntity1.put("osproductsuite", "272");
        operatingSystemEntity1.put("othertypedescription", "null");
        operatingSystemEntity1.put("suitemask", "272");
        operatingSystemEntity1.put("deleted", "false");
        operatingSystemEntity1.put("recordid", "00");
        operatingSystemEntity1.put("contenthash", "7f02e5a4");
        operatingSystemEntity1.put("publisher", "");
        operatingSystemEntity1.put("licensekey", "null");
        operatingSystemEntity1.put("licensetype", "null");
        operatingSystemEntity1.put("wproducttype", "3");
        incomingEvent = Event.builder()
                .entityDataTypes(Collections.EMPTY_MAP)
                .bizappsCustomerId("aaea6111-b13f-462b-9385-4f3baa7f0ccc")
                .ncentralServerGuid("225ed5bb-9e8e-4ad5-aa8c-7a018dc5ffa5")
                .eventingConfigurationCustomerId(1)
                .professionalModeLicenseType("Per Device")
                .eventType(EventType.INSERT)
                .entityType("cim_operatingsystem")
                .entity(operatingSystemEntity1)
                .newValues(Collections.emptyMap())
                .build();

        expectedResult = OperatingSystemOuterClass.OperatingSystem.newBuilder()
                .setDeviceId(1907480434)
                .setType("Microsoft Windows Server 2016 Standard x64 Edition")
                .setName("Microsoft Windows Server 2019 Standard")
                .setVersion("10.0.17763")
                .setInstallDate(Timestamp.newBuilder().setSeconds(1478102778).build())
                .setLastBootTime(Timestamp.newBuilder().setSeconds(1494367994).setNanos(491000000).build())
                .setTimeSystemUp(6402)
                .setServicePackMajor(0)
                .setServicePackMinor(0)
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

        //<editor-fold desc="Operating system event missing deviceid">
        parsingAssertMessage = "Operating system event missing deviceid";
        Map<String, String> operatingSystemEntiry2 = new HashMap<>(operatingSystemEntity);
        operatingSystemEntiry2.remove("deviceid");
        incomingEvent = Event.builder()
                .entityDataTypes(Collections.EMPTY_MAP)
                .bizappsCustomerId("aaea6111-b13f-462b-9385-4f3baa7f0ccc")
                .ncentralServerGuid("225ed5bb-9e8e-4ad5-aa8c-7a018dc5ffa5")
                .eventingConfigurationCustomerId(1)
                .professionalModeLicenseType("Per Device")
                .eventType(EventType.INSERT)
                .entityType("cim_operatingsystem")
                .entity(operatingSystemEntiry2)
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