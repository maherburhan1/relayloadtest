package com.solarwinds.msp.ncentral.eventproduction.converter;

import com.nable.logging.Logger;
import com.nable.logging.Loggers;
import com.solarwinds.constants.GeneralConstants;
import com.solarwinds.msp.ncentral.eventproduction.api.entity.Event;
import com.solarwinds.msp.ncentral.eventproduction.api.entity.Tools;
import com.solarwinds.msp.ncentral.proto.entity.entity.ContactOuterClass;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * This class contains various methods for parsing of common entity parts.
 */
public final class EntityParserHelper {

    private static final String INVALID_VALUE = String.valueOf(GeneralConstants.VALUE_NOT_SET);
    private static final String ATTRIBUTE_TICKET_ID = "ticketid";

    private static final String IS_PRIMARY = "setIsPrimary";
    private static final String FULL_NAME = "setFullName";

    private static final Logger logger = Loggers.EVENT_PRODUCER;

    private EntityParserHelper() {}

    /**
     * Parses the contact from the specified parameters.
     *
     * @param entity the {@link Map} with the entity data.
     * @param firstNameKey the first name key.
     * @param lastNameKey the last name key.
     * @param contact the {@link ContactOuterClass.Contact} object that provides a builder.
     * @return The {@link ContactOuterClass.Contact} with the primary contact data.
     */
    public static ContactOuterClass.Contact parseContact(Map<String, String> entity, String firstNameKey,
            String lastNameKey, ContactOuterClass.Contact contact) {
        final String firstName = entity.get(firstNameKey);
        final String lastName = entity.get(lastNameKey);
        final String fullName = Stream.of(firstName, lastName)
                .filter(value -> value != null && !value.isEmpty())
                .collect(Collectors.joining(" "));

        final Map<String, Object> entityValues = new HashMap<>();
        entityValues.put(IS_PRIMARY, true);
        entityValues.put(FULL_NAME, fullName);

        return Tools.setNullableField(contact.toBuilder(), entityValues).build();
    }

    /**
     * Validates the ticket ID in the specified event.
     *
     * @param event the event with the ticket ID.
     * @return {@code true} if the ticket ID is valid (it does not equal to value "-1"), {@code false} otherwise.
     */
    public static boolean validateTicketId(Event event) {
        final String ticketId = event.getEntity().get(ATTRIBUTE_TICKET_ID);
        if (INVALID_VALUE.equals(ticketId)) {
            logger.info("{} event has an invalid ticket id. Ignoring.", event.getEntityType());
            return false;
        }
        return true;
    }
}
