package ch.unibas.dmi.dbis.cs108.server.core.structures;

import ch.unibas.dmi.dbis.cs108.server.core.logic.GameLogic;
import ch.unibas.dmi.dbis.cs108.server.networking.ClientHandler;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
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

    private static final Logger logger = Logger.getLogger(Lobby.class.getName());
    private final String id;
    private final List<ClientHandler> players;
    private final int maxPlayers;
    private LobbyStatus status;
    private GameLogic gameLogic;
    private ScheduledExecutorService turnScheduler; // For automatic turns

    public Lobby(String id, int maxPlayers) {
        this.id = id;
        this.maxPlayers = maxPlayers;
        this.players = new CopyOnWriteArrayList<>();
        this.status = LobbyStatus.IN_LOBBY;
        this.turnScheduler = Executors.newSingleThreadScheduledExecutor();
    }

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
        return this.gameLogic;
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
        System.out.println("Lobby started game");
        if (status == LobbyStatus.IN_GAME) {
            logger.warning("Game is already started in lobby " + id);
            return false;
        }

        if (!isFull()) {
            logger.warning("Could not start game in lobby " + id + ": " + players.size() + " players (expected " + maxPlayers + ")");
            return false;
        }

        status = LobbyStatus.IN_GAME;
        logger.info("Game started in lobby " + id);

        String[] playerNames = players.stream()
                .map(ClientHandler::getPlayerName)
                .toArray(String[]::new);

        this.gameLogic = new GameLogic(this);
        gameLogic.startGame(playerNames);

        // Start automatic turn scheduler (runs every minute)
        turnScheduler.scheduleAtFixedRate(
                () -> {
                    handleAutomaticTurn();
                    broadcastMessage("TURN$" + gameLogic.getTurnManager().getPlayerTurn());},
                1,  // Initial delay (1 minute)
                1,  // Period (1 minute)
                TimeUnit.MINUTES
        );

        return true;
    }

    /**
     * Ends the game.
     *
     */
    public void endGame() {
        if (status == LobbyStatus.IN_GAME) {
            status = LobbyStatus.GAME_ENDED;

            // Shutdown the turn scheduler
            turnScheduler.shutdown();
            try {
                if (!turnScheduler.awaitTermination(1, TimeUnit.SECONDS)) {
                    turnScheduler.shutdownNow();
                }
            } catch (InterruptedException e) {
                logger.warning("Interrupted while stopping turn scheduler: " + e.getMessage());
                Thread.currentThread().interrupt();
            }
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

    /**
     * Manually ends the current turn and resets the turn timer.
     */
    public void manualEndTurn() {
        if (status != LobbyStatus.IN_GAME) {
            logger.warning("Cannot end turn manually - game not in progress.");
            return;
        }

        // Call the method to handle turn transition
        handleAutomaticTurn();

        // Reset the scheduler for the next turn (cancel the current task)
        resetTurnTimer();
    }

    /**
     * Resets the turn timer, schedules the next turn after 1 minute.
     */
    private void resetTurnTimer() {
        // Cancel the current scheduled task
        turnScheduler.shutdownNow();
        try {
            if (!turnScheduler.awaitTermination(1, TimeUnit.SECONDS)) {
                turnScheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            logger.warning("Interrupted while stopping turn scheduler: " + e.getMessage());
            Thread.currentThread().interrupt();
        }

        // Reinitialize the scheduler and schedule the next turn
        turnScheduler = Executors.newSingleThreadScheduledExecutor();

        // Schedule next turn (1 minute delay)
        turnScheduler.scheduleAtFixedRate(
                () -> {
                    handleAutomaticTurn();
                    broadcastMessage("TURN$" + gameLogic.getTurnManager().getPlayerTurn());},
                1,  // Initial delay (1 minute)
                1,  // Period (1 minute)
                TimeUnit.MINUTES
        );
    }

    /**
     * Handles automatic turn progression (called by scheduler).
     */
    private void handleAutomaticTurn() {
        if (status != LobbyStatus.IN_GAME) {
            logger.warning("Cannot advance turn - game not in progress");
            return;
        }

        try {
            gameLogic.getTurnManager().nextTurn();
        } catch (Exception e) {
            logger.severe("Error during automatic turn: " + e.getMessage());
        }
    }

}