package com.solarwinds.msp.ncentral.eventproduction.sample;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.protobuf.Any;

import com.nable.util.StringUtils;
import com.solarwinds.msp.ncentral.proto.entity.entity.ClientOuterClass;
import com.solarwinds.msp.relay.PublisherGrpc;
import com.solarwinds.msp.relay.PublisherGrpc.PublisherBlockingStub;
import com.solarwinds.msp.relay.PublisherGrpc.PublisherFutureStub;
import com.solarwinds.msp.relay.PublisherGrpc.PublisherStub;
import com.solarwinds.msp.relay.Relay;
import com.solarwinds.msp.relay.Relay.PublishRequest;
import com.solarwinds.msp.relay.Relay.PublishRequest.Builder;
import com.solarwinds.msp.relay.Relay.PublishResponse;
import com.solarwinds.msp.relay.Relay.RequestMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Locale;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;

import io.grpc.ManagedChannel;
import io.grpc.netty.shaded.io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.shaded.io.grpc.netty.NettyChannelBuilder;
import io.grpc.stub.StreamObserver;

/**
 * This is a sample client for dev-testing how MSP Relay works at the moment. It was introduced during initial works on
 * a client for the MSP Relay when it was necessary to test following without a need to deploy N-central server and
 * configure eventing:
 * <ul>
 *     <li>Authentication</li>
 *     <li>Various modes of gRPC communication</li>
 *     <li>Logging gRPC requests</li>
 *     <li>Handling erroneous states (wrong credential, hostname, MSP Relay is down, ...)</li>
 * </ul>
 * <p>
 * Typically, when you need to try anything with MSP Relay, this is your sandbox.
 * <p>
 * In order to use custom (non-default) configuration, use the following command line options:
 * <pre>
 * -Dhost=custom-hostname -Dport=8080 -DclientCertificate=/path/to/certificate/client.crt \
 * -DclientKey=/path/to/certificate/client.pem -DserverCertificate=/path/to/certificate/server.crt \
 * -DoverrideAuthority=custom-hostname
 * </pre>
 */

