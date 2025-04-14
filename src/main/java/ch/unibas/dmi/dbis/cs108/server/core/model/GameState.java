package ch.unibas.dmi.dbis.cs108.server.core.model;

import ch.unibas.dmi.dbis.cs108.server.core.logic.TurnManager;
import ch.unibas.dmi.dbis.cs108.shared.entities.Player;
import java.util.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Logger;

/**
 * Core game state class that delegates to specialized managers
 */
public class GameState {
    private static final Logger LOGGER = Logger.getLogger(GameState.class.getName());

    private final ReadWriteLock stateLock = new ReentrantReadWriteLock();
    private final BoardManager boardManager;
    private final TurnManager turnManager;
    private final List<Player> players = new ArrayList<>();

    public GameState() {
        this.boardManager = new BoardManager(stateLock);
        this.turnManager = new TurnManager(this);
    }

    public void setPlayers(String[] playerNames) {
        stateLock.writeLock().lock();
        try {
            players.clear();
            for (String name : playerNames) {
                players.add(new Player(name));
            }
        } finally {
            stateLock.writeLock().unlock();
        }
    }

    public List<Player> getPlayers() {
        stateLock.readLock().lock();
        try {
            return Collections.unmodifiableList(players);
        } finally {
            stateLock.readLock().unlock();
        }
    }

    public Player findPlayerByName(String name) {
        stateLock.readLock().lock();
        try {
            return players.stream()
                    .filter(p -> p.getName().equals(name))
                    .findFirst()
                    .orElse(null);
        } finally {
            stateLock.readLock().unlock();
        }
    }

    /**
     * Gets the current state of the BoardManager.
     * @return The current object of the BoardManager.
     */
    public BoardManager getBoardManager() {
        return boardManager;
    }

    /**
     * Gets the current state of the TurnManager.
     * @return The current object of the TurnManager.
     */
    public TurnManager getTurnManager() {
        return turnManager;
    }

    /**
     * Gets the current state of the StateLock.
     * @return The current object of the StateLock.
     */
    public ReadWriteLock getStateLock() {
        return stateLock;
    }

    /**
     * Reset the entire game state
     */
    public void reset() {
        stateLock.writeLock().lock();
        try {
            players.forEach(Player::reset);
            boardManager.reset();
            turnManager.reset();
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