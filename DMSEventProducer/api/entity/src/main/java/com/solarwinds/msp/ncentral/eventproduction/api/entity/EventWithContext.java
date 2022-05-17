package com.solarwinds.msp.ncentral.eventproduction.api.entity;

import java.util.Optional;

/**
 * Common event interface with data required for server and bizapps customer identification as well as customer id where
 * eventing originated from.
 */
public interface EventWithContext {

    /**
     * @return N-central server GUID for this event.
     */
    String getNcentralServerGuid();

    /**
     * @return Bizapps customer ID for this event.
     */
    Optional<String> getBizappsCustomerId();

    /**
     * @return This events entity type.
     */
    String getEntityType();

    /**
     * @return Originating customer ID where is eventing configured
     */
    int getEventingConfigurationCustomerId();
}
