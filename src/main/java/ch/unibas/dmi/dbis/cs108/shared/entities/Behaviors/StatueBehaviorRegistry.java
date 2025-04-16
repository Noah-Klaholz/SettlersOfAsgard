package ch.unibas.dmi.dbis.cs108.shared.entities.Behaviors;

import ch.unibas.dmi.dbis.cs108.server.core.logic.GameLogic;
import ch.unibas.dmi.dbis.cs108.shared.entities.Purchasables.Statues.Statue;
import ch.unibas.dmi.dbis.cs108.shared.game.Player;
import java.util.HashMap;
import java.util.Map;

/**
 * Registry for statue behaviors.
 * This registry is non‑static so that it can be instantiated and extended at run‑time.
 */
public class StatueBehaviorRegistry {
    /**
     * Map of statue behaviors, keyed by their use type.
     */
    private final Map<String, StatueBehavior> behaviors = new HashMap<>();

    /**
     * Initializes the registry with default behaviors.
     * This method can be overridden to add custom behaviors.
     */
    public StatueBehaviorRegistry() {
        initializeBehaviors();
    }

    private void initializeBehaviors() {
        registerBehavior("Jörmungandr", (Statue statue, GameLogic gameLogic, Player player, Statue.useType useType) -> {
            if(player.getPurchasableEntities().isEmpty()) {
                return false;
            }
            return true;
        });
        // TODO: Register additional statue behaviors here.
    }

    /**
     * Registers a statue behavior for the given use type.
     *
     * @param name the identifier of the behavior
     * @param behavior the behavior implementation
     */
    public void registerBehavior(String name, StatueBehavior behavior) {
        behaviors.put(name, behavior);
    }

    /**
     * Executes the behavior associated with the specified statue.
     *
     * @param statue the statue to execute the behavior on
     * @param gameLogic the current game logic instance
     * @param player the player performing the action
     * @return true if execution was successful, false otherwise
     */
    public boolean execute(Statue statue, GameLogic gameLogic, Player player, Statue.useType useType) {
        StatueBehavior behavior = behaviors.get(statue.getName());
        if (behavior != null) {
            return behavior.execute(statue, gameLogic, player, useType);
        }
        return false;
    }

    /**
     * Functional interface for statue behaviors.
     * Each behavior receives the statue, game logic, and player as parameters.
     */
    @FunctionalInterface
    public interface StatueBehavior {
        boolean execute(Statue statue, GameLogic gameLogic, Player player, Statue.useType useType);
    }
}