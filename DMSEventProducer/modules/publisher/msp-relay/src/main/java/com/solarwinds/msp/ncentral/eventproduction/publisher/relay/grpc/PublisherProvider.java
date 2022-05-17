package com.solarwinds.msp.ncentral.eventproduction.publisher.relay.grpc;

import com.nable.logging.Logger;
import com.nable.logging.Loggers;
import com.solarwinds.enumeration.event.EventRelayType;
import com.solarwinds.msp.ncentral.eventproduction.publisher.relay.configuration.MspRelayConfigurationChange;
import com.solarwinds.msp.ncentral.eventproduction.publisher.relay.configuration.MspRelayConfigurationService;
import com.solarwinds.msp.ncentral.eventproduction.publisher.relay.configuration.MspRelayCredentialsRefreshService;
import com.solarwinds.msp.ncentral.eventproduction.publisher.relay.configuration.MspRelayUriRefreshService;
import com.solarwinds.util.crypto.MutualSslTlsAuthenticationCredentials;
import com.solarwinds.msp.ncentral.eventproduction.publisher.relay.failure.StatusRuntimeExceptionHandler;
import com.solarwinds.msp.relay.PublisherGrpc;
import com.solarwinds.msp.relay.PublisherGrpc.PublisherFutureStub;

import org.jooq.lambda.Unchecked;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Observable;
import java.util.Observer;
import java.util.Optional;

import javax.annotation.concurrent.ThreadSafe;
import javax.net.ssl.SSLException;

import io.grpc.ManagedChannel;
import io.grpc.health.v1.HealthGrpc;
import io.grpc.health.v1.HealthGrpc.HealthBlockingStub;
import io.grpc.netty.shaded.io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.shaded.io.grpc.netty.NettyChannelBuilder;
import io.grpc.netty.shaded.io.netty.handler.ssl.SslContext;
import io.grpc.netty.shaded.io.netty.handler.ssl.SslContextBuilder;

/**
 * Provides gRPC client stub for the MSP Relay Publisher service.
 */
@Component
@ThreadSafe
public class PublisherProvider extends Observable implements AutoCloseable, Observer {

    private static final Logger logger = Loggers.EVENT_PRODUCER;

    private final MspRelayConfigurationService mspRelayConfigurationService;
    private final Map<String, ManagedChannel> managedPublishingChannels;
    private final Map<String, ManagedChannel> managedEventHealthChannels;

    /**
     * Creates a new instance of this class with the specified parameters.
     */
    @Autowired
    public PublisherProvider(MspRelayConfigurationService mspRelayConfigurationService,
            MspRelayUriRefreshService mspRelayUriRefreshService,
            MspRelayCredentialsRefreshService mspRelayCredentialsRefreshService,
            StatusRuntimeExceptionHandler statusRuntimeExceptionHandler) {
        this.mspRelayConfigurationService = mspRelayConfigurationService;

        mspRelayUriRefreshService.addObserver(this);
        mspRelayCredentialsRefreshService.addObserver(this);
        statusRuntimeExceptionHandler.subscribeForFailureNotifications();

        managedPublishingChannels = new HashMap<>();
        managedEventHealthChannels = new HashMap<>();
    }

    /**
     * Gets the {@link FuturePublisher} instance with the wrapped {@link PublisherFutureStub} instance.
     *
     * @param businessApplicationsCustomerId The Business Applications Customer ID being used to publish.
     * @return The {@link FuturePublisher} instance with the wrapped {@link PublisherFutureStub} instance.
     */
    public FuturePublisher getFuturePublisher(String businessApplicationsCustomerId)
            throws RemoteException, URISyntaxException {
        checkRelayCredentials(businessApplicationsCustomerId);
        PublisherFutureStub publisherFutureStub =
                PublisherGrpc.newFutureStub(getManagedPublishingChannel(businessApplicationsCustomerId));
        return new FuturePublisher(publisherFutureStub);
    }

    /**
     * Gets the {@link HealthBlockingStub} instance.
     *
     * @param businessApplicationsCustomerId the Business Applications Customer ID to retrieve the health blocking stub
     * for.
     * @return The {@link HealthBlockingStub} instance.
     */
    public HealthBlockingStub getHealthBlockingStub(String businessApplicationsCustomerId, EventRelayType relayType)
            throws RemoteException, URISyntaxException {
        checkRelayCredentials(businessApplicationsCustomerId);
        return HealthGrpc.newBlockingStub(getManagedHealthCheckChannel(businessApplicationsCustomerId, relayType));
    }

    @Override
    public void close() {
        shutdownChannels();
        logger.info("Shutdown of the MSP Relay gRPC channels requested.");
    }

