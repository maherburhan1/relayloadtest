package com.solarwinds.msp.ncentral.eventproduction.converter.impl.entity;

import com.solarwinds.constants.GeneralConstants;
import com.solarwinds.msp.ncentral.eventproduction.api.entity.Event;
import com.solarwinds.msp.ncentral.eventproduction.api.entity.Tools;
import com.solarwinds.msp.ncentral.proto.entity.asset.AssetMediaAccessOuterClass;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class handles additional business logic for processing data for the {@link
 * AssetMediaAccessOuterClass.AssetMediaAccess} data Protocol Buffers entity.
 */
class AssetMediaAccessEvent implements EntityParser<AssetMediaAccessOuterClass.AssetMediaAccess> {

    private static final String CAPACITY = "capacity";
    private static final String SET_SIZE = "setSizeMb";

    @Override
    public List<AssetMediaAccessOuterClass.AssetMediaAccess> parseRecord(Event event,
            AssetMediaAccessOuterClass.AssetMediaAccess messageEntity) {
        Double sizeMegaBytes = Tools.getDouble(event.getEntity(), CAPACITY);
        if (sizeMegaBytes != null) {
            sizeMegaBytes = sizeMegaBytes / GeneralConstants.BYTES_IN_ONE_MEGABYTE;
        }

        final Map<String, Object> entityValues = new HashMap<>();
        entityValues.put(SET_SIZE, sizeMegaBytes);

        return Collections.singletonList(Tools.setNullableField(messageEntity.toBuilder(), entityValues).build());
    }
}
