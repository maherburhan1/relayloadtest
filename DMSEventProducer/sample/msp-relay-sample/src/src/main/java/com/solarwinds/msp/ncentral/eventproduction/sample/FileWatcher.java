package com.solarwinds.msp.ncentral.eventproduction.sample;

import com.solarwinds.msp.relay.Relay;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.Collections;
import java.util.List;

/**
 * Class monitors changes being made on the user home directory. If the watched file is changed, it will be read and
 * parsed.
 * <p>
 * The configuration file may contain the following options, each on the new line:
 * <pre>
 * response_status
 * request_delay
 * </pre>
 * <p>
 * If and only if the response_status equals to "PUBLISH_ERROR", Fake Relay server will be answering to all upcoming
 * requests with "PUBLISH_ERROR", until the value is changed to "OK" or any other text or no text at all, that will
 * switch Fake Relay server back to answer "OK" to all upcoming requests. The default value of the server is to answer
 * with "Relay.ResponseMessage.ResponseStatus.OK", and this value will be used even if the file which is being watched
 * contains incorrect or no text.
 * <p>
 * Examples:
 * <p>
 * Pause the request processing for 10 seconds and then response with status OK:
 * <pre>
 * response_status=OK
 * request_delay=10000
 * </pre>
 * <p>
 * Pause the request processing for 1 minute and then response with status PUBLISH_ERROR:
 * <pre>
 * response_status=PUBLISH_ERROR
 * request_delay=60000
 * </pre>
 * <p>
 * Do not pause the request processing and response with status OK:
 * <pre>
 * response_status=OK
 * request_delay=0
 * </pre>
 * or
 * <pre>
 * response_status=
 * request_delay=
 * </pre>
 * or no configuration file or blank configuration file.
 */
public class FileWatcher {

    private static final String CONFIGURATION_FILE_DIRECTORY = System.getProperty("user.home");

    private static final Logger logger = LoggerFactory.getLogger(FileWatcher.class);

    private final String responseStatusFileName;
    private final PublishResponseLogger publishResponseLogger;

    /**
     * Creates an instance of this class with the specified parameters.
     */
    public FileWatcher(String responseStatusFileName, PublishResponseLogger publishResponseLogger) {
        this.responseStatusFileName = responseStatusFileName;
        this.publishResponseLogger = publishResponseLogger;
    }

    /**
     * Starts to watch the specified response status file.
     */
    public void watchOffers() {
        final Path absoluteFilePath = Paths.get(CONFIGURATION_FILE_DIRECTORY, responseStatusFileName);
        touchServerChangingStateFile(absoluteFilePath);

        final Path responseStatusFileDirectoryPath = Paths.get(CONFIGURATION_FILE_DIRECTORY);
        final WatchService watchingService;

        try {
            watchingService = FileSystems.getDefault().newWatchService();
            responseStatusFileDirectoryPath.register(watchingService, StandardWatchEventKinds.ENTRY_CREATE,
                    StandardWatchEventKinds.ENTRY_DELETE, StandardWatchEventKinds.ENTRY_MODIFY);
        } catch (IOException e) {
            logger.error("Something with registering of event of change went wrong.", e);
            return;
        }

        while (true) {
            final WatchKey didEventOccurredKey;
            try {
                didEventOccurredKey = watchingService.take();
            } catch (InterruptedException e) {
                logger.error("Taking of events from the queue went wrong. Most likely shutting down.");
                return;
            }

            for (WatchEvent<?> eventChange : didEventOccurredKey.pollEvents()) {
                final WatchEvent.Kind<?> eventKind = eventChange.kind();
                final Path changedFile = (Path) eventChange.context();

                if (eventKind.equals(StandardWatchEventKinds.ENTRY_MODIFY) && changedFile.endsWith(
                        responseStatusFileName)) {
                    logger.debug("Event {} was triggered. The file '{}' has been modified.", eventKind.name(),
                            changedFile.toString());

                    final MspRelayConfiguration configuration = getServerConfigurationFromFile(absoluteFilePath);

                    logger.info("The MSP Relay Server configuration has been changed to: {}", configuration);
                    publishResponseLogger.setConfiguration(configuration);
                }
            }
            didEventOccurredKey.reset();
        }
    }

    private MspRelayConfiguration getServerConfigurationFromFile(Path fileName) {
        final MspRelayConfiguration configuration = new MspRelayConfiguration();
        int lineCount = 0;
        for (String line : readFile(fileName)) {
            lineCount++;
            logger.debug("Configuration file line {}: '{}'.", lineCount, line);
            if (!line.contains("=")) {
                continue;
            }
            final String[] lineParts = line.split("=");
            if (MspRelayConfiguration.PREFIX_RESPONSE_STATUS.equalsIgnoreCase(lineParts[0].trim())) {
                if (MspRelayConfiguration.RESPONSE_STATUS_PUBLISH_ERROR.equals(lineParts[1])) {
                    configuration.setResponseStatus(Relay.ResponseMessage.ResponseStatus.PUBLISH_ERROR);
                }
            } else if (MspRelayConfiguration.PREFIX_REQUEST_DELAY.equalsIgnoreCase(lineParts[0].trim())) {
                configuration.setRequestDelayMilliseconds(NumberUtils.toLong(lineParts[1]));
            }
        }
        return configuration;
    }

    private List<String> readFile(Path fileName) {
        try {
            final List<String> fileContent = Files.readAllLines(fileName);
            if (CollectionUtils.isNotEmpty(fileContent)) {
                return fileContent;
            }
        } catch (Exception e) {
            logger.error("It is not possible to read the file '{}'.", fileName, e);
        }
        return Collections.emptyList();
    }

    private void touchServerChangingStateFile(Path absoluteFilePath) {
        try {
            if (Files.notExists(absoluteFilePath)) {
                Files.createFile(absoluteFilePath);
            }
        } catch (IOException e) {
            logger.error("Error while touching the file '{}'.", responseStatusFileName, e);
        }
    }
}
