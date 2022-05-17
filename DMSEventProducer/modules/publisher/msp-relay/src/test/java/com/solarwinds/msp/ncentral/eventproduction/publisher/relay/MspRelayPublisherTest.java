package com.solarwinds.msp.ncentral.eventproduction.publisher.relay;

import com.google.protobuf.Any;
import com.google.protobuf.Message;

import com.solarwinds.msp.ncentral.eventproduction.api.service.publisher.PublishedEventInfo;
import com.solarwinds.msp.ncentral.eventproduction.api.service.publisher.PublishingContext;
import com.solarwinds.msp.ncentral.eventproduction.api.service.tracking.EventingControlService;
import com.solarwinds.msp.ncentral.eventproduction.publisher.relay.grpc.RequestBatcher;
import com.solarwinds.msp.ncentral.proto.entity.entity.ClientOuterClass;
import com.solarwinds.msp.relay.Relay.Context;
import com.solarwinds.msp.relay.Relay.RequestMessage;
import com.solarwinds.msp.relay.Relay.ResponseMessage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MspRelayPublisherTest {

    private static final String BIZAPPS_CUSTOMER_ID = "BizAppsCustomerId-1";
    private static final String SYSTEM_GUID = "SystemGuid-A";
    private static final String EVENT_TYPE = "com.solarwinds.msp.ncentral.proto.entity.entity.ClientOuterClass.Client";

    @InjectMocks
    private MspRelayPublisher mspRelayPublisher;
    @Mock
    private RequestBatcher requestBatcherMock;
    private ResponseMessage responseMessage;
    private Message event;
    @Mock
    private PublishingContext publishingContextMock;
    @Captor
    private ArgumentCaptor<Context> contextArgumentCaptor;
    @Captor
    private ArgumentCaptor<RequestMessage> requestMessageArgumentCaptor;
    @Mock
    private Future<ResponseMessage> responseMessageFutureMock;
    @Mock
    private EventingControlService eventingControlServiceMock;

    @BeforeEach
    void setup() throws Exception {
        mspRelayPublisher.onEventingStart();
        verify(eventingControlServiceMock).addStartupListenerOrExecuteStartup(mspRelayPublisher);
        mock_requestBatcher();
        mock_publishingContext();
        mock_event();
    }

    private void mock_requestBatcher() throws Exception {
        when(requestBatcherMock.addToBatch(any(), any())).thenReturn(responseMessageFutureMock);
    }

    private void mock_publishingContext() {
        when(publishingContextMock.getBizappsCustomerId()).thenReturn(Optional.of(BIZAPPS_CUSTOMER_ID));
        when(publishingContextMock.getSystemGuid()).thenReturn(SYSTEM_GUID);
    }

    private void mock_event() {
        event = ClientOuterClass.Client.newBuilder().build();
    }

    @Test
    void publish_returns_info_with_responseMessages() throws ExecutionException, InterruptedException {
        mock_responseFuture_get();

        Optional<Future<PublishedEventInfo>> publishedEventInfoFuture =
                mspRelayPublisher.publish(event, publishingContextMock);
        assertThat(publishedEventInfoFuture).isNotEmpty();
        assertThat(publishedEventInfoFuture.get().get().getInfo()).isEqualTo(
                String.format("responseMessage=[%s]", responseMessage.toString()));
    }

    private void mock_responseFuture_get() throws ExecutionException, InterruptedException {
        responseMessage = ResponseMessage.newBuilder()
                .setId(123)
                .setStatusValue(456)
                .setStatus(ResponseMessage.ResponseStatus.OK)
                .build();
        when(responseMessageFutureMock.get()).thenReturn(responseMessage);
    }

    @Test
    void publish_calls_requestBatcher_with_expected_context() throws Exception {
        mspRelayPublisher.publish(event, publishingContextMock);

        verify(requestBatcherMock).addToBatch(contextArgumentCaptor.capture(), any());
        Context context = contextArgumentCaptor.getValue();
        assertThat(context.getBizappsCustomerId()).isEqualTo(BIZAPPS_CUSTOMER_ID);
        assertThat(context.getSystemGuid()).isEqualTo(SYSTEM_GUID);
    }

    @Test
    void publish_calls_requestBatcher_with_expected_message() throws Exception {
        mspRelayPublisher.publish(event, publishingContextMock);

        verify(requestBatcherMock).addToBatch(any(), requestMessageArgumentCaptor.capture());
        RequestMessage requestMessage = requestMessageArgumentCaptor.getValue();
        assertThat(requestMessage.getId()).isEqualTo(0);
        assertThat(requestMessage.getEventType()).isEqualTo(EVENT_TYPE);
        assertThat(requestMessage.getPayload()).isEqualTo(Any.pack(event));
    }

    @Test
    void publish_always_increments_requestMessage_id_supplied_to_requestBatcher() throws Exception {
        final int publishCount = 100;
        for (int i = 0; i < publishCount; i++) {
            mspRelayPublisher.publish(event, publishingContextMock);
        }

        verify(requestBatcherMock, times(publishCount)).addToBatch(any(), requestMessageArgumentCaptor.capture());
        assertThat(requestMessageArgumentCaptor.getAllValues()).extracting(RequestMessage::getId)
                .containsSequence(IntStream.range(0, publishCount).boxed().collect(Collectors.toList()));
    }
}
