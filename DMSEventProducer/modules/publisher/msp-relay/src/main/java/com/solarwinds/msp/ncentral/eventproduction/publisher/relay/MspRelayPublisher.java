package com.solarwinds.msp.ncentral.eventproduction.publisher.relay;

import com.google.protobuf.Any;
import com.google.protobuf.Message;

import com.nable.logging.Logger;
import com.nable.logging.Loggers;
import com.solarwinds.msp.ncentral.eventproduction.api.service.publisher.EventPublisher;
import com.solarwinds.msp.ncentral.eventproduction.api.service.publisher.PublishedEventInfo;
import com.solarwinds.msp.ncentral.eventproduction.api.service.publisher.PublishingContext;
import com.solarwinds.msp.ncentral.eventproduction.api.service.tracking.EventingControlService;
import com.solarwinds.msp.ncentral.eventproduction.api.service.tracking.EventingStartupListener;
import com.solarwinds.msp.ncentral.eventproduction.publisher.relay.grpc.RequestBatcher;
import com.solarwinds.msp.relay.Relay;
import com.solarwinds.util.concurrent.FutureMapper;

import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.concurrent.ThreadSafe;

/**
 * Event publisher that sends events to the MSP Relay.
 */
@Component
@ThreadSafe
public class MspRelayPublisher implements EventPublisher<Message>, EventingStartupListener {

    private static final Logger logger = Loggers.EVENT_PRODUCER;

    private final RequestBatcher requestBatcher;
    private AtomicInteger messageCounter;

    /**
     * Creates an instance of this class with the specified parameter.
     */
    public MspRelayPublisher(RequestBatcher requestBatcher, EventingControlService eventingControlService) {
        this.requestBatcher = requestBatcher;
        eventingControlService.addStartupListenerOrExecuteStartup(this);
    }

    @Override
    public void onEventingStart() {
        messageCounter = new AtomicInteger();
        logger.info("Component {} initialized.", this.getClass().getSimpleName());
    }

    @Override
    public Optional<Future<PublishedEventInfo>> publish(Message event, PublishingContext publishingContext)
            throws InterruptedException {
        final Relay.Context.Builder contextBuilder =
                Relay.Context.newBuilder().setSystemGuid(publishingContext.getSystemGuid());
        publishingContext.getBizappsCustomerId().ifPresent(contextBuilder::setBizappsCustomerId);

        final int messageId = messageCounter.getAndIncrement();
        final Relay.RequestMessage requestMessage = Relay.RequestMessage.newBuilder()
                .setId(messageId)
                .setEventType(getProtocolBuffersMessageType(event))
                .setPayload(Any.pack(event))
                .build();

        final Future<Relay.ResponseMessage> responseFuture =
                requestBatcher.addToBatch(contextBuilder.build(), requestMessage);
        if (logger.isDebugEnabled()) {
            logger.debug("Event [{}] of type [{}] batched for sending to MSP Relay as message [{}]", event.hashCode(),
                    event.getClass().getSimpleName(), requestMessage.getId());
        }
        return Optional.of(new FutureMapper<>(responseFuture, MspRelayPublishedEventInfo::new));
    }

    private String getProtocolBuffersMessageType(Message message) {
        return message.getClass().getCanonicalName();
    }
}
