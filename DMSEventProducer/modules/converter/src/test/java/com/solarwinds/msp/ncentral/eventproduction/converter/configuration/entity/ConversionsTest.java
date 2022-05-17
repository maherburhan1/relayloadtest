package com.solarwinds.msp.ncentral.eventproduction.converter.configuration.entity;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.solarwinds.msp.ncentral.eventproduction.converter.configuration.provider.EventConversionConfigurationComponentConfiguration;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.DefaultResourceLoader;

import java.io.IOException;

public class ConversionsTest {

    @Test
    void mergeConversions() throws IOException {
        String configurationStringToMerge = "conversions:\n"
                + "  \"TestEntity\":\n"
                + "    entityPath: \"com.solarwinds.msp.ncentral.proto.entity.entity.TestEntityOuterClass.TestEntity\"\n"
                + "    tables:\n"
                + "      \"test\":\n"
                + "        fields:\n"
                + "          \"field1\":\n"
                + "            targetField: \"TestField\"\n"
                + "            isMandatory: true";
        Conversions baseConfigurations = new EventConversionConfigurationComponentConfiguration().provideConversions(
                new DefaultResourceLoader());
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        Conversions conversionsToMerge = mapper.readValue(configurationStringToMerge, Conversions.class);
        baseConfigurations.mergeConversions(conversionsToMerge.getConversions());
        Assertions.assertTrue(baseConfigurations.getConversions().containsKey("TestEntity"));
    }
}