package com.solarwinds.msp.ncentral.eventproduction.adapter.eventbus.configuration;

import com.solarwinds.entities.Environment;
import com.typesafe.config.Config;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class EventBusConfig {
    private static final Logger LOGGER = LoggerFactory.getLogger(EventBusConfig.class);

    private final String streamNamePrefix;
    private final String username;
    private final String password;
    private final String clientIdPrefix;
    private final String environment;
    private final String groupId;
    private final String clientId;
    private final String trustStoreLocation;
    private final String trustStorePassword;
    private final boolean useSSL;
    private final int maximumProducerRebuildsOnError;
    private final int pollMillisecondTimeout;

    public EventBusConfig() {
        Config config = (new ConfigurationManager()).getConfig();
        this.streamNamePrefix = config.getString("ncentral.event.adapter.event_bus_stream_name_prefix");
        this.username = config.getString("ncentral.event.adapter.event_bus_username");
        this.password = config.getString("ncentral.event.adapter.event_bus_password");
        this.clientIdPrefix = config.getString("ncentral.event.adapter.event_bus_client_id_prefix");
        this.environment = config.getString("ncentral.event.adapter.environment");
        this.useSSL = config.getBoolean("ncentral.event.adapter.event_bus_useSSL");
        this.maximumProducerRebuildsOnError =
                config.getInt("ncentral.event.adapter.maximum_producer_rebuilds_on_error");
        this.pollMillisecondTimeout = config.getInt("ncentral.event.adapter.poll_milliseconds_timeout");
        this.groupId = config.getString("ncentral.event.adapter.event_bus_group_id");
        this.clientId = config.getString("ncentral.event.adapter.event_bus_client_id");
        this.trustStoreLocation = config.getString("ncentral.event.adapter.event_bus_trust_store_location");
        this.trustStorePassword = config.getString("ncentral.event.adapter.event_bus_trust_store_password");

        File eventBusTrustStoreDestinationFile = new File(trustStoreLocation);
        boolean exists = eventBusTrustStoreDestinationFile.length() > 0;
        if (!exists) {
            ClassLoader classloader = Thread.currentThread().getContextClassLoader();
            InputStream eventBusTrustStoreResourceFile = classloader.getResourceAsStream("eventbus.truststore.jks");
            try {
                if (eventBusTrustStoreResourceFile != null) {
                    FileUtils.copyInputStreamToFile(eventBusTrustStoreResourceFile, eventBusTrustStoreDestinationFile);
                }
            } catch (IOException e) {
                LOGGER.error("Could not copy event bus truststore input stream to file.", e);
            }
        } else {
            LOGGER.debug("Event bus truststore file already exists. Skipping resource extraction.");
        }
    }

    public String getStreamNamePrefix() {
        return streamNamePrefix;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getClientIdPrefix() {
        return clientIdPrefix;
    }

    public boolean getUseSSL() {
        return useSSL;
    }

    public int getMaxProducerRebuilds() {
        return maximumProducerRebuildsOnError;
    }

    public int getPollTimeout() {
        return pollMillisecondTimeout;
    }

    public String getGroupId() {
        return groupId;
    }

    public String getClientId() {
        return clientId;
    }

    public String getTrustStoreLocation() {
        return trustStoreLocation;
    }

    public String getTrustStorePassword() {
        return trustStorePassword;
    }

    public Environment getEnvironment() {
        switch (this.environment) {
            case "Dev":
                return Environment.Dev;
            case "Stage":
                return Environment.Stage;
            case "Production":
                return Environment.Production;
            default:
                return Environment.Test;
        }
    }
}
