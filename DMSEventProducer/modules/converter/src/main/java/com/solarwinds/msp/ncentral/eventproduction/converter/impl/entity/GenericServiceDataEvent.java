package com.solarwinds.msp.ncentral.eventproduction.converter.impl.entity;

import com.google.protobuf.Timestamp;

import com.nable.util.StringUtils;
import com.solarwinds.msp.ncentral.eventproduction.api.entity.Event;
import com.solarwinds.msp.ncentral.eventproduction.api.entity.Tools;
import com.solarwinds.msp.ncentral.proto.entity.tasks.GenericServiceDataOuterClass;
import com.solarwinds.util.time.ZonedDateTimeParser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * This class handles additional business logic for processing data for the {@link
 * GenericServiceDataOuterClass.GenericServiceData} data Protocol Buffers entity.
 */
class GenericServiceDataEvent implements EntityParser<GenericServiceDataOuterClass.GenericServiceData> {

    private static final String SET_SOURCE_NAME = "setSourceName";
    private static final String ADD_ALL_GENERIC_FIELD = "addAllGenericField";

    @Override
    public List<GenericServiceDataOuterClass.GenericServiceData> parseRecord(Event event,
            GenericServiceDataOuterClass.GenericServiceData messageEntity) {
        final List<GenericServiceDataOuterClass.GenericServiceData.genericTypedData> allGenericData =
                getGenericData(event);

        final Map<String, Object> genericServiceDataValues = new HashMap<>();
        genericServiceDataValues.put(SET_SOURCE_NAME, event.getEntityType());
        genericServiceDataValues.put(ADD_ALL_GENERIC_FIELD, allGenericData);

        return Collections.singletonList(
                Tools.setNullableField(messageEntity.toBuilder(), genericServiceDataValues).build());
    }

    private static List<GenericServiceDataOuterClass.GenericServiceData.genericTypedData> getGenericData(Event event) {

        List<GenericServiceDataOuterClass.GenericServiceData.genericTypedData> allGenericData = new ArrayList<>();

        Set<String> ignoreFields =
                new HashSet<>(Arrays.asList("taskid", "lastupdated", "scantime", "datadelay", "errormessage", "state"));

        Map<String, String> entityDataTypes = new HashMap<>(event.getEntityDataTypes());

        entityDataTypes.keySet().removeAll(ignoreFields);
        Map<String, String> entity = event.getEntity();

        for (Map.Entry<String, String> datatype : entityDataTypes.entrySet()) {
            String fieldName = StringUtils.valueOfOrNull(datatype.getKey());
            String fieldType = "String";
            String stringValue = null;
            Boolean booleanValue = null;
            Double doubleValue = null;
            Float floatValue = null;
            Integer integerValue = null;
            Long longValue = null;
            Timestamp timestampValue = null;

            switch (datatype.getValue().toLowerCase()) {
                case "bit":
                    booleanValue = Tools.getBoolean(entity, fieldName);
                    fieldType = "Boolean";
                    break;
                case "tinyint":
                case "smallint":
                case "integer":
                    integerValue = Tools.getInteger(entity, fieldName);
                    fieldType = "Integer";
                    break;
                case "bigint":
                    longValue = Tools.getLong(entity, fieldName);
                    fieldType = "Long";
                    break;
                case "decimal":
                case "real":
                case "float":
                    floatValue = Tools.getFloat(entity, fieldName);
                    fieldType = "Float";
                    break;
                case "double":
                    doubleValue = Tools.getDouble(entity, fieldName);
                    fieldType = "Double";
                    break;
                case "date":
                case "timestamp":
                    timestampValue = Tools.toTimestamp(ZonedDateTimeParser.parseDateTime(entity, fieldName));
                    fieldType = "Timestamp";
                    break;
                default:
                    stringValue = entity.getOrDefault(fieldName, null);
                    fieldType = "String";
            }

            Map<String, Object> genericData = new HashMap<>();
            genericData.put("setFieldName", fieldName);
            genericData.put("setFieldType", fieldType);
            genericData.put("setBooleanField", booleanValue);
            genericData.put("setDoubleField", doubleValue);
            genericData.put("setIntegerField", integerValue);
            genericData.put("setStringField", stringValue);
            genericData.put("setTimestampField", timestampValue);
            genericData.put("setLongField", longValue);
            genericData.put("setFloatField", floatValue);

            allGenericData.add(Tools.setNullableField(
                    GenericServiceDataOuterClass.GenericServiceData.genericTypedData.newBuilder(), genericData)
                    .build());
        }
        return allGenericData.stream()
                .sorted(Comparator.comparing(
                        GenericServiceDataOuterClass.GenericServiceData.genericTypedData::getFieldName))
                .collect(Collectors.toList());
    }
}
