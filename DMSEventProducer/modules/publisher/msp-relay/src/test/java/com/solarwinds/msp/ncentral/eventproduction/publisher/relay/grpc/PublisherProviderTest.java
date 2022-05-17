package com.solarwinds.msp.ncentral.eventproduction.publisher.relay.grpc;

import com.solarwinds.enumeration.event.EventRelayType;
import com.solarwinds.msp.ncentral.eventproduction.publisher.relay.configuration.MspRelayConfigurationChange;
import com.solarwinds.msp.ncentral.eventproduction.publisher.relay.configuration.MspRelayConfigurationService;
import com.solarwinds.msp.ncentral.eventproduction.publisher.relay.configuration.MspRelayCredentialsRefreshService;
import com.solarwinds.msp.ncentral.eventproduction.publisher.relay.configuration.MspRelayUriRefreshService;
import com.solarwinds.util.crypto.MutualSslTlsAuthenticationCredentials;
import com.solarwinds.msp.ncentral.eventproduction.publisher.relay.failure.StatusRuntimeExceptionHandler;
import com.solarwinds.msp.relay.PublisherGrpc;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import io.grpc.ManagedChannel;
import io.grpc.health.v1.HealthGrpc;
import io.grpc.netty.shaded.io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.shaded.io.grpc.netty.NettyChannelBuilder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * This class represents the unit test of the {@link PublisherProvider} class.
 */
@ExtendWith(MockitoExtension.class)
class PublisherProviderTest {

    private static final URI RELAY_URL = mock(URI.class);
    private static final String OVERRIDE_AUTHORITY = "relay.com";
    private static final String SECONDARY_OVERRIDE_AUTHORITY = "secondary.relay.com";
    private static final String BUSINESS_APPLICATIONS_CUSTOMER_ID = "Bizapps-Customer-ID";

