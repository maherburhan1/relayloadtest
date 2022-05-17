package com.solarwinds.msp.ncentral.eventproduction.converter.impl.entity;

import com.solarwinds.msp.ncentral.eventproduction.api.entity.Event;
import com.solarwinds.msp.ncentral.eventproduction.converter.EntityParserHelper;
import com.solarwinds.msp.ncentral.proto.entity.tasks.IncidentTicketsOuterClass;

import java.util.Collections;
import java.util.List;

/**
 * This class handles additional business logic for processing data for the {@link
 * IncidentTicketsOuterClass.IncidentTickets} data Protocol Buffers entity.
 */
class IncidentTicketsEvent implements EntityParser<IncidentTicketsOuterClass.IncidentTickets> {

    @Override
    public List<IncidentTicketsOuterClass.IncidentTickets> parseRecord(Event event,
            IncidentTicketsOuterClass.IncidentTickets messageEntity) {
        return EntityParserHelper.validateTicketId(event) ? Collections.singletonList(messageEntity) :
                Collections.emptyList();
    }
}
