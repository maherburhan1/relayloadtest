package com.solarwinds.msp.ncentral.eventproduction.api.service.tracking;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Tracks running status of components.
 */
@Service
public class ComponentStatusService {
    private final Map<Class, AtomicBoolean> componentsRunningStatus = new ConcurrentHashMap<>();

    /**
     * Sets running state for a component.
     *
     * @param componentClass component
     * @param isRunning {@code true} when running, {@code false} when stopped
     * @param <T> component class
     */
    public <T> void setRunning(Class<T> componentClass, boolean isRunning) {
        componentsRunningStatus.computeIfAbsent(componentClass, (c) -> new AtomicBoolean(false)).set(isRunning);
    }

    /**
     * Gets running state of a component.
     *
     * @param componentClass component
     * @param <T> component class
     * @return {@code true} when running, {@code false} when stopped
     */
    public <T> boolean isRunning(Class<T> componentClass) {
        return componentsRunningStatus.computeIfAbsent(componentClass, (c) -> new AtomicBoolean(false)).get();
    }
}