    private static final String RELAY_CLIENT_CERTIFICATE =
            "-----BEGIN CERTIFICATE-----\n" + "MIIDNTCCAh2gAwIBAgIRAIp03aS/49zuJ7LBh8Qd7UkwDQYJKoZIhvcNAQELBQAw\n"
                    + "JzEVMBMGA1UEChMMY2VydC1tYW5hZ2VyMQ4wDAYDVQQDEwVyZWxheTAeFw0xOTA3\n"
                    + "MjYxOTI2MDJaFw0yMDA3MjUxOTI2MDJaMDwxFTATBgNVBAoTDGNlcnQtbWFuYWdl\n"
                    + "cjEjMCEGA1UEAxMabi1jZW50cmFsOmluc3RhbmNlOmNsaWVudDEwggEiMA0GCSqG\n"
                    + "SIb3DQEBAQUAA4IBDwAwggEKAoIBAQCl4BSqucQ5FRgN3dZSKNCARdLfJQCW96xT\n"
                    + "Rw9jE/tP70cBQ3O+/iP7E0QIgkFdS9tBuz7mRnjJCczj8aThxUXbAPE1+yRksexc\n"
                    + "9DuL4E0Ger/xIy7tWn9HzyVWOWvNUCbHSHs9DWbqA3FJWbxdy+SIxjkivkIf6MFA\n"
                    + "S909sB0Hv6ZWKT1aIrnK2g2DIz+siXNGclShrKodoCbbjD4HJUB3ioMFBzZ3D/rP\n"
                    + "hp90zs+qdBtpFe4clteVXk2NWcW5zOetXtkdL6ghO01BBGEzjTVyB0JkX9OvG3rA\n"
                    + "naWHJyCWgWQWKLMxFem+AK7mUNdTy6DC3dQjoiPwrqSk1a9zVGt9AgMBAAGjRzBF\n"
                    + "MA4GA1UdDwEB/wQEAwIFoDAMBgNVHRMBAf8EAjAAMCUGA1UdEQQeMByCGm4tY2Vu\n"
                    + "dHJhbDppbnN0YW5jZTpjbGllbnQxMA0GCSqGSIb3DQEBCwUAA4IBAQB+GH83WN1T\n"
                    + "02dIMhJJiFk3LvM4GcSOE4Xvd16K0AvwahSGW+v0z8mAeZsa7v7DnLFhxwUmrcVi\n"
                    + "pxzywAYMbyJVxKsagzI7JdtC5K7t7wkbVllNi/FXdp3oxl10APW9jyMwLq5kQjUz\n"
                    + "ZxuxFlkKEgaXxS8MJmDt+87muGYE1oshnjMRS8IngYYbZ/XcLlRKl7M6Y7e/FHOB\n"
                    + "n9wv9Vb0S7HNRXz2nX5e0hev80TWra6YMqDxTCauzhMfQ2neelORic1n0b+3aGon\n"
                    + "H1zdqIvKXqN4bGmfnVikZrXmZ63ip0KnqIqcbAzSvvbhFAp50kBZUq+IMY9EeL1A\n" + "iqd/0hTO4Jua\n"
                    + "-----END CERTIFICATE-----";
    private static final String RELAY_CLIENT_PRIVATE_KEY =
            "-----BEGIN PRIVATE KEY-----\n" + "MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQCl4BSqucQ5FRgN\n"
                    + "3dZSKNCARdLfJQCW96xTRw9jE/tP70cBQ3O+/iP7E0QIgkFdS9tBuz7mRnjJCczj\n"
                    + "8aThxUXbAPE1+yRksexc9DuL4E0Ger/xIy7tWn9HzyVWOWvNUCbHSHs9DWbqA3FJ\n"
                    + "Wbxdy+SIxjkivkIf6MFAS909sB0Hv6ZWKT1aIrnK2g2DIz+siXNGclShrKodoCbb\n"
                    + "jD4HJUB3ioMFBzZ3D/rPhp90zs+qdBtpFe4clteVXk2NWcW5zOetXtkdL6ghO01B\n"
                    + "BGEzjTVyB0JkX9OvG3rAnaWHJyCWgWQWKLMxFem+AK7mUNdTy6DC3dQjoiPwrqSk\n"
                    + "1a9zVGt9AgMBAAECggEAYaTmJZYTR5L+RPNovZafHlN8DbWngjcr7x3uleFmWCi/\n"
                    + "wKOnWDAcmClALE4ZEkWcPTZvkXi8k4O8ZBaUe/733zLPy3oac1g1joaLIw0X6o+J\n"
                    + "u/Z9ImXadTggf4Bv0D21hixJt6fTBECgiJBVjVWAI2+kpKohHM2ULTg7dZvdivg4\n"
                    + "mcKdAFWrRKRYHILybZTE1nPGkJL4GE5MDzbl0HR7w19/T3K6LkFv2odZRt2l2xHh\n"
                    + "hhEe7PxAOFG6xuDdieuTnOdIyj3h3fupsudyKTOEK/QOScDkIakrEMe30zAbFyFO\n"
                    + "eVrV4kO8mFYEOEIWuHy6eX+ILG8fKam56mvoMtyBNQKBgQDETcikxSxrw1QHQgNC\n"
                    + "TuX5GtdybQ/HuPmtpPLEP2Xf72CDX3N9iWkeX8JY06OnOSEdXolG59pitiYFuvwb\n"
                    + "T3HS6nz8/zlJVc+pQNttaVfp026r/BP3qhPiz0FkmI+7HwryA/G4CHLuhqleJyh2\n"
                    + "rbJIUPARCzJd9BsZAtpBG6tEvwKBgQDYUXElqiHe/CVUhGd/LVlacYPZ0n/86GFa\n"
                    + "bu/rUm0Bmh5eR4Pnvlki0L8ibiI3ANtcAOj55lqLfvtscLkyPLHMyyT5xnjvLQI6\n"
                    + "Otoh80x9zyDYFtQGhkWZglpD+nn4vsY3ZMLhdTctmBzRVrqn4uEdUbLujZhd6ccu\n"
                    + "Ton5yz5ywwKBgD+/CqMHLI+qFIVu2GQV/vRP1Rrhc7hlxx6ua+9yrwBzWaIPww9H\n"
                    + "Kg2lBxWVVJWfJbPF2UpfTDm18M73k5rCx7G4V/JIZy/7X74Uw6zaAqR59nx/FXTd\n"
                    + "lCuZJiEmsbYQNJu20C31AG2Y6TMcADXZ3gVB01OVkTuVY59Rn9J4y7s9AoGBAMqZ\n"
                    + "tLSHHQQg+sGm5Nx7V9XavZgUjGUOLKgrgS/Q2mGGvGym5Ib3g7qU8n9H0Wnctpb8\n"
                    + "DziHJETEoK0XAo1nxUs+9Jmv5vJ0IELzffwQSJMInQVmlBysQ6GcMfi7LuPnofSd\n"
                    + "vdy2EQD2F/+FEHtPhnIUY9cD7TIAU9ouGxJG5m1LAoGAMy70zUbNw4Yqo4XcfksN\n"
                    + "7bC7eyFYofvyOjCm7vk2kM5rGHsuu5cVp3NEB7tKkdqiy3y9Cwc0KKMXH4UMOX1w\n"
                    + "ALdYfJzPAlMNKfC060VptbIDZVaSL2heW7bCZy8m/11/yBfshUuOCNE6WT4Bjha0\n"
                    + "eZd6eRf8fUbZoCeTjd3bdHA=\n" + "-----END PRIVATE KEY-----";
    public static final String RELAY_SERVER_PRIVATE_KEY =
            "-----BEGIN CERTIFICATE-----\n" + "MIIDEzCCAfugAwIBAgIRAKCexhyyqahoof0B7HHTDU4wDQYJKoZIhvcNAQELBQAw\n"
                    + "JzEVMBMGA1UEChMMY2VydC1tYW5hZ2VyMQ4wDAYDVQQDEwVyZWxheTAeFw0xOTA4\n"
                    + "MjYxODQwMDhaFw0xOTA5MjUxOTQwMDhaMCsxFTATBgNVBAoTDGNlcnQtbWFuYWdl\n"
                    + "cjESMBAGA1UEAxMJcmVsYXkuZGV2MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIB\n"
                    + "CgKCAQEA2mggFKpDVcmXpUuUzjUASN32pZjxhF/H/c4vzGeQQgOselhFtA7jj9KZ\n"
                    + "h35RUtn8oomMZs30jaibdlH74fPFvpNBzX0dFqhbeprwRISTXJHwko5Q4Eu/n4yH\n"
                    + "zztSxiyXoUFzRzCSRrUQjhV8bqDTEeXhqryRCVfysH3k3gOiQq7aUZvUTuK87LVU\n"
                    + "RZ+JAmnOvm14t3OVXkXDHPKmALqdqtf5yw6WiI4Wb3W3Tg+OjqMeH2uANSg0S5U0\n"
                    + "2r7h0yKZVKgQ1ijS50zRdscJ7hAjg9sKxm9mR+UfYy89C6WVYs4wYJuGQ//K8PkQ\n"
                    + "hvKozcvuH+YfmMtxgIoOQ3TvHmhQkQIDAQABozYwNDAOBgNVHQ8BAf8EBAMCBaAw\n"
                    + "DAYDVR0TAQH/BAIwADAUBgNVHREEDTALgglyZWxheS5kZXYwDQYJKoZIhvcNAQEL\n"
                    + "BQADggEBAE8sFs2Trukjh8uVQw21Yrl0iIT2aRRuqpipCYAbY4mZBD7XXfMLJrxr\n"
                    + "C0dKD+0LbSiVGZpzBlsPRxAVzxIqQ+/njpkhmPqNh3YX6XxEvEJDW5OA6D3NcgEO\n"
                    + "DaRmS6Y1WENaSLB4+qdsAk4cFHbMr5II845x9n2QZPQ1Kn3CVgaQwGSt5/j7RWJ0\n"
                    + "cA+7Xkec8/UPZDAvW905C90jJbjyRjH8M1jbdEkOWgtQw0S/kIkPrZ+l3OSnWJkT\n"
                    + "OF0RqA/Gmossctg2nUPa0wRydaz/WRGdpB+esMectt459j06TQ4Y6uWTnxfZLlxl\n"
                    + "++4jpYS9mGGO+tKJngI3dzyw6EX9ImA=\n" + "-----END CERTIFICATE-----";

