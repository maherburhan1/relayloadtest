package com.solarwinds.msp.ncentral.eventproduction.api.publisher;

import com.solarwinds.msp.ncentral.eventproduction.api.service.publisher.PublishingContext;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

public class PublishingContextTest {
    @Test
    void build_returns_correct_PublishingContext_instance() {
        PublishingContext publishingContext = PublishingContext.builder()
                .withBizappsCustomerId("BIZ_APPS_ID_1")
                .withSystemGuid("SYSTEM_GUID_2")
                .withEntityType("ENTITY_TYPE")
                .withEventingConfigurationCustomerId(1)
                .build();

        assertThat(publishingContext.getBizappsCustomerId()).isEqualTo(Optional.of("BIZ_APPS_ID_1"));
        assertThat(publishingContext.getSystemGuid()).isEqualTo("SYSTEM_GUID_2");
        assertThat(publishingContext.getEntityType()).isEqualTo("ENTITY_TYPE");
        assertThat(publishingContext.getEventingConfigurationCustomerId()).isEqualTo(1);
    }

    @Test
    void getBizappsCustomerId_returns_emptyOptional_when_bizappsCustomerId_is_missing() {
        PublishingContext publishingContext = PublishingContext.builder()
                .withSystemGuid("SYSTEM_GUID_2")
                .withEntityType("ENTITY_TYPE")
                .withEventingConfigurationCustomerId(1)
                .build();

        assertThat(publishingContext.getBizappsCustomerId()).isEmpty();
    }

    @Test
    void build_fails_when_systemGuid_is_missing() {
        assertThatNullPointerException().isThrownBy(() -> PublishingContext.builder().build());
    }

    @Test
    void build_fails_when_eventingConfigurationCustomerId_is_missing() {
        final PublishingContext.Builder builder = PublishingContext.builder();
        builder.withSystemGuid("SYSTEM_GUID");
        assertThatNullPointerException().isThrownBy(builder::build);
    }
}