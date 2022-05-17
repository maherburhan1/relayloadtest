package com.solarwinds.msp.ncentral.eventproduction.controller.impl;

import com.solarwinds.msp.ncentral.eventproduction.api.service.tracking.EventingControlService;
import com.solarwinds.msp.ncentral.eventproduction.controller.EventControllerConfiguration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalTime;
import java.util.Observer;
import java.util.OptionalInt;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ThrottleTest {

    private static final int ACKNOWLEDGEMENT_BUFFER_SIZE = 10;

    private Throttle throttle;
    private Observer observer;
    @Mock
    private AcknowledgementController acknowledgementControllerMock;
    @Mock
    private EventControllerConfiguration eventControllerConfigurationMock;
    @Mock
    private EventingControlService eventingControlServiceMock;

    @BeforeEach
    void setup() {
        ArgumentCaptor<Observer> observerArgumentCaptor = ArgumentCaptor.forClass(Observer.class);
        doNothing().when(acknowledgementControllerMock).addObserver(observerArgumentCaptor.capture());
        when(eventControllerConfigurationMock.getMaximumAcknowledgeBufferSize()).thenReturn(
                OptionalInt.of(ACKNOWLEDGEMENT_BUFFER_SIZE));

        throttle = new Throttle(acknowledgementControllerMock, eventControllerConfigurationMock,
                eventingControlServiceMock);
        verify(eventingControlServiceMock).addStartupListenerOrExecuteStartup(throttle);
        throttle.onEventingStart();

        observer = observerArgumentCaptor.getValue();
    }

    @Test
    void isClosed_returns_false_when_acknowledgement_buffer_is_not_full() {
        setPendingEventsCount(ACKNOWLEDGEMENT_BUFFER_SIZE - 1);
        assertThat(throttle.isClosed()).isFalse();
    }

    private void setPendingEventsCount(int pendingEventsCount) {
        observer.update(acknowledgementControllerMock, pendingEventsCount);
    }

    @Test
    void isClosed_returns_true_when_acknowledgement_buffer_is_full() {
        setPendingEventsCount(ACKNOWLEDGEMENT_BUFFER_SIZE);
        assertThat(throttle.isClosed()).isTrue();
    }

    @Test
    void waitUntilOpen_waits_acknowledgement_buffer_is_not_full() throws InterruptedException, ExecutionException {
        setPendingEventsCount(ACKNOWLEDGEMENT_BUFFER_SIZE);
        Future<LocalTime> futureEndTimeInner = Executors.newSingleThreadExecutor().submit(() -> {
            throttle.waitUntilOpen();
            Thread.sleep(100);
            return LocalTime.now();
        });
        Thread.sleep(500); // give it some time to run

        LocalTime endTimeOuter = LocalTime.now();
        setPendingEventsCount(ACKNOWLEDGEMENT_BUFFER_SIZE - 1);
        Thread.sleep(500); // give it some time to run

        assertThat(futureEndTimeInner.get()).isAfter(endTimeOuter);
    }
}