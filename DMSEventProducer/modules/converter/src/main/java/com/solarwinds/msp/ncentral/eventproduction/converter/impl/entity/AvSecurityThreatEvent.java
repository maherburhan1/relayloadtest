package com.solarwinds.msp.ncentral.eventproduction.converter.impl.entity;

import com.solarwinds.msp.ncentral.eventproduction.api.entity.Event;
import com.solarwinds.msp.ncentral.proto.entity.tasks.AvSecurityThreatOuterClass;

import java.util.Collections;
import java.util.List;

/**
 * This class handles additional business logic for processing data for the {@link
 * AvSecurityThreatOuterClass.AvSecurityThreat} data Protocol Buffers entity.
 */
class AvSecurityThreatEvent implements EntityParser<AvSecurityThreatOuterClass.AvSecurityThreat> {

    private static final String ENTITY_TYPE_EVENT_DATA_MALWARE = "eventdata_malware";
    private static final String NAME_AV_DEFENDER = "AV Defender";

    @Override
    public List<AvSecurityThreatOuterClass.AvSecurityThreat> parseRecord(Event event,
            AvSecurityThreatOuterClass.AvSecurityThreat messageEntity) {
        if (ENTITY_TYPE_EVENT_DATA_MALWARE.equalsIgnoreCase(event.getEntityType())) {
            return Collections.singletonList(messageEntity.toBuilder().setAvSolutionName(NAME_AV_DEFENDER).build());
        }
        return Collections.singletonList(messageEntity);
    }
}
