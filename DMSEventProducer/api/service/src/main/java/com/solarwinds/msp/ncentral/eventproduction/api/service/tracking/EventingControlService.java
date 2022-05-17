package com.solarwinds.msp.ncentral.eventproduction.api.service.tracking;

import com.nable.logging.Logger;
import com.nable.logging.Loggers;
import com.solarwinds.msp.ncentral.eventproduction.api.entity.EventWithContext;
import com.solarwinds.msp.ncentral.eventproduction.api.entity.ServerStatusEvent;
import com.solarwinds.msp.ncentral.eventproduction.api.service.persistence.TimestampedEvent;
import com.solarwinds.msp.ncentral.proto.entity.MspSourceSystemEventOuterClass;

import org.springframework.stereotype.Service;

import java.util.LinkedHashSet;
import java.util.Observable;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Service for controlling various states of eventing designed to control/notify components to change their behavior
 * based on current state of eventing.
 */
@Service
public class EventingControlService extends Observable {

    private static final Logger logger = Loggers.EVENT_PRODUCER;

    static final String TABLE_ID_NAME_DELIMITER = ":";

    private final AtomicBoolean eventingStartupExecuted = new AtomicBoolean(false);

    private final AtomicBoolean globalSendingEnabled = new AtomicBoolean(true);
    private final AtomicBoolean globalScrapingEnabled = new AtomicBoolean(true);

    private final Set<String> sendingEnabledTables = ConcurrentHashMap.newKeySet();
    private final Set<String> bufferingEnabledTables = ConcurrentHashMap.newKeySet();
    private final Set<Integer> eventingEnabledIds = ConcurrentHashMap.newKeySet();

    // LinkedHashSet to preserve order inserted (i.e. preserve spring load order)
    private final Set<EventingStartupListener> startupListeners = new LinkedHashSet<>();
    private final Lock startupListenersAccess = new ReentrantLock();

    /**
     * Adds listener to be notified when eventing is started, more precisely, when {@link
     * EventingControlService#startEventing()} is invoked. Either adds listener for later notification (eager loaded
     * components) in case startup wasn't yet executed, or executes onEventingStart straight away (lazy loaded
     * components).
     *
     * @param listener listener to be registered for startup event
     */
    public void addStartupListenerOrExecuteStartup(EventingStartupListener listener) {
        startupListenersAccess.lock();
        try {
            if (eventingStartupExecuted.get()) {
                // This really shouldn't happen since anything that touches EventBootstrapper will initialize everything else
                // before code in bootstrapper is initialized, but it's safety handling for the case where some component
                // would get initialized by spring after eventing start was called
                listener.onEventingStart();
            }

            // And each listener must be added into the list so that it's called when eventing feature flag si switched on
            startupListeners.add(listener);
        } finally {
            startupListenersAccess.unlock();
        }
    }

    /**
     * @return set of table identifiers (customerid:tableName) that are enabled for sending
     */
    public Set<String> getSendingEnabledTables() {
        return sendingEnabledTables;
    }

    /**
     * @return set of table identifiers (customerid:tableName) that are enabled for buffering
     */
    Set<String> getBufferingEnabledTables() {
        return bufferingEnabledTables;
    }

    /**
     * Tests if the Event Production is currently enabled for a given customer.
     *
     * @param customerId the customer ID.
     * @return {@code true} if Event Production is enabled for the customer, {@code false} otherwise.
     */
    public boolean isEventingEnabledForCustomer(int customerId) {
        return eventingEnabledIds.contains(customerId);
    }

    /**
     * Tests if event with given customer and type is eligible for sending from buffer.
     *
     * @return true if event can be sent, false if it should be prevented from being sent
     */
    public <T> boolean isEventEligibleForSend(TimestampedEvent<T> event) {
        return testEventEligibility(sendingEnabledTables, event);
    }

