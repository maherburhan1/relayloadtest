package com.solarwinds.msp.ncentral.eventproduction.persistence.file;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * This class represents the Spring configuration for the MSP Event Persistence File module.
 */
@Configuration
@ComponentScan(basePackages = {"com.solarwinds.msp.ncentral.eventproduction.persistence.file"}, lazyInit = true)
@Lazy
public class EventPersistenceFileComponentConfiguration {
    public static final String FILE_BUFFER_PERSIST_DIRECTORY_BEAN = "eventFileBufferPersistDirectory";

    @Bean(name = FILE_BUFFER_PERSIST_DIRECTORY_BEAN)
    public Path getEventFileBufferPersistDirectory() {return Paths.get("/backup/events/fileBuffers");}

}
