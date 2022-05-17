package com.solarwinds.msp.ncentral.eventproduction.converter.impl.entity;

import com.solarwinds.msp.ncentral.eventproduction.converter.TestCases;
import com.solarwinds.msp.ncentral.eventproduction.converter.impl.EventIdBuilder;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class EventIdBuilderTest {

    static Stream<Arguments> getEventIdTest() {
        TestCases.Builder<List<Object>, String> testCasesBuilder = TestCases.newBuilder();

        String expectedResult;
        String assertMessage;

        //<editor-fold desc="Some strings make an id.">
        assertMessage = "Id generates without error for list of strings.";
        List<Object> idRequest = new ArrayList<>(Arrays.asList("NAME-1", "PUBLISHER-1", "VERSION-1"));
        expectedResult = DigestUtils.sha1Hex(StringUtils.join(idRequest, ""));

        testCasesBuilder.addTestCase(idRequest, expectedResult, assertMessage);
        //</editor-fold>

        //<editor-fold desc="Some numbers make an id.">
        assertMessage = "Id generates without error for list of numbers.";
        idRequest = new ArrayList<>(Arrays.asList(1, 2.012, 12345L, 4));
        expectedResult = DigestUtils.sha1Hex(StringUtils.join(idRequest, ""));

        testCasesBuilder.addTestCase(idRequest, expectedResult, assertMessage);
        //</editor-fold>

        //<editor-fold desc="Combination of numbers and strings make an id.">
        assertMessage = "Id generates without error for list of strings and numbers.";
        idRequest = new ArrayList<>(Arrays.asList("NAME-1", 2.012, 12345L, 4));
        expectedResult = DigestUtils.sha1Hex(StringUtils.join(idRequest, ""));

        testCasesBuilder.addTestCase(idRequest, expectedResult, assertMessage);
        //</editor-fold>

        return testCasesBuilder.build().toArguments();
    }

    @ParameterizedTest(name = "Run {index}: {3}")
    @MethodSource
    void getEventIdTest(List<Object> idRequest, String expectedResult, String assertMessage) {
        assertThat(EventIdBuilder.getEventId(idRequest))
                .isEqualTo(expectedResult)
                .describedAs(assertMessage);
    }
}