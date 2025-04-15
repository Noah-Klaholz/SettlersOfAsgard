package ch.unibas.dmi.dbis.cs108.shared.entities;

import ch.unibas.dmi.dbis.cs108.server.core.model.GameState;
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
        //TODO Add default behaviors here
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
     * @param name the identifier of the structure to execute
     * @param gameState the current game state to modify if needed
     * @param playerName the name of the player performing the action
     * @return true if execution was successful, false otherwise
     */
    public boolean execute(String name, GameState gameState, String playerName) {
        StructureBehavior behavior = behaviors.get(name);
        if (behavior != null) {
            return behavior.execute(gameState, playerName);
        }
        return false;
    }

    /**
     * Functional interface for structure behaviors.
     * Each behavior receives the game state, the player name, and the tile.
     */
    @FunctionalInterface
    public interface StructureBehavior {
        boolean execute(GameState gameState, String playerName);
    }
}