package com.solarwinds.msp.ncentral.eventproduction.converter.impl.entity;

import com.google.protobuf.GeneratedMessageV3;

import com.solarwinds.msp.ncentral.eventproduction.api.entity.Event;
import com.solarwinds.msp.ncentral.eventproduction.api.entity.EventType;
import com.solarwinds.msp.ncentral.eventproduction.converter.EventParser;
import com.solarwinds.msp.ncentral.proto.entity.ActionOuterClass;
import com.solarwinds.msp.ncentral.proto.entity.MspContextOuterClass;
import com.solarwinds.msp.ncentral.proto.entity.entity.ServiceInstanceTypeOuterClass;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

class ServiceInstanceTypeEventTest {

    private static Stream<Arguments> parseRecordTest() {
        EventParserTestCases testCases = new EventParserTestCases();
        ServiceInstanceTypeOuterClass.ServiceInstanceType expectedResult;
        Event incomingEvent;
        String parsingAssertMessage;

        Map<String, String> serviceTypeEntity = new HashMap<>();
        serviceTypeEntity.put("servicetypeid", "25");
        serviceTypeEntity.put("servicetype", "ESXi");
        serviceTypeEntity.put("description", "servicetype for ESXi Server related services");
        serviceTypeEntity.put("isgenericserviceengine", "true");

        //<editor-fold desc="Valid service type event">
        parsingAssertMessage = "Valid service instance type event";
        Map<String, String> serviceItemTypeEntity1 = new HashMap<>(serviceTypeEntity);

        incomingEvent = Event.builder()
                .entityDataTypes(Collections.EMPTY_MAP)
                .bizappsCustomerId("aaea6111-b13f-462b-9385-4f3baa7f0ccc")
                .ncentralServerGuid("225ed5bb-9e8e-4ad5-aa8c-7a018dc5ffa5")
                .eventingConfigurationCustomerId(1)
                .professionalModeLicenseType("Per Device")
                .eventType(EventType.INSERT)
                .entityType("servicetype")
                .entity(serviceItemTypeEntity1)
                .newValues(Collections.emptyMap())
                .build();

        expectedResult = ServiceInstanceTypeOuterClass.ServiceInstanceType.newBuilder()
                .setServiceInstanceTypeId(25)
                .setName("ESXi")
                .setDescription("servicetype for ESXi Server related services")
                .setGenericServiceEngine(true)
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
            List<GeneratedMessageV3> expectedResult, String assertMessage) {
        Assertions.assertEquals(expectedResult, eventParser.parse(incomingEvent), assertMessage);
    }
}