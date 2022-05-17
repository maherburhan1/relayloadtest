package com.solarwinds.msp.ncentral.eventproduction.converter.impl.entity;

import com.google.protobuf.GeneratedMessageV3;
import com.google.protobuf.Timestamp;

import com.solarwinds.msp.ncentral.eventproduction.api.entity.Event;
import com.solarwinds.msp.ncentral.eventproduction.api.entity.EventType;
import com.solarwinds.msp.ncentral.eventproduction.converter.EventParser;
import com.solarwinds.msp.ncentral.proto.entity.ActionOuterClass;
import com.solarwinds.msp.ncentral.proto.entity.MspContextOuterClass;
import com.solarwinds.msp.ncentral.proto.entity.asset.ApplicationStatusOuterClass;
import com.solarwinds.msp.ncentral.proto.entity.entity.ApplicationOuterClass;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

class ApplicationStatusEventTest {

    private static final String BIZAPPS_CUSTOMER_ID = "aaea6111-b13f-462b-9385-4f3baa7f0ccc";
    private static final boolean DELETED = true;
    private static final Integer DEVICE_ID = 12345;
    private static final String DISPLAY_NAME = "Application display name";
    private static final String LICENSE_KEY = "Application license key";
    private static final String LICENSE_TYPE = "Application license type";
    private static final String NCENTRAL_SERVER_GUID = "225ed5bb-9e8e-4ad5-aa8c-7a018dc5ffa5";
    private static final String PRODUCT_ID = "Application product id";
    private static final String PUBLISHER = "Application publisher name";
    private static final String VERSION = "Application version name";
    private static final Integer EVENTING_CONFIG_CUSTOMER_ID = 1;

    private static Stream<Arguments> parseRecordTest() {
        EventParserTestCases testCases = new EventParserTestCases();
        final Map<String, String> validEntity = new HashMap<>();
        validEntity.put("deviceid", String.valueOf(DEVICE_ID));
        validEntity.put("deleted", String.valueOf(DELETED));
        validEntity.put("displayname", DISPLAY_NAME);
        validEntity.put("installationdate", "2019-02-21T13:51:52.068Z");
        validEntity.put("lastupdated", "2019-02-21T13:51:53.068Z");
        validEntity.put("licensekey", LICENSE_KEY);
        validEntity.put("licensetype", LICENSE_TYPE);
        validEntity.put("productid", PRODUCT_ID);
        validEntity.put("publisher", PUBLISHER);
        validEntity.put("version", VERSION);

        //<editor-fold desc="Valid ApplicationStatus event">
        {
            String parsingAssertMessage = "Valid ApplicationStatus event";
            Map<String, String> entity = new HashMap<>(validEntity);

            ApplicationStatusOuterClass.ApplicationStatus expectedResult = ApplicationStatusOuterClass.ApplicationStatus
                    .newBuilder()
                    .setAction(ActionOuterClass.Action.ADD)
                    .setContext(MspContextOuterClass.MspContext.newBuilder()
                            .setBizAppsCustomerId(BIZAPPS_CUSTOMER_ID)
                            .setSystemGuid(NCENTRAL_SERVER_GUID)
                            .build())
                    .setDeleted(DELETED)
                    .setDeviceId(DEVICE_ID)
                    .setInstallationDate(Timestamp.newBuilder().setSeconds(1550757112).setNanos(68000000).build())
                    .setApplication(ApplicationOuterClass.Application.newBuilder()
                            .setName(DISPLAY_NAME)
                            .setPublisher(PUBLISHER)
                            .setVersion(VERSION)
                            .build())
                    .setLastUpdated(Timestamp.newBuilder().setSeconds(1550757113).setNanos(68000000).build())
                    .setLicenseKey(LICENSE_KEY)
                    .setLicenseType(LICENSE_TYPE)
                    .setProductId(PRODUCT_ID)
                    .build();
            testCases.setTestCase(createEventForInsert(entity), expectedResult, parsingAssertMessage);
        }
        //</editor-fold>

        //<editor-fold desc="ApplicationStatus event missing device id">
        {
            String parsingAssertMessage = "ApplicationStatus event missing device id";
            Map<String, String> entity = new HashMap<>(validEntity);
            entity.remove("deviceid");

            testCases.setTestCase(createEventForInsert(entity), parsingAssertMessage);
        }
        //</editor-fold>

        //<editor-fold desc="ApplicationStatus update event without changed data">
        {
            String parsingAssertMessage = "ApplicationStatus update event without changed data";
            Map<String, String> entity = new HashMap<>(validEntity);

            testCases.setTestCase(createEvent(entity, Collections.emptyMap(), EventType.UPDATE), parsingAssertMessage);
        }
        //</editor-fold>

        return testCases.toArguments();
    }

    private static Event createEventForInsert(Map<String, String> entity) {
        return createEvent(entity, entity, EventType.INSERT);
    }

    private static Event createEvent(Map<String, String> entity, Map<String, String> newValues, EventType eventType) {
        return Event.builder()
                .entityDataTypes(Collections.EMPTY_MAP)
                .bizappsCustomerId(BIZAPPS_CUSTOMER_ID)
                .ncentralServerGuid(NCENTRAL_SERVER_GUID)
                .eventingConfigurationCustomerId(EVENTING_CONFIG_CUSTOMER_ID)
                .professionalModeLicenseType("Per Device")
                .eventType(eventType)
                .entityType("cim_application")
                .entity(entity)
                .newValues(newValues)
                .build();
    }

    @ParameterizedTest(name = "Run {index}: {3}")
    @MethodSource
    void parseRecordTest(EventParser<GeneratedMessageV3> eventParser, Event incomingEvent,
            List<com.google.protobuf.GeneratedMessageV3> expectedResult, String assertMessage) {
        List<com.google.protobuf.GeneratedMessageV3> result = eventParser.parse(incomingEvent);
        result.removeIf(message -> message.getClass().equals(ApplicationOuterClass.Application.class));

        Assertions.assertThat(result).as(assertMessage).isEqualTo(expectedResult);
    }
}