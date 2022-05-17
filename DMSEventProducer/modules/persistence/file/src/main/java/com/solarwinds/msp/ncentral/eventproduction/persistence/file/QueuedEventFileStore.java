package com.solarwinds.msp.ncentral.eventproduction.persistence.file;

import com.google.protobuf.MessageLite;

import com.nable.logging.Logger;
import com.nable.logging.Loggers;
import com.solarwinds.constants.GeneralConstants;
import com.solarwinds.msp.ncentral.eventproduction.api.service.persistence.EventFileStore;
import com.solarwinds.msp.ncentral.eventproduction.api.service.persistence.SerializableEvent;
import com.solarwinds.msp.ncentral.eventproduction.api.service.persistence.TimestampedEvent;
import com.solarwinds.msp.ncentral.eventproduction.api.service.publisher.PublishingContext;
import com.solarwinds.msp.ncentral.eventproduction.api.service.tracking.EventingControlService;
import com.solarwinds.msp.ncentral.eventproduction.api.service.tracking.EventingStartupListener;

import org.apache.commons.lang3.SerializationUtils;
import org.iq80.leveldb.DB;
import org.iq80.leveldb.DBIterator;
import org.iq80.leveldb.Options;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.ZonedDateTime;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

import static com.solarwinds.msp.ncentral.eventproduction.persistence.file.EventPersistenceFileComponentConfiguration.FILE_BUFFER_PERSIST_DIRECTORY_BEAN;
import static org.iq80.leveldb.impl.Iq80DBFactory.bytes;
import static org.iq80.leveldb.impl.Iq80DBFactory.factory;

/**
 * The event store is used to offload Protocol Buffers events. It provides a means of saving events to a file, and
 * reading them back in one at a time.
 *
 * @param <T> the event type.
 */
@Component
public class QueuedEventFileStore<T extends MessageLite> implements EventFileStore<T>, EventingStartupListener {

    private static final Logger logger = Loggers.EVENT_PRODUCER;

    // LevelDB uses a memory buffer for writes which periodically will dump into files once the write buffer
    // size limit is reached. Here, we are setting an 8MB write buffer. The default write buffer size is 4MB if
    // not specified.
    private static final int WRITE_BUFFER_SIZE_IN_BYTES = GeneralConstants.BYTES_IN_ONE_MEGABYTE * 8;

    private final AtomicReference<File> fileQueueDirectory = new AtomicReference<>();

    private final AtomicReference<DB> fileQueueStore = new AtomicReference<>();
    private final AtomicInteger count = new AtomicInteger();

    private final Path storageDirectory;

    /**
     * Creates an instance of the event store.
     *
     * @param storageDirectory The directory to use for leveldb sst files.
     */
    public QueuedEventFileStore(@Qualifier(FILE_BUFFER_PERSIST_DIRECTORY_BEAN) Path storageDirectory,
            EventingControlService eventingControlService) {
        this.storageDirectory = storageDirectory;
        eventingControlService.addStartupListenerOrExecuteStartup(this);
    }

    @Override
    public synchronized void onEventingStart() {
        if (!storeInitialized()) {
            // Ensure the storage DIR exists. Try to create if it doesn't
            File storeDirectory = storageDirectory.toFile();
            createFileStoreDirectories(storeDirectory);
            fileQueueDirectory.set(storeDirectory);
            count.set(0);

            initializeFileQueueStore();
            logger.info("Component {} initialized.", this.getClass().getSimpleName());
        } else {
            logger.debug("Component {} is already initialized, does not need to be reinitialized.",
                    this.getClass().getSimpleName());
        }
    }

    private void createFileStoreDirectories(File storeDirectory) {
        if (!storeDirectory.exists() && !storeDirectory.mkdirs()) {
            throw new IllegalArgumentException("Failed to create directory path: " + storeDirectory.getAbsolutePath());
        }
    }

    private void deleteEventFileQueueDirectory() {
        if (!fileQueueDirectory.get().exists()) {
            return;
        }

        final File[] storeFiles = fileQueueDirectory.get().listFiles();
        if (storeFiles != null) {
            for (File storeFile : storeFiles) {
                if (!storeFile.delete()) {
                    logger.error("Unable to clean up persistent event store file: {}", storeFile.getAbsolutePath());
                }
            }
        }

        if (!fileQueueDirectory.get().delete()) {
            logger.error("Unable to clean up persistent event store directory: {}",
                    fileQueueDirectory.get().getAbsolutePath());
        }
    }

