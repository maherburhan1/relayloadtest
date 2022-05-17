package com.solarwinds.msp.ncentral.eventproduction.controller;

/**
 * Interface defining methods needed to ping MSP Relays in order to check health.
 */
public interface EventRelayPingService {

    /**
     * Pings all MSP Relays and then indicates whether they are all healthy and ready to receive events.
     *
     * @return boolean indicating whether all MSP Relays are OK.
     */
    boolean checkHealthOfAllRelays();
}
