package ch.unibas.dmi.dbis.cs108.server.core.events;

import ch.unibas.dmi.dbis.cs108.client.core.events.GameEvent;
import ch.unibas.dmi.dbis.cs108.client.core.observer.GameEventListener;

import java.util.ArrayList;
import java.util.List;

public class EventDispatcher {
    private List<GameEventListener> listeners = new ArrayList<GameEventListener>();

    public void register(GameEventListener listener) {
        listeners.add(listener);
    }

    public void dispatch(GameEvent event) {
        for (GameEventListener listener : listeners) {
            listener.onEvent(event);
        }
    }
}
