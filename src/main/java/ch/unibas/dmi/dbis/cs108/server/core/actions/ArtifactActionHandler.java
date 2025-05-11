package ch.unibas.dmi.dbis.cs108.server.core.actions;

import ch.unibas.dmi.dbis.cs108.server.core.model.GameState;
import ch.unibas.dmi.dbis.cs108.shared.entities.Behaviors.ArtifactBehaviorRegistry;
import ch.unibas.dmi.dbis.cs108.shared.entities.Findables.Artifact;
import ch.unibas.dmi.dbis.cs108.shared.game.Player;
import ch.unibas.dmi.dbis.cs108.shared.game.Tile;

import java.util.concurrent.locks.ReadWriteLock;

/**
 * Handles all artifact-related actions in the game.
 * <p>
 * This class is responsible for managing artifact usage, including field artifacts
 * that target specific locations and player artifacts that target specific players.
 * It ensures proper validation of all actions and coordinates with the ArtifactBehaviorRegistry
 * to execute artifact effects.
 * </p>
 * <p>
 * All methods use thread-safe locking to ensure data consistency during game state changes.
 * </p>
 */
public class ArtifactActionHandler {
    /**
     * The game state that this handler operates on.
     */
    private final GameState gameState;
    /**
     * The lock used for thread safety.
     */
    private final ReadWriteLock gameLock;
    /**
     * The registry that contains all artifact behaviors.
     */
    private final ArtifactBehaviorRegistry registry = new ArtifactBehaviorRegistry();

    /**
     * Creates a new ArtifactActionHandler with the specified game state and lock.
     *
     * @param gameState The current game state
     * @param gameLock  The lock used for thread safety
     */
    public ArtifactActionHandler(GameState gameState, ReadWriteLock gameLock) {
        this.gameState = gameState;
        this.gameLock = gameLock;
    }

    /**
     * Uses an artifact on a specific field/tile on the board.
     *
     * @param x          The x-coordinate on the board
     * @param y          The y-coordinate on the board
     * @param artifactId The ID of the artifact to use
     * @param playerName The name of the player using the artifact
     * @return true if the artifact was successfully used, false otherwise
     */
    public boolean useFieldArtifact(int x, int y, int artifactId, String playerName) {
        return executeWithLock(() -> {
            System.out.println("[ArtifactActionHandler] useFieldArtifact called with x=" + x + ", y=" + y + ", artifactId=" + artifactId + ", playerName=" + playerName);

            // Validate player
            Player player = gameState.findPlayerByName(playerName);
            if (player == null) {
                System.out.println("[ArtifactActionHandler] Player not found: " + playerName);
                return false;
            }

            // Verify the player has the artifact
            Artifact artifact = findPlayerArtifact(player, artifactId);
            if (artifact == null) {
                System.out.println("[ArtifactActionHandler] Artifact with ID " + artifactId + " not found in player inventory.");
                return false;
            }

            // Validate target tile
            Tile targetTile = gameState.getBoardManager().getTile(x, y);
            if (targetTile == null) {
                System.out.println("[ArtifactActionHandler] Target tile (" + x + "," + y + ") is null.");
                return false;
            }

            // Check if artifact is a field artifact
            if (!(artifact.getUseType() == Artifact.UseType.FIELD || artifact.getUseType() == Artifact.UseType.TRAP)) {
                System.out.println("[ArtifactActionHandler] Artifact " + artifact.getName() + " is not a FIELD artifact.");
                return false;
            }

            // Execute artifact effect
            boolean success = registry.executeFieldArtifact(artifact, gameState, player, x, y);
            System.out.println("[ArtifactActionHandler] registry.executeFieldArtifact returned: " + success);

            // Remove artifact from player inventory if used successfully
            if (success) {
                player.removeArtifact(artifact);
                System.out.println("[ArtifactActionHandler] Artifact removed from player inventory.");
            } else {
                System.out.println("[ArtifactActionHandler] Artifact NOT removed (effect failed).");
            }

            return success;
        });
    }

    /**
     * Uses an artifact on a specific player.
     *
     * @param artifactId       The ID of the artifact to use
     * @param targetPlayerName The name of the player being targeted
     * @param playerName       The name of the player using the artifact
     * @return true if the artifact was successfully used, false otherwise
     */
    public boolean usePlayerArtifact(int artifactId, String targetPlayerName, String playerName) {
        return executeWithLock(() -> {
            System.out.println("[ArtifactActionHandler] usePlayerArtifact called with artifactId=" + artifactId + ", targetPlayerName=" + targetPlayerName + ", playerName=" + playerName);

            // Validate player
            Player player = gameState.findPlayerByName(playerName);
            if (player == null) {
                System.out.println("[ArtifactActionHandler] Player not found: " + playerName);
                return false;
            }

            // Verify the player has the artifact
            Artifact artifact = findPlayerArtifact(player, artifactId);
            if (artifact == null) {
                System.out.println("[ArtifactActionHandler] Artifact with ID " + artifactId + " not found in player inventory.");
                return false;
            }

            // Validate target player
            Player targetPlayer = gameState.findPlayerByName(targetPlayerName);
            if (targetPlayer == null) {
                System.out.println("[ArtifactActionHandler] Target player not found: " + targetPlayerName);
                return false;
            }

            // Check if artifact is a player artifact
            if (!(artifact.getUseType() == Artifact.UseType.PLAYER)) {
                System.out.println("[ArtifactActionHandler] Artifact " + artifact.getName() + " is not a PLAYER artifact.");
                return false;
            }

            // Execute artifact effect
            boolean success = registry.executePlayerArtifact(artifact, gameState, player, targetPlayer);
            System.out.println("[ArtifactActionHandler] registry.executePlayerArtifact returned: " + success);

            // Remove artifact from player inventory if used successfully
            if (success) {
                player.removeArtifact(artifact);
                System.out.println("[ArtifactActionHandler] Artifact removed from player inventory.");
            } else {
                System.out.println("[ArtifactActionHandler] Artifact NOT removed (effect failed).");
            }

            return success;
        });
    }

    /**
     * Finds an artifact in a player's inventory by its ID.
     *
     * @param player     The player whose inventory to check
     * @param artifactId The ID of the artifact to find
     * @return The artifact if found, null otherwise
     */
    private Artifact findPlayerArtifact(Player player, int artifactId) {
        for (Artifact artifact : player.getArtifacts()) {
            if (artifact.getId() == artifactId) {
                return artifact;
            }
        }
        return null;
    }

    /**
     * Executes an action with proper locking.
     *
     * @param action The action to execute
     * @return The result of the action
     */
    private boolean executeWithLock(ActionWithResult action) {
        gameLock.writeLock().lock();
        try {
            return action.execute();
        } finally {
            gameLock.writeLock().unlock();
        }
    }

    /**
     * Functional interface for actions that return a boolean result.
     */
    @FunctionalInterface
    private interface ActionWithResult {
        boolean execute();
    }
}
