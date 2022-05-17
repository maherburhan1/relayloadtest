package com.solarwinds.msp.ncentral.eventproduction.sample;

import com.solarwinds.msp.ncentral.proto.entity.entity.ClientOuterClass;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.security.plain.PlainLoginModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class DirectKafkaProducer implements Runnable {
    String appId = UUID.randomUUID().toString();
    String clientId = UUID.randomUUID().toString();
    String transactionId = UUID.randomUUID().toString();

    private final Properties props = new Properties();
    
    private static final Logger logger = LoggerFactory.getLogger(MspDirectClient.class);
    private int numberOfMessages = 0;

    //String topicName = "eu.ncentral.entity.Client";
    String topicName = "eu.ncentral.entity.Client.8partitions";
    int transactional = 0;

    public DirectKafkaProducer setNumberOfMessages(int n) {
        this.numberOfMessages = n;
        return this;
    }

    protected DirectKafkaProducer() {
        //Assign localhost id
        props.put("bootstrap.servers",
                "stg-morbius-0.eb.stg.davinci.system-monitor.com:443, stg-morbius-1.eb.stg.davinci.system-monitor.com:443, stg-morbius-2.eb.stg.davinci.system-monitor.com:443");

        //Set acknowledgements for producer requests.
        props.put("acks", "all");

        //If the request fails, the producer can automatically retry,
        props.put("retries", 0);

        //Specify buffer size in config
        props.put("batch.size", 16384);

        //Reduce the no of requests less than 0
        props.put("linger.ms", 1);

        //The buffer.memory controls the total amount of memory available to the producer for buffering.
        props.put("buffer.memory", 33554432);

        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");

        props.put("value.serializer", "org.apache.kafka.common.serialization.ByteArraySerializer");

        props.put("ssl.truststore.location",
                "/Users/maherburhan/Documents/development/java/n-central2/n-central/frameworks/DMSEventProducer/sample/msp-relay-sample/keystore2.jks");
        props.put("ssl.truststore.password", "h0rS3s");
        props.put("ssl.truststore.type", "JKS");
        props.put("ssl.keystore.type", "JKS");
        props.put("ssl.keymanager.algorithm", "SunX509");
        props.put("sasl.mechanism", "PLAIN");
        props.put("security.protocol", "SASL_SSL");

        props.setProperty("client.id", clientId);

        String userName = "ddveml";
        String pwd = "deDhHZzFvVr1Ogc";

        //        props.put("username", "ddveml");
        //        props.put("password", "deDhHZzFvVr1Ogc");

        //props.put("security.protocol", "SASL_PLAINTEXT");
        props.put("sasl.mechanism", "PLAIN");
        props.put("sasl.jaas.config",
                PlainLoginModule.class.getName() + " required username=\"" + userName + "\" password=\"" + pwd + "\";");
    }

    private Producer<String, byte[]> getProducer() {
        Producer<String, byte[]> producer = new KafkaProducer<String, byte[]>(props);
        return producer;
    }

    public DirectKafkaProducer setTransactional(int flag) {
        transactional = flag;
        return this;
    }

    public void runNoTransaction(int numberOfMsgs) throws ExecutionException, InterruptedException {
        MspDirectClient dClient = new MspDirectClient();
        props.getProperty("enable.auto.commit", "true");
        Producer<String, byte[]> producer = getProducer();
        List<Future<RecordMetadata>> futures = new ArrayList<>();
        long startTime = System.currentTimeMillis();
        try {
            for (int i = 0; i < numberOfMsgs; i++) {
                ClientOuterClass.Client c = dClient.generateClientEvent();
                Future<RecordMetadata> f = producer.send(
                        new ProducerRecord<String, byte[]>(topicName, Integer.toString(i), c.toByteArray()));
                futures.add(f);
            }
            producer.close();
            HandleProducerResult t = new HandleProducerResult(futures);
            t.run();
        } catch (Throwable t) {
            throw t;
        }
        long endTime = System.currentTimeMillis();
        logger.info("Done, elapsed time: {}", (endTime - startTime));
    }

    public void runWithTransaction(int numberOfMsgs) throws ExecutionException, InterruptedException {
        MspDirectClient dClient = new MspDirectClient();
        props.getProperty("enable.auto.commit", "false");
        //props.setProperty("enable.idempotency", "false");
        //props.setProperty("message.send.max.retries", "3");
        props.setProperty("retries", "3");
        props.setProperty("application.id", appId);
        props.setProperty("transactional.id", transactionId);
        Producer<String, byte[]> producer = getProducer();

        producer.initTransactions();
        long startTime = System.currentTimeMillis();
        logger.info("Begin transaction...");
        producer.beginTransaction();
        try {
            for (int i = 0; i < numberOfMsgs; i++) {
                ClientOuterClass.Client c = dClient.generateClientEvent();
                Future<RecordMetadata> f = producer.send(
                        new ProducerRecord<String, byte[]>(topicName, Integer.toString(i), c.toByteArray()));

            }
            logger.info("End transaction");
            producer.commitTransaction();
        } catch (Throwable t) {
            producer.abortTransaction();
            producer.flush();
        } finally {
            producer.close();
        }
        long endTime = System.currentTimeMillis();
        logger.info("Done, elapsed time: {}", (endTime - startTime));

    }

    public static void main(String[] args) throws Exception {
        long startTime = 0l;
        long endTime = 0l;
        if (System.getProperty("numberOfThreads") == null) {
            System.setProperty("numberOfThreads", "10");
        }
        int numberOfThreads = Integer.valueOf(System.getProperty("numberOfThreads"));

        if (System.getProperty("numberOfMessages") == null) {
            System.setProperty("numberOfMessages", "100000");
        }
        int numberOfMessages = Integer.valueOf(System.getProperty("numberOfMessages"));
        if (System.getProperty("transactional") == null) {
            System.setProperty("transactional", "0");
        }
        int transactional = Integer.valueOf(System.getProperty("transactional"));

        while (true) {
            int totalNumberOfMessages = 0;
            startTime = System.currentTimeMillis();
            ThreadPoolExecutor threadPool = (ThreadPoolExecutor) Executors.newFixedThreadPool(numberOfThreads);

            List<DirectKafkaProducer> clientArray = new ArrayList<DirectKafkaProducer>();
            for (int i = 0; i < numberOfThreads; i++) {
                DirectKafkaProducer client = DirectKafkaProducerBuilder.build()
                        .setNumberOfMessages(numberOfMessages)
                        .setTransactional(transactional);
                clientArray.add(client);
            }
            startTime = System.currentTimeMillis();
            logger.info("Start time: {}", startTime);
            int clientMsgs = 0;
            for (DirectKafkaProducer client : clientArray) {
                threadPool.execute(client);
            }

            do {
                //logger.info(">>>>>>>>>>>>>> checking Progress every 1 second >>>>>>>>>>>>>>>>>>>");
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } while (threadPool.getActiveCount() != 0);
            totalNumberOfMessages = numberOfMessages * numberOfThreads;
            endTime = System.currentTimeMillis();
            long elapsedTime = endTime - startTime;
            logger.info("<<<<<<<<<<<<<<<<<< Completed in {} ms with total of {} messages >>>>>>>>>>>>>>>>>>>>>",
                    elapsedTime, totalNumberOfMessages);

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

    @Override
    public void run() {
        try {
            if (transactional == 0) {
                runNoTransaction(numberOfMessages);
            } else {
                runWithTransaction(numberOfMessages);
            }
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public class HandleProducerResult implements Runnable {
        List<Future<RecordMetadata>> theList = new ArrayList<>();

        public HandleProducerResult(List<Future<RecordMetadata>> list) {
            this.theList.addAll(list);
        }

        @Override
        public void run() {
            String name = Thread.currentThread().getName();
            logger.info("Checking acknowledgements for thread {} ", name);
            for (Future<RecordMetadata> o : theList) {
                try {

                    o.get().topic();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
            }
            logger.info("Checking acknowledgements for thread {} is done.", name);
        }
    }
}
