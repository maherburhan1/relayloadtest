package com.solarwinds.msp.ncentral.eventproduction.converter.impl;

import com.google.protobuf.GeneratedMessageV3;

import com.solarwinds.msp.ncentral.eventproduction.api.entity.Event;
import com.solarwinds.msp.ncentral.eventproduction.converter.impl.entity.TestEntityEvent;
import com.solarwinds.msp.ncentral.eventproduction.converter.testutility.TestEntity;
import com.solarwinds.msp.ncentral.eventproduction.converter.testutility.TestInvalidEntity;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * This class represents the unit test of the {@link EntityParserServiceImpl} class.
 */
@ExtendWith(MockitoExtension.class)
class EntityParserServiceImplTest {

    private static final String INPUT_EVENT = "Input event";
    private static final String INPUT_MESSAGE = "Input message";

    private static final String VALID_CLASS = "Valid Class";
    private static final String INVALID_CLASS = "Invalid Class";

    private static final String VALID_CLASS_NAME = TestEntity.class.getName();
    private static final String INVALID_CLASS_NAME = "Invalid Class Name";

    private static final Logger logger = LoggerFactory.getLogger(EntityParserServiceImplTest.class);

    @Mock
    private Event event;

    @InjectMocks
    private EntityParserServiceImpl<GeneratedMessageV3> service;

    // ----------------------------------------------------------------------------------------------------------------
    // Tests of the getNewInstance method
    // ----------------------------------------------------------------------------------------------------------------

    @Test
    void getNewInstance() {
        getNewInstanceOfValidClassName();
    }

    @Test
    void getNewInstanceNull() {
        assertThat(service.getNewInstance(null)).isNotPresent();
    }

    @Test
    void getNewInstanceBlankClassName() {
        assertThat(service.getNewInstance("")).isNotPresent();
    }

    @Test
    void getNewInstanceInvalidClass() {
        assertThat(service.getNewInstance(INVALID_CLASS_NAME)).isNotPresent();
    }

    @Test
    void getNewInstanceRepeatWithValidClass() {
        long lastCreationTime = System.nanoTime();
        for (int i = 0; i < 5; i++) {
            final long startTime = System.nanoTime();
            final TestEntity result = getNewInstanceOfValidClassName();
            final long endTime = System.nanoTime();
            logDuration(VALID_CLASS_NAME, startTime, endTime, i);

            assertThat(result.getCreationTimeInNanoSeconds()).isBetween(startTime, endTime);
            assertThat(result.getCreationTimeInNanoSeconds()).isGreaterThanOrEqualTo(lastCreationTime);
            lastCreationTime = result.getCreationTimeInNanoSeconds();
        }
    }

    @Test
    void getNewInstanceRepeatWithInvalidClass() {
        for (int i = 0; i < 5; i++) {
            final long startTime = System.nanoTime();
            getNewInstanceInvalidClass();
            final long endTime = System.nanoTime();
            if (i == 0) {
                logDurationFirstRun(INVALID_CLASS_NAME, startTime, endTime);
            } else {
                logDuration(INVALID_CLASS_NAME, startTime, endTime, i);
            }
        }
    }

    // ----------------------------------------------------------------------------------------------------------------
    // Tests of the parseRecord method
    // ----------------------------------------------------------------------------------------------------------------

    @Test
    void parseRecord() {
        when(event.toString()).thenReturn(INPUT_EVENT);
        getParsedRecord();
    }

    @Test
    void parseRecordMessageEntityNull() {
        assertThrows(NullPointerException.class, () -> service.parseRecord(event, null));
        verifyNoInteractions(event);
    }

    @Test
    void parseRecordMessageEntityThrowsException() {
        final TestEntity entity = new TestEntity();
        entity.setMessage(TestEntityEvent.MESSAGE_THROW_EXCEPTION);
        assertThat(service.parseRecord(event, entity)).isNotPresent();

        verifyNoInteractions(event);
    }

    @Test
    void parseRecordBlankClassMessageEntity() {
        final GeneratedMessageV3 entity = new TestEntity() {};
        assertThat(service.parseRecord(event, entity)).isNotPresent();

        verifyNoInteractions(event);
    }

    @Test
    void parseRecordInvalidClassMessageEntity() {
        final TestInvalidEntity entity = new TestInvalidEntity();
        assertThat(service.parseRecord(event, entity)).isNotPresent();

        verifyNoInteractions(event);
    }

    @Test
    void parseRecordMessageEntityIsNotAllowedEvent() {
        final TestEntity entity = new TestEntity();
        entity.setMessage(TestEntityEvent.MESSAGE_RETURN_NULL);
        assertThat(service.parseRecord(event, entity)).isEmpty();
    }

    @Test
    void parseRecordRepeatWithValidClass() {
        when(event.toString()).thenReturn(INPUT_EVENT);
        final long firstStartTime = System.nanoTime();
        getParsedRecord();
        final long firstEndTime = System.nanoTime();
        logDurationFirstRun(VALID_CLASS, firstStartTime, firstEndTime);

        for (int i = 0; i < 5; i++) {
            final long startTime = System.nanoTime();
            final TestEntity result = getParsedRecord();
            final long endTime = System.nanoTime();
            logDuration(VALID_CLASS, startTime, endTime, i);

            assertThat(result.getTimeInNanoSeconds()).isBetween(firstStartTime, firstEndTime);
        }
    }

    @Test
    void parseRecordRepeatWithInvalidClass() {
        for (int i = 0; i < 6; i++) {
            final TestInvalidEntity entity = new TestInvalidEntity();
            final long startTime = System.nanoTime();
            final Optional<List<GeneratedMessageV3>> results = service.parseRecord(event, entity);
            final long endTime = System.nanoTime();
            if (i == 0) {
                logDurationFirstRun(INVALID_CLASS, startTime, endTime);
            } else {
                logDuration(INVALID_CLASS, startTime, endTime, i);
            }

            assertThat(results).isNotPresent();
        }
        verifyNoInteractions(event);
    }

    // ----------------------------------------------------------------------------------------------------------------
    // Private methods
    // ----------------------------------------------------------------------------------------------------------------

    private TestEntity getNewInstanceOfValidClassName() {
        final GeneratedMessageV3 result = service.getNewInstance(VALID_CLASS_NAME).orElse(null);
        assertThat(result).isNotNull();
        assertThat(result).isInstanceOf(TestEntity.class);
        return (TestEntity) result;
    }

    private TestEntity getParsedRecord() {
        final String expectedMessage = String.format(TestEntityEvent.MESSAGE_TEMPLATE, INPUT_EVENT, INPUT_MESSAGE);
        final TestEntity expectedEntity = new TestEntity();
        expectedEntity.setMessage(expectedMessage);

        final TestEntity entity = new TestEntity();
        entity.setMessage(INPUT_MESSAGE);
        final List<GeneratedMessageV3> results = service.parseRecord(event, entity).orElse(Collections.emptyList());

        assertThat(results).hasSize(1);
        final TestEntity result = ((TestEntity) results.get(0));
        assertThat(result.getMessage()).isEqualTo(expectedMessage);

        return result;
    }

    private void logDurationFirstRun(String logHeader, long startTime, long endTime) {
        final long duration = endTime - startTime;
        logger.info("{}: First run - instantiate class ... duration = {} ns.", logHeader, duration);
    }

    private void logDuration(String logHeader, long startTime, long endTime, int runNumber) {
        final long duration = endTime - startTime;
        logger.info("{}: Using cached object ... run = {}, duration = {} ns.", logHeader, runNumber, duration);
    }
}