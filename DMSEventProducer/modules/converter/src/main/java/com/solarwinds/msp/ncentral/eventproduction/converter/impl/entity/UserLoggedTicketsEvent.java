package com.solarwinds.msp.ncentral.eventproduction.converter.impl.entity;

import com.solarwinds.msp.ncentral.eventproduction.api.entity.Event;
import com.solarwinds.msp.ncentral.eventproduction.converter.EntityParserHelper;
import com.solarwinds.msp.ncentral.proto.entity.tasks.UserLoggedTicketsOuterClass;

import java.util.Collections;
import java.util.List;

/**
 * This class handles additional business logic for processing data for the {@link
 * UserLoggedTicketsOuterClass.UserLoggedTickets} data Protocol Buffers entity.
 */
class UserLoggedTicketsEvent implements EntityParser<UserLoggedTicketsOuterClass.UserLoggedTickets> {

    @Override
    public List<UserLoggedTicketsOuterClass.UserLoggedTickets> parseRecord(Event event,
            UserLoggedTicketsOuterClass.UserLoggedTickets messageEntity) {
        return EntityParserHelper.validateTicketId(event) ? Collections.singletonList(messageEntity) :
                Collections.emptyList();
    }
}
