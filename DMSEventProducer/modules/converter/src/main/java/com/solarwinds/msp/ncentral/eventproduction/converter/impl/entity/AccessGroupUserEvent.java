package com.solarwinds.msp.ncentral.eventproduction.converter.impl.entity;

import com.solarwinds.msp.ncentral.eventproduction.api.entity.Event;
import com.solarwinds.msp.ncentral.proto.entity.entity.AccessGroupUserOuterClass;
import com.solarwinds.msp.ncentral.proto.entity.entity.AccessGroupUserOuterClass.AccessGroupUser;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.solarwinds.msp.ncentral.eventproduction.api.entity.Tools.getInteger;

/**
 * This class handles additional business logic for processing data for the {@link
 * AccessGroupUserOuterClass.AccessGroup} data Protocol Buffers entity.
 */
class AccessGroupUserEvent implements EntityParser<AccessGroupUserOuterClass.AccessGroupUser> {

    private static final String ATTRIBUTE_USER_ID = "userid";

    @Override
    public List<AccessGroupUser> parseRecord(Event event, AccessGroupUser messageEntity) {
        final Map<String, String> entity = event.getEntity();
        final Integer userId = getInteger(entity, ATTRIBUTE_USER_ID);
        if (userId == null) {
            return Collections.emptyList();
        }
        return Collections.singletonList(messageEntity.toBuilder().addUserId(userId).build());
    }
}
