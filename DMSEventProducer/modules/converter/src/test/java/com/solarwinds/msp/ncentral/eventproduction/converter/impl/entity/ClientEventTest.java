package com.solarwinds.msp.ncentral.eventproduction.converter.impl.entity;

import com.google.protobuf.GeneratedMessageV3;
import com.google.protobuf.Timestamp;

import com.solarwinds.msp.ncentral.eventproduction.api.entity.Event;
import com.solarwinds.msp.ncentral.eventproduction.api.entity.EventType;
import com.solarwinds.msp.ncentral.eventproduction.converter.EventParser;
import com.solarwinds.msp.ncentral.proto.entity.ActionOuterClass;
import com.solarwinds.msp.ncentral.proto.entity.MspContextOuterClass;
import com.solarwinds.msp.ncentral.proto.entity.entity.AddressOuterClass;
import com.solarwinds.msp.ncentral.proto.entity.entity.ClientOuterClass;
import com.solarwinds.msp.ncentral.proto.entity.entity.ContactOuterClass;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

class ClientEventTest {
    static Stream<Arguments> parseRecordTest() {
        EventParserTestCases testCases = new EventParserTestCases();
        ClientOuterClass.Client expectedResult;
        Event incomingEvent;
        String parsingAssertMessage;

        //<editor-fold desc="Valid client event">
        parsingAssertMessage = "Valid client event";
        Map<String, String> custentity = new HashMap<>();

        custentity.put("contactemail", "Ima.Testingstuff@mail.com");
        custentity.put("contactdepartment", "Test Department");
        custentity.put("contactphonenumber", "555-444-3333");
        custentity.put("contactext", null);
        custentity.put("contactfirstname", "Ima");
        custentity.put("contactlastname", "Testingstuff");
        custentity.put("phone", "555-444-3333");
        custentity.put("contacttitle", "Mr.");

        custentity.put("street1", "235 street st");
        custentity.put("street2", "u7");
        custentity.put("country", "CA");
        custentity.put("stateprov", "ontario");
        custentity.put("city", "kanata");
        custentity.put("postalcode", "k7k6k4");

        custentity.put("externalid", "8887868");
        custentity.put("externalid2", "85858");
        custentity.put("maintenancewindowstart", null);
        custentity.put("parentid", "1");
        custentity.put("customername", "Another So");
        custentity.put("psacustomername", "Another So");
        custentity.put("notificationsenabled", "true");
        custentity.put("maintenancewindowduration", "0");
        custentity.put("isserviceorg", "true");
        custentity.put("deleted", "false");
        custentity.put("customerid", "103");
        custentity.put("lastupdated", "2019-02-21T13:51:52.068Z");
        custentity.put("creationtime", "2019-02-21T13:51:52.068Z");
        custentity.put("issystem", "false");
        custentity.put("ondemandallowwarrantyexport", "false");

        incomingEvent = Event.builder()
                .entityDataTypes(Collections.EMPTY_MAP)
                .bizappsCustomerId("aaea6111-b13f-462b-9385-4f3baa7f0ccc")
                .ncentralServerGuid("225ed5bb-9e8e-4ad5-aa8c-7a018dc5ffa5")
                .eventingConfigurationCustomerId(1)
                .professionalModeLicenseType("Per Device")
                .eventType(EventType.INSERT)
                .entityType("customer")
                .entity(custentity)
                .newValues(Collections.emptyMap())
                .build();

        expectedResult = ClientOuterClass.Client.newBuilder()
                .setClientId(103)
                .setParentId(1)
                .setName("Another So")
                .setClientType(ClientOuterClass.Client.ClientType.VAR)
                .setCreated(Timestamp.newBuilder().setSeconds(1550757112).setNanos(68000000).build())
                .setLastUpdated(Timestamp.newBuilder().setSeconds(1550757112).setNanos(68000000).build())
                .addAddress(AddressOuterClass.Address.newBuilder()
                        .setAddress1("235 street st")
                        .setAddress2("u7")
                        .setCity("kanata")
                        .setPostalCode("k7k6k4")
                        .setStateProvince("ontario")
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
                .setAction(ActionOuterClass.Action.ADD)
                .setContext(MspContextOuterClass.MspContext.newBuilder()
                        .setBizAppsCustomerId("aaea6111-b13f-462b-9385-4f3baa7f0ccc")
                        .setSystemGuid("225ed5bb-9e8e-4ad5-aa8c-7a018dc5ffa5")
                        .build())
                .build();

        testCases.setTestCase(incomingEvent, expectedResult, parsingAssertMessage);
        //</editor-fold>

        //<editor-fold desc="Client missing customerid event">
        parsingAssertMessage = "Client missing customerid event";
        custentity = new HashMap<>();

        incomingEvent = Event.builder()
                .entityDataTypes(Collections.EMPTY_MAP)
                .bizappsCustomerId("aaea6111-b13f-462b-9385-4f3baa7f0ccc")
                .ncentralServerGuid("225ed5bb-9e8e-4ad5-aa8c-7a018dc5ffa5")
                .eventingConfigurationCustomerId(1)
                .professionalModeLicenseType("Per Device")
                .eventType(EventType.INSERT)
                .entityType("customer")
                .entity(custentity)
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