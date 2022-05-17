package com.solarwinds.msp.ncentral.eventproduction.api.service.scraping;

import com.solarwinds.msp.ncentral.eventproduction.api.service.tracking.EventingControlService;

/**
 * Export existing entities to events.
 */
public interface EventScraper {

    /**
     * Starts scraping after system starts. Checks enabled eventing configurations and delegates startup on {@link
     * EventingControlService}.
     */
    void startupScraping();
}