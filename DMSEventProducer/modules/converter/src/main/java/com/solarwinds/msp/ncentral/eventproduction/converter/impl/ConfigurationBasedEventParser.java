package com.solarwinds.msp.ncentral.eventproduction.converter.impl;

import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.GeneratedMessageV3;
import com.google.protobuf.Message;
import com.google.protobuf.Timestamp;

import com.nable.logging.Logger;
import com.nable.logging.Loggers;
import com.solarwinds.msp.ncentral.eventproduction.api.entity.Event;
import com.solarwinds.msp.ncentral.eventproduction.api.entity.EventStatistic;
import com.solarwinds.msp.ncentral.eventproduction.api.entity.EventType;
import com.solarwinds.msp.ncentral.eventproduction.api.entity.Tools;
import com.solarwinds.msp.ncentral.eventproduction.api.service.statistics.EventStatistics;
import com.solarwinds.msp.ncentral.eventproduction.converter.EntityParserService;
import com.solarwinds.msp.ncentral.eventproduction.converter.EventParser;
import com.solarwinds.msp.ncentral.eventproduction.converter.ServiceProcessingRules;
import com.solarwinds.msp.ncentral.eventproduction.converter.configuration.EventConversionConfigurationService;
import com.solarwinds.msp.ncentral.proto.entity.MspContextOuterClass;
import com.solarwinds.util.time.ZonedDateTimeParser;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.MethodUtils;
import org.jooq.lambda.tuple.Tuple;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.solarwinds.msp.ncentral.eventproduction.converter.ServiceProcessingRules.PROCESS_WITH_MAPPED_SERVICE_DATA;

/**
 * Parses {@link Event} object's string event data into Protocol Buffers objects - descendants of {@link
 * GeneratedMessageV3} class.
 */
@Component
public class ConfigurationBasedEventParser implements EventParser<GeneratedMessageV3> {

    private static final String CUSTOM_SERVICE_ENTITY = "GenericServiceData";
    private static final String CUSTOM_SERVICE_TABLE_PREFIX = "data";

    private static final String CLASS_NAME_STRING = String.class.getName().toLowerCase();
    private static final String CLASS_NAME_TIMESTAMP = Timestamp.class.getName().toLowerCase();
    private static final String CLASS_SIMPLE_NAME_BOOLEAN = Boolean.class.getSimpleName().toLowerCase();
    private static final String METHOD_NAME_PREFIX_ADD = "add";
    private static final String METHOD_NAME_PREFIX_SET = "set";
    private static final String METHOD_NAME_SET_CONTEXT = "setContext";
    private static final String METHOD_NAME_SET_ACTION_VALUE = "setActionValue";
    private static final String NULL = "null";
    private static final int PARAMETER_COUNT = 1;
    private static final int PARAMETER_INDEX = 0;

    private static final Logger logger = Loggers.EVENT_PRODUCER;

    private final EntityParserService<GeneratedMessageV3> entityParserService;
    private final EventConversionConfigurationService eventConversionConfigurationService;
    private final EventStatistics eventStatistics;

    @Autowired
    public ConfigurationBasedEventParser(EntityParserService<GeneratedMessageV3> entityParserService,
            EventConversionConfigurationService eventConversionConfigurationService, EventStatistics eventStatistics) {
        this.entityParserService = entityParserService;
        this.eventConversionConfigurationService = eventConversionConfigurationService;
        this.eventStatistics = eventStatistics;
    }

