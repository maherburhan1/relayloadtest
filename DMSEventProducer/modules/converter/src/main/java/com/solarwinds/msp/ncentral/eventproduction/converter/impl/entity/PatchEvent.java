package com.solarwinds.msp.ncentral.eventproduction.converter.impl.entity;

import com.solarwinds.msp.ncentral.eventproduction.api.entity.Event;
import com.solarwinds.msp.ncentral.eventproduction.api.entity.Tools;
import com.solarwinds.msp.ncentral.proto.entity.entity.PatchCategoryOuterClass;
import com.solarwinds.msp.ncentral.proto.entity.entity.PatchOuterClass;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class handles additional business logic for processing data for the {@link PatchOuterClass.Patch} data Protocol
 * Buffers entity.
 */
class PatchEvent implements EntityParser<PatchOuterClass.Patch> {

    private static final String ATTRIBUTE_SEVERITY = "severity";
    private static final String ATTRIBUTE_CLASSIFICATION = "classification";
    private static final String SET_SOURCE_ID = "setSourceId";
    private static final String SET_SEVERITY = "setSeverity";
    private static final String SET_PATCH_CATEGORY = "setPatchCategory";

    @Override
    public List<PatchOuterClass.Patch> parseRecord(Event event, PatchOuterClass.Patch messageEntity) {
        final Map<String, String> entity = event.getEntity();

        final String severity = entity.get(ATTRIBUTE_SEVERITY);
        final String classification = entity.get(ATTRIBUTE_CLASSIFICATION);
        PatchOuterClass.Patch.Severity patchSeverity = PatchOuterClass.Patch.Severity.UNRECOGNIZED;
        if (severity != null) {
            patchSeverity = PatchOuterClass.Patch.Severity.valueOf(severity.toUpperCase());
        }

        final Map<String, Object> patchCategoryValues = new HashMap<>();
        patchCategoryValues.put(SET_SOURCE_ID, classification);

        final Map<String, Object> patchValues = new HashMap<>();
        patchValues.put(SET_SEVERITY, patchSeverity);
        patchValues.put(SET_PATCH_CATEGORY,
                Tools.setNullableField(PatchCategoryOuterClass.PatchCategory.newBuilder(), patchCategoryValues)
                        .build());

        return Collections.singletonList(Tools.setNullableField(messageEntity.toBuilder(), patchValues).build());
    }
}
