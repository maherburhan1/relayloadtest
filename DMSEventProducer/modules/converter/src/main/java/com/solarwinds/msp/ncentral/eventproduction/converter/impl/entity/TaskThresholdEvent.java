package com.solarwinds.msp.ncentral.eventproduction.converter.impl.entity;

import com.solarwinds.msp.ncentral.eventproduction.api.entity.Event;
import com.solarwinds.msp.ncentral.eventproduction.api.entity.Tools;
import com.solarwinds.msp.ncentral.proto.entity.tasks.TaskThresholdOuterClass;

import java.util.Collections;
import java.util.List;

/**
 * This class handles additional business logic for processing data for the {@link
 * TaskThresholdOuterClass.TaskThreshold} data Protocol Buffers entity.
 */
class TaskThresholdEvent implements EntityParser<TaskThresholdOuterClass.TaskThreshold> {

    @Override
    public List<TaskThresholdOuterClass.TaskThreshold> parseRecord(Event event,
            TaskThresholdOuterClass.TaskThreshold messageEntity) {
        return Collections.singletonList(messageEntity.toBuilder().setLastUpdated(Tools.getNowTimestamp()).build());
    }
}