    private Map<String, String> getParsableEntities(String entityType, ServiceProcessingRules serviceProcessingRule) {
        Map<String, String> parsableEntities =
                eventConversionConfigurationService.getEntityNamesForEventName(entityType);

        switch (serviceProcessingRule) {
            case PROCESS_ALL_EVENTS_AVAILABLE:
                if (entityType.toLowerCase().startsWith(CUSTOM_SERVICE_TABLE_PREFIX)) {
                    parsableEntities.putAll(
                            eventConversionConfigurationService.getEntityNamesForEntityName(CUSTOM_SERVICE_ENTITY));
                }
                break;
            case PROCESS_WITH_GENERIC_SERVICE_DATA_ONLY:
                if (entityType.toLowerCase().startsWith(CUSTOM_SERVICE_TABLE_PREFIX)) {
                    parsableEntities.clear();
                    parsableEntities.putAll(
                            eventConversionConfigurationService.getEntityNamesForEntityName(CUSTOM_SERVICE_ENTITY));
                }
                break;
            default:
                break;
        }

        if (MapUtils.isEmpty(parsableEntities) && entityType.toLowerCase().startsWith(CUSTOM_SERVICE_TABLE_PREFIX)) {
            parsableEntities.putAll(
                    eventConversionConfigurationService.getEntityNamesForEntityName(CUSTOM_SERVICE_ENTITY));
        }
        return parsableEntities;
    }

    @Override
    public List<GeneratedMessageV3> parse(Event event) {
        return parse(event, PROCESS_WITH_MAPPED_SERVICE_DATA);
    }

    @Override
    public List<GeneratedMessageV3> parse(Event event, ServiceProcessingRules serviceProcessingRule) {
        List<GeneratedMessageV3> parsedEvents = Collections.emptyList();
        Map<String, String> parsableEntities = getParsableEntities(event.getEntityType(), serviceProcessingRule);

        if (parsableEntities.size() > 0) {
            if (logger.isDebugEnabled()) {
                logger.debug("Attempting to convert {} to [{}] protocol buffer(s).", event.getEntityType(),
                        parsableEntities.keySet().stream().map(Object::toString).collect(Collectors.joining(",")));
            }
            parsedEvents = parsableEntities.entrySet()
                    .stream()
                    .filter(eventEntity -> shouldBeParsed(event, eventEntity.getKey()))
                    .flatMap(eventEntity -> parse(event, eventEntity).stream())
                    .collect(Collectors.toList());
        }
        if (!parsedEvents.isEmpty()) {
            eventStatistics.addStatistic(EventStatistic.builder()
                    .statisticType(EventStatistic.StatisticType.PROCESSING_DATA)
                    .statisticSubType(EventStatistic.StatisticSubType.SUCCESSFULLY_PARSED)
                    .statisticValue(parsedEvents.size())
                    .build());
        }
        return parsedEvents;
    }

    private boolean shouldBeParsed(Event event, String entityName) {
        List<String> trackedEntityFields =
                eventConversionConfigurationService.getEntityTrackedFields(entityName, event.getEntityType());

        Collection<String> newValues = new ArrayList<>(event.getNewValues().keySet());
        newValues.retainAll(trackedEntityFields);
        if (CollectionUtils.isEmpty(newValues) && event.getEventType().equals(EventType.UPDATE)) {
            logger.debug("{} event has no viable changes. {} entity will not be generated.", event.getEntityType(),
                    entityName);
            eventStatistics.addStatistic(EventStatistic.builder()
                    .statisticType(EventStatistic.StatisticType.PROCESSING_DATA)
                    .statisticSubType(EventStatistic.StatisticSubType.IGNORED_NO_RELEVANT_CHANGES)
                    .statisticValue(1)
                    .build());
            return false;
        }

        List<String> mandatoryFields =
                eventConversionConfigurationService.getEntityMandatoryFields(entityName, event.getEntityType());
        return !isMandatoryFieldMissing(mandatoryFields, event);
    }

    private List<GeneratedMessageV3> parse(Event event, Map.Entry<String, String> eventEntity) {
        return parseEntityObject(eventEntity, event).map(parsedEvent -> setCommonEntityProperties(parsedEvent, event))
                .map(parsedEvent -> applyBusinessLogic(parsedEvent, event))
                .orElseGet(() -> {
                    logger.warn("Protocol buffer entity could not be created.  Please see previous error for details.");
                    return Collections.emptyList();
                });
    }

