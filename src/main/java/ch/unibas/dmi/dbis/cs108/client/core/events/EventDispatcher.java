package ch.unibas.dmi.dbis.cs108.client.core.events;
import java.util.ArrayList;
import java.util.List;
import ch.unibas.dmi.dbis.cs108.client.core.events.*;
import ch.unibas.dmi.dbis.cs108.client.core.observer.GameEventListener;

/**
 * EventDispatcher is responsible for managing and dispatching game events to registered listeners.
 * It allows listeners to register themselves and receive notifications when events occur.
 */
public class EventDispatcher {
    /**
     * List of registered listeners that will be notified when an event occurs.
     */
    private List<GameEventListener> listeners = new ArrayList<GameEventListener>();

    /**
     * Registers a listener to receive game events.
     *
     * @param listener The listener to register.
     */
    public void register(GameEventListener listener) {
        listeners.add(listener);
    }

    /**
     * Dispatches a game event to all registered listeners.
     *
     * @param event The event to dispatch.
     */
    public void dispatch(GameEvent event) {
        for (GameEventListener listener : listeners) {
            listener.onEvent(event);
        }
    }
}
