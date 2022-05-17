package com.solarwinds.msp.ncentral.eventproduction.adapter.eventbus.client;

import com.google.protobuf.ByteString;
import com.google.protobuf.GeneratedMessageV3;

import com.nable.util.StringUtils;
import com.solarwinds.core.MSPEventConsumer;
import com.solarwinds.core.consumer.EventConsumerConfig;
import com.solarwinds.entities.Environment;
import com.solarwinds.msp.eventbus.EventBusRecord;
import com.solarwinds.msp.eventbus.EventBusRecords;
import com.solarwinds.msp.ncentral.eventproduction.adapter.eventbus.configuration.EventBusConfig;

import org.apache.commons.lang3.reflect.MethodUtils;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.util.Date;
import java.util.concurrent.ExecutionException;

import Msp.EventOuterClass.Event;

public class Consumer implements AutoCloseable {
    private static final Logger LOGGER = LoggerFactory.getLogger(Consumer.class);

    private final EventBusConfig conf;
    private final String idFilter;
    private final String streamName;
    private final Class msgClass;
    private final String entityName;
    private final int pollTimeout;

    private MSPEventConsumer<byte[]> eventConsumer;

    Consumer(EventBusConfig conf, String filter, String entityName, Class msgClass) {
        this.conf = conf;
        this.entityName = entityName;
        this.msgClass = msgClass;
        idFilter = filter;
        pollTimeout = conf.getPollTimeout();
        streamName = conf.getStreamNamePrefix() + entityName;
    }

    void initConsumer(String overrideId) throws MalformedURLException {
        Environment eventBusEnvironment = conf.getEnvironment();

        String id = overrideId;
        if (id == null) {
            id = conf.getGroupId();
        }
        id = id + "-" + entityName;

        String uniqueClientIdForEvent = conf.getClientId() + "-" + entityName;
        EventConsumerConfig eventConsumerConfig =
                new EventConsumerConfig(id, uniqueClientIdForEvent, conf.getUsername(), conf.getPassword(),
                        ByteArrayDeserializer.class, eventBusEnvironment);
        eventConsumerConfig.setSSL(conf.getTrustStoreLocation(), conf.getTrustStorePassword());

        LOGGER.info("Viewing stream: " + streamName);
        eventConsumer = new MSPEventConsumer<>(streamName, eventConsumerConfig);
    }

    private void process(EventBusRecord<String, byte[]> consumerRecord) {
        synchronized (Consumer.class) {
            if (consumerRecord.value() == null) {
                LOGGER.info("Received a NULL event, ignoring");
            } else {
                GeneratedMessageV3 parsedRecord = parseRecord(consumerRecord);

                if (parsedRecord == null) {
                    LOGGER.info("Couldn't get event from received data, size " + consumerRecord.value().length + ".");
                    return;
                }

                if (checkMatchFilter(parsedRecord)) {
                    LOGGER.info("Received event(size " + consumerRecord.value().length + ") on " + streamName + ":");
                    LOGGER.info("Source Stream   :         {}", consumerRecord.stream());
                    LOGGER.info("Record timestamp:         {}", new Date(consumerRecord.timestamp()));
                    LOGGER.info("Record key:               {}", consumerRecord.key());
                    LOGGER.info("N-central Event:          {}", parsedRecord);

                }
            }
        }
    }

    private boolean checkMatchFilter(GeneratedMessageV3 entity) {
        if (StringUtils.isEmpty(idFilter) || entity == null) {
            return true;
        }

        try {
            Method m = entity.getClass().getMethod("getContext");
            Object context = m.invoke(entity);
            if (context == null) {
                LOGGER.warn("MSPContext returned NULL.");
            } else {
                String bizAppsCustomerId = (String) MethodUtils.invokeMethod(context, "getBizAppsCustomerID");
                String systemGuid = (String) MethodUtils.invokeMethod(context, "getSystemGUID");

                return (bizAppsCustomerId.equals(idFilter) || systemGuid.equals(idFilter));
            }
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            LOGGER.warn("Could not get method to filter records by BizAppsCustomerId or SystemGUID.", e);
        }

        return true;
    }

    private void checkpoint() {
        eventConsumer.commit();
    }

    private GeneratedMessageV3 parseRecord(EventBusRecord<String, byte[]> consumerRecord) {
        try {
            GeneratedMessageV3 parsedMessage = null;
            try {
                Method parseFromMethod = msgClass.getMethod("parseFrom", consumerRecord.value().getClass());
                parsedMessage = (GeneratedMessageV3) parseFromMethod.invoke(null, consumerRecord.value());

            } catch (Exception x) {
                LOGGER.info("Could not parse record normally. Attempting to parse Unknownfields.", x);
                Event event = Event.parseFrom(consumerRecord.value());
                Method parseFromMethod = msgClass.getMethod("parseFrom", ByteString.class);
                parsedMessage =
                        (GeneratedMessageV3) parseFromMethod.invoke(null, event.getUnknownFields().toByteString());

            }
            return parsedMessage;
        } catch (Exception e) {
            LOGGER.info("Unexpected exception parsing record. ", e);
        }
        return null;
    }

    public void run() {
        try {
            while (!Thread.currentThread().isInterrupted()) {
                EventBusRecords<String, byte[]> eventRecords = eventConsumer.poll(pollTimeout);

                for (EventBusRecord<String, byte[]> record : eventRecords) {
                    process(record);
                }

                if (!eventRecords.isEmpty()) {
                    checkpoint();
                }
            }
        } catch (ExecutionException | InterruptedException e) {
            LOGGER.error(String.format("Aborting monitor for '%s' due to exception '%s'", streamName,
                    e.getLocalizedMessage()), e);
            throw new RuntimeException(String.format("Aborting monitor for '%s' due to exception '%s'", streamName,
                    e.getLocalizedMessage()), e);
        } finally {
            eventConsumer.close();
        }
    }

    @Override
    public void close() {
        LOGGER.info("Closed Consumer for entity [{}]", entityName);
    }
}
