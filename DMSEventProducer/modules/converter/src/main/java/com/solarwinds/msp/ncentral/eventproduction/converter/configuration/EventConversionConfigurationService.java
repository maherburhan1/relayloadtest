package com.solarwinds.msp.ncentral.eventproduction.converter.configuration;

import com.solarwinds.msp.ncentral.eventproduction.api.entity.Event;

import java.util.List;
import java.util.Map;

/**
 * Service that provides configuration for converting {@link Event} objects into protobuf event entity objects.
 */
public interface EventConversionConfigurationService {

    /**
     * Gets a {@link Map<>} of event entities names to generate for the event in question.
     *
     * @param event the {@link String} instance.
     * @return the {@link Map<>} of event entities names to generate for the event in question.
     */
    Map<String, String> getEntityNamesForEventName(String event);

    /**
     * Gets a {@link Map<>} of event entity based on the name to generate for the event in question.
     *
     * @param entity the {@link String} instance.
     * @return the {@link Map<>} of event entity names to generate for the event in question.
     */
    Map<String, String> getEntityNamesForEntityName(String entity);

    /**
     * Gets a {@link Map<String, String>} of event all entities. These are keys indicating the name values indicating
     * the full class path.
     *
     * @return the {@link Map<>} of all event entity names.
     */
    Map<String, String> getAllEntityNames();

    /**
     * Gets a {@link Boolean} indicating whether the table in questions has time series data.
     *
     * @param tableName the table to lookup.
     * @return {@link Boolean}.
     */
    boolean isTimeSeries(String tableName);

    /**
     * Gets a {@link Map<>} of event customer lookup criteria for the table in question.
     *
     * @param tableName the table to lookup.
     * @return the {@link Map<>} of customer lookup criteria.
     */
    Map<String, String> getCustomerLookupCriteria(String tableName);

    /**
     * Gets a {@link Map<>} of event default customer lookup criteria for the entity in question.
     *
     * @param entityName the entity to lookup.
     * @return the {@link Map<>} of customer lookup criteria.
     */
    Map<String, String> getDefaultCustomerLookupCriteriaForEntity(String entityName);

    /**
     * Gets a {@link String} name of the incremental date column.
     *
     * @param tableName the table to lookup.
     * @return the {@link String} of incremental load date.
     */
    String getIncrementalLoadColumn(String tableName);

    /**
     * Gets a {@link List<String>} of tracked event entity fields.
     *
     * @param tableName the table to lookup.
     * @return the {@link List<String>} of tracked entity fields.
     */
    List<String> getEntityTrackedFields(String entityName, String tableName);

    /**
     * Gets a {@link List<String>} of mandatory event entity fields.
     *
     * @param tableName the table to lookup.
     * @return the {@link List<String>} of mandatory entity fields.
     */
    List<String> getEntityMandatoryFields(String entityName, String tableName);

    /**
     * Gets a {@link Map<>} of child entities and their class paths.
     *
     * @param primaryEventName primary event name to lookup children for.
     * @return the {@link Map<>} of child entities and their class paths.
     */
    Map<String, String> getChildEntitiesByParentName(String primaryEventName);

    /**
     * Gets a {@link Map<>} of source fields and target destinations.
     *
     * @param entityName the entity to lookup.
     * @param tableName the table to lookup.
     * @return the {@link Map<>} of source fields and target destinations
     */
    Map<String, String> getEntityMappedFields(String entityName, String tableName);
}


