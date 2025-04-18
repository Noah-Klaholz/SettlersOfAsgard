package ch.unibas.dmi.dbis.cs108.shared.entities.Behaviors;

import ch.unibas.dmi.dbis.cs108.SETTINGS;
import ch.unibas.dmi.dbis.cs108.server.core.model.BoardManager;
import ch.unibas.dmi.dbis.cs108.server.core.model.GameState;
import ch.unibas.dmi.dbis.cs108.shared.entities.EntityRegistry;
import ch.unibas.dmi.dbis.cs108.shared.entities.Findables.Artifact;
import ch.unibas.dmi.dbis.cs108.shared.entities.Purchasables.Structure;
import ch.unibas.dmi.dbis.cs108.shared.game.Player;
import ch.unibas.dmi.dbis.cs108.shared.game.Status;

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

    private void initializeBehaviors() {
        registerBehavior("Rune Table", (structure, gameState, player) -> {
            if (player.getEnergy() < 4) {
                return false;
            }
            player.addEnergy((int)structure.getParams().get(0).getValue());
            player.addRunes((int)structure.getParams().get(1).getValue());
            return true;
        });

        registerBehavior("Mimisbrunnr", (structure, gameState, player) -> {
            if(player.getArtifacts().size() < SETTINGS.Config.MAX_ARTIFACTS.getValue()) {
                int randomId = (int)(Math.random() * 12) + 10;
                Artifact artifact = EntityRegistry.getArtifact(randomId);
                player.addArtifact(artifact);
                return true;
            }
            return false;
        });

        registerBehavior("ActiveTrap", (structure, gameState, player) -> {
            player.addRunes(-(int)structure.getParams().get(0).getValue());
            return true;
        });

        registerBehavior("Helgrindr", (structure, gameState, player) -> {
            player.addBuff(Status.BuffType.DEBUFFABLE, 0); // sets the player to non-debuffable
            return true;
        });

        registerBehavior("Huginn and Muninn", (structure, gameState, player) -> {
            // TODO implement showing of 1 field with artifact (can be already purchased)
            return true;
        });

        registerBehavior("Ran's Hall", (structure, gameState, player) -> {
            player.addEnergy((int)structure.getParams().get(0).getValue());
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

            for (int i = 0; i < numberOfBuffs; i++) {
                int random = (int)Math.ceil(Math.random() * buffTypes.length);
                player.addBuff(buffTypes[random],(int)structure.getParams().get(random+1).getValue()); // +1 because 0 is the number of buffs
            }
            structure.setParam(0, 1); // Reset number of buffs -> should never be changed permanently
            return true;
        });

        registerBehavior("Tree", (structure, gameState, player) -> {
            player.addEnergy((int)structure.getParams().get(0).getValue());
            return true;
        });

        // No need to register behavior for the Overgrowth structure since it does not have any effect
    }

    /**
     * Registers a structure behavior for the given use type.
     *
     * @param name the identifier of the behavior
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
     * @param player the player performing the action
     * @return true if execution was successful, false otherwise
     */
    public boolean execute(Structure structure, GameState gameState, Player player) {
        StructureBehavior behavior = behaviors.get(structure.getName());
        if (behavior != null) {
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
        boolean execute(Structure structure, GameState gameState, Player player);
    }
}