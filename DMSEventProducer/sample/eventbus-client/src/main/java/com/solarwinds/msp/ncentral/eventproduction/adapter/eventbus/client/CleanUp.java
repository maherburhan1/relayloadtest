package com.solarwinds.msp.ncentral.eventproduction.adapter.eventbus.client;

import com.solarwinds.msp.ncentral.eventproduction.adapter.eventbus.configuration.EventBusConfig;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class CleanUp {
    private static final Logger LOGGER = LoggerFactory.getLogger(CleanUp.class);

    public void run() {
        LOGGER.info("Cleaning up trust file.");
        EventBusConfig config = new EventBusConfig();
        File eventBusTrustStoreDestinationFile = new File(config.getTrustStoreLocation());
        if (eventBusTrustStoreDestinationFile.length() > 0) {
            eventBusTrustStoreDestinationFile.delete();
        }
    }
}