    @Mock
    private MspRelayConfigurationService mspRelayConfigurationServiceMock;
    @Mock
    private MspRelayUriRefreshService mspRelayUriRefreshServiceMock;
    @Mock
    private MspRelayCredentialsRefreshService mspRelayCredentialsRefreshServiceMock;
    @Mock
    private StatusRuntimeExceptionHandler statusRuntimeExceptionHandlerMock;
    @InjectMocks
    private PublisherProvider publisherProvider;

    @Mock
    private MspRelayConfigurationChange connectionConfigurationChangeMock;

    @Test
    void constructor_invokes_statusRuntimeExceptionHandler() {
        verify(statusRuntimeExceptionHandlerMock).subscribeForFailureNotifications();
    }

    @Test
    void constructor_adds_itself_as_MspRelayUriRefreshService_observer() {
        verify(mspRelayUriRefreshServiceMock).addObserver(publisherProvider);
    }

    @Test
    void constructor_adds_itself_as_MspRelayCredentialsRefreshService_observer() {
        verify(mspRelayCredentialsRefreshServiceMock).addObserver(publisherProvider);
    }

    @Test
    void getPublisherFutureStub() throws Exception {
        when(mspRelayConfigurationServiceMock.getServerUri(any(), any())).thenReturn(RELAY_URL);
        when(mspRelayConfigurationServiceMock.getOverrideAuthority()).thenReturn(Optional.of(OVERRIDE_AUTHORITY));
        when(mspRelayConfigurationServiceMock.getMutualSslTlsAuthenticationCredentials(any())).thenReturn(
                createMutualSslTlsAuthenticationCredentials());
        when(mspRelayConfigurationServiceMock.getServerCertificateForTrustManager(any())).thenReturn(
                Optional.of(RELAY_SERVER_PRIVATE_KEY));
        when(RELAY_URL.getHost()).thenReturn("Test.Relay");
        when(RELAY_URL.getPort()).thenReturn(100);

        final FuturePublisher expectedResult =
                createFuturePublisher(RELAY_URL.getHost(), RELAY_URL.getPort(), OVERRIDE_AUTHORITY, true);

        final FuturePublisher result = publisherProvider.getFuturePublisher(BUSINESS_APPLICATIONS_CUSTOMER_ID);

        assertThat(result).usingRecursiveComparison()
                .ignoringFieldsMatchingRegexes(".*allocationSite.timestamp", ".*channelCreationTimeNanos",
                        ".*channelStr", ".*logId.*", ".*sslContext.ctx",
                        ".*sslContext.sessionContext.provider.materialMap.chain",
                        ".*sslContext.sessionContext.provider.materialMap.leak",
                        ".*sslContext.sessionContext.provider.materialMap.privateKey", ".*stopwatch.startTick")
                .isEqualTo(expectedResult);
    }

