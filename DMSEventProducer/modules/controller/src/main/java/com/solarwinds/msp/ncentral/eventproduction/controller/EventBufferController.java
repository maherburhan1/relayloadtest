package com.solarwinds.msp.ncentral.eventproduction.controller;

import com.google.protobuf.MessageLite;

import com.nable.logging.Logger;
import com.nable.logging.Loggers;
import com.solarwinds.constants.GeneralConstants;
import com.solarwinds.msp.ncentral.common.time.TimeService;
import com.solarwinds.msp.ncentral.eventproduction.api.entity.EventStatistic;
import com.solarwinds.msp.ncentral.eventproduction.api.service.persistence.EventFileStore;
import com.solarwinds.msp.ncentral.eventproduction.api.service.persistence.TimestampedEvent;
import com.solarwinds.msp.ncentral.eventproduction.api.service.scraping.EventScraper;
import com.solarwinds.msp.ncentral.eventproduction.api.service.statistics.EventStatistics;
import com.solarwinds.msp.ncentral.eventproduction.api.service.tracking.ComponentStatusService;
import com.solarwinds.msp.ncentral.eventproduction.api.service.tracking.EventingControlService;
import com.solarwinds.msp.ncentral.eventproduction.api.service.tracking.EventingStartupListener;
import com.solarwinds.msp.ncentral.eventproduction.controller.impl.EventFailuresMonitor;
import com.solarwinds.msp.ncentral.eventproduction.controller.impl.PersistedEventSendBuffer;

import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static com.solarwinds.msp.ncentral.eventproduction.controller.EventBufferController.BufferFlowStates.STABLE_FLOW_TO_MEMORY;
import static com.solarwinds.msp.ncentral.eventproduction.controller.EventBufferController.BufferFlowStates.TERMINATED;
import static com.solarwinds.msp.ncentral.eventproduction.controller.EventBufferController.BufferFlowStates.UNSTABLE_OVERFLOW_TO_FILE;
import static com.solarwinds.msp.ncentral.eventproduction.controller.EventBufferController.BufferStatus.AVAILABLE;
import static com.solarwinds.msp.ncentral.eventproduction.controller.EventBufferController.BufferStatus.FULL;

/**
 * A utility class for encapsulating the event buffer logic. This makes it easier to use with the event controller, as
 * this class simply provides the methods of queueing and dequeueing. All control flow logic is internal to this
 * controller so whatever class is using this controller needs only to provide the buffer objects and then the overflow
 * and termination logic will be contained here.
 *
 * @param <T> Generic type extending the MessageLite interface (protobuf).
 */
@Component
public class EventBufferController<T extends MessageLite> implements EventingStartupListener {

    private static final Logger logger = Loggers.EVENT_PRODUCER;

    /**
     * Only check the size of memory / file buffer after N inserts/removals to save on performance. Typical system can
     * have 160 inserts per second.
     */
    public static final int NUMBER_OF_OPERATIONS_UNTIL_CAPACITY_CHECK = 200;
    /**
     * When memory buffer has become full (100%), deem it available again once it's memory usage goes below 75%.
     */
    private static final double MEMORY_BUFFER_PERCENTAGE_AVAILABLE_AGAIN = 0.75;
    /**
     * The maximum number of events we want to add back into memory from file at a time.
     */
    private static final int MAX_NUMBER_OF_RECORDS_TO_RELOAD_TO_MEMORY_AT_ONCE = 10_000;
    /**
     * If the JVM max memory is unbounded, we need to use a reasonable amount of memory for memory buffer size calc.
     * Since N-central minimum memory is 4GB and jetty is assigned about 40% of that, we use a safe option of ~1.5GB.
     */
    private static final long DEFAULT_JVM_MEMORY = GeneralConstants.BYTES_IN_ONE_MEGABYTE * 1_500L;

    private static final double DEFAULT_MEMORY_BUFFER_SIZE = 5.0;
    private static final double DEFAULT_FILE_BUFFER_SIZE = 5.0;
    private static final Duration DEFAULT_PAUSE_BETWEEN_RELAY_PINGS = Duration.ofSeconds(30);

