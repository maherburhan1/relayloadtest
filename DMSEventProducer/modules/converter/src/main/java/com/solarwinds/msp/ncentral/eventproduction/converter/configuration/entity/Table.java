package com.solarwinds.msp.ncentral.eventproduction.converter.configuration.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Collections;
import java.util.Map;

/**
 * Configuration for N-central DB table columns to MSP EventBus protobuf entity field conversion - specific for a
 * table.
 */
public class Table {
    @JsonProperty(value = "joinToCustomer", required = true)
    private String joinToCustomer = "";

    @JsonProperty(value = "whereToCustomer", required = true)
    private String whereToCustomer = "";

    @JsonProperty(value = "incrementalLoadDate", required = true)
    private String incrementalLoadDate = "";

    @JsonProperty(value = "isTimeSeries", required = true)
    private boolean isTimeSeries = false;

    private Map<String, Field> fields;

    public String getJoinToCustomer() {
        return joinToCustomer;
    }

    public String getWhereToCustomer() {
        return whereToCustomer;
    }

    public String getIncrementalLoadDate() {
        return incrementalLoadDate;
    }

    public Boolean isTimeSeries() {
        return isTimeSeries;
    }

    public Map<String, Field> getFields() {
        if (fields == null) {
            return Collections.emptyMap();
        } else {
            return Collections.unmodifiableMap(fields);
        }
    }
}
