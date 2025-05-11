package ch.unibas.dmi.dbis.cs108.shared.entities.Behaviors;

import ch.unibas.dmi.dbis.cs108.server.core.model.GameState;
import ch.unibas.dmi.dbis.cs108.shared.entities.EntityRegistry;
import ch.unibas.dmi.dbis.cs108.shared.entities.Findables.Artifact;
import ch.unibas.dmi.dbis.cs108.shared.entities.GameEntity;
import ch.unibas.dmi.dbis.cs108.shared.entities.Purchasables.Structure;
import ch.unibas.dmi.dbis.cs108.shared.game.Player;
import ch.unibas.dmi.dbis.cs108.shared.game.Status;
import ch.unibas.dmi.dbis.cs108.shared.game.Tile;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Registry for artifact behaviors based on their useType.
 * <p>
 * This registry manages different types of artifact behaviors:
 * - Player artifacts: Target another player (or self)
 * - Field artifacts: Target a specific tile on the board
 * - Trap artifacts: Create an ActiveTrap structure on a specific tile
 * </p>
 * <p>
 * The registry is non-static to allow for runtime extension and modification
 * of behaviors. Default behaviors are initialized in the constructor.
 * </p>
 */
public class ArtifactBehaviorRegistry {
    /**
     * Logger for the ArtifactBehaviorRegistry class.
     */
    private static final Logger logger = Logger.getLogger(ArtifactBehaviorRegistry.class.getName());

    /**
     * Map of player-targeting artifact behaviors, keyed by artifact name
     */
    private final Map<String, PlayerArtifactBehavior> playerBehaviors = new HashMap<>();

    /**
     * Map of field-targeting artifact behaviors, keyed by artifact name
     */
    private final Map<String, FieldArtifactBehavior> fieldBehaviors = new HashMap<>();

    /**
     * Map of trap artifact behaviors, keyed by artifact name
     */
    private final Map<String, TrapArtifactBehavior> trapBehaviors = new HashMap<>();

    /**
     * Initializes the registry with default behaviors for all artifacts.
     */
    public ArtifactBehaviorRegistry() {
        initializeDefaultBehaviors();
    }

