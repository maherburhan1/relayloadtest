package com.solarwinds.msp.ncentral.eventproduction.converter;

/**
 * This class defines the possible values for how to parse n-central events.
 */
public enum ServiceProcessingRules {
    PROCESS_ALL_EVENTS_AVAILABLE,
    PROCESS_WITH_GENERIC_SERVICE_DATA_ONLY,
    PROCESS_WITH_MAPPED_SERVICE_DATA
}