    private final AtomicReference<BufferFlowStates> currentFlowState = new AtomicReference<>(STABLE_FLOW_TO_MEMORY);
    private final AtomicReference<BufferStatus> memoryBufferStatus = new AtomicReference<>();
    private final PersistedEventSendBuffer<T> memoryBuffer;
    private final EventFileStore<T> fileBuffer;
    private final EventStatistics eventStatistics;
    private final EventFailuresMonitor eventFailuresMonitor;
    private final EventControllerConfiguration eventControllerConfiguration;
    private final EventRelayPingService eventRelayPingService;
    private final EventScraper eventScraper;
    private final ComponentStatusService componentStatusService;
    private final TimeService timeService;
    private final AtomicReference<Future<?>> reloadExecutorStatus = new AtomicReference<>();
    private final AtomicReference<Future<?>> checkRelayHealthStatus = new AtomicReference<>();
    private final AtomicInteger operationsSinceLastCapacityCheck = new AtomicInteger(0);

    private Duration pauseBetweenRelayPings;
    private ExecutorService reloadEventsFromFileToMemoryExecutor;
    private ExecutorService checkRelayHealthPingExecutor;

    // The maximum memory size of the memory buffer in bytes.
    private long maximumMemoryBufferByteSize;
    // Once memory buffer is full, it needs to go below this size to be deemed available again.
    private long memoryBufferAvailableAgainByteSize;
    // The maximum disk usage size of the file buffer in bytes.
    private long maximumFileBufferByteSize;

    /**
     * Initialize an NEP buffer controller with a memory buffer and file buffer and specified byte size limits for
     * each.
     *
     * @param persistedEventSendBuffer The memory queue, which can have events dequeued by the event controller and
     * re-sent.
     * @param queuedEventStore The file queue, which will handle overflow if memory queue becomes full and re-loads
     * events into memory when it becomes available again.
     * @param eventStatistics The service responsible for tracking statistics for the buffer controller, such as
     * additions and removals to the queue as well as state transitions.
     * @param eventFailuresMonitor The monitor responsible for sending email notifications.
     * @param eventRelayPingService The service responsible for pinging msp relays.
     * @param eventControllerConfiguration class for getting configured data
     * @param eventingControlService The service responsible for detecting changes in state to a customers eventing
     * configuration (for example, if they have disabled eventing).
     * @param componentStatusService {@link ComponentStatusService} to make the internal processing state visible to
     * others
     * @param eventScraper The service responsible for triggering event database scraping.
     */
    public EventBufferController(PersistedEventSendBuffer<T> persistedEventSendBuffer,
            EventFileStore<T> queuedEventStore, EventStatistics eventStatistics,
            EventFailuresMonitor eventFailuresMonitor, EventRelayPingService eventRelayPingService,
            EventControllerConfiguration eventControllerConfiguration, EventingControlService eventingControlService,
            ComponentStatusService componentStatusService, @Lazy EventScraper eventScraper, TimeService timeService) {
        this.memoryBuffer = persistedEventSendBuffer;
        this.fileBuffer = queuedEventStore;
        this.eventFailuresMonitor = eventFailuresMonitor;
        this.eventStatistics = eventStatistics;
        this.eventRelayPingService = eventRelayPingService;
        this.eventControllerConfiguration = eventControllerConfiguration;
        this.componentStatusService = componentStatusService;
        this.eventScraper = eventScraper;
        this.timeService = timeService;

        eventingControlService.addStartupListenerOrExecuteStartup(this);
    }

    @Override
    public synchronized void onEventingStart() {
        if (reloadEventsFromFileToMemoryExecutor == null) {
            reloadEventsFromFileToMemoryExecutor = Executors.newSingleThreadExecutor();
        }
        if (checkRelayHealthPingExecutor == null) {
            checkRelayHealthPingExecutor = Executors.newSingleThreadExecutor();
        }

        final double memoryBufferPercentageSize =
                eventControllerConfiguration.getMemoryBufferPercentageSize().orElse(DEFAULT_MEMORY_BUFFER_SIZE);
        final double fileBufferPercentageSize =
                eventControllerConfiguration.getFileBufferPercentageSize().orElse(DEFAULT_FILE_BUFFER_SIZE);
        pauseBetweenRelayPings = eventControllerConfiguration.getBufferControllerWaitTimeBetweenRelayPings()
                .orElse(DEFAULT_PAUSE_BETWEEN_RELAY_PINGS);

        resetBufferAvailability();

        calculateByteLimits(memoryBufferPercentageSize, fileBufferPercentageSize);
        operationsSinceLastCapacityCheck.set(0);
        clearBuffers();
        logger.info("Component {} initialized.", this.getClass().getSimpleName());
    }

