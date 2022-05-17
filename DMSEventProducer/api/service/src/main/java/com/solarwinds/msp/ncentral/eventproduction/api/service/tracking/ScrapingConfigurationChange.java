package com.solarwinds.msp.ncentral.eventproduction.api.service.tracking;

/**
 * Representation of scraping configuration change.
 */
public class ScrapingConfigurationChange {

    private int customerId;
    private boolean startScraping;
    private boolean stopScraping;

    private ScrapingConfigurationChange() {}

    public int getCustomerId() {
        return customerId;
    }

    public boolean isStartScraping() {
        return startScraping;
    }

    public boolean isStopScraping() {
        return stopScraping;
    }

    /**
     * @return {@link ScrapingConfigurationChange.ScrapingConfigurationChangeBuilder} builder for creating {@link
     * ScrapingConfigurationChange} instances.
     */
    public static ScrapingConfigurationChange.ScrapingConfigurationChangeBuilder builder() {
        return new ScrapingConfigurationChange.ScrapingConfigurationChangeBuilder();
    }

    /**
     * Builder class for {@link ScrapingConfigurationChange} instances.
     */
    public static final class ScrapingConfigurationChangeBuilder {
        private int customerId;
        private boolean startScraping;
        private boolean stopScraping;

        private ScrapingConfigurationChangeBuilder() {}

        public ScrapingConfigurationChangeBuilder withCustomerId(Integer customerId) {
            this.customerId = customerId;
            return this;
        }

        public ScrapingConfigurationChangeBuilder stopScraping() {
            this.stopScraping = true;
            return this;
        }

        public ScrapingConfigurationChangeBuilder startScraping() {
            this.startScraping = true;
            return this;
        }

        public ScrapingConfigurationChangeBuilder restartScraping() {
            this.startScraping = true;
            this.stopScraping = true;
            return this;
        }

        public ScrapingConfigurationChange build() {
            ScrapingConfigurationChange configurationChange = new ScrapingConfigurationChange();
            configurationChange.customerId = customerId;

            if (startScraping && !stopScraping) {
                throw new IllegalArgumentException("If startScraping is true, conditionally stopScraping has to be "
                        + "true for correct scraping threads handling.");
            }

            configurationChange.startScraping = startScraping;
            configurationChange.stopScraping = stopScraping;

            return configurationChange;
        }
    }

}
