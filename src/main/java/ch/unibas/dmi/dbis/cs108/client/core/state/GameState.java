package ch.unibas.dmi.dbis.cs108.client.core.state;

import ch.unibas.dmi.dbis.cs108.server.core.logic.TurnManager;
import ch.unibas.dmi.dbis.cs108.server.core.model.BoardManager;
import ch.unibas.dmi.dbis.cs108.shared.game.Player;
import java.util.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Logger;

public class GameState {
    /** Logger to log logging */
    private static final Logger LOGGER = Logger.getLogger(ch.unibas.dmi.dbis.cs108.client.core.state.GameState.class.getName());
    /** State Lock for Thread safe handling */
    private final ReadWriteLock stateLock = new ReentrantReadWriteLock();
    /** BoardManager that contains all info about the board & tiles */
    private final BoardManager boardManager;
    /** List of players in the game (stored as player objects) */
    private final List<Player> players = new ArrayList<>();
    /** The index of the player whose turn it is (0 to 3) */
    private int playerRound;
    /** The gameRound (0 to 4) */
    private int gameRound;

    /**
     * Creates a new GameState. Initializes the BoardManager.
     */
    public GameState() {
        this.boardManager = new BoardManager(stateLock);
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
     * Sets the gameRound
     *
     * @param gameRound the gameRound to set
     */
    public void setGameRound(int gameRound) {
        this.gameRound = gameRound;
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
     * Gets the gameRound
     *
     * @return the gameRound
     */
    public int getGameRound() {
        return gameRound;
    }

    /**
     * Sets the list of players
     *
     * @param playerNames the players to set
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
        } finally {
            stateLock.writeLock().unlock();
        }
    }

}