    /**
     * Takes in a timestamped event object and persists it back to the front of the event queue. This method ignores
     * sizing restrictions for the queues. This should only be used to re-queue events that were removed to be re-sent,
     * but failed.
     *
     * @param event The timestamped event object to re-add to beginning of queue.
     */
    public void addFirst(TimestampedEvent<T> event) {
        eventStatistics.addStatistic(EventStatistic.builder()
                .statisticType(EventStatistic.StatisticType.BUFFER_INFORMATION)
                .statisticSubType(EventStatistic.StatisticSubType.IN_MEMORY_QUEUE)
                .statisticValue(1)
                .build());
        memoryBuffer.addFirst(event);
    }

    /**
     * Takes in a timestamped event object and persists it to the end of the event queue.
     *
     * @param event The timestamped event object to add to the end of the queue.
     */
    public void enqueue(TimestampedEvent<T> event) {
        evaluateCurrentBufferState();

        switch (currentFlowState.get()) {
            case STABLE_FLOW_TO_MEMORY:
                memoryBuffer.addLast(event);
                eventStatistics.addStatistic(EventStatistic.builder()
                        .statisticType(EventStatistic.StatisticType.BUFFER_INFORMATION)
                        .statisticSubType(EventStatistic.StatisticSubType.IN_MEMORY_QUEUE)
                        .statisticValue(1)
                        .build());
                break;
            case UNSTABLE_OVERFLOW_TO_FILE:
                eventStatistics.addStatistic(EventStatistic.builder()
                        .statisticType(EventStatistic.StatisticType.BUFFER_INFORMATION)
                        .statisticSubType(EventStatistic.StatisticSubType.IN_DISK_QUEUE)
                        .statisticValue(1)
                        .build());
                fileBuffer.persist(event);
                break;
            case TERMINATED:
                logger.error("Cannot persist event id [{}] of type [{}] into the event queue as the buffers have been "
                        + "terminated.", event.getEvent().hashCode(), event.getEvent().getClass().getSimpleName());
                break;
        }
    }

    /**
     * Attempts to dequeue, removing the oldest event from the event queue.
     *
     * @return An optional of the oldest timestamped event object in the event queue. If the queues have been terminated
     * due to overflow, the optional will be empty.
     * @throws InterruptedException if the thread is interrupted.
     */
    public Optional<TimestampedEvent<T>> dequeue() throws InterruptedException {
        evaluateCurrentBufferState();
        if (!areBuffersTerminated()) {
            eventStatistics.addStatistic(EventStatistic.builder()
                    .statisticType(EventStatistic.StatisticType.BUFFER_INFORMATION)
                    .statisticSubType(EventStatistic.StatisticSubType.IN_MEMORY_QUEUE)
                    .statisticValue(-1)
                    .build());
            return Optional.of(memoryBuffer.takeFirst());
        }
        return Optional.empty();
    }

    /**
     * Gets the current flow state of the event buffers. (i.e. whether the buffers are stable, overflowing, or
     * terminated)
     *
     * @return Enum representing the current buffer flow state.
     */
    public BufferFlowStates currentState() {
        return currentFlowState.get();
    }

    /**
     * Ends any executor threads running for the buffer controller to free up resources.
     */
    public synchronized void close() {
        logger.info("Event Buffer Controller stop requested");
        if (reloadEventsFromFileToMemoryExecutor != null) {
            reloadEventsFromFileToMemoryExecutor.shutdownNow();
            reloadEventsFromFileToMemoryExecutor = null;
        }
        if (checkRelayHealthPingExecutor != null) {
            checkRelayHealthPingExecutor.shutdownNow();
            checkRelayHealthPingExecutor = null;
        }
    }

    /**
     * Gets the current count of elements in the buffer controller's internal memory queue.
     *
     * @return Long value indicating the count of elements in the memory queue.
     */
    public synchronized long getMemoryBufferCurrentCount() { return memoryBuffer.currentCount(); }

    /**
     * Gets the current size of the buffer controller's internal memory queue in bytes.
     *
     * @return Long value indicating the current byte size of the memory queue.
     */
    public synchronized long getMemoryBufferCurrentByteSize() { return memoryBuffer.currentByteSize(); }

    /**
     * Gets the current count of elements in the buffer controller's internal file queue.
     *
     * @return Long value indicating the count of elements in the file queue.
     */
    public synchronized long getFileBufferCurrentCount() { return fileBuffer.currentCount(); }

    /**
     * Gets the current size of the buffer controller's internal file queue in bytes.
     *
     * @return Long value indicating the current byte size of the file queue.
     */
    public synchronized long getFileBufferCurrentByteSize() { return fileBuffer.currentByteSize(); }

