package com.solarwinds.msp.ncentral.eventproduction.api.service.tracking;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class ScrapingConfigurationChangeTest {

    private static final Integer CUSTOMER_ID = 1;

    @Test
    void set_restart_sets_stop_start() {
        final ScrapingConfigurationChange change =
                ScrapingConfigurationChange.builder().withCustomerId(CUSTOMER_ID).restartScraping().build();
        assertThat(change.isStopScraping()).isTrue();
        assertThat(change.isStartScraping()).isTrue();
    }

    @Test
    void start_without_stop_invalid_state() {
        assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(
                () -> ScrapingConfigurationChange.builder().withCustomerId(CUSTOMER_ID).startScraping().build());
    }

    @Test
    void test_builder_fills_correct_data() {
        final ScrapingConfigurationChange change = ScrapingConfigurationChange.builder()
                .withCustomerId(CUSTOMER_ID)
                .startScraping()
                .stopScraping()
                .build();
        assertThat(change.isStopScraping()).isTrue();
        assertThat(change.isStartScraping()).isTrue();
        assertThat(change.getCustomerId()).isEqualTo(CUSTOMER_ID);
    }

}