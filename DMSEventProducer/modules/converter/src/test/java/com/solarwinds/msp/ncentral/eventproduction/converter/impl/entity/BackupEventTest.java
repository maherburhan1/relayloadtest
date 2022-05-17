package com.solarwinds.msp.ncentral.eventproduction.converter.impl.entity;

import com.google.protobuf.GeneratedMessageV3;
import com.google.protobuf.Timestamp;

import com.solarwinds.msp.ncentral.eventproduction.api.entity.Event;
import com.solarwinds.msp.ncentral.eventproduction.api.entity.EventType;
import com.solarwinds.msp.ncentral.eventproduction.converter.EventParser;
import com.solarwinds.msp.ncentral.proto.entity.ActionOuterClass;
import com.solarwinds.msp.ncentral.proto.entity.MspContextOuterClass;
import com.solarwinds.msp.ncentral.proto.entity.tasks.BackupOuterClass;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

class BackupEventTest {

    private static Stream<Arguments> parseRecordTest() {
        EventParserTestCases testCases = new EventParserTestCases();
        BackupOuterClass.Backup expectedResult;
        Event incomingEvent;
        String parsingAssertMessage;

        Map<String, String> backupEntity = new HashMap<>();
        backupEntity.put("taskid", "1907480434");
        backupEntity.put("scantime", "2019-02-21T13:51:52.068Z");
        backupEntity.put("lastupdated", "2019-02-21T13:51:52.068Z");
        backupEntity.put("state", "3");

        //<editor-fold desc="Valid Backup Exec event">
        parsingAssertMessage = "Valid Backup Exec event";
        Map<String, String> backupEntity1 = new HashMap<>(backupEntity);
        backupEntity1.put("job_stat", "2");
        backupEntity1.put("byte_rate", "100");
        backupEntity1.put("total_bytes", "100");
        backupEntity1.put("err_stat", null);
        backupEntity1.put("job_name", "Backup Exec job");
        backupEntity1.put("start_time", "2019-02-21T13:51:52.068Z");
        backupEntity1.put("end_time", "2019-02-21T13:51:52.068Z");
        incomingEvent = Event.builder()
                .entityDataTypes(Collections.EMPTY_MAP)
                .bizappsCustomerId("aaea6111-b13f-462b-9385-4f3baa7f0ccc")
                .ncentralServerGuid("225ed5bb-9e8e-4ad5-aa8c-7a018dc5ffa5")
                .eventingConfigurationCustomerId(1)
                .professionalModeLicenseType("Per Device")
                .eventType(EventType.INSERT)
                .entityType("databackupexec_detailed")
                .entity(backupEntity1)
                .newValues(Collections.emptyMap())
                .build();

        expectedResult = BackupOuterClass.Backup.newBuilder()
                .setTaskId(1907480434)
                .setStateId(3)
                .setBackupStatus(BackupOuterClass.Backup.BackupStatus.newBuilder()
                        .setStatusId(6)
                        .setName("Failed")
                        .setDescription("Job terminated with an error.")
                        .build())
                .setByteRate(100)
                .setTotalBytesProcessed(100)
                .setDescription("Backup Exec job")
                .setBackupType("Backup Exec")
                .setJobStart(Timestamp.newBuilder().setSeconds(1550757112).setNanos(68000000).build())
                .setJobEnd(Timestamp.newBuilder().setSeconds(1550757112).setNanos(68000000).build())
                .setLastUpdated(Timestamp.newBuilder().setSeconds(1550757112).setNanos(68000000).build())
                .setAction(ActionOuterClass.Action.ADD)
                .setContext(MspContextOuterClass.MspContext.newBuilder()
                        .setBizAppsCustomerId("aaea6111-b13f-462b-9385-4f3baa7f0ccc")
                        .setSystemGuid("225ed5bb-9e8e-4ad5-aa8c-7a018dc5ffa5")
                        .build())
                .build();

        testCases.setTestCase(incomingEvent, expectedResult, parsingAssertMessage);
        //</editor-fold>

        //<editor-fold desc="Valid Asigra/Xilocore Backup event">
        parsingAssertMessage = "Valid Asigra/Xilocore Backup event";
        Map<String, String> backupEntity2 = new HashMap<>(backupEntity);
        backupEntity2.put("backup_status", "2");
        backupEntity2.put("protected_size", "100");
        backupEntity2.put("backup_duration", "0");
        backupEntity2.put("backup_status_desc", "Successful Backup");
        backupEntity2.put("last_backup", "2019-02-21T13:51:52.068Z");
        incomingEvent = Event.builder()
                .entityDataTypes(Collections.EMPTY_MAP)
                .bizappsCustomerId("aaea6111-b13f-462b-9385-4f3baa7f0ccc")
                .ncentralServerGuid("225ed5bb-9e8e-4ad5-aa8c-7a018dc5ffa5")
                .eventingConfigurationCustomerId(1)
                .professionalModeLicenseType("Per Device")
                .eventType(EventType.INSERT)
                .entityType("databackupdetails_detailed")
                .entity(backupEntity2)
                .newValues(Collections.emptyMap())
                .build();

        expectedResult = BackupOuterClass.Backup.newBuilder()
                .setTaskId(1907480434)
                .setStateId(3)
                .setBackupStatus(BackupOuterClass.Backup.BackupStatus.newBuilder()
                        .setStatusId(6)
                        .setName("Failed")
                        .setDescription("Job terminated with an error.")
                        .build())
                .setByteRate(100)
                .setTotalBytesProcessed(100)
                .setErrorStatus("Successful Backup")
                .setBackupType("Asigra/Xilocore Backup")
                .setJobStart(Timestamp.newBuilder().setSeconds(1550757112).setNanos(68000000).build())
                .setJobEnd(Timestamp.newBuilder().setSeconds(1550757113).setNanos(68000000).build())
                .setLastUpdated(Timestamp.newBuilder().setSeconds(1550757112).setNanos(68000000).build())
                .setAction(ActionOuterClass.Action.ADD)
                .setContext(MspContextOuterClass.MspContext.newBuilder()
                        .setBizAppsCustomerId("aaea6111-b13f-462b-9385-4f3baa7f0ccc")
                        .setSystemGuid("225ed5bb-9e8e-4ad5-aa8c-7a018dc5ffa5")
                        .build())
                .build();

        testCases.setTestCase(incomingEvent, expectedResult, parsingAssertMessage);
        //</editor-fold>

        //<editor-fold desc="Valid D2D Backup Manager event">
        parsingAssertMessage = "Valid D2D Backup Manager event";
        Map<String, String> backupEntity3 = new HashMap<>(backupEntity);
        backupEntity3.put("event_status", "2");
        backupEntity3.put("event_job_size", "100");
        backupEntity3.put("errormessage", null);
        backupEntity3.put("event_status_desc", "Successful Backup");
        backupEntity3.put("event_job_name", "D2D Backup Manager job");
        backupEntity3.put("event_job_date", "2019-02-21T13:51:52.068Z");
        incomingEvent = Event.builder()
                .entityDataTypes(Collections.EMPTY_MAP)
                .bizappsCustomerId("aaea6111-b13f-462b-9385-4f3baa7f0ccc")
                .ncentralServerGuid("225ed5bb-9e8e-4ad5-aa8c-7a018dc5ffa5")
                .eventingConfigurationCustomerId(1)
                .professionalModeLicenseType("Per Device")
                .eventType(EventType.INSERT)
                .entityType("databackupmanagerevents_detailed")
                .entity(backupEntity3)
                .newValues(Collections.emptyMap())
                .build();

        expectedResult = BackupOuterClass.Backup.newBuilder()
                .setTaskId(1907480434)
                .setStateId(3)
                .setBackupStatus(BackupOuterClass.Backup.BackupStatus.newBuilder()
                        .setStatusId(6)
                        .setName("Failed")
                        .setDescription("Job terminated with an error.")
                        .build())
                .setTotalBytesProcessed(100)
                .setErrorStatus("Successful Backup")
                .setDescription("D2D Backup Manager job")
                .setBackupType("Backup Manager")
                .setJobStart(Timestamp.newBuilder().setSeconds(1550757112).setNanos(68000000).build())
                .setJobEnd(Timestamp.newBuilder().setSeconds(1550757112).setNanos(68000000).build())
                .setLastUpdated(Timestamp.newBuilder().setSeconds(1550757112).setNanos(68000000).build())
                .setAction(ActionOuterClass.Action.ADD)
                .setContext(MspContextOuterClass.MspContext.newBuilder()
                        .setBizAppsCustomerId("aaea6111-b13f-462b-9385-4f3baa7f0ccc")
                        .setSystemGuid("225ed5bb-9e8e-4ad5-aa8c-7a018dc5ffa5")
                        .build())
                .build();

        testCases.setTestCase(incomingEvent, expectedResult, parsingAssertMessage);
        //</editor-fold>

        //<editor-fold desc="Valid Storagecraft event">
        parsingAssertMessage = "Valid Storagecraft event";
        Map<String, String> backupEntity4 = new HashMap<>(backupEntity);
        backupEntity4.put("nable_backup_status", "2");
        backupEntity4.put("nable_backup_size", "100");
        backupEntity4.put("nable_backup_duration", "0");
        backupEntity4.put("nable_backup_status_detail", "Successful Backup");
        backupEntity4.put("nable_backup_start_time", "2019-02-21T13:51:52.068Z");
        incomingEvent = Event.builder()
                .entityDataTypes(Collections.EMPTY_MAP)
                .bizappsCustomerId("aaea6111-b13f-462b-9385-4f3baa7f0ccc")
                .ncentralServerGuid("225ed5bb-9e8e-4ad5-aa8c-7a018dc5ffa5")
                .eventingConfigurationCustomerId(1)
                .professionalModeLicenseType("Per Device")
                .eventType(EventType.INSERT)
                .entityType("datanablebackupdetails_detailed")
                .entity(backupEntity4)
                .newValues(Collections.emptyMap())
                .build();

        expectedResult = BackupOuterClass.Backup.newBuilder()
                .setTaskId(1907480434)
                .setStateId(3)
                .setBackupStatus(BackupOuterClass.Backup.BackupStatus.newBuilder()
                        .setStatusId(6)
                        .setName("Failed")
                        .setDescription("Job terminated with an error.")
                        .build())
                .setByteRate(100)
                .setTotalBytesProcessed(100)
                .setErrorStatus("Successful Backup")
                .setBackupType("Storagecraft")
                .setJobStart(Timestamp.newBuilder().setSeconds(1550757112).setNanos(68000000).build())
                .setJobEnd(Timestamp.newBuilder().setSeconds(1550757113).setNanos(68000000).build())
                .setLastUpdated(Timestamp.newBuilder().setSeconds(1550757112).setNanos(68000000).build())
                .setAction(ActionOuterClass.Action.ADD)
                .setContext(MspContextOuterClass.MspContext.newBuilder()
                        .setBizAppsCustomerId("aaea6111-b13f-462b-9385-4f3baa7f0ccc")
                        .setSystemGuid("225ed5bb-9e8e-4ad5-aa8c-7a018dc5ffa5")
                        .build())
                .build();

        testCases.setTestCase(incomingEvent, expectedResult, parsingAssertMessage);
        //</editor-fold>

        //<editor-fold desc="Valid MaxBackup event">
        parsingAssertMessage = "Valid MaxBackup event";
        Map<String, String> backupEntity5 = new HashMap<>(backupEntity);
        backupEntity5.put("max_backup_status", "2");
        backupEntity5.put("max_plugin", "MaxBackup");
        backupEntity5.put("max_processed_size", "100");
        backupEntity5.put("max_session_duration", "0");
        backupEntity5.put("max_backup_status_desc", "Successful Backup");
        backupEntity5.put("max_backup_end_time", "2019-02-21T13:51:52.068Z");
        incomingEvent = Event.builder()
                .entityDataTypes(Collections.EMPTY_MAP)
                .bizappsCustomerId("aaea6111-b13f-462b-9385-4f3baa7f0ccc")
                .ncentralServerGuid("225ed5bb-9e8e-4ad5-aa8c-7a018dc5ffa5")
                .eventingConfigurationCustomerId(1)
                .professionalModeLicenseType("Per Device")
                .eventType(EventType.INSERT)
                .entityType("datamaxbackupstatus_detailed")
                .entity(backupEntity5)
                .newValues(Collections.emptyMap())
                .build();

        expectedResult = BackupOuterClass.Backup.newBuilder()
                .setTaskId(1907480434)
                .setStateId(3)
                .setBackupStatus(BackupOuterClass.Backup.BackupStatus.newBuilder()
                        .setStatusId(6)
                        .setName("Failed")
                        .setDescription("Job terminated with an error.")
                        .build())
                .setByteRate(100)
                .setTotalBytesProcessed(100)
                .setErrorStatus("Successful Backup")
                .setDescription("MaxBackup")
                .setBackupType("MSP Backup")
                .setJobStart(Timestamp.newBuilder().setSeconds(1550757111).setNanos(68000000).build())
                .setJobEnd(Timestamp.newBuilder().setSeconds(1550757112).setNanos(68000000).build())
                .setLastUpdated(Timestamp.newBuilder().setSeconds(1550757112).setNanos(68000000).build())
                .setAction(ActionOuterClass.Action.ADD)
                .setContext(MspContextOuterClass.MspContext.newBuilder()
                        .setBizAppsCustomerId("aaea6111-b13f-462b-9385-4f3baa7f0ccc")
                        .setSystemGuid("225ed5bb-9e8e-4ad5-aa8c-7a018dc5ffa5")
                        .build())
                .build();

        testCases.setTestCase(incomingEvent, expectedResult, parsingAssertMessage);
        //</editor-fold>

        //<editor-fold desc="Valid Windows Backup event">
        parsingAssertMessage = "Valid Windows Backup event";
        Map<String, String> backupEntity6 = new HashMap<>(backupEntity);
        backupEntity6.put("ampwblasterrorstatus", "2");
        backupEntity6.put("ampwblastbackuptarget", "Windows Backup");
        backupEntity6.put("errormessage", "");
        backupEntity6.put("ampwblasterrordescription", "Successful Backup");
        backupEntity6.put("ampwblastbackuptime", "2019-02-21T13:51:52.068Z");
        incomingEvent = Event.builder()
                .entityDataTypes(Collections.EMPTY_MAP)
                .bizappsCustomerId("aaea6111-b13f-462b-9385-4f3baa7f0ccc")
                .ncentralServerGuid("225ed5bb-9e8e-4ad5-aa8c-7a018dc5ffa5")
                .eventingConfigurationCustomerId(1)
                .professionalModeLicenseType("Per Device")
                .eventType(EventType.INSERT)
                .entityType("datastwindowsbackup_detailed")
                .entity(backupEntity6)
                .newValues(Collections.emptyMap())
                .build();

        expectedResult = BackupOuterClass.Backup.newBuilder()
                .setTaskId(1907480434)
                .setStateId(3)
                .setBackupStatus(BackupOuterClass.Backup.BackupStatus.newBuilder()
                        .setStatusId(6)
                        .setName("Failed")
                        .setDescription("Job terminated with an error.")
                        .build())
                .setErrorStatus("Successful Backup")
                .setDescription("Windows Backup")
                .setBackupType("Windows Backup")
                .setJobStart(Timestamp.newBuilder().setSeconds(1550757112).setNanos(68000000).build())
                .setJobEnd(Timestamp.newBuilder().setSeconds(1550757112).setNanos(68000000).build())
                .setLastUpdated(Timestamp.newBuilder().setSeconds(1550757112).setNanos(68000000).build())
                .setAction(ActionOuterClass.Action.ADD)
                .setContext(MspContextOuterClass.MspContext.newBuilder()
                        .setBizAppsCustomerId("aaea6111-b13f-462b-9385-4f3baa7f0ccc")
                        .setSystemGuid("225ed5bb-9e8e-4ad5-aa8c-7a018dc5ffa5")
                        .build())
                .build();

        testCases.setTestCase(incomingEvent, expectedResult, parsingAssertMessage);
        //</editor-fold>

        //<editor-fold desc="Valid Veeam event">
        parsingAssertMessage = "Valid Veeam event";
        Map<String, String> backupEntity7 = new HashMap<>(backupEntity);
        backupEntity7.put("ampvjmjobresultstatus", "2");
        backupEntity7.put("ampvjmjobname", "Veeam Backup");
        backupEntity7.put("ampvjmjobid", "Veeam Backup Id");
        backupEntity7.put("ampvjmbackupsize", "100");
        backupEntity7.put("ampvjmduration", "0");
        backupEntity7.put("errormessage", "");
        backupEntity7.put("ampvjmjobresult", "Successful Backup");
        backupEntity7.put("ampvjmstarttime", "2019-02-21T13:51:52.068Z");
        backupEntity7.put("ampvjmendtime", "2019-02-21T13:51:52.068Z");
        incomingEvent = Event.builder()
                .entityDataTypes(Collections.EMPTY_MAP)
                .bizappsCustomerId("aaea6111-b13f-462b-9385-4f3baa7f0ccc")
                .ncentralServerGuid("225ed5bb-9e8e-4ad5-aa8c-7a018dc5ffa5")
                .eventingConfigurationCustomerId(1)
                .professionalModeLicenseType("Per Device")
                .eventType(EventType.INSERT)
                .entityType("datastveeamjobmonitor_detailed")
                .entity(backupEntity7)
                .newValues(Collections.emptyMap())
                .build();

        expectedResult = BackupOuterClass.Backup.newBuilder()
                .setTaskId(1907480434)
                .setStateId(3)
                .setBackupStatus(BackupOuterClass.Backup.BackupStatus.newBuilder()
                        .setStatusId(6)
                        .setName("Failed")
                        .setDescription("Job terminated with an error.")
                        .build())
                .setDescription("Veeam Backup")
                .setByteRate(100000)
                .setTotalBytesProcessed(100000)
                .setBackupType("Veeam Backup")
                .setJobStart(Timestamp.newBuilder().setSeconds(1550757112).setNanos(68000000).build())
                .setJobEnd(Timestamp.newBuilder().setSeconds(1550757112).setNanos(68000000).build())
                .setLastUpdated(Timestamp.newBuilder().setSeconds(1550757112).setNanos(68000000).build())
                .setAction(ActionOuterClass.Action.ADD)
                .setContext(MspContextOuterClass.MspContext.newBuilder()
                        .setBizAppsCustomerId("aaea6111-b13f-462b-9385-4f3baa7f0ccc")
                        .setSystemGuid("225ed5bb-9e8e-4ad5-aa8c-7a018dc5ffa5")
                        .build())
                .build();

        testCases.setTestCase(incomingEvent, expectedResult, parsingAssertMessage);
        //</editor-fold>

        //<editor-fold desc="Valid SureVeeam event">
        parsingAssertMessage = "Valid SureVeeam event";
        Map<String, String> backupEntity8 = new HashMap<>(backupEntity);
        backupEntity8.put("ampvsjmjobresultstatus", "2");
        backupEntity8.put("ampvsjmjobname", "SureVeeam Backup");
        backupEntity8.put("ampvsjmjobid", "SureVeeam Backup Id");
        backupEntity8.put("errormessage", "");
        backupEntity8.put("ampvsjmjobresult", "Successful Backup");
        backupEntity8.put("ampvsjmjobstart", "2019-02-21T13:51:52.068Z");
        backupEntity8.put("ampvsjmjobendtime", "2019-02-21T13:51:52.068Z");
        incomingEvent = Event.builder()
                .entityDataTypes(Collections.EMPTY_MAP)
                .bizappsCustomerId("aaea6111-b13f-462b-9385-4f3baa7f0ccc")
                .ncentralServerGuid("225ed5bb-9e8e-4ad5-aa8c-7a018dc5ffa5")
                .eventingConfigurationCustomerId(1)
                .professionalModeLicenseType("Per Device")
                .eventType(EventType.INSERT)
                .entityType("datastveeamsuremonitor_detailed")
                .entity(backupEntity8)
                .newValues(Collections.emptyMap())
                .build();

        expectedResult = BackupOuterClass.Backup.newBuilder()
                .setTaskId(1907480434)
                .setStateId(3)
                .setBackupStatus(BackupOuterClass.Backup.BackupStatus.newBuilder()
                        .setStatusId(6)
                        .setName("Failed")
                        .setDescription("Job terminated with an error.")
                        .build())
                .setDescription("SureVeeam Backup")
                .setBackupType("Veeam Sure Backup")
                .setJobStart(Timestamp.newBuilder().setSeconds(1550757112).setNanos(68000000).build())
                .setJobEnd(Timestamp.newBuilder().setSeconds(1550757112).setNanos(68000000).build())
                .setLastUpdated(Timestamp.newBuilder().setSeconds(1550757112).setNanos(68000000).build())
                .setAction(ActionOuterClass.Action.ADD)
                .setContext(MspContextOuterClass.MspContext.newBuilder()
                        .setBizAppsCustomerId("aaea6111-b13f-462b-9385-4f3baa7f0ccc")
                        .setSystemGuid("225ed5bb-9e8e-4ad5-aa8c-7a018dc5ffa5")
                        .build())
                .build();

        testCases.setTestCase(incomingEvent, expectedResult, parsingAssertMessage);
        //</editor-fold>

        //<editor-fold desc="Backup event with missing task id">
        parsingAssertMessage = "Backup event with missing task id";
        Map<String, String> backupEntity9 = new HashMap<>(backupEntity);
        backupEntity9.remove("taskid");
        incomingEvent = Event.builder()
                .entityDataTypes(Collections.EMPTY_MAP)
                .bizappsCustomerId("aaea6111-b13f-462b-9385-4f3baa7f0ccc")
                .ncentralServerGuid("225ed5bb-9e8e-4ad5-aa8c-7a018dc5ffa5")
                .eventingConfigurationCustomerId(1)
                .professionalModeLicenseType("Per Device")
                .eventType(EventType.INSERT)
                .entityType("datastveeamsuremonitor_detailed")
                .entity(backupEntity9)
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