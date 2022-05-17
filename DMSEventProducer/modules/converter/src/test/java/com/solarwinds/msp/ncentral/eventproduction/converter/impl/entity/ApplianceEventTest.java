package com.solarwinds.msp.ncentral.eventproduction.converter.impl.entity;

import com.google.protobuf.GeneratedMessageV3;
import com.google.protobuf.Timestamp;

import com.solarwinds.msp.ncentral.eventproduction.api.entity.Event;
import com.solarwinds.msp.ncentral.eventproduction.api.entity.EventType;
import com.solarwinds.msp.ncentral.eventproduction.converter.EventParser;
import com.solarwinds.msp.ncentral.proto.entity.ActionOuterClass;
import com.solarwinds.msp.ncentral.proto.entity.MspContextOuterClass;
import com.solarwinds.msp.ncentral.proto.entity.entity.ApplianceOuterClass;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

class ApplianceEventTest {

    private static Stream<Arguments> parseRecordTest() {
        EventParserTestCases testCases = new EventParserTestCases();
        ApplianceOuterClass.Appliance expectedResult;
        Event incomingEvent;
        String parsingAssertMessage;

        //<editor-fold desc="Valid appliance event">
        parsingAssertMessage = "Valid appliance event";
        Map<String, String> appentity = new HashMap<>();

        appentity.put("applianceid", "154400920");
        appentity.put("appliancename", "JSANTOS1-LT");
        appentity.put("customerid", "476");
        appentity.put("deviceid", "32877124");
        appentity.put("appliancetype", "Agent");
        appentity.put("isconfigrequired", "true");
        appentity.put("isreloadrequired", "false");
        appentity.put("ispublic", "false");
        appentity.put("uri", "jsantos1-lt.tul.solarwinds.net");
        appentity.put("description", "Network device discovered using Asset Discovery - 32877124");
        appentity.put("osid", "winnt");
        appentity.put("version", "2021.1.0.197");
        appentity.put("lastlogin", "2019-02-21T13:51:52.068Z");
        appentity.put("lastlogoff", null);
        appentity.put("autoupdate", "Never");
        appentity.put("issystem", "false");
        appentity.put("issoftwareagent", "false");
        appentity.put("deleted", "false");
        appentity.put("lastupdated", "2019-02-21T13:51:52.068Z");
        appentity.put("reportedosid", "win2000");
        appentity.put("reportedos", "Windows 10 Enterprise Build (18363)");
        appentity.put("upgradeattempts", "0");
        appentity.put("reboot", "false");
        appentity.put("creationtime", "2019-02-21T13:51:52.068Z");
        appentity.put("sourceip", "119.94.141.32");
        appentity.put("ismoduleconfigrequired", "false");
        appentity.put("reported_timezoneid", "08:00:00");

        incomingEvent = Event.builder()
                .entityDataTypes(Collections.EMPTY_MAP)
                .bizappsCustomerId("aaea6111-b13f-462b-9385-4f3baa7f0ccc")
                .ncentralServerGuid("225ed5bb-9e8e-4ad5-aa8c-7a018dc5ffa5")
                .eventingConfigurationCustomerId(1)
                .professionalModeLicenseType("Per Device")
                .eventType(EventType.INSERT)
                .entityType("appliance")
                .entity(appentity)
                .newValues(Collections.emptyMap())
                .build();

        expectedResult = ApplianceOuterClass.Appliance.newBuilder()
                .setApplianceId(154400920)
                .setApplianceName("JSANTOS1-LT")
                .setCustomerId(476)
                .setDeviceId(32877124)
                .setApplianceType("Agent")
                .setIsConfigRequired(true)
                .setIsReloadRequired(false)
                .setIsPublic(false)
                .setUri("jsantos1-lt.tul.solarwinds.net")
                .setDescription("Network device discovered using Asset Discovery - 32877124")
                .setVersion("2021.1.0.197")
                .setLastLogin(Timestamp.newBuilder().setSeconds(1550757112).setNanos(68000000).build())
                .setAutoUpdate("Never")
                .setIsSystem(false)
                .setUpgradeAttempts(0)
                .setReboot(false)
                .setCreationTime(Timestamp.newBuilder().setSeconds(1550757112).setNanos(68000000).build())
                .setIsModuleConfigRequired(false)
                .setReportedTimezoneId("08:00:00")
                .setLastUpdated(Timestamp.newBuilder().setSeconds(1550757112).setNanos(68000000).build())
                .setAction(ActionOuterClass.Action.ADD)
                .setContext(MspContextOuterClass.MspContext.newBuilder()
                        .setBizAppsCustomerId("aaea6111-b13f-462b-9385-4f3baa7f0ccc")
                        .setSystemGuid("225ed5bb-9e8e-4ad5-aa8c-7a018dc5ffa5")
                        .build())
                .build();

        testCases.setTestCase(incomingEvent, expectedResult, parsingAssertMessage);
        //</editor-fold>

        //<editor-fold desc="Appliance missing applianceid event">
        parsingAssertMessage = "Appliance missing applianceid event";
        Map<String, String> appentity1 = new HashMap<>(appentity);
        appentity1.replace("applianceid", null);
        incomingEvent = Event.builder()
                .entityDataTypes(Collections.EMPTY_MAP)
                .bizappsCustomerId("aaea6111-b13f-462b-9385-4f3baa7f0ccc")
                .ncentralServerGuid("225ed5bb-9e8e-4ad5-aa8c-7a018dc5ffa5")
                .eventingConfigurationCustomerId(1)
                .professionalModeLicenseType("Per Device")
                .eventType(EventType.INSERT)
                .entityType("appliance")
                .entity(appentity1)
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