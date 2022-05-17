package com.solarwinds.msp.ncentral.eventproduction.api.service.failure;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Observer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class FailureNotificationServiceTest {

    private static final String BIZAPPS_CUSTOMER_ID = "BIZAPPS_CUSTOMER_1";

    @Mock
    private Observer observerMock;
    @InjectMocks
    private FailureNotificationService service;
    @Captor
    private ArgumentCaptor<Failure> failureArgumentCaptor;

    @Test
    void registerException_invokes_observers_with_expectedFailure() {
        service.addObserver(observerMock);

        final RuntimeException expectedException =
                new RuntimeException("Level-1 message", new IllegalArgumentException("Level-2 message"));
        service.registerException(BIZAPPS_CUSTOMER_ID, expectedException);

        verify(observerMock).update(eq(service), failureArgumentCaptor.capture());
        Failure failure = failureArgumentCaptor.getValue();
        assertThat(failure.getBusinessApplicationsCustomerId()).isEqualTo(BIZAPPS_CUSTOMER_ID);
        assertThat(failure.getCaughtException()).isSameAs(expectedException);
    }

    @Test
    void registerException_throws_NullPointerException_when_exception_is_null() {
        assertThatNullPointerException().isThrownBy(() -> service.registerException(BIZAPPS_CUSTOMER_ID, null));
    }
}