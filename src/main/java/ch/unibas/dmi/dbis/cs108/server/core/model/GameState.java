package ch.unibas.dmi.dbis.cs108.server.core.model;

import ch.unibas.dmi.dbis.cs108.server.core.logic.GameEventNotifier;
import ch.unibas.dmi.dbis.cs108.server.core.logic.TurnManager;
import ch.unibas.dmi.dbis.cs108.shared.entities.Behaviors.StructureBehaviorRegistry;
import ch.unibas.dmi.dbis.cs108.shared.game.Player;
import ch.unibas.dmi.dbis.cs108.shared.protocol.CommunicationAPI;

import java.util.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Logger;

import java.io.*;
import java.nio.file.*;
import java.util.stream.Collectors;

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
    /** List of players in the game (stored as player objects) */
    private final List<Player> players = new ArrayList<>();
    /** GameEventNotifier to notify the game about events */
    private final GameEventNotifier notifier;
    /** The index of the player whose turn it is (0 to 3) */
    private int playerRound;
    /** The gameRound (0 to 4) */
    private int gameRound;
    /** The name of the players whose turn it is */
    private String playerTurn;
    /** Leaderboard: playerName -> points (runes) */
    private final Map<String, Integer> leaderboard = new HashMap<>();
    /** Path to the Leaderboard file */
    private static final String LEADERBOARD_FILE = "Leaderboard.txt";

    /**
     * Creates a new gameState object. Initializes the Board- and TurnManager.
     *
     * @param notifier the GameEventNotifier to notify the game about events
     */
    public GameState(GameEventNotifier notifier) {
        this.boardManager = new BoardManager(stateLock);
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

    /**
     * Sets the playerTurn
     *
     * @param playerTurn the playerTurn to set
     */
    public void setPlayerTurn(String playerTurn) {
        this.playerTurn = playerTurn;
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
     * Gets the playerTurn
     *
     * @return the playerTurn
     */
    public String getPlayerTurn() {
        return playerTurn;
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
     * Updates the leaderboard with the points of one player.
     * @param playerName name of the player
     * @param points points of the player
     */
    public void updateLeaderboard(String playerName, int points) {
        stateLock.writeLock().lock();
        try {
            leaderboard.merge(playerName, points, Integer::sum);
            saveLeaderboardToFile();
        }
        finally {
            stateLock.writeLock().unlock();
        }
    }

    /**
     * Saves the leaderboard to a file (sorted by points).
     */
    public void saveLeaderboardToFile() {
        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(LEADERBOARD_FILE))) {
            leaderboard.entrySet().stream()
                    .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                    .forEach(entry -> {
                        try {
                            writer.write(entry.getKey() + ": " + entry.getValue() + "\n");
                        }
                        catch (IOException e){
                            LOGGER.severe("Could not write Leaderboard to file: "+ e.getMessage());
                        }
                    });
        }
        catch (IOException e) {
            LOGGER.severe("Could not save leaderboard: "+ e.getMessage());
        }
    }

    /**
     * Loads the leaderboard from the file. Called at the start.
     */
    public void loadLeaderboard() {
        stateLock.readLock().lock();
        try {
            if(!Files.exists(Paths.get(LEADERBOARD_FILE))) {
                return;
            }
            try (BufferedReader reader = Files.newBufferedReader(Paths.get(LEADERBOARD_FILE))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] parts = line.split(": ");
                    if (parts.length == 2) {
                        leaderboard.put(parts[0], Integer.parseInt(parts[1]));
                    }
                }
            }
        }
        catch (IOException e) {
            LOGGER.severe("Could not read Leaderboard file: "+ e.getMessage());
        }
        finally {
            stateLock.writeLock().unlock();
        }
    }

    /**
     * Returns the leaderboard (sorted).
     *
     * @return the leaderboard.
     */
    public Map<String, Integer> getLeaderboard() {
        stateLock.readLock().lock();
        try {
            return leaderboard.entrySet().stream()
                    .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                    .collect(Collectors.toMap(
                            Map.Entry::getKey,
                            Map.Entry::getValue,
                            (e1, e2) -> e1,
                            LinkedHashMap::new
                    ));
        }
        finally {
            stateLock.writeLock().unlock();
        }
    }
}