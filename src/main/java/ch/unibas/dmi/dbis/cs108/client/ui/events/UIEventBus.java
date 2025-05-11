package ch.unibas.dmi.dbis.cs108.client.ui.events;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Thread-safe publish-subscribe event bus for UI events.
 * Supports multiple listeners per event type.
 */
public class UIEventBus {
    /**
     * Logger for UIEventBus.
     */
    private static final Logger LOGGER = Logger.getLogger(UIEventBus.class.getName());
    /**
     * Singleton instance of UIEventBus.
     */
    private static volatile UIEventBus instance;

    /**
     * Map of event types to their listeners.
     * Uses a concurrent hash map for thread-safe access.
     */
    private final Map<Class<?>, Set<EventListener<?>>> listeners = new ConcurrentHashMap<>();

    /**
     * Private constructor to enforce singleton pattern.
     */
    private UIEventBus() {
    }

    /**
     * Returns the singleton instance of UIEventBus.
     *
     * @return singleton instance
     */
    public static UIEventBus getInstance() {
        if (instance == null) {
            synchronized (UIEventBus.class) {
                if (instance == null) {
                    instance = new UIEventBus();
                }
            }
        }
        return instance;
    }

    /**
     * Subscribes a listener to an event type.
     *
     * @param eventType the event class to subscribe to
     * @param listener  the listener to notify
     * @param <T>       event type
     */
    public <T> void subscribe(Class<T> eventType, EventListener<T> listener) {
        listeners.computeIfAbsent(eventType, k -> new CopyOnWriteArraySet<>()).add(listener);
    }

    /**
     * Unsubscribes a listener from an event type.
     *
     * @param eventType the event class to unsubscribe from
     * @param listener  the listener to remove
     * @param <T>       event type
     */
    public <T> void unsubscribe(Class<T> eventType, EventListener<T> listener) {
        Set<EventListener<?>> set = listeners.get(eventType);
        if (set != null)
            set.remove(listener);
    }

    /**
     * Publishes an event to all listeners of its type.
     *
     * @param event the event to publish
     * @param <T>   event type
     */
    @SuppressWarnings("unchecked")
    public <T> void publish(T event) {
        if (event == null)
            return;
        Set<EventListener<?>> set = listeners.get(event.getClass());
        if (set != null) {
            for (EventListener<?> listener : set) {
                try {
                    ((EventListener<T>) listener).onEvent(event);
                } catch (Exception e) {
                    LOGGER.log(Level.SEVERE, "Exception in event listener", e);
                }
            }
        }
    }

    /**
     * Listener interface for events.
     *
     * @param <T> event type
     */
    public interface EventListener<T> {
        void onEvent(T event);
    }
}
