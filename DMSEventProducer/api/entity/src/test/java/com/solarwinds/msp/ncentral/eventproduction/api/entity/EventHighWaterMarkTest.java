package com.solarwinds.msp.ncentral.eventproduction.api.entity;

import org.junit.jupiter.api.Test;

import java.time.ZoneId;
import java.time.ZonedDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

class EventHighWaterMarkTest {

    private static final String TABLE_NAME = "TEST_TABLE";
    private static final int TABLE_EXPORT_ID = 1;
    private static final int TABLE_TRACKING_ID = 2;
    private static final int CUSTOMER_ID = 1;
    private static final ZonedDateTime NOW = ZonedDateTime.now(ZoneId.of("UTC"));
    private static final String ENTITY_NAME = "TEST_ENTITY";

    @Test
    void build_returns_correct_EventHighWaterMark_instance() {
        EventHighWaterMark eventHighWaterMark = getHighWaterMark();

        assertThat(eventHighWaterMark.getTableName()).isEqualTo(TABLE_NAME);
        assertThat(eventHighWaterMark.getCustomerId()).isEqualTo(CUSTOMER_ID);
        assertThat(eventHighWaterMark.getEntityName()).isEqualTo(ENTITY_NAME);
        assertThat(eventHighWaterMark.getLastProcessed()).isEqualTo(NOW);
    }

    @Test
    void build_fails_when_table_name_is_missing() {
        assertThatNullPointerException().isThrownBy(() -> EventHighWaterMark.builder().build());
    }

    private EventHighWaterMark getHighWaterMark() {
        return EventHighWaterMark.builder()
                .tableName(TABLE_NAME)
                .customerId(CUSTOMER_ID)
                .entityName(ENTITY_NAME)
                .lastProcessed(NOW)
                .build();
    }
}

