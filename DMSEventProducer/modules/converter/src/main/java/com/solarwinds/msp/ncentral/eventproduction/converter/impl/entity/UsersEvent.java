package com.solarwinds.msp.ncentral.eventproduction.converter.impl.entity;

import com.solarwinds.msp.ncentral.eventproduction.api.entity.Event;
import com.solarwinds.msp.ncentral.eventproduction.converter.EntityParserHelper;
import com.solarwinds.msp.ncentral.proto.entity.entity.ContactOuterClass;
import com.solarwinds.msp.ncentral.proto.entity.entity.UsersOuterClass;

import java.util.Collections;
import java.util.List;

/**
 * This class handles additional business logic for processing data for the {@link UsersOuterClass.Users} data Protocol
 * Buffers entity.
 */
class UsersEvent implements EntityParser<UsersOuterClass.Users> {

    private static final String ATTRIBUTE_FIRST_NAME = "firstname";
    private static final String ATTRIBUTE_LAST_NAME = "lastname";
    private static final int CONTACT_INDEX = 0;

    @Override
    public List<UsersOuterClass.Users> parseRecord(Event event, UsersOuterClass.Users messageEntity) {
        final ContactOuterClass.Contact mainContactEntity =
                EntityParserHelper.parseContact(event.getEntity(), ATTRIBUTE_FIRST_NAME, ATTRIBUTE_LAST_NAME,
                        messageEntity.getContact(CONTACT_INDEX));
        return Collections.singletonList(
                messageEntity.toBuilder().setContact(CONTACT_INDEX, mainContactEntity).build());
    }
}
