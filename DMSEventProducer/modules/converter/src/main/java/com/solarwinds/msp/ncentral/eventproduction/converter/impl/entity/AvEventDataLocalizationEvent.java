package com.solarwinds.msp.ncentral.eventproduction.converter.impl.entity;

import com.nable.logging.Logger;
import com.nable.logging.Loggers;
import com.solarwinds.msp.ncentral.eventproduction.api.entity.Event;
import com.solarwinds.msp.ncentral.eventproduction.api.entity.Tools;
import com.solarwinds.msp.ncentral.proto.entity.tasks.AvEventDataLocalizationOuterClass;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class handles additional business logic for processing data for the {@link
 * AvEventDataLocalizationOuterClass.AvEventDataLocalization} data Protocol Buffers entity.
 */
class AvEventDataLocalizationEvent implements EntityParser<AvEventDataLocalizationOuterClass.AvEventDataLocalization> {

    private static final String TABLE_NAME = "tablename";
    private static final String TABLE_EVENT_DATA_MALWARE = "eventdata_malware";
    private static final String TABLE_EVENT_DATA_MALWARE_ACTION = "eventdata_malware_action";
    private static final List<String> VALID_SOURCES =
            Arrays.asList(TABLE_EVENT_DATA_MALWARE, TABLE_EVENT_DATA_MALWARE_ACTION);

    private static final String FIELD = "field";
    private static final String FIELD_MALWARE_TYPE = "maltype";
    private static final String FIELD_STATE = "state";
    private static final String FIELD_THREAT_TYPE = "threattype";
    private static final String FIELD_ACTION = "action";

    private static final String SET_SOURCE_EVENT = "setSourceEvent";
    private static final String SOURCE_EVENT_AV_SECURITY_THREAT = "AvSecurityThreat";

    private static final String SET_EVENT_PROPERTY = "setEventProperty";
    private static final String EVENT_PROPERTY_MALWARE_TYPE_ID = "malwareTypeId";
    private static final String EVENT_PROPERTY_STATE_ID = "stateId";
    private static final String EVENT_PROPERTY_THREAT_TYPE_ID = "threatTypeId";
    private static final String EVENT_PROPERTY_ACTION_IDS = "actionIds";

    private static final Logger logger = Loggers.EVENT_PRODUCER;

    @Override
    public List<AvEventDataLocalizationOuterClass.AvEventDataLocalization> parseRecord(Event event,
            AvEventDataLocalizationOuterClass.AvEventDataLocalization messageEntity) {
        final Map<String, String> entity = event.getEntity();
        final String tableName = entity.getOrDefault(TABLE_NAME, null);
        if (!VALID_SOURCES.contains(tableName)) {
            logger.debug("'{}' localization event is not currently supported.", tableName);
            return Collections.emptyList();
        }

        final Map<String, Object> entityValues = new HashMap<>();
        entityValues.put(SET_SOURCE_EVENT, getSourceEvent(tableName));
        entityValues.put(SET_EVENT_PROPERTY, getEventProperty(entity.getOrDefault(FIELD, null)));

        return Collections.singletonList(Tools.setNullableField(messageEntity.toBuilder(), entityValues).build());
    }

    private String getSourceEvent(String tableName) {
        switch (tableName) {
            case TABLE_EVENT_DATA_MALWARE:
            case TABLE_EVENT_DATA_MALWARE_ACTION:
                return SOURCE_EVENT_AV_SECURITY_THREAT;
            default:
                return null;
        }
    }

    private String getEventProperty(String field) {
        switch (field) {
            case FIELD_MALWARE_TYPE:
                return EVENT_PROPERTY_MALWARE_TYPE_ID;
            case FIELD_STATE:
                return EVENT_PROPERTY_STATE_ID;
            case FIELD_THREAT_TYPE:
                return EVENT_PROPERTY_THREAT_TYPE_ID;
            case FIELD_ACTION:
                return EVENT_PROPERTY_ACTION_IDS;
            default:
                return null;
        }
    }
}
