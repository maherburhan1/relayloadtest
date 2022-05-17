package com.solarwinds.msp.ncentral.eventproduction.api.service.tracking;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ComponentStatusServiceTest {
    private final ComponentStatusService componentStatusService = new ComponentStatusService();

    @Test
    void setRunning_sets_state_and_getRunning_returns_it() {
        componentStatusService.setRunning(Integer.class, true);
        componentStatusService.setRunning(Long.class, false);
        assertThat(componentStatusService.isRunning(Integer.class)).isTrue();
        assertThat(componentStatusService.isRunning(Long.class)).isFalse();

        componentStatusService.setRunning(Integer.class, false);
        componentStatusService.setRunning(Long.class, true);
        assertThat(componentStatusService.isRunning(Integer.class)).isFalse();
        assertThat(componentStatusService.isRunning(Long.class)).isTrue();
    }

    @Test
    void getRunning_returns_false_by_default() {
        assertThat(componentStatusService.isRunning(Integer.class)).isFalse();
        assertThat(componentStatusService.isRunning(Long.class)).isFalse();
    }
}