    /**
     * Validates wethere a manditory field is missing from the event data.
     *
     * @param event {@link Event} that specifies which data should be examined.
     * @param mandatoryFields {@link List<String>} that specifies entity mandatory fields.
     * @return {@link Boolean} indicating a mandatory field is missing.
     */
    private boolean isMandatoryFieldMissing(List<String> mandatoryFields, Event event) {
        Map<String, String> entity = event.getEntity();
        for (String mandatoryField : mandatoryFields) {
            if (StringUtils.isEmpty(entity.get(mandatoryField)) || NULL.equalsIgnoreCase(entity.get(mandatoryField))) {
                logger.warn("{} is a key field for {} and cannot be empty.", mandatoryField, event.getEntityType());
                eventStatistics.addStatistic(EventStatistic.builder()
                        .statisticType(EventStatistic.StatisticType.ERROR_HANDLING)
                        .statisticSubType(EventStatistic.StatisticSubType.IGNORED_INCOMPLETE_DATA)
                        .statisticValue(1)
                        .build());
                return true;
            }
        }
        return false;
    }

    /**
     * Adds additional logic to the existing Protocol Buffers entity/message instance.
     *
     * @param parsedEvent the existing Protocol Buffers entity (message that extends {@link GeneratedMessageV3}).
     * @param event the {@link Event} with data to be processed.
     * @return The {@link List} of entities/messages - updated instances for the specified {@link Event}.
     */
    private List<GeneratedMessageV3> applyBusinessLogic(GeneratedMessageV3 parsedEvent, Event event) {
        // try to parse the record
        final Optional<List<GeneratedMessageV3>> resultsOptional = entityParserService.parseRecord(event, parsedEvent);
        if (!resultsOptional.isPresent()) {
            eventStatistics.addStatistic(EventStatistic.builder()
                    .statisticType(EventStatistic.StatisticType.ERROR_HANDLING)
                    .statisticSubType(EventStatistic.StatisticSubType.MALFORMED_DATA_ERROR)
                    .statisticValue(1)
                    .build());
            // return the unchanged Protocol Buffers entity
            return Collections.singletonList(parsedEvent);
        }

        List<GeneratedMessageV3> results = resultsOptional.get();
        if (CollectionUtils.isNotEmpty(results)) {
            return resultsOptional.get();
        }

        // track event timestamp that failed to meet the business logic
        eventStatistics.addStatistic(EventStatistic.builder()
                .statisticType(EventStatistic.StatisticType.ERROR_HANDLING)
                .statisticSubType(EventStatistic.StatisticSubType.IGNORED_INCOMPLETE_DATA)
                .statisticValue(1)
                .build());
        return Collections.emptyList();
    }

    /**
     * Adds common properties to the existing Protocol Buffers entity instance.
     *
     * @param parsedEvent {@link Object} that specifies the existing Protocol Buffers entity.
     * @param event {@link Event} that specifies which data should be processed.
     * @return {@link List<Object>} updated instance for the specified {@link Event}
     */
    private GeneratedMessageV3 setCommonEntityProperties(GeneratedMessageV3 parsedEvent, Event event) {
        try {
            final Message.Builder parsedEventBuilder = parsedEvent.toBuilder();
            final Method setContextMethod =
                    MethodUtils.getMatchingMethod(parsedEventBuilder.getClass(), METHOD_NAME_SET_CONTEXT,
                            MspContextOuterClass.MspContext.class);
            final Method setActionMethod =
                    MethodUtils.getMatchingMethod(parsedEventBuilder.getClass(), METHOD_NAME_SET_ACTION_VALUE,
                            int.class);
            if (setContextMethod != null) {
                setContextMethod.invoke(parsedEventBuilder, Tools.getMspContext(event));
            }
            if (setActionMethod != null) {
                setActionMethod.invoke(parsedEventBuilder, event.getEventType().ordinal());
            }
            parsedEvent = (GeneratedMessageV3) parsedEventBuilder.build();
        } catch (IllegalAccessException | InvocationTargetException e) {
            eventStatistics.addStatistic(EventStatistic.builder()
                    .statisticType(EventStatistic.StatisticType.ERROR_HANDLING)
                    .statisticSubType(EventStatistic.StatisticSubType.MALFORMED_DATA_ERROR)
                    .statisticValue(1)
                    .build());
            logger.error("Could not apply common properties to protocol buffer entity {}.",
                    parsedEvent.getClass().getSimpleName(), e);
        }
        return parsedEvent;
    }

