package com.solarwinds.msp.ncentral.eventproduction.publisher.relay.configuration;

import com.solarwinds.enumeration.event.EventRelayType;
import com.solarwinds.msp.ncentral.eventproduction.publisher.relay.MspRelayPublisher;
import com.solarwinds.util.crypto.MutualSslTlsAuthenticationCredentials;

import java.net.URI;
import java.net.URISyntaxException;
import java.rmi.RemoteException;
import java.time.Duration;
import java.util.Optional;
import java.util.OptionalInt;

/**
 * This interface represents the configuration for the {@link MspRelayPublisher} behavior.
 */
public interface MspRelayConfigurationService {

    /**
     * Gets the URI of MSP Relay from the configuration (for {@link EventRelayType#EVENT} type).
     *
     * @param businessApplicationsCustomerId the business applications customer ID being used to contact MSP Relay.
     * @return The MSP Relay URI.
     * @throws RemoteException if an error occurs when retrieving data
     * @throws URISyntaxException if an error occurs when parsing URI string
     */
    URI getServerUri(String businessApplicationsCustomerId) throws RemoteException, URISyntaxException;

    /**
     * Gets the URI of relay from the configuration by businessApplicationsCustomerId and type.
     *
     * @param businessApplicationsCustomerId the business applications customer ID being used to contact MSP Relay.
     * @param type type of the relay.
     * @return The MSP Relay URI.
     * @throws RemoteException if an error occurs when retrieving data
     * @throws URISyntaxException if an error occurs when parsing URI string
     */
    URI getServerUri(String businessApplicationsCustomerId, EventRelayType type)
            throws RemoteException, URISyntaxException;

    /**
     * Gets the MSP Relay Server certificate used for the trust manager. This method is intended for testing, but may
     * safely be used outside of tests as an alternative to the trust manager.
     *
     * @param businessApplicationsCustomerId the business applications customer ID being used to contact MSP Relay.
     * @return The MSP Relay Server certificate used for the trust manager.
     */
    Optional<String> getServerCertificateForTrustManager(String businessApplicationsCustomerId);

    /**
     * Gets credentials for mutual SSL/TLS authentication.
     *
     * @param bizappsCustomerId the BizApps Customer ID to retrieve credentials for.
     * @return The credentials for mutual SSL/TLS authentication.
     */
    Optional<MutualSslTlsAuthenticationCredentials> getMutualSslTlsAuthenticationCredentials(String bizappsCustomerId);

    /**
     * Gets the hostname or hostname part that is used for overriding of the authority used with TLS and HTTP virtual
     * hosting. It does not change what host is actually connected to. Is commonly in the form {@code host:port}. This
     * method is intended for testing, but may safely be used outside of tests as an alternative to DNS overrides.
     *
     * @return The hostname or hostname part that is used for overriding of the authority.
     */
    Optional<String> getOverrideAuthority();

    /**
     * Gets the maximum size of a batch (the number of messages with the same request context to be sent at once).
     *
     * @return The maximum size of a batch.
     */
    OptionalInt getBatchMaximumSize();

    /**
     * Gets the batch send interval (the time period after witch the accumulated messages are sent).
     *
     * @return The {@link Optional} of {@link Duration} representing the batch send interval.
     */
    Optional<Duration> getBatchSendInterval();

    /**
     * Gets the wait time for a response from the MSP Relay.
     *
     * @return The {@link Optional} of {@link Duration} representing the wait time for a response from the MSP Relay.
     */
    Optional<Duration> getWaitTimeForResponseAfterSending();

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
}
