package com.solarwinds.msp.ncentral.eventproduction.controller;

import java.time.Duration;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;

/**
 * Supplies configuration for {@link EventController} behavior.
 */
public interface EventControllerConfiguration {
    /**
     * @return {@code true} - events should be emitted / {@code false} events should not be emitted
     */
    boolean isEventsEmissionEnabled();

    /**
     * @return The percentage of the jetty memory that the memory buffer is limited to use.
     */
    OptionalDouble getMemoryBufferPercentageSize();

    /**
     * @return The percentage of the disk space that the file buffer is limited to use.
     */
    OptionalDouble getFileBufferPercentageSize();

    /**
     * @return Maximum size (count of events) of buffer for acknowledging sent events.
     */
    OptionalInt getMaximumAcknowledgeBufferSize();

    /**
     * Gets the period of time after which an email notification is sent if N-central is not being able to successfully
     * send events.
     *
     * @return The {@link Optional} of {@link Duration} representing the wait time before an email notification is sent.
     */
    Optional<Duration> getWaitTimeForUnsentEventsNotification();

    /**
     * Gets the wait time after the first error occurs.
     *
     * @return The {@link Optional} of {@link Duration} representing the wait time after the first error occurs.
     */
    Optional<Duration> getInitialWaitTimeAfterError();

    /**
     * Gets the maximum wait time after a subsequent error occurs.
     *
     * @return The {@link Optional} of {@link Duration} representing the wait time after a subsequent error occurs.
     */
    Optional<Duration> getMaximumWaitTimeAfterError();

    /**
     * Gets a factor by which wait time is increased when a subsequent error occurs.
     *
     * @return The factor by which wait time is increased when a subsequent error occurs.
     */
    OptionalInt getWaitTimeAfterErrorIncreaseFactor();

    /**
     * Gets the wait time for a response from the MSP Relay.
     *
     * @return The {@link Optional} of {@link Duration} representing the wait time for a response from the MSP Relay.
     */
    Optional<Duration> getWaitTimeForResponseAfterSending();

    /**
     * Gets the maximum count of event send retries in a case of an event acknowledgement error.
     *
     * @return The maximum count of event send retries in a case of an event acknowledgement error.
     */
    OptionalInt getMaximumEventRetriesCountOnAcknowledgementError();

    /**
     * Gets the amount of time the buffer controller will wait between pings to the Relay health service.
     *
     * @return The amount of time between Relay pings.
     */
    Optional<Duration> getBufferControllerWaitTimeBetweenRelayPings();
}