    private void initializeFileQueueStore() {
        try {
            fileQueueStore.set(factory.open(fileQueueDirectory.get(), getStoreOptions()));
        } catch (IOException e) {
            logger.error("Failed to initialize store for Event Production.", e);
        }
    }

    /**
     * Closes the persistent level db data store. This will not delete any persisted data in the directory.
     */
    @Override
    public synchronized void close() {
        try {
            if (storeInitialized()) {
                fileQueueStore.getAndSet(null).close();
            }
        } catch (IOException e) {
            logger.error("Failed to close store for Event Production.", e);
        }
    }

    /**
     * Wipe out any saved events and delete files.
     */
    public synchronized void clear() {
        // First, close the file store so we do not lock the resources.
        close();

        // Delete all files in the event store directory.
        deleteEventFileQueueDirectory();
        count.set(0);

        // Re-open the store after re-creating an empty directory.
        createFileStoreDirectories(fileQueueDirectory.get());
        initializeFileQueueStore();
    }

    /**
     * Takes the provided event data and persists it into the file store. The file store operates as a queue, so events
     * added through this method will be added to the "end" of the queue. When calling remove, the oldest event will be
     * removed from the file store.
     *
     * @param event The event to be saved/persisted.
     */
    @Override
    public synchronized void persist(TimestampedEvent<T> event) {
        if (storeInitialized()) {
            Objects.requireNonNull(event, "TimestampedEvent object cannot be persisted as null");
            Objects.requireNonNull(event.getEvent(), "Event data cannot be persisted as null");
            Objects.requireNonNull(event.getTimestamp(), "Timestamp cannot be persisted as null");
            Objects.requireNonNull(event.getPublishingContext(), "Publishing context cannot be persisted as null");

            fileQueueStore.get().put(getPersistTimestamp(event), convertEventToBytes(event));
            count.incrementAndGet();
        }
    }

    /**
     * Removes the oldest event from the file queue and returns it after deserializing.
     *
     * @return The deserialized event data.
     */
    @Override
    public synchronized Optional<TimestampedEvent<T>> remove() {
        if (storeInitialized()) {
            Optional<Entry<byte[], byte[]>> nextEntry = getNextEntryFromFileQueue();
            if (nextEntry.isPresent()) {
                // Remove this event data from the level db store.
                fileQueueStore.get().delete(nextEntry.get().getKey());
                count.decrementAndGet();

                // Get event data for the next event.
                return convertBytesToEvent(nextEntry.get().getValue());
            }
        }
        return Optional.empty();
    }

    /**
     * Checks whether there are any elements in the queue.
     *
     * @return a boolean indicating whether the queue has any elements.
     */
    @Override
    public synchronized boolean isEmpty() {
        if (!storeInitialized()) {
            return true;
        }

        return currentCount() == 0;
    }

    /**
     * Gets the current count of records in the file queue.
     *
     * @return Integer representing the count of records in the file queue.
     */
    @Override
    public synchronized long currentCount() {
        return count.get();
    }

    /**
     * Gets the current amount of disk space being used by the file queue in bytes.
     *
     * @return Long representing the current byte usage of the file queue.
     */
    @Override
    public synchronized long currentByteSize() {
        return getWorkingDirectory().map(QueuedEventFileStore::sumFilesSizesInDirectory).orElse(0L);
    }

    private static long sumFilesSizesInDirectory(File directory) {
        try (Stream<Path> paths = Files.walk(directory.toPath())) {
            return paths.mapToLong(path -> path.toFile().length()).sum();
        } catch (IOException e) {
            logger.error("An issue was encountered when trying to calculate the byte size of the file buffer folder.",
                    e);
        }
        return 0L;
    }

    /**
     * Returns the current working directory of this queued file store.
     *
     * @return File object for the directory.
     */
    @Override
    public synchronized Optional<File> getWorkingDirectory() {
        return Optional.ofNullable(fileQueueDirectory.get());
    }

