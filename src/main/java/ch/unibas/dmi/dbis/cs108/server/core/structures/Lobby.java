package ch.unibas.dmi.dbis.cs108.server.core.structures;

import ch.unibas.dmi.dbis.cs108.SETTINGS;
import ch.unibas.dmi.dbis.cs108.server.core.logic.GameEventNotifier;
import ch.unibas.dmi.dbis.cs108.server.core.logic.GameLogic;
import ch.unibas.dmi.dbis.cs108.server.core.model.Leaderboard;
import ch.unibas.dmi.dbis.cs108.server.networking.ClientHandler;
import ch.unibas.dmi.dbis.cs108.shared.game.Player;
import ch.unibas.dmi.dbis.cs108.shared.game.Tile;
import ch.unibas.dmi.dbis.cs108.shared.protocol.CommunicationAPI;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * An instance of this class represents a Lobby. It provides functionality regarding managing players and starting games.
 *
 * @author Vincent Schall
 * @version 1.0
 * @since 2025-03-19
 */
public class Lobby implements GameEventNotifier {

    /**
     * Logger to log logging
     */
    private static final Logger logger = Logger.getLogger(Lobby.class.getName());
    /**
     * Name of the Lobby (unique), serves as an ID.
     */
    private final String id;
    /**
     * The players, stored in a List of ClientHandlers.
     */
    private final List<ClientHandler> players;
    /**
     * The number of maximal Players allowed (currently always 4).
     */
    private final int maxPlayers;
    /**
     * Global leaderboard (gets it from the server)
     */
    private final Leaderboard leaderboard;
    /**
     * The status of the Lobby (In lobby, in game or game ended).
     */
    private LobbyStatus status;
    /**
     * The GameLogic corresponding to the game ongoing in the Lobby (only initialized when game starts).
     */
    private GameLogic gameLogic;
    /**
     * The turnScheduler responsible for automatically calling TurnManager.nextTurn() after a fixed time.
     */
    private ScheduledExecutorService turnScheduler; // For automatic turns

    /**
     * Creates the Lobby object and instantiates fields.
     *
     * @param id         The name of the Lobby as a String.
     * @param maxPlayers The number of maximum players as an Integer.
     *              @param leaderboard The leaderboard object to use.
     */
    public Lobby(String id, int maxPlayers, Leaderboard leaderboard) {
        this.id = id;
        this.maxPlayers = maxPlayers;
        this.players = new CopyOnWriteArrayList<>();
        this.status = LobbyStatus.IN_LOBBY;
        this.turnScheduler = Executors.newSingleThreadScheduledExecutor();
        this.leaderboard = leaderboard;
    }

    /**
     * Gets the ID of the Lobby.
     *
     * @return the current ID of the Lobby.
     */
    public String getId() {
        return id;
    }

    /**
     * Gets the number of maximum players.
     *
     * @return the number of maximum players.
     */
    public int getMaxPlayers() {
        return maxPlayers;
    }

    /**
     * Gets the players as a List of ClientHandlers.
     *
     * @return the current players in the Lobby.
     */
    public List<ClientHandler> getPlayers() {
        return players;
    }

    /**
     * Gets the turnScheduler.
     *
     * @return the TurnScheduler.
     */
    public ScheduledExecutorService getTurnScheduler() {
        return turnScheduler;
    }

    /**
     * Gets the GameLogic object in this Lobby. Only valid if the Game has started already.
     *
     * @return The GameLogic object in this Lobby.
     */
    public GameLogic getGameLogic() {
        if (status != LobbyStatus.IN_GAME) {
            logger.warning("Not yet in game, cannot return gameLogic from current Lobby.");
            return null;
        }
        return gameLogic;
    }

    /**
     * Sets the gameLogic field.
     *
     * @param gameLogic the object to set.
     */
    public void setGameLogic(GameLogic gameLogic) {
        this.gameLogic = gameLogic;
    }

    /**
     * Adds a player to the Lobby (if the Lobby is not full).
     *
     * @param player The player object to add.
     * @return if the action was successful.
     */
    public boolean addPlayer(ClientHandler player) {
        if (player == null) {
            logger.warning("Player is null, cannot add player to lobby.");
            return false;
        }
        if (!isFull() && !players.contains(player) && status == LobbyStatus.IN_LOBBY) {
            players.add(player);
            logger.info(player.getPlayerName() + " has joined Lobby: " + id);
            return true;
        }
        logger.warning(player.getPlayerName() + " could not join Lobby: " + id);
        return false;
    }

