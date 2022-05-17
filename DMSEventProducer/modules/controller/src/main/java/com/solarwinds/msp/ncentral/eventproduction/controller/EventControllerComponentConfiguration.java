package com.solarwinds.msp.ncentral.eventproduction.controller;

import com.solarwinds.msp.ncentral.eventproduction.persistence.file.EventPersistenceFileComponentConfiguration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Lazy;

import java.time.Clock;

/**
 * This class represents the Spring configuration for the MSP Event Controller module.
 */
@Configuration
@ComponentScan(basePackages = {"com.solarwinds.msp.ncentral.eventproduction.controller"}, lazyInit = true)
@Lazy
@Import(EventPersistenceFileComponentConfiguration.class)
public class EventControllerComponentConfiguration {

    public static final String SYSTEM_DEFAULT_ZONE_CLOCK_BEAN = "systemDefaultClock";

    @Bean(name = SYSTEM_DEFAULT_ZONE_CLOCK_BEAN)
    public Clock getSystemDefaultZoneClock() {
        return Clock.systemDefaultZone();
    }

}
