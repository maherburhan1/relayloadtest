package com.solarwinds.msp.ncentral.eventproduction.converter.impl.entity;

import com.solarwinds.msp.ncentral.eventproduction.api.entity.Event;
import com.solarwinds.msp.ncentral.proto.entity.entity.AccessGroupDeviceOuterClass;
import com.solarwinds.msp.ncentral.proto.entity.entity.AccessGroupDeviceOuterClass.AccessGroupDevice;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.solarwinds.msp.ncentral.eventproduction.api.entity.Tools.getInteger;

/**
 * This class handles additional business logic for processing data for the {@link
 * AccessGroupDeviceOuterClass.AccessGroup} data Protocol Buffers entity.
 */
class AccessGroupDeviceEvent implements EntityParser<AccessGroupDeviceOuterClass.AccessGroupDevice> {

    private static final String ATTRIBUTE_DEVICE_ID = "deviceid";

    @Override
    public List<AccessGroupDevice> parseRecord(Event event, AccessGroupDevice messageEntity) {
        final Map<String, String> entity = event.getEntity();
        final Integer deviceId = getInteger(entity, ATTRIBUTE_DEVICE_ID);
        if (deviceId == null) {
            return Collections.emptyList();
        }
        return Collections.singletonList(messageEntity.toBuilder().addDeviceId(deviceId).build());
    }
}