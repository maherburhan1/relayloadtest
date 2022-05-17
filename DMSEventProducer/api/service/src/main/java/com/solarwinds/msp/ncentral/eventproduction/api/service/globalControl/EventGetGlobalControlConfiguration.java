package com.solarwinds.msp.ncentral.eventproduction.api.service.globalControl;

import java.rmi.RemoteException;
import java.util.Set;

/**
 * Responsible for retrieving Global Control configuration data.
 */
public interface EventGetGlobalControlConfiguration {
    /**
     * Gets a list of all Business Applications Customer Ids configured for eventing in the system.
     *
     * @return A {@link Set} of all Business Applications Customer Ids configured for eventing.
     * @throws RemoteException if an error was encountered while trying to retrieve Bizapps Customer Ids.
     */
    Set<String> getActiveBusinessApplicationsCustomerIds() throws RemoteException;
}