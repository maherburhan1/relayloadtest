package com.solarwinds.msp.ncentral.eventproduction.controller.impl;

import com.solarwinds.msp.ncentral.eventproduction.api.service.notification.EventNotificationType;
import com.solarwinds.msp.ncentral.eventproduction.api.service.tracking.EventingControlService;
import com.solarwinds.msp.ncentral.eventproduction.controller.EventControllerConfiguration;
import com.solarwinds.msp.ncentral.eventproduction.controller.EventNotificationService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

/**
 * This class represents the unit test of the {@link EventFailuresMonitor} class.
 */
@ExtendWith(MockitoExtension.class)
public class EventFailuresMonitorTest {

    private static final int CUSTOMER_ID_1 = 50;
    private static final int CUSTOMER_ID_2 = 51;
    private static final long WAIT_TIME_IN_HOURS_FOR_UNSENT_EVENTS_NOTIFICATION = 4L;

    private Instant currentTime = Instant.now();

    private EventFailuresMonitor eventFailuresMonitor;

    @Mock
    EventNotificationService eventNotificationService;

    @Mock
    private EventControllerConfiguration eventControllerConfigurationMock;

    @Mock
    private EventingControlService eventingControlServiceMock;

    @BeforeEach
    void setup() {
        final Clock clock = mock(Clock.class);
        when(clock.instant()).thenAnswer((invocation) -> currentTime);
        eventFailuresMonitor =
                new EventFailuresMonitor(eventNotificationService, clock, eventControllerConfigurationMock,
                        eventingControlServiceMock);
        verify(eventingControlServiceMock).addStartupListenerOrExecuteStartup(eventFailuresMonitor);
        when(eventControllerConfigurationMock.getWaitTimeForUnsentEventsNotification()).thenReturn(
                Optional.of(Duration.ofHours(WAIT_TIME_IN_HOURS_FOR_UNSENT_EVENTS_NOTIFICATION)));
        eventFailuresMonitor.onEventingStart();

    }

    @Test
    void failures_are_monitored_no_notification_is_sent() {
        for (int i = 0; i < 5; i++) {
            eventFailuresMonitor.processFailure(CUSTOMER_ID_1);
            eventFailuresMonitor.processFailure(CUSTOMER_ID_2);
        }
        currentTime = currentTime.plus(WAIT_TIME_IN_HOURS_FOR_UNSENT_EVENTS_NOTIFICATION - 1, ChronoUnit.HOURS);

        eventFailuresMonitor.processFailure(CUSTOMER_ID_1);
        eventFailuresMonitor.processFailure(CUSTOMER_ID_2);
        verifyZeroInteractions(eventNotificationService);
    }

    @Test
    void no_notification_is_sent_due_to_reset() {
        for (int i = 0; i < 5; i++) {
            eventFailuresMonitor.processFailure(CUSTOMER_ID_1);
            eventFailuresMonitor.processFailure(CUSTOMER_ID_2);
        }
        eventFailuresMonitor.resetAll();
        currentTime = currentTime.plus(WAIT_TIME_IN_HOURS_FOR_UNSENT_EVENTS_NOTIFICATION + 1, ChronoUnit.HOURS);

        eventFailuresMonitor.processFailure(CUSTOMER_ID_1);
        eventFailuresMonitor.processFailure(CUSTOMER_ID_2);
        verifyZeroInteractions(eventNotificationService);
    }

    @Test
    void unable_to_send_events_notification_is_sent() {
        for (int i = 0; i < 5; i++) {
            eventFailuresMonitor.processFailure(CUSTOMER_ID_1);
            eventFailuresMonitor.processFailure(CUSTOMER_ID_2);
        }
        currentTime = currentTime.plus(WAIT_TIME_IN_HOURS_FOR_UNSENT_EVENTS_NOTIFICATION + 1, ChronoUnit.HOURS);

        eventFailuresMonitor.processSuccess(CUSTOMER_ID_2);

        eventFailuresMonitor.processFailure(CUSTOMER_ID_1);
        eventFailuresMonitor.processFailure(CUSTOMER_ID_2);
        verify(eventNotificationService).sendNotification(CUSTOMER_ID_1, EventNotificationType.UNABLE_TO_SEND_EVENTS);
        verify(eventNotificationService, never()).sendNotification(CUSTOMER_ID_2,
                EventNotificationType.UNABLE_TO_SEND_EVENTS);

        eventFailuresMonitor.processFailure(CUSTOMER_ID_1);
        eventFailuresMonitor.processFailure(CUSTOMER_ID_2);
        verifyNoMoreInteractions(eventNotificationService);
    }

    @Test
    void events_successfully_sent_notification_is_sent() {
        for (int i = 0; i < 5; i++) {
            eventFailuresMonitor.processFailure(CUSTOMER_ID_1);
        }
        currentTime = currentTime.plus(WAIT_TIME_IN_HOURS_FOR_UNSENT_EVENTS_NOTIFICATION + 1, ChronoUnit.HOURS);

        eventFailuresMonitor.processFailure(CUSTOMER_ID_1);
        verify(eventNotificationService).sendNotification(CUSTOMER_ID_1, EventNotificationType.UNABLE_TO_SEND_EVENTS);

        eventFailuresMonitor.processSuccess(CUSTOMER_ID_1);
        verify(eventNotificationService).sendNotification(CUSTOMER_ID_1,
                EventNotificationType.EVENTS_SUCCESSFULLY_SENT);

        eventFailuresMonitor.processFailure(CUSTOMER_ID_1);
        verifyNoMoreInteractions(eventNotificationService);
    }
}
