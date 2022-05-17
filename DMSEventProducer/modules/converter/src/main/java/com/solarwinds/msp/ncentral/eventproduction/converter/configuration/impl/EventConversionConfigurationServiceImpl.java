package com.solarwinds.msp.ncentral.eventproduction.converter.configuration.impl;

import com.nable.util.StringUtils;
import com.solarwinds.msp.ncentral.eventproduction.converter.configuration.EventConversionConfigurationService;
import com.solarwinds.msp.ncentral.eventproduction.converter.configuration.entity.Conversion;
import com.solarwinds.msp.ncentral.eventproduction.converter.configuration.entity.Conversions;
import com.solarwinds.msp.ncentral.eventproduction.converter.configuration.entity.Field;
import com.solarwinds.msp.ncentral.eventproduction.converter.configuration.entity.Table;

import org.apache.commons.collections.MapUtils;
import org.jooq.lambda.tuple.Tuple2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * This class implements the business logic to manage event entity configuration.
 */
@Service
public class EventConversionConfigurationServiceImpl implements EventConversionConfigurationService {
    private final Conversions conversions;
    private static final String DEFAULT_DATE_FIELD = "lastupdated";
    private static final String GENERIC_SERVICE_ENTITYNAME = "GenericServiceData";
    private static final String GENERIC_SERVICE_DATATABLE_PREFIX = "data";
    private static final String GENERIC_SERVICE_DATATABLE_SUFFIX1 = "_detailed";
    private static final String GENERIC_SERVICE_DATATABLE_SUFFIX2 = "_aggregated";

    @Autowired
    public EventConversionConfigurationServiceImpl(Conversions conversions) {
        this.conversions = conversions;
    }

    private Map<String, Conversion> getConversionsContainingTable(String tableName) {
        Map<String, Conversion> conversionMap = conversions.getConversions()
                .entrySet()
                .stream()
                .map(conversionEntry -> new Tuple2<>(conversionEntry.getKey(), conversionEntry.getValue()))
                .filter(conversionTuple -> conversionTuple.v2().getTables() != null)
                .filter(conversionTuple -> conversionTuple.v2().getTables().containsKey(tableName))
                .collect(Collectors.toMap(Tuple2::v1, Tuple2::v2));
        if (conversionMap.isEmpty() && tableName.startsWith(GENERIC_SERVICE_DATATABLE_PREFIX) && (
                tableName.endsWith(GENERIC_SERVICE_DATATABLE_SUFFIX1) || tableName.endsWith(
                        GENERIC_SERVICE_DATATABLE_SUFFIX2))) {
            conversionMap = conversions.getConversions()
                    .entrySet()
                    .stream()
                    .filter(conversionEntry -> conversionEntry.getKey().equals(GENERIC_SERVICE_ENTITYNAME))
                    .map(conversionEntry -> new Tuple2<>(conversionEntry.getKey(), conversionEntry.getValue()))
                    .filter(conversionTuple -> conversionTuple.v2().getTables() != null)
                    .collect(Collectors.toMap(Tuple2::v1, Tuple2::v2));
        }
        return conversionMap;
    }

    @Override
    public boolean isTimeSeries(String tableName) {
        if (StringUtils.isNotBlank(tableName)) {
            boolean isTimeSeries = getConversionsContainingTable(tableName).values()
                    .stream()
                    .flatMap(conversion -> getTableEntities(tableName, conversion).map(Table::isTimeSeries))
                    .findFirst()
                    .orElse(false);

            if (!isTimeSeries) {
                isTimeSeries = getConversionsContainingTable(tableName).values()
                        .stream()
                        .map(Conversion::isTimeSeries)
                        .findFirst()
                        .orElse(false);
            }
            return isTimeSeries;
        }
        return false;
    }

