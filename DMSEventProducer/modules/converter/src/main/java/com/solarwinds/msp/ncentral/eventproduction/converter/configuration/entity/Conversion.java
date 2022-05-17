package com.solarwinds.msp.ncentral.eventproduction.converter.configuration.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Configuration for N-central DB tables to MSP EventBus protobuf scheme conversion - for one entity.
 */
public class Conversion {
    private String entityPath;
    private Map<String, Table> tables;
    private Map<String, Field> fields;
    private List<String> childEntities;
    private String joinToCustomer;
    private String whereToCustomer;
    private String incrementalLoadDate;
    @JsonProperty(value = "isTimeSeries", required = true)
    private boolean isTimeSeries;

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

    public String getEntityPath() {
        return entityPath;
    }

    public Map<String, Table> getTables() {
        if (tables == null) {
            return Collections.emptyMap();
        } else {
            return Collections.unmodifiableMap(tables);
        }
    }

    public Map<String, Field> getFields() {
        if (fields == null) {
            return Collections.emptyMap();
        } else {
            return Collections.unmodifiableMap(fields);
        }
    }

    public List<String> getChildEntities() {
        if (childEntities == null) {
            return Collections.emptyList();
        } else {
            return Collections.unmodifiableList(childEntities);
        }
    }
}
