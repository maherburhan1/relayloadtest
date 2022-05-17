package com.solarwinds.msp.ncentral.eventproduction.controller.impl;

import com.nable.logging.Logger;
import com.nable.logging.Loggers;
import com.solarwinds.dto.Tuple;
import com.solarwinds.msp.ncentral.eventproduction.api.service.notification.EventNotificationType;
import com.solarwinds.msp.ncentral.eventproduction.api.service.tracking.EventingControlService;
import com.solarwinds.msp.ncentral.eventproduction.api.service.tracking.EventingStartupListener;
import com.solarwinds.msp.ncentral.eventproduction.controller.EventBufferController;
import com.solarwinds.msp.ncentral.eventproduction.controller.EventControllerConfiguration;
import com.solarwinds.msp.ncentral.eventproduction.controller.EventNotificationService;
import com.solarwinds.msp.ncentral.eventproduction.controller.EventControllerComponentConfiguration;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Class for failed events monitoring. It holds a map of {@link Integer} with Eventing Configuration Customer ID and
 * {@link Tuple} with {@link Instant} representing the time of the first failed event and a flag if a notification has
 * already been sent.
 */
@Component
public class EventFailuresMonitor implements EventingStartupListener {

    private static final Logger logger = Loggers.EVENT_PRODUCER;

    private static final long DEFAULT_WAIT_TIME_IN_HOURS_FOR_UNSENT_EVENTS_NOTIFICATION = 4L;

    private final Map<Integer, Tuple<Instant, Boolean>> failedEvents = new ConcurrentHashMap<>();
    private long waitTimeForUnsentEventsNotificationInHours = DEFAULT_WAIT_TIME_IN_HOURS_FOR_UNSENT_EVENTS_NOTIFICATION;
    private final Clock clock;

    private final EventNotificationService eventNotificationService;
    private final EventControllerConfiguration eventControllerConfiguration;

    /**
     * Creates an instance of this component.
     *
     * @param eventNotificationService the service responsible for building and sending email notifications.
     * @param clock the {@link Clock} providing access to the current instant, date and time using a time-zone.
     */
    public EventFailuresMonitor(EventNotificationService eventNotificationService,
            @Qualifier(EventControllerComponentConfiguration.SYSTEM_DEFAULT_ZONE_CLOCK_BEAN) Clock clock,
            EventControllerConfiguration eventControllerConfiguration, EventingControlService eventingControlService) {
        this.eventNotificationService = eventNotificationService;
        this.clock = clock;
        this.eventControllerConfiguration = eventControllerConfiguration;

        eventingControlService.addStartupListenerOrExecuteStartup(this);
    }

    @Override
    public void onEventingStart() {
        waitTimeForUnsentEventsNotificationInHours =
                eventControllerConfiguration.getWaitTimeForUnsentEventsNotification()
                        .map(Duration::toHours)
                        .orElse(DEFAULT_WAIT_TIME_IN_HOURS_FOR_UNSENT_EVENTS_NOTIFICATION);
        logger.info("Component {} initialized.", this.getClass().getSimpleName());
    }

    /**
     * If an event is successfully sent and acknowledged, a corresponding Eventing Configuration Customer ID record is
     * removed from the map (if there is any). If an email notification on failed events was sent already, a new one
     * that conditions are cleared is sent.
     *
     * @param eventingConfigurationCustomerId the Eventing Configuration Customer ID.
     */
    public synchronized void processSuccess(int eventingConfigurationCustomerId) {
        if (failedEvents.containsKey(eventingConfigurationCustomerId)) {
            if (failedEvents.get(eventingConfigurationCustomerId).getSecond()) {
                logger.info("Events are successfully sent again for Customer ID = {}", eventingConfigurationCustomerId);
                eventNotificationService.sendNotification(eventingConfigurationCustomerId,
                        EventNotificationType.EVENTS_SUCCESSFULLY_SENT);
            }
            failedEvents.remove(eventingConfigurationCustomerId);
        }
    }

    /**
     * If an event fails to be sent / acknowledged, a corresponding Eventing Configuration Customer ID record is added
     * to the map (if there is not one already). If there have been continual failures for a given period of time, an
     * email notification is sent.
     *
     * @param eventingConfigurationCustomerId the Eventing Configuration Customer ID.
     */
    public synchronized void processFailure(int eventingConfigurationCustomerId) {
        if (failedEvents.containsKey(eventingConfigurationCustomerId)) {
            if (!failedEvents.get(eventingConfigurationCustomerId).getSecond() && Instant.now(clock)
                    .isAfter(failedEvents.get(eventingConfigurationCustomerId).getFirst())) {
                logger.info("N-central has been unable to successfully send events for {} hours for Customer ID = {}",
                        waitTimeForUnsentEventsNotificationInHours, eventingConfigurationCustomerId);
                eventNotificationService.sendNotification(eventingConfigurationCustomerId,
                        EventNotificationType.UNABLE_TO_SEND_EVENTS);
                failedEvents.get(eventingConfigurationCustomerId).setSecond(Boolean.TRUE);
            }
        } else {
            failedEvents.put(eventingConfigurationCustomerId,
                    new Tuple<>(Instant.now(clock).plus(waitTimeForUnsentEventsNotificationInHours, ChronoUnit.HOURS),
                            Boolean.FALSE));
        }
    }

    /**
     * If the flow state of the memory / file buffer is changed, a corresponding email notification is sent.
     *
     * @param flowState the current buffer flow state.
     */
    public void processBufferFlowStateUpdate(EventBufferController.BufferFlowStates flowState) {
        switch (flowState) {
            case STABLE_FLOW_TO_MEMORY:
                eventNotificationService.sendNotificationToAll(EventNotificationType.MEMORY_QUEUE_STABLE);
                break;
            case UNSTABLE_OVERFLOW_TO_FILE:
                eventNotificationService.sendNotificationToAll(EventNotificationType.MEMORY_QUEUE_CAPACITY_REACHED);
                break;
            case TERMINATED:
                eventNotificationService.sendNotificationToAll(EventNotificationType.FILE_QUEUE_CAPACITY_REACHED);
        }
    }

    /**
     * When data export is enabled for a particular Customer ID, the potential corresponding record is reset.
     *
     * @param eventingConfigurationCustomerId the Customer ID.
     */
    public synchronized void reset(int eventingConfigurationCustomerId) {
        failedEvents.remove(eventingConfigurationCustomerId);
    }

    /**
     * When data export is enabled and events processing starts, the whole map is cleared.
     */
    public synchronized void resetAll() {
        failedEvents.clear();
    }

}
