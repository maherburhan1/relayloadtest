package com.solarwinds.msp.ncentral.eventproduction.producer;

import com.nable.logging.Logger;
import com.nable.logging.Loggers;
import com.solarwinds.error.GenericRuntimeException;
import com.solarwinds.msp.ncentral.eventproduction.api.entity.Event;
import com.solarwinds.msp.ncentral.eventproduction.api.entity.ServerStatusEvent;
import com.solarwinds.msp.ncentral.eventproduction.api.service.publisher.PublishingContext;
import com.solarwinds.msp.ncentral.eventproduction.controller.EventController;
import com.solarwinds.msp.ncentral.eventproduction.controller.impl.EventEmissionMonitor;
import com.solarwinds.msp.ncentral.eventproduction.converter.EventParser;
import com.solarwinds.msp.ncentral.eventproduction.converter.ServerStatusEventParser;
import com.solarwinds.util.function.TracingRunnable;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

/**
 * Entry point into event publishing pipeline.
 *
 * @param <T> the event type.
 */
@Component
public class EventProducer<T> {

    private static final Logger logger = Loggers.EVENT_PRODUCER;

    private final EventFilter eventFilter;
    private final EventParser<T> eventParser;
    private final ServerStatusEventParser<T> serverStatusEventParser;
    private final EventController<T> eventsController;
    private final EventEmissionMonitor eventEmissionMonitor;

    public EventProducer(EventFilter eventFilter, EventParser<T> eventParser,
            ServerStatusEventParser<T> serverStatusEventParser, EventController<T> eventController,
            EventEmissionMonitor eventEmissionMonitor) {
        this.eventFilter = eventFilter;
        this.eventParser = eventParser;
        this.serverStatusEventParser = serverStatusEventParser;
        this.eventsController = eventController;
        this.eventEmissionMonitor = eventEmissionMonitor;
    }

    /**
     * Tests if given {@link Event} should be published, converts it into desired {@link T} format and finally sends
     * further the event publishing pipeline.
     *
     * @param event the event to be published.
     * @throws GenericRuntimeException if an error occurs in runtime.
     */
    public void send(Event event) {
        if (eventFilter.test(event)) {
            final Supplier<String> tracingMessageSupplier =
                    () -> String.format("Publishing event for entity type [%s]: [%s]", event.getEntityType(), event);
            new TracingRunnable(() -> publish(event), logger, tracingMessageSupplier).run();
        } else {
            logger.trace("Event for entity type [{}] dropped: [{}]", event.getEntityType(), event);
        }
    }

    /**
     * Sends server status event further to eventing pipeline.
     *
     * @param event the event with server status to be published.
     * @throws GenericRuntimeException if an error occurs in runtime.
     */
    public void publishServerStatus(ServerStatusEvent event) {
        eventsController.publishEvents(Collections.singletonList(serverStatusEventParser.parse(event)),
                event.getEventTime(), createContext(event));
    }

    private void publish(Event event) {
        final List<T> parsedEvents = eventParser.parse(event);
        if (CollectionUtils.isNotEmpty(parsedEvents)) {
            eventEmissionMonitor.recordEmittedEvent(event.getEventingConfigurationCustomerId(), event.getEntityType(),
                    event.getUpdateTimestamp());
            eventsController.publishEvents(parsedEvents, event.getUpdateTimestamp(), createContext(event));
        }
    }

    private PublishingContext createContext(Event event) {
        final PublishingContext.Builder contextBuilder = PublishingContext.builder()
                .withSystemGuid(event.getNcentralServerGuid())
                .withEntityType(event.getEntityType())
                .withSkipBuffer(event.isDirectSend())
                .withEventingConfigurationCustomerId(event.getEventingConfigurationCustomerId());
        event.getBizappsCustomerId().ifPresent(contextBuilder::withBizappsCustomerId);
        return contextBuilder.build();
    }

    private PublishingContext createContext(ServerStatusEvent event) {
        final PublishingContext.Builder contextBuilder = PublishingContext.builder()
                .withSystemGuid(event.getNcentralServerGuid())
                .withSkipBuffer(event.isDirectSend())
                .withEventingConfigurationCustomerId(event.getEventingConfigurationCustomerId());
        event.getBizappsCustomerId().ifPresent(contextBuilder::withBizappsCustomerId);
        return contextBuilder.buildForServerStatusEvent();
    }
}
