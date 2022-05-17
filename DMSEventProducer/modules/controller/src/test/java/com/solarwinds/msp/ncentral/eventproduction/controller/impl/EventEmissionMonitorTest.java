package com.solarwinds.msp.ncentral.eventproduction.controller.impl;

import com.google.common.collect.Sets;

import com.solarwinds.msp.ncentral.eventproduction.api.service.tracking.EventingControlService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.ZonedDateTime;

import static com.solarwinds.msp.ncentral.eventproduction.controller.impl.EventEmissionMonitor.TIMESTAMPS_DIFFERENCE_TOLERANCE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * This class represents the unit test of the {@link EventEmissionMonitor} class.
 */
@ExtendWith(MockitoExtension.class)
public class EventEmissionMonitorTest {

    private static final String TABLE_1_NAME = "Table A";
    private static final String TABLE_2_NAME = "Table B";
    private static final int EVENTING_CUSTOMER_ID = 50;

    private static final ZonedDateTime NEWER_TIMESTAMP = ZonedDateTime.now();
    private static final ZonedDateTime OLDER_TIMESTAMP = ZonedDateTime.now().minusDays(1L);

    @Mock
    private EventingControlService eventingControlService;

    private EventEmissionMonitor eventEmissionMonitor;

    @BeforeEach
    void setUp() {
        eventEmissionMonitor = new EventEmissionMonitor(eventingControlService);
        verify(eventingControlService).addStartupListenerOrExecuteStartup(eventEmissionMonitor);
        eventEmissionMonitor.onEventingStart();
    }

    @Test
    void unacknowledgedEventsExist_tableScraped_noEmittedEvents() {
        when(eventingControlService.getSendingEnabledTables()).thenReturn(
                Sets.newHashSet(getTableIdentifier(TABLE_1_NAME), getTableIdentifier(TABLE_2_NAME)));

        final boolean result =
                eventEmissionMonitor.unacknowledgedEventsExist(EVENTING_CUSTOMER_ID, TABLE_1_NAME, OLDER_TIMESTAMP);

        assertThat(result).isFalse();
    }

    @Test
    void unacknowledgedEventsExist_tableScraped_emittedAndAcknowledgedTimestampsEqual() {
        when(eventingControlService.getSendingEnabledTables()).thenReturn(
                Sets.newHashSet(getTableIdentifier(TABLE_1_NAME), getTableIdentifier(TABLE_2_NAME)));
        eventEmissionMonitor.recordEmittedEvent(EVENTING_CUSTOMER_ID, TABLE_1_NAME, OLDER_TIMESTAMP);

        final boolean result =
                eventEmissionMonitor.unacknowledgedEventsExist(EVENTING_CUSTOMER_ID, TABLE_1_NAME, OLDER_TIMESTAMP);

        assertThat(result).isFalse();
    }

    @Test
    void unacknowledgedEventsExist_tableScraped_eventTimestampsEqualWithinTolerancy() {
        when(eventingControlService.getSendingEnabledTables()).thenReturn(
                Sets.newHashSet(getTableIdentifier(TABLE_1_NAME), getTableIdentifier(TABLE_2_NAME)));
        eventEmissionMonitor.recordEmittedEvent(EVENTING_CUSTOMER_ID, TABLE_1_NAME,
                OLDER_TIMESTAMP.minus(TIMESTAMPS_DIFFERENCE_TOLERANCE));

        final boolean result =
                eventEmissionMonitor.unacknowledgedEventsExist(EVENTING_CUSTOMER_ID, TABLE_1_NAME, OLDER_TIMESTAMP);

        assertThat(result).isFalse();
    }

    @Test
    void unacknowledgedEventsExist_tableScraped_emittedAndAcknowledgedTimestampsNotEqual() {
        when(eventingControlService.getSendingEnabledTables()).thenReturn(
                Sets.newHashSet(getTableIdentifier(TABLE_1_NAME), getTableIdentifier(TABLE_2_NAME)));
        eventEmissionMonitor.recordEmittedEvent(EVENTING_CUSTOMER_ID, TABLE_1_NAME, OLDER_TIMESTAMP);
        eventEmissionMonitor.recordEmittedEvent(EVENTING_CUSTOMER_ID, TABLE_1_NAME, NEWER_TIMESTAMP);

        final boolean result =
                eventEmissionMonitor.unacknowledgedEventsExist(EVENTING_CUSTOMER_ID, TABLE_1_NAME, OLDER_TIMESTAMP);

        assertThat(result).isTrue();
    }

    @Test
    void unacknowledgedEventsExist_tableNotScraped() {
        when(eventingControlService.getSendingEnabledTables()).thenReturn(
                Sets.newHashSet(getTableIdentifier(TABLE_2_NAME)));

        final boolean result =
                eventEmissionMonitor.unacknowledgedEventsExist(EVENTING_CUSTOMER_ID, TABLE_1_NAME, OLDER_TIMESTAMP);

        assertThat(result).isTrue();
    }

    private String getTableIdentifier(String tableName) {
        return String.format("%d%s%s", EVENTING_CUSTOMER_ID, EventEmissionMonitor.CUSTOMER_ID_TABLE_NAME_DELIMITER,
                tableName);
    }
}
