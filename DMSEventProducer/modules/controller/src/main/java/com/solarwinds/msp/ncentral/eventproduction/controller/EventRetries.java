package com.solarwinds.msp.ncentral.eventproduction.controller;

/**
 * This class contains constants that represents special meaning for number of event send retries for event that failed
 * during the acknowledgement. It also contains various methods related to event retries.
 */
public final class EventRetries {

    /**
     * If the event acknowledgement fail retry to send the event infinitely.
     * <p>
     * <b>Important</b>
     * <p>
     * If you edit this value make sure to edit the system configuration value as well:
     * <p>
     * {@link com.nable.server.common.SysConfigKeys.MspEventProducer#MAXIMUM_EVENT_RETRIES_COUNT_ON_ACKNOWLEDGEMENT_ERROR}
     */
    public static final int INFINITE = -1;

    /**
     * If the event acknowledgement fail do not retry to send the event.
     */
    public static final int NO_RETRIES = 0;

    private EventRetries() {}

    /**
     * Gets the valid value - non negative integer or {@link #INFINITE}.
     *
     * @param value the value to verify.
     * @return The value that is greater than lowest possible valid value, that is {@link #INFINITE}.
     */
    public static int getValidValue(int value) {
        return Math.max(value, INFINITE);
    }
}
