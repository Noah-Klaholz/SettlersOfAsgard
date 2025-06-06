package ch.unibas.dmi.dbis.cs108.server.core.actions;

import ch.unibas.dmi.dbis.cs108.server.core.model.BoardManager;
import ch.unibas.dmi.dbis.cs108.server.core.model.GameState;
import ch.unibas.dmi.dbis.cs108.shared.entities.Behaviors.StructureBehaviorRegistry;
import ch.unibas.dmi.dbis.cs108.shared.entities.EntityRegistry;
import ch.unibas.dmi.dbis.cs108.shared.entities.GameEntity;
import ch.unibas.dmi.dbis.cs108.shared.entities.Purchasables.Structure;
import ch.unibas.dmi.dbis.cs108.shared.game.Player;
import ch.unibas.dmi.dbis.cs108.shared.game.Tile;

import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.locks.ReadWriteLock;

/**
 * Handles all structure-related actions in the game including placement and activation.
 * <p>
 * This class manages structure placement and uses thread-safe locking to ensure
 * data consistency during game state changes.
 * </p>
 */
public class StructureActionHandler {
    /**
     * The registry for structure behaviors.
     */
    private final StructureBehaviorRegistry registry = new StructureBehaviorRegistry();
    /**
     * The current game state.
     */
    private final GameState gameState;
    /**
     * The lock used for thread safety.
     */
    private final ReadWriteLock gameLock;

    /**
     * Creates a new StructureActionHandler with the specified game state and lock.
     *
     * @param gameState The current game state
     * @param gameLock  The lock used for thread safety
     */
    public StructureActionHandler(GameState gameState, ReadWriteLock gameLock) {
        this.gameState = gameState;
        this.gameLock = gameLock;
    }

    /**
     * Places a structure on the board at the specified coordinates.
     *
     * @param x           The x-coordinate on the board
     * @param y           The y-coordinate on the board
     * @param structureID The ID of the structure to place
     * @param playerName  The name of the player placing the structure
     * @return true if the structure was successfully placed, false otherwise
     */
    public boolean placeStructure(int x, int y, int structureID, String playerName) {
        return executeWithLock(() -> {
            // Validate player and tile
            ValidationResult result = validatePlayerAndTile(x, y, playerName, true, false);
            if (!result.isValid()) return false;

            Player player = result.getPlayer();
            Tile tile = result.getTile();

            // Get structure and check if player can afford it
            Structure structure = EntityRegistry.getStructure(structureID);
            if (structure == null) return false;
            if (structure.getId() == 5 && !tile.hasRiver()) return false;

            // Check if player can afford the structure
            if (!player.buy(structure.getPrice())) return false;

            player.addPurchasableEntity(structure);
            tile.setEntity(structure);

            return true;
        });
    }

    /**
     * Uses a structure at the specified coordinates.
     *
     * @param x           The x-coordinate on the board
     * @param y           The y-coordinate on the board
     * @param structureID The ID of the structure to use
     * @param playerName  The name of the player using the structure
     * @return true if the structure effect was successfully executed, false otherwise
     */
    public boolean useStructure(int x, int y, int structureID, String playerName) {
        return executeWithLock(() -> {
            // Validate player, tile, and get structure
            ValidationResult result = validatePlayerAndTile(x, y, playerName, false, true);
            if (!result.isValid()) return false;

            Structure structure = getStructureFromTile(result.getTile(), structureID);
            if (structure == null) return false;

            // Check if structure is already activated this turn
            if (structure.isActivated()) {
                return false;
            }

            // Execute structure effect and mark as activated
            boolean success = registry.execute(structure, gameState, result.getPlayer());
            if (success) {
                structure.setActivated(true);
            }

            return success;
        });
    }

    /**
     * Cheat: A player destroy all enemy structures.
     *
     * @param playerName the name of the player
     * @return if the action was successful, false otherwise
     */
    public boolean ragnarok(String playerName) {
        return executeWithLock(() -> {
            for (Tile[] tiles : gameState.getBoardManager().getBoard().getTiles()) {
                for (Tile tile : tiles) {
                    if (tile.hasOwner() && tile.getOwner().equals(playerName)) continue;
                    if (tile.hasEntity()) tile.setEntity(null);
                }
            }
            for (Player p : gameState.getPlayers()) {
                if (Objects.equals(p.getName(), playerName)) continue;
                p.setPurchasableEntities(new ArrayList<>());
            }
            return true;
        });
    }

    /**
     * Validates player and tile for an action.
     *
     * @param x                   The x-coordinate
     * @param y                   The y-coordinate
     * @param playerName          The player's name
     * @param requireEmptyTile    Whether the tile should be empty
     * @param requireEntityOnTile Whether the tile should have an entity
     * @return A ValidationResult with the player and tile if valid
     */
    private ValidationResult validatePlayerAndTile(int x, int y, String playerName,
                                                   boolean requireEmptyTile,
                                                   boolean requireEntityOnTile) {
        // Get the player
        Player player = gameState.findPlayerByName(playerName);
        if (player == null) {
            return new ValidationResult(false, null, null);
        }

        // Find the tile on the board
        BoardManager boardManager = gameState.getBoardManager();
        Tile tile = boardManager.getTile(x, y);

        if (tile == null) {
            return new ValidationResult(false, player, null);
        }
        String owner = tile.getOwner();
        if (owner == null ||
                !owner.equals(playerName) ||
                (requireEmptyTile && tile.hasEntity()) ||
                (requireEntityOnTile && !tile.hasEntity())) {
            return new ValidationResult(false, player, null);
        }

        return new ValidationResult(true, player, tile);
    }

    /**
     * Gets a structure from a tile if it matches the specified ID.
     *
     * @param tile        The tile containing the entity
     * @param structureID The expected structure ID
     * @return The structure if found and ID matches, null otherwise
     */
    private Structure getStructureFromTile(Tile tile, int structureID) {
        GameEntity entity = tile.getEntity();
        if (!(entity instanceof Structure) || entity.getId() != structureID) {
            return null;
        }
        return (Structure) entity;
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

    /**
     * Helper class to store validation results.
     */
    private static class ValidationResult {
        private final boolean valid;
        private final Player player;
        private final Tile tile;

        public ValidationResult(boolean valid, Player player, Tile tile) {
            this.valid = valid;
            this.player = player;
            this.tile = tile;
        }

        /**
         * Checks if the validation was successful.
         *
         * @return true if valid, false otherwise
         */
        public boolean isValid() {
            return valid;
        }

        /**
         * Gets the player associated with the validation.
         *
         * @return The player
         */
        public Player getPlayer() {
            return player;
        }

        /**
         * Gets the tile associated with the validation.
         *
         * @return The tile
         */
        public Tile getTile() {
            return tile;
        }
    }
}