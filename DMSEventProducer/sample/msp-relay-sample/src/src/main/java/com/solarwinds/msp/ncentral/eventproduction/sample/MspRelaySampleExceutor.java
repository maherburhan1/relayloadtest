package com.solarwinds.msp.ncentral.eventproduction.sample;

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

    public int getBatchSize() {
        return batchSize;
    }

    public void setBatchSize(int batchSize) {
        this.batchSize = batchSize;
    }

    public int getNumberOfMessagesPerBatch() {
        return numberOfMessagesPerBatch;
    }

    public void setNumberOfMessagesPerBatch(int numberOfMessagesPerBatch) {
        this.numberOfMessagesPerBatch = numberOfMessagesPerBatch;
    }

    private static final Logger logger = LoggerFactory.getLogger(MspRelaySampleExceutor.class);

    MspRelaySampleExceutor(int numberOfThreads) {
        this.numberOfThreads = numberOfThreads;

    }

    MspRelaySampleExceutor() {
    }

    public void run() {

        while (true) {
            int totalNumberOfMessages = 0;
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
                String eventTypeName = client.getEventTypeEnum().name();
                totalNumberOfMessages += client.getNumberOfBatchesPerThread() * ("ALL".equals(eventTypeName) ?
                        client.getNumberOfMessages() * 2 :
                        client.getNumberOfMessages()); // sending to message type if type is ALL

            }

            do {
                //logger.info(">>>>>>>>>>>>>> checking Progress every 1 second >>>>>>>>>>>>>>>>>>>");
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } while (threadPool.getActiveCount() != 0);

            logger.info("<<<<<<<<<<<<<<<<<< Completed with total of {} messages >>>>>>>>>>>>>>>>>>>>>",
                    totalNumberOfMessages);

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
    }

    public static void main(String[] arguments) {
        long startTime = System.currentTimeMillis();
        if (System.getProperty("numberOfThreads") == null) {
            System.setProperty("numberOfThreads", "1");
        }
        if (System.getProperty("numberOfMessages") == null) {
            System.setProperty("numberOfMessages", "1");
        }
        if (System.getProperty("numberOfBatchesPerThread") == null) {
            System.setProperty("numberOfBatchesPerThread", "5");
        }

        if (System.getProperty("eventType") == null) {
            System.setProperty("eventType", EventTypeEnum.DEVICE.name());
        }
        MspRelaySampleExceutor exec = MspRelaySampleClientBuilder.buildMspRelaySampleExceutor();
        exec.run();
    }

}
