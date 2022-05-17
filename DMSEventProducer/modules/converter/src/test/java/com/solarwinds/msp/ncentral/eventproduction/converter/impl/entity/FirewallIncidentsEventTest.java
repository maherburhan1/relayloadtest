package com.solarwinds.msp.ncentral.eventproduction.converter.impl.entity;

import com.google.protobuf.GeneratedMessageV3;
import com.google.protobuf.Timestamp;

import com.solarwinds.msp.ncentral.eventproduction.api.entity.Event;
import com.solarwinds.msp.ncentral.eventproduction.api.entity.EventType;
import com.solarwinds.msp.ncentral.eventproduction.converter.EventParser;
import com.solarwinds.msp.ncentral.proto.entity.ActionOuterClass;
import com.solarwinds.msp.ncentral.proto.entity.MspContextOuterClass;
import com.solarwinds.msp.ncentral.proto.entity.tasks.FirewallIncidentsOuterClass;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

class FirewallIncidentsEventTest {

    private static Stream<Arguments> parseRecordTest() {
        EventParserTestCases testCases = new EventParserTestCases();
        FirewallIncidentsOuterClass.FirewallIncidents expectedResult;
        Event incomingEvent;
        String parsingAssertMessage;

        Map<String, String> entity = new HashMap<>();
        entity.put("lastupdated", "2019-02-21T13:51:52.068Z");
        entity.put("scantime", "2019-02-21T13:51:52.068Z");
        entity.put("taskid", "2030277180");
        entity.put("datadelay", "21");
        entity.put("errormessage", "");
        entity.put("state", "3");

        //<editor-fold desc="Valid firewall Incidents event (datafwwatchguard_detailed)">
        parsingAssertMessage = "Valid firewall Incidents event (datafwwatchguard_detailed)";
        Map<String, String> entity1 = new HashMap<>(entity);

        //datafwwatchguard_detailed
        entity1.put("sfw_signature", "lzIAAAAAAADN6AVhAAAAAA==");
        entity1.put("sfw_linecnt", "0");
        entity1.put("sfw_regex1", "f");
        entity1.put("sfw_regexline1", "");
        entity1.put("sfw_regex2", "f");
        entity1.put("sfw_regexline2", "");
        entity1.put("sfw_reg1count", "1");
        entity1.put("sfw_reg2count", "0");
        entity1.put("sfw_regex3", "f");
        entity1.put("sfw_regexline3", "");
        entity1.put("sfw_reg3count", "0");
        entity1.put("sfw_regex4", "f");
        entity1.put("sfw_regexline4", "");
        entity1.put("sfw_reg4count", "0");
        entity1.put("sfw_regex5", "f");
        entity1.put("sfw_regexline5", "");
        entity1.put("sfw_reg5count", "0");
        entity1.put("sfw_regex6", "f");
        entity1.put("sfw_regexline6", "");
        entity1.put("sfw_reg6count", "0");

        incomingEvent = Event.builder()
                .entityDataTypes(Collections.EMPTY_MAP)
                .bizappsCustomerId("aaea6111-b13f-462b-9385-4f3baa7f0ccc")
                .ncentralServerGuid("225ed5bb-9e8e-4ad5-aa8c-7a018dc5ffa5")
                .eventingConfigurationCustomerId(1)
                .professionalModeLicenseType("Per Device")
                .eventType(EventType.INSERT)
                .entityType("datafwwatchguard_detailed")
                .entity(entity1)
                .newValues(Collections.emptyMap())
                .build();

        expectedResult = FirewallIncidentsOuterClass.FirewallIncidents.newBuilder()
                .setTaskId(2030277180)
                .setScanTime(Timestamp.newBuilder().setSeconds(1550757112).setNanos(68000000).build())
                .setStateId(3)
                .setEmergencyIncidents(0)
                .setAlertIncidents(0)
                .setCriticalIncidents(0.0F)
                .setErrorIncidents(1.0F)
                .setWarningIncidents(0.0F)
                .setLastUpdated(Timestamp.newBuilder().setSeconds(1550757112).setNanos(68000000).build())
                .setAction(ActionOuterClass.Action.ADD)
                .setContext(MspContextOuterClass.MspContext.newBuilder()
                        .setBizAppsCustomerId("aaea6111-b13f-462b-9385-4f3baa7f0ccc")
                        .setSystemGuid("225ed5bb-9e8e-4ad5-aa8c-7a018dc5ffa5")
                        .build())
                .build();

        testCases.setTestCase(incomingEvent, expectedResult, parsingAssertMessage);
        //</editor-fold>

        //<editor-fold desc="Valid firewall Incidents event (datafwsonicwall_detailed)">
        parsingAssertMessage = "Valid firewall Incidents event (datafwsonicwall_detailed)";
        Map<String, String> entity2 = new HashMap<>(entity);
        //datafwsonicwall_detailed
        entity2.put("sfs_signature", "fdAAAAAAAAB2UaVRAQAAALvHVEPAu9ZIMdMHAAAAAAA=");
        entity2.put("sfs_linecnt", "0");
        entity2.put("sfs_regex1", "f");
        entity2.put("sfs_regexline1", "");
        entity2.put("sfs_regex2", "f");
        entity2.put("sfs_regexline2", "");
        entity2.put("sfs_regex3", "f");
        entity2.put("sfs_regexline3", "");
        entity2.put("sfs_regex4", "f");
        entity2.put("sfs_regexline4", "");
        entity2.put("sfs_reg1count", "0");
        entity2.put("sfs_reg2count", "0");
        entity2.put("sfs_reg3count", "1");
        entity2.put("sfs_reg4count", "0");
        incomingEvent = Event.builder()
                .entityDataTypes(Collections.EMPTY_MAP)
                .bizappsCustomerId("aaea6111-b13f-462b-9385-4f3baa7f0ccc")
                .ncentralServerGuid("225ed5bb-9e8e-4ad5-aa8c-7a018dc5ffa5")
                .eventingConfigurationCustomerId(1)
                .professionalModeLicenseType("Per Device")
                .eventType(EventType.INSERT)
                .entityType("datafwsonicwall_detailed")
                .entity(entity2)
                .newValues(Collections.emptyMap())
                .build();

        testCases.setTestCase(incomingEvent, expectedResult, parsingAssertMessage);
        //</editor-fold>

        //<editor-fold desc="Valid firewall Incidents event (datafwciscopix_detailed)">
        parsingAssertMessage = "Valid firewall Incidents event (datafwciscopix_detailed)";
        Map<String, String> entity3 = new HashMap<>(entity);
        //datafwciscopix_detailed
        entity3.put("sfc_signature", "kAwAAAAAAAC9mcZoAAAAAA==");
        entity3.put("sfc_linecnt", "0");
        entity3.put("sfc_regex1", "f");
        entity3.put("sfc_regexline1", "");
        entity3.put("sfc_regex2", "f");
        entity3.put("sfc_regexline2", "");
        entity3.put("sfc_regex3", "f");
        entity3.put("sfc_regexline3", "");
        entity3.put("sfc_regex4", "f");
        entity3.put("sfc_regexline4", "");
        entity3.put("sfc_reg1count", "0");
        entity3.put("sfc_reg2count", "0");
        entity3.put("sfc_reg3count", "1");
        entity3.put("sfc_reg4count", "0");
        entity3.put("sfc_regex5", "f");
        entity3.put("sfc_regexline5", "");
        entity3.put("sfc_reg5count", "0");

        incomingEvent = Event.builder()
                .entityDataTypes(Collections.EMPTY_MAP)
                .bizappsCustomerId("aaea6111-b13f-462b-9385-4f3baa7f0ccc")
                .ncentralServerGuid("225ed5bb-9e8e-4ad5-aa8c-7a018dc5ffa5")
                .eventingConfigurationCustomerId(1)
                .professionalModeLicenseType("Per Device")
                .eventType(EventType.INSERT)
                .entityType("datafwciscopix_detailed")
                .entity(entity3)
                .newValues(Collections.emptyMap())
                .build();

        testCases.setTestCase(incomingEvent, expectedResult, parsingAssertMessage);
        //</editor-fold>

        //<editor-fold desc="Valid firewall Incidents event (datafwnetscreen_detailed)">
        parsingAssertMessage = "Valid firewall Incidents event (datafwnetscreen_detailed)";
        Map<String, String> entity4 = new HashMap<>(entity);
        //datafwnetscreen_detailed
        entity4.put("sfn_signature", "QDAAAAAAAAAAeee======");
        entity4.put("sfn_linecnt", "0");
        entity4.put("sfn_regex1", "f");
        entity4.put("sfn_regexline1", "");
        entity4.put("sfn_regex2", "f");
        entity4.put("sfn_regexline2", "");
        entity4.put("sfn_regex3", "f");
        entity4.put("sfn_regexline3", "");
        entity4.put("sfn_regex4", "f");
        entity4.put("sfn_regexline4", "");
        entity4.put("sfn_regex5", "f");
        entity4.put("sfn_regexline5", "");
        entity4.put("sfn_reg1count", "0");
        entity4.put("sfn_reg2count", "0");
        entity4.put("sfn_reg3count", "0");
        entity4.put("sfn_reg4count", "1");
        entity4.put("sfn_reg5count", "0");

        incomingEvent = Event.builder()
                .entityDataTypes(Collections.EMPTY_MAP)
                .bizappsCustomerId("aaea6111-b13f-462b-9385-4f3baa7f0ccc")
                .ncentralServerGuid("225ed5bb-9e8e-4ad5-aa8c-7a018dc5ffa5")
                .eventingConfigurationCustomerId(1)
                .professionalModeLicenseType("Per Device")
                .eventType(EventType.INSERT)
                .entityType("datafwnetscreen_detailed")
                .entity(entity4)
                .newValues(Collections.emptyMap())
                .build();

        testCases.setTestCase(incomingEvent, expectedResult, parsingAssertMessage);
        //</editor-fold>

        //<editor-fold desc="Valid firewall Incidents event (datafwfortigate_detailed)">
        parsingAssertMessage = "Valid firewall Incidents event (datafwfortigate_detailed)";
        Map<String, String> entity5 = new HashMap<>(entity);
        //datafwfortigate_detailed
        entity5.put("sff_signature", "QDIAAAAAAADEgYC0AAAAAA==");
        entity5.put("sff_linecnt", "0");
        entity5.put("sff_regex1", "f");
        entity5.put("sff_regexline1", "");
        entity5.put("sff_reg1count", "0");
        entity5.put("sff_regex2", "f");
        entity5.put("sff_regexline2", "");
        entity5.put("sff_reg2count", "0");
        entity5.put("sff_regex3", "f");
        entity5.put("sff_regexline3", "");
        entity5.put("sff_reg3count", "0");
        entity5.put("sff_regex4", "f");
        entity5.put("sff_regexline4", "");
        entity5.put("sff_reg4count", "1");
        entity5.put("sff_regex5", "f");
        entity5.put("sff_regexline5", "");
        entity5.put("sff_reg5count", "0");

        incomingEvent = Event.builder()
                .entityDataTypes(Collections.EMPTY_MAP)
                .bizappsCustomerId("aaea6111-b13f-462b-9385-4f3baa7f0ccc")
                .ncentralServerGuid("225ed5bb-9e8e-4ad5-aa8c-7a018dc5ffa5")
                .eventingConfigurationCustomerId(1)
                .professionalModeLicenseType("Per Device")
                .eventType(EventType.INSERT)
                .entityType("datafwfortigate_detailed")
                .entity(entity5)
                .newValues(Collections.emptyMap())
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