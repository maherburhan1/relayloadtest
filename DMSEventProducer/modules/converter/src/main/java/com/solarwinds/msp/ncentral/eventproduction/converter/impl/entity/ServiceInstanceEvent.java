package com.solarwinds.msp.ncentral.eventproduction.converter.impl.entity;

import com.solarwinds.msp.ncentral.eventproduction.api.entity.Event;
import com.solarwinds.msp.ncentral.proto.entity.entity.ServiceInstanceOuterClass;
import com.solarwinds.msp.ncentral.proto.entity.entity.ServiceInstanceTypeOuterClass;

import java.util.Collections;
import java.util.List;

/**
 * This class handles additional business logic for processing data for the {@link
 * ServiceInstanceOuterClass.ServiceInstance} data Protocol Buffers entity.
 */
class ServiceInstanceEvent implements EntityParser<ServiceInstanceOuterClass.ServiceInstance> {

    @Override
    public List<ServiceInstanceOuterClass.ServiceInstance> parseRecord(Event event,
            ServiceInstanceOuterClass.ServiceInstance messageEntity) {
        final ServiceInstanceTypeOuterClass.ServiceInstanceType serviceInstanceType =
                messageEntity.getServiceInstanceType().toBuilder().clearDescription().build();
        return Collections.singletonList(messageEntity.toBuilder().setServiceInstanceType(serviceInstanceType).build());
    }
}
