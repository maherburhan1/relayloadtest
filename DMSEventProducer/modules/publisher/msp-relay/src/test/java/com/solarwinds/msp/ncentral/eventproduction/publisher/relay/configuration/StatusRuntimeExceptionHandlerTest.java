package com.solarwinds.msp.ncentral.eventproduction.publisher.relay.configuration;

import com.solarwinds.msp.ncentral.eventproduction.api.service.failure.Failure;
import com.solarwinds.msp.ncentral.eventproduction.api.service.failure.FailureNotificationService;
import com.solarwinds.msp.ncentral.eventproduction.publisher.relay.failure.StatusRuntimeExceptionHandler;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import io.grpc.Status;

import static org.junit.jupiter.params.provider.EnumSource.Mode.EXCLUDE;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class StatusRuntimeExceptionHandlerTest {

    private static final String BIZAPPS_CUSTOMER_ID_1 = "BIZAPPS-CUSTOMER-1";
    private static final String BIZAPPS_CUSTOMER_ID_2 = "BIZAPPS-CUSTOMER-2";

    @Mock
    private MspRelayUriRefreshService mspRelayUriRefreshServiceMock;
    @Mock
    private MspRelayCredentialsRefreshService mspRelayCredentialsRefreshServiceMock;
    @Mock
    private FailureNotificationService failureNotificationServiceMock;
    @InjectMocks
    private StatusRuntimeExceptionHandler statusRuntimeExceptionHandler;

    @Test
    void subscribeForFailureNotifications_addsItself_asFailureNotificationServiceObserver() {
        statusRuntimeExceptionHandler.subscribeForFailureNotifications();
        verify(failureNotificationServiceMock).addObserver(statusRuntimeExceptionHandler);
    }

    @Test
    void additional_subscribeForFailureNotifications_calls_are_ignored() {
        statusRuntimeExceptionHandler.subscribeForFailureNotifications();
        statusRuntimeExceptionHandler.subscribeForFailureNotifications();
        verify(failureNotificationServiceMock).addObserver(statusRuntimeExceptionHandler);
    }

    @ParameterizedTest
    @EnumSource(value = Status.Code.class, names = {"PERMISSION_DENIED", "UNAUTHENTICATED"})
    void newCertificateIsRequested_onPermissionDeniedOrUnAuthenticated(Status.Code statusCode) {
        notifyOnFailure(statusCode, BIZAPPS_CUSTOMER_ID_1);
        verify(mspRelayCredentialsRefreshServiceMock).requestNewClientCertificateForAuthentication(
                BIZAPPS_CUSTOMER_ID_1);
    }

    private void notifyOnFailure(Status.Code statusCode, String businessApplicationsCustomerId) {
        Failure failure = Failure.builder()
                .withBusinessApplicationsCustomerId(businessApplicationsCustomerId)
                .withThrowable(statusCode.toStatus().asRuntimeException())
                .build();
        statusRuntimeExceptionHandler.update(failureNotificationServiceMock, failure);
    }

    @ParameterizedTest
    @EnumSource(value = Status.Code.class, names = {"UNAVAILABLE"})
    void newRelayHostIsRequested_onUnavailable(Status.Code statusCode) {
        notifyOnFailure(statusCode, BIZAPPS_CUSTOMER_ID_1);
        verify(mspRelayUriRefreshServiceMock).requestEventBusRelayUri(BIZAPPS_CUSTOMER_ID_1);
    }

    @ParameterizedTest
    @EnumSource(value = Status.Code.class, names = {"PERMISSION_DENIED", "UNAUTHENTICATED"})
    void separateCertificateRequestsAreMade_forMultipleBizappsIds(Status.Code statusCode) {
        notifyOnFailure(statusCode, BIZAPPS_CUSTOMER_ID_1);
        notifyOnFailure(statusCode, BIZAPPS_CUSTOMER_ID_2);
        verify(mspRelayCredentialsRefreshServiceMock).requestNewClientCertificateForAuthentication(
                BIZAPPS_CUSTOMER_ID_1);
        verify(mspRelayCredentialsRefreshServiceMock).requestNewClientCertificateForAuthentication(
                BIZAPPS_CUSTOMER_ID_2);
    }

    @ParameterizedTest
    @EnumSource(value = Status.Code.class, names = {"UNAVAILABLE"})
    void separateRelayHostRequestsAreMade_forMultipleBizappsIds(Status.Code statusCode) {
        notifyOnFailure(statusCode, BIZAPPS_CUSTOMER_ID_1);
        notifyOnFailure(statusCode, BIZAPPS_CUSTOMER_ID_2);
        verify(mspRelayUriRefreshServiceMock).requestEventBusRelayUri(BIZAPPS_CUSTOMER_ID_1);
        verify(mspRelayUriRefreshServiceMock).requestEventBusRelayUri(BIZAPPS_CUSTOMER_ID_2);
    }

    @Test
    void separateRequestsAreMade_forMultipleStatusTypesAndSameBizappsId() {
        notifyOnFailure(Status.Code.PERMISSION_DENIED, BIZAPPS_CUSTOMER_ID_1);
        notifyOnFailure(Status.Code.UNAVAILABLE, BIZAPPS_CUSTOMER_ID_1);
        verify(mspRelayCredentialsRefreshServiceMock).requestNewClientCertificateForAuthentication(
                BIZAPPS_CUSTOMER_ID_1);
        verify(mspRelayUriRefreshServiceMock).requestEventBusRelayUri(BIZAPPS_CUSTOMER_ID_1);
    }

    @ParameterizedTest
    @EnumSource(value = Status.Code.class, names = {"PERMISSION_DENIED", "UNAUTHENTICATED"})
    void separateCertificateRequests_areNotMadeBySameBizappsId_beforeCooldownPeriod(Status.Code statusCode) {
        for (int i = 0; i < 10; i++) {
            notifyOnFailure(statusCode, BIZAPPS_CUSTOMER_ID_1);
        }
        verify(mspRelayCredentialsRefreshServiceMock).requestNewClientCertificateForAuthentication(
                BIZAPPS_CUSTOMER_ID_1);
    }

    @ParameterizedTest
    @EnumSource(value = Status.Code.class, names = {"UNAVAILABLE"})
    void separateRelayHostRequests_areNotMadeBySameBizappsId_beforeCooldownPeriod(Status.Code statusCode) {
        for (int i = 0; i < 10; i++) {
            notifyOnFailure(statusCode, BIZAPPS_CUSTOMER_ID_1);
        }
        verify(mspRelayUriRefreshServiceMock).requestEventBusRelayUri(BIZAPPS_CUSTOMER_ID_1);
    }

    @ParameterizedTest
    @EnumSource(value = Status.Code.class, names = {"PERMISSION_DENIED", "UNAUTHENTICATED", "UNAVAILABLE"},
            mode = EXCLUDE)
    void unsupportedStatusTypes_causeNoAction(Status.Code statusCode) {
        notifyOnFailure(statusCode, BIZAPPS_CUSTOMER_ID_1);
        verifyNoInteractions(mspRelayCredentialsRefreshServiceMock);
    }
}