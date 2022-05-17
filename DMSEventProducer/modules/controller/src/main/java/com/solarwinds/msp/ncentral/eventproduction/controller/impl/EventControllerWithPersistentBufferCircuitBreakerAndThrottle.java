package com.solarwinds.msp.ncentral.eventproduction.controller.impl;

import com.google.protobuf.MessageLite;

import com.nable.logging.Logger;
import com.nable.logging.Loggers;
import com.solarwinds.error.GenericRuntimeException;
import com.solarwinds.msp.ncentral.eventproduction.api.entity.EventStatistic;
import com.solarwinds.msp.ncentral.eventproduction.api.service.persistence.TimestampedEvent;
import com.solarwinds.msp.ncentral.eventproduction.api.service.publisher.PublishingContext;
import com.solarwinds.msp.ncentral.eventproduction.api.service.statistics.EventStatistics;
import com.solarwinds.msp.ncentral.eventproduction.api.service.tracking.ComponentStatusService;
import com.solarwinds.msp.ncentral.eventproduction.api.service.tracking.EventingControlService;
import com.solarwinds.msp.ncentral.eventproduction.api.service.tracking.EventingStartupListener;
import com.solarwinds.msp.ncentral.eventproduction.controller.EventBufferController;
import com.solarwinds.msp.ncentral.eventproduction.controller.EventController;
import com.solarwinds.msp.ncentral.eventproduction.controller.EventControllerConfiguration;
import com.solarwinds.msp.ncentral.eventproduction.controller.EventRetries;
import com.solarwinds.util.concurrent.InterruptibleRunnable;

import org.springframework.stereotype.Component;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Observable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

/**
 * {@link EventController} implementation that orchestrates:
 * <ul>
 * <li>buffering and persisting events locally</li>
 * <li>throttling events in case of high load</li>
 * <li>circuit breaking and retries in case of external communication issues</li>
 * <li>sending events outside</li>
 * <li>re-sending events in case of events acknowledgement issues</li>
 * <li>sending events directly while keeping throttling and circuit breaking logic</li>
 * </ul>
 *
 * @param <T> the event type.
 */
