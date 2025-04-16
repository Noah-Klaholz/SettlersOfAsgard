package ch.unibas.dmi.dbis.cs108.shared.entities.Behaviors;

import ch.unibas.dmi.dbis.cs108.SETTINGS;
import ch.unibas.dmi.dbis.cs108.server.core.logic.GameState;
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
            return true;
            //TODO Add behavior for Huginn and Muninn -> how to show this in networking? -> same with odings eye artifact
        });

        registerBehavior("Ran's Hall", (structure, gameState, player) -> {
            return true;
            //TODO Add behavior for Ran's Hall -> not yet deciced
        });

        registerBehavior("Surtur's Smeltery", (structure, gameState, player) -> {
            return true;
            //TODO Add behavior for Surtur's Smeltery -> not yet explained
        });

        registerBehavior("Tree", (structure, gameState, player) -> {
            player.addRunes((int)structure.getParams().get(0).getValue());
            return true;
        });
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