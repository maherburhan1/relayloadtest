package com.solarwinds.msp.ncentral.eventproduction.controller.impl;

import com.solarwinds.msp.ncentral.eventproduction.api.service.globalControl.EventGetGlobalControlConfiguration;
import com.solarwinds.msp.ncentral.eventproduction.api.service.pinging.EventHealthCheck;
import com.solarwinds.msp.ncentral.eventproduction.api.service.pinging.HealthCheckInfo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.rmi.RemoteException;
import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class EventRelayPingServiceTest {

    @InjectMocks
    private EventRelayPingServiceImpl eventRelayPingService;

    @Mock
    private EventGetGlobalControlConfiguration eventGetGlobalControlConfiguration;
    @Mock
    private EventHealthCheck eventHealthCheck;
    @Mock
    private HealthCheckInfo healthCheckInfo;

    @BeforeEach
    void setUp() throws RemoteException {
        when(eventHealthCheck.ping(any(), any())).thenReturn(healthCheckInfo);
        Set<String> bizappsIds = new HashSet<>();
        bizappsIds.add("BIZAPPS-CUSTOMER-ID");
        when(eventGetGlobalControlConfiguration.getActiveBusinessApplicationsCustomerIds()).thenReturn(bizappsIds);
    }

    @Test
    void testPingToRelaysOK() {
        when(healthCheckInfo.isHealthy()).thenReturn(true);

        assertThat(eventRelayPingService.checkHealthOfAllRelays()).isTrue();
        verify(eventHealthCheck, atLeastOnce()).ping(any(), any());
    }

    @Test
    void testPingToRelaysNotOK() {
        when(healthCheckInfo.isHealthy()).thenReturn(false);

        assertThat(eventRelayPingService.checkHealthOfAllRelays()).isFalse();
        verify(eventHealthCheck, atLeastOnce()).ping(any(), any());
    }
}