    /**
     * Generic parser for mapped fields.
     *
     * @param eventEntityDescription {@link Map.Entry<>} that specifies the existing Protocol Buffers entity.
     * @param event {@link Event} that specifies which data should be processed.
     * @return {@link List<Object>} updated instance for the specified {@link Event}
     */
    private Optional<GeneratedMessageV3> parseEntityObject(Map.Entry<String, String> eventEntityDescription,
            Event event) {
        final String className = eventEntityDescription.getValue();
        final Optional<GeneratedMessageV3> parentEntityObjectOptional = entityParserService.getNewInstance(className);
        if (!parentEntityObjectOptional.isPresent()) {
            logger.error("Protocol buffer {} entity could not be created for name '{}'.",
                    eventEntityDescription.getKey(), className);
            return Optional.empty();
        }

        GeneratedMessageV3 parentEntityObject = parentEntityObjectOptional.get();
        final Message.Builder parentEntityBuilder = parentEntityObject.newBuilderForType();

        setChildEntities(parentEntityBuilder, event, eventEntityDescription);

        final Method[] methods = parentEntityBuilder.getClass().getDeclaredMethods();
        final Map<String, String> mappedFields =
                eventConversionConfigurationService.getEntityMappedFields(eventEntityDescription.getKey(),
                        event.getEntityType());
        final List<String> mandatoryFields =
                eventConversionConfigurationService.getEntityMandatoryFields(eventEntityDescription.getKey(),
                        event.getEntityType());
        final Map<String, String> entity = event.getEntity();

        boolean setMinimumOneField = false;
        for (Map.Entry<String, String> mappedField : mappedFields.entrySet()) {
            if (StringUtils.isNotBlank(mappedField.getValue())) {
                final Optional<Boolean> result =
                        findMethodAndSetValue(methods, mappedField, parentEntityBuilder, entity, mandatoryFields);
                if (!result.isPresent()) {
                    return Optional.empty();
                }
                setMinimumOneField = setMinimumOneField || result.get();
            }
        }

        if (setMinimumOneField) {
            parentEntityObject = (GeneratedMessageV3) parentEntityBuilder.build();
        }
        return Optional.of(parentEntityObject);
    }

    private void setChildEntities(Message.Builder parentEntityBuilder, Event event,
            Map.Entry<String, String> eventEntityDescription) {
        eventConversionConfigurationService.getChildEntitiesByParentName(eventEntityDescription.getKey())
                .entrySet()
                .stream()
                .filter(childEntity -> hasMappedFields(event, childEntity))
                .map(childEntity -> parseEntityObject(childEntity, event).map(
                        parsedChildEntity -> Tuple.tuple(childEntity.getKey(), parsedChildEntity)))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .forEach(nameEntityTuple -> setChildEntity(parentEntityBuilder, nameEntityTuple.v1(),
                        nameEntityTuple.v2()));
    }

    private boolean hasMappedFields(Event event, Map.Entry<String, String> childEntity) {
        return eventConversionConfigurationService.getEntityMappedFields(childEntity.getKey(), event.getEntityType())
                .values()
                .stream()
                .anyMatch(StringUtils::isNotBlank);
    }

    private void setChildEntity(Message.Builder parentEntityBuilder, String entityName,
            GeneratedMessageV3 entityValue) {
        if (!isRepeated(parentEntityBuilder, entityName)) {
            setChildEntityForField(parentEntityBuilder, entityName, entityValue);
        } else {
            setChildEntityForRepeatedField(parentEntityBuilder, entityName, entityValue);
        }
    }

    private void setChildEntityForField(Message.Builder parentEntityBuilder, String entityName,
            GeneratedMessageV3 entityValue) {
        try {
            MethodUtils.invokeMethod(parentEntityBuilder, METHOD_NAME_PREFIX_SET + entityName, entityValue);
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            logger.debug(
                    "Protocol buffer child entity set method could not be found. Attempting add method for repeated "
                            + "fields.", e);
            setChildEntityForRepeatedField(parentEntityBuilder, entityName, entityValue);
        }
    }

