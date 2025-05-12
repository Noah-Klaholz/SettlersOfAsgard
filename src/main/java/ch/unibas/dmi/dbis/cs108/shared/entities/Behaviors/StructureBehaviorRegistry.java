package ch.unibas.dmi.dbis.cs108.shared.entities.Behaviors;

import ch.unibas.dmi.dbis.cs108.SETTINGS;
import ch.unibas.dmi.dbis.cs108.server.core.model.GameState;
import ch.unibas.dmi.dbis.cs108.shared.entities.EntityRegistry;
import ch.unibas.dmi.dbis.cs108.shared.entities.Findables.Artifact;
import ch.unibas.dmi.dbis.cs108.shared.entities.Purchasables.Structure;
import ch.unibas.dmi.dbis.cs108.shared.game.Player;
import ch.unibas.dmi.dbis.cs108.shared.game.Status;
import ch.unibas.dmi.dbis.cs108.shared.game.Tile;

import java.util.HashMap;
import java.util.Map;

/**
 * Registry for structure behaviors.
 * This registry is non‑static so that it can be instantiated and extended at run‑time.
 */
public class StructureBehaviorRegistry {
    /**
     * Map of structure behaviors, keyed by their use type.
     */
    private final Map<String, StructureBehavior> behaviors = new HashMap<>();

    /**
     * Initializes the registry with default behaviors.
     * This method can be overridden to add custom behaviors.
     */
    public StructureBehaviorRegistry() {
        initializeBehaviors();
    }

    /**
     * Initializes behavior based on  type.
     */
    private void initializeBehaviors() {
        registerBehavior("Rune Table", (structure, gameState, player) -> {
            if (player.getEnergy() < 4) {
                return false;
            }
            player.addEnergy((int) structure.getParams().get(0).getValue());
            player.addRunes((int) structure.getParams().get(1).getValue());
            return true;
        });

        registerBehavior("Mimisbrunnr", (structure, gameState, player) -> {
            if (player.getArtifacts().size() < SETTINGS.Config.MAX_ARTIFACTS.getValue()) {
                Artifact artifact = EntityRegistry.getRandomArtifact();
                player.addArtifact(artifact);
                return true;
            }
            player.addEnergy((int) structure.getParams().get(0).getValue());
            return false;
        });

        registerBehavior("Helgrindr", (structure, gameState, player) -> {
            player.addBuff(Status.BuffType.DEBUFFABLE, 0.0); // sets the player to non-debuffable
            player.addEnergy((int) structure.getParams().get(0).getValue());
            return true;
        });

        registerBehavior("Huginn and Muninn", (structure, gameState, player) -> {
            Tile[][] tiles = gameState.getBoardManager().getBoard().getTiles();
            boolean b = false;  // true if there is an artifact to show, false otherwise
            for (Tile[] tile : tiles) {
                for (Tile t : tile) {
                    if (t.getArtifact() != null) {
                        gameState.sendNotification(player.getName(),  t.getArtifact().getId() + "$" + t.getX() + "$" + t.getY());
                        player.addEnergy((int) structure.getParams().get(0).getValue());
                        return true;
                    }
                }
            }
            gameState.sendNotification(player.getName(), "NULL");
            player.addEnergy((int) structure.getParams().get(0).getValue());
            return true;
        });

        registerBehavior("Ran's Hall", (structure, gameState, player) -> {
            player.addEnergy((int) structure.getParams().get(0).getValue());
            return true;
        });

        registerBehavior("Surtur's Smeltery", (structure, gameState, player) -> {
            Status.BuffType[] buffTypes = {
                    Status.BuffType.RUNE_GENERATION,
                    Status.BuffType.ENERGY_GENERATION,
                    Status.BuffType.RIVER_RUNE_GENERATION,
                    Status.BuffType.SHOP_PRICE,
                    Status.BuffType.ARTIFACT_CHANCE
            };
            int numberOfBuffs = (int) structure.getParams().get(0).getValue();

            for (int i = 0; i < numberOfBuffs; i++) { // Use < instead of <= to avoid extra iteration
                int random = (int) (Math.random() * buffTypes.length); // Ensure random index is within bounds
                double val = structure.getParams().get(random + 2).getValue(); // +2 because 0 is number of buffs, 1 is debuffOtherPlayers
                player.addBuff(buffTypes[random], val);

                if (structure.getParams().get(1).getValue() == 1.0) { // If DebuffOtherPlayers is true
                    gameState.getPlayers().forEach(otherPlayer -> {
                        if (!otherPlayer.equals(player)) { // Avoid debuffing the current player
                            otherPlayer.addBuff(buffTypes[random], -val);
                        }
                    });
                }
            }

            structure.setParam(0, 1); // Reset number of buffs
            structure.setParam(1, 0); // Reset buffOtherPlayers
            return true;
        });

        // Cut from the game for now
        registerBehavior("Tree", (structure, gameState, player) -> true);

        registerBehavior("ActiveTrap", (structure, gameState, player) -> {
            player.addRunes((int) structure.getParams().get(0).getValue());
            return true;
        });

        // No need to register behavior for the Overgrowth structure since it does not have any effect
    }

    /**
     * Registers a structure behavior for the given use type.
     *
     * @param name     the identifier of the behavior
     * @param behavior the behavior implementation
     */
    public void registerBehavior(String name, StructureBehavior behavior) {
        behaviors.put(name, behavior);
    }

    /**
     * Executes the behavior associated with the specified use type.
     *
     * @param structure the structure to execute
     * @param gameState the current game state
     * @param player    the player performing the action
     * @return true if execution was successful, false otherwise
     */
    public boolean execute(Structure structure, GameState gameState, Player player) {
        StructureBehavior behavior = behaviors.get(structure.getName());
        if (behavior != null && !structure.isDisabled()) {
            return behavior.execute(structure, gameState, player);
        }
        return false;
    }

    /**
     * Functional interface for structure behaviors.
     * Each behavior receives the game state, the player name, and the tile.
     */
    @FunctionalInterface
    public interface StructureBehavior {
        /**
         * Execute the behavior
         *
         * @param structure the structure
         * @param gameState the gameState
         * @param player the player
         * @return true if the action was successful, false otherwise
         */
        boolean execute(Structure structure, GameState gameState, Player player);
    }
}
