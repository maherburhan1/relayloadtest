package com.solarwinds.msp.ncentral.eventproduction.api.service.tracking;

/**
 * Listener interface for registering eventing startup. Classes implementing this interface should register via {@link
 * EventingControlService#addStartupListenerOrExecuteStartup(EventingStartupListener)} if they want to receive startup
 * event.
 */
@FunctionalInterface
public interface EventingStartupListener {

    /**
     * This method is invoked during {@link EventingControlService#startEventing()}, when all system components such as
     * database access and DAOs are ready for use. It's up to implementation what actions will be taken based on
     * startupEvent.
     */
    void onEventingStart();
}
