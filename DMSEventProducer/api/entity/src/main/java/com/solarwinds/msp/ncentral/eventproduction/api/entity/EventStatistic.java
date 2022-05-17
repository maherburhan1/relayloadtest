package com.solarwinds.msp.ncentral.eventproduction.api.entity;

import java.util.Objects;

/**
 * Entity representing event statistic object data.
 */
public class EventStatistic {
    public enum StatisticType {
        BUFFER_INFORMATION("Buffer Information"),
        ERROR_HANDLING("Error Handling"),
        PROCESSING_DATA("Processing Data");

        public final String label;

        StatisticType(String label) {
            this.label = label;
        }
    }

    public enum StatisticSubType {
        MALFORMED_DATA_ERROR("Malformed Data Error"),
        CONVERTER_FATAL_ERROR("Converter Fatal Error"),
        CONTROLLER_FATAL_ERROR("Controller Fatal Error"),
        RELAY_FATAL_ERROR("Relay Fatal Error"),
        IGNORED_INCOMPLETE_DATA("Ignored Incomplete Data"),
        IGNORED_NO_RELEVANT_CHANGES("Ignored No Relevant Changes"),
        SUCCESSFULLY_PARSED("Successfully Parsed"),
        SUCCESSFULLY_SENT_TO_RELAY("Batches Successfully Sent To Relay"),
        RELAY_ACKNOWLEDGED("Relay Acknowledged"),
        SCRAPED_RECORD("Scraped Record"),
        SCRAPE_INITIATED("Scrape Initiated"),
        MEMORY_UTILIZED("Memory Utilized"),
        DISK_UTILIZED("Disk Utilized"),
        IN_MEMORY_QUEUE("In Memory Queue"),
        IN_DISK_QUEUE("In Disk Queue"),
        TOTAL_QUEUE("Total Queue"),
        MEMORY_BUFFER_FULL_EVENT("Memory Buffer Full Event"),
        DISK_BUFFER_FULL_EVENT("Disk Buffer Full Event"),
        EVENT_BYTE_SIZE("Event Byte Size"),
        PUBLISH_RETRY("Publish Retry");

        public final String label;

        StatisticSubType(String label) {
            this.label = label;

        }
    }

    private StatisticType statisticType;
    private StatisticSubType statisticSubType;
    private long statisticValue;
    private boolean clearStatistic;

    private long startTimeNanoSeconds;
    private long startTimeMilliSeconds;

    private EventStatistic() {}

    public long getStatisticValue() {
        return statisticValue;
    }

    public StatisticType getStatisticType() {
        return statisticType;
    }

    public StatisticSubType getStatisticSubType() {
        return statisticSubType;
    }

    public boolean shouldClearStatistic() {
        return clearStatistic;
    }

    public long getStartTimeNanoSeconds() {
        return startTimeNanoSeconds;
    }

    public long getStartTimeMilliSeconds() {
        return startTimeMilliSeconds;
    }

    /**
     * @return {@link EventStatBuilder} builder for creating {@link EventStatistic} instances.
     */
    public static EventStatBuilder builder() {
        return new EventStatBuilder();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        EventStatistic eventStatistic = (EventStatistic) o;
        return statisticType == eventStatistic.statisticType && statisticSubType == eventStatistic.statisticSubType
                && statisticValue == eventStatistic.statisticValue && clearStatistic == eventStatistic.clearStatistic
                && startTimeNanoSeconds == eventStatistic.startTimeNanoSeconds
                && startTimeMilliSeconds == eventStatistic.startTimeMilliSeconds;
    }

    @Override
    public int hashCode() {
        return Objects.hash(statisticType, statisticSubType, statisticValue);
    }

    @Override
    public String toString() {
        return "EventStatistic{" + "statisticType='" + statisticType + '\'' + ", statisticSubType='" + statisticSubType
                + '\'' + ", statisticValue=" + statisticValue + '}';
    }

    /**
     * Builder class for {@link EventStatistic} instances.
     */
    public static final class EventStatBuilder {
        private StatisticType statisticType;
        private StatisticSubType statisticSubType;
        private long statisticValue;
        private boolean clearStatistic;

        private long startTimeNanoSeconds;
        private long startTimeMilliSeconds;

        private EventStatBuilder() {}

        public EventStatBuilder statisticType(StatisticType statisticType) {
            this.statisticType = statisticType;
            return this;
        }

        public EventStatBuilder statisticSubType(StatisticSubType statisticSubType) {
            this.statisticSubType = statisticSubType;
            return this;
        }

        public EventStatBuilder statisticValue(long statisticValue) {
            this.statisticValue = statisticValue;
            return this;
        }

        public EventStatBuilder clearStatistic(boolean clearStatistic) {
            this.clearStatistic = clearStatistic;
            return this;
        }

        public EventStatBuilder startTimeNanoSeconds(long startTimeNanoSeconds) {
            this.startTimeNanoSeconds = startTimeNanoSeconds;
            return this;
        }

        public EventStatBuilder startTimeMilliSeconds(long startTimeMilliSeconds) {
            this.startTimeMilliSeconds = startTimeMilliSeconds;
            return this;
        }

        public EventStatistic build() {
            EventStatistic eventStatistic = new EventStatistic();
            eventStatistic.statisticType = Objects.requireNonNull(statisticType, "statisticType cannot be null");
            eventStatistic.statisticSubType =
                    Objects.requireNonNull(statisticSubType, "statisticSubType cannot be null");
            eventStatistic.statisticValue = statisticValue;
            eventStatistic.clearStatistic = clearStatistic;
            eventStatistic.startTimeNanoSeconds = startTimeNanoSeconds;
            eventStatistic.startTimeMilliSeconds = startTimeMilliSeconds;
            return eventStatistic;
        }
    }
}