    /**
     * Removes a player from the Lobby (if the Lobby is not empty).
     *
     * @param player The player object to remove.
     * @return if the action was successful.
     */
    public boolean removePlayer(ClientHandler player) {
        if (player == null) {
            logger.warning("Player is null, cannot remove player from lobby.");
            return false;
        }
        if (!players.isEmpty() && players.contains(player)) {
            if (status == LobbyStatus.IN_GAME) {
                endGame();
            }
            players.remove(player);
            logger.info(player + " has been removed from Lobby: " + id);
            return true;
        }
        logger.warning(player + " was not removed from Lobby: " + id);
        return false;
    }

    /**
     * Gets the status of the Lobby.
     *
     * @return The status of the Lobby.
     */
    public String getStatus() {
        return status.getStatus();
    }

    /**
     * Checks if the Lobby is full.
     *
     * @return if the Lobby is full.
     */
    public boolean isFull() {
        return players.size() == maxPlayers;
    }

    /**
     * Checks if the Lobby is empty.
     *
     * @return if the Lobby is empty.
     */
    public boolean isEmpty() {
        return players.isEmpty();
    }

    /**
     * Returns the state of the Lobby (id, players, maxplayers, status) as a String.
     *
     * @return the state of the Lobby as a String.
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
     * Checks for starting conditions and starts a game.
     *
     * @return if the game was started correctly.
     */
    public boolean startGame() {
        if (status == LobbyStatus.IN_GAME) {
            logger.warning("Game is already started in lobby " + id);
            return false;
        }

        if (!isFull()) {
            logger.warning(String.format(
                    "Cannot start game in lobby %s: %d players (expected %d)",
                    id, players.size(), maxPlayers));
            return false;
        }

        status = LobbyStatus.IN_GAME;
        logger.info("Game started in lobby " + id);

        String[] playerNames = players.stream()
                .map(ClientHandler::getPlayerName)
                .toArray(String[]::new);

        this.gameLogic = new GameLogic(this);
        players.forEach(ClientHandler::startGame);
        gameLogic.startGame(playerNames);
        startTurnScheduler();
        return true;
    }

    /**
     * Starts the TurnScheduler by first resetting it and the calling
     * processTurnChange once after every minute if not stopped.
     */
    private void startTurnScheduler() {
        // Start automatic turn scheduler (runs every minute)
        stopTurnScheduler();
        turnScheduler = Executors.newSingleThreadScheduledExecutor();
        turnScheduler.scheduleAtFixedRate(
                this::processTurnChange,
                SETTINGS.Config.TURN_TIME.getValue(), SETTINGS.Config.TURN_TIME.getValue(), TimeUnit.SECONDS
        );
    }

