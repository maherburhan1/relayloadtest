package com.solarwinds.msp.ncentral.eventproduction.converter.configuration.provider;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.solarwinds.msp.ncentral.eventproduction.converter.configuration.entity.Conversions;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/**
 * This class provides Spring configuration for the N-central DB tables to MSP EventBus protobuf scheme conversion.
 */
@Configuration
@Lazy
public class EventConversionConfigurationComponentConfiguration {
    private static final String CONVERSIONS_FILE_LOCATION_PATTERN =
            ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX + "event-conversion-configuration-*.yaml";

    @Bean
    public Conversions provideConversions(ResourceLoader resourceLoader) throws IOException {
        Conversions conversionsToMerge = null;
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        Resource[] conversionResources = ResourcePatternUtils.getResourcePatternResolver(resourceLoader)
                .getResources(CONVERSIONS_FILE_LOCATION_PATTERN);
        for (Resource resource : conversionResources) {
            try (InputStream inputStream = resource.getInputStream(); BufferedReader reader = new BufferedReader(
                    new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
                if (conversionsToMerge == null) {
                    conversionsToMerge = mapper.readValue(reader, Conversions.class);
                } else {
                    conversionsToMerge.mergeConversions(mapper.readValue(reader, Conversions.class).getConversions());
                }
            }
        }
        return conversionsToMerge;
    }
}