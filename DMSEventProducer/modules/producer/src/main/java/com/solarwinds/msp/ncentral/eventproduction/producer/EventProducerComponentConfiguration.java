package com.solarwinds.msp.ncentral.eventproduction.producer;

import com.solarwinds.msp.ncentral.eventproduction.api.service.EventApiServiceComponentConfiguration;
import com.solarwinds.msp.ncentral.eventproduction.controller.EventControllerComponentConfiguration;
import com.solarwinds.msp.ncentral.eventproduction.converter.EventConverterComponentConfiguration;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Lazy;

/**
 * This class represents the Spring configuration for the MSP Event Producer module.
 */
@Configuration
@ComponentScan(basePackages = {"com.solarwinds.msp.ncentral.eventproduction.producer"}, lazyInit = true)
@Import({EventControllerComponentConfiguration.class, EventConverterComponentConfiguration.class,
        EventApiServiceComponentConfiguration.class})
@Lazy
public class EventProducerComponentConfiguration {}