package com.solarwinds.msp.ncentral.eventproduction.converter.impl.entity;

import com.solarwinds.msp.ncentral.eventproduction.api.entity.Event;
import com.solarwinds.msp.ncentral.proto.entity.tasks.RemoteExecutionTaskOuterClass;

import java.util.Collections;
import java.util.List;

import static com.solarwinds.msp.ncentral.eventproduction.api.entity.Tools.getInteger;

/**
 * This class handles additional business logic for processing data for the {@link
 * RemoteExecutionTaskOuterClass.RemoteExecutionTask} data Protocol Buffers entity.
 */
class RemoteExecutionTaskEvent implements EntityParser<RemoteExecutionTaskOuterClass.RemoteExecutionTask> {

    private static final String ATTRIBUTE_LAST_RUN_RETURN_CODE = "last_run_return_code";
    private static final int RETURN_CODE_SUCCESSFUL = 0;

    @Override
    public List<RemoteExecutionTaskOuterClass.RemoteExecutionTask> parseRecord(Event event,
            RemoteExecutionTaskOuterClass.RemoteExecutionTask messageEntity) {
        final Integer returnCode = getInteger(event.getEntity(), ATTRIBUTE_LAST_RUN_RETURN_CODE);
        final boolean isSuccessfulTask = returnCode != null && returnCode == RETURN_CODE_SUCCESSFUL;
        return Collections.singletonList(messageEntity.toBuilder().setIsSuccessfulTask(isSuccessfulTask).build());
    }
}
