package com.solarwinds.msp.ncentral.eventproduction.controller.impl;

import com.nable.logging.Logger;
import com.nable.logging.Loggers;
import com.solarwinds.msp.ncentral.eventproduction.api.service.tracking.EventingControlService;
import com.solarwinds.msp.ncentral.eventproduction.api.service.tracking.EventingStartupListener;

import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This component is responsible for tracking emitted events. It holds the timestamp of the latest emitted event for
 * each Event Production customer ID / table name combination.
 */
@Component
public class EventEmissionMonitor implements EventingStartupListener {

    private static final Logger logger = Loggers.EVENT_PRODUCER;

    static final String CUSTOMER_ID_TABLE_NAME_DELIMITER = ":";
    static final Duration TIMESTAMPS_DIFFERENCE_TOLERANCE = Duration.ofMillis(1);

    private final Map<String, ZonedDateTime> emittedEvents = new ConcurrentHashMap<>();

    private final EventingControlService eventingControlService;

    /**
     * Creates an instance of this component.
     *
     * @param eventingControlService The service responsible for detecting changes in states of the Event Production.
     */
    public EventEmissionMonitor(EventingControlService eventingControlService) {
        this.eventingControlService = eventingControlService;
        eventingControlService.addStartupListenerOrExecuteStartup(this);
    }

    @Override
    public void onEventingStart() {
        emittedEvents.clear();
        logger.info("Component {} initialized.", this.getClass().getSimpleName());
    }

    /**
     * Records the timestamp of an emitted event for a given Event Production customer ID and a given table.
     *
     * @param eventingConfigurationCustomerId the Event Production customer ID.
     * @param tableName the name of the table in which the event has originated.
     * @param timestamp the timestamp of the event.
     */
    public synchronized void recordEmittedEvent(int eventingConfigurationCustomerId, String tableName,
            ZonedDateTime timestamp) {
        emittedEvents.put(getTableIdentifier(eventingConfigurationCustomerId, tableName), timestamp);
    }

    /**
     * Finds out if there could be emitted events that have not been acknowledged yet for a given Event Production
     * customer ID and a given table.
     * <p>
     * This situation occurs if the table has not been fully scraped yet, or if the timestamp of the latest emitted
     * event is not equal to the timestamp of the latest acknowledged event.
     *
     * @param eventingConfigurationCustomerId the Event Production customer ID.
     * @param tableName the name of the table.
     * @param lastAcknowledged the timestamp of the latest acknowledged event.
     * @return {@code true} if there could be not acknowledged events, {@code false} otherwise.
     */
    public boolean unacknowledgedEventsExist(int eventingConfigurationCustomerId, String tableName,
            ZonedDateTime lastAcknowledged) {
        final String tableIdentifier = getTableIdentifier(eventingConfigurationCustomerId, tableName);
        final ZonedDateTime lastEmitted = emittedEvents.get(tableIdentifier);
        return !eventingControlService.getSendingEnabledTables().contains(tableIdentifier) || (lastEmitted != null
                && Duration.between(lastEmitted, lastAcknowledged).abs().toMillis()
                > TIMESTAMPS_DIFFERENCE_TOLERANCE.toMillis());
    }

    /**
     * Logs the content of the map holding the timestamp of the latest emitted event for each Event Production customer
     * ID / table name combination.
     */
    public void logMonitorContent() {
        logger.info("Content of the {} component:", this.getClass().getSimpleName());
        emittedEvents.keySet()
                .forEach(identifier -> logger.info("Table identifier: {}, timestamp of the latest emitted event: {}.",
                        identifier, emittedEvents.get(identifier)));
    }

    private String getTableIdentifier(int eventingConfigurationCustomerId, String tableName) {
        return String.format("%d%s%s", eventingConfigurationCustomerId, CUSTOMER_ID_TABLE_NAME_DELIMITER, tableName);
    }
}
