package com.solarwinds.msp.ncentral.eventproduction.converter.impl.entity;

import com.google.protobuf.Timestamp;

import com.nable.logging.Logger;
import com.nable.logging.Loggers;
import com.solarwinds.msp.ncentral.eventproduction.api.entity.ClassTypes;
import com.solarwinds.msp.ncentral.eventproduction.api.entity.Event;
import com.solarwinds.msp.ncentral.eventproduction.api.entity.Tools;
import com.solarwinds.msp.ncentral.proto.entity.tasks.BackupOuterClass;
import com.solarwinds.util.time.ZonedDateTimeParser;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * This class handles additional business logic for processing data for the {@link BackupOuterClass.Backup} data
 * Protocol Buffers entity.
 */
class BackupEvent implements EntityParser<BackupOuterClass.Backup> {

    private static final Logger logger = Loggers.EVENT_PRODUCER;

    // TODO Split this huge "God Method" to smaller methods with clear and readable business logic.
    // TODO Define and use constants instead of duplicating strings and numbers.
    // TODO It is out of the scope of the NCCF-12175 bug.
    @Override
    public List<BackupOuterClass.Backup> parseRecord(Event event, BackupOuterClass.Backup messageEntity) {
        Map<String, String> entity = event.getEntity();

        Integer backupStatus = null;
        Float byteRate = null;
        Float totalBytesProcessed = null;
        String errorStatus = null;
        Timestamp jobStart = null;
        Timestamp jobEnd = null;
        Long duration;
        boolean hasErrorMessage;
        String errorMessage;
        String jobId;
        String backupType = null;

        Map<String, List<String>> backupStatuses = new HashMap<>();
        backupStatuses.put("3", new ArrayList<>(Arrays.asList("19", "Success", "Job terminated with success.")));
        backupStatuses.put("4", new ArrayList<>(
                Arrays.asList("3", "Warning", "Job terminated with success but there were some exceptions.")));
        backupStatuses.put("5", new ArrayList<>(Arrays.asList("6", "Failed", "Job terminated with an error.")));
        backupStatuses.put("0", new ArrayList<>(Arrays.asList("19", "Success", "Job terminated with success.")));
        backupStatuses.put("1", new ArrayList<>(
                Arrays.asList("3", "Warning", "Job terminated with success but there were some exceptions.")));
        backupStatuses.put("2", new ArrayList<>(Arrays.asList("6", "Failed", "Job terminated with an error.")));
        backupStatuses.put("Success", new ArrayList<>(Arrays.asList("19", "Success", "Job terminated with success.")));
        backupStatuses.put("Failure", new ArrayList<>(Arrays.asList("6", "Failed", "Job terminated with an error.")));
        backupStatuses.put("Warning", new ArrayList<>(
                Arrays.asList("3", "Warning", "Job terminated with success but there were some exceptions.")));
        backupStatuses.put("Failed", new ArrayList<>(Arrays.asList("6", "Failed", "Job terminated with an error.")));
        backupStatuses.put("Aborted",
                new ArrayList<>(Arrays.asList("106", "Aborted", "The job canceled and has finished processing.")));
        backupStatuses.put("Failedqueued", new ArrayList<>(
                Arrays.asList("108", "FailedQueued", "The job executed, failed and will run next time.")));
        backupStatuses.put("Completed",
                new ArrayList<>(Arrays.asList("109", "Completed", "The job executed and finished successfully.")));

        switch (event.getEntityType()) {
            case "databackupexec_detailed":
                backupStatus = Tools.getInteger(entity, "job_stat");
                errorMessage = entity.getOrDefault("errormessage", null);
                backupType = "Backup Exec";
                if (errorMessage != null) {
                    logger.info("{} event has backup with errors. Ignoring.", event.getEntityType());
                    return null;
                }
                break;
            case "databackupdetails_detailed":
                backupStatus = Tools.getInteger(entity, "backup_status");
                totalBytesProcessed = Optional.ofNullable(Tools.getFloat(entity, "protected_size")).orElse(0F);
                duration = Optional.ofNullable(Tools.getLong(entity, "backup_duration")).orElse(1L);
                duration = (0 == duration) ? 1 : duration;
                byteRate = totalBytesProcessed / duration;
                jobStart = Tools.toTimestamp(ZonedDateTimeParser.parseDateTime(entity, "last_backup"));
                jobEnd = (jobStart != null) ? Tools.toTimestamp(
                        ZonedDateTimeParser.parseDateTime(entity, "last_backup").plusSeconds(duration)) : null;
                Timestamp scanTime = Tools.toTimestamp(ZonedDateTimeParser.parseDateTime(entity, "scantime"));
                backupType = "Asigra/Xilocore Backup";
                if (jobStart == null || scanTime == null) {
                    logger.info("{} event has backup with errors. Ignoring.", event.getEntityType());
                    return null;
                }

                break;
            case "databackupmanagerevents_detailed":
                backupStatus = Tools.getInteger(entity, "event_status");
                errorStatus = StringUtils.isBlank(entity.getOrDefault("errormessage", null)) ?
                        entity.getOrDefault("event_status_desc", null) : entity.getOrDefault("errormessage", null);
                jobStart = Tools.toTimestamp(ZonedDateTimeParser.parseDateTime(entity, "event_job_date"));
                jobEnd = Tools.toTimestamp(ZonedDateTimeParser.parseDateTime(entity, "event_job_date"));
                backupType = "Backup Manager";
                if (jobStart == null) {
                    logger.info("{} event has backup with errors. Ignoring.", event.getEntityType());
                    return null;
                }
                break;
            case "datanablebackupdetails_detailed":
                backupStatus = Tools.getInteger(entity, "nable_backup_status");
                totalBytesProcessed = Optional.ofNullable(Tools.getFloat(entity, "nable_backup_size")).orElse(0F);
                duration = Optional.ofNullable(Tools.getLong(entity, "nable_backup_duration")).orElse(1L);
                duration = (0 == duration) ? 1 : duration;
                byteRate = totalBytesProcessed / duration;
                jobStart = Tools.toTimestamp(ZonedDateTimeParser.parseDateTime(entity, "nable_backup_start_time"));
                jobEnd = (jobStart != null) ? Tools.toTimestamp(
                        ZonedDateTimeParser.parseDateTime(entity, "nable_backup_start_time").plusSeconds(duration)) :
                        null;
                hasErrorMessage = StringUtils.isNotBlank(entity.getOrDefault("errormessage", null));
                backupType = "Storagecraft";
                if (hasErrorMessage || jobStart == null) {
                    logger.info("{} event has backup with errors. Ignoring.", event.getEntityType());
                    return null;
                }
                break;
            case "datamaxbackupstatus_detailed":
                backupStatus = Tools.getInteger(entity, "max_backup_status");
                totalBytesProcessed = Optional.ofNullable(Tools.getFloat(entity, "max_processed_size")).orElse(0F);
                duration = Optional.ofNullable(Tools.getLong(entity, "max_session_duration")).orElse(1L);
                duration = (0 == duration) ? 1 : duration;
                byteRate = totalBytesProcessed / duration;
                jobEnd = Tools.toTimestamp(ZonedDateTimeParser.parseDateTime(entity, "max_backup_end_time"));
                jobStart = (jobEnd != null) ? Tools.toTimestamp(
                        ZonedDateTimeParser.parseDateTime(entity, "max_backup_end_time").minusSeconds(duration)) : null;
                hasErrorMessage = StringUtils.isNotBlank(entity.getOrDefault("errormessage", null));
                Boolean backupInProgress = Tools.getBoolean(entity, "max_backup_inprogress");
                String maxPlugin = entity.getOrDefault("max_plugin", null);
                backupType = "MSP Backup";
                if (hasErrorMessage || jobStart == null || backupInProgress || "plugintotal".equalsIgnoreCase(
                        maxPlugin)) {
                    logger.info("{} event has backup with errors. Ignoring.", event.getEntityType());
                    return null;
                }
                break;
            case "datastwindowsbackup_detailed":
                backupStatus = Tools.getInteger(entity, "ampwblasterrorstatus");
                errorStatus = StringUtils.isBlank(entity.getOrDefault("errormessage", null)) ?
                        entity.getOrDefault("ampwblasterrordescription", null) :
                        entity.getOrDefault("errormessage", null);
                jobStart = Tools.toTimestamp(ZonedDateTimeParser.parseDateTime(entity, "ampwblastbackuptime"));
                jobEnd = Tools.toTimestamp(ZonedDateTimeParser.parseDateTime(entity, "ampwblastbackuptime"));
                backupType = "Windows Backup";
                if (jobStart == null) {
                    logger.info("{} event has backup with errors. Ignoring.", event.getEntityType());
                    return null;
                }
                break;
            case "datastveeamjobmonitor_detailed":
                backupStatus = Tools.getInteger(entity, "ampvjmjobresultstatus");
                totalBytesProcessed =
                        (Optional.ofNullable(Tools.getFloat(entity, "ampvjmbackupsize")).orElse(0F)) * 1000;
                duration = Optional.ofNullable(Tools.getLong(entity, "ampvjmduration")).orElse(1L);
                duration = (0 == duration) ? 1 : (duration * 60);
                byteRate = totalBytesProcessed / duration;
                errorStatus = entity.getOrDefault("errormessage", null);
                jobStart = Tools.toTimestamp(ZonedDateTimeParser.parseDateTime(entity, "ampvjmstarttime"));
                jobEnd = Tools.toTimestamp(ZonedDateTimeParser.parseDateTime(entity, "ampvjmendtime"));
                jobId = entity.getOrDefault("ampvjmjobid", null);
                backupType = "Veeam Backup";
                if (jobStart == null || jobEnd == null || jobId == null) {
                    logger.info("{} event has backup with errors. Ignoring.", event.getEntityType());
                    return null;
                }
                break;
            case "datastveeamsuremonitor_detailed":
                backupStatus = Tools.getInteger(entity, "ampvsjmjobresultstatus");
                errorStatus = entity.getOrDefault("errormessage", null);
                jobStart = Tools.toTimestamp(ZonedDateTimeParser.parseDateTime(entity, "ampvsjmjobstart"));
                jobEnd = Tools.toTimestamp(ZonedDateTimeParser.parseDateTime(entity, "ampvsjmjobendtime"));
                jobId = entity.getOrDefault("ampvsjmjobid", null);
                backupType = "Veeam Sure Backup";
                if (jobStart == null || jobEnd == null || jobId == null) {
                    logger.info("{} event has backup with errors. Ignoring.", event.getEntityType());
                    return null;
                }
                break;
            default:
        }

        Map<String, Object> statusValues = new HashMap<>();
        if (backupStatus != null) {
            statusValues.put("setStatusId",
                    Tools.parseObject(ClassTypes.INTEGER, backupStatuses.get(backupStatus.toString()).get(0)));
            statusValues.put("setName", backupStatuses.get(backupStatus.toString()).get(1));
            statusValues.put("setDescription", backupStatuses.get(backupStatus.toString()).get(2));
        }

        BackupOuterClass.Backup.BackupStatus backupStatusEntity =
                Tools.setNullableField(BackupOuterClass.Backup.BackupStatus.newBuilder(), statusValues).build();

        Map<String, Object> entityValues = new HashMap<>();
        entityValues.put("setByteRate", byteRate);
        entityValues.put("setTotalBytesProcessed", totalBytesProcessed);
        entityValues.put("setErrorStatus", errorStatus);
        entityValues.put("setJobStart", jobStart);
        entityValues.put("setJobEnd", jobEnd);
        entityValues.put("setBackupStatus", backupStatusEntity);
        entityValues.put("setBackupType", backupType);

        return Collections.singletonList(Tools.setNullableField(messageEntity.toBuilder(), entityValues).build());
    }
}
