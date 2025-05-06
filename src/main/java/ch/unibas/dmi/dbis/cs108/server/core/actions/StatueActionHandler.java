package ch.unibas.dmi.dbis.cs108.server.core.actions;

import ch.unibas.dmi.dbis.cs108.server.core.model.BoardManager;
import ch.unibas.dmi.dbis.cs108.server.core.model.GameState;
import ch.unibas.dmi.dbis.cs108.shared.entities.Behaviors.StatueBehaviorRegistry;
import ch.unibas.dmi.dbis.cs108.shared.entities.EntityRegistry;
import ch.unibas.dmi.dbis.cs108.shared.entities.Findables.Artifact;
import ch.unibas.dmi.dbis.cs108.shared.entities.GameEntity;
import ch.unibas.dmi.dbis.cs108.shared.entities.Purchasables.Statues.Statue;
import ch.unibas.dmi.dbis.cs108.shared.entities.Purchasables.Statues.StatueParameters;
import ch.unibas.dmi.dbis.cs108.shared.entities.Purchasables.Structure;
import ch.unibas.dmi.dbis.cs108.shared.game.Player;
import ch.unibas.dmi.dbis.cs108.shared.game.Tile;

import java.util.Objects;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.logging.Logger;

/**
 * Handles all statue-related actions in the game.
 * <p>
 * This class is responsible for managing statue placement, upgrades, and use of
 * statue abilities. It ensures proper validation of all actions and coordinates with
 * the StatueBehaviorRegistry to execute statue effects.
 * </p>
 * <p>
 * All methods use thread-safe locking to ensure data consistency during game state changes.
 * </p>
 */
public class StatueActionHandler {
    private final GameState gameState;
    private final ReadWriteLock gameLock;
    private final StatueBehaviorRegistry registry = new StatueBehaviorRegistry();

    /**
     * Creates a new StatueActionHandler with the specified game state and lock.
     *
     * @param gameState The current game state
     * @param gameLock  The lock used for thread safety
     */
    public StatueActionHandler(GameState gameState, ReadWriteLock gameLock) {
        this.gameState = gameState;
        this.gameLock = gameLock;
    }

    /**
     * Places a statue on the board at the specified coordinates.
     * <p>
     * The statue must be placeable at the given coordinates, the player must own the tile,
     * and the player must have enough runes to purchase the statue.
     * </p>
     *
     * @param x          The x-coordinate on the board
     * @param y          The y-coordinate on the board
     * @param statueId   The ID of the statue to place
     * @param playerName The name of the player placing the statue
     * @return true if the statue was successfully placed, false otherwise
     */
    public boolean placeStatue(int x, int y, int statueId, String playerName) {
        return executeWithLock(() -> {
            // Validate player and tile
            ValidationResult result = validatePlayerAndTile(x, y, playerName, true, false);
            if (!result.isValid()) {
                Logger.getGlobal().info("StatueActionHandler: placeStatue: Validation failed");
                return false;
            }

            Player player = result.getPlayer();
            Tile tile = result.getTile();

            // Get statue and check if player already has one
            Statue statue = EntityRegistry.getStatue(statueId);
            if (statue == null || player.hasStatue() || !Objects.equals(tile.getWorld(), statue.getWorld())) {
                Logger.getGlobal().info("StatueActionHandler: placeStatue: Statue not found or player already has one or world is incorrect.");
                return false;
            }

            // Check if player can afford the statue
            if (!player.buy(statue.getPrice())) {
                Logger.getGlobal().info("StatueActionHandler: placeStatue: Player cannot afford statue");
                return false;
            }

            tile.setEntity(statue);

            return true;
        });
    }

    /**
     * Upgrades a statue at the specified coordinates.
     * <p>
     * The tile must contain a statue of the specified ID, the player must own the tile,
     * the statue must not be at max level, and the player must have enough runes to upgrade.
     * </p>
     *
     * @param x          The x-coordinate on the board
     * @param y          The y-coordinate on the board
     * @param statueId   The ID of the statue to upgrade
     * @param playerName The name of the player upgrading the statue
     * @return true if the statue was successfully upgraded, false otherwise
     */
    public boolean upgradeStatue(int x, int y, int statueId, String playerName) {
        return executeWithLock(() -> {
            // Validate player, tile, and get statue
            ValidationResult result = validatePlayerAndTile(x, y, playerName, false, true);
            if (!result.isValid()) return false;

            Statue statue = getStatueFromTile(result.getTile(), statueId);
            if (statue == null) return false;

            Player player = result.getPlayer();
            // Check if statue can be upgraded (max level is 3)
            if (statue.getLevel() >= 3) return false;

            // Calculate upgrade cost and check if player can afford it
            if (!player.buy(statue.getUpgradePrice())) return false;
            statue.upgrade();

            return true;
        });
    }

