package com.solarwinds.msp.ncentral.eventproduction.converter.impl.entity;

import com.google.protobuf.GeneratedMessageV3;
import com.google.protobuf.Timestamp;

import com.solarwinds.msp.ncentral.eventproduction.api.entity.Event;
import com.solarwinds.msp.ncentral.eventproduction.api.entity.EventType;
import com.solarwinds.msp.ncentral.eventproduction.converter.EventParser;
import com.solarwinds.msp.ncentral.proto.entity.ActionOuterClass;
import com.solarwinds.msp.ncentral.proto.entity.MspContextOuterClass;
import com.solarwinds.msp.ncentral.proto.entity.entity.PatchCategoryOuterClass;
import com.solarwinds.msp.ncentral.proto.entity.entity.PatchOuterClass;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

class PatchEventTest {

    private static Stream<Arguments> parseRecordTest() {
        EventParserTestCases testCases = new EventParserTestCases();
        PatchOuterClass.Patch expectedResult;
        Event incomingEvent;
        String parsingAssertMessage;

        Map<String, String> patchEntity = new HashMap<>();
        patchEntity.put("lastupdated", "2019-02-21T13:51:52.068Z");
        patchEntity.put("patchguid", "06b10fef-59d6-4fff-a85b-44a5b73abb61");

        //<editor-fold desc="Valid patch event">
        parsingAssertMessage = "Valid patch event";
        Map<String, String> patchEntity1 = new HashMap<>(patchEntity);
        patchEntity1.put("kbnumbers", "2267602");
        patchEntity1.put("title",
                "Definition Update for Windows Defender Antivirus - KB2267602 (Definition 1.287.394.0)");
        patchEntity1.put("description",
                "Install this update to revise the definition files that are used to detect viruses, spyware, and other potentially unwanted software. Once you have installed this item, it cannot be removed.");
        patchEntity1.put("products", "8c3fcc84-7410-4a95-8b89-a166a0190486");
        patchEntity1.put("infourls", "https://go.microsoft.com/fwlink/?linkid=2007160");
        LocalDate publishedDate = LocalDate.of(2019, 2, 20);
        patchEntity1.put("publisheddate", publishedDate.toString());
        patchEntity1.put("classification", "e0789628-ce08-4437-be74-2495b842f43b");
        patchEntity1.put("severity", "Unspecified");
        patchEntity1.put("uninstallsupported", "false");
        patchEntity1.put("eula", "null");
        patchEntity1.put("deleted", "false");
        patchEntity1.put("superseded", "false");
        patchEntity1.put("removable", "false");
        patchEntity1.put("restartbehavior", "Never Reboots");
        patchEntity1.put("mayrequestuserinput", "false");
        patchEntity1.put("mustbeinstalledexclusively", "false");
        patchEntity1.put("updatessupersededbythisupdate",
                "80391e32-65de-4b9b-a6a0-8be57c1ac460,78e5cdc4-d1e8-4910-937b-d7067f190a5c,3343382a-6720-4660-8126-55fd97597a01,089c397f-3fb1-478d-8fc6-47d9cba7a134,714b54af-b251-44eb-b457-181a03b25a0d,b06fcc4d-3d31-4a11-a703-33e7ca90031c");
        patchEntity1.put("updatessupersedingthisupdate", "null");
        patchEntity1.put("languagessupported", "null");
        patchEntity1.put("patchcontenthash", "c3e3862e");

        incomingEvent = Event.builder()
                .entityDataTypes(Collections.EMPTY_MAP)
                .bizappsCustomerId("aaea6111-b13f-462b-9385-4f3baa7f0ccc")
                .ncentralServerGuid("225ed5bb-9e8e-4ad5-aa8c-7a018dc5ffa5")
                .eventingConfigurationCustomerId(1)
                .professionalModeLicenseType("Per Device")
                .eventType(EventType.INSERT)
                .entityType("patch")
                .entity(patchEntity1)
                .newValues(Collections.emptyMap())
                .build();

        expectedResult = PatchOuterClass.Patch.newBuilder()
                .setPatchGuid("06b10fef-59d6-4fff-a85b-44a5b73abb61")
                .setName("Definition Update for Windows Defender Antivirus - KB2267602 (Definition 1.287.394.0)")
                .setDescription(
                        "Install this update to revise the definition files that are used to detect viruses, spyware, and other potentially unwanted software. Once you have installed this item, it cannot be removed.")
                .setKbNumber("2267602")
                .setInfoUrls("https://go.microsoft.com/fwlink/?linkid=2007160")
                .setProducts("8c3fcc84-7410-4a95-8b89-a166a0190486")
                .setSeverity(PatchOuterClass.Patch.Severity.UNSPECIFIED)
                .setPublishDate(Timestamp.newBuilder()
                        .setSeconds(Instant.from(publishedDate.atStartOfDay().atZone(ZoneId.systemDefault()))
                                .getEpochSecond())
                        .build())
                .setPatchCategory(PatchCategoryOuterClass.PatchCategory.newBuilder()
                        .setSourceId("e0789628-ce08-4437-be74-2495b842f43b")
                        .build())
                .setAction(ActionOuterClass.Action.ADD)
                .setContext(MspContextOuterClass.MspContext.newBuilder()
                        .setBizAppsCustomerId("aaea6111-b13f-462b-9385-4f3baa7f0ccc")
                        .setSystemGuid("225ed5bb-9e8e-4ad5-aa8c-7a018dc5ffa5")
                        .build())
                .build();

        testCases.setTestCase(incomingEvent, expectedResult, parsingAssertMessage);
        //</editor-fold>

        //<editor-fold desc="Patch event missing patch GUID">
        parsingAssertMessage = "Patch event missing patch GUID";
        Map<String, String> patchEntity2 = new HashMap<>(patchEntity);
        patchEntity2.remove("patchguid");
        incomingEvent = Event.builder()
                .entityDataTypes(Collections.EMPTY_MAP)
                .bizappsCustomerId("aaea6111-b13f-462b-9385-4f3baa7f0ccc")
                .ncentralServerGuid("225ed5bb-9e8e-4ad5-aa8c-7a018dc5ffa5")
                .eventingConfigurationCustomerId(1)
                .professionalModeLicenseType("Per Device")
                .eventType(EventType.INSERT)
                .entityType("patch")
                .entity(patchEntity2)
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