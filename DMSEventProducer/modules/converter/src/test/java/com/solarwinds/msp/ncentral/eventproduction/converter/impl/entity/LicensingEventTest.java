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

class LicensingEventTest {

    private static Stream<Arguments> parseRecordTest() {
        EventParserTestCases testCases = new EventParserTestCases();
        LicensingOuterClass.Licensing expectedResult;
        Event incomingEvent;
        String parsingAssertMessage;

        //<editor-fold desc="Valid licensing event">
        parsingAssertMessage = "Valid licensing event";
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
        deventity.put("dynamicuri", "false");
        deventity.put("licensemode", "Professional");
        deventity.put("system", "false");
        deventity.put("customerid", "101");
        deventity.put("supportedos", "Microsoft Windows Server 2016 Standard x64 Edition");
        deventity.put("explicitlyunmanaged", "false");
        deventity.put("discoveredname", "EHAKKI-DT");
        deventity.put("veritasenabled", "false");
        deventity.put("snmpenabled", "false");
        deventity.put("edfenabled", "false");
        deventity.put("wirelessmanagementstate", "17");
        deventity.put("backupmanagerlicensetype", null);
        deventity.put("description", "Network device discovered using Asset Discovery - 1907480434");
        deventity.put("osid", "winnt");
        deventity.put("isprobe", "false");
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

        expectedResult = LicensingOuterClass.Licensing.newBuilder()
                .setDeviceId(1907480434)
                .addLicense(LicensingOuterClass.Licensing.License.newBuilder()
                        .setName("Remote Support Manager")
                        .setSourceName("remotemanagementenabled")
                        .setActive(false)
                        .build())
                .addLicense(LicensingOuterClass.Licensing.License.newBuilder()
                        .setName("Backup Exec")
                        .setSourceName("veritasenabled")
                        .setActive(false)
                        .build())
                .addLicense(LicensingOuterClass.Licensing.License.newBuilder()
                        .setName("Professional Network-class")
                        .setSourceName("ProfessionalNetwork")
                        .setActive(false)
                        .build())
                .addLicense(LicensingOuterClass.Licensing.License.newBuilder()
                        .setName("Third Party Patch Management (Professional and Essentials)")
                        .setSourceName("thirdpartypatchenabled")
                        .setActive(false)
                        .build())
                .addLicense(LicensingOuterClass.Licensing.License.newBuilder()
                        .setName("Backup Manager Advanced Server")
                        .setSourceName("BackupManagerAdvanced")
                        .setActive(false)
                        .build())
                .addLicense(LicensingOuterClass.Licensing.License.newBuilder()
                        .setName("Simple Network Management Protocol")
                        .setSourceName("snmpenabled")
                        .setActive(false)
                        .build())
                .addLicense(LicensingOuterClass.Licensing.License.newBuilder()
                        .setName("Professional Workstation-class")
                        .setSourceName("ProfessionalWorkstation")
                        .setActive(false)
                        .build())
                .addLicense(LicensingOuterClass.Licensing.License.newBuilder()
                        .setName("External Data Feed")
                        .setSourceName("edfenabled")
                        .setActive(false)
                        .build())
                .addLicense(LicensingOuterClass.Licensing.License.newBuilder()
                        .setName("Microsoft Patch Management on Essentials")
                        .setSourceName("patchenabledessential")
                        .setActive(false)
                        .build())
                .addLicense(LicensingOuterClass.Licensing.License.newBuilder()
                        .setName("Direct Support")
                        .setSourceName("reactiveenabled")
                        .setActive(false)
                        .build())
                .addLicense(LicensingOuterClass.Licensing.License.newBuilder()
                        .setName("Backup Manager SBS Server")
                        .setSourceName("BackupManagerSBS")
                        .setActive(false)
                        .build())
                .addLicense(LicensingOuterClass.Licensing.License.newBuilder()
                        .setName("Essential")
                        .setSourceName("Essential")
                        .setActive(false)
                        .build())
                .addLicense(LicensingOuterClass.Licensing.License.newBuilder()
                        .setName("Mobile")
                        .setSourceName("Mobile")
                        .setActive(false)
                        .build())
                .addLicense(LicensingOuterClass.Licensing.License.newBuilder()
                        .setName("Backup Manager Standard Server")
                        .setSourceName("BackupManagerStandard")
                        .setActive(false)
                        .build())
                .addLicense(LicensingOuterClass.Licensing.License.newBuilder()
                        .setName("Microsoft Patch Management")
                        .setSourceName("patchenabled")
                        .setActive(false)
                        .build())
                .addLicense(LicensingOuterClass.Licensing.License.newBuilder()
                        .setName("Professional")
                        .setSourceName("Professional")
                        .setActive(false)
                        .build())
                .addLicense(LicensingOuterClass.Licensing.License.newBuilder()
                        .setName("Net Path")
                        .setSourceName("netpathenabled")
                        .setActive(false)
                        .build())
                .addLicense(LicensingOuterClass.Licensing.License.newBuilder()
                        .setName("AV Defender")
                        .setSourceName("securitymanagerenabled")
                        .setActive(false)
                        .build())
                .addLicense(LicensingOuterClass.Licensing.License.newBuilder()
                        .setName("Professional Server-class")
                        .setSourceName("ProfessionalServer")
                        .setActive(false)
                        .build())
                .addLicense(LicensingOuterClass.Licensing.License.newBuilder()
                        .setName("Backup Manager Workstation (Laptop/Desktop)")
                        .setSourceName("BackupManagerWorkstation")
                        .setActive(false)
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

        //<editor-fold desc="Licensing missing deviceid event">
        parsingAssertMessage = "Licensing missing deviceid event";
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
        result.removeIf(message -> message.getClass().equals(DeviceOuterClass.Device.class));

        Assertions.assertEquals(expectedResult, result, assertMessage);
    }
}