package com.solarwinds.msp.ncentral.eventproduction.api.service.tracking;

import org.junit.jupiter.api.Test;

import java.util.OptionalInt;

import static org.assertj.core.api.Assertions.assertThat;

class EventingConfigurationChangeTest {

    @Test
    void setSendingChanged_returns_true() {
        final EventingConfigurationChange eventingConfigurationChange =
                new EventingConfigurationChange().setSendingConfigurationChanged();
        assertThat(eventingConfigurationChange.isSendingConfigurationChanged()).isTrue();
    }

    @Test
    void setRemoveBufferedEventsForCustomer_returns_nonempty_optional() {
        final EventingConfigurationChange eventingConfigurationChange =
                new EventingConfigurationChange().setRemoveEventsForCustomer(1);
        assertThat(eventingConfigurationChange.getRemoveEventsForCustomer().isPresent()).isTrue();
        assertThat(eventingConfigurationChange.getRemoveEventsForCustomer().getAsInt()).isEqualTo(1);
    }

    @Test
    void none_set_returns_false() {
        final EventingConfigurationChange eventingConfigurationChange = new EventingConfigurationChange();
        assertThat(eventingConfigurationChange.isSendingConfigurationChanged()).isFalse();
        assertThat(eventingConfigurationChange.getRemoveEventsForCustomer()).isEqualTo(OptionalInt.empty());
    }

}