package com.solarwinds.msp.ncentral.eventproduction.converter.impl.entity;

import com.solarwinds.msp.ncentral.eventproduction.api.entity.Event;
import com.solarwinds.msp.ncentral.eventproduction.converter.EntityParserHelper;
import com.solarwinds.msp.ncentral.proto.entity.entity.ClientOuterClass;
import com.solarwinds.msp.ncentral.proto.entity.entity.ContactOuterClass;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.solarwinds.msp.ncentral.eventproduction.api.entity.Tools.getBoolean;

/**
 * This class handles additional business logic for processing data for the {@link ClientOuterClass.Client} data
 * Protocol Buffers entity.
 */
class ClientEvent implements EntityParser<ClientOuterClass.Client> {

    private static final String ATTRIBUTE_IS_SYSTEM = "issystem";
    private static final String ATTRIBUTE_IS_SITE = "issite";
    private static final String ATTRIBUTE_IS_SERVICE_ORGANIZATION = "isserviceorg";
    private static final String ATTRIBUTE_FIRST_NAME = "contactfirstname";
    private static final String ATTRIBUTE_LAST_NAME = "contactlastname";
    private static final int CONTACT_INDEX = 0;

    @Override
    public List<ClientOuterClass.Client> parseRecord(Event event, ClientOuterClass.Client messageEntity) {
        final Map<String, String> entity = event.getEntity();

        final boolean isSystem = getBoolean(entity, ATTRIBUTE_IS_SYSTEM);
        final boolean isServiceOrganization = getBoolean(entity, ATTRIBUTE_IS_SERVICE_ORGANIZATION);
        final boolean isSite = getBoolean(entity, ATTRIBUTE_IS_SITE);
        final ClientOuterClass.Client.ClientType clientType;
        if (isServiceOrganization || isSystem) {
            clientType = ClientOuterClass.Client.ClientType.VAR;
        } else if (isSite) {
            clientType = ClientOuterClass.Client.ClientType.SITE;
        } else {
            clientType = ClientOuterClass.Client.ClientType.CUSTOMER;
        }

        final ContactOuterClass.Contact mainContactEntity =
                EntityParserHelper.parseContact(entity, ATTRIBUTE_FIRST_NAME, ATTRIBUTE_LAST_NAME,
                        messageEntity.getContact(CONTACT_INDEX));

        final ClientOuterClass.Client.Builder result = messageEntity.toBuilder();
        result.setContact(CONTACT_INDEX, mainContactEntity);
        result.setClientType(clientType);
        return Collections.singletonList(result.build());
    }
}