    @Test
    void getHealthBlockingStub() throws Exception {
        when(mspRelayConfigurationServiceMock.getOverrideAuthority()).thenReturn(Optional.of(OVERRIDE_AUTHORITY));
        when(mspRelayConfigurationServiceMock.getServerUri(any(), any())).thenReturn(RELAY_URL);
        when(mspRelayConfigurationServiceMock.getMutualSslTlsAuthenticationCredentials(any())).thenReturn(
                createMutualSslTlsAuthenticationCredentials());
        when(RELAY_URL.getHost()).thenReturn("Test.Relay");

        final HealthGrpc.HealthBlockingStub expectedResult =
                createHealthBlockingStub(RELAY_URL.getHost(), RELAY_URL.getPort(), OVERRIDE_AUTHORITY, true);

        final HealthGrpc.HealthBlockingStub result =
                publisherProvider.getHealthBlockingStub(BUSINESS_APPLICATIONS_CUSTOMER_ID, EventRelayType.EVENT);

        assertThat(result).usingRecursiveComparison()
                .ignoringFieldsMatchingRegexes(".*channelCreationTimeNanos", ".*logId.*", ".*stopwatch.startTick",
                        ".*sslContext.ctx", ".*channelStr", ".*materialMap.*")
                .isEqualTo(expectedResult);
    }

