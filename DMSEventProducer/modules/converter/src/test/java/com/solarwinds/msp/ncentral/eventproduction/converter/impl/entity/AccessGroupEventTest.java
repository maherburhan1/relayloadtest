package com.solarwinds.msp.ncentral.eventproduction.converter.impl.entity;

import com.google.protobuf.GeneratedMessageV3;
import com.google.protobuf.Timestamp;

import com.solarwinds.msp.ncentral.eventproduction.api.entity.Event;
import com.solarwinds.msp.ncentral.eventproduction.api.entity.EventType;
import com.solarwinds.msp.ncentral.eventproduction.converter.EventParser;
import com.solarwinds.msp.ncentral.proto.entity.ActionOuterClass;
import com.solarwinds.msp.ncentral.proto.entity.MspContextOuterClass;
import com.solarwinds.msp.ncentral.proto.entity.entity.AccessGroupOuterClass;
import com.solarwinds.msp.ncentral.proto.entity.entity.AccessGroupTypeOuterClass;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

class AccessGroupEventTest {

    private static Stream<Arguments> parseRecordTest() {
        EventParserTestCases testCases = new EventParserTestCases();
        AccessGroupOuterClass.AccessGroup expectedResult;
        Event incomingEvent;
        String parsingAssertMessage;

        Map<String, String> accessGroupByCustomerEntity = new HashMap<>();
        accessGroupByCustomerEntity.put("groupid", "725275586");
        accessGroupByCustomerEntity.put("customerid", "50");
        accessGroupByCustomerEntity.put("groupname", "Service Organization Access Group");
        accessGroupByCustomerEntity.put("description", "Access Group Test");
        accessGroupByCustomerEntity.put("deleted", "false");
        accessGroupByCustomerEntity.put("lastupdated", "2019-02-21T13:51:52.068Z");
        accessGroupByCustomerEntity.put("groupuuid", "b9fc7ac8-c0de-74b0-3420-68c4a670395c");
        accessGroupByCustomerEntity.put("grouptype", "1");

        // <editor-fold desc="Valid access group by customer event">
        parsingAssertMessage = "Valid access group by customer event";

        incomingEvent = Event.builder()
                .entityDataTypes(Collections.EMPTY_MAP)
                .bizappsCustomerId("aaea6111-b13f-462b-9385-4f3baa7f0ccc")
                .ncentralServerGuid("225ed5bb-9e8e-4ad5-aa8c-7a018dc5ffa5")
                .eventingConfigurationCustomerId(1)
                .professionalModeLicenseType("Per Device")
                .eventType(EventType.INSERT)
                .entityType("accessgroup")
                .entity(accessGroupByCustomerEntity)
                .newValues(Collections.emptyMap())
                .build();

        expectedResult = AccessGroupOuterClass.AccessGroup.newBuilder()
                .setGroupId(725275586)
                .setClientId(50)
                .setGroupName("Service Organization Access Group")
                .setDescription("Access Group Test")
                .setGroupType(AccessGroupTypeOuterClass.AccessGroupType.CLIENT)
                .setDeleted(false)
                .setLastUpdated(Timestamp.newBuilder().setSeconds(1550757112).setNanos(68000000).build())
                .setGroupUuid("b9fc7ac8-c0de-74b0-3420-68c4a670395c")
                .setAction(ActionOuterClass.Action.ADD)
                .setContext(MspContextOuterClass.MspContext.newBuilder()
                        .setBizAppsCustomerId("aaea6111-b13f-462b-9385-4f3baa7f0ccc")
                        .setSystemGuid("225ed5bb-9e8e-4ad5-aa8c-7a018dc5ffa5")
                        .build())
                .build();

        testCases.setTestCase(incomingEvent, expectedResult, parsingAssertMessage);
        // </editor-fold>

        // <editor-fold desc="Valid access group by device event">
        Map<String, String> accessGroupByDeviceEntity = new HashMap<>(accessGroupByCustomerEntity);
        accessGroupByDeviceEntity.replace("grouptype", "2");
        parsingAssertMessage = "Valid access group by device event";

        incomingEvent = Event.builder()
                .entityDataTypes(Collections.EMPTY_MAP)
                .bizappsCustomerId("aaea6111-b13f-462b-9385-4f3baa7f0ccc")
                .ncentralServerGuid("225ed5bb-9e8e-4ad5-aa8c-7a018dc5ffa5")
                .eventingConfigurationCustomerId(1)
                .professionalModeLicenseType("Per Device")
                .eventType(EventType.INSERT)
                .entityType("accessgroup")
                .entity(accessGroupByDeviceEntity)
                .newValues(Collections.emptyMap())
                .build();

        expectedResult = AccessGroupOuterClass.AccessGroup.newBuilder()
                .setGroupId(725275586)
                .setClientId(50)
                .setGroupName("Service Organization Access Group")
                .setDescription("Access Group Test")
                .setGroupType(AccessGroupTypeOuterClass.AccessGroupType.DEVICE)
                .setDeleted(false)
                .setLastUpdated(Timestamp.newBuilder().setSeconds(1550757112).setNanos(68000000).build())
                .setGroupUuid("b9fc7ac8-c0de-74b0-3420-68c4a670395c")
                .setAction(ActionOuterClass.Action.ADD)
                .setContext(MspContextOuterClass.MspContext.newBuilder()
                        .setBizAppsCustomerId("aaea6111-b13f-462b-9385-4f3baa7f0ccc")
                        .setSystemGuid("225ed5bb-9e8e-4ad5-aa8c-7a018dc5ffa5")
                        .build())
                .build();

        testCases.setTestCase(incomingEvent, expectedResult, parsingAssertMessage);
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
