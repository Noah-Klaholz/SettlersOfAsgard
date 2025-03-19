package ch.unibas.dmi.dbis.cs108.server;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Logger;

/**
 * An instance of this class represents a Lobby. It provides functionality regarding managing players and starting games.
 *
 * @author Vincent Schall
 * @version 1.0
 * @since 2025-03-19
 */
public class Lobby {

    /**
     * A unique String representing the name of the lobby.
     */
    private String id;

    /**
     * A list of the current players in the lobby.
     */
    private List<ClientHandler> players;

    /**
     * The maximal number of players that are allowed in one lobby.
     */
    private int maxPlayers;

    /**
     * A boolean to indicate if the game is running or not.
     */
    private boolean isGameStarted;

    /**
     * Logger to log important events.
     */
    private static final Logger logger = Logger.getLogger(Lobby.class.getName());

    /**
     * Constructs a new Lobby.
     *
     * @param id The unique String containing the name of the lobby.
     * @param maxPlayers The maximal amount of players allowed in the lobby.
     */
    public Lobby(String id, int maxPlayers) {
        this.id = id;
        this.maxPlayers = maxPlayers;
        this.players = new CopyOnWriteArrayList<>();
        this.isGameStarted = false;
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
        if(players.size() < maxPlayers && !isGameStarted) {
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
        if(!players.isEmpty() && players.contains(player)) {
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
     * @return The current value of isGameStarted.
     */
    public boolean isGameStarted(){
        return isGameStarted;
    }

    /**
     * Checks if the lobby is full, meaning the amount of players is equal to maxPlayers.
     *
     * @return True if the lobby is full, else false.
     */
    public boolean isFull(){
        return players.size() == maxPlayers;
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
                ", isGameStarted=" + isGameStarted +
                '}';
    }


}