package ch.unibas.dmi.dbis.cs108.shared.entities.Behaviors;

import ch.unibas.dmi.dbis.cs108.server.core.logic.GameLogic;
import ch.unibas.dmi.dbis.cs108.shared.entities.EntityRegistry;
import ch.unibas.dmi.dbis.cs108.shared.entities.Findables.Artifact;
import ch.unibas.dmi.dbis.cs108.shared.entities.Purchasables.Structure;
import ch.unibas.dmi.dbis.cs108.shared.game.Player;
import ch.unibas.dmi.dbis.cs108.shared.game.Status;
import ch.unibas.dmi.dbis.cs108.shared.game.Tile;

import java.util.HashMap;
import java.util.Map;

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

    /** Map of player-targeting artifact behaviors, keyed by artifact name */
    private final Map<String, PlayerArtifactBehavior> playerBehaviors = new HashMap<>();

    /** Map of field-targeting artifact behaviors, keyed by artifact name */
    private final Map<String, FieldArtifactBehavior> fieldBehaviors = new HashMap<>();

    /** Map of trap artifact behaviors, keyed by artifact name */
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
        registerPlayerBehavior("Tear of Yggdrasil", (artifact, gameLogic, player, targetPlayer) -> {
            // Example: Heal target player
            targetPlayer.addEnergy((int)artifact.getEffect());
            return true;
        });

        registerPlayerBehavior("Hel's Shadow", (artifact, gameLogic, player, targetPlayer) -> {
            targetPlayer.addBuff(Status.BuffType.RUNE_GENERATION, (int)artifact.getEffect());
            return true;
        });

        registerPlayerBehavior("Flame of Muspelheim", (artifact, gameLogic, player, targetPlayer) -> {
            targetPlayer.addBuff(Status.BuffType.SHOP_PRICE, (int)artifact.getEffect());
            return true;
        });

        registerPlayerBehavior("Ice Splinter of Niflheim", (artifact, gameLogic, player, targetPlayer) -> {
            targetPlayer.addBuff(Status.BuffType.ENERGY_GENERATION, (int)artifact.getEffect());
            return true;
        });

        registerPlayerBehavior("Ashes of Surtr", (artifact, gameLogic, player, targetPlayer) -> {
            targetPlayer.addBuff(Status.BuffType.SHOP_PRICE, (int)artifact.getEffect());
            return true;
        });

        registerPlayerBehavior("Fragment of Mjölnir", (artifact, gameLogic, player, targetPlayer) -> {
            targetPlayer.addBuff(Status.BuffType.ARTIFACT_CHANCE, (int)artifact.getEffect());
            return true;
        });

        registerPlayerBehavior("Freyr's Golden Apple", (artifact, gameLogic, player, targetPlayer) -> {
            targetPlayer.addEnergy((int)artifact.getEffect());
            return true;
        });

        // Field-targeting artifacts
        registerFieldBehavior("Freyja's Necklace", (artifact, gameLogic, player, x, y) -> {
            Tile tile = gameLogic.getGameState().getBoardManager().getTile(x, y);
            if (tile == null || !tile.hasEntity()) return false;
            tile.setBuff(Status.BuffType.RUNE_GENERATION, (int)artifact.getEffect());
            return true;
        });

        registerFieldBehavior("Fenrir's Bones", (artifact, gameLogic, player, x, y) -> {
            Tile tile = gameLogic.getGameState().getBoardManager().getTile(x, y);
            if (tile == null || !tile.hasEntity()) return false;
            tile.setBuff(Status.BuffType.ENERGY_GENERATION, (int)artifact.getEffect());
            return true;
        });

        registerFieldBehavior("Blood of Jörmungandr", (artifact, gameLogic, player, x, y) -> {
            Tile tile = gameLogic.getGameState().getBoardManager().getTile(x, y);
            if (tile == null || !tile.hasEntity()) return false;
            tile.setBuff(Status.BuffType.RIVER_RUNE_GENERATION, (int)artifact.getEffect());
            return true;
        });

        registerFieldBehavior("Odin's Eye", (artifact, gameLogic, player, x, y) -> {
            //TODO implement this artifact
            return true;
        });

        // Trap artifacts
        registerTrapBehavior("Fenrir's Chains", (artifact, gameLogic, player, x, y) -> {
            Tile tile = gameLogic.getGameState().getBoardManager().getTile(x, y);
            if (tile == null || tile.hasEntity()) return false;

            // Create a new ActiveTrap structure on the tile
            Structure trapStructure = EntityRegistry.getStructure((int)artifact.getEffect());
            if (trapStructure == null) return false;

            tile.setEntity(trapStructure);
            tile.setHasEntity(true);
            return true;
        });
    }

    /**
     * Registers a player-targeting artifact behavior.
     *
     * @param artifactName the name of the artifact
     * @param behavior the behavior implementation
     */
    public void registerPlayerBehavior(String artifactName, PlayerArtifactBehavior behavior) {
        playerBehaviors.put(artifactName, behavior);
    }

    /**
     * Registers a field-targeting artifact behavior.
     *
     * @param artifactName the name of the artifact
     * @param behavior the behavior implementation
     */
    public void registerFieldBehavior(String artifactName, FieldArtifactBehavior behavior) {
        fieldBehaviors.put(artifactName, behavior);
    }

    /**
     * Registers a trap artifact behavior.
     *
     * @param artifactName the name of the artifact
     * @param behavior the behavior implementation
     */
    public void registerTrapBehavior(String artifactName, TrapArtifactBehavior behavior) {
        trapBehaviors.put(artifactName, behavior);
    }

    /**
     * Executes a player-targeting artifact behavior.
     *
     * @param artifact the artifact being used
     * @param gameLogic the current game logic
     * @param player the player using the artifact
     * @param targetPlayer the player being targeted
     * @return true if execution was successful, false otherwise
     */
    public boolean executePlayerArtifact(Artifact artifact, GameLogic gameLogic, Player player, Player targetPlayer) {
        PlayerArtifactBehavior behavior = playerBehaviors.get(artifact.getName());
        if (behavior != null) {
            return behavior.execute(artifact, gameLogic, player, targetPlayer);
        }
        return false;
    }

    /**
     * Executes a field-targeting artifact behavior.
     *
     * @param artifact the artifact being used
     * @param gameLogic the current game logic
     * @param player the player using the artifact
     * @param x the x-coordinate of the targeted tile
     * @param y the y-coordinate of the targeted tile
     * @return true if execution was successful, false otherwise
     */
    public boolean executeFieldArtifact(Artifact artifact, GameLogic gameLogic, Player player, int x, int y) {
        FieldArtifactBehavior behavior = fieldBehaviors.get(artifact.getName());
        if (behavior != null) {
            return behavior.execute(artifact, gameLogic, player, x, y);
        }
        return false;
    }

    /**
     * Executes a trap artifact behavior.
     *
     * @param artifact the artifact being used
     * @param gameLogic the current game logic
     * @param player the player using the artifact
     * @param x the x-coordinate of the targeted tile
     * @param y the y-coordinate of the targeted tile
     * @return true if execution was successful, false otherwise
     */
    public boolean executeTrapArtifact(Artifact artifact, GameLogic gameLogic, Player player, int x, int y) {
        TrapArtifactBehavior behavior = trapBehaviors.get(artifact.getName());
        if (behavior != null) {
            return behavior.execute(artifact, gameLogic, player, x, y);
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
         * @param artifact the artifact being used
         * @param gameLogic the current game logic
         * @param player the player using the artifact
         * @param targetPlayer the player being targeted
         * @return true if execution was successful, false otherwise
         */
        boolean execute(Artifact artifact, GameLogic gameLogic, Player player, Player targetPlayer);
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
         * @param artifact the artifact being used
         * @param gameLogic the current game logic
         * @param player the player using the artifact
         * @param x the x-coordinate of the targeted tile
         * @param y the y-coordinate of the targeted tile
         * @return true if execution was successful, false otherwise
         */
        boolean execute(Artifact artifact, GameLogic gameLogic, Player player, int x, int y);
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
         * @param artifact the artifact being used
         * @param gameLogic the current game logic
         * @param player the player using the artifact
         * @param x the x-coordinate of the targeted tile
         * @param y the y-coordinate of the targeted tile
         * @return true if execution was successful, false otherwise
         */
        boolean execute(Artifact artifact, GameLogic gameLogic, Player player, int x, int y);
    }
}