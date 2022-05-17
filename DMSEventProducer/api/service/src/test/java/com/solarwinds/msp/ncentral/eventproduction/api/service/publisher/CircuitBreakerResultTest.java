package com.solarwinds.msp.ncentral.eventproduction.api.service.publisher;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

/**
 * This class represents the unit test of the {@link CircuitBreakerResult} class.
 */
@ExtendWith(MockitoExtension.class)
class CircuitBreakerResultTest {

    @Mock
    private Runnable runnable;

    @Test
    void onFailure() {
        final CircuitBreakerResult circuitBreakerResult = new CircuitBreakerResult(new RuntimeException("Some error."));
        circuitBreakerResult.onFailure(runnable);
        verify(runnable).run();
    }

    @Test
    void onFailureNoCommand() {
        final CircuitBreakerResult circuitBreakerResult = new CircuitBreakerResult();
        circuitBreakerResult.onFailure(runnable);
        verifyZeroInteractions(runnable);
    }
}