    @Override
    public void update(Observable o, Object arg) {
        if (arg instanceof MspRelayConfigurationChange) {
            final MspRelayConfigurationChange configurationChange = (MspRelayConfigurationChange) arg;
            if (configurationChange.isRelayHostChanged() || configurationChange.isCertificateChanged()) {
                deleteChannelsForBusinessApplicationsCustomerId(configurationChange.getBizAppsCustomerId());
                logger.info(
                        "The Publisher Provider has observed a change in the MSP Relay connection configuration for Business Applications Customer ID [{}]. The associated connection will be re-created with the new configuration details.",
                        configurationChange.getBizAppsCustomerId());
            }
        }
    }

    private void checkRelayCredentials(String businessApplicationsCustomerId) {
        if (!mspRelayConfigurationService.getMutualSslTlsAuthenticationCredentials(businessApplicationsCustomerId)
                .isPresent()) {
            logger.warn("No credentials for MSP Relay authentication found.");
        }
    }

    private ManagedChannel getManagedPublishingChannel(String businessApplicationsCustomerId)
            throws RemoteException, URISyntaxException {
        if (!managedPublishingChannels.containsKey(businessApplicationsCustomerId)) {
            managedPublishingChannels.put(businessApplicationsCustomerId,
                    createManagedChannel(businessApplicationsCustomerId, EventRelayType.EVENT));
        }
        return managedPublishingChannels.get(businessApplicationsCustomerId);
    }

    private ManagedChannel getManagedHealthCheckChannel(String businessApplicationsCustomerId, EventRelayType relayType)
            throws RemoteException, URISyntaxException {

        if (!managedEventHealthChannels.containsKey(businessApplicationsCustomerId)) {
            managedEventHealthChannels.put(businessApplicationsCustomerId,
                    createManagedChannel(businessApplicationsCustomerId, relayType));
        }
        return managedEventHealthChannels.get(businessApplicationsCustomerId);
    }

    private ManagedChannel createManagedChannel(String businessApplicationsCustomerId, EventRelayType relayType)
            throws RemoteException, URISyntaxException {
        final URI relayUrl = getRelayUrl(businessApplicationsCustomerId, relayType);
        final NettyChannelBuilder channelBuilder = createChannelBuilder(relayUrl);
        mspRelayConfigurationService.getMutualSslTlsAuthenticationCredentials(businessApplicationsCustomerId)
                    .ifPresent(Unchecked.consumer(credentials -> channelBuilder.sslContext(
                            createSslContext(credentials, businessApplicationsCustomerId))));
        return channelBuilder.build();
    }

    /**
     * Removes the channels for the specified Business Applications Customer ID. The next time the code attempts to
     * publish or health check for the specified Business Applications Customer ID, they will be recreated since the key
     * was deleted from the mapping.
     */
    private void deleteChannelsForBusinessApplicationsCustomerId(String businessApplicationsCustomerId) {
        managedPublishingChannels.remove(businessApplicationsCustomerId);
        managedEventHealthChannels.remove(businessApplicationsCustomerId);
        notifyObserversOfChannelChange(businessApplicationsCustomerId);
    }

    private void notifyObserversOfChannelChange(String businessApplicationsCustomerId) {
        setChanged();
        notifyObservers(businessApplicationsCustomerId);
    }

    private URI getRelayUrl(String businessApplicationsCustomerId, EventRelayType relayType)
            throws RemoteException, URISyntaxException {
        return mspRelayConfigurationService.getServerUri(businessApplicationsCustomerId, relayType);
    }

    private NettyChannelBuilder createChannelBuilder(URI relayUrl) {
        final NettyChannelBuilder channelBuilder =
                NettyChannelBuilder.forAddress(relayUrl.getHost(), relayUrl.getPort());

        final Optional<String> overrideAuthority = mspRelayConfigurationService.getOverrideAuthority();
        overrideAuthority.ifPresent(channelBuilder::overrideAuthority);

        return channelBuilder;
    }

    private SslContext createSslContext(MutualSslTlsAuthenticationCredentials credentials,
            String businessApplicationsCustomerId) throws SSLException {
        final SslContextBuilder builder = createSslContextBuilder(credentials);

        final Optional<String> serverCertificate =
                mspRelayConfigurationService.getServerCertificateForTrustManager(businessApplicationsCustomerId);
        serverCertificate.ifPresent(certificate -> builder.trustManager(getInputStream(certificate)));
        return builder.build();
    }

    private SslContextBuilder createSslContextBuilder(MutualSslTlsAuthenticationCredentials credentials) {
        return GrpcSslContexts.forClient()
                .keyManager(getInputStream(credentials.getClientX509CertificatePemEncoded()),
                        getInputStream(credentials.getClientPkcs8PrivateKeyPemEncoded()));
    }

    private ByteArrayInputStream getInputStream(String value) {
        return new ByteArrayInputStream(value.getBytes(StandardCharsets.US_ASCII));
    }

    private void shutdownChannels() {
        managedPublishingChannels.values().stream().filter(Objects::nonNull).forEach(ManagedChannel::shutdown);
        managedEventHealthChannels.values().stream().filter(Objects::nonNull).forEach(ManagedChannel::shutdown);
    }
}