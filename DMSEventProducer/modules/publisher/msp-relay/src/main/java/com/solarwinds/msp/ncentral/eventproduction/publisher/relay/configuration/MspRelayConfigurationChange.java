package com.solarwinds.msp.ncentral.eventproduction.publisher.relay.configuration;

/**
 * Represents a change in the MSP Relay configuration.
 */
public class MspRelayConfigurationChange {

    private String bizAppsCustomerId;
    private boolean certificateChanged;
    private boolean relayHostChanged;

    /**
     * @return the BizApps Customer ID
     */
    public String getBizAppsCustomerId() {
        return bizAppsCustomerId;
    }

    /**
     * @param bizAppsCustomerId the bizAppsCustomerId to set
     */
    public MspRelayConfigurationChange setBizAppsCustomerId(String bizAppsCustomerId) {
        this.bizAppsCustomerId = bizAppsCustomerId;
        return this;
    }

    /**
     * @return the certificateChanged flag
     */
    public boolean isCertificateChanged() {
        return certificateChanged;
    }

    /**
     * @return the relayHostChanged flag
     */
    public boolean isRelayHostChanged() {
        return relayHostChanged;
    }

    /**
     * Sets certificateChanged flag to true.
     */
    public MspRelayConfigurationChange setCertificateChanged() {
        this.certificateChanged = true;
        return this;
    }

    /**
     * Sets relayHostChanged flag to true.
     */
    public MspRelayConfigurationChange setRelayHostChanged() {
        this.relayHostChanged = true;
        return this;
    }
}