    @Override
    public Map<String, String> getCustomerLookupCriteria(String tableName) {
        if (StringUtils.isNotBlank(tableName)) {
            Map<String, String> lookupCriteria = Optional.of(getConversionsContainingTable(tableName).values()
                    .stream()
                    .flatMap(conversion -> getTableEntities(tableName, conversion))
                    .filter(tableEntry -> StringUtils.isNotBlank(tableEntry.getJoinToCustomer()))
                    .collect(Collectors.toMap(Table::getJoinToCustomer, Table::getWhereToCustomer,
                            (join1, join2) -> join1))).orElse(Collections.emptyMap());
            if (lookupCriteria.isEmpty()) {
                lookupCriteria = Optional.of(getConversionsContainingTable(tableName).entrySet()
                        .stream()
                        .flatMap(conversionTuple -> getDefaultCustomerLookupCriteriaForEntity(
                                conversionTuple.getKey()).entrySet().stream())
                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (join1, join2) -> join1)))
                        .orElse(Collections.emptyMap());
            }
            return lookupCriteria;
        }
        return Collections.emptyMap();
    }

    @Override
    public Map<String, String> getDefaultCustomerLookupCriteriaForEntity(String entityName) {
        if (StringUtils.isNotBlank(entityName)) {
            Map<String, String> lookupCriteria = Optional.of(conversions.getConversions()
                    .entrySet()
                    .stream()
                    .filter(conversionEntry -> conversionEntry.getKey().equals(entityName))
                    .filter(conversionEntry -> StringUtils.isNotBlank(conversionEntry.getValue().getJoinToCustomer()))
                    .map(conversionEntry -> new Tuple2<>(conversionEntry.getValue().getJoinToCustomer(),
                            conversionEntry.getValue().getWhereToCustomer()))
                    .collect(Collectors.toMap(Tuple2::v1, Tuple2::v2))).orElse(Collections.emptyMap());

            List<String> entityTables = conversions.getConversions()
                    .entrySet()
                    .stream()
                    .filter(conversionEntry -> conversionEntry.getKey().equals(entityName))
                    .flatMap(conversionEntry -> conversionEntry.getValue().getTables().keySet().stream())
                    .collect(Collectors.toList());

            if (entityTables.size() == 1) {
                Map<String, String> tableLevelLookupCriteria = conversions.getConversions()
                        .entrySet()
                        .stream()
                        .filter(conversionEntry -> conversionEntry.getKey().equals(entityName))
                        .flatMap(conversionEntry -> conversionEntry.getValue()
                                .getTables()
                                .values()
                                .stream()
                                .filter(table -> StringUtils.isNotBlank(table.getJoinToCustomer()))
                                .collect(Collectors.toMap(Table::getJoinToCustomer, Table::getWhereToCustomer,
                                        (join1, join2) -> join1))
                                .entrySet()
                                .stream())
                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

                return tableLevelLookupCriteria.isEmpty() ? lookupCriteria : tableLevelLookupCriteria;
            } else {
                return lookupCriteria;
            }
        }
        return Collections.emptyMap();
    }

    @Override
    public String getIncrementalLoadColumn(String tableName) {
        if (tableName != null) {
            Map<String, String> tableEntities = getEntityNamesForEventName(tableName);
            String defaultDateField = tableEntities.keySet()
                    .stream()
                    .flatMap(entityName -> getEntityMappedFields(entityName, tableName).entrySet().stream())
                    .map(Map.Entry::getKey)
                    .distinct()
                    .filter(DEFAULT_DATE_FIELD::equalsIgnoreCase)
                    .findFirst()
                    .orElse("");

            String parentDateField = conversions.getConversions()
                    .values()
                    .stream()
                    .filter(conversion -> conversion.getTables().containsKey(tableName))
                    .filter(conversion -> StringUtils.isNotBlank(conversion.getIncrementalLoadDate()))
                    .map(Conversion::getIncrementalLoadDate)
                    .distinct()
                    .findFirst()
                    .orElse(defaultDateField);

            return getConversionsContainingTable(tableName).values()
                    .stream()
                    .flatMap(conversion -> getTableEntities(tableName, conversion))
                    .filter(tableEntry -> StringUtils.isNotBlank(tableEntry.getIncrementalLoadDate()))
                    .map(Table::getIncrementalLoadDate)
                    .findFirst()
                    .orElse(parentDateField);
        }
        return "";
    }

    private List<String> getChildConversionsList(String primaryEventName) {
        Optional<Map.Entry<String, Conversion>> conversion = conversions.getConversions()
                .entrySet()
                .stream()
                .filter(conversionEntry -> conversionEntry.getKey().equals(primaryEventName))
                .findFirst();

        return (conversion.isPresent()) ? conversion.get().getValue().getChildEntities() : Collections.emptyList();
    }

    private Map<String, Conversion> getConversionsForEntities(List<String> eventNames) {
        return conversions.getConversions()
                .entrySet()
                .stream()
                .map(conversionEntry -> new Tuple2<>(conversionEntry.getKey(), conversionEntry.getValue()))
                .filter(conversionTuple -> conversionTuple.v2().getTables() != null)
                .filter(conversionTuple -> eventNames.contains(conversionTuple.v1))
                .collect(Collectors.toMap(Tuple2::v1, Tuple2::v2));
    }

    private Map<String, Field> getChildFieldsForChildEntities(List<String> childEntities) {
        Map<String, Field> result = new HashMap<>();
        for (String childEntityName : childEntities) {
            Map<String, Field> childFields = conversions.getConversions()
                    .entrySet()
                    .stream()
                    .filter(conversionEntry -> conversionEntry.getKey().equals(childEntityName))
                    .map(conversionEntry -> conversionEntry.getValue().getFields())
                    .findFirst()
                    .orElse(Collections.emptyMap());
            if (!childFields.isEmpty()) {
                result.putAll(childFields);
            }
        }
        return result;
    }

    private Map<String, Field> getDefaultFieldMapForEntity(String entityName) {
        return conversions.getConversions()
                .entrySet()
                .stream()
                .filter(conversionEntry -> conversionEntry.getKey().equals(entityName))
                .map(conversionEntry -> conversionEntry.getValue().getFields())
                .findFirst()
                .orElse(Collections.emptyMap());
    }

    private Map<String, Field> getFieldMapForEntityTable(String entityName, String tableName) {
        Stream<Conversion> conversions = getConversionsContainingTable(tableName).entrySet()
                .stream()
                .filter(conversionEntry -> conversionEntry.getKey().equals(entityName))
                .map(Map.Entry::getValue);
        return conversions.flatMap(conversion -> getTableEntities(tableName, conversion).map(Table::getFields))
                .findFirst()
                .orElse(Collections.emptyMap());
    }

    private Stream<Table> getTableEntities(String tableName, Conversion conversion) {
        return conversion.getTables()
                .entrySet()
                .stream()
                .filter(tableEntry -> tableEntry.getKey().equalsIgnoreCase(tableName))
                .map(Map.Entry::getValue)
                .filter(Objects::nonNull);
    }

    private Map<String, Field> getFieldsForTableEntity(String entityName, String tableName) {
        if (StringUtils.isNotEmpty(entityName) && StringUtils.isNotEmpty(tableName)) {
            Map<String, Field> result = new HashMap<>();
            result.putAll(getChildFieldsForChildEntities(getChildConversionsList(entityName)));
            result.putAll(getDefaultFieldMapForEntity(entityName));
            result.putAll(getFieldMapForEntityTable(entityName, tableName));
            return result;
        }
        return Collections.emptyMap();
    }

    @Override
    public Map<String, String> getEntityNamesForEventName(String event) {
        if (event != null) {
            return getConversionsContainingTable(event).entrySet()
                    .stream()
                    .map(conversionEntry -> new Tuple2<>(conversionEntry.getKey(),
                            conversionEntry.getValue().getEntityPath()))
                    .collect(Collectors.toMap(Tuple2::v1, Tuple2::v2));
        }
        return Collections.emptyMap();
    }

    @Override
    public Map<String, String> getAllEntityNames() {
        return conversions.getConversions()
                .entrySet()
                .stream()
                .map(conversionEntry -> new Tuple2<>(conversionEntry.getKey(),
                        conversionEntry.getValue().getEntityPath()))
                .collect(Collectors.toMap(Tuple2::v1, Tuple2::v2));
    }

    @Override
    public Map<String, String> getEntityNamesForEntityName(String entityName) {
        if (entityName != null) {
            return getConversionsForEntities(Collections.singletonList(entityName)).entrySet()
                    .stream()
                    .map(conversionEntry -> new Tuple2<>(conversionEntry.getKey(),
                            conversionEntry.getValue().getEntityPath()))
                    .collect(Collectors.toMap(Tuple2::v1, Tuple2::v2));
        }
        return Collections.emptyMap();
    }

    @Override
    public List<String> getEntityTrackedFields(String entityName, String tableName) {
        if (StringUtils.isNotEmpty(entityName) && StringUtils.isNotEmpty(tableName)) {
            Map<String, Field> eventEntityFieldMap = getFieldsForTableEntity(entityName, tableName);

            if (MapUtils.isNotEmpty(eventEntityFieldMap)) {
                return eventEntityFieldMap.entrySet()
                        .stream()
                        .filter(fieldEntry -> fieldEntry.getValue().isTracked())
                        .map(Map.Entry::getKey)
                        .collect(Collectors.toList());
            }
        }
        return Collections.emptyList();
    }

    @Override
    public List<String> getEntityMandatoryFields(String entityName, String tableName) {
        if (StringUtils.isNotEmpty(entityName) && StringUtils.isNotEmpty(tableName)) {
            Map<String, Field> eventEntityFieldMap = getFieldsForTableEntity(entityName, tableName);

            if (MapUtils.isNotEmpty(eventEntityFieldMap)) {
                return eventEntityFieldMap.entrySet()
                        .stream()
                        .filter(fieldEntry -> fieldEntry.getValue().isMandatory())
                        .map(Map.Entry::getKey)
                        .collect(Collectors.toList());
            }
        }
        return Collections.emptyList();
    }

    @Override
    public Map<String, String> getChildEntitiesByParentName(String primaryEventName) {
        if (StringUtils.isNotEmpty(primaryEventName)) {
            Map<String, Conversion> childEntities =
                    getConversionsForEntities(getChildConversionsList(primaryEventName));
            if (MapUtils.isNotEmpty(childEntities)) {
                return childEntities.entrySet()
                        .stream()
                        .map(childEntityEntry -> new Tuple2<>(childEntityEntry.getKey(),
                                childEntityEntry.getValue().getEntityPath()))
                        .collect(Collectors.toMap(Tuple2::v1, Tuple2::v2));
            }
        }
        return Collections.emptyMap();
    }

    @Override
    public Map<String, String> getEntityMappedFields(String entityName, String tableName) {
        if (StringUtils.isNotEmpty(entityName) && StringUtils.isNotEmpty(tableName)) {
            Map<String, Field> eventEntityFieldMap = getFieldsForTableEntity(entityName, tableName);

            if (MapUtils.isNotEmpty(eventEntityFieldMap)) {
                return eventEntityFieldMap.entrySet()
                        .stream()
                        .map(fieldEntry -> new Tuple2<>(fieldEntry.getKey(), fieldEntry.getValue().getTargetField()))
                        .collect(Collectors.toMap(Tuple2::v1, Tuple2::v2));
            }
        }
        return Collections.emptyMap();
    }
}
