package ch.unibas.dmi.dbis.cs108.server.core.model;

import ch.unibas.dmi.dbis.cs108.server.core.logic.GameEventNotifier;
import ch.unibas.dmi.dbis.cs108.shared.game.Player;
import ch.unibas.dmi.dbis.cs108.shared.protocol.CommunicationAPI;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Logger;

/**
 * Core game state class that delegates to specialized managers
 */
public class GameState {
    /**
     * Logger to log logging
     */
    private static final Logger LOGGER = Logger.getLogger(GameState.class.getName());
    /**
     * State Lock for Thread safe handling
     */
    private final ReadWriteLock stateLock = new ReentrantReadWriteLock();
    /**
     * BoardManager that contains all info about the board & tiles
     */
    private final BoardManager boardManager;
    /**
     * List of players in the game (stored as player objects)
     */
    private final List<Player> players = new ArrayList<>();
    /**
     * GameEventNotifier to notify the game about events
     */
    private final GameEventNotifier notifier;
    /**
     * The index of the player whose turn it is (0 to 3)
     */
    private int playerRound;
    /**
     * The gameRound (0 to 4)
     */
    private int gameRound;
    /**
     * The name of the players whose turn it is
     */
    private String playerTurn;
    /**
     * A list of notifications (used for special effects)
     */
    private final List<String> notifications = new ArrayList<>();

    /**
     * Creates a new gameState object. Initializes the Board- and TurnManager.
     *
     * @param notifier the GameEventNotifier to notify the game about events
     */
    public GameState(GameEventNotifier notifier) {
        this.boardManager = new BoardManager(stateLock);
        this.boardManager.initializeBoard(8, 7);
        this.notifier = notifier;
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
     *
     * @return The current object of the BoardManager.
     */
    public BoardManager getBoardManager() {
        return boardManager;
    }

    /**
     * Gets the current state of the StateLock.
     *
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
            notifications.clear();
            players.forEach(Player::reset);
            boardManager.reset();
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
        notifications.add(s);
        notifier.sendMessageToPlayer(player, CommunicationAPI.NetworkProtocol.Commands.INFO.getCommand() + s);
    }

    /**
     * Gets the playerTurn
     *
     * @return the playerTurn
     */
    public String getPlayerTurn() {
        return playerTurn;
    }

    /**
     * Sets the playerTurn
     *
     * @param playerTurn the playerTurn to set
     */
    public void setPlayerTurn(String playerTurn) {
        this.playerTurn = playerTurn;
    }

    /**
     * Gets the playerRound
     *
     * @return the playerRound
     */
    public int getPlayerRound() {
        return playerRound;
    }

    /**
     * Sets the playerRound
     *
     * @param playerRound the playerRound to set
     */
    public void setPlayerRound(int playerRound) {
        this.playerRound = playerRound;
    }

    /**
     * Gets the gameRound
     *
     * @return the gameRound
     */
    public int getGameRound() {
        return gameRound;
    }

    /**
     * Sets the gameRound
     *
     * @param gameRound the gameRound to set
     */
    public void setGameRound(int gameRound) {
        this.gameRound = gameRound;
    }

    public GameEventNotifier getNotifier() {
        return notifier;
    }

    /**
     * Gets the list of notifications.
     *
     * @return the list
     */
    public List<String> getNotifications() {
        return notifications;
    }
}