package com.solarwinds.msp.ncentral.eventproduction.converter.impl.entity;

import com.solarwinds.msp.ncentral.eventproduction.api.entity.Event;
import com.solarwinds.msp.ncentral.proto.entity.entity.ServiceOuterClass;
import com.solarwinds.msp.ncentral.proto.entity.entity.ServiceTypeOuterClass;

import java.util.Collections;
import java.util.List;

/**
 * This class handles additional business logic for processing data for the {@link ServiceOuterClass.Service} data
 * Protocol Buffers entity.
 */
class ServiceEvent implements EntityParser<ServiceOuterClass.Service> {

    @Override
    public List<ServiceOuterClass.Service> parseRecord(Event event, ServiceOuterClass.Service messageEntity) {
        final ServiceTypeOuterClass.ServiceType serviceType =
                messageEntity.getServiceType().toBuilder().clearName().build();
        return Collections.singletonList(messageEntity.toBuilder().setServiceType(serviceType).build());
    }
}