    private Optional<Entry<byte[], byte[]>> getNextEntryFromFileQueue() {
        Optional<DBIterator> iterator = getIterator();
        if (iterator.isPresent() && iterator.get().hasNext()) {
            return Optional.of(iterator.get().next());
        }
        return Optional.empty();
    }

    private byte[] convertEventToBytes(TimestampedEvent<T> event) {
        SerializableEvent serializableEvent = buildSerializableEvent(event);
        return SerializationUtils.serialize(serializableEvent);
    }

    private Optional<TimestampedEvent<T>> convertBytesToEvent(byte[] byteData) {
        SerializableEvent serializableEvent = SerializationUtils.deserialize(byteData);

        return createEventFromType(serializableEvent.getEventType(), serializableEvent.getEventData()).map(
                eventData -> {
                    ZonedDateTime timestamp = SerializationUtils.deserialize(serializableEvent.getTimestamp());
                    PublishingContext publishingContext =
                            SerializationUtils.deserialize(serializableEvent.getPublishingContext());
                    return new TimestampedEvent<>(eventData, timestamp, publishingContext);
                });
    }

    /**
     * Generate the Protocol Buffers object for the given type from the data. Problems constructing the correct
     * class/builder result in a {@link ClassNotFoundException}.
     *
     * @param eventTypeName the qualified name of the Protocol Buffers type.
     * @param eventByteData the bytes to reconstruct the object from.
     * @return The constructed Protocol Buffers object, or {@link Optional#empty()} if it could not be converted.
     */
    private Optional<T> createEventFromType(String eventTypeName, byte[] eventByteData) {
        try {
            // Find the parseFrom method on the type we're interested in.
            // That can be used to process the message into an object.
            Class<?> parsingTypeClass = Class.forName(eventTypeName);
            Method staticParseFromMethod = parsingTypeClass.getMethod("parseFrom", byte[].class);
            Object[] parseFromMethodParameters = {eventByteData};
            return Optional.of((T) staticParseFromMethod.invoke(null, parseFromMethodParameters));
        } catch (ClassNotFoundException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            logger.error("An issue was encountered when trying to parse an Event from event byte data.", e);
        }
        return Optional.empty();
    }

    private Optional<DBIterator> getIterator() {
        if (storeInitialized()) {
            DBIterator iterator = fileQueueStore.get().iterator();
            iterator.seekToFirst();
            return Optional.of(iterator);
        }
        return Optional.empty();
    }

    private SerializableEvent buildSerializableEvent(TimestampedEvent<T> event) {
        return SerializableEvent.builder()
                .withEventData(event.getEvent().toByteArray())
                .withEventType(event.getEvent().getClass().getTypeName())
                .withTimestamp(SerializationUtils.serialize(event.getTimestamp()))
                .withPublishingContext(SerializationUtils.serialize(event.getPublishingContext()))
                .build();
    }

    private Options getStoreOptions() {
        Options storeOptions = new Options();
        storeOptions.writeBufferSize(WRITE_BUFFER_SIZE_IN_BYTES);
        return storeOptions;
    }

    /**
     * System.nanoTime() is specifically used because it will not be affected by system clock. It is a guaranteed
     * positive elapsed timer with the precision we need to ensure events are stored in proper order. If user changes
     * the system clock, there is daylight savings, leap seconds, or any other issue, we won't have a problem when using
     * this method. LevelDB will store our records as FIFO automatically if we insert keys using their nanoTime
     * timestamp since LevelDB sorts key data lexicographically. Therefore, when getting an iterator from the store and
     * seeking to the first element, that element can be interpreted as the oldest element in the key store.
     *
     * @return a byte array representing the system nano time at time of persist that can be used as a key.
     */
    private byte[] getPersistTimestamp(TimestampedEvent<T> event) {
        long persistTimeStamp =
                event.getTimestampForPersistence() != 0 ? event.getTimestampForPersistence() : System.nanoTime();
        event.setTimestampForPersistence(persistTimeStamp);
        return bytes(Long.toString(persistTimeStamp));
    }

    private boolean storeInitialized() {
        return fileQueueStore.get() != null;
    }
}