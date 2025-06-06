package ch.unibas.dmi.dbis.cs108.server.core.logic;

import ch.unibas.dmi.dbis.cs108.server.core.actions.ArtifactActionHandler;
import ch.unibas.dmi.dbis.cs108.server.core.actions.StatueActionHandler;
import ch.unibas.dmi.dbis.cs108.server.core.actions.StructureActionHandler;
import ch.unibas.dmi.dbis.cs108.server.core.actions.TileActionHandler;
import ch.unibas.dmi.dbis.cs108.server.core.model.GameState;
import ch.unibas.dmi.dbis.cs108.server.core.structures.Command;
import ch.unibas.dmi.dbis.cs108.shared.game.Player;
import ch.unibas.dmi.dbis.cs108.shared.protocol.CommunicationAPI;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Logger;

/**
 * Implementation of the GameLogicInterface with proper concurrency control.
 * Acts as a facade coordinating specialized components.
 */
public class GameLogic implements GameLogicInterface {
    /**
     * The logger for this class.
     */
    private static final Logger LOGGER = Logger.getLogger(GameLogic.class.getName());

    // Thread safety mechanism
    /**
     * The lock for the game logic.
     * It uses a ReadWriteLock to allow multiple readers or one writer at a time.
     */
    private final ReadWriteLock gameLock = new ReentrantReadWriteLock();

    /**
     * The GameEventNotifier to notify game events.
     */
    private final GameEventNotifier notifier;
    /**
     * The GameState object representing the current state of the game.
     */
    private final GameState gameState;
    /**
     * The CommandProcessor to process commands.
     */
    private final CommandProcessor commandProcessor;
    /**
     * The TurnManager to manage the turns of the players.
     */
    private final TurnManager turnManager;
    /**
     * The TileActionHandler to handle tile-related actions.
     */
    private final TileActionHandler tileActionHandler;
    /**
     * The StructureActionHandler to handle structure-related actions.
     */
    private final StructureActionHandler structureActionHandler;
    /**
     * The StatueActionHandler to handle statue-related actions.
     */
    private final StatueActionHandler statueActionHandler;
    /**
     * The ArtifactActionHandler to handle artifact-related actions.
     */
    private final ArtifactActionHandler artifactActionHandler;

    /**
     * Constructor initializes game with proper components
     *
     * @param notifier the GameEventNotifier to notify game events
     */
    public GameLogic(GameEventNotifier notifier) {
        this.notifier = notifier;
        this.gameState = new GameState(notifier);
        this.turnManager = new TurnManager(gameState);
        this.tileActionHandler = new TileActionHandler(gameState, gameLock);
        this.structureActionHandler = new StructureActionHandler(gameState, gameLock);
        this.statueActionHandler = new StatueActionHandler(gameState, gameLock);
        this.artifactActionHandler = new ArtifactActionHandler(gameState, gameLock);
        this.commandProcessor = new CommandProcessor(this);
    }

    /**
     * Gets the GameState with thread-safe access.
     *
     * @return the current object of the GameState.
     */
    public GameState getGameState() {
        gameLock.readLock().lock();
        try {
            return gameState;
        } finally {
            gameLock.readLock().unlock();
        }
    }

    /**
     * Gets the GameEventNotifier related to this GameLogic object.
     *
     * @return The current object implementing the GameEventNotifier interface (Lobby).
     */
    public GameEventNotifier getNotifier() {
        return notifier;
    }

    /**
     * Starts the game.
     *
     * @param players the names of the players as an array of Strings.
     */
    @Override
    public void startGame(String[] players) {
        gameLock.writeLock().lock();
        try {
            gameState.setPlayers(players);
            turnManager.nextTurn();
        } finally {
            gameLock.writeLock().unlock();
        }
    }

    /**
     * Process an incoming message by delegating to CommandProcessor
     */
    @Override
    public void processCommand(Command command) {
        if (command != null) {
            String response = commandProcessor.processCommand(command);
            if (notifier != null && response != null) {
                // Send the response of the command (e.g. Error or Ok message)
                if (response.startsWith(CommunicationAPI.NetworkProtocol.Commands.ERROR.getCommand())) {
                    LOGGER.warning("Error processing game command: " + command + " led to response: " + response);
                    notifier.sendMessageToPlayer(command.getPlayer().getName(), response);
                } else {
                    //LOGGER.info("Command processed successfully: " + response);
                }
                // Send an updated version of the GameState to all players
                notifier.broadcastMessage(gameState.createDetailedStatusMessage());
            }
        }
    }

    /**
     * Creates the final score message without modifying the original player order.
     *
     * @return the final score message as a String in the format:
     * player1$score1$player2$score2$player3$score3$player4$score4
     * (ordered from highest to lowest score)
     */
    public String createFinalScoreMessage() {
        // Create a new sorted list without modifying the original
        List<Player> sortedPlayers = new ArrayList<>(gameState.getPlayers());
        sortedPlayers.sort(Comparator.comparingInt(Player::getRunes).reversed());

        StringBuilder result = new StringBuilder();
        for (Player player : sortedPlayers) {
            result.append(player.getName())
                    .append("$")
                    .append(player.getRunes())
                    .append("$");
        }
        // remove the last '$'
        if (!result.isEmpty()) {
            result.setLength(result.length() - 1);
        }

        return result.toString();
    }