    @Test
    void close() throws Exception {
        when(mspRelayConfigurationServiceMock.getServerUri(any(), any())).thenReturn(RELAY_URL);
        when(RELAY_URL.getHost()).thenReturn("Test.Relay");

        final ManagedChannel managedChannel =
                (ManagedChannel) publisherProvider.getFuturePublisher(BUSINESS_APPLICATIONS_CUSTOMER_ID)
                        .getStub()
                        .getChannel();
        assertThat(managedChannel.isShutdown()).isFalse();

        publisherProvider.close();
        assertThat(managedChannel.isShutdown()).isTrue();
    }

    @Test
    void connectionConfigurationChangeUpdatesAssociatedChannel() throws Exception {
        when(connectionConfigurationChangeMock.isCertificateChanged()).thenReturn(true);
        when(connectionConfigurationChangeMock.getBizAppsCustomerId()).thenReturn(BUSINESS_APPLICATIONS_CUSTOMER_ID);
        when(mspRelayConfigurationServiceMock.getOverrideAuthority()).thenReturn(Optional.of(OVERRIDE_AUTHORITY));
        when(mspRelayConfigurationServiceMock.getServerUri(any(), any())).thenReturn(RELAY_URL);
        when(mspRelayConfigurationServiceMock.getMutualSslTlsAuthenticationCredentials(any())).thenReturn(
                createMutualSslTlsAuthenticationCredentials());
        when(RELAY_URL.getHost()).thenReturn("Test.Relay");

        // Get the initial stub information from publisher.
        FuturePublisher previousStub = publisherProvider.getFuturePublisher(BUSINESS_APPLICATIONS_CUSTOMER_ID);
        assertThat(previousStub).isNotNull();

        // Signal that a config change was made, this will remove the existing channel object so that it will be recreated.
        publisherProvider.update(null, connectionConfigurationChangeMock);

        // Get the publisher stub again, which should be re-created since the previous entry was signalled as invalid.
        when(mspRelayConfigurationServiceMock.getOverrideAuthority()).thenReturn(
                Optional.of(SECONDARY_OVERRIDE_AUTHORITY));
        FuturePublisher newStub = publisherProvider.getFuturePublisher(BUSINESS_APPLICATIONS_CUSTOMER_ID);
        assertThat(newStub).isNotNull();
        assertThat(newStub.getStub().getChannel().authority()).isNotEqualTo(
                previousStub.getStub().getChannel().authority());
    }

    private Optional<MutualSslTlsAuthenticationCredentials> createMutualSslTlsAuthenticationCredentials() {
        return Optional.of(MutualSslTlsAuthenticationCredentials.builder()
                .clientX509CertificatePemEncoded(RELAY_CLIENT_CERTIFICATE)
                .clientPkcs8PrivateKeyPemEncoded(RELAY_CLIENT_PRIVATE_KEY)
                .build());
    }

    private FuturePublisher createFuturePublisher(String host, int port, String overrideAuthority,
            boolean withCertificates) throws Exception {
        return new FuturePublisher(createPublisherFutureStub(host, port, overrideAuthority, withCertificates));
    }

    private HealthGrpc.HealthBlockingStub createHealthBlockingStub(String host, int port, String overrideAuthority,
            boolean withCertificates) throws Exception {
        return HealthGrpc.newBlockingStub(createManagedChannel(host, port, overrideAuthority, withCertificates));
    }

    private PublisherGrpc.PublisherFutureStub createPublisherFutureStub(String host, int port, String overrideAuthority,
            boolean withCertificates) throws Exception {
        return PublisherGrpc.newFutureStub(createManagedChannel(host, port, overrideAuthority, withCertificates));
    }

    private ManagedChannel createManagedChannel(String host, int port, String overrideAuthority,
            boolean withCertificates) throws Exception {
        final NettyChannelBuilder channelBuilder =
                NettyChannelBuilder.forAddress(host, port).overrideAuthority(overrideAuthority);
        if (withCertificates) {
            channelBuilder.sslContext(GrpcSslContexts.forClient()
                    .keyManager(getInputStream(RELAY_CLIENT_CERTIFICATE), getInputStream(RELAY_CLIENT_PRIVATE_KEY))
                    .trustManager(getInputStream(RELAY_SERVER_PRIVATE_KEY))
                    .build());
        }
        return channelBuilder.build();
    }

    private ByteArrayInputStream getInputStream(String value) {
        return new ByteArrayInputStream(value.getBytes(StandardCharsets.US_ASCII));
    }
}