    /**
     * Stops the TurnScheduler by resetting it.
     */
    public void stopTurnScheduler() {
        if (turnScheduler != null) {
            turnScheduler.shutdownNow();
            try {
                if (!turnScheduler.awaitTermination(1, TimeUnit.SECONDS)) {
                    logger.warning("Turn Scheduler failed to terminate");
                }
            } catch (InterruptedException e) {
                logger.warning("Interrupted while waiting for turn Scheduler to terminate");
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * Handles the request to start a new turn. Calls the GameLogic.TurnManager.nextTurn()
     * method and broadcasts a message to the players.
     */
    private void processTurnChange() {
        try {
            if (status != LobbyStatus.IN_GAME) {
                logger.warning("Turn change attempted while not in game");
                return;
            }
            gameLogic.getTurnManager().nextTurn();
            if (gameLogic.getGameState().getGameRound() > 4) {
                endGame();
                return;
            }
            broadcastTurnUpdate();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error while processing turn change", e);
        }
    }

    /**
     * Manually ends the current turn.
     *
     * @return true if the turn was ended successfully, false otherwise.
     */
    @Override
    public boolean manualEndTurn() {
        if (status != LobbyStatus.IN_GAME) {
            logger.warning("Cannot end turn manually - game not in progress.");
            return false;
        }

        processTurnChange();
        startTurnScheduler();
        return true;
    }

    /**
     * Sends a message to a specific player.
     *
     * @param player  The player to send the message to.
     * @param message The message to send.
     */
    @Override
    public void sendMessageToPlayer(String player, String message) {
        for (ClientHandler client : players) {
            if (client.getPlayerName().equals(player)) {
                client.sendMessage(message);
                return;
            }
        }
        logger.warning("Player " + player + " not found in lobby " + id);
    }

    /**
     * Ends the game.
     */
    @Override
    public void endGame() {
        if (status == LobbyStatus.IN_GAME) {
            status = LobbyStatus.GAME_ENDED;
            gameLogic.getTurnManager().giveFinalScores();
            broadcastMessage(CommunicationAPI.NetworkProtocol.Commands.ENDGAME.getCommand() + "$" + gameLogic.createFinalScoreMessage());
            gameLogic.getGameState().getPlayers().forEach(player -> {
                leaderboard.update(player.getName(), player.getRunes());
            });
            leaderboard.save();
            gameLogic.getGameState().reset();
            stopTurnScheduler();
        }
    }

    /**
     * Sends a message to every player in the Lobby.
     *
     * @param message The message to send.
     */
    @Override
    public void broadcastMessage(String message) {
        for (ClientHandler player : players) {
            player.sendMessage(message);
        }
    }

    /**
     * Lists the players as a String.
     *
     * @return The String of all players.
     */
    public String listPlayers() {
        if (players.isEmpty()) {
            return "No available players";
        }

        return players.stream()
                .map(ClientHandler::getPlayerName)
                .collect(Collectors.joining(", "));
    }

    /**
     * Returns the name of the host player.
     *
     * @return the name of the host player
     */
    public String getHostName() {
        return players.get(0).getPlayerName();
    }

    /**
     * Sends a message to all clients when called. It sends:
     * TURN$... when a player begins his turn.
     * ENDR$... when a gameRound ends.
     */
    private void broadcastTurnUpdate() {
        broadcastMessage("TURN$" + gameLogic.getGameState().getPlayerTurn());
        broadcastMessage(gameLogic.getGameState().createDetailedStatusMessage());
    }

    /**
     * Changes the name of a player in the game.
     *
     * @param oldName The old name of the player.
     * @param newName The new name of the player.
     */
    public void changeName(String oldName, String newName) {
        if (oldName == null || newName == null) {
            logger.warning("Old name or new name is null, cannot change name.");
            return;
        }
        if (oldName.equals(newName)) {
            logger.warning("Old name and new name are the same, cannot change name.");
            return;
        }
        if (gameLogic == null) {
            logger.warning("GameLogic is null, cannot change name.");
            return;
        }
        if (gameLogic.getGameState().findPlayerByName(oldName) == null) {
            logger.warning("Player with name " + oldName + " not found in game.");
            return;
        }
        // Set the newName to the player object
        Player p = gameLogic.getGameState().findPlayerByName(oldName);
        p.setName(newName);
        // Update the player name for all tiles
        p.getOwnedTiles().forEach(tile -> {
            tile.setOwnerName(newName);
        });
        // Update the player name of the current turn if the player of the oldName is the current player
        if (gameLogic.getGameState().getPlayerTurn().equals(oldName)) {
            gameLogic.getGameState().setPlayerTurn(newName);
        }
        broadcastMessage(gameLogic.getGameState().createDetailedStatusMessage());
    }

    /**
     * Defines the states the lobby can be in.
     * "IN_LOBBY" means the game has not started yet.
     * "IN_GAME" means the game is currently being played.
     * "GAME_ENDED" means the game has ended.
     */
    public enum LobbyStatus {
        /**
         * The game is in the lobby.
         */
        IN_LOBBY("In lobby"),
        /**
         * The game is in progress.
         */
        IN_GAME("In-Game"),
        /**
         * The game has ended.
         * This status is set when the game is over and the players are no longer in the game.
         */
        GAME_ENDED("Game has ended");
        /**
         * The game is in the lobby.
         */

        /**
         * The status of the Lobby.
         */
        private final String status;

        /**
         * Constructor for the LobbyStatus enum.
         *
         * @param status The status of the Lobby.
         */
        LobbyStatus(String status) {
            this.status = status;
        }

        /**
         * Gets the status of the Lobby.
         *
         * @return The status of the Lobby.
         */
        public String getStatus() {
            return status;
        }
    }
}