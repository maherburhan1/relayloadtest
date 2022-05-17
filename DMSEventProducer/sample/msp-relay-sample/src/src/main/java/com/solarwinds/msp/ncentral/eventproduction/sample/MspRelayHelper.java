package com.solarwinds.msp.ncentral.eventproduction.sample;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * This class contains various methods for MSP Relay Sample Server and Client.
 */
public final class MspRelayHelper {

    private MspRelayHelper() {}

    /**
     * Read the content of the file specified by the file path by using the {@link StandardCharsets#US_ASCII} character
     * set.
     *
     * @param filePath the file path.
     * @return The content of the file as {@link String}.
     * @throws IOException if an error occurs.
     */
    public static String readFile(String filePath) throws IOException {
        return new String(Files.readAllBytes(Paths.get(filePath.trim())), StandardCharsets.US_ASCII);
    }

    /**
     * Creates a {@link ByteArrayInputStream} from the specified string value by using the {@link
     * StandardCharsets#US_ASCII} character set.
     *
     * @param value the string value.
     * @return The {@link ByteArrayInputStream} created from the specified string value.
     */
    public static ByteArrayInputStream getInputStream(String value) {
        return new ByteArrayInputStream(value.getBytes(StandardCharsets.US_ASCII));
    }
}
