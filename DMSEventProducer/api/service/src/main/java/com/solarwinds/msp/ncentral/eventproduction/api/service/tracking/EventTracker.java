package com.solarwinds.msp.ncentral.eventproduction.api.service.tracking;

import java.rmi.RemoteException;

import com.solarwinds.msp.ncentral.eventproduction.api.entity.EventHighWaterMark;

/**
 * 
 * Provides functionality to track events.
 *
 */
public interface EventTracker {

    /**
     * Records the timestamp of the event.
     * <p>
     * e.g When an event has been intentionally ignored or has been successfully
     * acknowledged recording its timestamp helps in not processing the record
     * again if the data is processed again at later point in time.
     * 
     * @param eventHighWaterMark {@link EventHighWaterMark} details of the event to track
     * @throws RemoteException if an error occurs
     */
    void trackEventTimestamp(EventHighWaterMark eventHighWaterMark) throws RemoteException;

}
