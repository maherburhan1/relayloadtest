package com.solarwinds.msp.ncentral.eventproduction.converter.impl.entity;

import com.solarwinds.msp.ncentral.eventproduction.api.entity.EventType;
import com.solarwinds.msp.ncentral.proto.entity.ActionOuterClass;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class ActionEventTest {
    @Test
    void verifyEventTypeEntityMapping() {
        Assertions.assertThat(ActionOuterClass.Action.ADD.ordinal())
                .as("Match Insert action ordinal")
                .isEqualTo(EventType.INSERT.ordinal());
        Assertions.assertThat(ActionOuterClass.Action.EDIT.ordinal())
                .as("Match Update action ordinal")
                .isEqualTo(EventType.UPDATE.ordinal());
        Assertions.assertThat(ActionOuterClass.Action.DELETE.ordinal())
                .as("Match Delete action ordinal")
                .isEqualTo(EventType.DELETE.ordinal());
    }
}