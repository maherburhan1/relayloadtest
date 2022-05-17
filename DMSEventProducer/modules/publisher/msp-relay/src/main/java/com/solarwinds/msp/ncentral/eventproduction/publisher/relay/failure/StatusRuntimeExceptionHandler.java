package com.solarwinds.msp.ncentral.eventproduction.publisher.relay.failure;

import com.nable.logging.Logger;
import com.nable.logging.Loggers;
import com.solarwinds.dto.Tuple;
import com.solarwinds.msp.ncentral.eventproduction.api.service.failure.Failure;
import com.solarwinds.msp.ncentral.eventproduction.api.service.failure.FailureNotificationService;
import com.solarwinds.msp.ncentral.eventproduction.publisher.relay.configuration.MspRelayCredentialsRefreshService;
import com.solarwinds.msp.ncentral.eventproduction.publisher.relay.configuration.MspRelayUriRefreshService;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.atomic.AtomicBoolean;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;

/**
 * Handles certain gRPC failures during MSP Relay Publishing.
 */
@Component
public class StatusRuntimeExceptionHandler implements Observer {
    private static final Logger logger = Loggers.EVENT_PRODUCER;

    private static final Duration KEYBOX_REQUEST_COOLDOWN_TIME = Duration.ofMinutes(10L);

    private final MspRelayUriRefreshService mspRelayUriRefreshService;
    private final MspRelayCredentialsRefreshService mspRelayCredentialsRefreshService;
    private final FailureNotificationService failureNotificationService;
    private final AtomicBoolean isSubscribed = new AtomicBoolean(false);
    private final Map<Tuple<String, Status.Code>, Instant> currentWaitTimes = new HashMap<>();

    public StatusRuntimeExceptionHandler(MspRelayUriRefreshService mspRelayUriRefreshService,
            MspRelayCredentialsRefreshService mspRelayCredentialsRefreshService,
            FailureNotificationService failureNotificationService) {
        this.mspRelayUriRefreshService = mspRelayUriRefreshService;
        this.mspRelayCredentialsRefreshService = mspRelayCredentialsRefreshService;
        this.failureNotificationService = failureNotificationService;
    }

    /**
     * Subscribes by {@link FailureNotificationService}
     */
    public void subscribeForFailureNotifications() {
        if (!isSubscribed.getAndSet(true)) {
            failureNotificationService.addObserver(this);
        }
    }

    @Override
    public void update(Observable o, Object arg) {
        if (arg instanceof Failure) {
            Failure failure = (Failure) arg;
            ExceptionUtils.getThrowableList(failure.getCaughtException())
                    .stream()
                    .filter(t -> t instanceof StatusRuntimeException)
                    .map(t -> (StatusRuntimeException) t)
                    .findFirst()
                    .ifPresent(statusRuntimeExcetion -> handleFailure(statusRuntimeExcetion,
                            failure.getBusinessApplicationsCustomerId()));
        }
    }

    private void handleFailure(StatusRuntimeException statusRuntimeException, String businessApplicationsCustomerId) {
        final Status.Code statusCode = statusRuntimeException.getStatus().getCode();
        if (canMakeKeyBoxRequest(statusCode, businessApplicationsCustomerId)) {
            if (isCredentialIssue(statusCode)) {
                logger.warn(
                        "The [{}] component is attempting to recover from gRPC credentials error [{}] for Business Applications Customer ID [{}]",
                        getClass(), statusRuntimeException, businessApplicationsCustomerId);
                mspRelayCredentialsRefreshService.requestNewClientCertificateForAuthentication(
                        businessApplicationsCustomerId);
            } else if (isRelayHostIssue(statusCode)) {
                logger.warn(
                        "The [{}] component is attempting to recover from gRPC host error [{}] for Business Applications Customer ID [{}]",
                        getClass(), statusRuntimeException, businessApplicationsCustomerId);
                mspRelayUriRefreshService.requestEventBusRelayUri(businessApplicationsCustomerId);
            }
        }
    }

    private synchronized boolean canMakeKeyBoxRequest(Status.Code statusCode, String businessApplicationsCustomerId) {
        Tuple<String, Status.Code> key = new Tuple<>(businessApplicationsCustomerId, statusCode);
        if (!currentWaitTimes.containsKey(key)) {
            currentWaitTimes.put(key, Instant.now().plus(KEYBOX_REQUEST_COOLDOWN_TIME));
            return true;
        }

        Instant waitUntilTime = currentWaitTimes.get(key);
        if (Instant.now().isAfter(waitUntilTime)) {
            currentWaitTimes.put(key, Instant.now().plus(KEYBOX_REQUEST_COOLDOWN_TIME));
            return true;
        }

        return false;
    }

    private boolean isCredentialIssue(Status.Code statusCode) {
        switch (statusCode) {
            case PERMISSION_DENIED:
            case UNAUTHENTICATED:
                return true;
            default:
                return false;
        }
    }

    private boolean isRelayHostIssue(Status.Code statusCode) {
        return statusCode.equals(Status.Code.UNAVAILABLE);
    }
}