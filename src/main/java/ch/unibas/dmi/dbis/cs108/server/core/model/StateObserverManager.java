package ch.unibas.dmi.dbis.cs108.server.core.model;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Manages observers for the game state
 */
public class StateObserverManager {

    private final List<StateObserver> observers = new CopyOnWriteArrayList<>();

    /**
     * Add a state observer to be notified of changes
     */
    public void addObserver(StateObserver observer) {
        observers.add(observer);
    }

    /**
     * Remove a state observer
     */
    public void removeObserver(StateObserver observer) {
        observers.remove(observer);
    }

    /**
     * Notify all observers of state change
     */
    public void notifyObservers(GameState gameState) {
        for (StateObserver observer : observers) {
            observer.onStateChanged(gameState);
        }
    }

    /**
     * Interface for observer pattern to notify about state changes
     */
    public interface StateObserver {
        void onStateChanged(GameState gameState);
    }
}