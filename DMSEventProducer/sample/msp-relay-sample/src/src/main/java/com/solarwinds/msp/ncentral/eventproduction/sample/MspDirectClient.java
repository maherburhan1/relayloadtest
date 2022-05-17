package com.solarwinds.msp.ncentral.eventproduction.sample;

import com.google.protobuf.Any;

import com.solarwinds.core.MSPEventProducer;
import com.solarwinds.core.producer.EventProducerConfig;
import com.solarwinds.core.serialization.ByteArraySerializer;
import com.solarwinds.entities.Environment;
import com.solarwinds.msp.ncentral.proto.entity.entity.ClientOuterClass;
import com.solarwinds.msp.ncentral.proto.entity.entity.DeviceOuterClass;
import com.solarwinds.msp.relay.Relay;

import org.apache.kafka.clients.producer.RecordMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import Msp.EventContextOuterClass;
import Msp.EventOuterClass;

public class MspDirectClient {

    private List<MSPEventProducer> producers = new ArrayList<MSPEventProducer>();
    final Random random = new Random();
    private static final Logger logger = LoggerFactory.getLogger(MspDirectClient.class);
    String username;
    String password;
    String trustStorePassword;
    String clientId;
    String streamPrefix;
    String jkslocation;
    long PUBLISH_TIMEOUT_SECONDS = 30;
    static int batchSize = 100000;
    static int currentCnt = 0;
    static int totalCount = 0;
    static int currentErrCnt = 0;
    long startTime = 0l;
    long elapsedtime = 0l;

    public MspDirectClient() {

    }

    private void init() {
        username = System.getProperty("username");
        password = System.getProperty("password");
        trustStorePassword = System.getProperty("EVENTBUS_TRUST_STORE_PASSWORD");
        clientId = System.getProperty("clientId");
        streamPrefix = System.getProperty("streamprefix"); // nonprod.eu1.ncentral
        jkslocation = System.getProperty("jkslocation");

        logger.info("Env:\nCluster User: {}\nJKS: {}\nStreamPrefix{}\n", username, jkslocation, streamPrefix);

    }

    public static void main(String[] arguments) {

        /*export EVENTBUS_TRUST_STORE_PASSWORD=h0rS3s
        export username=ddveml
        export password=deDhHZzFvVr1Ogc
        export trustStorePassword=h0rS3s
        export clientId=
                export streamPrefix= */
        System.setProperty("username", "ddveml");
        System.setProperty("password", "deDhHZzFvVr1Ogc");
        System.setProperty("EVENTBUS_TRUST_STORE_PASSWORD", "h0rS3s");
        System.setProperty("clientId", "Directclient-1");
        System.setProperty("streamprefix", "nonprod.eu1.ncentral."); // nonprod.eu1.ncentral
        System.setProperty("jkslocation",
                "/Users/maherburhan/Documents/development/java/n-central2/n-central/frameworks/DMSEventProducer/sample/msp-relay-sample/keystore2.jks");

        String username = System.getProperty("username");
        String password = System.getProperty("password");
        String trustStorePassword = System.getProperty("EVENTBUS_TRUST_STORE_PASSWORD");
        String clientId = System.getProperty("clientId");
        String streamPrefix = System.getProperty("streamprefix"); // nonprod.eu1.ncentral
        String jkslocation = System.getProperty("jkslocation");

        MspDirectClient client = new MspDirectClient();
        client.start(false);
    }

