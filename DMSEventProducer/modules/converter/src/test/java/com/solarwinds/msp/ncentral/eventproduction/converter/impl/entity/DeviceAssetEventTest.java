package com.solarwinds.msp.ncentral.eventproduction.converter.impl.entity;

import com.google.protobuf.GeneratedMessageV3;
import com.google.protobuf.Timestamp;

import com.solarwinds.msp.ncentral.eventproduction.api.entity.Event;
import com.solarwinds.msp.ncentral.eventproduction.api.entity.EventType;
import com.solarwinds.msp.ncentral.eventproduction.converter.EventParser;
import com.solarwinds.msp.ncentral.proto.entity.ActionOuterClass;
import com.solarwinds.msp.ncentral.proto.entity.MspContextOuterClass;
import com.solarwinds.msp.ncentral.proto.entity.asset.DeviceAssetOuterClass;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

class DeviceAssetEventTest {

    private static Stream<Arguments> parseRecordTest() {
        EventParserTestCases testCases = new EventParserTestCases();
        DeviceAssetOuterClass.DeviceAsset expectedResult;
        Event incomingEvent;
        String parsingAssertMessage;

        //<editor-fold desc="Valid device asset event">
        parsingAssertMessage = "Valid device asset event";
        Map<String, String> devAssentity = new HashMap<>();

        devAssentity.put("deviceid", "1907480434");
        devAssentity.put("domain", "swdev.local");
        devAssentity.put("manufacturer", "Dell Inc.");
        devAssentity.put("model", "OptiPlex 9020");
        devAssentity.put("netbiosname", "EHAKKI-DT");
        devAssentity.put("systemtype", "x64-based PC");
        devAssentity.put("totalphysicalmemory", "34359738368");
        devAssentity.put("serialnumber", "78Z0V52");
        devAssentity.put("version", "5.5.0");
        devAssentity.put("amt_uuid", "a8461754-a1e5-e511-b16b-0894ef11a790");
        devAssentity.put("qstenabled", "0");
        devAssentity.put("amt_version", "1.1");
        devAssentity.put("wirelessmanagementstate", "1");
        devAssentity.put("deleted", "false");
        devAssentity.put("chassistype", "Convertible");
        devAssentity.put("totalmemory_slots", "4");
        devAssentity.put("populatedmemory_slots", "4");
        devAssentity.put("uuid", "55b017d1-653a-a910-d16b-a2a68b76105f");
        devAssentity.put("timezone", "(UTC-05:00) Eastern Time (US & Canada)");
        devAssentity.put("productnumber", "06B7");
        devAssentity.put("lastupdated", "2019-02-21T13:51:52.068Z");

        incomingEvent = Event.builder()
                .entityDataTypes(Collections.EMPTY_MAP)
                .bizappsCustomerId("aaea6111-b13f-462b-9385-4f3baa7f0ccc")
                .ncentralServerGuid("225ed5bb-9e8e-4ad5-aa8c-7a018dc5ffa5")
                .eventingConfigurationCustomerId(1)
                .professionalModeLicenseType("Per Device")
                .eventType(EventType.INSERT)
                .entityType("cim_computersystem")
                .entity(devAssentity)
                .newValues(Collections.emptyMap())
                .build();

        expectedResult = DeviceAssetOuterClass.DeviceAsset.newBuilder()
                .setDeviceId(1907480434)
                .setDomain("swdev.local")
                .setManufacturer("Dell Inc.")
                .setModelNumber("OptiPlex 9020")
                .setNetBiosName("EHAKKI-DT")
                .setSystemType("x64-based PC")
                .setTotalMemoryBytes(34359738368F)
                .setTotalMemorySlots(4)
                .setPopulatedMemorySlots(4)
                .setSerialNumber("78Z0V52")
                .setVersion("5.5.0")
                .setAmtUuid("a8461754-a1e5-e511-b16b-0894ef11a790")
                .setQstEnabled("0")
                .setAmtVersion("1.1")
                .setWirelessManagementState("1")
                .setChassisType("Convertible")
                .setUuid("55b017d1-653a-a910-d16b-a2a68b76105f")
                .setTimeZone("(UTC-05:00) Eastern Time (US & Canada)")
                .setProductNumber("06B7")
                .setLastUpdated(Timestamp.newBuilder().setSeconds(1550757112).setNanos(68000000).build())
                .setAction(ActionOuterClass.Action.ADD)
                .setContext(MspContextOuterClass.MspContext.newBuilder()
                        .setBizAppsCustomerId("aaea6111-b13f-462b-9385-4f3baa7f0ccc")
                        .setSystemGuid("225ed5bb-9e8e-4ad5-aa8c-7a018dc5ffa5")
                        .build())
                .build();

        testCases.setTestCase(incomingEvent, expectedResult, parsingAssertMessage);
        //</editor-fold>

        //<editor-fold desc="Device asset missing deviceid event">
        parsingAssertMessage = "Device asset missing deviceid event";
        devAssentity = new HashMap<>();

        incomingEvent = Event.builder()
                .entityDataTypes(Collections.EMPTY_MAP)
                .bizappsCustomerId("aaea6111-b13f-462b-9385-4f3baa7f0ccc")
                .ncentralServerGuid("225ed5bb-9e8e-4ad5-aa8c-7a018dc5ffa5")
                .eventingConfigurationCustomerId(1)
                .professionalModeLicenseType("Per Device")
                .eventType(EventType.INSERT)
                .entityType("cim_computersystem")
                .entity(devAssentity)
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