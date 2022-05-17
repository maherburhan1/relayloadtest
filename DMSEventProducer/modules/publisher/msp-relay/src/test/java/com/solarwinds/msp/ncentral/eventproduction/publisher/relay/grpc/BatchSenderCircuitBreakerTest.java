package com.solarwinds.msp.ncentral.eventproduction.publisher.relay.grpc;

import com.solarwinds.msp.ncentral.eventproduction.api.service.publisher.CircuitBreakerResult;
import com.solarwinds.msp.ncentral.eventproduction.publisher.relay.configuration.MspRelayConfigurationService;
import com.solarwinds.util.concurrent.InterruptibleRunnable;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.internal.stubbing.answers.AnswersWithDelay;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.OptionalInt;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * This class represents the unit test of the {@link BatchSenderCircuitBreaker} class.
 */
@ExtendWith(MockitoExtension.class)
class BatchSenderCircuitBreakerTest {

    private static final long INITIAL_WAIT_TIME_IN_MILLISECONDS = 10L;
    private static final long MAXIMUM_WAIT_TIME_IN_MILLISECONDS = 100L;
    private static final int WAIT_TIME_INCREASE_FACTOR = 2;
    private static final int INVALID_VALUE = -666;

    @Mock
    private MspRelayConfigurationService mspRelayConfigurationServiceMock;
    @Mock
    private InterruptibleRunnable executeCommandOkMock;
    @Mock
    private InterruptibleRunnable executeCommandFailingMock;
    @Mock
    private Runnable onFailureCommandMock;

    @InjectMocks
    private BatchSenderCircuitBreaker circuitBreaker;

    // ----------------------------------------------------------------------------------------------------------------
    // Tests of the initialize method
    // ----------------------------------------------------------------------------------------------------------------

    @Test
    void initialize() {
        circuitBreaker.initialize();

        assertThat(circuitBreaker.getInitialWaitTimeInMilliseconds()).isEqualTo(
                BatchSenderCircuitBreaker.DEFAULT_INITIAL_WAIT_TIME_IN_MILLISECONDS);
        assertThat(circuitBreaker.getMaximumWaitTimeInMilliseconds()).isEqualTo(
                BatchSenderCircuitBreaker.DEFAULT_MAXIMUM_WAIT_TIME_IN_MILLISECONDS);
        assertThat(circuitBreaker.getWaitTimeIncreaseFactor()).isEqualTo(
                BatchSenderCircuitBreaker.DEFAULT_WAIT_TIME_INCREASE_FACTOR);
        assertThat(circuitBreaker.getNextWaitTimeInMilliseconds()).isEqualTo(
                BatchSenderCircuitBreaker.DEFAULT_INITIAL_WAIT_TIME_IN_MILLISECONDS);

        verify(mspRelayConfigurationServiceMock).getInitialWaitTimeAfterError();
        verify(mspRelayConfigurationServiceMock).getMaximumWaitTimeAfterError();
        verify(mspRelayConfigurationServiceMock).getWaitTimeAfterErrorIncreaseFactor();
    }

    @Test
    void initializeCustom() {
        when(mspRelayConfigurationServiceMock.getInitialWaitTimeAfterError()).thenReturn(
                getDuration(INITIAL_WAIT_TIME_IN_MILLISECONDS));
        when(mspRelayConfigurationServiceMock.getMaximumWaitTimeAfterError()).thenReturn(
                getDuration(MAXIMUM_WAIT_TIME_IN_MILLISECONDS));
        when(mspRelayConfigurationServiceMock.getWaitTimeAfterErrorIncreaseFactor()).thenReturn(
                OptionalInt.of(WAIT_TIME_INCREASE_FACTOR));

        circuitBreaker.initialize();

        assertThat(circuitBreaker.getInitialWaitTimeInMilliseconds()).isEqualTo(INITIAL_WAIT_TIME_IN_MILLISECONDS);
        assertThat(circuitBreaker.getMaximumWaitTimeInMilliseconds()).isEqualTo(MAXIMUM_WAIT_TIME_IN_MILLISECONDS);
        assertThat(circuitBreaker.getWaitTimeIncreaseFactor()).isEqualTo(WAIT_TIME_INCREASE_FACTOR);
        assertThat(circuitBreaker.getNextWaitTimeInMilliseconds()).isEqualTo(INITIAL_WAIT_TIME_IN_MILLISECONDS);
    }

    @Test
    void initializeInvalidInitialWaitTime() {
        when(mspRelayConfigurationServiceMock.getInitialWaitTimeAfterError()).thenReturn(getDuration(INVALID_VALUE));

        assertThrows(IllegalArgumentException.class, () -> circuitBreaker.initialize());

        verify(mspRelayConfigurationServiceMock).getInitialWaitTimeAfterError();
    }

    @Test
    void initializeInvalidMaximumWaitTime() {
        when(mspRelayConfigurationServiceMock.getMaximumWaitTimeAfterError()).thenReturn(getDuration(INVALID_VALUE));

        assertThrows(IllegalArgumentException.class, () -> circuitBreaker.initialize());

        verify(mspRelayConfigurationServiceMock).getInitialWaitTimeAfterError();
        verify(mspRelayConfigurationServiceMock).getMaximumWaitTimeAfterError();
    }

    @Test
    void initializeInvalidWaitTimeIncreaseFactor() {
        when(mspRelayConfigurationServiceMock.getWaitTimeAfterErrorIncreaseFactor()).thenReturn(
                OptionalInt.of(INVALID_VALUE));

        assertThrows(IllegalArgumentException.class, () -> circuitBreaker.initialize());

        verify(mspRelayConfigurationServiceMock).getInitialWaitTimeAfterError();
        verify(mspRelayConfigurationServiceMock).getMaximumWaitTimeAfterError();
        verify(mspRelayConfigurationServiceMock).getWaitTimeAfterErrorIncreaseFactor();
    }

