package com.solarwinds.msp.ncentral.eventproduction.api.entity;

import org.apache.commons.lang3.reflect.MethodUtils;
import org.jooq.lambda.tuple.Tuple4;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.lang.reflect.InvocationTargetException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EventTest {

    private static final String BIZAPPS_CUSTOMER_ID = "BIZAPPS_CUSTOMER_ID";
    private static final ZonedDateTime NOW = ZonedDateTime.now(ZoneId.of("UTC"));
    private static final String SERVER_GUID = "SERVER_GUID";
    private static final int CUSTOMER_ID = 1;
    private static final String LICENCE_MODE_TYPE = "LICENSE_MODE_TYPE";
    private static final EventType EVENT_TYPE = EventType.INSERT;
    private static final String ENTITY_NAME = "TEST_ENTITY";
    private static final String TABLE_NAME = "TABLE_NAME";
    private static final Map<String, String> ENTITY_DATA_TYPES = Collections.singletonMap("Field1", "int");
    private static final Map<String, String> ENTITY_DATA = Collections.singletonMap("Field1", "1");
    private static final Map<String, String> ENTITY_DATA_NEW = Collections.singletonMap("Field2", "2");

    private static final String SERVER_GUID2 = "SERVER_GUID2";
    private static final int CUSTOMER_ID2 = 2;
    private static final String LICENCE_MODE_TYPE2 = "LICENSE_MODE_TYPE2";
    private static final EventType EVENT_TYPE2 = EventType.UPDATE;
    private static final String ENTITY_NAME2 = "TEST_ENTITY2";
    private static final String TABLE_NAME2 = "TABLE_NAME2";
    private static final Map<String, String> ENTITY_DATA_TYPES2 = Collections.singletonMap("Field2", "int");
    private static final Map<String, String> ENTITY_DATA2 = Collections.singletonMap("Field2", "2");
    private static final Map<String, String> ENTITY_DATA_NEW2 = Collections.singletonMap("Field3", "3");
    private static Event testEvent;

    @BeforeEach
    void setUp() {
        testEvent = getFullyConfiguredEvent();
    }

    @ParameterizedTest(name = "Run {index}: {2}")
    @MethodSource
    public void checkProperty(Tuple4<Object, Boolean, String, String> incomingSetting, String assertMessage)
            throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Event.EventBuilder eventBuilder = Event.builder().copyFrom(testEvent);

        if (incomingSetting.v2) {
            MethodUtils.invokeMethod(eventBuilder, incomingSetting.v3(), incomingSetting.v1());
            assertThrows(NullPointerException.class, eventBuilder::build);
        } else {
            MethodUtils.invokeMethod(eventBuilder, incomingSetting.v3(), incomingSetting.v1());
            Event updatedEvent = eventBuilder.build();

            assertThat(MethodUtils.invokeMethod(updatedEvent, incomingSetting.v4())).isEqualTo(incomingSetting.v1());

        }
    }

    private static Stream<Arguments> checkProperty() {
        // <editor-fold desc="Mapped fields request">
        return Stream.of(Arguments.of(
                new Tuple4<Object, Boolean, String, String>(null, true, "ncentralServerGuid", "getNcentralServerGuid"),
                "Build event with missing server id."), Arguments.of(
                new Tuple4<Object, Boolean, String, String>(null, true, "eventingConfigurationCustomerId",
                        "getEventingConfigurationCustomerId"), "Build event with missing customer id."), Arguments.of(
                new Tuple4<Object, Boolean, String, String>(null, true, "professionalModeLicenseType",
                        "getProfessionalModeLicenseType"), "Build event with missing license mode type."),
                Arguments.of(new Tuple4<Object, Boolean, String, String>(null, true, "eventType", "getEventType"),
                        "Build event with missing event type."),
                Arguments.of(new Tuple4<Object, Boolean, String, String>(null, true, "entityType", "getEntityType"),
                        "Build event with missing table name."),
                Arguments.of(new Tuple4<Object, Boolean, String, String>(null, true, "entity", "getEntity"),
                        "Build event with missing entity data."),
                Arguments.of(new Tuple4<Object, Boolean, String, String>(null, true, "newValues", "getNewValues"),
                        "Build event with missing new entity data."), Arguments.of(
                        new Tuple4<Object, Boolean, String, String>(null, true, "entityDataTypes",
                                "getEntityDataTypes"), "Build event with missing entity data types."),

                Arguments.of(new Tuple4<Object, Boolean, String, String>(SERVER_GUID2, false, "ncentralServerGuid",
                        "getNcentralServerGuid"), "Build event with new server id."), Arguments.of(
                        new Tuple4<Object, Boolean, String, String>(CUSTOMER_ID2, false,
                                "eventingConfigurationCustomerId", "getEventingConfigurationCustomerId"),
                        "Build event with new customer id."), Arguments.of(
                        new Tuple4<Object, Boolean, String, String>(LICENCE_MODE_TYPE2, false,
                                "professionalModeLicenseType", "getProfessionalModeLicenseType"),
                        "Build event with new license mode type."), Arguments.of(
                        new Tuple4<Object, Boolean, String, String>(EVENT_TYPE2, false, "eventType", "getEventType"),
                        "Build event with new event type."), Arguments.of(
                        new Tuple4<Object, Boolean, String, String>(TABLE_NAME2, false, "entityType", "getEntityType"),
                        "Build event with new table name."),
                Arguments.of(new Tuple4<Object, Boolean, String, String>(ENTITY_DATA2, false, "entity", "getEntity"),
                        "Build event with new entity data."), Arguments.of(
                        new Tuple4<Object, Boolean, String, String>(ENTITY_DATA_NEW2, false, "newValues",
                                "getNewValues"), "Build event with new new entity data."), Arguments.of(
                        new Tuple4<Object, Boolean, String, String>(ENTITY_DATA_TYPES2, false, "entityDataTypes",
                                "getEntityDataTypes"), "Build event with new entity data types."));
        // </editor-fold>
    }

    @Test
    void eventBuilder_copyFrom() {
        Event copiedEvent = Event.builder().copyFrom(testEvent).build();
        assertEquals(testEvent.hashCode(), copiedEvent.hashCode());
    }

    @Test
    void getNcentralServerGuid() {
        assertEquals(SERVER_GUID, testEvent.getNcentralServerGuid());
    }

    @Test
    void getBizappsCustomerId() {
        assertEquals(Optional.of(BIZAPPS_CUSTOMER_ID), testEvent.getBizappsCustomerId());
    }

    @Test
    void getEventingConfigurationCustomerId() {
        assertEquals(CUSTOMER_ID, testEvent.getEventingConfigurationCustomerId());
    }

    @Test
    void getProfessionalModeLicenseType() {
        assertEquals(LICENCE_MODE_TYPE, testEvent.getProfessionalModeLicenseType());
    }

    @Test
    void getEventType() {
        assertEquals(EVENT_TYPE, testEvent.getEventType());
    }

    @Test
    void getEntityType() {
        assertEquals(TABLE_NAME, testEvent.getEntityType());
    }

    @Test
    void getEntity() {
        assertEquals(ENTITY_DATA, testEvent.getEntity());
    }

    @Test
    void getNewValues() {
        assertEquals(ENTITY_DATA_NEW, testEvent.getNewValues());
    }

    @Test
    void getEntityDataTypes() {
        assertEquals(ENTITY_DATA_TYPES, testEvent.getEntityDataTypes());
    }

    @Test
    void isDirectSend() {
        assertFalse(testEvent.isDirectSend());
    }

    @Test
    void isExportEnabled() {
        assertTrue(testEvent.isExportEnabled());
    }

    private Event getFullyConfiguredEvent() {
        return Event.builder()
                .entityDataTypes(ENTITY_DATA_TYPES)
                .bizappsCustomerId(BIZAPPS_CUSTOMER_ID)
                .ncentralServerGuid(SERVER_GUID)
                .eventingConfigurationCustomerId(CUSTOMER_ID)
                .professionalModeLicenseType(LICENCE_MODE_TYPE)
                .eventType(EVENT_TYPE)
                .entityType(TABLE_NAME)
                .entity(ENTITY_DATA)
                .newValues(ENTITY_DATA_NEW)
                .directSend(false)
                .exportEnabled(true)
                .build();
    }
}