public class MspRelaySampleClient implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(MspRelaySampleClient.class);

    private String host;
    private int port;
    private String certificate;
    private String privateKey;
    private String serverCertificate;
    private String overrideAuthority;
    private Integer numberOfMessages = 1;
    private ManagedChannel channel;

    private boolean sendPredefinedEvent = false;
    private double avgMsgSize = 0d;
    private int numberOfBatchesPerThread = 1;
    private int failedBatchCount = 0;
    private int numberOfAknow = 0;
    String eventTypeList = "CLIENT";

    public void setEventTypeList(String events) {this.eventTypeList = events;}

    public int getNumberOfBatchesPerThread() {
        return numberOfBatchesPerThread;
    }

    public void setNumberOfBatchesPerThread(int numberOfBatchesPerThread) {
        this.numberOfBatchesPerThread = numberOfBatchesPerThread;
    }

    public void setSendPredefinedEvent(boolean sendPredefinedEvent) {
        this.sendPredefinedEvent = sendPredefinedEvent;
    }

    public Integer getNumberOfMessages() {
        return numberOfMessages;
    }

    public void setNumberOfMessages(Integer numberOfMessages) {
        this.numberOfMessages = numberOfMessages;
    }

    public MspRelaySampleClient() {

    }

    /**
     * Runs the application.
     *
     * @param arguments the application arguments - ignored.
     */
    public static void main(String[] arguments) {
        ThreadPoolExecutor executor = null;
        MspRelaySampleClient client = null;
        try {
            client = new MspRelaySampleClient();
            client.run();
        } catch (Throwable e) {
            logger.error("An error occurred. Exit.", e);
            System.exit(1);
        }
    }

    @Override
    public void run() throws RuntimeException {
        try {
            runit();
        } catch (Throwable t) {
            logger.error("An error occurred. Exit.", t);
            throw new RuntimeException(t);
        } finally {
            //            logger.info("failed messages: {} ", failedBatchCount);
            channel.shutdown();
        }
    }

    private void runit() throws ExecutionException, InterruptedException, IOException {

        logger.trace("Starting the client.");
        channel = getManagedChannel();
        logger.trace("Building request message.");
        final Builder requestBuilder = getRequestMessageBuilder();
        long startTime = System.currentTimeMillis();
        String[] list = null;
        int totalBatchCount = 0;
        if (StringUtils.isNotBlank(eventTypeList)) {
            list = eventTypeList.split(",");
        }
        if (list != null) {
            for (String e : list) {
                for (int i = 0; i < numberOfBatchesPerThread; i++) {

                    EventTypeEnum et = EventTypeEnum.valueOf(e);
                    generateCorrectMessages(requestBuilder, et);
                    try {
                        publishViaFuture(requestBuilder, channel);
                    } catch (Throwable th) {
                        th.printStackTrace();
                        failedBatchCount++;
                    }
                    // make sure it's empty
                    requestBuilder.clearMessages();
                }
            }
        } else {
            for (int i = 0; i < numberOfBatchesPerThread; i++) {
                EventTypeEnum et = EventTypeEnum.valueOf(eventTypeList);
                generateCorrectMessages(requestBuilder, et);
                try {
                    publishViaFuture(requestBuilder, channel);
                } catch (Throwable th) {
                    th.printStackTrace();
                    failedBatchCount++;
                }
                // make sure it's empty
                requestBuilder.clearMessages();
            }
        }
        // Update total batch count
        numberOfBatchesPerThread = numberOfBatchesPerThread * list.length;
        String name = Thread.currentThread().getName();
        logger.info("Thread Pool name: {}" + ", Batches: {}" + ", Messages per batch: {}" + ", Avrage message size: {}"
                        + ", Elapsed time (ms): {}" + ", Messages sent: {}" + ", Batch failed: {}"
                        + ", Acknowledgement received: {}", name, numberOfBatchesPerThread, numberOfMessages, avgMsgSize,
                (System.currentTimeMillis() - startTime), (numberOfMessages * numberOfBatchesPerThread),
                failedBatchCount, numberOfAknow);

    }

    protected void readSystemProperties() {
        //        logger.info("Reading and parsing the command line system properties.");

        final String hostString = System.getProperty("host");
        final String portString = System.getProperty("port");
        final String certificatePath = System.getProperty("clientCertificate");
        final String keyPath = System.getProperty("clientKey");
        final String serverCertificatePath = System.getProperty("serverCertificate");
        final String overrideAuthorityString = System.getProperty("overrideAuthority");

        final String numberOfMessagesLocal = System.getProperty("numberOfMessages");

        //        logger.debug("System properties:\n\thost = {}\n\tport = {}\n\tclientCertificate = {}\n\tclientKey = "
        //                        + "{}\n\tserverCertificate = {}\n\toverrideAuthority = {}", hostString, portString, certificatePath,
        //                keyPath, serverCertificatePath, overrideAuthorityString);

        try {
            host = StringUtils.isNotBlank(hostString) ? hostString.trim() : MspRelaySampleConstant.DEFAULT_SERVER_HOST;
            port = portString != null ? Integer.parseInt(portString) : MspRelaySampleConstant.DEFAULT_SERVER_PORT;
            certificate = StringUtils.isNotBlank(certificatePath) ? MspRelayHelper.readFile(certificatePath) :
                    MspRelaySampleConstant.DEFAULT_CLIENT_CERTIFICATE;
            privateKey = StringUtils.isNotBlank(keyPath) ? MspRelayHelper.readFile(keyPath) :
                    MspRelaySampleConstant.DEFAULT_CLIENT_PRIVATE_KEY;
            serverCertificate = StringUtils.isNotBlank(serverCertificatePath) ? serverCertificatePath :
                    MspRelaySampleConstant.DEFAULT_SERVER_CERTIFICATE_PATH;
            serverCertificate = MspRelayHelper.readFile(serverCertificate);
            overrideAuthority = StringUtils.isNotBlank(overrideAuthorityString) ? overrideAuthorityString.trim() : null;

            if (StringUtils.isNotBlank(numberOfMessagesLocal)) {
                this.numberOfMessages = Integer.valueOf(numberOfMessagesLocal);
            }

            String numberOfBatchesPerThreadStr = System.getProperty("numberOfBatchesPerThread");
            if (StringUtils.isNotBlank(numberOfBatchesPerThreadStr)) {
                numberOfBatchesPerThread = Integer.valueOf(numberOfBatchesPerThreadStr);
            }

            String eventTypeStr = System.getProperty("eventType");
            if (StringUtils.isNotBlank(eventTypeStr)) {
                this.eventTypeList = eventTypeStr.replace(" ", "").toUpperCase(Locale.ROOT);
                if ("ALL".equals(this.eventTypeList)) {
                    EventTypeEnum[] events = EventTypeEnum.values();
                    StringBuilder sb = new StringBuilder();
                    for (EventTypeEnum t : events) {
                        if (t.equals(EventTypeEnum.ALL) || t.equals(EventTypeEnum.MIXED) || t.equals(
                                EventTypeEnum.RANDOM)) {
                            continue;
                        }
                        sb.append(t.name());
                        sb.append(",");

                    }
                    // remove the last comma
                    int ind = sb.lastIndexOf(",");
                    sb.delete(ind, ind + 1);
                    this.eventTypeList = sb.toString();
                }
            }

        } catch (Exception e) {
            logger.info("host: {}", host);
            logger.info("port: {}", port);
            logger.info("override authority: {}", overrideAuthority);
            logger.info("client certificate:\n{}", certificate);
            logger.info("client private key:\n{}", privateKey);
            logger.info("server certificate:\n{}", serverCertificate);
            logger.info("eventType:\n{}", this.eventTypeList);
            logger.info("numberof batches:\n{}", this.numberOfBatchesPerThread);
            logger.info("number of messages:\n{}", this.numberOfMessages);
            throw new IllegalArgumentException("Cannot parse system properties. " + MspRelaySampleConstant.HELP, e);
        }

    }

    public int getActualPatchCount() {
        return (numberOfBatchesPerThread * eventTypeList.split(",").length);
    }

    private ManagedChannel getManagedChannel() throws IOException {

        final NettyChannelBuilder nettyChannelBuilder = NettyChannelBuilder.forAddress(host, port)
                .sslContext(GrpcSslContexts.forClient()
                        .keyManager(MspRelayHelper.getInputStream(certificate),
                                MspRelayHelper.getInputStream(privateKey))
                        .trustManager(MspRelayHelper.getInputStream(serverCertificate))
                        .build());
        if (StringUtils.isNotBlank(overrideAuthority)) {
            nettyChannelBuilder.overrideAuthority(overrideAuthority);
        }
        return nettyChannelBuilder.build();

        //ManagedChannelBuilder.keepAliveTime(), keepAliveTimeout(), and keepAliveWithoutCalls()
    }

    private Builder getRequestMessageBuilder() {
        //        final Context context = Context.newBuilder()
        //                .setBizappsCustomerId(BUSINESS_APPLICATIONS_CUSTOMER_ID)
        //                .setSystemGuid(N_CENTRAL_SYSTEM_GUID)
        //                .build();
        final Builder requestBuilder = PublishRequest.newBuilder(); //.setContext(context);
        //logger.info("Message to be sent to MSP Event Relay:\n{}\n", requestBuilder.toString());
        return requestBuilder;
    }

    private void generateCorrectMessages(Builder requestBuilder, EventTypeEnum eventTypeEnum) {
        generateMessages(requestBuilder, eventTypeEnum);
    }

    private void generateIncorrectMessages(Builder requestBuilder, EventTypeEnum eventTypeEnum) {
        generateMessages(requestBuilder, eventTypeEnum);
    }

    private void generateMessages(Builder requestBuilder, EventTypeEnum eventTypeEnum) {
        final Random random = new Random();
        int randomNum = random.nextInt(numberOfMessages);
        for (int requestMessageCnt = 1; requestMessageCnt <= numberOfMessages; requestMessageCnt++) {

            if (sendPredefinedEvent == false) {
                final int messageId = random.nextInt();
                final int clientId = random.nextInt();
                final ClientOuterClass.Client client = ClientOuterClass.Client.newBuilder()
                        .setClientId(clientId)
                        .setName(String.format("%s [clientId=%d]", MspRelaySampleConstant.CLIENT_NAME_PREFIX, clientId))
                        .setParentId(MspRelaySampleConstant.CLIENT_PARENT_ID)
                        .build();
                requestBuilder.addMessages(RequestMessage.newBuilder()
                        .setId(messageId)
                        .setEventType(eventTypeEnum.name())
                        .setPayload(Any.pack(client))
                        .build());
            } else {
                RequestMessage b = null;

                switch (eventTypeEnum) {
                    case CLIENT: {

                        b = RequestMessage.newBuilder()
                                .setId(random.nextInt())
                                .setEventType(MspRelayBuildEvent.ValidClientEvent)
                                .setPayload(Any.pack(MspRelayBuildEvent.ValidClientEventObject))
                                .build();
                        requestBuilder.addMessages(b);

                        break;
                    }

                    case DEVICE: {
                        b = RequestMessage.newBuilder()
                                .setId(random.nextInt())
                                .setEventType(MspRelayBuildEvent.validDeviceEvent)
                                .setPayload(Any.pack(MspRelayBuildEvent.validDeviceEventObject))
                                .build();
                        requestBuilder.addMessages(b);
                        break;
                    }

                    case TASKTHRESHOLD: {
                        b = RequestMessage.newBuilder()
                                .setId(random.nextInt())
                                .setEventType(MspRelayBuildEvent.taskThreshold)
                                .setPayload(Any.pack(MspRelayBuildEvent.taskThresholdObject))
                                .build();
                        requestBuilder.addMessages(b);
                        break;
                    }
                    case USER: {
                        b = RequestMessage.newBuilder()
                                .setId(random.nextInt())
                                .setEventType(MspRelayBuildEvent.user)
                                .setPayload(Any.pack(MspRelayBuildEvent.userObject))
                                .build();
                        requestBuilder.addMessages(b);
                        break;
                    }
                    case RULECUSTOMER: {
                        b = RequestMessage.newBuilder()
                                .setId(random.nextInt())
                                .setEventType(MspRelayBuildEvent.ruleCustomer)
                                .setPayload(Any.pack(MspRelayBuildEvent.ruleCustomerObject))
                                .build();
                        requestBuilder.addMessages(b);
                        break;
                    }
                    case FIREWALLINCIDENTS: {
                        b = RequestMessage.newBuilder()
                                .setId(random.nextInt())
                                .setEventType(MspRelayBuildEvent.firewallIncidents)
                                .setPayload(Any.pack(MspRelayBuildEvent.firewallIncidentsObject))
                                .build();
                        requestBuilder.addMessages(b);
                        break;
                    }
                    case APPLICATIONSTATUS: {
                        b = RequestMessage.newBuilder()
                                .setId(random.nextInt())
                                .setEventType(MspRelayBuildEvent.applicationStatus)
                                .setPayload(Any.pack(MspRelayBuildEvent.mappedDriveObject))
                                .build();
                        requestBuilder.addMessages(b);
                        break;
                    }
                    case ASSETMEDIAACCESS: {
                        b = RequestMessage.newBuilder()
                                .setId(random.nextInt())
                                .setEventType(MspRelayBuildEvent.assetMediaAccess)
                                .setPayload(Any.pack(MspRelayBuildEvent.assetMediaAccessObject))
                                .build();
                        requestBuilder.addMessages(b);
                        break;
                    }
                    case DEVICEASSET: {
                        b = RequestMessage.newBuilder()
                                .setId(random.nextInt())
                                .setEventType(MspRelayBuildEvent.deviceAsset)
                                .setPayload(Any.pack(MspRelayBuildEvent.deviceAssetObject))
                                .build();
                        requestBuilder.addMessages(b);
                        break;
                    }
                    case LOGICALDEVICE: {
                        b = RequestMessage.newBuilder()
                                .setId(random.nextInt())
                                .setEventType(MspRelayBuildEvent.logicalDevice)
                                .setPayload(Any.pack(MspRelayBuildEvent.logicalDeviceObject))
                                .build();
                        requestBuilder.addMessages(b);
                        break;
                    }
                    case MAPPEDDRIVE: {
                        b = RequestMessage.newBuilder()
                                .setId(random.nextInt())
                                .setEventType(MspRelayBuildEvent.mappedDrive)
                                .setPayload(Any.pack(MspRelayBuildEvent.mappedDriveObject))
                                .build();
                        requestBuilder.addMessages(b);
                        break;
                    }
                    case NETWORKADAPTERCONFIGURATION: {
                        b = RequestMessage.newBuilder()
                                .setId(random.nextInt())
                                .setEventType(MspRelayBuildEvent.networkAdapterConfiguration)
                                .setPayload(Any.pack(MspRelayBuildEvent.networkAdapterConfigurationObject))
                                .build();
                        requestBuilder.addMessages(b);
                        break;
                    }
                    case NETWORKADAPTER: {
                        b = RequestMessage.newBuilder()
                                .setId(random.nextInt())
                                .setEventType(MspRelayBuildEvent.networkAdapter)
                                .setPayload(Any.pack(MspRelayBuildEvent.networkAdapterObject))
                                .build();
                        requestBuilder.addMessages(b);
                        break;
                    }
                    case SYSTEMSERVICE: {
                        b = RequestMessage.newBuilder()
                                .setId(random.nextInt())
                                .setEventType(MspRelayBuildEvent.systemService)
                                .setPayload(Any.pack(MspRelayBuildEvent.systemServiceObject))
                                .build();
                        requestBuilder.addMessages(b);
                        break;
                    }
                    case VIDEOCONTROLLER: {
                        b = RequestMessage.newBuilder()
                                .setId(random.nextInt())
                                .setEventType(MspRelayBuildEvent.videoController)
                                .setPayload(Any.pack(MspRelayBuildEvent.videoControllerObject))
                                .build();
                        requestBuilder.addMessages(b);
                        break;
                    }
                    case PORTSTATUS: {
                        b = RequestMessage.newBuilder()
                                .setId(random.nextInt())
                                .setEventType(MspRelayBuildEvent.portStatus)
                                .setPayload(Any.pack(MspRelayBuildEvent.portStatusObject))
                                .build();
                        requestBuilder.addMessages(b);
                        break;
                    }
                    case NOTIFCATIONPROFILE: {
                        b = RequestMessage.newBuilder()
                                .setId(random.nextInt())
                                .setEventType(MspRelayBuildEvent.notificationProfile)
                                .setPayload(Any.pack(MspRelayBuildEvent.notificationProfileObject))
                                .build();
                        requestBuilder.addMessages(b);
                        break;
                    }
                    case SHAREDFOLDERS: {
                        b = RequestMessage.newBuilder()
                                .setId(random.nextInt())
                                .setEventType(MspRelayBuildEvent.sharedFolders)
                                .setPayload(Any.pack(MspRelayBuildEvent.sharedFoldersObject))
                                .build();
                        requestBuilder.addMessages(b);
                        break;
                    }
                    case APPLIANCE: {
                        b = RequestMessage.newBuilder()
                                .setId(random.nextInt())
                                .setEventType(MspRelayBuildEvent.appliance)
                                .setPayload(Any.pack(MspRelayBuildEvent.applianceObject))
                                .build();
                        requestBuilder.addMessages(b);
                        break;
                    }
                    case PATCH: {
                        b = RequestMessage.newBuilder()
                                .setId(random.nextInt())
                                .setEventType(MspRelayBuildEvent.patch)
                                .setPayload(Any.pack(MspRelayBuildEvent.patchObject))
                                .build();
                        requestBuilder.addMessages(b);
                        break;
                    }
                    case PATCHPRODUCT: {
                        b = RequestMessage.newBuilder()
                                .setId(random.nextInt())
                                .setEventType(MspRelayBuildEvent.patchProduct)
                                .setPayload(Any.pack(MspRelayBuildEvent.patchProductObject))
                                .build();
                        requestBuilder.addMessages(b);
                        break;
                    }
                    case GENERICSERVICEDATA: {
                        b = RequestMessage.newBuilder()
                                .setId(random.nextInt())
                                .setEventType(MspRelayBuildEvent.genericServiceData)
                                .setPayload(Any.pack(MspRelayBuildEvent.genericServiceDataObject))
                                .build();
                        requestBuilder.addMessages(b);
                        break;
                    }

                    case MIXED: {
                        b = RequestMessage.newBuilder()
                                .setId(random.nextInt())
                                .setEventType(MspRelayBuildEvent.ValidClientEvent)
                                .setPayload(Any.pack(MspRelayBuildEvent.ValidClientEventObject))
                                .build();
                        requestBuilder.addMessages(b);

                        RequestMessage b2 = RequestMessage.newBuilder()
                                .setId(random.nextInt())
                                .setEventType(MspRelayBuildEvent.validDeviceEvent)
                                .setPayload(Any.pack(MspRelayBuildEvent.validDeviceEventObject))
                                .build();
                        requestBuilder.addMessages(b2);

                        RequestMessage b3 = RequestMessage.newBuilder()
                                .setId(random.nextInt())
                                .setEventType(MspRelayBuildEvent.taskThreshold)
                                .setPayload(Any.pack(MspRelayBuildEvent.taskThresholdObject))
                                .build();
                        requestBuilder.addMessages(b3);

                        RequestMessage b4 = RequestMessage.newBuilder()
                                .setId(random.nextInt())
                                .setEventType(MspRelayBuildEvent.ruleCustomer)
                                .setPayload(Any.pack(MspRelayBuildEvent.ruleCustomerObject))
                                .build();
                        requestBuilder.addMessages(b4);

                        RequestMessage b5 = RequestMessage.newBuilder()
                                .setId(random.nextInt())
                                .setEventType(MspRelayBuildEvent.firewallIncidents)
                                .setPayload(Any.pack(MspRelayBuildEvent.firewallIncidentsObject))
                                .build();
                        requestBuilder.addMessages(b5);

                        RequestMessage b6 = RequestMessage.newBuilder()
                                .setId(random.nextInt())
                                .setEventType(MspRelayBuildEvent.firewallIncidents)
                                .setPayload(Any.pack(MspRelayBuildEvent.firewallIncidentsObject))
                                .build();
                        requestBuilder.addMessages(b6);

                    }
                    default: {
                        logger.info("Invalid event type: {}", eventTypeEnum.name());
                    }
                }
                if (requestMessageCnt == randomNum && b != null) {
                    avgMsgSize = b.getSerializedSize();
                }

            }
        }
    }

    private void publishViaBlockingCall(Builder requestBuilder, ManagedChannel channel) {
        final PublisherBlockingStub blockingStub = PublisherGrpc.newBlockingStub(channel);
        final PublishResponse response = blockingStub.publish(requestBuilder.build());
        logger.info("Blocking call ### Response:\n{}\n", response.toString());
    }

    private void publishViaStream(Builder requestBuilder, ManagedChannel channel) {
        final PublisherStub asynchronousStub = PublisherGrpc.newStub(channel);
        logger.info("Stream call ### >>> START\n");
        StreamObserver<PublishResponse> responseObserver = new StreamObserver<PublishResponse>() {
            @Override
            public void onNext(PublishResponse publishResponse) {
                logger.info("Response:\n{}\n\n===", publishResponse.toString());
            }

            @Override
            public void onError(Throwable throwable) {
                logger.error("An error occurred on response.", throwable);
            }

            @Override
            public void onCompleted() {
                logger.info("<<< END\n");
            }
        };
        asynchronousStub.publish(requestBuilder.build(), responseObserver);
    }

    private void publishViaFuture(Builder requestBuilder, ManagedChannel channel)
            throws InterruptedException, ExecutionException {
        final PublisherFutureStub futureStub = PublisherGrpc.newFutureStub(channel);
        final ListenableFuture<PublishResponse> responseFuture = futureStub.publish(requestBuilder.build());
        try {
            java.util.List<Relay.ResponseMessage> results = responseFuture.get().getResponseMessagesList();
            for (Relay.ResponseMessage msg : results) {
                if (msg.getStatusValue() != 1) {
                    logger.info("result: {} {}", msg.getStatus(), msg.getStatusValue());
                } else {
                    numberOfAknow++;
                }
            }

        } catch (Throwable ignored) {
            //logger.info("intentionally ignoring response status exception");
            ignored.printStackTrace();
        }
        //logger.info("Future non-blocking call ### Response:\n{}\n", responseFuture.get().toString());
    }

}
