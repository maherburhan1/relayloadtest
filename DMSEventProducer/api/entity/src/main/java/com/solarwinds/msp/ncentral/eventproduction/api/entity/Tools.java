package com.solarwinds.msp.ncentral.eventproduction.api.entity;

import com.google.protobuf.GeneratedMessageV3;
import com.google.protobuf.Timestamp;

import com.nable.logging.Logger;
import com.nable.logging.Loggers;
import com.solarwinds.msp.ncentral.proto.entity.MspContextOuterClass;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.math.NumberUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class Tools {

    private static final String SET_BUSINESS_APPLICATIONS_CUSTOMER_ID = "setBizAppsCustomerId";
    private static final String SET_SYSTEM_GUID = "setSystemGuid";
    private static final String TRUE_NUMBER = "1";
    private static final int PARAMETER_COUNT = 1;
    private static final int PARAMETER_INDEX = 0;

    public static final Map<Class<?>, Class<?>> wrapperPrimitiveMap =
            Stream.of(new AbstractMap.SimpleImmutableEntry<>(Boolean.class, Boolean.TYPE),
                    new AbstractMap.SimpleImmutableEntry<>(Byte.class, Byte.TYPE),
                    new AbstractMap.SimpleImmutableEntry<>(Character.class, Character.TYPE),
                    new AbstractMap.SimpleImmutableEntry<>(Short.class, Short.TYPE),
                    new AbstractMap.SimpleImmutableEntry<>(Integer.class, Integer.TYPE),
                    new AbstractMap.SimpleImmutableEntry<>(Long.class, Long.TYPE),
                    new AbstractMap.SimpleImmutableEntry<>(Double.class, Double.TYPE),
                    new AbstractMap.SimpleImmutableEntry<>(Float.class, Float.TYPE),
                    new AbstractMap.SimpleImmutableEntry<>(ArrayList.class, Iterable.class))
                    .collect(Collectors.collectingAndThen(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue),
                            Collections::unmodifiableMap));

    private static final Logger logger = Loggers.EVENT_PRODUCER;

    private Tools() {}

    /**
     * Converts {@link ZonedDateTime} to {@link Timestamp}, which is relative to an epoch at UTC midnight on January 1,
     * 1970.
     *
     * @param zonedDateTime {@link ZonedDateTime} to convert
     * @return {@link Timestamp} relative to an epoch at UTC midnight on January 1, 1970.
     */
    public static Timestamp toTimestamp(ZonedDateTime zonedDateTime) {
        if (zonedDateTime != null) {
            final Instant instantTime = zonedDateTime.toInstant();
            return Timestamp.newBuilder()
                    .setSeconds(instantTime.getEpochSecond())
                    .setNanos(instantTime.getNano())
                    .build();
        }
        return null;
    }

    public static <T extends GeneratedMessageV3.Builder<T>> T setNullableField(T entityBuilder,
            Map<String, Object> entityValues) {
        final Method[] methods = (entityBuilder.getClass()).getMethods();
        for (Map.Entry<String, Object> entry : entityValues.entrySet()) {
            if (entry.getValue() != null) {
                findMethodAndSetValue(methods, entry, entityBuilder);
            }
        }
        return entityBuilder;
    }

    private static <T extends GeneratedMessageV3.Builder<T>> void findMethodAndSetValue(Method[] methods,
            Map.Entry<String, Object> entry, T entityBuilder) {
        final String destination = entry.getKey();
        final Object value = entry.getValue();
        for (Method method : methods) {
            if (!method.getName().equals(destination) || method.getParameterCount() != PARAMETER_COUNT) {
                continue;
            }
            final Class<?> parameterType = method.getParameterTypes()[PARAMETER_INDEX];
            if (parameterType.getTypeName().equals(value.getClass().getTypeName()) || parameterType.equals(
                    wrapperPrimitiveMap.get(value.getClass()))) {
                try {
                    method.invoke(entityBuilder, value);
                } catch (IllegalAccessException | InvocationTargetException e) {
                    logger.warn("Could not invoke {} for {}.", destination, value.toString(), e);
                }
                return;
            }
        }
    }

    public static MspContextOuterClass.MspContext getMspContext(EventWithContext event) {
        final Map<String, Object> mspContextValues = new HashMap<>();
        event.getBizappsCustomerId()
                .ifPresent(value -> mspContextValues.put(SET_BUSINESS_APPLICATIONS_CUSTOMER_ID, value));
        mspContextValues.put(SET_SYSTEM_GUID, event.getNcentralServerGuid());

        final MspContextOuterClass.MspContext.Builder builder = MspContextOuterClass.MspContext.newBuilder();
        setNullableField(builder, mspContextValues);
        return builder.build();
    }

    public static Boolean getBoolean(Map<String, String> entity, String attributeName) {
        return (Boolean) Optional.ofNullable(entity.get(attributeName))
                .map(value -> parseObject(ClassTypes.BOOLEAN, value))
                .orElse(false);
    }

    public static Long getLong(Map<String, String> entity, String attributeName) {
        return (Long) Optional.ofNullable(entity.get(attributeName))
                .map(value -> parseObject(ClassTypes.LONG, value))
                .orElse(null);
    }

    public static Float getFloat(Map<String, String> entity, String attributeName) {
        return (Float) Optional.ofNullable(entity.get(attributeName))
                .map(value -> parseObject(ClassTypes.FLOAT, value))
                .orElse(null);
    }

    public static Integer getInteger(Map<String, String> entity, String attributeName) {
        return (Integer) Optional.ofNullable(entity.get(attributeName))
                .map(value -> parseObject(ClassTypes.INTEGER, value))
                .orElse(null);
    }

    public static Double getDouble(Map<String, String> entity, String attributeName) {
        return (Double) Optional.ofNullable(entity.get(attributeName))
                .map(value -> parseObject(ClassTypes.DOUBLE, value))
                .orElse(null);
    }

    public static Object parseObject(String objectType, String value) {
        if (NumberUtils.isCreatable(value) || ClassTypes.BOOLEAN.equalsIgnoreCase(objectType)) {
            try {
                switch (objectType.toLowerCase()) {
                    case ClassTypes.BOOLEAN:
                        return (TRUE_NUMBER.equals(value.trim())) || BooleanUtils.toBoolean(value);
                    case ClassTypes.LONG:
                        return NumberUtils.createLong(value);
                    case ClassTypes.INTEGER:
                    case ClassTypes.INT:
                        return NumberUtils.createInteger(value);
                    case ClassTypes.DOUBLE:
                        return NumberUtils.createDouble(value);
                    case ClassTypes.FLOAT:
                        return NumberUtils.createFloat(value);
                    default:
                        return null;
                }
            } catch (NumberFormatException e) {
                logger.debug("Could not parse '{}' into type '{}'.", value, objectType, e);
                return null;
            }
        } else {
            logger.debug("Could not parse '{}' into type '{}'.", value, objectType);
            return null;
        }
    }

    public static Timestamp getNowTimestamp() {
        final ZonedDateTime time = ZonedDateTime.now(ZoneOffset.UTC);
        return Timestamp.newBuilder().setSeconds(time.toEpochSecond()).setNanos(time.getNano()).build();
    }
}
