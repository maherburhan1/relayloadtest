package com.solarwinds.msp.ncentral.eventproduction.converter.impl.entity;


import com.solarwinds.msp.ncentral.eventproduction.api.entity.Event;
import com.solarwinds.msp.ncentral.proto.entity.entity.AccessGroupOuterClass;
import com.solarwinds.msp.ncentral.proto.entity.entity.AccessGroupOuterClass.AccessGroup;
import com.solarwinds.msp.ncentral.proto.entity.entity.AccessGroupTypeOuterClass;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.solarwinds.msp.ncentral.eventproduction.api.entity.Tools.getInteger;

/**
 * This class handles additional business logic for processing data for the {@link
 * AccessGroupOuterClass.AccessGroup} data Protocol Buffers entity.
 */
class AccessGroupEvent implements EntityParser<AccessGroupOuterClass.AccessGroup> {

    private static final String ATTRIBUTE_GROUP_TYPE = "grouptype";
    private static final Integer GROUP_TYPE_CUSTOMER = 1;

    @Override
    public List<AccessGroup> parseRecord(Event event, AccessGroup messageEntity) {
        final Map<String, String> entity = event.getEntity();
        final Integer groupType = getInteger(entity, ATTRIBUTE_GROUP_TYPE);
        final AccessGroupTypeOuterClass.AccessGroupType accessGroupType;
        if (GROUP_TYPE_CUSTOMER.equals(groupType)) {
            accessGroupType = AccessGroupTypeOuterClass.AccessGroupType.CLIENT;
        } else {
            accessGroupType = AccessGroupTypeOuterClass.AccessGroupType.DEVICE;
        }

        return Collections.singletonList(messageEntity.toBuilder().setGroupType(accessGroupType).build());
    }
}
