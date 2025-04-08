package ch.unibas.dmi.dbis.cs108.client.networking.events;

import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Logger;

/**
 * EventDispatcher is responsible for managing and dispatching events to registered listeners.
 * It allows listeners to register themselves and receive notifications when events occur.
 */
public class EventDispatcher {
    /**
     * Logger for the EventDispatcher class.
     */
    private static final Logger LOGGER = Logger.getLogger(EventDispatcher.class.getName());
    /**
     * Singleton instance of EventDispatcher.
     */
    private static final EventDispatcher INSTANCE = new EventDispatcher();
    /**
     * List of registered listeners that will be notified when an event occurs.
     */
    private final CopyOnWriteArrayList<EventListener<?>> listeners = new CopyOnWriteArrayList<>();
    /**
     * Private constructor to prevent instantiation.
     */

    /**
     * Private constructor to prevent instantiation.
     */
    public static EventDispatcher getInstance() {
        /**
         * Returns the singleton instance of EventDispatcher.
         */
        return INSTANCE;
    }

    /**
     * Registers a listener to receive events of a specific type.
     *
     * @param eventType The class of the event type.
     * @param listener  The listener to register.
     * @param <T>      The type of the event.
     */
    public <T extends Event> void registerListener(Class<T> eventType, EventListener<T> listener) {
        listeners.add(listener);
        LOGGER.fine("Registered listener for " + eventType.getSimpleName());
    }

    /**
     * Unregisters a listener from receiving events of a specific type.
     *
     * @param listener The listener to unregister.
     * @param <T>      The type of the event.
     */
    public <T extends Event> void unregisterListener(EventListener<T> listener) {
        listeners.remove(listener);
    }

    /**
     * Dispatches an event to all registered listeners.
     *
     * @param event The event to dispatch.
     * @param <T>   The type of the event.
     */
    @SuppressWarnings("unchecked")
    public <T extends Event> void dispatchEvent(T event) {
        for (EventListener<?> listener : listeners) {
            if (listener.getEventType().isInstance(event)) {
                try {
                    ((EventListener<T>) listener).onEvent(event);
                } catch (Exception e) {
                    LOGGER.warning("Error dispatching event: " + e.getMessage());
                }
            }
        }
    }

    /**
     * Interface for event listeners.
     *
     * @param <T> The type of the event.
     */
    public interface EventListener<T extends Event> {
        /**
         * Called when an event of type T occurs.
         *
         * @param event The event that occurred.
         */
        void onEvent(T event);

        /**
         * Returns the class of the event type this listener is interested in.
         *
         * @return The class of the event type.
         */
        Class<T> getEventType();
    }
}