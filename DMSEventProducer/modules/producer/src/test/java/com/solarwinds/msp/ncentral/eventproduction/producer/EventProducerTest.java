package com.solarwinds.msp.ncentral.eventproduction.producer;

import com.google.protobuf.Message;

import com.solarwinds.msp.ncentral.eventproduction.api.entity.Event;
import com.solarwinds.msp.ncentral.eventproduction.api.entity.ServerStatusEvent;
import com.solarwinds.msp.ncentral.eventproduction.api.entity.ServerStatusEventType;
import com.solarwinds.msp.ncentral.eventproduction.api.entity.Tools;
import com.solarwinds.msp.ncentral.eventproduction.api.service.publisher.PublishingContext;
import com.solarwinds.msp.ncentral.eventproduction.controller.EventController;
import com.solarwinds.msp.ncentral.eventproduction.controller.impl.EventEmissionMonitor;
import com.solarwinds.msp.ncentral.eventproduction.converter.EventParser;
import com.solarwinds.msp.ncentral.eventproduction.converter.ServerStatusEventParser;
import com.solarwinds.msp.ncentral.proto.entity.MspSourceSystemEventOuterClass.MspSourceSystemEvent;
import com.solarwinds.msp.ncentral.proto.entity.entity.ClientOuterClass;
import com.solarwinds.msp.ncentral.proto.entity.entity.DeviceOuterClass;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EventProducerTest<T extends Message> {

    private static final ZonedDateTime TIMESTAMP = ZonedDateTime.now(ZoneId.of("UTC"));
    private static final String ENTITY_TYPE = "entity-ABC";
    private static final String N_CENTRAL_SERVER_GUID = "n-central-server-DEF";
    private static final String BIZAPPS_CUSTOMER_ID = "bizapps-customer-id-GHI";
    private static final Integer EVENTING_CONFIGURATION_CUSTOMER_ID = 1;

    private static final ClientOuterClass.Client client = ClientOuterClass.Client.newBuilder().setClientId(123).build();
    private static final DeviceOuterClass.Device device = DeviceOuterClass.Device.newBuilder().setDeviceId(456).build();

    @Mock
    private EventFilter eventFilterMock;
    @Mock
    private EventParser<T> eventParserMock;
    @Mock
    private ServerStatusEventParser<T> serverStatusEventParserMock;
    @Mock
    private EventController<T> eventsControllerMock;
    @Mock
    private EventEmissionMonitor eventEmissionMonitorMock;
    @Mock
    private Event eventMock;

    @InjectMocks
    private EventProducer<T> eventProducer;

    @Captor
    private ArgumentCaptor<List<T>> eventsCaptor;
    @Captor
    private ArgumentCaptor<ZonedDateTime> timestampCaptor;
    @Captor
    private ArgumentCaptor<PublishingContext> publishingContextCaptor;

    @Test
    void send_does_nothing_when_eventFilter_returns_false() {
        when(eventFilterMock.test(any())).thenReturn(false);

        eventProducer.send(eventMock);

        verify(eventFilterMock).test(same(eventMock));
        verifyNoInteractions(eventParserMock);
        verifyNoInteractions(eventsControllerMock);
        verifyNoInteractions(eventEmissionMonitorMock);
    }

    @Test
    void send_publishes_nothing_when_there_are_no_relevant_changes() {
        when(eventFilterMock.test(any())).thenReturn(true);
        when(eventParserMock.parse(any())).thenReturn(Collections.emptyList());

        eventProducer.send(eventMock);

        verify(eventFilterMock).test(same(eventMock));
        verifyNoInteractions(eventsControllerMock);
        verifyNoInteractions(eventEmissionMonitorMock);
    }

    @Test
    void send_publishes_all_messages_created_in_converter() {
        mockEvent();
        when(eventFilterMock.test(same(eventMock))).thenReturn(true);
        when(eventParserMock.parse(same(eventMock))).thenReturn(createProtobufMessages());

        eventProducer.send(eventMock);

        verify(eventEmissionMonitorMock).recordEmittedEvent(eventMock.getEventingConfigurationCustomerId(),
                eventMock.getEntityType(), eventMock.getUpdateTimestamp());
        verify(eventsControllerMock).publishEvents(eventsCaptor.capture(), timestampCaptor.capture(),
                publishingContextCaptor.capture());

        assertThat(eventsCaptor.getValue()).contains((T) client, (T) device);

        assertThat(timestampCaptor.getValue()).isEqualTo(TIMESTAMP);

        PublishingContext publishingContext = publishingContextCaptor.getValue();
        assertThat(publishingContext.getSystemGuid()).isEqualTo(N_CENTRAL_SERVER_GUID);
        assertThat(publishingContext.getBizappsCustomerId()).isEqualTo(Optional.of(BIZAPPS_CUSTOMER_ID));
        assertThat(publishingContext.getEventingConfigurationCustomerId()).isEqualTo(
                EVENTING_CONFIGURATION_CUSTOMER_ID);
    }

    @Test
    void send_direct_context_uses_skipBuffer() {
        mockEvent();
        when(eventMock.isDirectSend()).thenReturn(true);
        when(eventFilterMock.test(same(eventMock))).thenReturn(true);
        when(eventParserMock.parse(same(eventMock))).thenReturn(createProtobufMessages());

        eventProducer.send(eventMock);

        verify(eventEmissionMonitorMock).recordEmittedEvent(eventMock.getEventingConfigurationCustomerId(),
                eventMock.getEntityType(), eventMock.getUpdateTimestamp());
        verify(eventsControllerMock).publishEvents(any(), any(), publishingContextCaptor.capture());

        PublishingContext publishingContext = publishingContextCaptor.getValue();
        assertTrue(publishingContext.isSkipBuffer());
    }

    @Test
    void publishServerStatus_sends_converted_message_with_context() {
        ServerStatusEvent eventMock = mockServerStatusEvent();

        final T builtEvent = (T) MspSourceSystemEvent.newBuilder()
                .setEventTime(Tools.toTimestamp(TIMESTAMP))
                .setEventType(MspSourceSystemEvent.EventType.UNKNOWN)
                .build();
        when(serverStatusEventParserMock.parse(same(eventMock))).thenReturn(builtEvent);

        eventProducer.publishServerStatus(eventMock);

        verifyNoInteractions(eventEmissionMonitorMock);
        verify(eventsControllerMock).publishEvents(eventsCaptor.capture(), timestampCaptor.capture(),
                publishingContextCaptor.capture());

        assertThat(eventsCaptor.getValue()).contains(builtEvent);

        assertThat(timestampCaptor.getValue()).isEqualTo(TIMESTAMP);

        PublishingContext publishingContext = publishingContextCaptor.getValue();
        assertThat(publishingContext.getSystemGuid()).isEqualTo(N_CENTRAL_SERVER_GUID);
        assertThat(publishingContext.getBizappsCustomerId()).isEqualTo(Optional.of(BIZAPPS_CUSTOMER_ID));
        assertThat(publishingContext.getEventingConfigurationCustomerId()).isEqualTo(
                EVENTING_CONFIGURATION_CUSTOMER_ID);
    }

    @Test
    void publishServerStatus_directSend_contextWithSkipBuffer() {
        ServerStatusEvent eventMock = spy(mockServerStatusEvent());
        when(eventMock.isDirectSend()).thenReturn(true);

        eventProducer.publishServerStatus(eventMock);

        verifyNoInteractions(eventEmissionMonitorMock);
        verify(eventsControllerMock).publishEvents(any(), any(), publishingContextCaptor.capture());

        PublishingContext publishingContext = publishingContextCaptor.getValue();
        assertTrue(publishingContext.isSkipBuffer());
    }

    private ServerStatusEvent mockServerStatusEvent() {
        return ServerStatusEvent.builder()
                .bizappsCustomerId(BIZAPPS_CUSTOMER_ID)
                .eventTime(TIMESTAMP)
                .eventType(ServerStatusEventType.UNKNOWN)
                .ncentralServerGuid(N_CENTRAL_SERVER_GUID)
                .eventingConfigurationCustomerId(EVENTING_CONFIGURATION_CUSTOMER_ID)
                .build();
    }

    private void mockEvent() {
        when(eventMock.getUpdateTimestamp()).thenReturn(TIMESTAMP);
        when(eventMock.getNcentralServerGuid()).thenReturn(N_CENTRAL_SERVER_GUID);
        when(eventMock.getBizappsCustomerId()).thenReturn(Optional.of(BIZAPPS_CUSTOMER_ID));
        when(eventMock.getEntityType()).thenReturn(ENTITY_TYPE);
        when(eventMock.getEventingConfigurationCustomerId()).thenReturn(EVENTING_CONFIGURATION_CUSTOMER_ID);
    }

    private List<T> createProtobufMessages() {
        return Arrays.asList((T) client, (T) device);
    }
}
