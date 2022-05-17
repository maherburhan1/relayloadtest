package com.solarwinds.msp.ncentral.eventproduction.adapter.eventbus.client;

import com.solarwinds.msp.ncentral.eventproduction.adapter.eventbus.configuration.EventBusConfig;
import com.solarwinds.msp.ncentral.eventproduction.converter.configuration.EventConversionConfigurationService;
import com.solarwinds.msp.ncentral.eventproduction.converter.configuration.impl.EventConversionConfigurationServiceImpl;
import com.solarwinds.msp.ncentral.eventproduction.converter.configuration.provider.EventConversionConfigurationComponentConfiguration;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.core.io.DefaultResourceLoader;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class RunNCentralEventConsumer {
    private static final int EXECUTORSERVICE_TERMINATION_TIMEOUT_SECONDS = 10;
    private static final Logger LOGGER = LoggerFactory.getLogger(Consumer.class);
    private static ExecutorService executorService = Executors.newSingleThreadExecutor();

    private EventBusConfig config;
    private int argIndex;
    private String overrideId = null;
    private String filter = "";
    private List<String> entityNames;
    private static final EventConversionConfigurationService eventConversionConfigurationService =
            createEventConfigurationService();

    private RunNCentralEventConsumer() {
        config = new EventBusConfig();
    }

    public static void main(String[] args) {
        RunNCentralEventConsumer runningConsumer = new RunNCentralEventConsumer();
        runningConsumer.processArguments(args);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            LOGGER.info("Shutdown executor service..");
            executorService.shutdown();
            try {
                if (!executorService.awaitTermination(EXECUTORSERVICE_TERMINATION_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                    LOGGER.info("Waited 10 seconds for shutdown. Killing now.");
                    executorService.shutdownNow();
                    if (!executorService.awaitTermination(EXECUTORSERVICE_TERMINATION_TIMEOUT_SECONDS,
                            TimeUnit.SECONDS)) {
                        LOGGER.error("Pool did not terminate");
                    }
                }
            } catch (InterruptedException ie) {
                executorService.shutdownNow();
                Thread.currentThread().interrupt();
            }
            CleanUp cleanup = new CleanUp();
            cleanup.run();
        }));

        runningConsumer.startConsumer();
    }

    private void startConsumer() {
        Map<String, String> allConversions = getEventConfigurationService().getAllEntityNames();
        List<String> childEntities = Arrays.asList(StringUtils.split("Contact,Address,ManagementTaskType,"
                + "ServiceInstanceType,BackupStatus,Application,PatchCategory,License,ServiceType", ','));
        allConversions.keySet().removeIf(e -> childEntities.contains(e));

        if (CollectionUtils.isNotEmpty(entityNames)) {
            allConversions.keySet().removeIf(e -> !entityNames.contains(e));
        }

        allConversions.forEach(
                (key, value) -> getEntityClass(value).ifPresent(e -> startSpecificConsumer(e.getClass(), key)));
    }

    private Optional<Object> getEntityClass(String className) {
        try {
            return Optional.of(BeanUtils.instantiateClass(ClassUtils.getClass(className)));
        } catch (ClassNotFoundException e) {
            LOGGER.error("Protocol buffer entity could not be created. ", e);
            return Optional.empty();
        }
    }

    private static EventConversionConfigurationService createEventConfigurationService() {
        try {
            EventConversionConfigurationComponentConfiguration eventConversionConfigurationComponentConfiguration =
                    new EventConversionConfigurationComponentConfiguration();
            return new EventConversionConfigurationServiceImpl(
                    eventConversionConfigurationComponentConfiguration.provideConversions(new DefaultResourceLoader()));
        } catch (IOException e) {
            LOGGER.error("Could not get an instance of the eventing configuration service.", e);
            throw new RuntimeException(e);
        }
    }

    public static EventConversionConfigurationService getEventConfigurationService() {
        return eventConversionConfigurationService;
    }

    private void startSpecificConsumer(Class<?> classType, String entityName) {

        try (Consumer ncentralEventConsumer = new Consumer(config, filter, entityName, classType)) {
            ncentralEventConsumer.initConsumer(overrideId);
            executorService.execute(ncentralEventConsumer::run);
        } catch (Exception e) {
            LOGGER.warn(String.format("Error creating consumer for %s", entityName), e);
        }
    }

    private boolean checkFilterArg(String[] args) {
        if ((args.length >= argIndex + 2) && ("-filter".equals(args[argIndex]))) {
            filter = args[argIndex + 1];
            argIndex += 2;
            return true;
        }
        return false;
    }

    private boolean checkIdOverrideArg(String[] args) {
        if ((args.length >= argIndex + 2) && ("-id".equals(args[argIndex]))) {
            overrideId = args[argIndex + 1];
            argIndex += 2;
            return true;
        }
        return false;
    }

    private boolean checkEntityArg(String[] args) {
        if ((args.length >= argIndex + 2) && ("-entity".equals(args[argIndex]))) {
            entityNames = Arrays.asList(StringUtils.split(args[argIndex + 1], ','));
            argIndex += 2;
            return true;
        }
        return false;
    }

    private void showUsage() {
        System.out.println("Usage: java -jar eventbus-client-11.0.1-SNAPSHOT-jar-with-dependencies.jar"
                + " [-id group_id] [-filter filter_id] [-entity entity_names]");
        System.out.println(" -id: Will override the event_bus_group_id with a specific value. If not"
                + " specified then the value from the reference.conf file is used.");
        System.out.println(
                "  -filter: Will only show events with a BizAppsCustomerID or systemGUID matching the filter");
        System.out.println(
                "  -entity: Will only show events with the entity names matching the filter. names must be comma separated no spaces.");
    }

    private boolean checkHelpArg(String[] args) {
        if ("-help".equals(args[argIndex]) || "-h".equals(args[argIndex])) {
            showUsage();
            argIndex++;
            return true;
        }
        return false;
    }

    private void badArgument(String[] args) {
        System.out.println("Unrecognized argument: " + args[argIndex]);
        showUsage();
        System.exit(-1);
    }

    private void processArguments(String[] args) {
        argIndex = 0;
        boolean foundId = false;
        boolean foundFilter = false;
        boolean foundEntityName = false;
        int foundValidArg = 0;

        while (argIndex < args.length) {
            if (checkHelpArg(args)) {
                System.exit(0);
            }

            if (!foundId) {
                foundId = checkIdOverrideArg(args);
                foundValidArg = (foundId) ? (foundValidArg + 1) : 0;
            }

            if (!foundFilter) {
                foundFilter = checkFilterArg(args);
                foundValidArg = (foundFilter) ? (foundValidArg + 1) : foundValidArg;
            }

            if (!foundEntityName) {
                foundEntityName = checkEntityArg(args);
                foundValidArg = (foundEntityName) ? (foundValidArg + 1) : foundValidArg;
            }

            if (foundValidArg * 2 != args.length) {
                badArgument(args);
            }
        }
    }
}