    private synchronized void calculateByteLimits(double memoryPercentage, double filePercentage) {
        // Percentage is stored as 5.0 for 5% instead of 0.05. Convert it to the decimal version here.
        final double memoryPercentageAsDecimal = memoryPercentage / GeneralConstants.HUNDRED;
        final double filePercentageAsDecimal = filePercentage / GeneralConstants.HUNDRED;

        // Get the max memory assigned to the JVM.
        long totalMemoryInBytes = Runtime.getRuntime().maxMemory();
        // There is an edge case where Runtime.maxMemory will return max value of Long if it is unbounded.
        if (totalMemoryInBytes == Long.MAX_VALUE) {
            totalMemoryInBytes = DEFAULT_JVM_MEMORY;
        }

        // Calculate the maximum memory allowed for the memory buffer.
        maximumMemoryBufferByteSize = Math.round(totalMemoryInBytes * memoryPercentageAsDecimal);

        // The memory buffer becomes available again when its size is less than N% of capacity. Example: 75%.
        memoryBufferAvailableAgainByteSize =
                Math.round(maximumMemoryBufferByteSize * MEMORY_BUFFER_PERCENTAGE_AVAILABLE_AGAIN);

        // Get the directory for the file buffer.
        fileBuffer.getWorkingDirectory().ifPresent(file -> {
            try {
                maximumFileBufferByteSize = Math.round(
                        Files.getFileStore(file.toPath().toRealPath()).getTotalSpace() * filePercentageAsDecimal);
            } catch (IOException e) {
                logger.error("An issue was encountered while trying to calculate the maximum file buffer byte size.",
                        e);
            }
        });

        logger.info("Memory buffer byte size limit set to {} bytes (~{} MB).", maximumMemoryBufferByteSize,
                convertBytesForLogging(maximumMemoryBufferByteSize, GeneralConstants.BYTES_IN_ONE_MEGABYTE));
        logger.info(
                "After memory buffer hits limit, it will only become available again once it goes below {} bytes (~{}"
                        + " MB).", memoryBufferAvailableAgainByteSize,
                convertBytesForLogging(memoryBufferAvailableAgainByteSize, GeneralConstants.BYTES_IN_ONE_MEGABYTE));
        logger.info("File buffer byte size limit set to {} bytes (~{} GB).", maximumFileBufferByteSize,
                convertBytesForLogging(maximumFileBufferByteSize, GeneralConstants.BYTES_IN_ONE_GIGABYTE));
    }

    /**
     * Will override the memory and file byte capacity that the {@link EventBufferController} has been initialized with
     * from system settings to custom values. Useful for testing.
     *
     * @param memoryByteSize the size in bytes to override memory capacity to.
     * @param fileByteSize the size in bytes to override file capacity to.
     */
    protected void overrideMemoryAndFileCapacity(long memoryByteSize, long fileByteSize) {
        maximumMemoryBufferByteSize = memoryByteSize;
        memoryBufferAvailableAgainByteSize =
                Math.round(maximumMemoryBufferByteSize * MEMORY_BUFFER_PERCENTAGE_AVAILABLE_AGAIN);
        maximumFileBufferByteSize = fileByteSize;
        logger.info("Memory buffer byte size limit has been overridden to {} bytes (~{} MB).",
                maximumMemoryBufferByteSize,
                convertBytesForLogging(maximumMemoryBufferByteSize, GeneralConstants.BYTES_IN_ONE_MEGABYTE));
        logger.info(
                "After memory buffer hits limit, it will only become available again once it goes below {} bytes (~{}"
                        + " MB).", memoryBufferAvailableAgainByteSize,
                convertBytesForLogging(memoryBufferAvailableAgainByteSize, GeneralConstants.BYTES_IN_ONE_MEGABYTE));
        logger.info("File buffer byte size limit has been overridden to {} bytes (~{} GB).", maximumFileBufferByteSize,
                convertBytesForLogging(maximumFileBufferByteSize, GeneralConstants.BYTES_IN_ONE_GIGABYTE));
    }

