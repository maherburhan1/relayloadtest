package com.solarwinds.msp.ncentral.eventproduction.converter.impl.entity;

import com.google.protobuf.GeneratedMessageV3;
import com.google.protobuf.Timestamp;

import com.solarwinds.msp.ncentral.eventproduction.api.entity.Event;
import com.solarwinds.msp.ncentral.eventproduction.api.entity.EventType;
import com.solarwinds.msp.ncentral.eventproduction.converter.EventParser;
import com.solarwinds.msp.ncentral.proto.entity.ActionOuterClass;
import com.solarwinds.msp.ncentral.proto.entity.MspContextOuterClass;
import com.solarwinds.msp.ncentral.proto.entity.entity.AddressOuterClass;
import com.solarwinds.msp.ncentral.proto.entity.entity.ContactOuterClass;
import com.solarwinds.msp.ncentral.proto.entity.entity.UsersOuterClass;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

class UsersEventTest {
    private static Stream<Arguments> parseRecordTest() {
        EventParserTestCases testCases = new EventParserTestCases();
        UsersOuterClass.Users expectedResult;
        Event incomingEvent;
        String parsingAssertMessage;

        Map<String, String> entity = new HashMap<>();
        entity.put("lastupdated", "2019-02-21T13:51:52.068Z");

        //<editor-fold desc="Valid users event">
        parsingAssertMessage = "Valid users event";
        Map<String, String> entity1 = new HashMap<>(entity);
        entity1.put("userid", "1813129454");
        entity1.put("customerid", "261");
        entity1.put("username", "servicetech@theservicegroup.com");
        entity1.put("passwordmd5 ", "");

        entity1.put("email1", "Ima.Testingstuff@mail.com");
        entity1.put("department", "Test Department");
        entity1.put("phone1", "555-444-3333");
        entity1.put("extension1", null);
        entity1.put("firstname", "Ima");
        entity1.put("lastname", "Testingstuff");
        entity1.put("title", "Mr.");

        entity1.put("street1", "235 street st");
        entity1.put("street2", "u7");
        entity1.put("country", "CA");
        entity1.put("stateprov", "ontario");
        entity1.put("city", "kanata");
        entity1.put("postalcode", "k7k6k4");

        entity1.put("isenabled", "f");
        entity1.put("primaryaccount", "SO User");
        entity1.put("autoso_admin", "f");
        entity1.put("autoso_user", "f");
        entity1.put("deleted", "f");
        entity1.put("isldap", "f");
        entity1.put("lastsynctoldap", "2019-04-16T11:25:42.298-04:00");
        entity1.put("islocked", "f");
        entity1.put("usetwofactorauthusername", "f");
        entity1.put("twofactorauthusername ", "");
        entity1.put("ldap_uuid", "");
        entity1.put("password", "4d2c1d859abd85a41s4f4ffcf95242f37cced8ccda2531b8f3715f7762a3256dc");
        entity1.put("twofactortwostepverificationsecretkey ",
                "#ENCR2#1:snc#5fFURAT3ZVU641kt9rQhs0pVnm8uGIv94LOql20/70dSEDKPw3EtJn7pDdkd");
        entity1.put("twofactortype", "TwoStepVerification");
        entity1.put("description", "Service Tech for ErdemCo");

        incomingEvent = Event.builder()
                .entityDataTypes(Collections.EMPTY_MAP)
                .bizappsCustomerId("aaea6111-b13f-462b-9385-4f3baa7f0ccc")
                .ncentralServerGuid("225ed5bb-9e8e-4ad5-aa8c-7a018dc5ffa5")
                .eventingConfigurationCustomerId(1)
                .professionalModeLicenseType("Per Device")
                .eventType(EventType.INSERT)
                .entityType("luser")
                .entity(entity1)
                .newValues(Collections.emptyMap())
                .build();

        expectedResult = UsersOuterClass.Users.newBuilder()
                .setUserId(1813129454)
                .setUserName("servicetech@theservicegroup.com")
                .setClientId(261)
                .setDescription("Service Tech for ErdemCo")
                .setEnabled(false)
                .setDeleted(false)
                .addAddress(AddressOuterClass.Address.newBuilder()
                        .setAddress1("235 street st")
                        .setAddress2("u7")
                        .setCity("kanata")
                        .setStateProvince("ontario")
                        .setPostalCode("k7k6k4")
                        .setCountry("CA")
                        .build())
                .addContact(ContactOuterClass.Contact.newBuilder()
                        .setTitle("Mr.")
                        .setPhoneNumber("555-444-3333")
                        .setLastName("Testingstuff")
                        .setFirstName("Ima")
                        .setEmail("Ima.Testingstuff@mail.com")
                        .setDepartment("Test Department")
                        .setIsPrimary(true)
                        .setFullName("Ima Testingstuff")
                        .build())

                .setLastUpdated(Timestamp.newBuilder().setSeconds(1550757112).setNanos(68000000).build())
                .setAction(ActionOuterClass.Action.ADD)
                .setContext(MspContextOuterClass.MspContext.newBuilder()
                        .setBizAppsCustomerId("aaea6111-b13f-462b-9385-4f3baa7f0ccc")
                        .setSystemGuid("225ed5bb-9e8e-4ad5-aa8c-7a018dc5ffa5")
                        .build())
                .build();

        testCases.setTestCase(incomingEvent, expectedResult, parsingAssertMessage);
        //</editor-fold>

        //<editor-fold desc="Users event missing customer id">
        parsingAssertMessage = "Users event missing customer id";
        Map<String, String> entity2 = new HashMap<>(entity1);
        entity2.remove("customerid");
        incomingEvent = Event.builder()
                .entityDataTypes(Collections.EMPTY_MAP)
                .bizappsCustomerId("aaea6111-b13f-462b-9385-4f3baa7f0ccc")
                .ncentralServerGuid("225ed5bb-9e8e-4ad5-aa8c-7a018dc5ffa5")
                .eventingConfigurationCustomerId(1)
                .professionalModeLicenseType("Per Device")
                .eventType(EventType.INSERT)
                .entityType("luser")
                .entity(entity2)
                .newValues(Collections.emptyMap())
                .build();

        testCases.setTestCase(incomingEvent, parsingAssertMessage);
        //</editor-fold>

        //<editor-fold desc="Users event missing username">
        parsingAssertMessage = "Users event missing username";
        Map<String, String> entity3 = new HashMap<>(entity1);
        entity3.remove("username");
        incomingEvent = Event.builder()
                .entityDataTypes(Collections.EMPTY_MAP)
                .bizappsCustomerId("aaea6111-b13f-462b-9385-4f3baa7f0ccc")
                .ncentralServerGuid("225ed5bb-9e8e-4ad5-aa8c-7a018dc5ffa5")
                .eventingConfigurationCustomerId(1)
                .professionalModeLicenseType("Per Device")
                .eventType(EventType.INSERT)
                .entityType("luser")
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