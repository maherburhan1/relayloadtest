package com.solarwinds.msp.ncentral.eventproduction.converter.impl.entity;

import com.solarwinds.msp.ncentral.eventproduction.api.entity.Event;
import com.solarwinds.msp.ncentral.eventproduction.api.entity.Tools;
import com.solarwinds.msp.ncentral.proto.entity.tasks.AvSecurityOuterClass;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * This class handles additional business logic for processing data for the {@link AvSecurityOuterClass.AvSecurity} data
 * Protocol Buffers entity.
 */
class AvSecurityEvent implements EntityParser<AvSecurityOuterClass.AvSecurity> {

    private static final String ENTITY_TYPE_DATA_AV_DEFENDER_STATUS_DETAILED = "dataavdefenderstatus_detailed";
    private static final String ENTITY_TYPE_DATA_ENDPOINT_SECURITY_STATUS_DETAILED =
            "dataendpointsecuritystatus_detailed";
    private static final String ENTITY_TYPE_DATA_BIT_DEFENDER_STATUS_DETAILED = "databitdefenderstatus_detailed";
    private static final String ENTITY_TYPE_DATA_AV_STATUS_DETAILED = "data20165_detailed";

    private static final String NAME_AV_DEFENDER = "AV Defender";
    private static final String NAME_ENDPOINT_SECURITY_MANAGER = "Endpoint Security Manager";
    private static final String NAME_AV_DEFENDER_UNMANAGED = "AV Defender (unmanaged)";

    private static final String ATTRIBUTE_AVD_SIGNATURE_AGE = "avd_signature_age";
    private static final String ATTRIBUTE_AVD_PROTECTION_STATE = "avd_protection_state";
    private static final String ATTRIBUTE_ES_DEF_FILE_AGE = "es_deffileage";
    private static final String ATTRIBUTE_ES_PROTECTION_STATUS = "es_protectionstatus";
    private static final String ATTRIBUTE_PRODUCT_STATUS_UPDATE_UPDATE_SIGAM = "product_status_update_updatesigam";
    private static final String ATTRIBUTE_BT_AGENT_VERSION = "bt_agent_version";
    private static final String ATTRIBUTE_AV_STATUS_NAME = "wmi20165_displayname";
    private static final String ATTRIBUTE_AV_STATUS_UP_TO_DATE = "wmi20165_uptodate";
    private static final String ATTRIBUTE_AV_STATUS_VERSION = "wmi20165_versionnumber";
    private static final String ATTRIBUTE_AV_STATUS_SCANNING_ENABLED = "wmi20165_scanningenabled";

    private static final int OUTDATED_DEFINITIONS_AGE = 6;
    private static final int SCANNING_DISABLED_STATE = 2;

    private static final String SET_NAME = "setName";
    private static final String SET_DEFINITIONS_UP_TO_DATE = "setDefinitionsUpToDate";
    private static final String SET_DEFINITIONS_AGE = "setDefinitionsAge";
    private static final String SET_SCANNING_ENABLED = "setScanningEnabled";
    private static final String SET_VERSION = "setVersion";

    @Override
    public List<AvSecurityOuterClass.AvSecurity> parseRecord(Event event,
            AvSecurityOuterClass.AvSecurity messageEntity) {
        Boolean definitionsUpToDate = null;
        Boolean scanningEnabled = null;
        String name = null;
        String version = null;

        final Map<String, String> entity = event.getEntity();
        switch (event.getEntityType()) {
            case ENTITY_TYPE_DATA_AV_DEFENDER_STATUS_DETAILED:
                name = NAME_AV_DEFENDER;
                definitionsUpToDate = Optional.ofNullable(Tools.getInteger(entity, ATTRIBUTE_AVD_SIGNATURE_AGE))
                        .orElse(OUTDATED_DEFINITIONS_AGE) < OUTDATED_DEFINITIONS_AGE;
                scanningEnabled = Optional.ofNullable(Tools.getInteger(entity, ATTRIBUTE_AVD_PROTECTION_STATE))
                        .orElse(SCANNING_DISABLED_STATE) < SCANNING_DISABLED_STATE;
                break;
            case ENTITY_TYPE_DATA_ENDPOINT_SECURITY_STATUS_DETAILED:
                name = NAME_ENDPOINT_SECURITY_MANAGER;
                definitionsUpToDate = Optional.ofNullable(Tools.getInteger(entity, ATTRIBUTE_ES_DEF_FILE_AGE))
                        .orElse(OUTDATED_DEFINITIONS_AGE) < OUTDATED_DEFINITIONS_AGE;
                scanningEnabled = Optional.ofNullable(Tools.getInteger(entity, ATTRIBUTE_ES_PROTECTION_STATUS))
                        .orElse(SCANNING_DISABLED_STATE) < SCANNING_DISABLED_STATE;
                break;
            case ENTITY_TYPE_DATA_BIT_DEFENDER_STATUS_DETAILED:
                name = NAME_AV_DEFENDER_UNMANAGED;
                version = entity.getOrDefault(ATTRIBUTE_PRODUCT_STATUS_UPDATE_UPDATE_SIGAM,
                        entity.getOrDefault(ATTRIBUTE_BT_AGENT_VERSION, null));
                break;
            case ENTITY_TYPE_DATA_AV_STATUS_DETAILED:
                name = entity.getOrDefault(ATTRIBUTE_AV_STATUS_NAME, null);
                version = entity.getOrDefault(ATTRIBUTE_AV_STATUS_VERSION, null);
                definitionsUpToDate = Tools.getBoolean(entity, ATTRIBUTE_AV_STATUS_UP_TO_DATE);
                scanningEnabled = Tools.getBoolean(entity, ATTRIBUTE_AV_STATUS_SCANNING_ENABLED);
                break;
            default:
        }

        final Map<String, Object> entityValues = new HashMap<>();
        entityValues.put(SET_NAME, name);
        entityValues.put(SET_DEFINITIONS_UP_TO_DATE, definitionsUpToDate);
        entityValues.put(SET_DEFINITIONS_AGE, null);
        entityValues.put(SET_SCANNING_ENABLED, scanningEnabled);
        entityValues.put(SET_VERSION, version);

        return Collections.singletonList(Tools.setNullableField(messageEntity.toBuilder(), entityValues).build());
    }
}