    /**
     * Buy a tile on the board.
     *
     * @param x          the x-coordinate of the tile.
     * @param y          the y-coordinate of the tile.
     * @param playerName the name of the player buying the tile.
     * @return if the action was successful.
     */
    public boolean buyTile(int x, int y, String playerName) {
        return tileActionHandler.buyTile(x, y, playerName);
    }

    /**
     * Place a structure on the board.
     *
     * @param x the x-coordinate of the tile.
     * @param y the y-coordinate of the tile.
     * @param structureId the ID of the structure.
     * @param playerName  the name of the player placing the structure.
     * @return if the action was successful.
         */
    public boolean placeStructure(int x, int y, int structureId, String playerName) {
        return structureActionHandler.placeStructure(x, y, structureId, playerName);
    }

    /**
     * Use a structure on the board.
     *
     * @param x           the x-coordinate of the tile.
     * @param y           the y-coordinate of the tile.
     * @param structureId the ID of the structure.
     * @param playerName  the name of the player using the structure.
     * @return if the action was successful.
     */
    public boolean useStructure(int x, int y, int structureId, String playerName) {
        return structureActionHandler.useStructure(x, y, structureId, playerName);
    }

    /**
     * Buy a statue.
     *
     * @param statueId   the ID of the statue.
     * @param playerName the name of the player buying the statue.
     * @param x          the x-coordinate of the tile.
     * @param y          the y-coordinate of the tile.
     * @return if the action was successful.
     */
    public boolean placeStatue(int x, int y, int statueId, String playerName) {
        return statueActionHandler.placeStatue(x, y, statueId, playerName);
    }

    /**
     * Upgrade a statue.
     *
     * @param statueId   the ID of the statue.
     * @param playerName the name of the player buying the statue.
     * @param x          the x-coordinate of the tile.
     * @param y          the y-coordinate of the tile.
     * @return if the action was successful.
     */
    public boolean upgradeStatue(int x, int y, int statueId, String playerName) {
        return statueActionHandler.upgradeStatue(x, y, statueId, playerName);
    }

    /**
     * Use a statue.
     *
     * @param statueId   the ID of the statue.
     * @param playerName the name of the player buying the statue.
     * @param x          the x-coordinate of the tile.
     * @param y          the y-coordinate of the tile.
     * @param params     the parameters for the statue action.
     * @return if the action was successful.
     */
    public boolean useStatue(int x, int y, int statueId, String playerName, String params) {
        return statueActionHandler.useStatue(x, y, statueId, playerName, params);
    }

    /**
     * Use a field artifact
     *
     * @param artifactId the ID of the artifact.
     * @param playerName the name of the player buying the statue.
     * @param x          the x-coordinate of the tile.
     * @param y          the y-coordinate of the tile.
     * @return if the action was successful.
     */
    public boolean useFieldArtifact(int x, int y, int artifactId, String playerName) {
        return artifactActionHandler.useFieldArtifact(x, y, artifactId, playerName);
    }

    /**
     * Use a player artifact.
     *
     * @param artifactId   the ID of the artifact.
     * @param playerName   the name of the player buying the statue.
     * @param targetPlayer the name of the target player.
     * @return if the action was successful.
     */
    public boolean usePlayerArtifact(int artifactId, String targetPlayer, String playerName) {
        return artifactActionHandler.usePlayerArtifact(artifactId, targetPlayer, playerName);
    }

    /**
     * Cheat: The player claims all tiles that are not already owned by someone.
     *
     * @param playerName the name of the player.
     * @return true if the action was successful, false otherwise.
     */
    public boolean claimAll(String playerName) {
        return tileActionHandler.claimAllTiles(playerName);
    }

    /**
     * Cheat: The player invokes ragnarok and destroys all enemy structures.
     *
     * @param playerName the name of the player.
     * @return true if the action was successful, false otherwise.
     */
    public boolean ragnarok(String playerName) {
        return structureActionHandler.ragnarok(playerName);
    }

    /**
     * Gets the current state of the TurnManager.
     *
     * @return The current TurnManager object.
     */
    public TurnManager getTurnManager() {
        return turnManager;
    }

    /**
     * Gets the current state of the CommandProcessor.
     *
     * @return The current object of the CommandProcessor.
     */
    public CommandProcessor getCommandProcessor() {
        return commandProcessor;
    }

    /**
     * Gets the current state of the TileActionHandler.
     *
     * @return The current object of the TileActionHandler.
     */
    public TileActionHandler getTileActionHandler() {
        return tileActionHandler;
    }

    /**
     * Gets the current state of the StructureActionHandler.
     *
     * @return The current object of the StructureActionHandler.
     */
    public StructureActionHandler getStructureActionHandlerActionHandler() {
        return structureActionHandler;
    }

    /**
     * Gets the current state of the ArtifactActionHandler.
     *
     * @return The current object of the ArtifactActionHandler.
     */
    public ArtifactActionHandler getArtifactActionHandler() {
        return artifactActionHandler;
    }

    /**
     * Gets the current state of the StatueActionHandler.
     *
     * @return The current object of the StatueActionHandler.
     */
    public StatueActionHandler getStatueActionHandler() {
        return statueActionHandler;
    }

}
