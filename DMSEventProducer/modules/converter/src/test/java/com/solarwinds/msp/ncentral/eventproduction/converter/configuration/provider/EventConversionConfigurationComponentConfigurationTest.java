package com.solarwinds.msp.ncentral.eventproduction.converter.configuration.provider;

import com.solarwinds.msp.ncentral.eventproduction.converter.configuration.entity.Conversions;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;
import org.springframework.util.ResourceUtils;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

public class EventConversionConfigurationComponentConfigurationTest {

    @Test
    public void provideConversions() throws IOException {
        assertThat(Collections.singletonList(
                new EventConversionConfigurationComponentConfiguration().provideConversions(
                        new DefaultResourceLoader())))
                .isNotNull();
    }

    @Test
    public void provideConversionsAddFile() throws IOException {

        String configurationStringToMerge = "conversions:\n"
                + "  \"TestEntity\":\n"
                + "    entityPath: \"com.solarwinds.msp.ncentral.proto.entity.entity.TestEntityOuterClass.TestEntity\"\n"
                + "    tables:\n"
                + "      \"test\":\n"
                + "        fields:\n"
                + "          \"field1\":\n"
                + "            targetField: \"TestField\"\n"
                + "            isMandatory: true";
        String fileName =
                ResourceUtils.getURL(ResourceLoader.CLASSPATH_URL_PREFIX + "event-conversion-configuration-base.yaml")
                        .getPath().replace("-base", "-test");
        try (FileOutputStream outputStream = new FileOutputStream(fileName)) {
            byte[] strToBytes = configurationStringToMerge.getBytes();
            outputStream.write(strToBytes);
        }

        Conversions updatedConversions = new EventConversionConfigurationComponentConfiguration().provideConversions(
                new DefaultResourceLoader());

        FileUtils.deleteQuietly(FileUtils.getFile(fileName));

        Assertions.assertTrue(updatedConversions.getConversions().containsKey("TestEntity"));
    }
}