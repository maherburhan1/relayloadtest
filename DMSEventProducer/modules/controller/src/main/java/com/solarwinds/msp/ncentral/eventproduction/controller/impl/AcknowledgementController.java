package com.solarwinds.msp.ncentral.eventproduction.controller.impl;

import com.google.protobuf.MessageLite;

import com.nable.logging.Logger;
import com.nable.logging.Loggers;
import com.solarwinds.dto.Tuple;
import com.solarwinds.msp.ncentral.eventproduction.api.entity.EventHighWaterMark;
import com.solarwinds.msp.ncentral.eventproduction.api.entity.EventStatistic;
import com.solarwinds.msp.ncentral.eventproduction.api.service.failure.FailureNotificationService;
import com.solarwinds.msp.ncentral.eventproduction.api.service.publisher.CircuitBreaker;
import com.solarwinds.msp.ncentral.eventproduction.api.service.publisher.PublishedEventInfo;
import com.solarwinds.msp.ncentral.eventproduction.api.service.publisher.PublishingContext;
import com.solarwinds.msp.ncentral.eventproduction.api.service.statistics.EventStatistics;
import com.solarwinds.msp.ncentral.eventproduction.api.service.tracking.ComponentStatusService;
import com.solarwinds.msp.ncentral.eventproduction.api.service.tracking.EventTracker;
import com.solarwinds.msp.ncentral.eventproduction.api.service.tracking.EventingConfigurationChange;
import com.solarwinds.msp.ncentral.eventproduction.api.service.tracking.EventingControlService;
import com.solarwinds.msp.ncentral.eventproduction.api.service.tracking.EventingStartupListener;
import com.solarwinds.msp.ncentral.eventproduction.controller.EventControllerConfiguration;
import com.solarwinds.msp.ncentral.eventproduction.controller.EventWithFuture;
import com.solarwinds.util.NullChecker;

import org.springframework.stereotype.Component;

import java.rmi.RemoteException;
import java.time.Duration;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * This class represents the Acknowledgement Controller. It handles acknowledgements of all events sent.
 *
 * @param <T> the event type.
 */
