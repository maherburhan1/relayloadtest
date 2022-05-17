package com.solarwinds.msp.ncentral.eventproduction.converter.impl.entity;

import com.google.protobuf.GeneratedMessageV3;
import com.google.protobuf.Timestamp;

import com.solarwinds.msp.ncentral.eventproduction.api.entity.Event;
import com.solarwinds.msp.ncentral.eventproduction.api.entity.EventType;
import com.solarwinds.msp.ncentral.eventproduction.converter.EventParser;
import com.solarwinds.msp.ncentral.proto.entity.ActionOuterClass;
import com.solarwinds.msp.ncentral.proto.entity.MspContextOuterClass;
import com.solarwinds.msp.ncentral.proto.entity.asset.MobileAttributesOuterClass;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

class MobileAttributesEventTest {

    private static Stream<Arguments> parseRecordTest() {
        EventParserTestCases testCases = new EventParserTestCases();
        MobileAttributesOuterClass.MobileAttributes expectedResult;
        Event incomingEvent;
        String parsingAssertMessage;

        //<editor-fold desc="Valid mobile attributes event">
        parsingAssertMessage = "Valid mobile attributes event";
        Map<String, String> mobileEntity = new HashMap<>();
        mobileEntity.put("deviceid", "1907480434");
        mobileEntity.put("devicename", "My Mobile");
        mobileEntity.put("osversion", "10.2");
        mobileEntity.put("buildversion", "1.2");
        mobileEntity.put("modelname", "Lumia 950XL");
        mobileEntity.put("model", "950XLA1");
        mobileEntity.put("serialnumber", "1234567890");
        mobileEntity.put("devicecapacity", "123");
        mobileEntity.put("availabledevicecapacity", "100");
        mobileEntity.put("internaldevicecapacity", "90");
        mobileEntity.put("internalavailabledevicecapacity", "80");
        mobileEntity.put("externaldevicecapacity", "43");
        mobileEntity.put("externalavailabledevicecapacity", "20");
        mobileEntity.put("kernelversion", "123");
        mobileEntity.put("imei", "im1e2");
        mobileEntity.put("jailbreakdetected", "false");
        mobileEntity.put("modemfirmwareversion", "2.2");
        mobileEntity.put("iccid", "12");
        mobileEntity.put("bluetoothmac", "1a 2b 3c 4d");
        mobileEntity.put("wifimac", "5a 6b 7c 8d");
        mobileEntity.put("ethernetmac", "9a 10 12 13");
        mobileEntity.put("currentcarriernetwork", "Virgin Mobile");
        mobileEntity.put("simcarriernetwork", "Virgin");
        mobileEntity.put("carriersettingsversion", "1.1");
        mobileEntity.put("phonenumber", "654-321-1111");
        mobileEntity.put("productname", "Windows Phone");
        mobileEntity.put("voiceroamingenabled", "true");
        mobileEntity.put("dataroamingenabled", "true");
        mobileEntity.put("cellulartechnology", "5");
        mobileEntity.put("subscribermcc", "12");
        mobileEntity.put("batterylevel", "87.5");
        mobileEntity.put("manufacturer", "Microsoft");
        mobileEntity.put("lastupdated", "2019-02-21T13:51:52.068Z");
        mobileEntity.put("deleted", "false");

        incomingEvent = Event.builder()
                .entityDataTypes(Collections.EMPTY_MAP)
                .bizappsCustomerId("aaea6111-b13f-462b-9385-4f3baa7f0ccc")
                .ncentralServerGuid("225ed5bb-9e8e-4ad5-aa8c-7a018dc5ffa5")
                .eventingConfigurationCustomerId(1)
                .professionalModeLicenseType("Per Device")
                .eventType(EventType.INSERT)
                .entityType("cim_mobile_attributes")
                .entity(mobileEntity)
                .newValues(Collections.emptyMap())
                .build();

        expectedResult = MobileAttributesOuterClass.MobileAttributes.newBuilder()
                .setDeviceId(1907480434)
                .setAvailableDeviceCapacityGb(100.0)
                .setBatteryLevel(87.5)
                .setBluetoothMac("1a 2b 3c 4d")
                .setBuildVersion("1.2")
                .setCarrierSettingsVersion("1.1")
                .setCellularTechnology(5)
                .setCurrentCarrierNetwork("Virgin Mobile")
                .setDataRoamingEnabled(true)
                .setDeviceCapacityGb(123.0)
                .setEthernetMac("9a 10 12 13")
                .setExternalAvailableDeviceCapacityGb(20.0)
                .setExternalDeviceCapacityGb(43.0)
                .setIccid("12")
                .setImei("im1e2")
                .setInternalAvailableDeviceCapacityGb(80.0)
                .setInternalDeviceCapacityGb(90.0)
                .setKernelVersion("123")
                .setManufacturer("Microsoft")
                .setModel("950XLA1")
                .setModelName("Lumia 950XL")
                .setModemFirmwareVersion("2.2")
                .setOsVersion("10.2")
                .setPhoneNumber("654-321-1111")
                .setProductName("Windows Phone")
                .setSerialNumber("1234567890")
                .setSimCarrierNetwork("Virgin")
                .setSubscriberMcc(12)
                .setVoiceRoamingEnabled(true)
                .setWifiMac("5a 6b 7c 8d")
                .setDeviceName("My Mobile")
                .setLastUpdated(Timestamp.newBuilder().setSeconds(1550757112).setNanos(68000000).build())
                .setAction(ActionOuterClass.Action.ADD)
                .setContext(MspContextOuterClass.MspContext.newBuilder()
                        .setBizAppsCustomerId("aaea6111-b13f-462b-9385-4f3baa7f0ccc")
                        .setSystemGuid("225ed5bb-9e8e-4ad5-aa8c-7a018dc5ffa5")
                        .build())
                .build();

        testCases.setTestCase(incomingEvent, expectedResult, parsingAssertMessage);
        //</editor-fold>

        //<editor-fold desc="Mobile attributes missing deviceid event">
        parsingAssertMessage = "Mobile attributes missing deviceid event";
        mobileEntity = new HashMap<>();

        incomingEvent = Event.builder()
                .entityDataTypes(Collections.EMPTY_MAP)
                .bizappsCustomerId("aaea6111-b13f-462b-9385-4f3baa7f0ccc")
                .ncentralServerGuid("225ed5bb-9e8e-4ad5-aa8c-7a018dc5ffa5")
                .eventingConfigurationCustomerId(1)
                .professionalModeLicenseType("Per Device")
                .eventType(EventType.INSERT)
                .entityType("device")
                .entity(mobileEntity)
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