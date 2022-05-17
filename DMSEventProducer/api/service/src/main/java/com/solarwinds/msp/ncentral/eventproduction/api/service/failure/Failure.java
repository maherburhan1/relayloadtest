package com.solarwinds.msp.ncentral.eventproduction.api.service.failure;

/**
 * Data holder class responsible for storing information regarding execution exceptions.
 */
public class Failure {
    private String businessApplicationsCustomerId;
    private Throwable caughtException;

    /**
     * Get the current stored Business Applications Customer ID.
     *
     * @return Business Applications Customer ID associated with this Configuration Failure.
     */
    public String getBusinessApplicationsCustomerId() {
        return businessApplicationsCustomerId;
    }

    /**
     * Get the exception produced by the gRPC failure.
     *
     * @return the exception associated with this Configuration Failure.
     */
    public Throwable getCaughtException() {
        return caughtException;
    }

    private Failure(Builder builder) {
        this.businessApplicationsCustomerId = builder.businessApplicationsCustomerId;
        this.caughtException = builder.caughtException;
    }

    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder to build {@link Failure}.
     */
    public static final class Builder {
        private String businessApplicationsCustomerId;
        private Throwable caughtException;

        private Builder() {}

        public Builder withBusinessApplicationsCustomerId(String businessApplicationsCustomerId) {
            this.businessApplicationsCustomerId = businessApplicationsCustomerId;
            return this;
        }

        public Builder withThrowable(Throwable caughtException) {
            this.caughtException = caughtException;
            return this;
        }

        public Failure build() {
            return new Failure(this);
        }
    }
}