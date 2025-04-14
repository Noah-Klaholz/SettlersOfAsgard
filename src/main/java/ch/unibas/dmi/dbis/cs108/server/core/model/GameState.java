package ch.unibas.dmi.dbis.cs108.server.core.model;

import ch.unibas.dmi.dbis.cs108.server.core.logic.ResourceManager;
import ch.unibas.dmi.dbis.cs108.server.core.logic.TurnManager;

import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Logger;

/**
 * Core game state class that delegates to specialized managers
 */
public class GameState {
    private static final Logger LOGGER = Logger.getLogger(GameState.class.getName());

    private final ReadWriteLock stateLock = new ReentrantReadWriteLock();
    private final PlayerManager playerManager;
    private final BoardManager boardManager;
    private final TurnManager turnManager;
    private final StateObserverManager observerManager;

    public GameState() {
        this.observerManager = new StateObserverManager();
        this.playerManager = new PlayerManager(stateLock, observerManager);
        this.boardManager = new BoardManager(stateLock, observerManager);

        // Create ResourceManager first, then pass it with 'this' to TurnManager
        ResourceManager resourceManager = new ResourceManager();
        this.turnManager = new TurnManager(this, resourceManager);
    }

    public PlayerManager getPlayerManager() {
        return playerManager;
    }

    public BoardManager getBoardManager() {
        return boardManager;
    }

    public TurnManager getTurnManager() {
        return turnManager;
    }

    public ReadWriteLock getStateLock() {
        return stateLock;
    }

    /**
     * Reset the entire game state
     */
    public void reset() {
        stateLock.writeLock().lock();
        try {
            playerManager.reset();
            boardManager.reset();
            turnManager.reset();
            observerManager.notifyObservers(null);
        } finally {
            stateLock.writeLock().unlock();
        }
    }

    /**
     * Convenience method for backward compatibility
     */
    public String createStateMessage() {
        return new GameStateSerializer(this).createStateMessage();
    }

    /**
     * Convenience method for backward compatibility
     */
    public String createDetailedStatusMessage() {
        return new GameStateSerializer(this).createDetailedStatusMessage();
    }
}