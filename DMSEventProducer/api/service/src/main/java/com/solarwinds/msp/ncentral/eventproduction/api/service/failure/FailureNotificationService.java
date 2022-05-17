package com.solarwinds.msp.ncentral.eventproduction.api.service.failure;

import com.solarwinds.util.NullChecker;

import org.springframework.stereotype.Service;

import java.util.Observable;

/**
 * Service responsible for notifying observers when certain exceptions occur in eventing.
 */
@Service
public class FailureNotificationService extends Observable {

    /**
     * Register the specified exception so that the relevant observers can handle the failure. The Business Applications
     * Customer ID which published the event is included if recovery actions are to be triggered (new certificate or
     * relay host requested).
     *
     * @param businessApplicationsCustomerId The Business Applications Customer ID that attempted to publish the event.
     * @param exception The associated exception from the gRPC failure.
     */
    public void registerException(String businessApplicationsCustomerId, Throwable exception) {
        updateObservers(businessApplicationsCustomerId, NullChecker.check(exception, "exception"));
    }

    private void updateObservers(String businessApplicationsCustomerId, Throwable exception) {
        Failure observedFailure = Failure.builder()
                .withBusinessApplicationsCustomerId(businessApplicationsCustomerId)
                .withThrowable(exception)
                .build();
        setChanged();
        notifyObservers(observedFailure);
    }
}