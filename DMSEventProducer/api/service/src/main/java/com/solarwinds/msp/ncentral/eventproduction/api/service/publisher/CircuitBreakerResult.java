package com.solarwinds.msp.ncentral.eventproduction.api.service.publisher;

/**
 * This class represents the Result of the CircuitBreaker execute method.
 */
public class CircuitBreakerResult {

    private Exception exception;

    public CircuitBreakerResult() {}

    public CircuitBreakerResult(Exception exception) {
        this.exception = exception;
    }

    /**
     * Runs the specified command in case of failure.
     *
     * @param onFailureCommand the {@link Runnable} command that should be executed in case of failure.
     */
    public void onFailure(Runnable onFailureCommand) {
        if (exception != null) {
            onFailureCommand.run();
        }
    }
}
