package com.solarwinds.msp.ncentral.eventproduction.publisher.relay.grpc;

import com.nable.logging.Logger;
import com.nable.logging.Loggers;
import com.solarwinds.msp.ncentral.eventproduction.api.service.publisher.CircuitBreaker;
import com.solarwinds.msp.ncentral.eventproduction.api.service.publisher.CircuitBreakerResult;
import com.solarwinds.msp.ncentral.eventproduction.publisher.relay.configuration.MspRelayConfigurationService;
import com.solarwinds.util.concurrent.InterruptibleRunnable;

import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * This class implements the command execution wrapper that limits count of subsequent errors by waiting after errors
 * occur.
 */
@Component
public class BatchSenderCircuitBreaker implements CircuitBreaker {

    public static final long DEFAULT_INITIAL_WAIT_TIME_IN_MILLISECONDS = 50L;
    public static final long DEFAULT_MAXIMUM_WAIT_TIME_IN_MILLISECONDS = 60_000L;
    public static final int DEFAULT_WAIT_TIME_INCREASE_FACTOR = 2;

    private static final Logger logger = Loggers.EVENT_PRODUCER;

    private final MspRelayConfigurationService mspRelayConfigurationService;

    private long initialWaitTimeInMilliseconds;
    private long maximumWaitTimeInMilliseconds;
    private int waitTimeIncreaseFactor;

    private volatile boolean isOpen;
    private LocalDateTime nextExecuteTime;
    private long nextWaitTimeInMilliseconds;

    /**
     * Creates an instance of this class with the specified parameters.
     */
    public BatchSenderCircuitBreaker(MspRelayConfigurationService mspRelayConfigurationService) {
        this.mspRelayConfigurationService = mspRelayConfigurationService;
    }

    long getInitialWaitTimeInMilliseconds() {
        return initialWaitTimeInMilliseconds;
    }

    long getMaximumWaitTimeInMilliseconds() {
        return maximumWaitTimeInMilliseconds;
    }

    int getWaitTimeIncreaseFactor() {
        return waitTimeIncreaseFactor;
    }

    long getNextWaitTimeInMilliseconds() {
        return nextWaitTimeInMilliseconds;
    }

    @Override
    public synchronized void initialize() {
        isOpen = false;
        initialWaitTimeInMilliseconds = mspRelayConfigurationService.getInitialWaitTimeAfterError()
                .map(Duration::toMillis)
                .orElse(DEFAULT_INITIAL_WAIT_TIME_IN_MILLISECONDS);
        if (initialWaitTimeInMilliseconds <= 0) {
            throw new IllegalArgumentException("The initialWaitTimeInMilliseconds must be a positive number.");
        }

        maximumWaitTimeInMilliseconds = mspRelayConfigurationService.getMaximumWaitTimeAfterError()
                .map(Duration::toMillis)
                .orElse(DEFAULT_MAXIMUM_WAIT_TIME_IN_MILLISECONDS);
        if (maximumWaitTimeInMilliseconds <= 0) {
            throw new IllegalArgumentException("The maximumWaitTimeInMilliseconds must be a positive number.");
        }

        waitTimeIncreaseFactor = mspRelayConfigurationService.getWaitTimeAfterErrorIncreaseFactor()
                .orElse(DEFAULT_WAIT_TIME_INCREASE_FACTOR);
        if (waitTimeIncreaseFactor <= 1) {
            throw new IllegalArgumentException("The waitTimeIncreaseFactor must be greater than one.");
        }
        resetWaitTime();
    }

    @Override
    public CircuitBreakerResult execute(InterruptibleRunnable command) throws InterruptedException {
        try {
            logger.debug("Executing command: {}", command);
            sleepIfOpen();
            command.run();
            return new CircuitBreakerResult();
        } catch (InterruptedException exception) {
            throw exception;
        } catch (Exception exception) {
            logger.error("Error executing command.", exception);
            handleFailure();
            return new CircuitBreakerResult(exception);
        }
    }

    @Override
    public synchronized void handleSuccess() {
        isOpen = false;
        resetWaitTime();
        notifyAll();
    }

    @Override
    public synchronized void handleFailure() {
        isOpen = true;
        final LocalDateTime now = LocalDateTime.now();
        if (nextExecuteTime == null || now.isAfter(nextExecuteTime)) {
            nextExecuteTime = now.plus(getAndIncreaseWaitTimeInMilliseconds(), ChronoUnit.MILLIS);
        }
    }

    private void resetWaitTime() {
        nextExecuteTime = null;
        nextWaitTimeInMilliseconds = initialWaitTimeInMilliseconds;
    }

    private synchronized void sleepIfOpen() throws InterruptedException {
        while (isOpen) {
            final LocalDateTime now = LocalDateTime.now();
            if (now.isBefore(nextExecuteTime)) {
                final long sleepTime = now.until(nextExecuteTime, ChronoUnit.MILLIS);
                logger.debug("Pausing command execution for {} milliseconds.", sleepTime);
                wait(sleepTime);
            } else {
                break;
            }
        }
    }

    private long getAndIncreaseWaitTimeInMilliseconds() {
        try {
            return nextWaitTimeInMilliseconds;
        } finally {
            nextWaitTimeInMilliseconds *= waitTimeIncreaseFactor;
            if (nextWaitTimeInMilliseconds > maximumWaitTimeInMilliseconds) {
                resetWaitTime();
            }
        }
    }
}
