package com.solarwinds.msp.ncentral.eventproduction.controller.impl;

import com.google.protobuf.GeneratedMessageV3;

import com.solarwinds.msp.ncentral.eventproduction.api.service.persistence.TimestampedEvent;
import com.solarwinds.msp.ncentral.eventproduction.api.service.publisher.EventPublisher;
import com.solarwinds.msp.ncentral.eventproduction.api.service.publisher.PublishedEventInfo;
import com.solarwinds.msp.ncentral.eventproduction.api.service.publisher.PublishingContext;
import com.solarwinds.msp.ncentral.eventproduction.controller.EventWithFuture;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.concurrent.Future;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.notNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class EventSenderTest {
    private static final ZonedDateTime TIME_STAMP = ZonedDateTime.now(ZoneId.of("UTC"));

    @Mock
    private EventPublisher eventPublisherMock;
    @Mock
    private AcknowledgementController acknowledgementControllerMock;
    @InjectMocks
    private EventSender<GeneratedMessageV3> eventSender;
    @Mock
    private TimestampedEvent<GeneratedMessageV3> timestampedEventMock;
    @Mock
    private PublishingContext publishingContextMock;
    @Mock
    private GeneratedMessageV3 generatedMessageV3Mock;
    @Mock
    private Future<PublishedEventInfo> publishedEventInfoFutureMock;
    @Captor
    private ArgumentCaptor<EventWithFuture> eventWithFutureArgumentCaptor;
    @Mock
    private Runnable onAcknowledgeFailureCommandMock;

    @Test
    void send_calls_eventPublisher_and_acknowledgementController() throws InterruptedException {
        initializeEventAndPublisherMocks();

        eventSender.forEvent(timestampedEventMock).onAcknowledgeFailure(onAcknowledgeFailureCommandMock).send();

        verify(eventPublisherMock).publish(eq(generatedMessageV3Mock), eq(publishingContextMock));
        verify(acknowledgementControllerMock).acknowledgeEvent(eventWithFutureArgumentCaptor.capture(),
                eq(onAcknowledgeFailureCommandMock));

        EventWithFuture eventWithFuture = eventWithFutureArgumentCaptor.getValue();
        assertThat(eventWithFuture.getFuture()).isSameAs(publishedEventInfoFutureMock);
        assertThat(eventWithFuture.getEvent()).isSameAs(generatedMessageV3Mock);
        assertThat(eventWithFuture.getTimestamp()).isEqualTo(TIME_STAMP);
    }

    private void initializeEventAndPublisherMocks() throws InterruptedException {
        when(timestampedEventMock.getEvent()).thenReturn(generatedMessageV3Mock);
        when(timestampedEventMock.getTimestamp()).thenReturn(TIME_STAMP);
        when(timestampedEventMock.getPublishingContext()).thenReturn(publishingContextMock);
        when(eventPublisherMock.publish(any(), any())).thenReturn(Optional.of(publishedEventInfoFutureMock));
    }

    @Test
    void send_uses_dummy_onAcknowledgeFailureCommand_when_it_is_not_specified() throws InterruptedException {
        initializeEventAndPublisherMocks();

        eventSender.forEvent(timestampedEventMock).send();

        verify(acknowledgementControllerMock).acknowledgeEvent(eventWithFutureArgumentCaptor.capture(), notNull());
    }

    @Test
    void send_calls_only_eventPublisher_when_publisher_returns_empty_future() throws InterruptedException {
        when(timestampedEventMock.getEvent()).thenReturn(generatedMessageV3Mock);
        when(timestampedEventMock.getPublishingContext()).thenReturn(publishingContextMock);
        when(eventPublisherMock.publish(any(), any())).thenReturn(Optional.empty());

        eventSender.forEvent(timestampedEventMock).send();

        verify(eventPublisherMock).publish(eq(generatedMessageV3Mock), eq(publishingContextMock));
        verifyNoInteractions(acknowledgementControllerMock);
    }

    @Test
    void send_throws_InterruptedException_when_eventPublisher_throws_InterruptedException()
            throws InterruptedException {
        when(timestampedEventMock.getEvent()).thenReturn(generatedMessageV3Mock);
        when(timestampedEventMock.getPublishingContext()).thenReturn(publishingContextMock);
        InterruptedException interruptedException = new InterruptedException();
        when(eventPublisherMock.publish(any(), any())).thenThrow(interruptedException);

        assertThatThrownBy(() -> eventSender.forEvent(timestampedEventMock).send()).isSameAs(interruptedException);

        verify(eventPublisherMock).publish(eq(generatedMessageV3Mock), eq(publishingContextMock));
    }

    @Test
    void send_throws_InterruptedException_when_acknowledgementController_throws_InterruptedException()
            throws InterruptedException {
        initializeEventAndPublisherMocks();
        InterruptedException interruptedException = new InterruptedException();
        doThrow(interruptedException).when(acknowledgementControllerMock).acknowledgeEvent(any(), any());

        assertThatThrownBy(() -> eventSender.forEvent(timestampedEventMock).send()).isSameAs(interruptedException);
    }
}