    /**
     * Initializes default behaviors for all artifacts based on their useType.
     * This includes player-targeting, field-targeting, and trap artifacts.
     */
    private void initializeDefaultBehaviors() {
        // Player-targeting artifacts
        registerPlayerBehavior("Tear of Yggdrasil", (artifact, gameState, player, targetPlayer) -> {
            // Remove energy from other player
            logger.info("Tear of Yggdrasil used on " + targetPlayer.getName());
            targetPlayer.addEnergy((int) artifact.getEffect());
            return true;
        });

        registerPlayerBehavior("Hel's Shadow", (artifact, gameState, player, targetPlayer) -> {
            // Remove rune efficiency from other player
            logger.info("Hel's Shadow used on " + targetPlayer.getName());
            targetPlayer.addBuff(Status.BuffType.RUNE_GENERATION, artifact.getEffect());
            return true;
        });

        registerPlayerBehavior("Flame of Muspelheim", (artifact, gameState, player, targetPlayer) -> {
            // Give shop discount to player
            logger.info("Flame of Muspelheim used on " + targetPlayer.getName());
            targetPlayer.addBuff(Status.BuffType.SHOP_PRICE, artifact.getEffect());
            return true;
        });

        registerPlayerBehavior("Ice Splinter of Niflheim", (artifact, gameState, player, targetPlayer) -> {
            // Remove energy generation efficiency from other player
            logger.info("Ice Splinter of Niflheim used on " + targetPlayer.getName());
            targetPlayer.addBuff(Status.BuffType.ENERGY_GENERATION, artifact.getEffect());
            return true;
        });

        registerPlayerBehavior("Ashes of Surtr", (artifact, gameState, player, targetPlayer) -> {
            // Give higher prices in shop to other player
            logger.info("Ashes of Surtr used on " + targetPlayer.getName());
            targetPlayer.addBuff(Status.BuffType.SHOP_PRICE, artifact.getEffect());
            return true;
        });

        registerPlayerBehavior("Fragment of Mjölnir", (artifact, gameState, player, targetPlayer) -> {
            // Give higher chance of artifacts to player
            logger.info("Fragment of Mjölnir used on " + targetPlayer.getName());
            targetPlayer.addBuff(Status.BuffType.ARTIFACT_CHANCE, artifact.getEffect());
            return true;
        });

        registerPlayerBehavior("Freyr's Golden Apple", (artifact, gameState, player, targetPlayer) -> {
            // Gives energy to player
            logger.info("Freyr's Golden Apple used on " + targetPlayer.getName());
            targetPlayer.addEnergy((int) artifact.getEffect());
            return true;
        });

        registerPlayerBehavior("Mjölnir Charm", (artifact, gameState, player, targetPlayer) -> {
            // Increases artifact chance
            logger.info("Mjölnir Charm used on " + targetPlayer.getName());
            targetPlayer.addBuff(Status.BuffType.ARTIFACT_CHANCE, artifact.getEffect());
            return true;
        });

        // Field-targeting artifacts
        registerFieldBehavior("Freyja's Necklace", (artifact, gameState, player, x, y) -> {
            // Give a major rune generation buff to the tile
            Tile tile = gameState.getBoardManager().getTile(x, y);
            if (tile == null) return false;
            tile.setBuff(Status.BuffType.RUNE_GENERATION, artifact.getEffect());
            return true;
        });

        registerFieldBehavior("Fenrir's Bones", (artifact, gameState, player, x, y) -> {
            // Give a major energy generation buff to the tile
            Tile tile = gameState.getBoardManager().getTile(x, y);
            if (tile == null || !tile.hasEntity()) return false;
            tile.setBuff(Status.BuffType.ENERGY_GENERATION, artifact.getEffect());
            return true;
        });

        registerFieldBehavior("Blood of Jörmungandr", (artifact, gameState, player, x, y) -> {
            // Give a huge rune generation buff to the river-tile
            Tile tile = gameState.getBoardManager().getTile(x, y);
            if (tile == null) return false;
            tile.setBuff(Status.BuffType.RIVER_RUNE_GENERATION, artifact.getEffect());
            return true;
        });

        registerFieldBehavior("Odin's Eye", (artifact, gameState, player, x, y) -> {
            boolean found = false;
            logger.info("Odin's Eye used on tile (" + x + "," + y + ")");
            // Checks a tile and all adjacent tiles for traps and notifies the user if found
            for (Tile tile : gameState.getBoardManager().getAdjacentTiles(x, y)) {
                if (tile == null || tile.getArtifact() == null) continue;
                GameEntity entity = tile.getEntity();
                if (entity != null && entity.isArtifact()) {
                    gameState.sendNotification(player.getName(),  entity.getId() + "$" + tile.getX() + "$" + tile.getY());
                    found = true;
                    break;
                }
            }
            if (!found) {
                gameState.sendNotification(player.getName(), "NULL");
            }
            return true;
        });

        // Trap artifacts
        registerTrapBehavior("Fenrir's Chains", (artifact, gameState, player, x, y) -> {
            // Places an active trap on the tile
            Tile tile = gameState.getBoardManager().getTile(x, y);
            if (tile == null || tile.hasEntity()) return false;

            // Create a new ActiveTrap structure on the tile
            Structure trapStructure = EntityRegistry.getStructure((int) artifact.getEffect());
            if (trapStructure == null) return false;

            tile.setEntity(trapStructure);
            return true;
        });
    }

    /**
     * Registers a player-targeting artifact behavior.
     *
     * @param artifactName the name of the artifact
     * @param behavior     the behavior implementation
     */
    public void registerPlayerBehavior(String artifactName, PlayerArtifactBehavior behavior) {
        playerBehaviors.put(artifactName, behavior);
    }

    /**
     * Registers a field-targeting artifact behavior.
     *
     * @param artifactName the name of the artifact
     * @param behavior     the behavior implementation
     */
    public void registerFieldBehavior(String artifactName, FieldArtifactBehavior behavior) {
        fieldBehaviors.put(artifactName, behavior);
    }

