package com.solarwinds.msp.ncentral.eventproduction.converter.impl.entity;

import com.google.protobuf.GeneratedMessageV3;
import com.google.protobuf.Timestamp;

import com.solarwinds.msp.ncentral.eventproduction.api.entity.Event;
import com.solarwinds.msp.ncentral.eventproduction.api.entity.EventType;
import com.solarwinds.msp.ncentral.eventproduction.api.entity.RolePermissionConstants;
import com.solarwinds.msp.ncentral.eventproduction.converter.EventParser;
import com.solarwinds.msp.ncentral.proto.entity.ActionOuterClass;
import com.solarwinds.msp.ncentral.proto.entity.MspContextOuterClass;
import com.solarwinds.msp.ncentral.proto.entity.entity.AccessGroupTypeOuterClass.AccessGroupType;
import com.solarwinds.msp.ncentral.proto.entity.entity.AdvancedReportingUserOuterClass;
import com.solarwinds.msp.ncentral.proto.entity.entity.AdvancedReportingUserOuterClass.AdvancedReportingUser;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

class AdvancedReportingUserEventTest {
    private static final String USER_ID_KEY = "userid";
    private static final String FIRST_NAME_KEY = "firstname";
    private static final String LAST_NAME_KEY = "lastname";
    private static final String CUSTOMER_ID_KEY = "customername";
    private static final String EMAIL_KEY = "email1";
    private static final String USER_ENABLED_KEY = "userenabled";
    private static final String DELETED_KEY = "deleted";
    private static final String LAST_UPDATED_KEY = "lastupdated";
    private static final String PERMISSION_ID_KEY = "permissionid";
    private static final String CUSTOMER_GROUP_KEY = "customergroup";
    private static final String TAKE_OVER_USER_ID = "takeoveruserid";
    private static final String TAKE_OVER_USER_EMAIL = "takeoveruseremail";

    private static final String USER_ID_VALUE = "725275586";
    private static final String FIRST_NAME_VALUE = "advanced";
    private static final String FIRST_NAME_DELETED_VALUE = "DELETED ID 725275586: advanced";
    private static final String LAST_NAME_VALUE = "user";
    private static final String CUSTOMER_ID_VALUE = "50";
    private static final String EMAIL_VALUE = "advanceduser@report.com";
    private static final String EMAIL_DELETED_VALUE = "DELETED 725275586: advanceduser@report.com";
    private static final String USER_ENABLED_VALUE_TRUE = "true";
    private static final String DELETED_VALUE_FALSE = "false";
    private static final String LAST_UPDATED_VALUE = "2019-02-21T13:51:52.068Z";
    private static final String PERMISSION_ID_VALUE_READ_ONLY = String
            .valueOf(RolePermissionConstants.NCENTRAL_REPORTS_ADVANCED_READ_ONLY);
    private static final String CUSTOMER_GROUP_VALUE = "100, 101, 50";
    private static final String TAKE_OVER_USER_ID_VALUE = "1234";
    private static final String TAKE_OVER_USER_EMAIL_VALUE = "advanceduserparent@report.com";

    private static final String BIZ_APPS_CUSTOMER_ID = "aaea6111-b13f-462b-9385-4f3baa7f0ccc";
    private static final String SERVER_GUID = "225ed5bb-9e8e-4ad5-aa8c-7a018dc5ffa5";
    private static final String ENTITY_TYPE = "advancedreportinguser_view";
    private static final String PER_DEVICE_LICENSE_TYPE = "Per Device";

