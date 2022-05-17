package com.solarwinds.msp.ncentral.eventproduction.api.service;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

/**
 * This class represents the Spring configuration for the Event API Service module.
 */
@Configuration
@ComponentScan(basePackages = {"com.solarwinds.msp.ncentral.eventproduction.api.service"}, lazyInit = true)
@Lazy
public class EventApiServiceComponentConfiguration {}
