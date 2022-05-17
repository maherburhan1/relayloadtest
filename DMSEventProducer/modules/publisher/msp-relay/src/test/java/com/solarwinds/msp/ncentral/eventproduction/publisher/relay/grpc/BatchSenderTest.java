package com.solarwinds.msp.ncentral.eventproduction.publisher.relay.grpc;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.protobuf.Any;
import com.google.protobuf.Timestamp;

import com.solarwinds.dto.Tuple;
import com.solarwinds.msp.ncentral.eventproduction.api.entity.EventStatistic;
import com.solarwinds.msp.ncentral.eventproduction.api.service.statistics.EventStatistics;
import com.solarwinds.msp.ncentral.eventproduction.api.service.tracking.EventingControlService;
import com.solarwinds.msp.ncentral.eventproduction.publisher.relay.configuration.MspRelayConfigurationService;
import com.solarwinds.msp.ncentral.proto.entity.ActionOuterClass;
import com.solarwinds.msp.ncentral.proto.entity.entity.AddressOuterClass;
import com.solarwinds.msp.ncentral.proto.entity.entity.ClientOuterClass;
import com.solarwinds.msp.ncentral.proto.entity.entity.ContactOuterClass;
import com.solarwinds.msp.relay.Relay;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.net.URISyntaxException;
import java.rmi.RemoteException;
import java.time.Duration;
import java.util.Collections;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * This class represents the unit test of the {@link BatchSender} class.
 */
@ExtendWith(MockitoExtension.class)
class BatchSenderTest {

    private static final Duration WAIT_TIME_FOR_RESPONSE_AFTER_SENDING = Duration.ofMillis(50L);
    private static final String BUSINESS_APPLICATIONS_ID = "BizApps-ID";
    private static final String BUSINESS_APPLICATIONS_CUSTOMER_ID = "Bizapps-Customer-ID";
    private static final int MESSAGE_ID = 123456;
    private static final String EVENT_TYPE = "Event Type";

    @Mock
    private MspRelayConfigurationService mspRelayConfigurationServiceMock;
    @Mock
    private PublisherProvider publisherProviderMock;
    @Mock
    private EventingControlService eventingControlServiceMock;
    @Mock
    private EventStatistics eventStatisticsMock;
    @InjectMocks
    private BatchSender batchSender;

    @Mock
    private CompletableFuture<Relay.PublishResponse> publishResponseCompletableFutureMock;
    @Mock
    private FuturePublisher futurePublisherMock;
    @Mock
    private ListenableFuture<Relay.PublishResponse> publishResponseListenableFutureMock;

    @BeforeEach
    void setUp() {
        when(mspRelayConfigurationServiceMock.getWaitTimeForResponseAfterSending()).thenReturn(
                Optional.of(WAIT_TIME_FOR_RESPONSE_AFTER_SENDING));
        batchSender.onEventingStart();
    }

    @Test
    public void constructor_adds_itself_as_EventingControlService_eventingStartupListener() {
        verify(eventingControlServiceMock).addStartupListenerOrExecuteStartup(batchSender);
    }

    @Test
    void sendBatch() throws Exception {
        when(publisherProviderMock.getFuturePublisher(any())).thenReturn(futurePublisherMock);
        when(futurePublisherMock.publish(expectedRequestArgument())).thenReturn(publishResponseListenableFutureMock);
        when(publishResponseListenableFutureMock.get(WAIT_TIME_FOR_RESPONSE_AFTER_SENDING.toMillis(),
                TimeUnit.MILLISECONDS)).thenReturn(createResponse());

        batchSender.sendBatch(new Tuple<>(createBatch(), publishResponseCompletableFutureMock));

        verify(publishResponseCompletableFutureMock).complete(createResponse());
        verify(eventStatisticsMock).addStatistic(createEventStatistic());
    }

    @ParameterizedTest
    @MethodSource
    void sendBatchResponseFutureException(Throwable throwable) throws Exception {
        when(publisherProviderMock.getFuturePublisher(any())).thenReturn(futurePublisherMock);
        when(futurePublisherMock.publish(expectedRequestArgument())).thenReturn(publishResponseListenableFutureMock);
        when(publishResponseListenableFutureMock.get(WAIT_TIME_FOR_RESPONSE_AFTER_SENDING.toMillis(),
                TimeUnit.MILLISECONDS)).thenThrow(throwable);

        assertThatThrownBy(() -> batchSender.sendBatch(
                new Tuple<>(createBatch(), publishResponseCompletableFutureMock))).isInstanceOf(RuntimeException.class)
                .hasCause(throwable);
        verify(publishResponseCompletableFutureMock).completeExceptionally(throwable);
        verifyNoInteractions(eventStatisticsMock);
    }

    static Stream<Throwable> sendBatchResponseFutureException() {
        return Stream.of(new TimeoutException("Timeout"),
                new ExecutionException("Execution error", new RuntimeException("Root cause")),
                new RuntimeException("Unchecked exception"));
    }

    @Test
    void sendBatchResponseFutureInterruptedException() throws Exception {
        when(publisherProviderMock.getFuturePublisher(any())).thenReturn(futurePublisherMock);
        when(futurePublisherMock.publish(expectedRequestArgument())).thenReturn(publishResponseListenableFutureMock);
        InterruptedException interruptedException = new InterruptedException("Interrupted");
        when(publishResponseListenableFutureMock.get(WAIT_TIME_FOR_RESPONSE_AFTER_SENDING.toMillis(),
                TimeUnit.MILLISECONDS)).thenThrow(interruptedException);

        assertThatThrownBy(
                () -> batchSender.sendBatch(new Tuple<>(createBatch(), publishResponseCompletableFutureMock))).isSameAs(
                interruptedException);
        verifyNoInteractions(publishResponseCompletableFutureMock);
        verifyNoInteractions(eventStatisticsMock);
    }

