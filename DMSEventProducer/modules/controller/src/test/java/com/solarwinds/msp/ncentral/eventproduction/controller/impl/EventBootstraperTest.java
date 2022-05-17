package com.solarwinds.msp.ncentral.eventproduction.controller.impl;

import com.solarwinds.msp.ncentral.eventproduction.api.service.connection.EventBusRelayStartUpConfiguration;
import com.solarwinds.msp.ncentral.eventproduction.api.service.scraping.EventScraper;
import com.solarwinds.msp.ncentral.eventproduction.api.service.tracking.EventingControlService;
import com.solarwinds.msp.ncentral.eventproduction.controller.EventBootstraper;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class EventBootstraperTest {

    @Mock
    private EventScraper eventScraperMock;
    @Mock
    private EventingControlService eventingControlService;
    @Mock
    private EventBusRelayStartUpConfiguration eventBusRelayHost;

    @InjectMocks
    private EventBootstraper bootstraper;

    @Test
    void bootstrapEventProduction_success() {
        bootstraper.bootstrapEventProduction();
        verify(eventBusRelayHost).requestMissingEventBusRelayConfiguration();
        verify(eventScraperMock).startupScraping();
        verify(eventingControlService).startEventing();
        assertThat(bootstraper.isBootstrapSequenceCompleted()).isTrue();
    }

    @Test
    void bootstrapEventProduction_failure() {
        doThrow(new RuntimeException()).when(eventScraperMock).startupScraping();
        bootstraper.bootstrapEventProduction();
        verify(eventBusRelayHost).requestMissingEventBusRelayConfiguration();
        verify(eventScraperMock).startupScraping();
        verify(eventingControlService).startEventing();
        verify(eventingControlService).stopEventing();
    }
}
