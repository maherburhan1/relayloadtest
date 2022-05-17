package com.solarwinds.msp.ncentral.eventproduction.converter.configuration.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Configuration for N-central DB table column to MSP EventBus protobuf entity field conversion.
 */
public class Field {
    private String targetField;

    @JsonProperty(
            value = "isMandatory",
            required = true)
    private boolean isMandatory = false;

    @JsonProperty(
            value = "isTracked",
            required = true)
    private boolean isTracked = true;

    public String getTargetField() {
        return targetField;
    }

    public boolean isMandatory() {
        return isMandatory;
    }

    public boolean isTracked() {
        return isTracked;
    }
}
