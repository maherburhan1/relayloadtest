package com.solarwinds.msp.ncentral.eventproduction.api.service.publisher;

/**
 * Details on published event.
 */
public interface PublishedEventInfo {
    /**
     * @return {@code true} if event was published successfully
     */
    boolean isSuccess();

    /**
     * @return text info on published event
     */
    String getInfo();
}