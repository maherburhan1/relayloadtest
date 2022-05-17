package com.solarwinds.msp.ncentral.eventproduction.publisher.relay.configuration;

import java.util.Observable;

/**
 * Service interface for requesting a client SSL/TLS authentication certificate for specified Business Applications
 * Customer ID. The service notifies all subscribers on newly created or changed certificates.
 */
public abstract class MspRelayCredentialsRefreshService extends Observable {

    /**
     * Requests a new client SSL/TLS authentication certificate. If a new certificate has been created or an existing
     * one updated, then all observers are notified of this change using the {@link MspRelayConfigurationChange} object
     * instance.
     *
     * @param businessApplicationsCustomerId the Business Applications Customer ID to request the certificate for.
     */
    public abstract void requestNewClientCertificateForAuthentication(String businessApplicationsCustomerId);
}