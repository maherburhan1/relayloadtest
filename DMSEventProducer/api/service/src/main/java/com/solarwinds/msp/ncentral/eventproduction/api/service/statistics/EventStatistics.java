package com.solarwinds.msp.ncentral.eventproduction.api.service.statistics;

import com.solarwinds.msp.ncentral.eventproduction.api.entity.EventStatistic;

/**
 * Provides functionality to track event statistics.
 */
public interface EventStatistics {

    /**
     * Records a statistic for an event.
     *
     * @param eventStatistic {@link EventStatistic} various statistics for an event.
     */
    void addStatistic(EventStatistic eventStatistic);
}