@Component
public class AcknowledgementController<T extends MessageLite> extends Observable
        implements Observer, EventingStartupListener, AutoCloseable {

    private static final String TABLE_DELIMITER = ":";
    private static final Logger logger = Loggers.EVENT_PRODUCER;

    private static final Duration DEFAULT_MAXIMUM_WAIT_TIME_AFTER_ERROR = Duration.ofMillis(60_000L);
    private static final Duration DEFAULT_WAIT_TIME_FOR_RESPONSE_AFTER_SENDING = Duration.ofMillis(15_000L);

    private final CircuitBreaker circuitBreaker;
    private final EventControllerConfiguration eventControllerConfiguration;
    private final EventFailuresMonitor eventFailuresMonitor;
    private final EventStatistics eventStatistics;
    private final EventTracker eventTracker;
    private final FailureNotificationService failureNotificationService;

    private final AtomicBoolean isStarted = new AtomicBoolean(false);
    private final AtomicInteger pendingEventsCount = new AtomicInteger(0);
    private final BlockingDeque<Tuple<EventWithFuture<T>, Runnable>> pendingEvents = new LinkedBlockingDeque<>();
    private final Map<String, AtomicInteger> pendingAcknowledgementsPerCustomerTable = new ConcurrentHashMap<>();
    private final Semaphore pendingEventsPermit = new Semaphore(1, true);
    private final ComponentStatusService componentStatusService;
    private final EventingControlService eventingControlService;

    private Duration waitTimeForEventProcessing;
    private ExecutorService executorServiceForMainProcessing;
    private ExecutorService executorServiceForOnAcknowledgeFailureCommands;

    /**
     * @param eventControllerConfiguration {@link EventControllerConfiguration} to get configuration from
     * @param eventFailuresMonitor {@link EventFailuresMonitor} to track errors in processing
     * @param eventStatistics {@link EventStatistics} to track processing metrics
     * @param eventTracker {@link EventTracker} to update high water marks for acknowledged events
     * @param circuitBreaker {@link CircuitBreaker} for notifying of acknowledge errors
     * @param componentStatusService {@link ComponentStatusService} to make the internal processing state visible to
     * others
     * @param eventingControlService the eventing status tracking service.
     * @param failureNotificationService the service responsible for collecting and notifying relevant observers about
     * exceptions in acknowledgement.
     */
    public AcknowledgementController(EventControllerConfiguration eventControllerConfiguration,
            EventFailuresMonitor eventFailuresMonitor, EventStatistics eventStatistics, EventTracker eventTracker,
            CircuitBreaker circuitBreaker, ComponentStatusService componentStatusService,
            EventingControlService eventingControlService, FailureNotificationService failureNotificationService) {
        this.eventControllerConfiguration = eventControllerConfiguration;
        this.eventFailuresMonitor = eventFailuresMonitor;
        this.eventStatistics = eventStatistics;
        this.eventTracker = eventTracker;
        this.circuitBreaker = circuitBreaker;
        this.componentStatusService = componentStatusService;
        this.eventingControlService = eventingControlService;
        this.failureNotificationService = failureNotificationService;

        eventingControlService.addObserver(this);
        eventingControlService.addStartupListenerOrExecuteStartup(this);
    }

    /**
     * Returns current amount of pending acknowledgements for given table and eventingConfigurationCustomerId
     *
     * @param tableName table to get acknowledgements count for
     * @param eventingConfigurationCustomerId eventing configuration customer id
     * @return number of pending events
     */
    public int getPendingAcknowledgementsForCustomerTable(String tableName, int eventingConfigurationCustomerId) {
        int pendingEventsCount = 0;
        final AtomicInteger pendingEvents = pendingAcknowledgementsPerCustomerTable.get(
                eventingConfigurationCustomerId + TABLE_DELIMITER + tableName);
        if (pendingEvents != null) {
            pendingEventsCount = pendingEvents.get();
        }
        return pendingEventsCount;
    }

    @Override
    public void onEventingStart() {
        waitTimeForEventProcessing = getWaitTimeForEventProcessing();
        pendingAcknowledgementsPerCustomerTable.clear();
        logger.info("Component {} initialized.", this.getClass().getSimpleName());
    }

    /**
     * Acknowledges the event.
     *
     * @param event the event to acknowledge.
     * @param onAcknowledgeFailureCommand the command to run in case of acknowledge failure.
     * @throws InterruptedException if interrupted while waiting.
     */
    public void acknowledgeEvent(EventWithFuture<T> event, Runnable onAcknowledgeFailureCommand)
            throws InterruptedException {
        pendingEvents.put(new Tuple<>(NullChecker.check(event, "event"),
                NullChecker.check(onAcknowledgeFailureCommand, "onAcknowledgeFailureCommand")));
        pendingAcknowledgementsPerCustomerTable.computeIfAbsent(getPendingAcknowledgementsCustomerTableKey(event),
                key -> new AtomicInteger(0)).incrementAndGet();
        setChanged();
        notifyObservers(pendingEventsCount.incrementAndGet());
    }

    private String getPendingAcknowledgementsCustomerTableKey(EventWithFuture<T> event) {
        return event.getPublishingContext().getEventingConfigurationCustomerId() + TABLE_DELIMITER
                + event.getPublishingContext().getEntityType();
    }

    /**
     * Starts the Acknowledgement Controller.
     */
    public synchronized void start() {
        logger.info("Events acknowledgement processing start requested.");
        if (!isStarted.getAndSet(true)) {
            executorServiceForMainProcessing = Executors.newSingleThreadExecutor();
            executorServiceForMainProcessing.execute(this::processAcknowledgements);

            executorServiceForOnAcknowledgeFailureCommands = Executors.newSingleThreadExecutor();
        }
    }

    /**
     * Closes the Acknowledgement Controller.
     */
    public synchronized void close() {
        logger.info("Events acknowledgement processing stop requested.");
        if (isStarted.getAndSet(false)) {
            if (executorServiceForMainProcessing != null) {
                executorServiceForMainProcessing.shutdownNow();
                executorServiceForMainProcessing = null;
            }
            if (executorServiceForOnAcknowledgeFailureCommands != null) {
                executorServiceForOnAcknowledgeFailureCommands.shutdownNow();
                executorServiceForOnAcknowledgeFailureCommands = null;
            }
        }
    }

    private void processAcknowledgements() {
        componentStatusService.setRunning(getClass(), true);
        logger.info("Events acknowledgement processing started.");

        try {
            while (isStarted.get() && !Thread.currentThread().isInterrupted()) {
                Tuple<EventWithFuture<T>, Runnable> pendingEvent;
                try {
                    pendingEventsPermit.acquire();
                    pendingEvent = pendingEvents.take();
                } finally {
                    pendingEventsPermit.release();
                }
                processPendingEvent(pendingEvent.getFirst(), pendingEvent.getSecond());
            }
        } catch (InterruptedException e) {
            logger.info("Events acknowledgement processing interrupted.");
        } finally {
            if (isStarted.get()) {
                close();
            }
            componentStatusService.setRunning(getClass(), false);
            logger.info("Events acknowledgement processing stopped.");
        }
    }

    private void decreasePendingEventsCountAndNotifyObservers(EventWithFuture<T> pendingEvent) {
        pendingAcknowledgementsPerCustomerTable.get(getPendingAcknowledgementsCustomerTableKey(pendingEvent))
                .decrementAndGet();
        setChanged();
        notifyObservers(pendingEventsCount.decrementAndGet());
    }

    private void processPendingEvent(EventWithFuture<T> event, Runnable onAcknowledgeFailureCommand)
            throws InterruptedException {
        try {
            PublishedEventInfo publishedEventInfo =
                    getPublishedEventInfoOrReturnAcknowledgementBackToQueueWhenInterrupted(event,
                            onAcknowledgeFailureCommand);
            if (publishedEventInfo.isSuccess()) {
                handlePublishingSuccess(event, publishedEventInfo);
            } else {
                handlePublishingFailure(event, onAcknowledgeFailureCommand);
                logEventPublishingFailure(event, publishedEventInfo);
            }
        } catch (ExecutionException e) {
            handlePublishingFailure(event, onAcknowledgeFailureCommand);
            logAcknowledgementFailure(event, e);
            handleExecutionException(e, event);
        } catch (TimeoutException e) {
            handlePublishingFailure(event, onAcknowledgeFailureCommand);
            logAcknowledgementFailure(event, e);
        }
    }

    private PublishedEventInfo getPublishedEventInfoOrReturnAcknowledgementBackToQueueWhenInterrupted(
            EventWithFuture<T> event, Runnable onAcknowledgeFailureCommand)
            throws ExecutionException, TimeoutException, InterruptedException {
        try {
            return event.getFuture().get(waitTimeForEventProcessing.toMillis(), TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            try {
                pendingEventsPermit.acquire();
                pendingEvents.addFirst(new Tuple<>(event, onAcknowledgeFailureCommand));
            } finally {
                pendingEventsPermit.release();
            }

            throw e;
        }
    }

    private void handlePublishingSuccess(EventWithFuture<T> event, PublishedEventInfo publishedEventInfo) {
        if (logger.isDebugEnabled()) {
            T protoEvent = event.getEvent();
            logger.debug("Event [{}] of type [{}] acknowledged: info=[{}], event size=[{}].", protoEvent.hashCode(),
                    protoEvent.getClass().getSimpleName(), publishedEventInfo.getInfo(),
                    protoEvent.toByteArray().length);
        }

        // notify the Event Publisher Circuit Breaker to reset after a success has occurred
        circuitBreaker.handleSuccess();

        eventFailuresMonitor.processSuccess(event.getPublishingContext().getEventingConfigurationCustomerId());
        eventStatistics.addStatistic(EventStatistic.builder()
                .statisticType(EventStatistic.StatisticType.PROCESSING_DATA)
                .statisticSubType(EventStatistic.StatisticSubType.RELAY_ACKNOWLEDGED)
                .statisticValue(1)
                .build());

        // Every time a message is successfully acknowledged set a new high water mark for that
        trackAcknowledgedEvent(event);

        decreasePendingEventsCountAndNotifyObservers(event);
    }

    private void logEventPublishingFailure(EventWithFuture<T> event, PublishedEventInfo publishedEventInfo) {
        logger.warn(
                "Event [{}] of type [{}] not acknowledged because it was not published successfully: " + "info=[{}].",
                event.getEvent().hashCode(), event.getEvent().getClass().getSimpleName(), publishedEventInfo.getInfo());
        eventStatistics.addStatistic(EventStatistic.builder()
                .statisticType(EventStatistic.StatisticType.ERROR_HANDLING)
                .statisticSubType(EventStatistic.StatisticSubType.RELAY_FATAL_ERROR)
                .statisticValue(1)
                .build());
    }

    private void logAcknowledgementFailure(EventWithFuture<T> event, Throwable exception) {
        logger.warn("Event [{}] of type [{}] not acknowledged due to the following error.", event.getEvent().hashCode(),
                event.getEvent().getClass().getSimpleName(), exception);
        eventStatistics.addStatistic(EventStatistic.builder()
                .statisticType(EventStatistic.StatisticType.ERROR_HANDLING)
                .statisticSubType(EventStatistic.StatisticSubType.CONTROLLER_FATAL_ERROR)
                .statisticValue(1)
                .build());
    }

    /**
     * If the root cause of the ExecutionException is a gRPC Status Runtime Exception, we will handle it here.
     */
    private void handleExecutionException(ExecutionException exception, EventWithFuture<T> event) {
        final Throwable cause = exception.getCause();
        if (cause != null) {
            event.getPublishingContext()
                    .getBizappsCustomerId()
                    .ifPresent(businessApplicationsCustomerId -> failureNotificationService.registerException(
                            businessApplicationsCustomerId, cause));
        }
    }

    private void trackAcknowledgedEvent(EventWithFuture<T> event) {
        final PublishingContext publishingContext = event.getPublishingContext();
        final T protoEvent = event.getEvent();
        if (publishingContext.getEntityType() != null) {
            final EventHighWaterMark.HighWaterMarkBuilder highWaterMarkBuilder = EventHighWaterMark.builder()
                    .entityName(protoEvent.getClass().getSimpleName())
                    .tableName(publishingContext.getEntityType())
                    .lastProcessed(event.getTimestamp())
                    .customerId(event.getPublishingContext().getEventingConfigurationCustomerId());
            try {
                eventTracker.trackEventTimestamp(highWaterMarkBuilder.build());
            } catch (RemoteException e) {
                logger.error("Cannot track timestamp of acknowledged event id [{}] of type [{}].",
                        protoEvent.hashCode(), protoEvent.getClass().getSimpleName(), e);
            }
        } else {
            logger.warn("Received acknowledgement for event id [{}] of type [{}] without entity type, which can't be "
                    + "tracked.", protoEvent.hashCode(), protoEvent.getClass().getSimpleName());
        }
    }

    private synchronized void handlePublishingFailure(EventWithFuture<T> event, Runnable onAcknowledgeFailureCommand) {
        // notify the Event Publisher Circuit Breaker to slow down after an error has occurred
        circuitBreaker.handleFailure();

        if (eventingControlService.isEventingEnabledForCustomer(
                event.getPublishingContext().getEventingConfigurationCustomerId())) {
            executorServiceForOnAcknowledgeFailureCommands.submit(() -> {
                onAcknowledgeFailureCommand.run();
                decreasePendingEventsCountAndNotifyObservers(event);
            });
        } else {
            logger.debug(
                    "Event [{}] of type [{}] is dropped because Event Production has been disabled for customer {}.",
                    event.getEvent().hashCode(), event.getEvent().getClass().getSimpleName(),
                    event.getPublishingContext().getEventingConfigurationCustomerId());
            decreasePendingEventsCountAndNotifyObservers(event);
        }
    }

    private Duration getWaitTimeForEventProcessing() {
        // A fail-safe timeout; it is not supposed to be needed under standard circumstances.
        final Duration maximumWaitTimeAfterError = eventControllerConfiguration.getMaximumWaitTimeAfterError()
                .orElse(DEFAULT_MAXIMUM_WAIT_TIME_AFTER_ERROR);
        final Duration waitTimeForResponseAfterSending =
                eventControllerConfiguration.getWaitTimeForResponseAfterSending()
                        .orElse(DEFAULT_WAIT_TIME_FOR_RESPONSE_AFTER_SENDING);
        return maximumWaitTimeAfterError.plus(waitTimeForResponseAfterSending).multipliedBy(2);
    }

    /**
     * Removes all events that are currently in any phase of the sending/acknowledgment process if the Event Production
     * has been disabled for a given customer.
     */
    @Override
    public void update(Observable observable, Object argument) {
        if (argument instanceof EventingConfigurationChange) {
            final EventingConfigurationChange configurationChange = (EventingConfigurationChange) argument;
            configurationChange.getRemoveEventsForCustomer().ifPresent(this::removeEventsForCustomer);
        }
    }

    private void removeEventsForCustomer(int customerId) {
        eventFailuresMonitor.reset(customerId);
        if (pendingEventsCount.get() > 0) {
            try {
                logger.info("Event Production has been disabled for customer {}. All pending events will be dropped.",
                        customerId);
                pendingEventsPermit.acquire();
                pendingEvents.stream()
                        .filter(event -> event.getFirst().getPublishingContext().getEventingConfigurationCustomerId()
                                == customerId)
                        .forEach(this::cancelAndRemove);
            } catch (InterruptedException exception) {
                logger.error("Removal of events for customer {} has not been finished.", customerId, exception);
                Thread.currentThread().interrupt();
                throw new CompletionException(exception);
            } finally {
                pendingEventsPermit.release();
            }
        }
    }

    private void cancelAndRemove(Tuple<EventWithFuture<T>, Runnable> event) {
        final EventWithFuture<T> removedEvent = event.getFirst();
        removedEvent.getFuture().cancel(false);
        pendingEvents.remove(event);

        logger.debug("Event [{}] of type [{}] is dropped because Event Production has been disabled for customer {}.",
                removedEvent.getEvent().hashCode(), removedEvent.getEvent().getClass().getSimpleName(),
                removedEvent.getPublishingContext().getEventingConfigurationCustomerId());

        decreasePendingEventsCountAndNotifyObservers(event.getFirst());
    }
}