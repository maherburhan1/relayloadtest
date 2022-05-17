package com.solarwinds.msp.ncentral.eventproduction.converter.impl.entity;

import com.google.protobuf.GeneratedMessageV3;

import com.solarwinds.msp.ncentral.eventproduction.api.entity.Event;
import com.solarwinds.msp.ncentral.eventproduction.api.service.statistics.EventStatistics;
import com.solarwinds.msp.ncentral.eventproduction.converter.EventParser;
import com.solarwinds.msp.ncentral.eventproduction.converter.TestCases;
import com.solarwinds.msp.ncentral.eventproduction.converter.configuration.impl.EventConversionConfigurationServiceImplTest;
import com.solarwinds.msp.ncentral.eventproduction.converter.configuration.provider.EventConversionConfigurationComponentConfiguration;
import com.solarwinds.msp.ncentral.eventproduction.converter.impl.ConfigurationBasedEventParser;
import com.solarwinds.msp.ncentral.eventproduction.converter.impl.EntityParserServiceImpl;

import org.junit.jupiter.params.provider.Arguments;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import static org.mockito.Mockito.mock;

class EventParserTestCases {

    private final TestCases.Builder<Event, List<? extends GeneratedMessageV3>> testCasesBuilder =
            TestCases.newBuilder();

    private static EventConversionConfigurationComponentConfiguration
            eventConversionConfigurationComponentConfiguration =
            new EventConversionConfigurationComponentConfiguration();

    private static EventParser<GeneratedMessageV3> eventParser =
            new ConfigurationBasedEventParser(new EntityParserServiceImpl(),
                    EventConversionConfigurationServiceImplTest.getEventConfigurationService(),
                    mock(EventStatistics.class));

    public void setTestCase(Event incomingEvent, String parsingAssertMessage) {
        setTestCase(incomingEvent, Collections.emptyList(), parsingAssertMessage);
    }

    public <T extends GeneratedMessageV3> void setTestCase(Event incomingEvent, T expectedResult,
            String parsingAssertMessage) {
        setTestCase(incomingEvent, Collections.singletonList(Objects.requireNonNull(expectedResult)),
                parsingAssertMessage);
    }

    public <T extends GeneratedMessageV3> void setTestCase(Event incomingEvent, List<T> expectedResult,
            String parsingAssertMessage) {
        testCasesBuilder.addTestCase(incomingEvent, expectedResult, parsingAssertMessage);
    }

    public Stream<Arguments> toArguments() {
        return testCasesBuilder.build()
                .getTestCases()
                .stream()
                .map(tc -> Arguments.of(eventParser, tc.v1(), tc.v2(), tc.v3()));
    }
}
