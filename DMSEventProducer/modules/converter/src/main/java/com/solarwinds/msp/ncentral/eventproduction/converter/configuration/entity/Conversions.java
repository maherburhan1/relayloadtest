package com.solarwinds.msp.ncentral.eventproduction.converter.configuration.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Configuration for N-central DB tables to MSP EventBus protobuf scheme conversion.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Conversions {
    private Map<String, Conversion> conversions;

    /**
     * Gets a {@link Map<>} of conversions that is then merged to the existing ones or added as new.
     *
     * @param incomingConversions {@link Map<>} of conversion objects to merge.
     */
    public void mergeConversions(Map<String, Conversion> incomingConversions) {
        if (conversions.isEmpty()) {
            conversions = incomingConversions;
        } else {
            Map<String, Conversion> mergedConversions = new HashMap<>(conversions);
            mergedConversions.putAll(incomingConversions);
            conversions = mergedConversions;
        }
    }

    /**
     * @return {@code Map<Protobuf_Entity_Name, Conversion_configuration>}
     */
    public Map<String, Conversion> getConversions() {
        if (conversions == null) {
            return Collections.emptyMap();
        } else {
            return Collections.unmodifiableMap(conversions);
        }
    }
}