package ch.unibas.dmi.dbis.cs108.client.networking.events;

import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Logger;

public class EventDispatcher {
    private static final Logger LOGGER = Logger.getLogger(EventDispatcher.class.getName());
    private static final EventDispatcher INSTANCE = new EventDispatcher();
    private final CopyOnWriteArrayList<EventListener<?>> listeners = new CopyOnWriteArrayList<>();

    public static EventDispatcher getInstance() {
        return INSTANCE;
    }

    public <T extends Event> void registerListener(Class<T> eventType, EventListener<T> listener) {
        listeners.add(listener);
        LOGGER.fine("Registered listener for " + eventType.getSimpleName());
    }

    public <T extends Event> void unregisterListener(EventListener<T> listener) {
        listeners.remove(listener);
    }

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

    public interface EventListener<T extends Event> {
        void onEvent(T event);

        Class<T> getEventType();
    }
}