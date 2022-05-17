package com.solarwinds.msp.ncentral.eventproduction.converter.impl.entity;

import com.google.protobuf.GeneratedMessageV3;
import com.google.protobuf.Timestamp;

import com.solarwinds.msp.ncentral.eventproduction.api.entity.Event;
import com.solarwinds.msp.ncentral.eventproduction.api.entity.EventType;
import com.solarwinds.msp.ncentral.eventproduction.converter.EventParser;
import com.solarwinds.msp.ncentral.proto.entity.ActionOuterClass;
import com.solarwinds.msp.ncentral.proto.entity.MspContextOuterClass;
import com.solarwinds.msp.ncentral.proto.entity.entity.AccessGroupUserOuterClass;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

class AccessGroupUserEventTest {

    private static Stream<Arguments> parseRecordTest() {
        EventParserTestCases testCases = new EventParserTestCases();
        AccessGroupUserOuterClass.AccessGroupUser expectedResult;
        Event incomingEvent;
        String parsingAssertMessage;

        Map<String, String> accessGroupUserEntity = new HashMap<>();
        accessGroupUserEntity.put("groupid", "725275586");
        accessGroupUserEntity.put("userid", "135495050");
        accessGroupUserEntity.put("deleted", "false");
        accessGroupUserEntity.put("lastupdated", "2019-02-21T13:51:52.068Z");

        // <editor-fold desc="Valid access group by user event">
        parsingAssertMessage = "Valid access group user event";

        incomingEvent = Event.builder()
                .entityDataTypes(Collections.EMPTY_MAP)
                .bizappsCustomerId("aaea6111-b13f-462b-9385-4f3baa7f0ccc")
                .ncentralServerGuid("225ed5bb-9e8e-4ad5-aa8c-7a018dc5ffa5")
                .eventingConfigurationCustomerId(1)
                .professionalModeLicenseType("Per Device")
                .eventType(EventType.INSERT)
                .entityType("accessgroupusermap")
                .entity(accessGroupUserEntity)
                .newValues(Collections.emptyMap())
                .build();

        expectedResult = AccessGroupUserOuterClass.AccessGroupUser.newBuilder()
                .setGroupId(725275586)
                .addUserId(135495050)
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

        // <editor-fold desc="Access Group User missing userId event">
        parsingAssertMessage = "Access Group User missing userId event";
        Map<String, String> accessGroupUserMissingUserIdEntity = new HashMap<>(accessGroupUserEntity);
        accessGroupUserMissingUserIdEntity.remove("userid");

        incomingEvent = Event.builder()
                .entityDataTypes(Collections.EMPTY_MAP)
                .bizappsCustomerId("aaea6111-b13f-462b-9385-4f3baa7f0ccc")
                .ncentralServerGuid("225ed5bb-9e8e-4ad5-aa8c-7a018dc5ffa5")
                .eventingConfigurationCustomerId(1)
                .professionalModeLicenseType("Per Device")
                .eventType(EventType.INSERT)
                .entityType("accessgroupusermap")
                .entity(accessGroupUserMissingUserIdEntity)
                .newValues(Collections.emptyMap())
                .build();

        testCases.setTestCase(incomingEvent, parsingAssertMessage);
        // </editor-fold>

        // <editor-fold desc="Access Group User missing groupid event">
        parsingAssertMessage = "Access Group User missing groupid event";
        Map<String, String> accessGroupUserMissingGroupIdEntity = new HashMap<>(accessGroupUserEntity);
        accessGroupUserMissingGroupIdEntity.remove("groupid");

        incomingEvent = Event.builder()
                .entityDataTypes(Collections.EMPTY_MAP)
                .bizappsCustomerId("aaea6111-b13f-462b-9385-4f3baa7f0ccc")
                .ncentralServerGuid("225ed5bb-9e8e-4ad5-aa8c-7a018dc5ffa5")
                .eventingConfigurationCustomerId(1)
                .professionalModeLicenseType("Per Device")
                .eventType(EventType.INSERT)
                .entityType("accessgroupusermap")
                .entity(accessGroupUserMissingGroupIdEntity)
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
