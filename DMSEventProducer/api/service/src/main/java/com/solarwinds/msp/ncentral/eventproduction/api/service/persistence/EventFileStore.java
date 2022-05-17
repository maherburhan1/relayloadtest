package com.solarwinds.msp.ncentral.eventproduction.api.service.persistence;

import com.google.protobuf.MessageLite;

import java.io.File;
import java.util.Optional;

/**
 * Persistent store for event objects.
 *
 * @param <T> event type
 */
public interface EventFileStore<T extends MessageLite> extends AutoCloseable {
    /**
     * This will save an event to the persistent store.
     *
     * @param event The event to be saved/persisted
     */
    void persist(TimestampedEvent<T> event);

    /**
     * Returns and removes the next event.
     *
     * @return The event with its timestamp.
     */
    Optional<TimestampedEvent<T>> remove();

    /**
     * Returns whether the file queue is empty (contains no elements).
     *
     * @return Boolean indicating whether store is empty.
     */
    boolean isEmpty();

    /**
     * Returns the current count of elements in the file queue.
     *
     * @return Long indicating the record count.
     */
    long currentCount();

    /**
     * Returns the current number of bytes being used on disk by the file queue.
     *
     * @return Long indicating the byte size usage.
     */
    long currentByteSize();

    /**
     * Returns the current working directory of this queued file store.
     *
     * @return File object for the directory.
     */
    Optional<File> getWorkingDirectory();

    /**
     * Clears all records from the file queue.
     */
    void clear();

    /**
     * Clears all records from the file queue and then frees the stream resources in use by file queue.
     */
    @Override
    void close();
}