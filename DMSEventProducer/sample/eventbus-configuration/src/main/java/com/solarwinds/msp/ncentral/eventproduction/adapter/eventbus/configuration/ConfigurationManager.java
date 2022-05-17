package com.solarwinds.msp.ncentral.eventproduction.adapter.eventbus.configuration;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

public class ConfigurationManager {
    private final Config conf;

    public ConfigurationManager() {
        this.conf = ConfigFactory.load();
    }

    public Config getConfig() {
        return conf;
    }
}
