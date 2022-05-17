package com.solarwinds.msp.ncentral.eventproduction.converter.impl.entity;

import com.google.protobuf.GeneratedMessageV3;
import com.google.protobuf.Timestamp;

import com.solarwinds.msp.ncentral.eventproduction.api.entity.Event;
import com.solarwinds.msp.ncentral.eventproduction.api.entity.EventType;
import com.solarwinds.msp.ncentral.eventproduction.converter.EventParser;
import com.solarwinds.msp.ncentral.proto.entity.ActionOuterClass;
import com.solarwinds.msp.ncentral.proto.entity.MspContextOuterClass;
import com.solarwinds.msp.ncentral.proto.entity.entity.DeviceOuterClass;
import com.solarwinds.msp.ncentral.proto.entity.entity.LicensingOuterClass;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

class DeviceEventTest {

    private static Stream<Arguments> parseRecordTest() {
        EventParserTestCases testCases = new EventParserTestCases();
        DeviceOuterClass.Device expectedResult;
        Event incomingEvent;
        String parsingAssertMessage;

        //<editor-fold desc="Valid device event">
        parsingAssertMessage = "Valid device event";
        Map<String, String> deventity = new HashMap<>();

        deventity.put("deviceclassid", "106");
        deventity.put("deviceclasslabel", "Servers - Windows");
        deventity.put("isdiscoveredasset", "true");
        deventity.put("thirdpartypatchenabled", "false");
        deventity.put("synchlongname", "true");
        deventity.put("tracking", "false");
        deventity.put("defaultaddresstype", "ipaddress");
        deventity.put("lost", "false");
        deventity.put("isdisconnected", "false");
        deventity.put("opsysid", "303");
        deventity.put("userspecifieddeviceclass", "false");
        deventity.put("firstsample", "2019-02-14T16:00:05.225Z");
        deventity.put("selfdiscoveryid", "-1");
        deventity.put("xabackupenabled", "false");
        deventity.put("dynamicuri", "true");
        deventity.put("licensemode", "Professional");
        deventity.put("system", "true");
        deventity.put("customerid", "101");
        deventity.put("supportedos", "Microsoft Windows Server 2016 Standard x64 Edition");
        deventity.put("explicitlyunmanaged", "true");
        deventity.put("discoveredname", "EHAKKI-DT");
        deventity.put("veritasenabled", "false");
        deventity.put("snmpenabled", "false");
        deventity.put("edfenabled", "false");
        deventity.put("wirelessmanagementstate", "17");
        deventity.put("backupmanagerlicensetype", null);
        deventity.put("description", "Network device discovered using Asset Discovery - 1907480434");
        deventity.put("osid", "winnt");
        deventity.put("isprobe", "true");
        deventity.put("enabled", "true");
        deventity.put("patchenabled", "false");
        deventity.put("netpathenabled", "false");
        deventity.put("supportedoslabel", "Microsoft Windows Server 2016 Standard x64 Edition");
        deventity.put("deviceclass", "Servers - Windows");
        deventity.put("monitorwarrantyexpiryenabled", "false");
        deventity.put("mspbackupenabled", "false");
        deventity.put("discoverednameon", "2019-02-14T16:00:05.333Z");
        deventity.put("ismanagedasset", "false");
        deventity.put("remotemanagementenabled", "false");
        deventity.put("longname", "EHAKKI-DT");
        deventity.put("virtualizationenabled", "false");
        deventity.put("essentialunattendedremotecontrolenabled", "false");
        deventity.put("usesysname", "false");
        deventity.put("reactiveenabled", "false");
        deventity.put("lastsample", "2019-02-14T16:00:05.229Z");
        deventity.put("isautoinstalled", "false");
        deventity.put("scheduledtasksenabled", "false");
        deventity.put("deviceid", "1907480434");
        deventity.put("uri", "1.1.1.1");
        deventity.put("createdon", "2019-02-21T13:51:52.068Z");
        deventity.put("ncentralassettag", "ee507d9d-498a-4921-a1ce-013a38504213-20190129-184159");
        deventity.put("deleted", "false");
        deventity.put("remotemanagementinstalled", "false");
        deventity.put("userspecifiedopsysid", "false");
        deventity.put("securitymanagerenabled", "false");
        deventity.put("isdisconnectable", "false");
        deventity.put("sourceuri", "test.testing.com");
        deventity.put("remotecontroluri", "remote.control.com");
        deventity.put("leaseexpirydate", "2019-02-21T13:51:52.068Z");
        deventity.put("expectedreplacementdate", "2019-02-21T13:51:52.068Z");
        deventity.put("cost", "123.45");
        deventity.put("warrantyexpirydate", "2019-02-21T13:51:52.068Z");
        deventity.put("purchasedate", "2019-02-21T13:51:52.068Z");
        deventity.put("ismanagedasset", "true");
        deventity.put("lastupdated", "2019-02-21T13:51:52.068Z");

        incomingEvent = Event.builder()
                .entityDataTypes(Collections.EMPTY_MAP)
                .bizappsCustomerId("aaea6111-b13f-462b-9385-4f3baa7f0ccc")
                .ncentralServerGuid("225ed5bb-9e8e-4ad5-aa8c-7a018dc5ffa5")
                .eventingConfigurationCustomerId(1)
                .professionalModeLicenseType("Per Device")
                .eventType(EventType.INSERT)
                .entityType("device")
                .entity(deventity)
                .newValues(Collections.emptyMap())
                .build();

        expectedResult = DeviceOuterClass.Device.newBuilder()
                .setClientId(101)
                .setDeviceId(1907480434)
                .setGuid("ee507d9d-498a-4921-a1ce-013a38504213-20190129-184159")
                .setName("EHAKKI-DT")
                .setDescription("Network device discovered using Asset Discovery - 1907480434")
                .setUri("1.1.1.1")
                .setDeviceClass("Servers - Windows")
                .setIsManaged(true)
                .setActive(true)
                .setInstallTimestamp(Timestamp.newBuilder().setSeconds(1550757112).setNanos(68000000).build())
                .setWarrantyExpiryDate(Timestamp.newBuilder().setSeconds(1550757112).setNanos(68000000).build())
                .setPurchaseDate(Timestamp.newBuilder().setSeconds(1550757112).setNanos(68000000).build())
                .setSourceUri("test.testing.com")
                .setRemoteControlUri("remote.control.com")
                .setIsDynamicUri(true)
                .setIsSystem(true)
                .setIsProbe(true)
                .setIsDiscoveredAsset(true)
                .setLeaseExpiryDate(Timestamp.newBuilder().setSeconds(1550757112).setNanos(68000000).build())
                .setExpectedReplacementDate(Timestamp.newBuilder().setSeconds(1550757112).setNanos(68000000).build())
                .setExplicitlyUnmanaged(true)
                .setDeviceCost(123.45)
                .setLastUpdated(Timestamp.newBuilder().setSeconds(1550757112).setNanos(68000000).build())
                .setAction(ActionOuterClass.Action.ADD)
                .setContext(MspContextOuterClass.MspContext.newBuilder()
                        .setBizAppsCustomerId("aaea6111-b13f-462b-9385-4f3baa7f0ccc")
                        .setSystemGuid("225ed5bb-9e8e-4ad5-aa8c-7a018dc5ffa5")
                        .build())
                .build();

        testCases.setTestCase(incomingEvent, expectedResult, parsingAssertMessage);
        //</editor-fold>

        //<editor-fold desc="Device missing deviceid event">
        parsingAssertMessage = "Device missing deviceid event";
        deventity = new HashMap<>();

        incomingEvent = Event.builder()
                .entityDataTypes(Collections.EMPTY_MAP)
                .bizappsCustomerId("aaea6111-b13f-462b-9385-4f3baa7f0ccc")
                .ncentralServerGuid("225ed5bb-9e8e-4ad5-aa8c-7a018dc5ffa5")
                .eventingConfigurationCustomerId(1)
                .professionalModeLicenseType("Per Device")
                .eventType(EventType.INSERT)
                .entityType("device")
                .entity(deventity)
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
        List<com.google.protobuf.GeneratedMessageV3> result = eventParser.parse(incomingEvent);
        result.removeIf(message -> message.getClass().equals(LicensingOuterClass.Licensing.class));

        Assertions.assertEquals(expectedResult, result, assertMessage);
    }
}