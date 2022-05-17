package com.solarwinds.msp.ncentral.eventproduction.controller.impl;

import com.nable.logging.Logger;
import com.nable.logging.Loggers;
import com.solarwinds.msp.ncentral.eventproduction.api.service.tracking.EventingControlService;
import com.solarwinds.msp.ncentral.eventproduction.api.service.tracking.EventingStartupListener;
import com.solarwinds.msp.ncentral.eventproduction.controller.EventControllerConfiguration;

import org.springframework.stereotype.Component;

import java.util.Observer;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Throttle to limit event send rate based on a count of pending events acknowledgements.
 */
@Component
public class Throttle implements EventingStartupListener {

    private static final Logger logger = Loggers.EVENT_PRODUCER;

    private static final int MAXIMUM_PENDING_EVENTS_COUNT_DEFAULT = 20_000;

    private final AtomicInteger pendingEventsCount = new AtomicInteger(0);
    private int maximumPendingEventsCount = MAXIMUM_PENDING_EVENTS_COUNT_DEFAULT;

    private final EventControllerConfiguration eventControllerConfiguration;

    public Throttle(AcknowledgementController acknowledgementController,
            EventControllerConfiguration eventControllerConfiguration, EventingControlService eventingControlService) {
        this.eventControllerConfiguration = eventControllerConfiguration;
        acknowledgementController.addObserver(createObserver());

        eventingControlService.addStartupListenerOrExecuteStartup(this);
    }

    @Override
    public void onEventingStart() {
        eventControllerConfiguration.getMaximumAcknowledgeBufferSize()
                .ifPresent(size -> maximumPendingEventsCount = size);
        logger.info("Component {} initialized.", this.getClass().getSimpleName());
    }

    private Observer createObserver() {
        return (observable, count) -> {
            synchronized (this) {
                pendingEventsCount.set((Integer) count);
                logger.debug("Count of pending events for acknowledgement is {}.", pendingEventsCount.get());
                notify();
            }
        };
    }

    /**
     * @return {@code true} when events cannot be sent at the moment / {@code false} when events can be sent
     */
    public boolean isClosed() {
        return pendingEventsCount.get() >= maximumPendingEventsCount;
    }

    /**
     * Blocks until events can be sent.
     *
     * @throws InterruptedException if interrupted while waiting
     */
    public void waitUntilOpen() throws InterruptedException {
        synchronized (this) {
            while (isClosed()) {
                logger.info("Waiting until count of pending events for acknowledgement becomes lower then {}.",
                        maximumPendingEventsCount);
                wait();
            }
        }
    }
}
