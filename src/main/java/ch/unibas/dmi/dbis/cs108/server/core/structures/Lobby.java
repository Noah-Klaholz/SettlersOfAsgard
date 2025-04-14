package ch.unibas.dmi.dbis.cs108.server.core.structures;

import ch.unibas.dmi.dbis.cs108.server.core.logic.GameLogic;
import ch.unibas.dmi.dbis.cs108.server.networking.ClientHandler;

import java.util.List;
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
public class Lobby {

    /**
     * Logger to log logging.
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
     * @param id The name of the Lobby as a String.
     * @param maxPlayers The number of maximum players as an Integer.
     */
    public Lobby(String id, int maxPlayers) {
        this.id = id;
        this.maxPlayers = maxPlayers;
        this.players = new CopyOnWriteArrayList<>();
        this.status = LobbyStatus.IN_LOBBY;
        this.turnScheduler = Executors.newSingleThreadScheduledExecutor();
    }

    /**
     * Gets the ID of the Lobby.
     * @return the current ID of the Lobby.
     */
    public String getId() {
        return id;
    }

    public List<ClientHandler> getPlayers() {
        return players;
    }

    public GameLogic getGameLogic() {
        if (status != LobbyStatus.IN_GAME) {
            logger.warning("Not yet in game, cannot return gameLogic from current Lobby.");
            return null;
        }
        return gameLogic;
    }

    public boolean addPlayer(ClientHandler player) {
        if (players.size() < maxPlayers && status == LobbyStatus.IN_LOBBY) {
            players.add(player);
            logger.info(player.toString() + " has joined Lobby: " + id);
            return true;
        }
        logger.warning(player.toString() + " could not join Lobby: " + id);
        return false;
    }

    public boolean removePlayer(ClientHandler player) {
        if (!players.isEmpty() && players.contains(player)) {
            players.remove(player);
            logger.info(player.toString() + " has been removed from Lobby: " + id);
            return true;
        }
        logger.warning(player.toString() + " was not removed from Lobby: " + id);
        return false;
    }

    public String getStatus() {
        return status.getStatus();
    }

    public boolean isFull() {
        return players.size() == maxPlayers;
    }

    public boolean isEmpty() {
        return players.isEmpty();
    }

    @Override
    public String toString() {
        return "Lobby{" +
                "id='" + id + '\'' +
                ", players=" + players.size() +
                ", maxPlayers=" + maxPlayers +
                ", status=" + getStatus() +
                '}';
    }

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
        gameLogic.startGame(playerNames);

        startTurnScheduler();
        return true;
    }

    private void startTurnScheduler() {
        // Start automatic turn scheduler (runs every minute)
        stopTurnScheduler();
        turnScheduler = Executors.newSingleThreadScheduledExecutor();
        turnScheduler.scheduleAtFixedRate(
                this::processTurnChange,
                1, 1, TimeUnit.MINUTES
        );
    }

    private void stopTurnScheduler() {
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

    private void processTurnChange() {
        try {
            if (status != LobbyStatus.IN_GAME) {
                logger.warning("Turn change attempted while not in game");
                return;
            }
            gameLogic.getTurnManager().nextTurn();
            broadcastTurnUpdate();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error while processing turn change", e);
        }
    }

    /**
     * Manually ends the current turn.
     */
    public void manualEndTurn() {
        if (status != LobbyStatus.IN_GAME) {
            logger.warning("Cannot end turn manually - game not in progress.");
            return;
        }

        processTurnChange();
        startTurnScheduler();
    }

    /**
     * Ends the game.
     */
    public void endGame() {
        if (status == LobbyStatus.IN_GAME) {
            status = LobbyStatus.GAME_ENDED;
            stopTurnScheduler();
        }
    }

    public void broadcastMessage(String message) {
        for (ClientHandler player : players) {
            player.sendMessage(message);
        }
    }

    public String listPlayers() {
        if (players.isEmpty()) {
            return "No available players";
        }

        return players.stream()
                .map(ClientHandler::getPlayerName)
                .collect(Collectors.joining(", "));
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

    private void broadcastTurnUpdate() {
        broadcastMessage("TURN$" + gameLogic.getTurnManager().getPlayerTurn());
        if (gameLogic.getTurnManager().isGameRoundComplete()) {
            broadcastMessage("ENDR$" + gameLogic.createFinalScoreMessage());
        }
    }

}