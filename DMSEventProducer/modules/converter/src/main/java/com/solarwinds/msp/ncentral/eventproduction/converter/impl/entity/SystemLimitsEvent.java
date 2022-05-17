package com.solarwinds.msp.ncentral.eventproduction.converter.impl.entity;

import com.solarwinds.msp.ncentral.eventproduction.api.entity.Event;
import com.solarwinds.msp.ncentral.eventproduction.api.entity.Tools;
import com.solarwinds.msp.ncentral.proto.entity.entity.SystemLimitsOuterClass;

import java.util.Collections;
import java.util.List;

/**
 * This class handles additional business logic for processing data for the {@link SystemLimitsOuterClass.SystemLimits}
 * data Protocol Buffers entity.
 */
class SystemLimitsEvent implements EntityParser<SystemLimitsOuterClass.SystemLimits> {

    @Override
    public List<SystemLimitsOuterClass.SystemLimits> parseRecord(Event event,
            SystemLimitsOuterClass.SystemLimits messageEntity) {
        return Collections.singletonList(messageEntity.toBuilder().setLastUpdated(Tools.getNowTimestamp()).build());
    }
}
