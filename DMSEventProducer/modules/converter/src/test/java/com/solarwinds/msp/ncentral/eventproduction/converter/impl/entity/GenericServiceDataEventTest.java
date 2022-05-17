package com.solarwinds.msp.ncentral.eventproduction.converter.impl.entity;

import com.google.protobuf.GeneratedMessageV3;
import com.google.protobuf.Timestamp;

import com.solarwinds.msp.ncentral.eventproduction.api.entity.Event;
import com.solarwinds.msp.ncentral.eventproduction.api.entity.EventType;
import com.solarwinds.msp.ncentral.eventproduction.converter.EventParser;
import com.solarwinds.msp.ncentral.proto.entity.ActionOuterClass;
import com.solarwinds.msp.ncentral.proto.entity.MspContextOuterClass;
import com.solarwinds.msp.ncentral.proto.entity.tasks.GenericServiceDataOuterClass;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static com.solarwinds.msp.ncentral.eventproduction.converter.ServiceProcessingRules.PROCESS_WITH_GENERIC_SERVICE_DATA_ONLY;
import static org.assertj.core.api.Assertions.assertThat;

class GenericServiceDataEventTest {

    private static Stream<Arguments> parseRecordTest() {
        EventParserTestCases testCases = new EventParserTestCases();
        GenericServiceDataOuterClass.GenericServiceData expectedResult;
        Event incomingEvent;
        String parsingAssertMessage;

        Map<String, String> entity = new HashMap<>();
        entity.put("lastupdated", "2019-02-21T13:51:52.068Z");
        entity.put("scantime", "2019-02-21T13:51:52.068Z");

        Map<String, String> entityDataTypes = new HashMap<>();
        entityDataTypes.put("taskid", "INTEGER");
        entityDataTypes.put("scantime", "TIMESTAMP");
        entityDataTypes.put("datadelay", "SMALLINT");
        entityDataTypes.put("errormessage", "VARCHAR");
        entityDataTypes.put("state", "TINYINT");

        //<editor-fold desc="Valid custom service event (datadns_detailed)">
        parsingAssertMessage = "Valid custom service event (datadns_detailed)";
        Map<String, String> entity1 = new HashMap<>(entity);
        entity1.put("taskid", "887366936");
        entity1.put("datadelay", "5");
        entity1.put("errormessage", "");
        entity1.put("state", "5");
        entity1.put("dnsa", "f");
        entity1.put("dns_response", "0");
        entity1.put("dnsr", "t");
        entity1.put("foo01_tinyint", Integer.toString(Integer.MAX_VALUE - 1));
        entity1.put("foo02_smallint", Integer.toString(Integer.MAX_VALUE - 2));
        entity1.put("foo03_bigint", Long.toString(Long.MAX_VALUE - 1));
        entity1.put("foo04_decimal", "123.456");
        entity1.put("foo05_real", "123.4567");
        entity1.put("foo06_float", "123.45678");
        entity1.put("foo07_double", Double.toString(Double.MAX_VALUE - 1.0D));
        entity1.put("foo08_date", "2019-02-21T13:51:51.068Z");
        entity1.put("foo09_timestamp", "2019-02-21T13:51:50.068Z");
        entity1.put("foo10_string", "foo string value");

        Map<String, String> entityDataTypes1 = new HashMap<>(entityDataTypes);
        entityDataTypes1.put("dnsa", "BIT");
        entityDataTypes1.put("dns_response", "INTEGER");
        entityDataTypes1.put("dnsr", "BIT");
        entityDataTypes1.put("foo01_tinyint", "TINYINT");
        entityDataTypes1.put("foo02_smallint", "SMALLINT");
        entityDataTypes1.put("foo03_bigint", "BIGINT");
        entityDataTypes1.put("foo04_decimal", "DECIMAL");
        entityDataTypes1.put("foo05_real", "REAL");
        entityDataTypes1.put("foo06_float", "FLOAT");
        entityDataTypes1.put("foo07_double", "DOUBLE");
        entityDataTypes1.put("foo08_date", "DATE");
        entityDataTypes1.put("foo09_timestamp", "TIMESTAMP");
        entityDataTypes1.put("foo10_string", "VARCHAR");

        incomingEvent = Event.builder()
                .entityDataTypes(entityDataTypes1)
                .bizappsCustomerId("aaea6111-b13f-462b-9385-4f3baa7f0ccc")
                .ncentralServerGuid("225ed5bb-9e8e-4ad5-aa8c-7a018dc5ffa5")
                .eventingConfigurationCustomerId(1)
                .professionalModeLicenseType("Per Device")
                .eventType(EventType.INSERT)
                .entityType("datadns_detailed")
                .entity(entity1)
                .newValues(Collections.emptyMap())
                .build();

        expectedResult = GenericServiceDataOuterClass.GenericServiceData.newBuilder()
                .setTaskId(887366936)
                .setSourceName("datadns_detailed")
                .addGenericField(GenericServiceDataOuterClass.GenericServiceData.genericTypedData.newBuilder()
                        .setFieldName("dns_response")
                        .setFieldType("Integer")
                        .setIntegerField(0)
                        .build())
                .addGenericField(GenericServiceDataOuterClass.GenericServiceData.genericTypedData.newBuilder()
                        .setFieldName("dnsa")
                        .setFieldType("Boolean")
                        .setBooleanField(false)
                        .build())
                .addGenericField(GenericServiceDataOuterClass.GenericServiceData.genericTypedData.newBuilder()
                        .setFieldName("dnsr")
                        .setFieldType("Boolean")
                        .setBooleanField(true)
                        .build())
                .addGenericField(GenericServiceDataOuterClass.GenericServiceData.genericTypedData.newBuilder()
                        .setFieldName("foo01_tinyint")
                        .setFieldType("Integer")
                        .setIntegerField(Integer.MAX_VALUE - 1)
                        .build())
                .addGenericField(GenericServiceDataOuterClass.GenericServiceData.genericTypedData.newBuilder()
                        .setFieldName("foo02_smallint")
                        .setFieldType("Integer")
                        .setIntegerField(Integer.MAX_VALUE - 2)
                        .build())
                .addGenericField(GenericServiceDataOuterClass.GenericServiceData.genericTypedData.newBuilder()
                        .setFieldName("foo03_bigint")
                        .setFieldType("Long")
                        .setLongField(Long.MAX_VALUE - 1)
                        .build())
                .addGenericField(GenericServiceDataOuterClass.GenericServiceData.genericTypedData.newBuilder()
                        .setFieldName("foo04_decimal")
                        .setFieldType("Float")
                        .setFloatField(123.456F)
                        .build())
                .addGenericField(GenericServiceDataOuterClass.GenericServiceData.genericTypedData.newBuilder()
                        .setFieldName("foo05_real")
                        .setFieldType("Float")
                        .setFloatField(123.4567F)
                        .build())
                .addGenericField(GenericServiceDataOuterClass.GenericServiceData.genericTypedData.newBuilder()
                        .setFieldName("foo06_float")
                        .setFieldType("Float")
                        .setFloatField(123.45678F)
                        .build())
                .addGenericField(GenericServiceDataOuterClass.GenericServiceData.genericTypedData.newBuilder()
                        .setFieldName("foo07_double")
                        .setFieldType("Double")
                        .setDoubleField(Double.MAX_VALUE - 1.0D)
                        .build())
                .addGenericField(GenericServiceDataOuterClass.GenericServiceData.genericTypedData.newBuilder()
                        .setFieldName("foo08_date")
                        .setFieldType("Timestamp")
                        .setTimestampField(Timestamp.newBuilder().setSeconds(1550757112 - 1).setNanos(68000000).build())
                        .build())
                .addGenericField(GenericServiceDataOuterClass.GenericServiceData.genericTypedData.newBuilder()
                        .setFieldName("foo09_timestamp")
                        .setFieldType("Timestamp")
                        .setTimestampField(Timestamp.newBuilder().setSeconds(1550757112 - 2).setNanos(68000000).build())
                        .build())
                .addGenericField(GenericServiceDataOuterClass.GenericServiceData.genericTypedData.newBuilder()
                        .setFieldName("foo10_string")
                        .setFieldType("String")
                        .setStringField("foo string value")
                        .build())
                .setScanTime(Timestamp.newBuilder().setSeconds(1550757112).setNanos(68000000).build())
                .setStateId(5)
                .setLastUpdated(Timestamp.newBuilder().setSeconds(1550757112).setNanos(68000000).build())
                .setAction(ActionOuterClass.Action.ADD)
                .setContext(MspContextOuterClass.MspContext.newBuilder()
                        .setBizAppsCustomerId("aaea6111-b13f-462b-9385-4f3baa7f0ccc")
                        .setSystemGuid("225ed5bb-9e8e-4ad5-aa8c-7a018dc5ffa5")
                        .build())
                .build();

        testCases.setTestCase(incomingEvent, expectedResult, parsingAssertMessage);
        //</editor-fold>

        //<editor-fold desc="Custom service scan detail missing taskid">
        parsingAssertMessage = "Custom service scan detail missing taskid";
        Map<String, String> entity2 = new HashMap<>(entity1);
        entity2.remove("taskid");
        incomingEvent = Event.builder()
                .entityDataTypes(Collections.EMPTY_MAP)
                .bizappsCustomerId("aaea6111-b13f-462b-9385-4f3baa7f0ccc")
                .ncentralServerGuid("225ed5bb-9e8e-4ad5-aa8c-7a018dc5ffa5")
                .eventingConfigurationCustomerId(1)
                .professionalModeLicenseType("Per Device")
                .eventType(EventType.INSERT)
                .entityType("datadns_detailed")
                .entity(entity2)
                .newValues(Collections.emptyMap())
                .build();

        testCases.setTestCase(incomingEvent, parsingAssertMessage);
        //</editor-fold>

        //<editor-fold desc="Valid custom service event (datadhcp_detailed)">
        parsingAssertMessage = "Valid custom service event (datadhcp_detailed)";
        Map<String, String> entity3 = new HashMap<>(entity);
        entity3.put("taskid", "2136530529");
        entity3.put("datadelay", "20");
        entity3.put("errormessage", "");
        entity3.put("state", "3");
        entity3.put("activequeuelength", "0");

        Map<String, String> entityDataTypes2 = new HashMap<>(entityDataTypes);
        entityDataTypes2.put("activequeuelength", "INTEGER");

        incomingEvent = Event.builder()
                .entityDataTypes(entityDataTypes2)
                .bizappsCustomerId("aaea6111-b13f-462b-9385-4f3baa7f0ccc")
                .ncentralServerGuid("225ed5bb-9e8e-4ad5-aa8c-7a018dc5ffa5")
                .eventingConfigurationCustomerId(1)
                .professionalModeLicenseType("Per Device")
                .eventType(EventType.INSERT)
                .entityType("datadhcp_detailed")
                .entity(entity3)
                .newValues(Collections.emptyMap())
                .build();

        expectedResult = GenericServiceDataOuterClass.GenericServiceData.newBuilder()
                .setTaskId(2136530529)
                .setSourceName("datadhcp_detailed")
                .addGenericField(GenericServiceDataOuterClass.GenericServiceData.genericTypedData.newBuilder()
                        .setFieldName("activequeuelength")
                        .setFieldType("Integer")
                        .setIntegerField(0)
                        .build())
                .setScanTime(Timestamp.newBuilder().setSeconds(1550757112).setNanos(68000000).build())
                .setStateId(3)
                .setLastUpdated(Timestamp.newBuilder().setSeconds(1550757112).setNanos(68000000).build())
                .setAction(ActionOuterClass.Action.ADD)
                .setContext(MspContextOuterClass.MspContext.newBuilder()
                        .setBizAppsCustomerId("aaea6111-b13f-462b-9385-4f3baa7f0ccc")
                        .setSystemGuid("225ed5bb-9e8e-4ad5-aa8c-7a018dc5ffa5")
                        .build())
                .build();

        testCases.setTestCase(incomingEvent, expectedResult, parsingAssertMessage);
        //</editor-fold>

        //<editor-fold desc="Valid custom service event (dataprinterpgcnt_detailed)">
        parsingAssertMessage = "Valid custom service event (dataprinterpgcnt_detailed)";
        Map<String, String> entity4 = new HashMap<>(entity);
        entity4.put("taskid", "2030277180");
        entity4.put("datadelay", "21");
        entity4.put("errormessage", "");
        entity4.put("state", "3");
        entity4.put("snmp20650_pgcount", "4665");
        entity4.put("snmp20650_pwcount", "4");

        Map<String, String> entityDataTypes3 = new HashMap<>(entityDataTypes);
        entityDataTypes3.put("snmp20650_pgcount", "INTEGER");
        entityDataTypes3.put("snmp20650_pwcount", "INTEGER");

        incomingEvent = Event.builder()
                .entityDataTypes(entityDataTypes3)
                .bizappsCustomerId("aaea6111-b13f-462b-9385-4f3baa7f0ccc")
                .ncentralServerGuid("225ed5bb-9e8e-4ad5-aa8c-7a018dc5ffa5")
                .eventingConfigurationCustomerId(1)
                .professionalModeLicenseType("Per Device")
                .eventType(EventType.INSERT)
                .entityType("dataprinterpgcnt_detailed")
                .entity(entity4)
                .newValues(Collections.emptyMap())
                .build();

        expectedResult = GenericServiceDataOuterClass.GenericServiceData.newBuilder()
                .setTaskId(2030277180)
                .setSourceName("dataprinterpgcnt_detailed")
                .addGenericField(GenericServiceDataOuterClass.GenericServiceData.genericTypedData.newBuilder()
                        .setFieldName("snmp20650_pgcount")
                        .setFieldType("Integer")
                        .setIntegerField(4665)
                        .build())
                .addGenericField(GenericServiceDataOuterClass.GenericServiceData.genericTypedData.newBuilder()
                        .setFieldName("snmp20650_pwcount")
                        .setFieldType("Integer")
                        .setIntegerField(4)
                        .build())
                .setScanTime(Timestamp.newBuilder().setSeconds(1550757112).setNanos(68000000).build())
                .setStateId(3)
                .setLastUpdated(Timestamp.newBuilder().setSeconds(1550757112).setNanos(68000000).build())
                .setAction(ActionOuterClass.Action.ADD)
                .setContext(MspContextOuterClass.MspContext.newBuilder()
                        .setBizAppsCustomerId("aaea6111-b13f-462b-9385-4f3baa7f0ccc")
                        .setSystemGuid("225ed5bb-9e8e-4ad5-aa8c-7a018dc5ffa5")
                        .build())
                .build();

        testCases.setTestCase(incomingEvent, expectedResult, parsingAssertMessage);
        //</editor-fold>

        return testCases.toArguments();
    }

    @ParameterizedTest(name = "Run {index}: {3}")
    @MethodSource
    void parseRecordTest(EventParser<GeneratedMessageV3> eventParser, Event incomingEvent,
            List<com.google.protobuf.GeneratedMessageV3> expectedResult, String assertMessage) {
        assertThat(eventParser.parse(incomingEvent, PROCESS_WITH_GENERIC_SERVICE_DATA_ONLY)).isEqualTo(expectedResult)
                .withFailMessage(assertMessage);
    }
}