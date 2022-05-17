package com.solarwinds.msp.ncentral.eventproduction.controller.impl;

import com.solarwinds.msp.ncentral.common.time.TimeService;
import com.solarwinds.msp.ncentral.eventproduction.api.service.tracking.EventingControlService;
import com.solarwinds.msp.ncentral.eventproduction.controller.EventControllerConfiguration;
import com.solarwinds.util.concurrent.InterruptibleRunnable;

import org.assertj.core.data.Percentage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.internal.stubbing.answers.AnswersWithDelay;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.OptionalInt;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CircuitBreakerTest {

    private static final long INITIAL_WAIT_TIME_AFTER_ERROR_IN_MILLIS = 1_000L;
    private static final long MAXIMUM_WAIT_TIME_AFTER_ERROR_IN_MILLIS = 4_000L;
    private static final int WAIT_TIME_AFTER_ERROR_INCREASE_FACTOR = 2;

    @InjectMocks
    private CircuitBreaker circuitBreaker;
    @Mock
    private EventControllerConfiguration eventControllerConfigurationMock;
    @Mock
    private EventingControlService eventingControlServiceMock;
    @Mock
    private TimeService timeServiceMock;
    @Mock
    private InterruptibleRunnable commandOkMock;
    @Mock
    private InterruptibleRunnable commandFailingMock;
    @Mock
    private Runnable onFailureCommandMock;

    @BeforeEach
    void setup() {
        when(eventControllerConfigurationMock.getInitialWaitTimeAfterError()).thenReturn(
                Optional.of(Duration.ofMillis(INITIAL_WAIT_TIME_AFTER_ERROR_IN_MILLIS)));
        when(eventControllerConfigurationMock.getMaximumWaitTimeAfterError()).thenReturn(
                Optional.of(Duration.ofMillis(MAXIMUM_WAIT_TIME_AFTER_ERROR_IN_MILLIS)));
        when(eventControllerConfigurationMock.getWaitTimeAfterErrorIncreaseFactor()).thenReturn(
                OptionalInt.of(WAIT_TIME_AFTER_ERROR_INCREASE_FACTOR));
        circuitBreaker.onEventingStart();
        verify(eventingControlServiceMock).addStartupListenerOrExecuteStartup(circuitBreaker);
    }

    @Test
    void execute_invokes_command_immediately_when_circuit_is_closed() throws InterruptedException {
        invokeSuccessCommand();
        verify(commandOkMock).run();
    }

    @Test
    void execute_does_not_invoke_onFailureCommand_when_command_succeeds() throws InterruptedException {
        invokeSuccessCommand().onFailure(onFailureCommandMock);
        verify(commandOkMock).run();
        verify(onFailureCommandMock, never()).run();
    }

    @Test
    void execute_invokes_onFailureCommand_when_command_throwsException() throws InterruptedException {
        invokeCommandThrowingException();
        verify(onFailureCommandMock).run();
        verify(commandFailingMock).run();
    }

    @Test
    void execute_throwsError_when_command_throwsError() throws InterruptedException {
        Error error = new Error();
        doThrow(error).when(commandFailingMock).run();
        assertThatThrownBy(() -> circuitBreaker.execute(commandFailingMock)).isSameAs(error);
    }

    @Test
    void execute_throwsInterruptedException_when_command_throwsInterruptedException() throws InterruptedException {
        InterruptedException interruptedException = new InterruptedException();
        doThrow(interruptedException).when(commandFailingMock).run();
        assertThatThrownBy(() -> circuitBreaker.execute(commandFailingMock)).isSameAs(interruptedException);
    }

    @Test
    void execute_does_not_wait_during_the_first_command_failure() throws InterruptedException {
        assertThat(measureDuration(this::invokeCommandThrowingException)).isNotCloseTo(
                INITIAL_WAIT_TIME_AFTER_ERROR_IN_MILLIS, Percentage.withPercentage(25));
    }

    @Test
    void execute_waits_exponentially_on_subsequent_command_after_previous_command_fails() throws InterruptedException {
        Instant previousFailureInstant = invokeCommandThrowingException();

        long expectedDurationInMillis = INITIAL_WAIT_TIME_AFTER_ERROR_IN_MILLIS;
        long maximumInvocationCount = getInvocationsCountToGoTwiceThroughTheWaitTimesInterval();
        for (int invocationCounter = 1; invocationCounter <= maximumInvocationCount; invocationCounter++) {
            Instant currentFailureInstant = invokeCommandThrowingException();

            ArgumentCaptor<Duration> sleepTimeCaptor = ArgumentCaptor.forClass(Duration.class);
            verify(timeServiceMock).sleep(sleepTimeCaptor.capture());
            assertThat(sleepTimeCaptor.getValue().toMillis()).isCloseTo(
                    expectedDurationInMillis - previousFailureInstant.until(currentFailureInstant, ChronoUnit.MILLIS),
                    Percentage.withPercentage(25));
            reset(timeServiceMock);

            previousFailureInstant = currentFailureInstant;
            expectedDurationInMillis *= WAIT_TIME_AFTER_ERROR_INCREASE_FACTOR;
            if (expectedDurationInMillis > MAXIMUM_WAIT_TIME_AFTER_ERROR_IN_MILLIS) {
                expectedDurationInMillis = INITIAL_WAIT_TIME_AFTER_ERROR_IN_MILLIS;
            }
        }
    }

    @Test
    void execute_closes_circuit_after_command_succeeds() throws InterruptedException {
        invokeCommandThrowingException();
        invokeSuccessCommand();

        assertThat(measureDuration(this::invokeSuccessCommand)).isNotCloseTo(INITIAL_WAIT_TIME_AFTER_ERROR_IN_MILLIS,
                Percentage.withPercentage(10));
    }

    private CircuitBreaker.Result invokeSuccessCommand() throws InterruptedException {
        return circuitBreaker.execute(commandOkMock);
    }

    private Instant invokeCommandThrowingException() throws InterruptedException {
        doAnswer(new AnswersWithDelay(1, invocation -> {
            throw new RuntimeException();
        })).when(commandFailingMock).run();
        try {
            circuitBreaker.execute(commandFailingMock).onFailure(onFailureCommandMock);
            return Instant.now();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private long getInvocationsCountToGoTwiceThroughTheWaitTimesInterval() {
        return 2 * (1 + Math.round(Math.ceil(Math.log(
                (double) MAXIMUM_WAIT_TIME_AFTER_ERROR_IN_MILLIS / (double) INITIAL_WAIT_TIME_AFTER_ERROR_IN_MILLIS)
                / Math.log(WAIT_TIME_AFTER_ERROR_INCREASE_FACTOR))));
    }

    private long measureDuration(InterruptibleRunnable command) throws InterruptedException {
        final LocalTime startTime = LocalTime.now();
        command.run();
        final LocalTime endTime = LocalTime.now();
        return startTime.until(endTime, ChronoUnit.MILLIS);
    }
}