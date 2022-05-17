package com.solarwinds.msp.ncentral.eventproduction.api.service.tracking;

import com.solarwinds.msp.ncentral.eventproduction.api.service.persistence.TimestampedEvent;
import com.solarwinds.msp.ncentral.eventproduction.api.service.publisher.PublishingContext;
import com.solarwinds.msp.ncentral.proto.entity.MspSourceSystemEventOuterClass.MspSourceSystemEvent;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Observer;
import java.util.OptionalInt;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EventingControlServiceTest {

    private static final int CUSTOMER_ID = 1;
    private static final String TABLE_1 = "tableOne";
    private static final String SYSTEM_GUID = "systemGuid";

    @Mock
    private Observer observerMock;

    @Captor
    private ArgumentCaptor<Object> observerArgumentCaptor;

    @InjectMocks
    private EventingControlService service;

    @BeforeEach
    void setUp() {
        service.addObserver(observerMock);
    }

    @Test
    void backupStarted_eventingEnabled_stopsScrapingAndEventSending_notifiesObservers() {
        service.startEventing();
        service.updateSystemBackupStatus(true);
        assertFalse(service.getGlobalScrapingEnabled());
        assertFalse(service.getGlobalSendingEnabled());

        verify(observerMock, times(2)).update(same(service), any());
    }

    @Test
    void backupStarted_eventingDisabled_queuesScrapingAndSendingStop_notifiesObserversAfterStart() {
        service.updateSystemBackupStatus(true);
        assertFalse(service.getGlobalScrapingEnabled());
        assertFalse(service.getGlobalSendingEnabled());

        verify(observerMock, never()).update(same(service), any());
        service.startEventing();
        verify(observerMock, times(1)).update(same(service), any());
    }

    @Test
    void backupFinished_reenablesScrapingAndEventSending_notifiesObservers() {
        service.startEventing();
        service.updateSystemBackupStatus(false);
        assertTrue(service.getGlobalScrapingEnabled());
        assertTrue(service.getGlobalSendingEnabled());

        verify(observerMock, times(2)).update(same(service), any());
    }

    @Test
    void eventingStopped_everythingIsDisabled_notifiesObservers() {
        service.stopEventing();
        assertFalse(service.getGlobalScrapingEnabled());
        assertFalse(service.getGlobalSendingEnabled());

        verify(observerMock).update(same(service), any());
    }

    @Test
    void eventingStarted_everythingIsEnabled_notifiesObservers_sendsStartupEventsForEagerLoadedListeners() {
        final EventingStartupListener startupListenerMock = mock(EventingStartupListener.class);
        service.addStartupListenerOrExecuteStartup(startupListenerMock);

        service.startEventing();

        assertTrue(service.getGlobalScrapingEnabled());
        assertTrue(service.getGlobalSendingEnabled());
        verify(startupListenerMock).onEventingStart();

        verify(observerMock).update(same(service), any());
    }

    @Test
    void eventingStarted_everythingIsEnabled_notifiesObservers_lazyLoadedGetExecutedRightAway() {
        final EventingStartupListener startupListenerMock = mock(EventingStartupListener.class);

        service.startEventing();

        service.addStartupListenerOrExecuteStartup(startupListenerMock);

        assertTrue(service.getGlobalScrapingEnabled());
        assertTrue(service.getGlobalSendingEnabled());
        verify(startupListenerMock).onEventingStart();

        verify(observerMock).update(same(service), any());
    }

    @Test
    void updateEventingStateForCustomer_disabledEventing__disables_eventing_for_customer_stops_scraping() {
        mockStartedEventingAfterScrapingStarted();
        service.updateEventingStateForCustomer(false, CUSTOMER_ID);
        verify(observerMock, times(4)).update(any(EventingControlService.class), observerArgumentCaptor.capture());
        assertScrapingNotificationAfterDisable(3);
    }

    private void assertScrapingNotificationAfterDisable(int expectedNotificationInvocationOrder) {
        assertScrapingConfigurationChangeNotification(expectedNotificationInvocationOrder, CUSTOMER_ID, false, true);
        reset(observerMock);
    }

    private void mockStartedEventingAfterScrapingStarted() {
        service.updateEventingStateForCustomer(true, CUSTOMER_ID);
    }

    @Test
    void updateEventingStateForCustomer_single_enable_sets_scraping_for_start() {
        service.updateEventingStateForCustomer(true, CUSTOMER_ID);

        verify(observerMock, times(2)).update(any(EventingControlService.class), observerArgumentCaptor.capture());
        assertScrapingConfigurationChangeNotification(1, CUSTOMER_ID, true, true);
        assertThat(service.getSendingEnabledTables()).isEmpty();
        assertThat(service.getBufferingEnabledTables()).isEmpty();
    }

    @Test
    void updateStateForTable_scraping_inProgress_table_not_enabled_for_send_enabled_for_buffering() {
        service.updateEventingStateForCustomer(true, CUSTOMER_ID);
        service.updateStateForTable(EventTableStateChange.builder()
                .customerId(CUSTOMER_ID)
                .tableName(TABLE_1)
                .scrapingState(EventTableScrapingState.IN_PROGRESS)
                .build());

        assertThat(service.getSendingEnabledTables()).doesNotContain(
                CUSTOMER_ID + EventingControlService.TABLE_ID_NAME_DELIMITER + TABLE_1);
        assertThat(service.getBufferingEnabledTables()).contains(
                CUSTOMER_ID + EventingControlService.TABLE_ID_NAME_DELIMITER + TABLE_1);

        verify(observerMock, times(3)).update(any(EventingControlService.class), observerArgumentCaptor.capture());
        assertScrapingConfigurationChangeNotification(1, CUSTOMER_ID, true, true);
        assertEventingConfigurationChangeNotification(0, true, OptionalInt.empty());
        assertEventingConfigurationChangeNotification(2, true, OptionalInt.empty());
    }

    @Test
    void updateStateForTable_scraping_failed_after_inProgress_table_disabled_for_buffering_and_sending() {
        service.updateEventingStateForCustomer(true, CUSTOMER_ID);
        service.updateStateForTable(EventTableStateChange.builder()
                .customerId(CUSTOMER_ID)
                .tableName(TABLE_1)
                .scrapingState(EventTableScrapingState.IN_PROGRESS)
                .build());

        service.updateStateForTable(EventTableStateChange.builder()
                .customerId(CUSTOMER_ID)
                .tableName(TABLE_1)
                .scrapingState(EventTableScrapingState.FAILED)
                .build());

        assertThat(service.getSendingEnabledTables()).doesNotContain(
                CUSTOMER_ID + EventingControlService.TABLE_ID_NAME_DELIMITER + TABLE_1);
        assertThat(service.getBufferingEnabledTables()).doesNotContain(
                CUSTOMER_ID + EventingControlService.TABLE_ID_NAME_DELIMITER + TABLE_1);

        verify(observerMock, times(4)).update(any(EventingControlService.class), observerArgumentCaptor.capture());
        assertScrapingConfigurationChangeNotification(1, CUSTOMER_ID, true, true);
        assertEventingConfigurationChangeNotification(0, true, OptionalInt.empty());
        assertEventingConfigurationChangeNotification(2, true, OptionalInt.empty());
        assertEventingConfigurationChangeNotification(3, true, OptionalInt.empty());
    }

    @Test
    void updateStateForTable_scraping_inprogress_then_finished_table_enabled_for_send_and_buffer() {
        service.updateStateForTable(EventTableStateChange.builder()
                .customerId(CUSTOMER_ID)
                .tableName(TABLE_1)
                .scrapingState(EventTableScrapingState.IN_PROGRESS)
                .build());
        service.updateStateForTable(EventTableStateChange.builder()
                .customerId(CUSTOMER_ID)
                .tableName(TABLE_1)
                .scrapingState(EventTableScrapingState.FINISHED)
                .build());

        assertThat(service.getSendingEnabledTables()).contains(
                CUSTOMER_ID + EventingControlService.TABLE_ID_NAME_DELIMITER + TABLE_1);
        assertThat(service.getBufferingEnabledTables()).contains(
                CUSTOMER_ID + EventingControlService.TABLE_ID_NAME_DELIMITER + TABLE_1);

        verify(observerMock, times(2)).update(any(EventingControlService.class), observerArgumentCaptor.capture());
        assertEventingConfigurationChangeNotification(0, true, OptionalInt.empty());
    }

    @Test
    void updateStateForTable_scraping_skip_enables_send_buffer() {
        service.updateEventingStateForCustomer(true, CUSTOMER_ID);
        service.updateStateForTable(EventTableStateChange.builder()
                .customerId(CUSTOMER_ID)
                .tableName(TABLE_1)
                .scrapingState(EventTableScrapingState.SKIP)
                .build());

        assertThat(service.getSendingEnabledTables()).contains(
                CUSTOMER_ID + EventingControlService.TABLE_ID_NAME_DELIMITER + TABLE_1);
        assertThat(service.getBufferingEnabledTables()).contains(
                CUSTOMER_ID + EventingControlService.TABLE_ID_NAME_DELIMITER + TABLE_1);

        verify(observerMock, times(3)).update(any(EventingControlService.class), observerArgumentCaptor.capture());
        assertScrapingConfigurationChangeNotification(1, CUSTOMER_ID, true, true);
        assertEventingConfigurationChangeNotification(0, true, OptionalInt.empty());
        assertEventingConfigurationChangeNotification(2, true, OptionalInt.empty());
    }

    private void assertEventingConfigurationChangeNotification(int expectedInvocationOrder,
            boolean sendingConfigurationChanged, OptionalInt removeBufferedEventsForCustomer) {
        assertThat(observerArgumentCaptor.getAllValues().get(expectedInvocationOrder)).isInstanceOf(
                EventingConfigurationChange.class);
        final EventingConfigurationChange eventingConfigurationChange =
                (EventingConfigurationChange) observerArgumentCaptor.getAllValues().get(expectedInvocationOrder);
        assertThat(eventingConfigurationChange.isSendingConfigurationChanged()).isEqualTo(sendingConfigurationChanged);
        assertThat(eventingConfigurationChange.getRemoveEventsForCustomer()).isEqualTo(removeBufferedEventsForCustomer);
    }

    private void assertScrapingConfigurationChangeNotification(int expectedNotificationInvocationOrder, int customerId,
            boolean startScraping, boolean stopScraping) {
        assertThat(observerArgumentCaptor.getAllValues().get(expectedNotificationInvocationOrder)).isInstanceOf(
                ScrapingConfigurationChange.class);
        final ScrapingConfigurationChange scrapingConfigurationChange =
                (ScrapingConfigurationChange) observerArgumentCaptor.getAllValues()
                        .get(expectedNotificationInvocationOrder);
        assertThat(scrapingConfigurationChange.getCustomerId()).isEqualTo(customerId);
        assertThat(scrapingConfigurationChange.isStartScraping()).isEqualTo(startScraping);
        assertThat(scrapingConfigurationChange.isStopScraping()).isEqualTo(stopScraping);
    }

    @Test
    void changeOccurredInSendingConfiguration_invoked_by_scraping_state_change() {
        service.updateEventingStateForCustomer(true, CUSTOMER_ID);

        service.updateStateForTable(EventTableStateChange.builder()
                .customerId(CUSTOMER_ID)
                .tableName(TABLE_1)
                .scrapingState(EventTableScrapingState.IN_PROGRESS)
                .build());

        service.updateStateForTable(EventTableStateChange.builder()
                .customerId(CUSTOMER_ID)
                .tableName(TABLE_1)
                .scrapingState(EventTableScrapingState.FINISHED)
                .build());

        verify(observerMock, times(4)).update(any(EventingControlService.class), observerArgumentCaptor.capture());
        assertEventingConfigurationChangeNotification(0, true, OptionalInt.empty());
        assertEventingConfigurationChangeNotification(2, true, OptionalInt.empty());
        assertEventingConfigurationChangeNotification(3, true, OptionalInt.empty());
    }

    @Test
    void customerStateChange_invokes_buffering_and_sending_change() {
        service.updateEventingStateForCustomer(false, CUSTOMER_ID);

        verify(observerMock, times(2)).update(any(EventingControlService.class), observerArgumentCaptor.capture());
        assertEventingConfigurationChangeNotification(0, true, OptionalInt.of(CUSTOMER_ID));
    }

    @Test
    void enableEventingFinishScrapingEnableWithDifferentTables_sendingEnabledTablesAreCleanedUp() {
        service.updateEventingStateForCustomer(true, CUSTOMER_ID);

        service.updateStateForTable(EventTableStateChange.builder()
                .customerId(CUSTOMER_ID)
                .tableName(TABLE_1)
                .scrapingState(EventTableScrapingState.FINISHED)
                .build());

        assertThat(service.getSendingEnabledTables()).contains(
                CUSTOMER_ID + EventingControlService.TABLE_ID_NAME_DELIMITER + TABLE_1);

        service.updateEventingStateForCustomer(true, CUSTOMER_ID);

        assertThat(service.getSendingEnabledTables()).isEmpty();
    }

    @Test
    void enableEventingFinishScrapingDisableEventing_sendingEnabledTablesAreCleanedUp() {
        service.updateEventingStateForCustomer(true, CUSTOMER_ID);

        service.updateStateForTable(EventTableStateChange.builder()
                .customerId(CUSTOMER_ID)
                .tableName(TABLE_1)
                .scrapingState(EventTableScrapingState.FINISHED)
                .build());

        assertThat(service.getSendingEnabledTables()).contains(
                CUSTOMER_ID + EventingControlService.TABLE_ID_NAME_DELIMITER + TABLE_1);

        service.updateEventingStateForCustomer(false, CUSTOMER_ID);

        assertThat(service.getSendingEnabledTables()).isEmpty();
    }

    @Test
    void test_event_sending_eligibility_transition() {
        TimestampedEvent<?> eventMock = mockEventForEligibility();

        assertThat(service.isEventEligibleForSend(eventMock)).isFalse();

        service.updateStateForTable(EventTableStateChange.builder()
                .scrapingState(EventTableScrapingState.FINISHED)
                .customerId(CUSTOMER_ID)
                .tableName(TABLE_1)
                .build());

        assertThat(service.isEventEligibleForSend(eventMock)).isTrue();
    }

    @Test
    void system_event_is_always_eligible_for_sending_and_buffering() {
        final MspSourceSystemEvent systemEvent = MspSourceSystemEvent.newBuilder().build();
        final TimestampedEvent timestampedEvent = mockEventForEligibility();
        when(timestampedEvent.getEvent()).thenReturn(systemEvent);

        assertThat(service.isEventEligibleForSend(timestampedEvent)).isTrue();
    }

    private TimestampedEvent<?> mockEventForEligibility() {
        TimestampedEvent<?> eventMock = mock(TimestampedEvent.class);
        PublishingContext publishingContextMock = PublishingContext.builder()
                .withEventingConfigurationCustomerId(CUSTOMER_ID)
                .withEntityType(TABLE_1)
                .withSystemGuid(SYSTEM_GUID)
                .build();
        when(eventMock.getPublishingContext()).thenReturn(publishingContextMock);
        return eventMock;
    }

}
