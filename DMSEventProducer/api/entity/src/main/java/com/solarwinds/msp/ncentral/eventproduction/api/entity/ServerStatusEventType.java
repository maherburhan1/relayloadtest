package com.solarwinds.msp.ncentral.eventproduction.api.entity;

/**
 * System control event types for reporting server state to relay.
 */
public enum ServerStatusEventType {
    UNKNOWN,
    SYSTEM_RESTORE,
    EVENTING_FRESH_START;
}