    // ----------------------------------------------------------------------------------------------------------------
    // Tests of the execute method
    // ----------------------------------------------------------------------------------------------------------------

    @Test
    void executeSuccessCommand() throws Exception {
        invokeSuccessCommand();
        verify(executeCommandOkMock).run();
    }

    @Test
    void executeSuccessCommandAndNoOnFailureCommand() throws Exception {
        invokeSuccessCommand().onFailure(onFailureCommandMock);
        verify(executeCommandOkMock).run();
        verifyNoInteractions(onFailureCommandMock);
    }

    @Test
    void executeFailingCommandAndOnFailureCommand() throws Exception {
        invokeCommandThrowingException();
        verify(onFailureCommandMock).run();
        verify(executeCommandFailingMock).run();
    }

    @Test
    void executeErrorCommandThrowsError() throws Exception {
        final Error error = new Error();
        doThrow(error).when(executeCommandFailingMock).run();
        assertThatThrownBy(() -> circuitBreaker.execute(executeCommandFailingMock)).isSameAs(error);
    }

    @Test
    void execute_throws_InterruptedException_when_commandThrows_InterruptedException() throws Exception {
        final InterruptedException interruptedException = new InterruptedException();
        doThrow(interruptedException).when(executeCommandFailingMock).run();
        assertThatThrownBy(() -> circuitBreaker.execute(executeCommandFailingMock)).isSameAs(interruptedException);
    }

    @Test
    void executeNoDelayForFirstFailure() throws Exception {
        initializeCustom();
        invokeCommandThrowingException();
        assertThat(circuitBreaker.getNextWaitTimeInMilliseconds()).isEqualTo(INITIAL_WAIT_TIME_IN_MILLISECONDS * 2);
    }

    @Test
    void executeFailingCommandsWithExponentialDelays() throws Exception {
        initializeCustom();

        long expectedDurationInMilliseconds = INITIAL_WAIT_TIME_IN_MILLISECONDS * 2;
        final long maximumInvocationCount = getInvocationsCountToGoTwiceThroughTheWaitTimesInterval();
        for (int invocationCounter = 1; invocationCounter <= maximumInvocationCount; invocationCounter++) {
            invokeCommandThrowingException();

            assertThat(circuitBreaker.getNextWaitTimeInMilliseconds()).isEqualTo(expectedDurationInMilliseconds);

            // sleep for a while to pass the close window
            Thread.sleep(expectedDurationInMilliseconds);

            expectedDurationInMilliseconds *= WAIT_TIME_INCREASE_FACTOR;
            if (expectedDurationInMilliseconds > MAXIMUM_WAIT_TIME_IN_MILLISECONDS) {
                expectedDurationInMilliseconds = INITIAL_WAIT_TIME_IN_MILLISECONDS;
            }
        }
    }

    @Test
    void executeResetDelay() throws Exception {
        initializeCustom();
        invokeCommandThrowingException();
        invokeSuccessCommand();

        invokeSuccessCommand();
        assertThat(circuitBreaker.getNextWaitTimeInMilliseconds()).isEqualTo(INITIAL_WAIT_TIME_IN_MILLISECONDS);
    }

    // ----------------------------------------------------------------------------------------------------------------
    // Tests of the handleSuccess method
    // ----------------------------------------------------------------------------------------------------------------

    @Test
    void handleSuccess() throws Exception {
        initializeCustom();
        executeSuccessCommandAndNoOnFailureCommand();
        assertThat(circuitBreaker.getNextWaitTimeInMilliseconds()).isEqualTo(INITIAL_WAIT_TIME_IN_MILLISECONDS);
    }

    // ----------------------------------------------------------------------------------------------------------------
    // Tests of the handleFailure method
    // ----------------------------------------------------------------------------------------------------------------

    @Test
    void handleFailure() throws Exception {
        initializeCustom();
        executeFailingCommandAndOnFailureCommand();
        assertThat(circuitBreaker.getNextWaitTimeInMilliseconds()).isEqualTo(INITIAL_WAIT_TIME_IN_MILLISECONDS * 2);
    }

    // ----------------------------------------------------------------------------------------------------------------
    // Private methods
    // ----------------------------------------------------------------------------------------------------------------

    private Optional<Duration> getDuration(long value) {
        return Optional.of(Duration.of(value, ChronoUnit.MILLIS));
    }

    private CircuitBreakerResult invokeSuccessCommand() throws Exception {
        final CircuitBreakerResult result = circuitBreaker.execute(executeCommandOkMock);
        circuitBreaker.handleSuccess();
        return result;
    }

    private void invokeCommandThrowingException() throws Exception {
        doAnswer(new AnswersWithDelay(1, invocation -> {
            throw new RuntimeException();
        })).when(executeCommandFailingMock).run();
        circuitBreaker.execute(executeCommandFailingMock).onFailure(onFailureCommandMock);
    }

    private static long getInvocationsCountToGoTwiceThroughTheWaitTimesInterval() {
        return 2 * (1 + Math.round(Math.ceil(
                Math.log((double) MAXIMUM_WAIT_TIME_IN_MILLISECONDS / (double) INITIAL_WAIT_TIME_IN_MILLISECONDS) / Math
                        .log(WAIT_TIME_INCREASE_FACTOR))));
    }
}
