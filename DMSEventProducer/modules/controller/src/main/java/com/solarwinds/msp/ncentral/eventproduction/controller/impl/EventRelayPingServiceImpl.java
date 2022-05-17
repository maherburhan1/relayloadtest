package com.solarwinds.msp.ncentral.eventproduction.controller.impl;

import com.nable.logging.Logger;
import com.nable.logging.Loggers;
import com.solarwinds.enumeration.event.EventRelayType;
import com.solarwinds.msp.ncentral.eventproduction.api.service.globalControl.EventGetGlobalControlConfiguration;
import com.solarwinds.msp.ncentral.eventproduction.api.service.pinging.EventHealthCheck;
import com.solarwinds.msp.ncentral.eventproduction.api.service.pinging.HealthCheckInfo;
import com.solarwinds.msp.ncentral.eventproduction.controller.EventRelayPingService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of the Event Relay Ping Service. Responsible for pinging all MSP Relays and checking if they are able
 * to receive events.
 */
@Service
public class EventRelayPingServiceImpl implements EventRelayPingService {

    private static final Logger logger = Loggers.EVENT_PRODUCER;

    private final EventHealthCheck relayHealthCheck;
    private final EventGetGlobalControlConfiguration eventGetGlobalControlConfiguration;

    @Autowired
    public EventRelayPingServiceImpl(EventHealthCheck relayHealthCheck,
            @Lazy EventGetGlobalControlConfiguration eventGetGlobalControlConfiguration) {
        this.relayHealthCheck = relayHealthCheck;
        this.eventGetGlobalControlConfiguration = eventGetGlobalControlConfiguration;
    }

    @Override
    public boolean checkHealthOfAllRelays() {
        List<HealthCheckInfo> relayResponses = new ArrayList<>();
        try {
            for (String businessApplicationsCustomerId : eventGetGlobalControlConfiguration.getActiveBusinessApplicationsCustomerIds()) {
                relayResponses.add(pingRelay(businessApplicationsCustomerId));
            }
        } catch (RemoteException e) {
            logger.error("An error was encountered while trying to ping all available relays.", e);
            return false;
        }

        return relayResponses.stream().allMatch(HealthCheckInfo::isHealthy);
    }

    private HealthCheckInfo pingRelay(String businessApplicationsCustomerId) {
        return relayHealthCheck.ping(businessApplicationsCustomerId, EventRelayType.EVENT);
    }
}