    private synchronized void evaluateCurrentBufferState() {
        if (shouldCheckSizes()) {
            // Update memory buffer to either be full or available based on size.
            updateMemoryBufferStatus();
            if (STABLE_FLOW_TO_MEMORY.equals(currentFlowState.get()) && !isMemoryBufferAvailable()) {
                // If the memory buffer is currently stable, we need to check if it has gone over capacity.
                currentFlowState.set(UNSTABLE_OVERFLOW_TO_FILE);
                eventFailuresMonitor.processBufferFlowStateUpdate(UNSTABLE_OVERFLOW_TO_FILE);
                logger.warn(
                        "The event production memory queue has reached capacity and future events will now overflow to file queue until the queue is stable again.");
            } else if (UNSTABLE_OVERFLOW_TO_FILE.equals(currentFlowState.get())) {
                if (getFileBufferCurrentByteSize() >= maximumFileBufferByteSize) {
                    // If the file buffer fills, we need to terminate buffering and wait for a relay ping.
                    terminateBuffers();
                    return;
                }

                // If the memory buffer has overflowed into file buffer, we need to periodically check if it is
                // available again. If available, load events back into memory from file.
                if (isMemoryReloadReady()) {
                    reloadExecutorStatus.set(
                            reloadEventsFromFileToMemoryExecutor.submit(this::reloadEventsIntoMemoryFromFile));
                }

                // If the memory buffer is available and the file buffer is empty, we are stable again.
                if (isMemoryBufferAvailable() && fileBuffer.isEmpty()) {
                    currentFlowState.set(STABLE_FLOW_TO_MEMORY);
                    eventFailuresMonitor.processBufferFlowStateUpdate(STABLE_FLOW_TO_MEMORY);
                    fileBuffer.clear();
                    logger.info(
                            "The event production memory queue has fully recovered from overflow and reached a stable state.");
                }
            }
        }
    }

    private synchronized void reloadEventsIntoMemoryFromFile() {
        String logMessageTemplate = "Reloading events from file buffer into memory buffer: {}";
        logger.info(logMessageTemplate, "started");
        componentStatusService.setRunning(getClass(), true);

        try {
            int counter = 0;
            while (isMemoryBufferAvailable() && counter < MAX_NUMBER_OF_RECORDS_TO_RELOAD_TO_MEMORY_AT_ONCE
                    && getFileBufferCurrentCount() > 0) {
                evaluateCurrentBufferState();

                final Optional<TimestampedEvent<T>> eventFromFile = fileBuffer.remove();
                eventStatistics.addStatistic(EventStatistic.builder()
                        .statisticType(EventStatistic.StatisticType.BUFFER_INFORMATION)
                        .statisticSubType(EventStatistic.StatisticSubType.IN_DISK_QUEUE)
                        .statisticValue(-1)
                        .build());
                if (eventFromFile.isPresent()) {
                    eventStatistics.addStatistic(EventStatistic.builder()
                            .statisticType(EventStatistic.StatisticType.BUFFER_INFORMATION)
                            .statisticSubType(EventStatistic.StatisticSubType.IN_MEMORY_QUEUE)
                            .statisticValue(1)
                            .build());
                    memoryBuffer.addLast(eventFromFile.get());
                }
                counter++;
            }
            evaluateCurrentBufferState();
        } finally {
            componentStatusService.setRunning(getClass(), false);
            logger.info(logMessageTemplate, "finished");
        }
    }

    private synchronized void checkEventRelayStatus() {
        componentStatusService.setRunning(getClass(), true);
        logger.info(
                "Buffer Controller will now ping the relay every {}ms to check status and attempt to transition back "
                        + "to an available state.", pauseBetweenRelayPings.toMillis());
        try {
            while (!Thread.interrupted()) {
                if (areAllRelaysHealthy()) {
                    resetBufferAvailability();
                    eventScraper.startupScraping();
                    logger.info("Buffer Controller has determined that relay health is OK and will now accept events "
                            + "again. Scraping has been automatically started.");
                    break;
                }

                try {
                    pauseBeforeRetry();
                } catch (InterruptedException e) {
                    logger.info("While waiting to ping the event relay, the event buffer controller was interrupted.");
                    break;
                }
            }
        } finally {
            componentStatusService.setRunning(getClass(), false);
        }
    }

    private boolean areAllRelaysHealthy() {
        try {
            return eventRelayPingService.checkHealthOfAllRelays();
        } catch (RuntimeException e) {
            // Catch unchecked RuntimeException to prevent errors from stopping the loop mechanism.
            logger.error("The buffer controller encountered an error while pinging the relay.", e);
            return false;
        }
    }

    private void pauseBeforeRetry() throws InterruptedException {
        logger.info("Buffer Controller has determined that relay health is NOT OK and will wait another {}ms "
                + "to ping again.", pauseBetweenRelayPings.toMillis());
        /*
        Wait for N amount of seconds between relay pings.
        Please Note: This statement is intentionally a "sleep()" and not "wait()". This method is submitted
        to a single thread executor which gets run whenever the buffers have both been terminated. In this
        state, we are not adding/removing any elements to the buffers and do not want to release monitors.
        */
        timeService.sleep(pauseBetweenRelayPings);
    }

