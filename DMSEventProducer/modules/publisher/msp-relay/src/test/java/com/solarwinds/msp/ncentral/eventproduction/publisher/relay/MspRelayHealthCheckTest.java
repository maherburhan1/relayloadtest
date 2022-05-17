package com.solarwinds.msp.ncentral.eventproduction.publisher.relay;

import com.solarwinds.enumeration.event.EventRelayType;
import com.solarwinds.msp.ncentral.eventproduction.api.service.pinging.HealthCheckInfo;
import com.solarwinds.msp.ncentral.eventproduction.api.service.tracking.EventingControlService;
import com.solarwinds.msp.ncentral.eventproduction.publisher.relay.grpc.PublisherProvider;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.net.URISyntaxException;
import java.rmi.RemoteException;

import io.grpc.health.v1.HealthCheckResponse;
import io.grpc.health.v1.HealthGrpc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MspRelayHealthCheckTest {

    @InjectMocks
    private MspRelayHealthCheck mspRelayHealthCheck;

    @Mock
    private PublisherProvider publisherProvider;
    @Mock
    private EventingControlService eventingControlServiceMock;
    private HealthCheckResponse healthCheckResponse;
    private HealthGrpc.HealthBlockingStub healthBlockingStubMock;

    private static final String BUSINESS_APPLICATIONS_ID = "Biz-Apps-ID";

    @BeforeEach
    void setup() throws RemoteException, URISyntaxException {
        mspRelayHealthCheck.onEventingStart();
        verify(eventingControlServiceMock).addStartupListenerOrExecuteStartup(mspRelayHealthCheck);
        mock_publisherData();
    }

    private void mock_publisherData() throws RemoteException, URISyntaxException {
        healthBlockingStubMock = mock(HealthGrpc.HealthBlockingStub.class);
        healthCheckResponse = mock(HealthCheckResponse.class);
        when(publisherProvider.getHealthBlockingStub(any(), any())).thenReturn(healthBlockingStubMock);
        when(healthBlockingStubMock.check(any())).thenReturn(healthCheckResponse);
    }

    private void mock_publisherHealthStubHealthy() {
        when(healthCheckResponse.getStatus()).thenReturn(HealthCheckResponse.ServingStatus.SERVING);
    }

    private void mock_publisherHealthStubNotHealthy() {
        when(healthCheckResponse.getStatus()).thenReturn(HealthCheckResponse.ServingStatus.NOT_SERVING);
    }

    @Test
    void test_OK_ping() {
        mock_publisherHealthStubHealthy();
        HealthCheckInfo response = mspRelayHealthCheck.ping(BUSINESS_APPLICATIONS_ID, EventRelayType.EVENT);
        assertThat(response.isHealthy()).isTrue();
    }

    @Test
    void test_NOT_OK_ping() {
        mock_publisherHealthStubNotHealthy();
        HealthCheckInfo response = mspRelayHealthCheck.ping(BUSINESS_APPLICATIONS_ID, EventRelayType.EVENT);
        assertThat(response.isHealthy()).isFalse();
    }
}
