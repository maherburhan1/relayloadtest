package com.solarwinds.msp.ncentral.eventproduction.controller.impl;

import com.nable.logging.Logger;
import com.nable.logging.Loggers;
import com.solarwinds.msp.ncentral.common.time.TimeService;
import com.solarwinds.msp.ncentral.eventproduction.api.service.tracking.EventingControlService;
import com.solarwinds.msp.ncentral.eventproduction.api.service.tracking.EventingStartupListener;
import com.solarwinds.msp.ncentral.eventproduction.controller.EventControllerConfiguration;
import com.solarwinds.util.concurrent.InterruptibleRunnable;

import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;

/**
 * Command execution wrapper that limits count of subsequent errors by waiting after errors occur.
 */
@Component
public class CircuitBreaker implements EventingStartupListener {

    private static final Logger logger = Loggers.EVENT_PRODUCER;

    private static final long DEFAULT_MAXIMUM_WAIT_TIME_IN_MILLISECONDS_AFTER_ERROR = 60_000L;
    private static final int DEFAULT_WAIT_TIME_AFTER_ERROR_INCREASE_FACTOR = 2;
    private static final long DEFAULT_INITIAL_WAIT_TIME_IN_MILLISECONDS_AFTER_ERROR = 50L;

    private long initialWaitTimeAfterErrorInMillis = DEFAULT_INITIAL_WAIT_TIME_IN_MILLISECONDS_AFTER_ERROR;
    private long maximumWaitTimeAfterErrorInMillis = DEFAULT_MAXIMUM_WAIT_TIME_IN_MILLISECONDS_AFTER_ERROR;
    private int waitTimeAfterErrorIncreaseFactor = DEFAULT_WAIT_TIME_AFTER_ERROR_INCREASE_FACTOR;

    private boolean isOpen;
    private Instant nextExecuteTime;
    private long nextWaitTimeAfterErrorInMillis;

    private final EventControllerConfiguration eventControllerConfiguration;
    private final TimeService timeService;

    public CircuitBreaker(EventControllerConfiguration eventControllerConfiguration,
            EventingControlService eventingControlService, TimeService timeService) {
        this.eventControllerConfiguration = eventControllerConfiguration;
        this.timeService = timeService;
        eventingControlService.addStartupListenerOrExecuteStartup(this);
    }

    @Override
    public void onEventingStart() {
        initialWaitTimeAfterErrorInMillis = eventControllerConfiguration.getInitialWaitTimeAfterError()
                .map(Duration::toMillis)
                .orElse(DEFAULT_INITIAL_WAIT_TIME_IN_MILLISECONDS_AFTER_ERROR);
        maximumWaitTimeAfterErrorInMillis = eventControllerConfiguration.getMaximumWaitTimeAfterError()
                .map(Duration::toMillis)
                .orElse(DEFAULT_MAXIMUM_WAIT_TIME_IN_MILLISECONDS_AFTER_ERROR);
        waitTimeAfterErrorIncreaseFactor = eventControllerConfiguration.getWaitTimeAfterErrorIncreaseFactor()
                .orElse(DEFAULT_WAIT_TIME_AFTER_ERROR_INCREASE_FACTOR);

        if (initialWaitTimeAfterErrorInMillis <= 0) {
            throw new IllegalStateException("initialWaitTimeAfterErrorInMillis must not be positive");
        }
        if (maximumWaitTimeAfterErrorInMillis <= 0) {
            throw new IllegalStateException("maximumWaitTimeAfterErrorInMillis must not be positive");
        }
        if (waitTimeAfterErrorIncreaseFactor <= 1) {
            throw new IllegalStateException("waitTimeAfterErrorIncreaseFactor must not be greater than one");
        }

        resetWaitTimeAfterError();
        logger.info("Component {} initialized.", this.getClass().getSimpleName());
    }

    private void resetWaitTimeAfterError() {
        nextWaitTimeAfterErrorInMillis = initialWaitTimeAfterErrorInMillis;
    }

    /**
     * Runs given command wrapped with a circuit breaker logic that limits count of subsequent errors by waiting after
     * an error occurs during the command execution. Exponential backoff algorithm is used to determine the wait time.
     *
     * @param command the command to execute.
     * @return The {@link Result} that can invoke additional command in case of error.
     * @throws InterruptedException if the execution was interrupted while waiting.
     */
    public synchronized Result execute(InterruptibleRunnable command) throws InterruptedException {
        try {
            sleepIfOpen();
            command.run();
            handleSuccess();
            return new Result();
        } catch (InterruptedException exception) {
            throw exception;
        } catch (Exception exception) {
            handlerFailure(exception);
            return new Result(exception);
        }
    }

    private void sleepIfOpen() throws InterruptedException {
        if (isOpen) {
            Instant now = Instant.now();
            if (now.isBefore(nextExecuteTime)) {
                logger.info("Pausing command execution until {}", nextExecuteTime.atZone(ZoneId.systemDefault()));
                timeService.sleep(Duration.between(now, nextExecuteTime));
            }
        }
    }

    private void handleSuccess() {
        isOpen = false;
        nextExecuteTime = null;
        resetWaitTimeAfterError();
    }

    private void handlerFailure(Exception e) {
        isOpen = true;
        nextExecuteTime = Instant.now().plus(getAndIncreaseWaitTimeAfterErrorInMillis(), ChronoUnit.MILLIS);
        logger.info("Error executing command", e);
    }

    private long getAndIncreaseWaitTimeAfterErrorInMillis() {
        try {
            return nextWaitTimeAfterErrorInMillis;
        } finally {
            nextWaitTimeAfterErrorInMillis *= waitTimeAfterErrorIncreaseFactor;
            if (nextWaitTimeAfterErrorInMillis > maximumWaitTimeAfterErrorInMillis) {
                resetWaitTimeAfterError();
            }
        }
    }

    public static class Result {
        private Exception exception;

        private Result() {
        }

        private Result(Exception exception) {
            this();
            this.exception = exception;
        }

        void onFailure(Runnable onFailureCommand) {
            if (exception != null) {
                onFailureCommand.run();
            }
        }

        boolean callFailed() {
            return exception != null;
        }
    }
}
