package com.solarwinds.msp.ncentral.eventproduction.sample;

import com.nable.util.StringUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class MspRelaySampleExceutor {
    private int numberOfThreads;
    private int batchSize = 0;
    private int numberOfMessagesPerBatch = 1;

    public int getNumberOfThreads() {
        return numberOfThreads;
    }

    public void setNumberOfThreads(int numberOfThreads) {
        this.numberOfThreads = numberOfThreads;
    }

    private static final Logger logger = LoggerFactory.getLogger(MspRelaySampleExceutor.class);

    protected MspRelaySampleExceutor(int numberOfThreads) {
        this.numberOfThreads = numberOfThreads;

    }

    private MspRelaySampleExceutor() {
    }

    public void run() {

        while (true) {
            excute();
        }
    }

    private void excute() {
        int totalNumberOfMessages = 0;
        logger.info("Initializing thread pool to {} threads", numberOfThreads);
        ThreadPoolExecutor threadPool = (ThreadPoolExecutor) Executors.newFixedThreadPool(numberOfThreads);

        List<MspRelaySampleClient> clientArray = new ArrayList<MspRelaySampleClient>();
        for (int i = 0; i < numberOfThreads; i++) {
            MspRelaySampleClient client = MspRelaySampleClientBuilder.buildSampleClient();
            clientArray.add(client);
        }
        long startTime = System.currentTimeMillis();
        logger.info("Start time: {}", startTime);
        int clientMsgs = 0;
        for (MspRelaySampleClient client : clientArray) {
            threadPool.execute(client);
            totalNumberOfMessages += (client.getActualPatchCount() * client.getNumberOfMessages());
        }

        do {
            //logger.info(">>>>>>>>>>>>>> checking Progress every 1 second >>>>>>>>>>>>>>>>>>>");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } while (threadPool.getActiveCount() != 0);

        logger.info("<<<<<<<<<<<<<<<<<< Completed with total of {} messages in {} MS >>>>>>>>>>>>>>>>>>>>>",
                totalNumberOfMessages, (System.currentTimeMillis() - startTime));

        threadPool.shutdown();
        try {
            if (!threadPool.awaitTermination(5, TimeUnit.SECONDS)) {
                threadPool.shutdownNow();
                logger.info("Total time (sec) = " + (System.currentTimeMillis() - startTime) / 1000);
            }
        } catch (InterruptedException ex) {
            logger.info("************* Thread was interrupted!!! ************");
            threadPool.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    public static void main(String[] arguments) {
        long startTime = System.currentTimeMillis();
        String numberOfThreads = System.getProperty("numberOfThreads");
        if (StringUtils.isNotBlank(numberOfThreads)) {
            System.setProperty("numberOfThreads", "1");
        }
        String numberOfMessages = System.getProperty("numberOfMessages");
        if (StringUtils.isNotBlank(numberOfMessages)) {
            System.setProperty("numberOfMessages", "1");
        }
        String numberOfBatchesPerThread = System.getProperty("numberOfBatchesPerThread");
        if (StringUtils.isNotBlank(numberOfBatchesPerThread)) {
            System.setProperty("numberOfBatchesPerThread", "1");
        }
        String eventType = System.getProperty("eventType");
        if (StringUtils.isNotBlank(eventType)) {
            System.setProperty("eventType",
                    EventTypeEnum.GENERICSERVICEDATA.name() + ", " + EventTypeEnum.FIREWALLINCIDENTS.name());
        }

        MspRelaySampleExceutor exec = MspRelaySampleClientBuilder.buildMspRelaySampleExceutor();
        exec.run();
    }

}
