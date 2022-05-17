package com.solarwinds.msp.ncentral.eventproduction.converter;

import com.nable.util.StringUtils;
import com.solarwinds.constants.GeneralConstants;
import com.solarwinds.msp.ncentral.eventproduction.api.entity.Event;
import com.solarwinds.msp.ncentral.eventproduction.api.entity.EventType;
import com.solarwinds.msp.ncentral.proto.entity.entity.ContactOuterClass;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * This class represents the unit test of the {@link EntityParserHelper} class.
 */
class EntityParserHelperTest {

    private static final String ATTRIBUTE_FIRST_NAME = "contactfirstname";
    private static final String ATTRIBUTE_LAST_NAME = "contactlastname";

    private static final String FIRST_NAME = "Bruce";
    private static final String LAST_NAME = "Dickinson";

    private static final String ATTRIBUTE_TICKET_ID = "ticketid";
    private static final String INVALID_TICKET_ID = String.valueOf(GeneralConstants.VALUE_NOT_SET);
    private static final String VALID_TICKET_ID = String.valueOf(123456);

    // ----------------------------------------------------------------------------------------------------------------
    // Tests of the parseContact method
    // ----------------------------------------------------------------------------------------------------------------

    @ParameterizedTest(
            name = "{index}: firstName={0}, lastName={1}, firstNameKey={2}, lastNameKey={3}, => expected={4}")
    @MethodSource("parseContactArguments")
    void parseContact(String firstName, String lastName, String firstNameKey, String lastNameKey,
            String expectedFullName) {
        final ContactOuterClass.Contact expectedResult = createExpectedResult(expectedFullName);

        final ContactOuterClass.Contact result =
                EntityParserHelper.parseContact(createContactEntity(firstName, lastName), firstNameKey, lastNameKey,
                        ContactOuterClass.Contact.newBuilder().build());

        assertThat(result).isEqualTo(expectedResult);
    }

    private static Stream<Arguments> parseContactArguments() {
        return Stream.of(Arguments.of(FIRST_NAME, LAST_NAME, ATTRIBUTE_FIRST_NAME, ATTRIBUTE_LAST_NAME,
                FIRST_NAME + " " + LAST_NAME),
                Arguments.of(FIRST_NAME, null, ATTRIBUTE_FIRST_NAME, ATTRIBUTE_LAST_NAME, FIRST_NAME),
                Arguments.of(null, LAST_NAME, ATTRIBUTE_FIRST_NAME, ATTRIBUTE_LAST_NAME, LAST_NAME),
                Arguments.of(null, null, ATTRIBUTE_FIRST_NAME, ATTRIBUTE_LAST_NAME, GeneralConstants.EMPTY_STRING),
                Arguments.of(FIRST_NAME, LAST_NAME, FIRST_NAME, LAST_NAME, GeneralConstants.EMPTY_STRING),
                Arguments.of(null, null, null, null, GeneralConstants.EMPTY_STRING));
    }

    // ----------------------------------------------------------------------------------------------------------------
    // Tests of the validateTicketId method
    // ----------------------------------------------------------------------------------------------------------------

    @ParameterizedTest(name = "{index}: ticketId={0} => expected={1}")
    @MethodSource("validateTicketIdArguments")
    void validateTicketId(String value, boolean expected) {
        final Event event = createEvent(value);
        final boolean result = EntityParserHelper.validateTicketId(event);
        assertThat(result).isEqualTo(expected);
    }

    private static List<Arguments> validateTicketIdArguments() {
        final List<Arguments> arguments = new ArrayList<>();
        arguments.add(Arguments.of(INVALID_TICKET_ID, false));
        arguments.add(Arguments.of(VALID_TICKET_ID, true));
        arguments.add(Arguments.of(null, true));
        arguments.add(Arguments.of("", true));
        arguments.add(Arguments.of(" ", true));
        arguments.add(Arguments.of("         ", true));
        arguments.add(Arguments.of("\t \n \r       ", true));
        arguments.add(Arguments.of("null", true));
        arguments.add(Arguments.of("Same random string value is still valid Ticket ID :-D", true));
        return arguments;
    }

    // ----------------------------------------------------------------------------------------------------------------
    // Private methods
    // ----------------------------------------------------------------------------------------------------------------

    private ContactOuterClass.Contact createExpectedResult(String fullName) {
        return ContactOuterClass.Contact.newBuilder().setIsPrimary(true).setFullName(fullName).build();
    }

    private Map<String, String> createContactEntity(String firstName, String lastName) {
        final Map<String, String> entity = new HashMap<>();
        if (StringUtils.isNotBlank(firstName)) {
            entity.put(ATTRIBUTE_FIRST_NAME, firstName);
        }
        if (StringUtils.isNotBlank(lastName)) {
            entity.put(ATTRIBUTE_LAST_NAME, lastName);
        }
        return entity;
    }

    private Event createEvent(String ticketId) {
        return Event.builder()
                .ncentralServerGuid("N-central Server GUID")
                .eventingConfigurationCustomerId(1234567)
                .professionalModeLicenseType("Professional Mode License Type")
                .eventType(EventType.INSERT)
                .entityType("Entity Type")
                .entity(createTicketEntity(ticketId))
                .newValues(createTicketEntity(ticketId))
                .entityDataTypes(createTicketEntity(ticketId))
                .build();
    }

    private Map<String, String> createTicketEntity(String ticketId) {
        final Map<String, String> entity = new HashMap<>();
        entity.put(ATTRIBUTE_TICKET_ID, ticketId);
        return entity;
    }
}
