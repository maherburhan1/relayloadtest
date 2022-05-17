package com.solarwinds.msp.ncentral.eventproduction.converter.testutility;

import com.google.protobuf.GeneratedMessageV3;
import com.google.protobuf.Message;

import com.solarwinds.msp.ncentral.eventproduction.converter.impl.entity.TestEntityEvent;

/**
 * This class represents the Test {@link GeneratedMessageV3} for unit testing.
 * <p>
 * <b>Important:</b> It has to have the corresponding
 * {@link TestEntityEvent} class present in the {@link com.solarwinds.msp.ncentral.eventproduction.converter.impl.entity}
 * package.
 *
 * @see TestEntityEvent
 */
public class TestEntity extends GeneratedMessageV3 {

    private final long creationTimeInNanoSeconds;
    private String message;
    private long timeInNanoSeconds;

    public TestEntity() {
        this.creationTimeInNanoSeconds = System.nanoTime();
    }

    public long getCreationTimeInNanoSeconds() {
        return creationTimeInNanoSeconds;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public long getTimeInNanoSeconds() {
        return timeInNanoSeconds;
    }

    public void setTimeInNanoSeconds(long timeInNanoSeconds) {
        this.timeInNanoSeconds = timeInNanoSeconds;
    }

    @Override
    protected FieldAccessorTable internalGetFieldAccessorTable() {
        return null;
    }

    @Override
    protected Message.Builder newBuilderForType(BuilderParent builderParent) {
        return null;
    }

    @Override
    public Message.Builder newBuilderForType() {
        return null;
    }

    @Override
    public Message.Builder toBuilder() {
        return null;
    }

    @Override
    public Message getDefaultInstanceForType() {
        return null;
    }
}