    /**
     * Tests if event with given customer and type can be inserted into buffer.
     *
     * @return true if event can be buffered, false if it should not be added to buffer
     */
    public boolean canEventBeInsertedIntoBuffer(EventWithContext event) {
        return bufferingEnabledTables.contains(
                event.getEventingConfigurationCustomerId() + TABLE_ID_NAME_DELIMITER + event.getEntityType())
                || event instanceof ServerStatusEvent;

    }

    private <T> boolean testEventEligibility(Set<String> stateTables, TimestampedEvent<T> event) {
        return stateTables.contains(
                event.getPublishingContext().getEventingConfigurationCustomerId() + TABLE_ID_NAME_DELIMITER
                        + event.getPublishingContext().getEntityType())
                || event.getEvent() instanceof MspSourceSystemEventOuterClass.MspSourceSystemEvent;
    }

    /**
     * @return true if events should be send to relay, false otherwise
     */
    public synchronized boolean getGlobalSendingEnabled() {
        return globalSendingEnabled.get();
    }

    /**
     * @return true if event scraping is enabled, false otherwise
     */
    public synchronized boolean getGlobalScrapingEnabled() {
        return globalScrapingEnabled.get();
    }

    /**
     * Starts eventing, allowing for general event processing and scraping and notifies all components.
     */
    public void startEventing() {
        startupListenersAccess.lock();
        try {
            logger.info("Eventing is starting.");
            eventingStartupExecuted.set(true);
            startupListeners.forEach(EventingStartupListener::onEventingStart);
            logger.info("Eventing components initialized.");
        } finally {
            startupListenersAccess.unlock();
        }

        updateEventingState(true);
        logger.info("Eventing started.");
    }

    /**
     * Stops eventing, disabling general event processing and scraping and notifies all components.
     */
    public void stopEventing() {
        logger.info("Eventing was stopped. Stopping all components.");
        updateEventingState(false);
    }

    private void updateEventingState(boolean eventingEnabled) {
        globalSendingEnabled.set(eventingEnabled);
        globalScrapingEnabled.set(eventingEnabled);

        setChangedAndNotifyObservers();
    }

    /**
     * Updates current backup status so that components can reflect correct behavior for running backup.
     *
     * @param backupRunning true if backup is running, false otherwise
     */
    public void updateSystemBackupStatus(boolean backupRunning) {
        globalSendingEnabled.set(!backupRunning);
        globalScrapingEnabled.set(!backupRunning);
        if (eventingStartupExecuted.get()) {
            if (backupRunning) {
                logger.info("System backup is running, pausing scraping and events sending.");
            } else {
                logger.info("System backup is not running, resuming scraping and events sending.");
            }
            setChangedAndNotifyObservers();
        } else {
            if (backupRunning) {
                logger.info("System backup is running and eventing is disabled, "
                        + "marking scraping/sending to be paused in case it gets enabled.");
            } else {
                logger.info("System backup is not running and eventing is disabled, "
                        + "marking scraping/sending to be running in case it gets enabled.");
            }
        }
    }

