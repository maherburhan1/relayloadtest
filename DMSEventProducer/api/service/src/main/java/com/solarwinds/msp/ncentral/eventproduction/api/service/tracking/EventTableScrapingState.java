package com.solarwinds.msp.ncentral.eventproduction.api.service.tracking;

public enum EventTableScrapingState {
    /**
     * Scraping should be skipped - for use cases such as adding new fresh table for eventing, which does not need to go
     * through scraping and can go straight to live eventing.
     */
    SKIP,
    /**
     * Scraping is in progress, sending is forbidden for a table that has scraping in progress, but it is possible to
     * buffer events during scraping for affected table.
     */
    IN_PROGRESS,
    /**
     * Scraping has finished, all standard eventing mechanisms are allowed for live eventing.
     */
    FINISHED,
    /**
     * Scraping has failed, we should disable buffering and sending for the table.
     */
    FAILED;
}
