package com.solarwinds.msp.ncentral.eventproduction.converter.impl;

import com.solarwinds.msp.ncentral.eventproduction.api.entity.Tools;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Builds unique id based on key properties
 */
public class EventIdBuilder {

    private EventIdBuilder() {
    }

    public static String getEventId(List<Object> keyProperties) {
        List<String> valuesForKey = new ArrayList<>();
        for (Object property : keyProperties) {
            if (property != null) {
                String propertySimpleName = property.getClass().getName().toLowerCase();
                if (Tools.wrapperPrimitiveMap.containsValue(property.getClass())
                        || Tools.wrapperPrimitiveMap.containsKey(property.getClass())
                        || propertySimpleName.equalsIgnoreCase(
                        "java.lang.string")) {
                    valuesForKey.add(property.toString());
                }
            }
        }
        return DigestUtils.sha1Hex(StringUtils.join(valuesForKey, ""));
    }
}