    public void start(boolean checkStatus) {
        try {
            List<CompletableFuture<RecordMetadata>> collection = new ArrayList<>();
            init();
            int cnt = 0;
            try {
                EventProducerConfig conf =
                        new EventProducerConfig(clientId, username, password, ByteArraySerializer.class,
                                Environment.Stage);
                conf.setSSL(jkslocation, trustStorePassword);
                MSPEventProducer<byte[]> clientP = new MSPEventProducer<>(streamPrefix + "entity.Client", conf);
                startTime = System.currentTimeMillis();
                //while (!Thread.currentThread().isInterrupted()) {
                while (totalCount < 1000000) {
                    if (checkStatus == true) {
                        RecordMetadata t = publish(clientP, generateClientEvent().toByteArray()).whenComplete(
                                MspDirectClient::printPublicationResult).get(PUBLISH_TIMEOUT_SECONDS, TimeUnit.SECONDS);

                    } else {
                        totalCount++;
                        currentCnt++;
                        CompletableFuture<RecordMetadata> t = publish(clientP, generateClientEvent().toByteArray());
                        collection.add(t);
                        if (currentCnt >= batchSize) {
                            // check status
                            Thread thread = new Thread(new HandleProducerResult(collection));
                            thread.start();
                            collection.clear();
                            elapsedtime = System.currentTimeMillis() - startTime;
                            logger.info("Processed {} in {} MS. Total messages processed: {}", currentCnt, elapsedtime,
                                    totalCount);
                            startTime = System.currentTimeMillis();
                            currentCnt = 0;
                        }
                    }
                }
                //}
                logger.info("Done!");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    static void printPublicationResult(RecordMetadata result, Throwable error) {
        currentCnt++;
        if (currentCnt >= batchSize) {
            totalCount += currentCnt;
            currentCnt = 0; // reset
            currentErrCnt = 0;

            logger.info("number of messages processed: {}, with {} errors, target topic {}", currentCnt, currentErrCnt,
                    result.topic());
        }
        if (error != null) {
            currentErrCnt++;
            logger.error("publication failed due to: " + error);
            error.printStackTrace();
        } else {
            logger.info("Cuurent count {}, total count {} ", currentCnt, totalCount);
            //logger.info("successfully published event with metadata: " + result);
        }

    }

    public CompletableFuture<RecordMetadata> publish(final MSPEventProducer<byte[]> producer, byte[] event) {

        return CompletableFuture.supplyAsync(() -> {
            try {

                return producer.send(event).get(30, TimeUnit.SECONDS);
            } catch (InterruptedException | ExecutionException | TimeoutException e) {
                throw new RuntimeException("failed to publish", e);
            }
        });
    }

    public CompletableFuture<RecordMetadata> publish(final MSPEventProducer<ClientOuterClass.Client> producer,
            ClientOuterClass.Client event) {

        return CompletableFuture.supplyAsync(() -> {
            try {

                return producer.send(event).get(30, TimeUnit.SECONDS);
            } catch (InterruptedException | ExecutionException | TimeoutException e) {
                throw new RuntimeException("failed to publish", e);
            }
        });
    }

    public CompletableFuture<RecordMetadata> publish(final MSPEventProducer<DeviceOuterClass.Device> producer,
            final DeviceOuterClass.Device event) {

        return CompletableFuture.supplyAsync(() -> {
            try {
                return producer.send(event).get(30, TimeUnit.SECONDS);
            } catch (InterruptedException | ExecutionException | TimeoutException e) {
                throw new RuntimeException("failed to publish", e);
            }
        });
    }

    protected ClientOuterClass.Client generateClientEvent() {

        ClientOuterClass.Client c = MspRelayBuildEvent.ValidClientEventObject;
        return c;
    }

    private EventOuterClass.Event generateDeviceEvent() {

        Relay.RequestMessage b = Relay.RequestMessage.newBuilder()
                .setId(random.nextInt())
                .setEventType(MspRelayBuildEvent.validDeviceEvent)
                .setPayload(Any.pack(MspRelayBuildEvent.validDeviceEventObject))
                .build();
        return EventOuterClass.Event.newBuilder()
                .setEventContext(EventContextOuterClass.EventContext.newBuilder()
                        .setEventId("sample-event" + ThreadLocalRandom.current().nextLong())
                        .build())
                .setPayload(b.getPayload().getValue())
                .build();
    }

    public class HandleProducerResult implements Runnable {
        List<CompletableFuture<RecordMetadata>> theList = new ArrayList<>();

        public HandleProducerResult(List<CompletableFuture<RecordMetadata>> list) {
            this.theList.addAll(list);
        }

        public void handle(List<CompletableFuture<RecordMetadata>> list) {
            try {
                for (CompletableFuture<RecordMetadata> r : list) {
                    r.get();
                }

            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            String name = Thread.currentThread().getName();
            logger.info("Checking acknowledgements for thread {} ", name);
            for (CompletableFuture<RecordMetadata> o : theList) {
                try {

                    logger.info("Status: {} ", o.get().topic());
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

