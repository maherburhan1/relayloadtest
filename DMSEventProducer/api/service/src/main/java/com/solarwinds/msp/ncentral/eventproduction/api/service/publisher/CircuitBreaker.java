package com.solarwinds.msp.ncentral.eventproduction.api.service.publisher;

import com.solarwinds.util.concurrent.InterruptibleRunnable;

/**
 * This interface represents the command execution wrapper that limits count of subsequent errors by waiting after
 * errors occur.
 */
public interface CircuitBreaker {

    /**
     * Initializes this class using configuration options.
     *
     * @throws IllegalArgumentException if cannot get valid configuration options, e.g. initial/maximum wait time or
     * wait time increase factor.
     */
    void initialize();

    /**
     * Runs given command wrapped with a circuit breaker logic that limits count of subsequent errors by waiting after
     * an error occurs during the command execution. Exponential backoff algorithm is used to determine the wait time.
     *
     * @param command the to execute.
     * @return The {@link CircuitBreakerResult} that can invoke additional command in case of error.
     * @throws InterruptedException if interrupted while waiting
     */
    CircuitBreakerResult execute(InterruptibleRunnable command) throws InterruptedException;

    /**
     * Handles the successful command result - notify the Circuit Breaker. This method should be called when a success
     * has occurred during the command post processing.
     */
    void handleSuccess();

    /**
     * Handles the failed command result - notify the Circuit Breaker. This method should be called when a failure has
     * occurred during the command post processing.
     */
    void handleFailure();
}
