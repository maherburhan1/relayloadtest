package com.solarwinds.msp.ncentral.eventproduction.converter.impl.entity;

import com.solarwinds.msp.ncentral.eventproduction.api.entity.Event;
import com.solarwinds.msp.ncentral.eventproduction.api.entity.Tools;
import com.solarwinds.msp.ncentral.proto.entity.tasks.UtilizationOuterClass;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class handles additional business logic for processing data for the {@link UtilizationOuterClass.Utilization}
 * data Protocol Buffers entity.
 */
class UtilizationEvent implements EntityParser<UtilizationOuterClass.Utilization> {

    private static final String ENTITY_TYPE_DATA_CPU = "datacpu_detailed";
    private static final String ENTITY_TYPE_DATA_CPU_CISCO = "datacpucisco_detailed";
    private static final String ENTITY_TYPE_DATA_DISK = "datadisk_detailed";
    private static final String ENTITY_TYPE_DATA_VM_DATA_STORE = "datavmdatastore_detailed";
    private static final String ENTITY_TYPE_DATA_MEMORY = "datamemory_detailed";

    private static final String ATTRIBUTE_MEMORY_VIRTUAL_FREE = "memory_virtualfree";
    private static final String ATTRIBUTE_MEMORY_VIRTUAL_TOTAL = "memory_virtualtotal";
    private static final String ATTRIBUTE_MEMORY_VIRTUAL_USED = "memory_virtualused";
    private static final String ATTRIBUTE_MEMORY_VIRTUAL_USAGE = "memory_virtualusage";

    private static final String UNIT_TYPE_PERCENT = "Percent";
    private static final String UNIT_TYPE_KILO_BYTES = "KB";
    private static final float ONE_HUNDRED = 100F;

    private static final String SET_UTILIZATION_TYPE = "setUtilizationType";
    private static final String SET_UNIT_TYPE = "setUnitType";
    private static final String SET_UNITS_AVAILABLE = "setUnitsAvailable";
    private static final String SET_UNITS_TOTAL = "setUnitsTotal";
    private static final String SET_UNITS_CONSUMED = "setUnitsConsumed";
    private static final String SET_USAGE_PERCENT = "setUsagePercent";

    @Override
    public List<UtilizationOuterClass.Utilization> parseRecord(Event event,
            UtilizationOuterClass.Utilization messageEntity) {
        final List<UtilizationOuterClass.Utilization> results = new ArrayList<>();
        addMemoryData(event, messageEntity, results);
        addVirtualMemoryData(event, messageEntity, results);
        return results;
    }

    private void addMemoryData(Event event, UtilizationOuterClass.Utilization messageEntity,
            List<UtilizationOuterClass.Utilization> results) {
        UtilizationOuterClass.Utilization.UtilizationType utilizationType =
                UtilizationOuterClass.Utilization.UtilizationType.UNRECOGNIZED;
        String unitType = null;
        Float unitsAvailable = null;
        Float unitsTotal = null;
        Float unitsConsumed = null;

        switch (event.getEntityType()) {
            case ENTITY_TYPE_DATA_CPU:
            case ENTITY_TYPE_DATA_CPU_CISCO:
                utilizationType = UtilizationOuterClass.Utilization.UtilizationType.CPU;
                unitType = UNIT_TYPE_PERCENT;
                unitsAvailable = ONE_HUNDRED - messageEntity.getUsagePercent();
                unitsTotal = ONE_HUNDRED;
                unitsConsumed = messageEntity.getUsagePercent();
                break;
            case ENTITY_TYPE_DATA_DISK:
            case ENTITY_TYPE_DATA_VM_DATA_STORE:
                utilizationType = UtilizationOuterClass.Utilization.UtilizationType.DISK;
                unitType = UNIT_TYPE_KILO_BYTES;
                break;
            case ENTITY_TYPE_DATA_MEMORY:
                utilizationType = UtilizationOuterClass.Utilization.UtilizationType.PHYSICAL_MEMORY;
                unitType = UNIT_TYPE_KILO_BYTES;
                break;
            default:
        }

        final Map<String, Object> entityValues =
                createValues(utilizationType, unitType, unitsAvailable, unitsTotal, unitsConsumed);

        results.add(Tools.setNullableField(messageEntity.toBuilder(), entityValues).build());
    }

    private void addVirtualMemoryData(Event event, UtilizationOuterClass.Utilization messageEntity,
            List<UtilizationOuterClass.Utilization> results) {
        if (event.getEntityType().equalsIgnoreCase(ENTITY_TYPE_DATA_MEMORY)) {
            final float virtualMemoryFree = Tools.getFloat(event.getEntity(), ATTRIBUTE_MEMORY_VIRTUAL_FREE);
            final float virtualMemoryTotal = Tools.getFloat(event.getEntity(), ATTRIBUTE_MEMORY_VIRTUAL_TOTAL);
            final float virtualMemoryUsed = Tools.getFloat(event.getEntity(), ATTRIBUTE_MEMORY_VIRTUAL_USED);
            final float virtualMemoryUsage = Tools.getFloat(event.getEntity(), ATTRIBUTE_MEMORY_VIRTUAL_USAGE);

            final Map<String, Object> entityValues =
                    createValues(UtilizationOuterClass.Utilization.UtilizationType.VIRTUAL_MEMORY, UNIT_TYPE_KILO_BYTES,
                            virtualMemoryFree, virtualMemoryTotal, virtualMemoryUsed);
            entityValues.put(SET_USAGE_PERCENT, virtualMemoryUsage);

            results.add(Tools.setNullableField(messageEntity.toBuilder(), entityValues).build());
        }
    }

    private Map<String, Object> createValues(UtilizationOuterClass.Utilization.UtilizationType utilizationType,
            String unitType, Float unitsAvailable, Float unitsTotal, Float unitsConsumed) {
        final Map<String, Object> values = new HashMap<>();
        values.put(SET_UTILIZATION_TYPE, utilizationType);
        values.put(SET_UNIT_TYPE, unitType);
        values.put(SET_UNITS_AVAILABLE, unitsAvailable);
        values.put(SET_UNITS_TOTAL, unitsTotal);
        values.put(SET_UNITS_CONSUMED, unitsConsumed);
        return values;
    }
}
