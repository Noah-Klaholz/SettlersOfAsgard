package ch.unibas.dmi.dbis.cs108.server.core.model;

import ch.unibas.dmi.dbis.cs108.server.core.logic.GameEventNotifier;
import ch.unibas.dmi.dbis.cs108.server.core.logic.TurnManager;
import ch.unibas.dmi.dbis.cs108.shared.game.Player;
import ch.unibas.dmi.dbis.cs108.shared.protocol.CommunicationAPI;

import java.util.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Logger;

/**
 * Core game state class that delegates to specialized managers
 */
public class GameState {
    /** Logger to log logging */
    private static final Logger LOGGER = Logger.getLogger(GameState.class.getName());
    /** State Lock for Thread safe handling */
    private final ReadWriteLock stateLock = new ReentrantReadWriteLock();
    /** BoardManager that contains all info about the board & tiles */
    private final BoardManager boardManager;
    /** TurnManager that handles all turn-based logic */
    private final TurnManager turnManager;
    /** List of players in the game (stored as player objects) */
    private final List<Player> players = new ArrayList<>();
    /** GameEventNotifier to notify the game about events */
    private final GameEventNotifier notifier;

    /**
     * Creates a new gameState object. Initializes the Board- and TurnManager.
     */
    public GameState(GameEventNotifier notifier) {
        this.boardManager = new BoardManager(stateLock);
        this.turnManager = new TurnManager(this);
        this.notifier = notifier;
    }

    /**
     * Sets the players based on a String array of names
     *
     * @param playerNames the names of the players
     */
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

    /**
     * Gets the list of players
     *
     * @return the players
     */
    public List<Player> getPlayers() {
        stateLock.readLock().lock();
        try {
            return Collections.unmodifiableList(players);
        } finally {
            stateLock.readLock().unlock();
        }
    }

    /**
     * Finds the player with a given name
     *
     * @param name the name of the player
     * @return the player object
     */
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
     * create a detailed status message containing all info
     *
     * @return a String of the status message
     */
    public String createDetailedStatusMessage() {
        return new GameStateSerializer(this).createDetailedStatusMessage();
    }

    public void sendNotification(String player, String s) {
        notifier.sendMessageToPlayer(player, CommunicationAPI.NetworkProtocol.Commands.INFO.getCommand() + s);
    }
}