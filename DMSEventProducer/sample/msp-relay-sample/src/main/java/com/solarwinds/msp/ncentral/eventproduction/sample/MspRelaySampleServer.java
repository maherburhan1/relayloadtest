package com.solarwinds.msp.ncentral.eventproduction.sample;

import com.nable.util.StringUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.Executors;

import io.grpc.Server;
import io.grpc.ServerBuilder;

/**
 * Sample gRPC server that provides the MSP Relay interface, logs received messages and returns a response with the
 * configured status (default = OK). It is intended to be used with the {@link MspRelaySampleClient}.
 * <p>
 * In order to use custom (non-default) configuration, use the following command line options:
 * <pre>
 * -Dport=8081 -DconfigurationFileName=MspRelayConfigurationOne.txt -Dcertificate=/path/server.crt -Dkey=/path/server.pem
 * </pre>
 * or use full standalone command:
 * <pre>
 * mvn exec:java -Dport=8081 -DconfigurationFileName=MspRelayConfigurationOne.txt \
 * -Dcertificate=/path/to/certificate/server.crt -Dkey=/path/to/key/server.pem
 * </pre>
 */
public class MspRelaySampleServer {

    /**
     * Default MSP Relay Sample Server port to listen on.
     */
    private static final int DEFAULT_SERVER_PORT = 8080;

    /**
     * Default MSP Relay Sample Server configuration file name.
     */
    private static final String DEFAULT_CONFIGURATION_FILE_NAME = "MspRelayConfiguration.txt";

    /**
     * Default PEM Encoded X509 Server Certificate.
     */
    private static final String DEFAULT_SERVER_CERTIFICATE = MspRelaySampleConstant.DEFAULT_CLIENT_CERTIFICATE;

    /**
     * Default PEM Encoded PKCS#8 Server Private Key.
     */
    private static final String DEFAULT_SERVER_PRIVATE_KEY = MspRelaySampleConstant.DEFAULT_CLIENT_PRIVATE_KEY;

    private static final String HELP =
            "Use the following options:\n" + "\t-Dport=<NUMBER>    - the MSP Relay Sample Server port to listen on\n"
                    + "\t-DconfigurationFileName=<FILE_PATH>    - the MSP Relay Sample Server configuration file path\n"
                    + "\t-Dcertificate=<FILE_PATH>    - the PEM Encoded X509 Server Certificate file path\n"
                    + "\t-Dkey=<FILE_PATH>    - the PEM Encoded PKCS#8 Server Private Key file path";

    private static final Logger logger = LoggerFactory.getLogger(MspRelaySampleServer.class);

    private int port;
    private String configurationFileName;
    private String certificate;
    private String privateKey;

    /**
     * Runs the application.
     *
     * @param arguments the application arguments - ignored.
     */
    public static void main(String[] arguments) {
        try {
            final MspRelaySampleServer server = new MspRelaySampleServer();
            server.run();
        } catch (Exception e) {
            logger.error("An error occurred. Exit.", e);
        }
    }

    private void run() throws IOException, InterruptedException {
        logger.info("Configuring the server.");

        readSystemProperties();

        final PublishResponseLogger publishResponseLogger = new PublishResponseLogger();
        final FileWatcher fileWatcher = new FileWatcher(configurationFileName, publishResponseLogger);

        Executors.newSingleThreadExecutor().submit(fileWatcher::watchOffers);

        final Server server = ServerBuilder.forPort(port)
                .useTransportSecurity(MspRelayHelper.getInputStream(certificate),
                        MspRelayHelper.getInputStream(privateKey))
                .addService(publishResponseLogger)
                .addService(new MspRelayHealthService())
                .build();
        logger.info("Starting the server.");
        server.start();

        logger.info("Waiting for requests or termination.");
        server.awaitTermination();
    }

    private void readSystemProperties() {
        logger.info("Reading and parsing the command line system properties.");

        final String portString = System.getProperty("port");
        final String fileName = System.getProperty("configurationFileName");
        final String certificatePath = System.getProperty("certificate");
        final String keyPath = System.getProperty("key");
        logger.debug("System properties:\n\tport = {}\n\tconfigurationFileName = {}\n\tcertificate = {}\n\tkey = {}",
                portString, fileName, certificatePath, keyPath);

        try {
            port = portString != null ? Integer.parseInt(portString) : DEFAULT_SERVER_PORT;
            configurationFileName =
                    StringUtils.isNotBlank(fileName) ? fileName.trim() : DEFAULT_CONFIGURATION_FILE_NAME;
            certificate = StringUtils.isNotBlank(certificatePath) ? MspRelayHelper.readFile(certificatePath) :
                    DEFAULT_SERVER_CERTIFICATE;
            privateKey =
                    StringUtils.isNotBlank(keyPath) ? MspRelayHelper.readFile(keyPath) : DEFAULT_SERVER_PRIVATE_KEY;
        } catch (Exception e) {
            throw new IllegalArgumentException("Cannot parse system properties. " + HELP, e);
        }
        logger.debug("port: {}", port);
        logger.debug("configuration file name: {}", configurationFileName);
        logger.debug("certificate:\n{}", certificate);
        logger.debug("private key:\n{}", privateKey);
    }
}
