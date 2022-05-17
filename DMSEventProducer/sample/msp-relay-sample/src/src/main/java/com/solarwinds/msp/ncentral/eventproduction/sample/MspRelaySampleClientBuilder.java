package com.solarwinds.msp.ncentral.eventproduction.sample;

import com.nable.util.StringUtils;

public class MspRelaySampleClientBuilder {
    public MspRelaySampleClientBuilder() {
    }

    public static MspRelaySampleClient buildSampleClient() {
        int numberOfMsgs = StringUtils.isNotBlank(System.getProperty("numberOfMessages")) ?
                Integer.valueOf(System.getProperty("numberOfMessages")) : 1;
        int batches = StringUtils.isNotBlank(System.getProperty("numberOfBatchesPerThread")) ?
                Integer.valueOf(System.getProperty("numberOfBatchesPerThread")) : 1;
        MspRelaySampleClient relay = new MspRelaySampleClient();
        relay.setSendPredefinedEvent(true);
        relay.setNumberOfMessages(numberOfMsgs);
        relay.setNumberOfBatchesPerThread(batches);
        relay.readSystemProperties();
        return relay;
    }

    public static MspRelaySampleExceutor buildMspRelaySampleExceutor() {
        MspRelaySampleExceutor exec = new MspRelaySampleExceutor();
        int numberOfThs = StringUtils.isNotBlank(System.getProperty("numberOfThreads")) ?
                Integer.valueOf(System.getProperty("numberOfThreads")) : 1;

        exec.setNumberOfThreads(numberOfThs);

        return exec;
    }
}
