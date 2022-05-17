package com.solarwinds.msp.ncentral.eventproduction.converter.impl.entity;

import com.google.protobuf.GeneratedMessageV3;
import com.google.protobuf.Timestamp;

import com.solarwinds.msp.ncentral.eventproduction.api.entity.Event;
import com.solarwinds.msp.ncentral.eventproduction.api.entity.EventType;
import com.solarwinds.msp.ncentral.eventproduction.converter.EventParser;
import com.solarwinds.msp.ncentral.proto.entity.ActionOuterClass;
import com.solarwinds.msp.ncentral.proto.entity.MspContextOuterClass;
import com.solarwinds.msp.ncentral.proto.entity.entity.AccessGroupCustomerOuterClass;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

class AccessGroupCustomerEventTest {

    private static Stream<Arguments> parseRecordTest() {
        EventParserTestCases testCases = new EventParserTestCases();
        AccessGroupCustomerOuterClass.AccessGroupCustomer expectedResult;
        Event incomingEvent;
        String parsingAssertMessage;

        Map<String, String> accessGroupCustomerEntity = new HashMap<>();
        accessGroupCustomerEntity.put("groupid", "725275586");
        accessGroupCustomerEntity.put("customerid", "50");
        accessGroupCustomerEntity.put("deleted", "false");
        accessGroupCustomerEntity.put("lastupdated", "2019-02-21T13:51:52.068Z");

        // <editor-fold desc="Valid access group by customer event">
        parsingAssertMessage = "Valid access group customer event";

        incomingEvent = Event.builder()
                .entityDataTypes(Collections.EMPTY_MAP)
                .bizappsCustomerId("aaea6111-b13f-462b-9385-4f3baa7f0ccc")
                .ncentralServerGuid("225ed5bb-9e8e-4ad5-aa8c-7a018dc5ffa5")
                .eventingConfigurationCustomerId(1)
                .professionalModeLicenseType("Per Device")
                .eventType(EventType.INSERT)
                .entityType("accessgroupcustomermap")
                .entity(accessGroupCustomerEntity)
                .newValues(Collections.emptyMap())
                .build();

        expectedResult = AccessGroupCustomerOuterClass.AccessGroupCustomer.newBuilder()
                .setGroupId(725275586)
                .addClientId(50)
                .setDeleted(false)
                .setLastUpdated(Timestamp.newBuilder().setSeconds(1550757112).setNanos(68000000).build())
                .setAction(ActionOuterClass.Action.ADD)
                .setContext(MspContextOuterClass.MspContext.newBuilder()
                        .setBizAppsCustomerId("aaea6111-b13f-462b-9385-4f3baa7f0ccc")
                        .setSystemGuid("225ed5bb-9e8e-4ad5-aa8c-7a018dc5ffa5")
                        .build())
                .build();

        testCases.setTestCase(incomingEvent, expectedResult, parsingAssertMessage);
        // </editor-fold>

        // <editor-fold desc="Access Group Customer missing customerid event">
        parsingAssertMessage = "Access Group Customer missing customerid event";
        Map<String, String> accessGroupCustomerMissingCustomerIdEntity = new HashMap<>(accessGroupCustomerEntity);
        accessGroupCustomerMissingCustomerIdEntity.remove("customerid");

        incomingEvent = Event.builder()
                .entityDataTypes(Collections.EMPTY_MAP)
                .bizappsCustomerId("aaea6111-b13f-462b-9385-4f3baa7f0ccc")
                .ncentralServerGuid("225ed5bb-9e8e-4ad5-aa8c-7a018dc5ffa5")
                .eventingConfigurationCustomerId(1)
                .professionalModeLicenseType("Per Device")
                .eventType(EventType.INSERT)
                .entityType("accessgroupcustomermap")
                .entity(accessGroupCustomerMissingCustomerIdEntity)
                .newValues(Collections.emptyMap())
                .build();

        testCases.setTestCase(incomingEvent, parsingAssertMessage);
        // </editor-fold>

        // <editor-fold desc="Access Group Customer missing groupid event">
        parsingAssertMessage = "Access Group Customer missing groupid event";
        Map<String, String> accessGroupCustomerMissingGroupIdEntity = new HashMap<>(accessGroupCustomerEntity);
        accessGroupCustomerMissingGroupIdEntity.remove("groupid");

        incomingEvent = Event.builder()
                .entityDataTypes(Collections.EMPTY_MAP)
                .bizappsCustomerId("aaea6111-b13f-462b-9385-4f3baa7f0ccc")
                .ncentralServerGuid("225ed5bb-9e8e-4ad5-aa8c-7a018dc5ffa5")
                .eventingConfigurationCustomerId(1)
                .professionalModeLicenseType("Per Device")
                .eventType(EventType.INSERT)
                .entityType("accessgroupcustomermap")
                .entity(accessGroupCustomerMissingGroupIdEntity)
                .newValues(Collections.emptyMap())
                .build();

        testCases.setTestCase(incomingEvent, parsingAssertMessage);
        // </editor-fold>
        return testCases.toArguments();
    }

    @ParameterizedTest(name = "Run {index}: {3}")
    @MethodSource
    void parseRecordTest(EventParser<GeneratedMessageV3> eventParser, Event incomingEvent,
            List<com.google.protobuf.GeneratedMessageV3> expectedResult, String assertMessage) {
        Assertions.assertEquals(expectedResult, eventParser.parse(incomingEvent), assertMessage);
    }
}
