package com.solarwinds.msp.ncentral.eventproduction.converter.configuration.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.solarwinds.msp.ncentral.eventproduction.converter.TestCases;
import com.solarwinds.msp.ncentral.eventproduction.converter.configuration.EventConversionConfigurationService;
import com.solarwinds.msp.ncentral.eventproduction.converter.configuration.entity.Conversions;
import com.solarwinds.msp.ncentral.eventproduction.converter.configuration.provider.EventConversionConfigurationComponentConfiguration;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.collections.map.SingletonMap;
import org.jooq.lambda.tuple.Tuple2;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.core.io.DefaultResourceLoader;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

public class EventConversionConfigurationServiceImplTest {

    private static final EventConversionConfigurationService eventConversionConfigurationService =
            createEventConfigurationService();

    private static EventConversionConfigurationService createEventConfigurationService() {
        try {
            EventConversionConfigurationComponentConfiguration eventConversionConfigurationComponentConfiguration =
                    new EventConversionConfigurationComponentConfiguration();
            return new EventConversionConfigurationServiceImpl(
                    eventConversionConfigurationComponentConfiguration.provideConversions(new DefaultResourceLoader()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static EventConversionConfigurationService getEventConfigurationService() {
        return eventConversionConfigurationService;
    }

    @ParameterizedTest(name = "Run {index}: {2}")
    @MethodSource
    void getEntityMappedFieldsTest(Tuple2<String, String> incomingEvent, Object expectedResult, String assertMessage) {
        Map<String, String> result =
                getEventConfigurationService().getEntityMappedFields(incomingEvent.v1, incomingEvent.v2);
        Assertions.assertEquals(MapUtils.isEmpty((Map) expectedResult), MapUtils.isEmpty(result), assertMessage);
    }

    private static Stream<Arguments> getEntityMappedFieldsTest() {
        TestCases.Builder<Tuple2<String, String>, Object> testCasesBuilder = TestCases.newBuilder();

        // <editor-fold desc="Mapped fields request">
        testCasesBuilder.addTestCase(new Tuple2<>("Device", "device"), new SingletonMap("Key1", "Value1"),
                "Valid mapped fields request");
        testCasesBuilder.addTestCase(new Tuple2<>("", "device"), new HashMap<>(),
                "Missing(null) entity fields request");
        testCasesBuilder.addTestCase(new Tuple2<>(null, "device"), new HashMap<>(),
                "Missing(empty string) entity fields request");
        testCasesBuilder.addTestCase(new Tuple2<>("Device", null), new HashMap<>(),
                "Missing(null) event table fields request");
        testCasesBuilder.addTestCase(new Tuple2<>("Device", ""), new HashMap<>(),
                "Missing(empty string) event table fields request");
        testCasesBuilder.addTestCase(new Tuple2<>("NonExistentEntity", "device"), new HashMap<>(),
                "Non existent entity fields request");
        testCasesBuilder.addTestCase(new Tuple2<>("Device", "NonExistentTable"), new HashMap<>(),
                "Non existent event table fields request");
        // </editor-fold>

        return testCasesBuilder.build().toArguments();
    }

    @ParameterizedTest(name = "Run {index}: {2}")
    @MethodSource
    void getChildEntitiesByParentName(Tuple2<String, String> incomingEvent, Object expectedResult,
            String assertMessage) {
        Map<String, String> result = getEventConfigurationService().getChildEntitiesByParentName(incomingEvent.v1);
        Assertions.assertEquals(MapUtils.isEmpty((Map) expectedResult), MapUtils.isEmpty(result), assertMessage);
    }

    private static Stream<Arguments> getChildEntitiesByParentName() {
        TestCases.Builder<Tuple2<String, String>, Object> testCasesBuilder = TestCases.newBuilder();

        // <editor-fold desc="Child entities request">
        testCasesBuilder.addTestCase(new Tuple2<>("Client", "customer"), new SingletonMap("Key1", "Value1"),
                "Valid mapped fields request (with children)");
        testCasesBuilder.addTestCase(new Tuple2<>("Device", "device"), null,
                "Valid mapped fields request (no children)");
        testCasesBuilder.addTestCase(new Tuple2<>("", "device"), new HashMap<>(),
                "Missing(null) entity fields request");
        testCasesBuilder.addTestCase(new Tuple2<>(null, "device"), new HashMap<>(),
                "Missing(empty string) entity fields request");
        testCasesBuilder.addTestCase(new Tuple2<>("Device", null), new HashMap<>(),
                "Missing(null) event table fields request");
        testCasesBuilder.addTestCase(new Tuple2<>("Device", ""), new HashMap<>(),
                "Missing(empty string) event table fields request");
        testCasesBuilder.addTestCase(new Tuple2<>("NonExistentEntity", "device"), new HashMap<>(),
                "Non existent entity fields request");
        testCasesBuilder.addTestCase(new Tuple2<>("Device", "NonExistentTable"), new HashMap<>(),
                "Non existent event table fields request");
        // </editor-fold>

        return testCasesBuilder.build().toArguments();
    }

    @ParameterizedTest(name = "Run {index}: {2}")
    @MethodSource
    void getEntityMandatoryFields(Tuple2<String, String> incomingEvent, Object expectedResult, String assertMessage) {
        List<String> result =
                getEventConfigurationService().getEntityMandatoryFields(incomingEvent.v1, incomingEvent.v2);
        Assertions.assertEquals(CollectionUtils.isEmpty((List) expectedResult), CollectionUtils.isEmpty(result),
                assertMessage);
    }

    private static Stream<Arguments> getEntityMandatoryFields() {
        TestCases.Builder<Tuple2<String, String>, Object> testCasesBuilder = TestCases.newBuilder();

        // <editor-fold desc="Mandatory fields request">
        testCasesBuilder.addTestCase(new Tuple2<>("Device", "device"), Collections.singletonList("Value1"),
                "Valid mapped fields request");
        testCasesBuilder.addTestCase(new Tuple2<>("", "device"), new ArrayList<>(),
                "Missing(null) entity fields request");
        testCasesBuilder.addTestCase(new Tuple2<>(null, "device"), new ArrayList<>(),
                "Missing(empty string) entity fields request");
        testCasesBuilder.addTestCase(new Tuple2<>("Device", null), new ArrayList<>(),
                "Missing(null) event table fields request");
        testCasesBuilder.addTestCase(new Tuple2<>("Device", ""), new ArrayList<>(),
                "Missing(empty string) event table fields request");
        testCasesBuilder.addTestCase(new Tuple2<>("NonExistentEntity", "device"), new ArrayList<>(),
                "Non existent entity fields request");
        testCasesBuilder.addTestCase(new Tuple2<>("Device", "NonExistentTable"), new ArrayList<>(),
                "Non existent event table fields request");
        // </editor-fold>

        return testCasesBuilder.build().toArguments();
    }

    @ParameterizedTest(name = "Run {index}: {2}")
    @MethodSource
    void getEntityNamesForEventName(String incomingEvent, Object expectedResult, String assertMessage) {
        Map<String, String> result = getEventConfigurationService().getEntityNamesForEventName(incomingEvent);
        Assertions.assertEquals(MapUtils.isEmpty((Map) expectedResult), MapUtils.isEmpty(result), assertMessage);
    }

    private static Stream<Arguments> getEntityNamesForEventName() {
        TestCases.Builder<String, Object> testCasesBuilder = TestCases.newBuilder();

        // <editor-fold desc="Event names request">
        testCasesBuilder.addTestCase("device", new SingletonMap("Key1", "Value1"), "Valid table entity request");
        testCasesBuilder.addTestCase(null, new HashMap<>(), "Missing(null) table entity request");
        testCasesBuilder.addTestCase("", new HashMap<>(), "Missing(empty string) table entity request");
        testCasesBuilder.addTestCase("NonExistentTable", new HashMap<>(), "Non existent table entity request");
        // </editor-fold>

        return testCasesBuilder.build().toArguments();
    }

    @ParameterizedTest(name = "Run {index}: {2}")
    @MethodSource
    void getEntityTrackedFields(Tuple2<String, String> incomingEvent, Object expectedResult, String assertMessage) {
        List<String> result = getEventConfigurationService().getEntityTrackedFields(incomingEvent.v1, incomingEvent.v2);
        Assertions.assertEquals(CollectionUtils.isEmpty((List) expectedResult), CollectionUtils.isEmpty(result),
                assertMessage);
    }

    private static Stream<Arguments> getEntityTrackedFields() {
        TestCases.Builder<Tuple2<String, String>, Object> testCasesBuilder = TestCases.newBuilder();

        // <editor-fold desc="Tracked fields request">
        testCasesBuilder.addTestCase(new Tuple2<>("Device", "device"), Collections.singletonList("Value1"),
                "Valid mapped fields request");
        testCasesBuilder.addTestCase(new Tuple2<>("", "device"), new ArrayList<>(),
                "Missing(null) entity fields request");
        testCasesBuilder.addTestCase(new Tuple2<>(null, "device"), new ArrayList<>(),
                "Missing(empty string) entity fields request");
        testCasesBuilder.addTestCase(new Tuple2<>("Device", null), new ArrayList<>(),
                "Missing(null) event table fields request");
        testCasesBuilder.addTestCase(new Tuple2<>("Device", ""), new ArrayList<>(),
                "Missing(empty string) event table fields request");
        testCasesBuilder.addTestCase(new Tuple2<>("NonExistentEntity", "device"), new ArrayList<>(),
                "Non existent entity fields request");
        testCasesBuilder.addTestCase(new Tuple2<>("Device", "NonExistentTable"), new ArrayList<>(),
                "Non existent event table fields request");
        // </editor-fold>

        return testCasesBuilder.build().toArguments();
    }

    @Test
    void mergeConversions() throws IOException {
        String configurationStringToMerge = "conversions:\n" + "  \"TestEntity\":\n"
                + "    entityPath: \"com.solarwinds.msp.ncentral.proto.entity.entity.TestEntityOuterClass.TestEntity\"\n"
                + "    tables:\n" + "      \"test\":\n" + "        fields:\n" + "          \"field1\":\n"
                + "            targetField: \"TestField\"\n" + "            isMandatory: true";
        Conversions baseConfigurations = new EventConversionConfigurationComponentConfiguration().provideConversions(
                new DefaultResourceLoader());
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        Conversions conversionsToMerge = mapper.readValue(configurationStringToMerge, Conversions.class);
        baseConfigurations.mergeConversions(conversionsToMerge.getConversions());
        Assertions.assertTrue(baseConfigurations.getConversions().containsKey("TestEntity"));
    }

    @Test
    void getConversions() throws IOException {
        Assertions.assertNotNull(
                new EventConversionConfigurationComponentConfiguration().provideConversions(new DefaultResourceLoader())
                        .getConversions());
    }

    @ParameterizedTest(name = "Run {index}: {2}")
    @MethodSource
    public void isTimeSeries(String incomingEvent, Object expectedResult, String assertMessage) {
        boolean result = getEventConfigurationService().isTimeSeries(incomingEvent);
        Assertions.assertEquals(expectedResult, result, assertMessage);
    }

    private static Stream<Arguments> isTimeSeries() {
        TestCases.Builder<String, Boolean> testCasesBuilder = TestCases.newBuilder();

        // <editor-fold desc="Customer lookup criteria request">
        testCasesBuilder.addTestCase("appliancetask", false, "Valid isTimeSeries request, default value.");
        testCasesBuilder.addTestCase("datacpu_detailed", true, "Valid isTimeSeries request, existing entry.");
        testCasesBuilder.addTestCase("datasnmp_detailed", true,
                "Valid isTimeSeries request, Non existing data table entry.");
        testCasesBuilder.addTestCase("test", false,
                "Valid isTimeSeries request when table linked to multiple entities and lookup is not blank");
        testCasesBuilder.addTestCase(null, false, "Missing(null) table name request");
        testCasesBuilder.addTestCase("", false, "Missing(empty string) table name request");
        testCasesBuilder.addTestCase("NonExistentTable", false, "Non existent table name request");
        // </editor-fold>

        return testCasesBuilder.build().toArguments();
    }

    @ParameterizedTest(name = "Run {index}: {2}")
    @MethodSource
    public void getCustomerLookupCriteria(String incomingEvent, Object expectedResult, String assertMessage) {
        Map<String, String> result = getEventConfigurationService().getCustomerLookupCriteria(incomingEvent);
        Assertions.assertEquals(MapUtils.isEmpty((Map) expectedResult), MapUtils.isEmpty(result), assertMessage);
    }

    private static Stream<Arguments> getCustomerLookupCriteria() {
        TestCases.Builder<String, Object> testCasesBuilder = TestCases.newBuilder();

        // <editor-fold desc="Customer lookup criteria request">
        testCasesBuilder.addTestCase("appliancetask",
                new SingletonMap(" device custTable ", " maintable.deviceid = custTable.deviceid "),
                "Valid customer lookup criteria request");
        testCasesBuilder.addTestCase("device", new HashMap<>(),
                "Valid customer lookup criteria request when table linked to multiple entities and lookup is blank");
        testCasesBuilder.addTestCase("datasnmp_detailed",
                new SingletonMap(" appliancetask jointable1, device custTable ",
                        " maintable.taskid = jointable1.taskid\tand jointable1.deviceid = custTable.deviceid "),
                "Valid customer lookup criteria request when table is data table and not explicitly defined in YAML.");
        testCasesBuilder.addTestCase("test", new HashMap<>(),
                "Valid customer lookup criteria request when table linked to multiple entities and lookup is not blank");
        testCasesBuilder.addTestCase(null, new HashMap<>(), "Missing(null) event name request");
        testCasesBuilder.addTestCase("", new HashMap<>(), "Missing(empty string) event name request");
        testCasesBuilder.addTestCase("NonExistentTable", new HashMap<>(), "Non existent event name request");
        // </editor-fold>

        return testCasesBuilder.build().toArguments();
    }

    @ParameterizedTest(name = "Run {index}: {2}")
    @MethodSource
    public void getDefaultCustomerLookupCriteriaForEntity(String incomingEvent, Object expectedResult,
            String assertMessage) {
        Map<String, String> result =
                getEventConfigurationService().getDefaultCustomerLookupCriteriaForEntity(incomingEvent);
        Assertions.assertEquals(MapUtils.isEmpty((Map) expectedResult), MapUtils.isEmpty(result), assertMessage);
        assertThat(result).isEqualTo(expectedResult);
    }

    private static Stream<Arguments> getDefaultCustomerLookupCriteriaForEntity() {
        TestCases.Builder<String, Object> testCasesBuilder = TestCases.newBuilder();

        // <editor-fold desc="Default entity customer lookup criteria request">
        testCasesBuilder.addTestCase("GenericServiceData",
                new SingletonMap(" appliancetask jointable1, device custTable ",
                        " maintable.taskid = jointable1.taskid and jointable1.deviceid = custTable.deviceid "),
                "Valid default entity customer lookup criteria request");
        testCasesBuilder.addTestCase("Task",
                new SingletonMap(" device custTable ", " maintable.deviceid = custTable.deviceid "),
                "Valid customer lookup criteria request");
        testCasesBuilder.addTestCase("Device", new HashMap<>(),
                "Valid default entity customer lookup criteria request when table linked to multiple entities and lookup is blank");
        testCasesBuilder.addTestCase("TestEntity1", new HashMap<>(),
                "Valid default entity customer lookup criteria request when table linked to multiple entities and lookup is not blank");
        testCasesBuilder.addTestCase(null, new HashMap<>(), "Missing(null) entity name request");
        testCasesBuilder.addTestCase("", new HashMap<>(), "Missing(empty string) entity name request");
        testCasesBuilder.addTestCase("NonExistentEntity", new HashMap<>(), "Non existent entity name request");
        // </editor-fold>

        return testCasesBuilder.build().toArguments();
    }

    @ParameterizedTest(name = "Run {index}: {2}")
    @MethodSource
    public void getIncrementalLoadColumn(String incomingEvent, Object expectedResult, String assertMessage) {
        String result = getEventConfigurationService().getIncrementalLoadColumn(incomingEvent);
        Assertions.assertEquals(expectedResult, result, assertMessage);
    }

    private static Stream<Arguments> getIncrementalLoadColumn() {
        TestCases.Builder<String, Object> testCasesBuilder = TestCases.newBuilder();

        // <editor-fold desc="Incremental load column  request">
        testCasesBuilder.addTestCase("appliancetask", "lastupdated", "Valid incremental load column default.");
        testCasesBuilder.addTestCase("eventdata_localization", "",
                "Valid incremental load column when table has no date column that can be used.");
        testCasesBuilder.addTestCase("notificationbuffer", "timesent",
                "Valid incremental load column when table has non default column.");
        testCasesBuilder.addTestCase(null, "", "Missing(null) table name request");
        testCasesBuilder.addTestCase("", "", "Missing(empty string) table name request");
        testCasesBuilder.addTestCase("NonExistentTable", "", "Non existent table name request");
        // </editor-fold>

        return testCasesBuilder.build().toArguments();
    }

    @ParameterizedTest(name = "Run {index}: {2}")
    @MethodSource
    public void getEntityNamesForEntityName(String incomingEntity, Object expectedResult, String assertMessage) {
        Map<String, String> result = getEventConfigurationService().getEntityNamesForEntityName(incomingEntity);
        Assertions.assertEquals(MapUtils.isEmpty((Map) expectedResult), MapUtils.isEmpty(result), assertMessage);
    }

    private static Stream<Arguments> getEntityNamesForEntityName() {
        TestCases.Builder<String, Object> testCasesBuilder = TestCases.newBuilder();

        // <editor-fold desc="Entity names request">
        testCasesBuilder.addTestCase("Device",
                new SingletonMap("Device", "com.solarwinds.msp.ncentral.proto.entity.entity.DeviceOuterClass.Device"),
                "Valid table entity name request");
        testCasesBuilder.addTestCase(null, new HashMap<>(), "Missing(null) entity name request");
        testCasesBuilder.addTestCase("", new HashMap<>(), "Missing(empty string) entity name request");
        testCasesBuilder.addTestCase("NonExistentTable", new HashMap<>(), "Non existent entity name request");
        // </editor-fold>

        return testCasesBuilder.build().toArguments();
    }

    @BeforeAll
    public static void setUp() throws Exception {
        String configurationStringToMerge = "conversions:\n" + "  \"TestEntity1\":\n"
                + "    entityPath: \"com.solarwinds.msp.ncentral.proto.entity.tasks.TaskOuterClass.TestEntity1\"\n"
                + "    tables:\n" + "      \"test\":\n" + "        joinToCustomer: \" device custTable \"\n"
                + "        whereToCustomer: \" maintable.deviceid = custTable.deviceid \"\n" + "        fields:\n"
                + "          \"testid\":\n" + "            targetField: \"TestId\"\n"
                + "            isMandatory: true\n" + "  \"TestEntity2\":\n"
                + "    entityPath: \"com.solarwinds.msp.ncentral.proto.entity.tasks.TaskOuterClass.TestEntity2\"\n"
                + "    tables:\n" + "      \"test\":\n" + "        joinToCustomer: \" device custTable \"\n"
                + "        whereToCustomer: \" maintable.deviceid = custTable.deviceid \"\n" + "        fields:\n"
                + "          \"testid\":\n" + "            targetField: \"TestId\"\n"
                + "            isMandatory: true\n";
        Conversions baseConfigurations = new EventConversionConfigurationComponentConfiguration().provideConversions(
                new DefaultResourceLoader());
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        Conversions conversionsToMerge = mapper.readValue(configurationStringToMerge, Conversions.class);
        baseConfigurations.mergeConversions(conversionsToMerge.getConversions());
    }
}
