package ch.unibas.dmi.dbis.cs108.shared.entities.Behaviors;

import ch.unibas.dmi.dbis.cs108.server.core.logic.GameLogic;
import ch.unibas.dmi.dbis.cs108.shared.entities.Purchasables.Structure;
import ch.unibas.dmi.dbis.cs108.shared.game.Player;
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
        registerBehavior("Rune Table", (structure, gameLogic, player) -> {
            if (player.getEnergy() < 4) {
                return false;
            }
            player.addEnergy((int)structure.getParams().get(0).getValue());
            player.addRunes((int)structure.getParams().get(1).getValue());
            return true;
        });
        registerBehavior("ActiveTrap", (structure, gameLogic, player) -> {
            player.addRunes((int)structure.getParams().get(0).getValue());
            return true;
        });
        //TODO Add other behaviors here
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
     * @param gameLogic the current game logic to modify if needed
     * @param player the player performing the action
     * @return true if execution was successful, false otherwise
     */
    public boolean execute(Structure structure, GameLogic gameLogic, Player player) {
        StructureBehavior behavior = behaviors.get(structure.getName());
        if (behavior != null) {
            return behavior.execute(structure, gameLogic, player);
        }
        return false;
    }

    /**
     * Functional interface for structure behaviors.
     * Each behavior receives the game state, the player name, and the tile.
     */
    @FunctionalInterface
    public interface StructureBehavior {
        boolean execute(Structure structure, GameLogic gameLogic, Player player);
    }
}