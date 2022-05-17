package com.solarwinds.msp.ncentral.eventproduction.api.service.scraping;

import java.sql.Timestamp;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Wrapper of parameters for the {@link EventScraper#export} method
 */
public class ExportParameters {
    private List<String> entityTypesNames;
    private Optional<Timestamp> fromTimestamp;
    private Optional<Timestamp> toTimestamp;
    private boolean useAddOperations;

    private ExportParameters() {
    }

    public List<String> getEntityTypesNames() {
        return entityTypesNames;
    }

    public Optional<Timestamp> getFromTimestamp() {
        return fromTimestamp.map(ts -> new Timestamp(ts.getTime()));
    }

    public Optional<Timestamp> getToTimestamp() {
        return toTimestamp.map(ts -> new Timestamp(ts.getTime()));
    }

    public boolean isUseAddOperations() {
        return useAddOperations;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private List<String> entityTypesNames;
        private Timestamp fromTimestamp;
        private Timestamp toTimestamp;
        private boolean useAddOperations;

        private Builder() {}

        public Builder entityTypesNames(List<String> entityTypesNames) {
            this.entityTypesNames = Collections.unmodifiableList(entityTypesNames);
            return this;
        }

        public Builder fromTimestamp(Timestamp fromTimestamp) {
            this.fromTimestamp = new Timestamp(fromTimestamp.getTime());
            return this;
        }

        public Builder toTimestamp(Timestamp toTimestamp) {
            this.toTimestamp = new Timestamp(toTimestamp.getTime());
            return this;
        }

        public Builder useAddOperations(boolean useAddOperations) {
            this.useAddOperations = useAddOperations;
            return this;
        }

        public ExportParameters build() {
            ExportParameters exportParameters = new ExportParameters();
            exportParameters.entityTypesNames =
                    Objects.requireNonNull(entityTypesNames, "entityTypesNames must not be null");
            exportParameters.fromTimestamp = Optional.ofNullable(fromTimestamp);
            exportParameters.toTimestamp = Optional.ofNullable(toTimestamp);
            exportParameters.useAddOperations = this.useAddOperations;
            return exportParameters;
        }
    }
}