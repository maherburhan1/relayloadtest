package com.solarwinds.msp.ncentral.eventproduction.controller;

import com.solarwinds.msp.ncentral.eventproduction.api.service.notification.EventNotificationType;

/**
 * This interface represents the business logic related to MSP Event Producer email notifications.
 */
public interface EventNotificationService {

    /**
     * Sends an email notification of a given type to a given customer.
     *
     * @param eventingConfigurationCustomerId the Eventing Configuration Customer ID.
     * @param eventNotificationType the {@link EventNotificationType}.
     */
    void sendNotification(int eventingConfigurationCustomerId, EventNotificationType eventNotificationType);

    /**
     * Sends an email notification of a given type to all eligible customers (i.e., with enabled Event Production and
     * configured email address).
     *
     * @param eventNotificationType the {@link EventNotificationType}.
     */
    void sendNotificationToAll(EventNotificationType eventNotificationType);
}
