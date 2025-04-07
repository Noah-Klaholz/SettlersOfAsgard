package ch.unibas.dmi.dbis.cs108.client.ui.events;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class UIEventBus {
    private static UIEventBus instance;
    private final Map<Class<?>, Set<EventListener<?>>> listeners = new HashMap<>();

    private UIEventBus() {
    }

    public static UIEventBus getInstance() {
        if (instance == null) {
            instance = new UIEventBus();
        }
        return instance;
    }

    public <T> void subscribe(Class<T> eventType, EventListener<T> listener) {
        listeners.computeIfAbsent(eventType, k -> new HashSet<>()).add(listener);
    }

    public <T> void unsubscribe(Class<T> eventType, EventListener<T> listener) {
        Set<EventListener<?>> set = listeners.get(eventType);
        if (set != null) {
            set.remove(listener);
        }
    }

    @SuppressWarnings("unchecked")
    public <T> void publish(T event) {
        if (event == null) return;
        Set<EventListener<?>> set = listeners.get(event.getClass());
        if (set != null) {
            for (EventListener<?> listener : set) {
                ((EventListener<T>) listener).onEvent(event);
            }
        }
    }

    public interface EventListener<T> {
        void onEvent(T event);
    }
}