    private synchronized void updateMemoryBufferStatus() {
        if (isMemoryBufferAvailable()) {
            if (getMemoryBufferCurrentByteSize() >= maximumMemoryBufferByteSize) {
                memoryBufferStatus.set(FULL);
                eventStatistics.addStatistic(EventStatistic.builder()
                        .statisticType(EventStatistic.StatisticType.BUFFER_INFORMATION)
                        .statisticSubType(EventStatistic.StatisticSubType.MEMORY_BUFFER_FULL_EVENT)
                        .statisticValue(1)
                        .build());
            }
        } else {
            if (getMemoryBufferCurrentByteSize() <= memoryBufferAvailableAgainByteSize) {
                memoryBufferStatus.set(AVAILABLE);
            }
        }
    }

    private synchronized void terminateBuffers() {
        logger.warn(
                "The memory and file buffers for event production have both overflowed and will now terminate. Will "
                        + "have to result to data scraping.");
        currentFlowState.set(TERMINATED);
        eventFailuresMonitor.processBufferFlowStateUpdate(TERMINATED);
        clearBuffers();
        if (isRelayPingReady()) {
            checkRelayHealthStatus.set(checkRelayHealthPingExecutor.submit(this::checkEventRelayStatus));
        }
        eventStatistics.addStatistic(EventStatistic.builder()
                .statisticType(EventStatistic.StatisticType.BUFFER_INFORMATION)
                .statisticSubType(EventStatistic.StatisticSubType.IN_DISK_QUEUE)
                .statisticValue(0)
                .clearStatistic(true)
                .build());
        eventStatistics.addStatistic(EventStatistic.builder()
                .statisticType(EventStatistic.StatisticType.BUFFER_INFORMATION)
                .statisticSubType(EventStatistic.StatisticSubType.IN_MEMORY_QUEUE)
                .statisticValue(0)
                .clearStatistic(true)
                .build());
        eventStatistics.addStatistic(EventStatistic.builder()
                .statisticType(EventStatistic.StatisticType.BUFFER_INFORMATION)
                .statisticSubType(EventStatistic.StatisticSubType.DISK_BUFFER_FULL_EVENT)
                .statisticValue(1)
                .build());
    }

    private boolean areBuffersTerminated() {
        return TERMINATED.equals(currentFlowState.get());
    }

    private boolean isMemoryBufferAvailable() {
        return AVAILABLE.equals(memoryBufferStatus.get());
    }

    private boolean isMemoryReloadReady() {
        final Future<?> reloadExecutorStatusFuture = reloadExecutorStatus.get();
        return (isMemoryBufferAvailable() && ((reloadExecutorStatusFuture == null)
                || reloadExecutorStatusFuture.isDone() || reloadExecutorStatusFuture.isCancelled()));
    }

    private boolean isRelayPingReady() {
        final Future<?> checkRelayHealthStatusFuture = checkRelayHealthStatus.get();
        return ((checkRelayHealthStatusFuture == null) || checkRelayHealthStatusFuture.isDone()
                || checkRelayHealthStatusFuture.isCancelled());
    }

    /**
     * We only check object / file sizes every N operations to save on performance.
     */
    private boolean shouldCheckSizes() {
        if (operationsSinceLastCapacityCheck.incrementAndGet() >= NUMBER_OF_OPERATIONS_UNTIL_CAPACITY_CHECK) {
            operationsSinceLastCapacityCheck.set(0);
            return true;
        }
        return false;
    }

    private synchronized void resetBufferAvailability() {
        currentFlowState.set(STABLE_FLOW_TO_MEMORY);
        memoryBufferStatus.set(AVAILABLE);
    }

    private void clearBuffers() {
        memoryBuffer.clear();
        fileBuffer.clear();
    }

    /**
     * Shorthand method just to convert bytes for log printing.
     */
    private String convertBytesForLogging(long bytes, int unitConversion) {
        final double conversion = (double) bytes / unitConversion;
        return String.format("%.2f", conversion);
    }

    public enum BufferFlowStates {
        STABLE_FLOW_TO_MEMORY,
        UNSTABLE_OVERFLOW_TO_FILE,
        TERMINATED
    }

    public enum BufferStatus {
        AVAILABLE,
        FULL
    }
}