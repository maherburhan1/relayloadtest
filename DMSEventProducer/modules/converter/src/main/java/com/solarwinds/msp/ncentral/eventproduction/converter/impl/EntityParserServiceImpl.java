package com.solarwinds.msp.ncentral.eventproduction.converter.impl;

import com.google.protobuf.GeneratedMessageV3;

import com.nable.logging.Logger;
import com.nable.logging.Loggers;
import com.nable.util.StringUtils;
import com.solarwinds.msp.ncentral.eventproduction.api.entity.Event;
import com.solarwinds.msp.ncentral.eventproduction.converter.EntityParserService;
import com.solarwinds.msp.ncentral.eventproduction.converter.impl.entity.EntityParser;

import org.apache.commons.lang3.ClassUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This class implements the business logic related to parsing of event entities/messages.
 *
 * @param <MESSAGE> the Message/Entity type.
 */
@Service
public class EntityParserServiceImpl<MESSAGE extends GeneratedMessageV3> implements EntityParserService<MESSAGE> {

    private static final String CLASS_TEMPLATE = EntityParser.class.getPackage().getName() + ".%sEvent";

    private static final Logger logger = Loggers.EVENT_PRODUCER;

    private final Map<String, Class> fullClassNameCache = new ConcurrentHashMap<>();
    private final Set<String> invalidFullClassNameCache = ConcurrentHashMap.newKeySet();
    private final Map<String, EntityParser<MESSAGE>> simpleClassNameCache = new ConcurrentHashMap<>();
    private final Set<String> invalidSimpleClassNameCache = ConcurrentHashMap.newKeySet();

    @Override
    public Optional<MESSAGE> getNewInstance(String className) {
        Optional<Class<MESSAGE>> generatedMessageV3Class = getClass(className);
        if (!generatedMessageV3Class.isPresent()) {
            logger.debug("Cannot find the class with name {}.", className);
        }

        return generatedMessageV3Class.flatMap(this::getNewInstance);
    }

    private Optional<MESSAGE> getNewInstance(Class<MESSAGE> generatedMessageV3Class) {
        try {
            return Optional.of(BeanUtils.instantiateClass(generatedMessageV3Class));
        } catch (Exception e) {
            logger.error("Cannot create an instance of class with name {}.", generatedMessageV3Class.getName(), e);
            return Optional.empty();
        }
    }

    @Override
    public Optional<List<MESSAGE>> parseRecord(Event event, MESSAGE messageEntity) {
        final String className = messageEntity.getClass().getSimpleName();
        final Optional<EntityParser<MESSAGE>> entityParser = getEntityParser(className);
        if (!entityParser.isPresent()) {
            logger.debug("Entity processing for {} does not require additional business logic applied.", className);
        }
        return entityParser.flatMap(parser -> parseRecord(parser, event, messageEntity));
    }

    private Optional<List<MESSAGE>> parseRecord(EntityParser<MESSAGE> entityParser, Event event,
            MESSAGE messageEntity) {
        try {
            return Optional.ofNullable(entityParser.parseRecord(event, messageEntity));
        } catch (Exception e) {
            logger.error("An error occurred when parsing a record.", e);
            return Optional.empty();
        }
    }

    @SuppressWarnings("unchecked")
    private <T> Optional<Class<T>> getClass(String className) {
        if (StringUtils.isBlank(className)) {
            logger.error("Cannot get class for blank class name '{}'.", className);
            return Optional.empty();
        }

        // check if there was already an attempt to get a class that exist
        final Class<T> result = fullClassNameCache.get(className);
        if (result != null) {
            return Optional.of(result);
        }

        // check if there was already an attempt to get a class that does not exist
        if (invalidFullClassNameCache.contains(className)) {
            return Optional.empty();
        }

        // try to find the class and cache the outcome
        try {
            final Class<T> theClass = (Class<T>) ClassUtils.getClass(className);
            fullClassNameCache.put(className, theClass);
            return Optional.of(theClass);
        } catch (Exception e) {
            invalidFullClassNameCache.add(className);
            logger.trace("Cannot get class for name {}.", className, e);
            return Optional.empty();
        }
    }

    @SuppressWarnings("unchecked")
    private Optional<EntityParser<MESSAGE>> getEntityParser(String className) {
        if (StringUtils.isBlank(className)) {
            logger.error("Cannot instantiate the required EntityParser class with blank class name '{}'.", className);
            return Optional.empty();
        }

        // check if there was already an attempt to get a class that exist
        final EntityParser<MESSAGE> result = simpleClassNameCache.get(className);
        if (result != null) {
            return Optional.of(result);
        }

        // check if there was already an attempt to get a class that does not exist
        if (invalidSimpleClassNameCache.contains(className)) {
            return Optional.empty();
        }

        // try to find the class and cache the outcome
        Optional<EntityParser<MESSAGE>> entityParser;
        try {
            final String entityParserClassName = String.format(CLASS_TEMPLATE, className);
            entityParser = getClass(entityParserClassName).map(BeanUtils::instantiateClass)
                    .map(o -> (EntityParser<MESSAGE>) o);
        } catch (Exception e) {
            logger.error("Cannot instantiate the required EntityParser class '{}'.", className, e);
            entityParser = Optional.empty();
        }

        if (entityParser.isPresent()) {
            simpleClassNameCache.put(className, entityParser.get());
        } else {
            invalidSimpleClassNameCache.add(className);
        }

        return entityParser;
    }
}