package ch.unibas.dmi.dbis.cs108.client.core.observer;

import ch.unibas.dmi.dbis.cs108.client.core.events.GameEvent;

public interface GameEventListener {
    void onEvent(GameEvent event);
}