    private void setChildEntityForRepeatedField(Message.Builder parentEntityBuilder, String entityName,
            GeneratedMessageV3 entityValue) {
        try {
            MethodUtils.invokeMethod(parentEntityBuilder, METHOD_NAME_PREFIX_ADD + entityName, entityValue);
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            logger.error("Protocol buffer child entity add/set method could not be executed.", e);
        }
    }

    private boolean isRepeated(Message.Builder parentEntityBuilder, String entityName) {
        FieldDescriptor field = parentEntityBuilder.getDescriptorForType().findFieldByName(entityName.toLowerCase());
        if (field != null) {
            return field.isRepeated();
        }
        return false;
    }

    private Optional<Boolean> findMethodAndSetValue(Method[] methods, Map.Entry<String, String> mappedField,
            Message.Builder parentEntityBuilder, Map<String, String> entity, List<String> mandatoryFields) {
        final String sourceField = mappedField.getKey();
        final String targetField = mappedField.getValue();

        String eventValue = entity.get(sourceField);
        if (NULL.equalsIgnoreCase(eventValue)) {
            eventValue = null;
        }

        boolean setMinimumOneField = false;
        for (Method entityMethod : methods) {
            if (entityMethod.getName().equalsIgnoreCase(METHOD_NAME_PREFIX_SET + targetField)
                    && entityMethod.getParameterCount() == PARAMETER_COUNT) {
                final Optional<Boolean> result =
                        setValue(entityMethod, sourceField, targetField, eventValue, parentEntityBuilder, entity,
                                mandatoryFields);
                if (!result.isPresent()) {
                    return Optional.empty();
                }
                setMinimumOneField = setMinimumOneField || result.get();
            }
        }
        return Optional.of(setMinimumOneField);
    }

    private Optional<Boolean> setValue(Method entityMethod, String sourceField, String targetField, String eventValue,
            Message.Builder parentEntityBuilder, Map<String, String> entity, List<String> mandatoryFields) {
        final Class<?> parameterType = entityMethod.getParameterTypes()[PARAMETER_INDEX];
        final String parameterTypeName = parameterType.getName().toLowerCase();
        try {
            if (CLASS_NAME_STRING.equals(parameterTypeName)) {
                if (eventValue != null) {
                    entityMethod.invoke(parentEntityBuilder, eventValue);
                }
            } else if (CLASS_NAME_TIMESTAMP.equals(parameterTypeName)) {
                final Timestamp timeValue = Tools.toTimestamp(ZonedDateTimeParser.parseDateTime(entity, sourceField));
                if (timeValue != null) {
                    entityMethod.invoke(parentEntityBuilder, timeValue);
                } else if (mandatoryFields.contains(sourceField)) {
                    logger.warn("{} is a key field for {} and could not be parsed.", sourceField,
                            parentEntityBuilder.getDescriptorForType().getName());
                    return Optional.empty();
                }
            } else if (Tools.wrapperPrimitiveMap.containsValue(parameterType)) {
                final Object parsedValue = parseObjectValue(parameterTypeName, eventValue);
                if (mandatoryFields.contains(sourceField) && parsedValue == null) {
                    logger.warn("{} is a key field for {} and could not be parsed.", sourceField,
                            parentEntityBuilder.getDescriptorForType().getName());
                    return Optional.empty();
                } else if (eventValue != null) {
                    entityMethod.invoke(parentEntityBuilder, parsedValue);
                }
            }
            return Optional.of(true);
        } catch (IllegalAccessException | InvocationTargetException e) {
            logger.warn("Could not invoke {} on {} for {}.", targetField,
                    parentEntityBuilder.getDescriptorForType().getName(), entity.get(sourceField), e);
        }
        return Optional.of(false);
    }

    private Object parseObjectValue(String parameterTypeName, String eventValue) {
        return Optional.ofNullable(eventValue)
                .map(value -> Tools.parseObject(parameterTypeName, value))
                .orElse(CLASS_SIMPLE_NAME_BOOLEAN.equals(parameterTypeName) ? false : null);
    }
}
