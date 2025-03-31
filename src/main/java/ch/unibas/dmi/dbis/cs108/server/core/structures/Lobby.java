package ch.unibas.dmi.dbis.cs108.server.core.structures;

import ch.unibas.dmi.dbis.cs108.server.networking.ClientHandler;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * An instance of this class represents a Lobby. It provides functionality regarding managing players and starting games.
 *
 * @author Vincent Schall
 * @version 1.0
 * @since 2025-03-19
 */
public class Lobby {

    /**
     * Logger to log important events.
     */
    private static final Logger logger = Logger.getLogger(Lobby.class.getName());
    /**
     * A unique String representing the name of the lobby.
     */
    private final String id;
    /**
     * A list of the current players in the lobby.
     */
    private final List<ClientHandler> players;
    /**
     * The maximal number of players that are allowed in one lobby.
     */
    private final int maxPlayers;
    /**
     * An enum indicating the status of the lobby
     */
    private LobbyStatus status;

    /**
     * Constructs a new Lobby.
     *
     * @param id         The unique String containing the name of the lobby.
     * @param maxPlayers The maximal amount of players allowed in the lobby.
     */
    public Lobby(String id, int maxPlayers) {
        this.id = id;
        this.maxPlayers = maxPlayers;
        this.players = new CopyOnWriteArrayList<>();
        this.status = LobbyStatus.IN_LOBBY;
    }

    /**
     * Gets the value of the ID.
     *
     * @return The current value of the ID.
     */
    public String getId() {
        return id;
    }

    /**
     * Gets the list of current players in the lobby.
     *
     * @return The current list of players in the lobby.
     */
    public List<ClientHandler> getPlayers() {
        return players;
    }

    /**
     * Adds a player to the lobby if two requirements are met:
     * 1. There are at most maxPlayers-1 players already in the lobby.
     * 2. The game has not started yet.
     *
     * @param player The player that wants to join the lobby.
     * @return True if the action was successful, else false.
     */
    public boolean addPlayer(ClientHandler player) {
        if (players.size() < maxPlayers && status == LobbyStatus.IN_LOBBY) {
            players.add(player);
            logger.info(player.toString() + " has joined Lobby: " + id);
            return true;
        }
        logger.warning(player.toString() + " could not join Lobby: " + id);
        return false;
    }

    /**
     * Removes a player from the lobby if two requirements are met:
     * 1. The list of players is not empty.
     * 2. The player is currently in the lobby.
     *
     * @param player The player that wants to exit the lobby.
     * @return True if the action was successful, else false.
     */
    public boolean removePlayer(ClientHandler player) {
        if (!players.isEmpty() && players.contains(player)) {
            players.remove(player);
            logger.info(player.toString() + " has been removed from Lobby: " + id);
            return true;
        }
        logger.warning(player.toString() + " was not removed from Lobby: " + id);
        return false;
    }

    /**
     * Gets the value of isGameStarted.
     *
     * @return The current value of isGameStarted as a String
     */
    public String getStatus() {
        return status.getStatus();
    }

    /**
     * Checks if the lobby is full, meaning the amount of players is equal to maxPlayers.
     *
     * @return True if the lobby is full, else false.
     */
    public boolean isFull() {
        return players.size() == maxPlayers;
    }

    /**
     * Checks if the lobby is empty
     *
     * @return True if the lobby is empty, else false
     */
    public boolean isEmpty() {
        return players.isEmpty();
    }

    /**
     * Returns the state of the lobby, including:
     * 1. The id
     * 2. The players
     * 3. The maximum amount of players
     * 4. If the game has started
     *
     * @return The state of the game as a String.
     */
    @Override
    public String toString() {
        return "Lobby{" +
                "id='" + id + '\'' +
                ", players=" + players.size() +
                ", maxPlayers=" + maxPlayers +
                ", status=" + getStatus() +
                '}';
    }

    /**
     * Starts the game in the lobby.
     *
     * @return true if the game was started successfully, false otherwise.
     */
    public boolean startGame() {
        // Check if the game is already started
        if (status == LobbyStatus.IN_GAME) {
            logger.warning("Game is already started in lobby " + id);
            return false;
        }

        // Check if the lobby has the required number of players
        if (players.size() != maxPlayers) {
            logger.warning("Could not start game in lobby " + id + ": " + players.size() + " players (expected " + maxPlayers + ")");
            return false;
        }

        // Start the game
        status = LobbyStatus.IN_GAME;
        logger.info("Game started in lobby " + id);

        // Notify all players that the game has started
        for (ClientHandler player : players) {
            player.sendMessage("GAME_STARTED:");
        }

        // Additional game initialization logic can go here
        // start game here

        return true;
    }

    /**
     * Ends the game
     *
     * @return true if the operation was successful, false otherwise
     */
    public boolean endGame() {
        if (status == LobbyStatus.IN_GAME) {
            status = LobbyStatus.GAME_ENDED;
        }
        return true;
    }

    /**
     * Broadcasts a message to all players in the lobby.
     *
     * @param message The message to broadcast
     */
    public void broadcastMessage(String message) {
        for (ClientHandler player : players) {
            player.sendMessage(message);
        }
    }

    public String listPlayers() {
        if (players.isEmpty()) {
            return "Players: No available players";
        }

        String playerList = players.stream()
                .map(ClientHandler::getPlayerName)
                .collect(Collectors.joining(", "));

        return "Players: " + playerList;
    }

    public enum LobbyStatus {
        IN_LOBBY("In lobby"),
        IN_GAME("In-Game"),
        GAME_ENDED("Game has ended");

        private final String status;

        LobbyStatus(String status) {
            this.status = status;
        }

        public String getStatus() {
            return status;
        }
    }
}