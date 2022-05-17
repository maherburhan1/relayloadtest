package com.solarwinds.msp.ncentral.eventproduction.converter.impl.entity;


import com.solarwinds.msp.ncentral.eventproduction.api.entity.Event;
import com.solarwinds.msp.ncentral.proto.entity.entity.AccessGroupCustomerOuterClass;
import com.solarwinds.msp.ncentral.proto.entity.entity.AccessGroupCustomerOuterClass.AccessGroupCustomer;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.solarwinds.msp.ncentral.eventproduction.api.entity.Tools.getInteger;

/**
 * This class handles additional business logic for processing data for the {@link
 * AccessGroupCustomerOuterClass.AccessGroup} data Protocol Buffers entity.
 */
class AccessGroupCustomerEvent implements EntityParser<AccessGroupCustomerOuterClass.AccessGroupCustomer> {

    private static final String ATTRIBUTE_CUSTOMER_ID = "customerid";

    @Override
    public List<AccessGroupCustomer> parseRecord(Event event, AccessGroupCustomer messageEntity) {
        final Map<String, String> entity = event.getEntity();
        final Integer customerId = getInteger(entity, ATTRIBUTE_CUSTOMER_ID);
        if (customerId == null) {
            return Collections.emptyList();
        }
        return Collections.singletonList(messageEntity.toBuilder().addClientId(customerId).build());
    }
}