    /**
     * Updates eventing state for given set of tables. On enable, sending and buffering for provided customer are
     * cleared and tables are queued for scraping and scraper is notified, on disable, tables are disabled from sending
     * and buffering, removed from tables to be scraped and all components are notified. If table names are empty or
     * null and eventing is set to false, eventing is completely turned off for given customer.
     *
     * @param eventingEnabled enabled or disabled eventing state
     * @param customerId customer id where eventing is configured
     * @throws IllegalArgumentException if eventingEnabled is true and empty or null tableNames are provided
     */
    public synchronized void updateEventingStateForCustomer(boolean eventingEnabled, int customerId) {
        final ScrapingConfigurationChange.ScrapingConfigurationChangeBuilder scrapingConfigurationChangeBuilder =
                ScrapingConfigurationChange.builder().withCustomerId(customerId);
        final EventingConfigurationChange eventingConfigurationChange =
                new EventingConfigurationChange().setSendingConfigurationChanged();
        if (eventingEnabled) {
            logger.info("Eventing configuration was enabled for customer {}, (re)starting scraper, "
                    + "disabling sending and buffering settings for all tables for provided customer.", customerId);

            sendingEnabledTables.removeIf(
                    tableIdentifier -> tableIdentifier.startsWith(customerId + TABLE_ID_NAME_DELIMITER));
            bufferingEnabledTables.removeIf(
                    tableIdentifier -> tableIdentifier.startsWith(customerId + TABLE_ID_NAME_DELIMITER));
            eventingEnabledIds.add(customerId);

            scrapingConfigurationChangeBuilder.restartScraping();
        } else {
            logger.info("Eventing configuration was disabled for customer {}, stopping any ongoing scraping, "
                    + "disabling online sending and cleaning buffered data.", customerId);
            disableTablesForEventing(customerId);
            eventingEnabledIds.remove(customerId);
            scrapingConfigurationChangeBuilder.stopScraping();
            // we want to notify buffering change only on disable, otherwise we could accidentally lose some DELETEs
            // on the way for example when eventing is switched to higher level
            eventingConfigurationChange.setRemoveEventsForCustomer(customerId);
        }

        setChangedAndNotifyObservers(eventingConfigurationChange);
        setChangedAndNotifyObservers(scrapingConfigurationChangeBuilder.build());
    }

    private void disableTablesForEventing(int customerId) {
        sendingEnabledTables.removeIf(
                tableIdentifier -> tableIdentifier.startsWith(customerId + TABLE_ID_NAME_DELIMITER));
        bufferingEnabledTables.removeIf(
                tableIdentifier -> tableIdentifier.startsWith(customerId + TABLE_ID_NAME_DELIMITER));
    }

    /**
     * Single table status update, for example when scraping is finished.
     */
    public synchronized void updateStateForTable(EventTableStateChange eventTableState) {
        logger.info("Scraping is now in state {} for table {} and customer {}.", eventTableState.getScrapingState(),
                eventTableState.getTableName(), eventTableState.getCustomerId());

        String tableIdentifier =
                eventTableState.getCustomerId() + TABLE_ID_NAME_DELIMITER + eventTableState.getTableName();

        switch (eventTableState.getScrapingState()) {
            case FAILED:
                bufferingEnabledTables.remove(tableIdentifier);
                sendingEnabledTables.remove(tableIdentifier);
                logger.debug("Scraping failed: Disabling buffering and sending for table {} and customer {}.",
                        eventTableState.getTableName(), eventTableState.getCustomerId());
                break;
            case IN_PROGRESS:
                bufferingEnabledTables.add(tableIdentifier);
                logger.debug("Buffering enabled for the table {} and customer {}.", eventTableState.getTableName(),
                        eventTableState.getCustomerId());
                break;
            case FINISHED:
                sendingEnabledTables.add(tableIdentifier);
                logger.debug("Sending enabled for the table {} and customer {}.", eventTableState.getTableName(),
                        eventTableState.getCustomerId());
                break;
            case SKIP:
                bufferingEnabledTables.add(tableIdentifier);
                sendingEnabledTables.add(tableIdentifier);
                logger.debug("Scraping skip: Sending and buffering enabled for the table {} and customer {}.",
                        eventTableState.getTableName(), eventTableState.getCustomerId());
                break;
        }
        setChangedAndNotifyObservers(new EventingConfigurationChange().setSendingConfigurationChanged());
    }

    private void setChangedAndNotifyObservers() {
        setChanged();
        notifyObservers();
    }

    private void setChangedAndNotifyObservers(EventingConfigurationChange eventingConfigurationChange) {
        setChanged();
        notifyObservers(eventingConfigurationChange);
    }

    private void setChangedAndNotifyObservers(ScrapingConfigurationChange scrapingConfigurationChange) {
        setChanged();
        notifyObservers(scrapingConfigurationChange);
    }
}