@Component
public class EventControllerWithPersistentBufferCircuitBreakerAndThrottle<T extends MessageLite>
        implements EventController<T>, EventingStartupListener {

    private static final Logger logger = Loggers.EVENT_PRODUCER;

    private static final int DEFAULT_MAXIMUM_EVENT_RETRIES_COUNT_ON_ACKNOWLEDGEMENT_ERROR = EventRetries.INFINITE;

    private final AcknowledgementController<T> acknowledgementController;
    private final CircuitBreaker circuitBreaker;
    private final ComponentStatusService componentStatusService;
    private final EventBufferController<T> eventBufferController;
    private final EventControllerConfiguration eventControllerConfiguration;
    private final EventFailuresMonitor eventFailuresMonitor;
    private final EventSender<T> eventSender;
    private final EventStatistics eventStatistics;
    private final EventingControlService eventingControlService;
    private final Throttle throttle;

    private final AtomicBoolean isStarted = new AtomicBoolean(false);

    private int eventRetriesLimit;
    private ExecutorService executorService;

    /**
     * Creates an instance of this class with the specified parameters.
     */
    public EventControllerWithPersistentBufferCircuitBreakerAndThrottle(EventBufferController<T> eventBufferController,
            CircuitBreaker circuitBreaker, Throttle throttle, EventSender<T> eventSender,
            AcknowledgementController<T> acknowledgementController, EventFailuresMonitor eventFailuresMonitor,
            EventingControlService eventingControlService, EventStatistics eventStatistics,
            ComponentStatusService componentStatusService, EventControllerConfiguration eventControllerConfiguration) {
        this.eventBufferController = eventBufferController;
        this.circuitBreaker = circuitBreaker;
        this.throttle = throttle;
        this.eventSender = eventSender;
        this.acknowledgementController = acknowledgementController;
        this.eventFailuresMonitor = eventFailuresMonitor;
        this.eventingControlService = eventingControlService;
        this.eventControllerConfiguration = eventControllerConfiguration;
        this.eventStatistics = eventStatistics;
        this.componentStatusService = componentStatusService;

        this.eventingControlService.addObserver(this);
        this.eventingControlService.addStartupListenerOrExecuteStartup(this);
    }

    @Override
    public void onEventingStart() {
        final int value = eventControllerConfiguration.getMaximumEventRetriesCountOnAcknowledgementError()
                .orElse(DEFAULT_MAXIMUM_EVENT_RETRIES_COUNT_ON_ACKNOWLEDGEMENT_ERROR);
        eventRetriesLimit = EventRetries.getValidValue(value);
        logger.info("Component {} initialized.", getClass().getSimpleName());
    }

    @Override
    public void update(Observable observable, Object argument) {
        if (eventingControlService.getGlobalSendingEnabled() && !isStarted.get()) {
            start();
        } else if (!eventingControlService.getGlobalSendingEnabled() && isStarted.get()) {
            stop();
        }
    }

    @Override
    public void publishEvents(List<T> events, ZonedDateTime timestamp, PublishingContext publishingContext) {
        final Function<T, TimestampedEvent<T>> eventMapping =
                event -> new TimestampedEvent<>(event, timestamp, publishingContext);
        if (publishingContext.isSkipBuffer()) {
            events.stream().map(eventMapping).forEach(this::sendEventDirectlyRetryIfNecessary);
        } else {
            events.stream().map(eventMapping).forEach(this::addEventIntoBuffer);
        }
    }

    /**
     * Attempts to send event directly without buffering one time.
     *
     * @param eventData the {@link TimestampedEvent} data to be sent.
     * @return {@code true} if event data was sent successfully, {@code false} otherwise.
     * @throws InterruptedException if the execution was interrupted while waiting.
     */
    private boolean sendEventDirectly(TimestampedEvent<T> eventData) throws InterruptedException {
        if (logger.isDebugEnabled()) {
            final T event = eventData.getEvent();
            logger.debug("Buffer bypass: Event [{}] of type [{}] with content=[{}] accepted for direct processing.",
                    event.hashCode(), event.getClass().getSimpleName(), event);
        }
        throttle.waitUntilOpen();
        return !circuitBreaker.execute(getEventSendCommand(eventData)).callFailed();
    }

    private void addEventIntoBuffer(TimestampedEvent<T> eventData) {
        if (logger.isDebugEnabled()) {
            final T event = eventData.getEvent();
            logger.debug("Event [{}] of type [{}] with content=[{}] accepted for processing.", event.hashCode(),
                    event.getClass().getSimpleName(), event);
        }
        eventBufferController.enqueue(eventData);
    }

    private synchronized void start() {
        logger.info("Events processing start requested.");
        if (!isStarted.getAndSet(true)) {
            acknowledgementController.start();
            eventFailuresMonitor.resetAll();
            executorService = Executors.newSingleThreadExecutor();
            executorService.execute(this::processEvents);
        }
    }

    private synchronized void stop() {
        logger.info("Events processing stop requested.");
        if (isStarted.getAndSet(false)) {
            acknowledgementController.close();
            eventBufferController.close();
            if (executorService != null) {
                executorService.shutdownNow();
            }
        }
    }

    private void processEvents() {
        componentStatusService.setRunning(getClass(), true);
        logger.info("Events processing started.");

        try {
            while (isStarted.get() && !Thread.interrupted()) {
                if (throttle.isClosed()) {
                    throttle.waitUntilOpen();
                    continue;
                }
                final TimestampedEvent<T> event = eventBufferController.dequeue().orElse(null);
                if (event != null) {
                    circuitBreaker.execute(getEventSendCommand(event)).onFailure(getOnEventSendFailureCommand(event));
                }
            }
        } catch (InterruptedException e) {
            logger.info("Events processing interrupted.");
        } finally {
            if (isStarted.get()) {
                stop();
            }
            componentStatusService.setRunning(getClass(), false);
            logger.info("Events processing stopped.");
        }
    }

    private Runnable getOnEventSendFailureCommand(TimestampedEvent<T> event) {
        return () -> {
            event.setSendRetryCount(0);
            if (event.getPublishingContext().isSkipBuffer()) {
                logger.debug("Buffer bypassing event [{}] of type [{}] failed to send, resending directly outside of "
                        + "buffer.", event.getEvent().hashCode(), event.getEvent().getClass().getSimpleName());
                sendEventDirectlyRetryIfNecessary(event);
            } else {
                eventBufferController.addFirst(event);
                monitorFailure(event);
            }
        };
    }

    private InterruptibleRunnable getEventSendCommand(TimestampedEvent<T> event) {
        return () -> eventSender.forEvent(event).onAcknowledgeFailure(getOnAcknowledgeFailureCommand(event)).send();
    }

    private Runnable getOnAcknowledgeFailureCommand(TimestampedEvent<T> event) {
        final int eventRetriesCount = Math.max(event.getSendRetryCount(), 0);
        // check if the event should be re-sent or not
        if (shouldRetryAfter(eventRetriesCount)) {
            return () -> {
                final int newRetriesCount = event.incrementRetryCount();
                if (event.getPublishingContext().isSkipBuffer()) {
                    logger.debug(
                            "Buffer bypassing event [{}] of type [{}] failed to send, resending directly outside of "
                                    + "buffer.", event.getEvent().hashCode(),
                            event.getEvent().getClass().getSimpleName());
                    sendEventDirectlyRetryIfNecessary(event);
                } else {
                    eventBufferController.addFirst(event);
                }
                monitorFailure(event);
                eventStatistics.addStatistic(EventStatistic.builder()
                        .statisticType(EventStatistic.StatisticType.PROCESSING_DATA)
                        .statisticSubType(EventStatistic.StatisticSubType.PUBLISH_RETRY)
                        .statisticValue(1)
                        .build());
                logger.info("Event [{}] of type [{}] re-send attempt #{} on acknowledge failure.",
                        event.getEvent().hashCode(), event.getEvent().getClass().getSimpleName(), newRetriesCount);
            };
        } else {
            return () -> {
                monitorFailure(event);
                logger.error("Event [{}] of type [{}] send failed due to #{} acknowledge failure(s).",
                        event.getEvent().hashCode(), event.getEvent().getClass().getSimpleName(),
                        eventRetriesCount + 1);
            };
        }
    }

    private void monitorFailure(TimestampedEvent<T> event) {
        eventFailuresMonitor.processFailure(event.getPublishingContext().getEventingConfigurationCustomerId());
    }

    private boolean shouldRetryAfter(int currentRetryCount) {
        return eventRetriesLimit != EventRetries.NO_RETRIES && (eventRetriesLimit == EventRetries.INFINITE
                || eventRetriesLimit > currentRetryCount);
    }

    private void sendEventDirectlyRetryIfNecessary(TimestampedEvent<T> event) {
        try {
            int currentRetryCount = 0;
            while (!sendEventDirectly(event)) {
                monitorFailure(event);
                if (!shouldRetryAfter(currentRetryCount)) {
                    break;
                }
                currentRetryCount++;
            }
        } catch (InterruptedException e) {
            final String errorMessage = "The direct sending of an event was interrupted.";
            logger.warn(errorMessage);
            Thread.currentThread().interrupt();
            throw new GenericRuntimeException(errorMessage, e);
        }
    }
}