    private static Stream<Arguments> parseRecordTest() {
        EventParserTestCases testCases = new EventParserTestCases();
        AdvancedReportingUserOuterClass.AdvancedReportingUser expectedResult;
        Event incomingEvent;
        String parsingAssertMessage;

        Map<String, String> advancedReportingUserEntity = new HashMap<>();
        advancedReportingUserEntity.put(USER_ID_KEY, USER_ID_VALUE);
        advancedReportingUserEntity.put(FIRST_NAME_KEY, FIRST_NAME_VALUE);
        advancedReportingUserEntity.put(LAST_NAME_KEY, LAST_NAME_VALUE);
        advancedReportingUserEntity.put(CUSTOMER_ID_KEY, CUSTOMER_ID_VALUE);
        advancedReportingUserEntity.put(EMAIL_KEY, EMAIL_VALUE);
        advancedReportingUserEntity.put(USER_ENABLED_KEY, USER_ENABLED_VALUE_TRUE);
        advancedReportingUserEntity.put(DELETED_KEY, DELETED_VALUE_FALSE);
        advancedReportingUserEntity.put(LAST_UPDATED_KEY, LAST_UPDATED_VALUE);
        advancedReportingUserEntity.put(PERMISSION_ID_KEY, PERMISSION_ID_VALUE_READ_ONLY);
        advancedReportingUserEntity.put(CUSTOMER_GROUP_KEY, CUSTOMER_GROUP_VALUE);

        // <editor-fold desc="Valid advanced reporting user event">
        parsingAssertMessage = "Valid Advanced Reporting User event";

        incomingEvent = Event.builder()
                .entityDataTypes(Collections.emptyMap())
                .bizappsCustomerId(BIZ_APPS_CUSTOMER_ID)
                .ncentralServerGuid(SERVER_GUID)
                .eventingConfigurationCustomerId(1)
                .professionalModeLicenseType(PER_DEVICE_LICENSE_TYPE)
                .eventType(EventType.INSERT)
                .entityType(ENTITY_TYPE)
                .entity(advancedReportingUserEntity)
                .newValues(Collections.emptyMap())
                .build();

        AdvancedReportingUser.ResourceAccessGroup resourceGroup =
                AdvancedReportingUser.ResourceAccessGroup
                .newBuilder()
                .addAllId(Arrays.asList(100, 101, 50))
                .setType(AccessGroupType.CLIENT)
                .build();
        expectedResult = AdvancedReportingUserOuterClass.AdvancedReportingUser
                .newBuilder()
                .setUserId(725275586)
                .setFirstName(FIRST_NAME_VALUE)
                .setLastName(LAST_NAME_VALUE)
                .setEmail(EMAIL_VALUE)
                .setEnabled(true)
                .setRoleType(
                        AdvancedReportingUserOuterClass.AdvancedReportingUser.RoleType.READ_ONLY)
                .addResourceAccessGroup(resourceGroup)
                .setDeleted(false)
                .setLastUpdated(Timestamp.newBuilder().setSeconds(1550757112).setNanos(68000000).build())
                .setAction(ActionOuterClass.Action.ADD)
                .setContext(MspContextOuterClass.MspContext.newBuilder()
                        .setBizAppsCustomerId(BIZ_APPS_CUSTOMER_ID)
                        .setSystemGuid(SERVER_GUID)
                        .build())
                .build();

        testCases.setTestCase(incomingEvent, expectedResult, parsingAssertMessage);
        // </editor-fold>

        // <editor-fold desc="Advanced Reporting User with no customer access event">
        parsingAssertMessage = "Advanced Reporting User with no customer access event";
        Map<String, String> advancedReportingUserCustomerAccessRemovedEntity = new HashMap<>(
                advancedReportingUserEntity);
        advancedReportingUserCustomerAccessRemovedEntity.replace(CUSTOMER_GROUP_KEY, null);

        incomingEvent = Event.builder()
                .entityDataTypes(Collections.emptyMap())
                .bizappsCustomerId(BIZ_APPS_CUSTOMER_ID)
                .ncentralServerGuid(SERVER_GUID)
                .eventingConfigurationCustomerId(1)
                .professionalModeLicenseType(PER_DEVICE_LICENSE_TYPE)
                .eventType(EventType.INSERT)
                .entityType(ENTITY_TYPE)
                .entity(advancedReportingUserCustomerAccessRemovedEntity)
                .newValues(Collections.emptyMap())
                .build();

        resourceGroup = AdvancedReportingUser.ResourceAccessGroup.newBuilder().setType(AccessGroupType.CLIENT).build();
        expectedResult = AdvancedReportingUserOuterClass.AdvancedReportingUser
                .newBuilder()
                .setUserId(725275586)
                .setFirstName(FIRST_NAME_VALUE)
                .setLastName(LAST_NAME_VALUE)
                .setEmail(EMAIL_VALUE)
                .setEnabled(true)
                .setRoleType(
                        AdvancedReportingUserOuterClass.AdvancedReportingUser.RoleType.READ_ONLY)
                .addResourceAccessGroup(resourceGroup)
                .setDeleted(false)
                .setLastUpdated(Timestamp.newBuilder().setSeconds(1550757112).setNanos(68000000).build())
                .setAction(ActionOuterClass.Action.ADD)
                .setContext(MspContextOuterClass.MspContext.newBuilder()
                        .setBizAppsCustomerId(BIZ_APPS_CUSTOMER_ID)
                        .setSystemGuid(SERVER_GUID)
                        .build())
                .build();
        testCases.setTestCase(incomingEvent, expectedResult, parsingAssertMessage);
        // </editor-fold>

        // <editor-fold desc="Advanced Reporting User missing customer access event">
        parsingAssertMessage = "Advanced Reporting User missing customer access event";
        Map<String, String> advancedReportingUserCustomerAccessMissingEntity = new HashMap<>(
                advancedReportingUserEntity);
        advancedReportingUserCustomerAccessMissingEntity.remove(CUSTOMER_GROUP_KEY);

        incomingEvent = Event.builder()
                .entityDataTypes(Collections.emptyMap())
                .bizappsCustomerId(BIZ_APPS_CUSTOMER_ID)
                .ncentralServerGuid(SERVER_GUID)
                .eventingConfigurationCustomerId(1)
                .professionalModeLicenseType(PER_DEVICE_LICENSE_TYPE)
                .eventType(EventType.INSERT)
                .entityType(ENTITY_TYPE)
                .entity(advancedReportingUserCustomerAccessMissingEntity)
                .newValues(Collections.emptyMap())
                .build();

        testCases.setTestCase(incomingEvent, parsingAssertMessage);
        // </editor-fold>

        // <editor-fold desc="Advanced Reporting User permission missing event">
        parsingAssertMessage = "Advanced Reporting User permission missing event";
        Map<String, String> advancedReportingUserPermissionMissingEntity = new HashMap<>(advancedReportingUserEntity);
        advancedReportingUserPermissionMissingEntity.replace(PERMISSION_ID_KEY, "-1");

        incomingEvent = Event.builder()
                .entityDataTypes(Collections.emptyMap())
                .bizappsCustomerId(BIZ_APPS_CUSTOMER_ID)
                .ncentralServerGuid(SERVER_GUID)
                .eventingConfigurationCustomerId(1)
                .professionalModeLicenseType(PER_DEVICE_LICENSE_TYPE)
                .eventType(EventType.INSERT)
                .entityType(ENTITY_TYPE)
                .entity(advancedReportingUserPermissionMissingEntity)
                .newValues(Collections.emptyMap())
                .build();

        testCases.setTestCase(incomingEvent, parsingAssertMessage);
        // </editor-fold>

        // <editor-fold desc="Advanced Reporting User permission missing event">
        parsingAssertMessage = "Advanced Reporting User missing mandatory field event";
        Map<String, String> advancedReportingUserMissingMandatoryFieldEntity = new HashMap<>(
                advancedReportingUserEntity);
        advancedReportingUserMissingMandatoryFieldEntity.remove(EMAIL_KEY);

        incomingEvent = Event.builder()
                .entityDataTypes(Collections.emptyMap())
                .bizappsCustomerId(BIZ_APPS_CUSTOMER_ID)
                .ncentralServerGuid(SERVER_GUID)
                .eventingConfigurationCustomerId(1)
                .professionalModeLicenseType(PER_DEVICE_LICENSE_TYPE)
                .eventType(EventType.INSERT)
                .entityType(ENTITY_TYPE)
                .entity(advancedReportingUserMissingMandatoryFieldEntity)
                .newValues(Collections.emptyMap())
                .build();

        testCases.setTestCase(incomingEvent, parsingAssertMessage);
        // </editor-fold>

        // <editor-fold desc="Delete Advanced Reporting User event with manage
        // permission">
        parsingAssertMessage = "Delete Advanced Reporting User event with manage permission";

        Map<String, String> advancedReportingUserDeleteEntity = new HashMap<>(advancedReportingUserEntity);
        advancedReportingUserDeleteEntity.replace(DELETED_KEY, "true");
        advancedReportingUserDeleteEntity.replace(PERMISSION_ID_KEY,
                String.valueOf(RolePermissionConstants.NCENTRAL_REPORTS_ADVANCED_ALL));
        incomingEvent = Event.builder()
                .entityDataTypes(Collections.emptyMap())
                .bizappsCustomerId(BIZ_APPS_CUSTOMER_ID)
                .ncentralServerGuid(SERVER_GUID)
                .eventingConfigurationCustomerId(1)
                .professionalModeLicenseType(PER_DEVICE_LICENSE_TYPE)
                .eventType(EventType.DELETE)
                .entityType(ENTITY_TYPE)
                .entity(advancedReportingUserDeleteEntity)
                .newValues(Collections.emptyMap())
                .build();

        resourceGroup = AdvancedReportingUser.ResourceAccessGroup
                .newBuilder()
                .addAllId(Collections.emptyList())
                .setType(AccessGroupType.CLIENT)
                .build();
        expectedResult = AdvancedReportingUserOuterClass.AdvancedReportingUser
                .newBuilder()
                .setUserId(725275586)
                .setFirstName(FIRST_NAME_VALUE)
                .setLastName(LAST_NAME_VALUE)
                .setEmail(EMAIL_VALUE)
                .setEnabled(true)
                .setRoleType(
                        AdvancedReportingUserOuterClass.AdvancedReportingUser.RoleType.MANAGE)
                .addResourceAccessGroup(resourceGroup)
                .setDeleted(true)
                .setLastUpdated(Timestamp.newBuilder().setSeconds(1550757112).setNanos(68000000).build())
                .setAction(ActionOuterClass.Action.DELETE)
                .setContext(MspContextOuterClass.MspContext.newBuilder()
                        .setBizAppsCustomerId(BIZ_APPS_CUSTOMER_ID)
                        .setSystemGuid(SERVER_GUID)
                        .build())
                .build();

        testCases.setTestCase(incomingEvent, expectedResult, parsingAssertMessage);

        // <editor-fold desc="Delete Advanced Reporting User event with manage permission with take over user">
        parsingAssertMessage = "Delete Advanced Reporting User event with manage permission with take over user";

        Map<String, String> advancedReportingUserDeleteWithTakeOverUserEntity =
                new HashMap<>(advancedReportingUserEntity);
        advancedReportingUserDeleteWithTakeOverUserEntity.replace(DELETED_KEY, "true");
        advancedReportingUserDeleteWithTakeOverUserEntity.replace(PERMISSION_ID_KEY,
                String.valueOf(RolePermissionConstants.NCENTRAL_REPORTS_ADVANCED_ALL));
        advancedReportingUserDeleteWithTakeOverUserEntity.put(TAKE_OVER_USER_ID, TAKE_OVER_USER_ID_VALUE);
        advancedReportingUserDeleteWithTakeOverUserEntity.put(TAKE_OVER_USER_EMAIL, TAKE_OVER_USER_EMAIL_VALUE);
        advancedReportingUserDeleteWithTakeOverUserEntity.replace(FIRST_NAME_KEY, FIRST_NAME_DELETED_VALUE);
        advancedReportingUserDeleteWithTakeOverUserEntity.replace(EMAIL_KEY, EMAIL_DELETED_VALUE);

        incomingEvent = Event.builder()
                .entityDataTypes(Collections.emptyMap())
                .bizappsCustomerId(BIZ_APPS_CUSTOMER_ID)
                .ncentralServerGuid(SERVER_GUID)
                .eventingConfigurationCustomerId(1)
                .professionalModeLicenseType(PER_DEVICE_LICENSE_TYPE)
                .eventType(EventType.DELETE)
                .entityType(ENTITY_TYPE)
                .entity(advancedReportingUserDeleteWithTakeOverUserEntity)
                .newValues(Collections.emptyMap())
                .build();

        resourceGroup = AdvancedReportingUser.ResourceAccessGroup
                .newBuilder()
                .addAllId(Collections.emptyList())
                .setType(AccessGroupType.CLIENT)
                .build();
        expectedResult = AdvancedReportingUserOuterClass.AdvancedReportingUser
                .newBuilder()
                .setUserId(725275586)
                .setFirstName(FIRST_NAME_VALUE)
                .setLastName(LAST_NAME_VALUE)
                .setEmail(EMAIL_VALUE)
                .setEnabled(true)
                .setRoleType(
                        AdvancedReportingUserOuterClass.AdvancedReportingUser.RoleType.MANAGE)
                .addResourceAccessGroup(resourceGroup)
                .setDeleted(true)
                .setParentUserId(1234)
                .setParentUserEmail(TAKE_OVER_USER_EMAIL_VALUE)
                .setLastUpdated(Timestamp.newBuilder().setSeconds(1550757112).setNanos(68000000).build())
                .setAction(ActionOuterClass.Action.DELETE)
                .setContext(MspContextOuterClass.MspContext.newBuilder()
                        .setBizAppsCustomerId(BIZ_APPS_CUSTOMER_ID)
                        .setSystemGuid(SERVER_GUID)
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
