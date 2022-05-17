package com.solarwinds.msp.ncentral.eventproduction.converter.impl.entity;

import com.google.protobuf.Timestamp;

import com.solarwinds.msp.ncentral.eventproduction.api.entity.Event;
import com.solarwinds.msp.ncentral.eventproduction.api.entity.Tools;
import com.solarwinds.msp.ncentral.proto.entity.tasks.PatchActionOuterClass;
import com.solarwinds.util.time.ZonedDateTimeParser;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class handles additional business logic for processing data for the {@link PatchActionOuterClass.PatchAction}
 * data Protocol Buffers entity.
 */
class PatchActionEvent implements EntityParser<PatchActionOuterClass.PatchAction> {

    private static final String ENTITY_TYPE_CIM_PATCH = "cim_patch";
    private static final String ATTRIBUTE_LAST_UPDATED = "lastupdated";
    private static final String SET_ACTION_TYPE = "setActionType";
    private static final String SET_ACTION_DATE = "setActionDate";

    @Override
    public List<PatchActionOuterClass.PatchAction> parseRecord(Event event,
            PatchActionOuterClass.PatchAction messageEntity) {
        PatchActionOuterClass.PatchAction.ActionType actionType;
        Timestamp actionDate = null;
        if (ENTITY_TYPE_CIM_PATCH.equals(event.getEntityType())) {
            actionType = PatchActionOuterClass.PatchAction.ActionType.INSTALLATION;
        } else {
            actionType = PatchActionOuterClass.PatchAction.ActionType.APPROVAL;
            actionDate = Tools.toTimestamp(ZonedDateTimeParser.parseDateTime(event.getEntity(), ATTRIBUTE_LAST_UPDATED));
        }

        Map<String, Object> patchActionValues = new HashMap<>();
        patchActionValues.put(SET_ACTION_TYPE, actionType);
        patchActionValues.put(SET_ACTION_DATE, actionDate);

        return Collections.singletonList(Tools.setNullableField(messageEntity.toBuilder(), patchActionValues).build());
    }
}
