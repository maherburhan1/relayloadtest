package com.solarwinds.msp.ncentral.eventproduction.controller.impl;

import com.solarwinds.msp.ncentral.eventproduction.api.service.persistence.TimestampedEvent;
import com.solarwinds.msp.ncentral.eventproduction.api.service.publisher.EventPublisher;
import com.solarwinds.msp.ncentral.eventproduction.controller.EventWithFuture;

import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Sends events outside.
 *
 * @param <T> event type
 */
@Component
public class EventSender<T> {
    private final EventPublisher<T> eventPublisher;
    private final AcknowledgementController acknowledgementController;

    public EventSender(EventPublisher<T> eventPublisher, AcknowledgementController acknowledgementController) {
        this.eventPublisher = eventPublisher;
        this.acknowledgementController = acknowledgementController;
    }

    /**
     * Creates {@link Request} instance for sending given event outside. This instance enables to further customized the
     * request behavior and ({@link Request#send()}) the request.
     *
     * @param timestampedEvent event to send
     * @return {@link Request} instance
     */
    public Request forEvent(TimestampedEvent<T> timestampedEvent) {
        return new Request(timestampedEvent);
    }

    /**
     * Definition of request object
     */
    public class Request {
        private final TimestampedEvent<T> timestampedEvent;
        private Runnable onAcknowledgeFailureCommand;

        private Request(TimestampedEvent<T> timestampedEvent) {
            this.timestampedEvent = timestampedEvent;
        }

        /**
         * Sets a command to execute when this request acknowledgement fails.
         *
         * @param onAcknowledgeFailureCommand execute this when this request acknowledgement fails
         * @return this request
         */
        public Request onAcknowledgeFailure(Runnable onAcknowledgeFailureCommand) {
            this.onAcknowledgeFailureCommand = onAcknowledgeFailureCommand;
            return this;
        }

        /**
         * Sends this request.
         *
         * @throws InterruptedException if interrupted while waiting
         */
        public void send() throws InterruptedException {
            Runnable onFailureCommand = (onAcknowledgeFailureCommand != null) ? onAcknowledgeFailureCommand : () -> {
            };

            Optional<EventWithFuture> eventWithFuture =
                    eventPublisher.publish(timestampedEvent.getEvent(), timestampedEvent.getPublishingContext())
                            .map(future -> new EventWithFuture(timestampedEvent, future));
            if (eventWithFuture.isPresent()) {
                acknowledgementController.acknowledgeEvent(eventWithFuture.get(), onFailureCommand);
            }
        }
    }
}