    @ParameterizedTest
    @MethodSource
    void sendBatchPublisherException(Throwable throwable) throws Exception {
        when(publisherProviderMock.getFuturePublisher(any())).thenReturn(futurePublisherMock);
        when(futurePublisherMock.publish(any())).thenThrow(throwable);

        assertThatThrownBy(() -> batchSender.sendBatch(
                new Tuple<>(createBatch(), publishResponseCompletableFutureMock))).isInstanceOf(RuntimeException.class)
                .hasCause(throwable);
        verify(publishResponseCompletableFutureMock).completeExceptionally(throwable);
        verifyNoInteractions(eventStatisticsMock);
    }

    static Stream<Throwable> sendBatchPublisherException() {
        return Stream.of(new RuntimeException("Unchecked exception"));
    }

    @ParameterizedTest
    @MethodSource
    void sendBatchPublisherProviderException(Throwable throwable) throws Exception {
        when(publisherProviderMock.getFuturePublisher(any())).thenThrow(throwable);

        assertThatThrownBy(() -> batchSender.sendBatch(
                new Tuple<>(createBatch(), publishResponseCompletableFutureMock))).isInstanceOf(RuntimeException.class)
                .hasCause(throwable);
        verify(publishResponseCompletableFutureMock).completeExceptionally(throwable);
        verifyNoInteractions(eventStatisticsMock);
    }

    static Stream<Throwable> sendBatchPublisherProviderException() {
        return Stream.of(new URISyntaxException("Some URI", "Some URI parsing error"),
                new RemoteException("Some persistence error"), new RuntimeException("Unchecked exception"));
    }

    @Test
    void sendBatchNull() {
        assertThatNullPointerException().isThrownBy(() -> batchSender.sendBatch(null));
        verifyNoInteractions(publishResponseCompletableFutureMock);
    }

    private Relay.PublishRequest expectedRequestArgument() {
        return argThat(request -> {
            assertThat(request.getContext().getSystemGuid()).isEqualTo(BUSINESS_APPLICATIONS_ID);
            assertThat(request.getContext().getBizappsCustomerId()).isEqualTo(BUSINESS_APPLICATIONS_CUSTOMER_ID);
            assertThat(request.getMessagesList()).isEqualTo(Collections.singletonList(createRequest()));
            return true;
        });
    }

    private Batch createBatch() {
        final Relay.Context.Builder contextBuilder = Relay.Context.newBuilder()
                .setSystemGuid(BUSINESS_APPLICATIONS_ID)
                .setBizappsCustomerId(BUSINESS_APPLICATIONS_CUSTOMER_ID);
        final Batch batch = new Batch(contextBuilder.build());
        batch.addMessage(createRequest());
        return batch;
    }

    private Relay.RequestMessage createRequest() {
        return Relay.RequestMessage.newBuilder()
                .setId(MESSAGE_ID)
                .setEventType(EVENT_TYPE)
                .setPayload(Any.pack(createRequestMessagePayload()))
                .build();
    }

    private ClientOuterClass.Client createRequestMessagePayload() {
        return ClientOuterClass.Client.newBuilder()
                .setClientId(123456)
                .setClientType(ClientOuterClass.Client.ClientType.VAR)
                .setName("Name")
                .setParentId(12345)
                .setAction(ActionOuterClass.Action.EDIT)
                .addContact(ContactOuterClass.Contact.newBuilder()
                        .setDepartment("Department")
                        .setEmail("email@email.com")
                        .setFirstName("First Name")
                        .setFullName("Full Name")
                        .setIsPrimary(true)
                        .setLastName("Last Name")
                        .setPhoneNumber("123 456 789")
                        .setTitle("Title")
                        .build())
                .setCreated(Timestamp.newBuilder().setSeconds(1234567).setNanos(9876543).build())
                .setLastUpdated(Timestamp.newBuilder().setSeconds(9876543).setNanos(1234567).build())
                .addAddress(AddressOuterClass.Address.newBuilder()
                        .setAddress1("Address 1")
                        .setAddress2("Address 2")
                        .setCity("City")
                        .setCountry("Country")
                        .setPostalCode("123 45")
                        .build())
                .setDeleted(false)
                .setActionValue(951753)
                .setClientTypeValue(753159)
                .build();
    }

    private Relay.PublishResponse createResponse() {
        return Relay.PublishResponse.newBuilder().addResponseMessages(createResponseMessage()).build();
    }

    private Relay.ResponseMessage createResponseMessage() {
        return Relay.ResponseMessage.newBuilder()
                .setId(MESSAGE_ID)
                .setStatus(Relay.ResponseMessage.ResponseStatus.OK)
                .build();
    }

    private EventStatistic createEventStatistic() {
        return EventStatistic.builder()
                .statisticType(EventStatistic.StatisticType.PROCESSING_DATA)
                .statisticSubType(EventStatistic.StatisticSubType.SUCCESSFULLY_SENT_TO_RELAY)
                .statisticValue(createResponse().getResponseMessagesCount())
                .build();
    }
}