    /**
     * Uses a statue's ability at the specified coordinates.
     * <p>
     * The tile must contain a statue of the specified ID, the player must own the tile,
     * and the statue's effect will be executed with the provided parameters.
     * </p>
     *
     * @param x          The x-coordinate on the board
     * @param y          The y-coordinate on the board
     * @param statueId   The ID of the statue to use
     * @param playerName The name of the player using the statue
     * @param params     Parameter string in format "KEY1:VALUE1;KEY2:VALUE2;..."
     * @return true if the statue effect was successfully executed, false otherwise
     */
    public boolean useStatue(int x, int y, int statueId, String playerName, String params) {
        return executeWithLock(() -> {
            // Validate player, tile, and get statue
            ValidationResult result = validatePlayerAndTile(x, y, playerName, false, true);
            if (!result.isValid()) return false;

            Statue statue = getStatueFromTile(result.getTile(), statueId);
            if (statue == null) return false;

            // Parse parameters and execute statue effect
            StatueParameters statueParams = parseParameters(params);
            return registry.executeStatue(statue, gameState, result.getPlayer(), statueParams);
        });
    }

    /**
     * Parses a parameter string in format "KEY1:VALUE1;KEY2:VALUE2;..."
     * into a StatueParameters object.
     *
     * @param paramsString The parameter string
     * @return StatueParameters object with parsed values
     */
    private StatueParameters parseParameters(String paramsString) {
        StatueParameters.Builder builder = new StatueParameters.Builder();

        if (paramsString == null || paramsString.isEmpty()) {
            return builder.build();
        }

        String[] paramPairs = paramsString.split(";");
        for (String pair : paramPairs) {
            String[] keyValue = pair.split(":", 2);
            if (keyValue.length != 2) {
                continue;
            }

            String key = keyValue[0].toUpperCase();
            String value = keyValue[1];

            switch (key) {
                case "PLAYER":
                    Player targetPlayer = gameState.findPlayerByName(value);
                    if (targetPlayer != null) {
                        builder.withPlayer(targetPlayer);
                    }
                    break;
                case "TILE":
                    String[] coordinates = value.split(",");
                    if (coordinates.length == 2) {
                        try {
                            int tileX = Integer.parseInt(coordinates[0]);
                            int tileY = Integer.parseInt(coordinates[1]);
                            builder.withTile(tileX, tileY);
                        } catch (NumberFormatException e) {
                            // Invalid coordinates format
                        }
                    }
                    break;
                case "STRUCTURE":
                    try {
                        int structureId = Integer.parseInt(value);
                        Structure structure = findStructureById(structureId);
                        if (structure != null) {
                            builder.withStructure(structure);
                        }
                    } catch (NumberFormatException e) {
                        // Invalid structure ID format
                    }
                    break;
                case "ARTIFACT":
                    try {
                        int artifactId = Integer.parseInt(value);
                        Artifact artifact = findArtifactById(artifactId);
                        if (artifact != null) {
                            builder.withArtifact(artifact);
                        }
                    } catch (NumberFormatException e) {
                        // Invalid artifact ID format
                    }
                    break;
            }
        }

        return builder.build();
    }

    /**
     * Finds a structure by its ID.
     *
     * @param structureId The structure ID
     * @return The structure or null if not found
     */
    private Structure findStructureById(int structureId) {
        return EntityRegistry.getStructure(structureId);
    }

    /**
     * Finds an artifact by its ID.
     *
     * @param artifactId The artifact ID
     * @return The artifact or null if not found
     */
    private Artifact findArtifactById(int artifactId) {
        return EntityRegistry.getArtifact(artifactId);
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

        // Validate tile state
        if (tile == null || tile.getOwner() == null ||
                !tile.getOwner().equals(playerName) ||
                (requireEmptyTile && tile.hasEntity()) ||
                (requireEntityOnTile && !tile.hasEntity())) {
            return new ValidationResult(false, player, null);
        }

        return new ValidationResult(true, player, tile);
    }

    /**
     * Gets a statue from a tile if it matches the specified ID.
     *
     * @param tile     The tile containing the entity
     * @param statueId The expected statue ID
     * @return The statue if found and ID matches, null otherwise
     */
    private Statue getStatueFromTile(Tile tile, int statueId) {
        GameEntity entity = tile.getEntity();
        if (!(entity instanceof Statue) || entity.getId() != statueId) {
            return null;
        }
        return (Statue) entity;
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

        public boolean isValid() {
            return valid;
        }

        public Player getPlayer() {
            return player;
        }

        public Tile getTile() {
            return tile;
        }
    }
}