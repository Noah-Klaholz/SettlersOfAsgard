package ch.unibas.dmi.dbis.cs108.client.core.observer;

import ch.unibas.dmi.dbis.cs108.client.core.events.GameEvent;

/**
 * Interface for listening to game events.
 * Implement this interface to handle specific game events in your application.
 */
public interface GameEventListener {
    /**
     * Called when a game event occurs.
     *
     * @param event The game event that occurred.
     */
    void onEvent(GameEvent event);
}