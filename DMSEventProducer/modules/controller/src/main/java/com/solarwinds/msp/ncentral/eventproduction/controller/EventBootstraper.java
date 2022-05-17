package com.solarwinds.msp.ncentral.eventproduction.controller;

import com.nable.logging.Logger;
import com.nable.logging.Loggers;
import com.solarwinds.msp.ncentral.eventproduction.api.service.connection.EventBusRelayStartUpConfiguration;
import com.solarwinds.msp.ncentral.eventproduction.api.service.scraping.EventScraper;
import com.solarwinds.msp.ncentral.eventproduction.api.service.tracking.EventingControlService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Event production bootstrapper.
 */
@Component
public class EventBootstraper {

    private static final Logger logger = Loggers.EVENT_PRODUCER;
    private static AtomicBoolean eventBootStrapperInitialized = new AtomicBoolean();

    private final EventScraper eventScraper;
    private final EventingControlService eventingControlService;
    private final EventBusRelayStartUpConfiguration eventBusRelayHost;

    @Autowired
    public EventBootstraper(EventScraper eventScraper, EventingControlService eventingControlService,
            EventBusRelayStartUpConfiguration eventBusRelayHost) {
        this.eventScraper = eventScraper;
        this.eventingControlService = eventingControlService;
        this.eventBusRelayHost = eventBusRelayHost;
    }

    /**
     * Bootstraps event production after system is started.
     */
    public synchronized void bootstrapEventProduction() {
        logger.info("Event production bootstrap started.");
        try {
            eventBusRelayHost.requestMissingEventBusRelayConfiguration();
            eventingControlService.startEventing();
            eventScraper.startupScraping();
            eventBootStrapperInitialized.compareAndSet(false, true);
            logger.info("Event production bootstrap finished.");
        } catch (Exception e) {
            logger.error("Error has occurred during event production bootstrap, stopping eventing.", e);
            eventingControlService.stopEventing();
            eventBootStrapperInitialized.compareAndSet(true, false);
        }
    }

    /**
     * Returns true if the event production bootstrap sequence is already completed.
     *
     * @return true if event production bootstrap sequence has been completed and false otherwise.
     */
    public boolean isBootstrapSequenceCompleted() {
        return eventBootStrapperInitialized.get();
    }
}
