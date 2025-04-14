package ch.unibas.dmi.dbis.cs108.server.core.logic;

import ch.unibas.dmi.dbis.cs108.server.core.model.PlayerManager;
import ch.unibas.dmi.dbis.cs108.server.networking.ClientHandler;
import ch.unibas.dmi.dbis.cs108.server.core.actions.ArtifactActionHandler;
import ch.unibas.dmi.dbis.cs108.server.core.actions.StatueActionHandler;
import ch.unibas.dmi.dbis.cs108.server.core.actions.StructureActionHandler;
import ch.unibas.dmi.dbis.cs108.server.core.actions.TileActionHandler;
import ch.unibas.dmi.dbis.cs108.server.core.model.GameState;
import ch.unibas.dmi.dbis.cs108.shared.entities.Player;
import ch.unibas.dmi.dbis.cs108.shared.protocol.CommunicationAPI;

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
    private static final Logger LOGGER = Logger.getLogger(GameLogic.class.getName());

    // Thread safety mechanism
    private final ReadWriteLock gameLock = new ReentrantReadWriteLock();

    private final GameState gameState;
    private final CommandProcessor commandProcessor;
    private final TurnManager turnManager;
    private final ResourceManager resourceManager;
    private final TileActionHandler tileActionHandler;
    private final StructureActionHandler structureActionHandler;
    private final StatueActionHandler statueActionHandler;
    private final ArtifactActionHandler artifactActionHandler;

    private CommunicationAPI communicationApi;

    /**
     * Constructor initializes game with proper components
     */
    public GameLogic() {
        this.gameState = new GameState();
        this.resourceManager = new ResourceManager();
        this.turnManager = gameState.getTurnManager();
        this.tileActionHandler = new TileActionHandler(gameState, gameLock);
        this.structureActionHandler = new StructureActionHandler(gameState, gameLock);
        this.statueActionHandler = new StatueActionHandler(gameState, gameLock);
        this.artifactActionHandler = new ArtifactActionHandler(gameState, gameLock);
        this.commandProcessor = new CommandProcessor(this, new GameRules(gameState));
    }

    /**
     * Gets the GameState with thread-safe access.
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
     * Set the communication API for notifying clients
     */
    public void setCommunicationApi(CommunicationAPI communicationApi) {
        this.communicationApi = communicationApi;
        this.turnManager.setCommunicationApi(communicationApi);
    }

    /**
     * Starts the game.
     * @param players the names of the players as an array of Strings.
     */
    @Override
    public void startGame(String[] players) {
        gameLock.writeLock().lock();
        try {
            gameState.getPlayerManager().setPlayers(players);
            gameState.getBoardManager().initializeBoard(10, 10);
            gameState.getTurnManager().setGameRound(0);
            gameState.getTurnManager().nextTurn();
        } finally {
            gameLock.writeLock().unlock();
        }

    }

    /**
     * Process incoming message by delegating to CommandProcessor
     */
    @Override
    public void processMessage(String message) {
        if (message != null && !message.isEmpty()) {
            String response = commandProcessor.processCommand(message);
            if (communicationApi != null && response != null) {
                communicationApi.sendMessage(response);
            }
        }
    }

    /**
     * Finalizes and ends the current game with proper cleanup
     */
    @Override
    public void endGame() {
        gameLock.writeLock().lock();
        try {
            gameState.reset();
            if (communicationApi != null) {
                communicationApi.sendMessage("GAME_ENDED");
            }
        } finally {
            gameLock.writeLock().unlock();
        }
    }

    /**
     * Sorts the players based on scores.
     */
    public void sortPlayersByScore(List<Player> players) {
        players.sort(Comparator.comparingInt(Player::getRunes).reversed());
    }

    /**
     * Creates the final score message.
     * @return the final score message as a String.
     */
    public String createFinalScoreMessage() {
        List<Player> players = gameState.getPlayerManager().getPlayers();
        sortPlayersByScore(players);

        StringBuilder result = new StringBuilder("FINAL_SCORE$");
        for (Player player : players) {
            result.append(player.getName()).append("=").append(player.getRunes()).append(",");
        }
        return result.toString();
    }

    /**
     * Buy a tile on the board.
     * @return if the action was successful.
     */
    public boolean buyTile(int x, int y, String playerName) {
        return tileActionHandler.buyTile(x, y, playerName);
    }

    /**
     * Buy a structure.
     * @return if the action was successful.
     */
    public boolean buyStructure(String structureId, String playerName) {
        return structureActionHandler.buyStructure(structureId, playerName);
    }

    /**
     * Place a structure on the board.
     * @return if the action was successful.
     */
    public boolean placeStructure(int x, int y, int structureId, String playerName) {
        return structureActionHandler.placeStructure(x, y, structureId, playerName);
    }

    /**
     * Use a structure on the board.
     * @return if the action was successful.
     */
    public boolean useStructure(int x, int y, int structureId, String useType, String playerName) {
        return structureActionHandler.useStructure(x, y, structureId, useType, playerName);
    }

    /**
     * Buy a statue.
     * @return if the action was successful.
     */
    public boolean buyStatue(String statueId, String playerName) {
        return statueActionHandler.buyStatue(statueId, playerName);
    }

    /**
     * Upgrade a statue.
     * @return if the action was successful.
     */
    public boolean upgradeStatue(int x, int y, String statueId, String playerName) {
        return statueActionHandler.upgradeStatue(x, y, statueId, playerName);
    }

    /**
     * Use a statue.
     * @return if the action was successful.
     */
    public boolean useStatue(int x, int y, int statueId, String useType, String playerName) {
        return statueActionHandler.useStatue(x, y, statueId, useType, playerName);
    }

    /**
     * Use a field artifact
     * @return if the action was successful.
     */
    public boolean useFieldArtifact(int x, int y, int artifactId, String useType, String playerName) {
        return artifactActionHandler.useFieldArtifact(x, y, artifactId, useType, playerName);
    }

    /**
     * Use a player artifact.
     * @return if the action was successful.
     */
    public boolean usePlayerArtifact(int artifactId, String targetPlayer, String useType, String playerName) {
        return artifactActionHandler.usePlayerArtifact(artifactId, targetPlayer, useType, playerName);
    }

    /**
     * Gets the current state of the TurnManager.
     * @return The current TurnManager object.
     */
    public TurnManager getTurnManager() {
        return turnManager;
    }

    /**
     * Gets the current state of the ResourceManager.
     * @return The current ResourceManager object.
     */
    public ResourceManager getResourceManager() {
        return resourceManager;
    }

    /**
     * Gets the current state of the CommandProcessor.
     * @return The current object of the CommandProcessor.
     */
    public CommandProcessor getCommandProcessor() {
        return commandProcessor;
    }

    /**
     * Gets the current state of the TileActionHandler.
     * @return The current object of the TileActionHandler.
     */
    public TileActionHandler getTileActionHandler() {
        return tileActionHandler;
    }

    /**
     * Gets the current state of the StructureActionHandler.
     * @return The current object of the StructureActionHandler.
     */
    public StructureActionHandler getStructureActionHandlerActionHandler() {
        return structureActionHandler;
    }

    /**
     * Gets the current state of the ArtifactActionHandler.
     * @return The current object of the ArtifactActionHandler.
     */
    public ArtifactActionHandler getArtifactActionHandler() {
        return artifactActionHandler;
    }

    /**
     * Gets the current state of the StatueActionHandler.
     * @return The current object of the StatueActionHandler.
     */
    public StatueActionHandler getStatueActionHandler() {
        return statueActionHandler;
    }
}