    /**
     * Registers a trap artifact behavior.
     *
     * @param artifactName the name of the artifact
     * @param behavior     the behavior implementation
     */
    public void registerTrapBehavior(String artifactName, TrapArtifactBehavior behavior) {
        trapBehaviors.put(artifactName, behavior);
    }

    /**
     * Executes a player-targeting artifact behavior.
     *
     * @param artifact     the artifact being used
     * @param gameState    the current game state
     * @param player       the player using the artifact
     * @param targetPlayer the player being targeted
     * @return true if execution was successful, false otherwise
     */
    public boolean executePlayerArtifact(Artifact artifact, GameState gameState, Player player, Player targetPlayer) {
        PlayerArtifactBehavior behavior = playerBehaviors.get(artifact.getName());
        if (behavior != null) {
            return behavior.execute(artifact, gameState, player, targetPlayer);
        }
        return false;
    }

    /**
     * Executes a field-targeting artifact behavior.
     *
     * @param artifact  the artifact being used
     * @param gameState the current game state
     * @param player    the player using the artifact
     * @param x         the x-coordinate of the targeted tile
     * @param y         the y-coordinate of the targeted tile
     * @return true if execution was successful, false otherwise
     */
    public boolean executeFieldArtifact(Artifact artifact, GameState gameState, Player player, int x, int y) {
        FieldArtifactBehavior behavior = fieldBehaviors.get(artifact.getName());
        if (behavior != null) {
            return behavior.execute(artifact, gameState, player, x, y);
        }
        return false;
    }

    /**
     * Executes a trap artifact behavior.
     *
     * @param artifact  the artifact being used
     * @param gameState the current game state
     * @param player    the player using the artifact
     * @param x         the x-coordinate of the targeted tile
     * @param y         the y-coordinate of the targeted tile
     * @return true if execution was successful, false otherwise
     */
    public boolean executeTrapArtifact(Artifact artifact, GameState gameState, Player player, int x, int y) {
        TrapArtifactBehavior behavior = trapBehaviors.get(artifact.getName());
        if (behavior != null) {
            return behavior.execute(artifact, gameState, player, x, y);
        }
        return false;
    }

    /**
     * Functional interface for player-targeting artifact behaviors.
     * These artifacts affect another player (or self).
     */
    @FunctionalInterface
    public interface PlayerArtifactBehavior {
        /**
         * Executes the player-targeting artifact behavior.
         *
         * @param artifact     the artifact being used
         * @param gameState    the current game state
         * @param player       the player using the artifact
         * @param targetPlayer the player being targeted
         * @return true if execution was successful, false otherwise
         */
        boolean execute(Artifact artifact, GameState gameState, Player player, Player targetPlayer);
    }

    /**
     * Functional interface for field-targeting artifact behaviors.
     * These artifacts affect a specific tile on the board.
     */
    @FunctionalInterface
    public interface FieldArtifactBehavior {
        /**
         * Executes the field-targeting artifact behavior.
         *
         * @param artifact  the artifact being used
         * @param gameState the current game state
         * @param player    the player using the artifact
         * @param x         the x-coordinate of the targeted tile
         * @param y         the y-coordinate of the targeted tile
         * @return true if execution was successful, false otherwise
         */
        boolean execute(Artifact artifact, GameState gameState, Player player, int x, int y);
    }

    /**
     * Functional interface for trap artifact behaviors.
     * These artifacts create an ActiveTrap structure on a specific tile.
     */
    @FunctionalInterface
    public interface TrapArtifactBehavior {
        /**
         * Executes the trap artifact behavior.
         *
         * @param artifact  the artifact being used
         * @param gameState the current game state
         * @param player    the player using the artifact
         * @param x         the x-coordinate of the targeted tile
         * @param y         the y-coordinate of the targeted tile
         * @return true if execution was successful, false otherwise
         */
        boolean execute(Artifact artifact, GameState gameState, Player player, int x, int y);
    }
}