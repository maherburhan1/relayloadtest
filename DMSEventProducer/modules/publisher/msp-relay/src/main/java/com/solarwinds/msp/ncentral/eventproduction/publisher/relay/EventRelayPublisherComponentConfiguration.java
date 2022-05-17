package com.solarwinds.msp.ncentral.eventproduction.publisher.relay;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

/**
 * This class represents the Spring configuration for the MSP Event Relay Publisher module.
 */
@Configuration
@ComponentScan(basePackages = {"com.solarwinds.msp.ncentral.eventproduction.publisher.relay"}, lazyInit = true)
@Lazy
public class EventRelayPublisherComponentConfiguration {

}
