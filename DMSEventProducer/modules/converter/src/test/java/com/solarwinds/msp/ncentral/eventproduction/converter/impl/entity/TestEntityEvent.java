package com.solarwinds.msp.ncentral.eventproduction.converter.impl.entity;

import com.solarwinds.msp.ncentral.eventproduction.api.entity.Event;
import com.solarwinds.msp.ncentral.eventproduction.converter.testutility.TestEntity;

import java.util.Collections;
import java.util.List;

/**
 * This class represents the Test Entity Parser for unit testing.
 * <p>
 * <b>Important:</b> It uses the {@link TestEntity} class.
 *
 * @see TestEntity
 */
public class TestEntityEvent implements EntityParser<TestEntity> {

    public static final String MESSAGE_THROW_EXCEPTION = "Throw exception";
    public static final String MESSAGE_RETURN_NULL = "Return null";
    public static final String MESSAGE_TEMPLATE = TestEntityEvent.class.getSimpleName() + ": %s, %s";

    private final long creationTimeInNanoSeconds = System.nanoTime();

    public long getCreationTimeInNanoSeconds() {
        return creationTimeInNanoSeconds;
    }

    @Override
    public List<TestEntity> parseRecord(Event event, TestEntity messageEntity) {
        if (MESSAGE_THROW_EXCEPTION.equals(messageEntity.getMessage())) {
            throw new IllegalArgumentException(MESSAGE_THROW_EXCEPTION);
        }
        if (MESSAGE_RETURN_NULL.equals(messageEntity.getMessage())) {
            return null;
        }
        final TestEntity testEntity = new TestEntity();
        testEntity.setMessage(String.format(MESSAGE_TEMPLATE, event.toString(), messageEntity.getMessage()));
        testEntity.setTimeInNanoSeconds(creationTimeInNanoSeconds);
        return Collections.singletonList(testEntity);
    }
}
