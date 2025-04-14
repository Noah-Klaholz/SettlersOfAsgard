package ch.unibas.dmi.dbis.cs108.shared.entities;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Registry for structure behaviors based on useType.
 * Maps structure types to their respective behavior implementations.
 */
public class StructureBehaviorRegistry {
    /**
     * Map associating useType strings with behavior functions.
     * Each function accepts an array of Objects as parameters and returns a boolean.
     */
    private static final Map<String, Function<Object[], Boolean>> behaviors = new HashMap<>();

    /**
     * Static initializer to register all structure behaviors.
     */
    static {
        // Register all structure behaviors
        behaviors.put("FARM", params -> {
            // Extract parameters from the array
            // Player player = (Player)params[0];
            // Tile tile = (Tile)params[1];

            // Implement farm behavior
            // player.addResource("food", 5);
            return true;
        });

        behaviors.put("MINE", params -> {
            // Implement mine behavior
            return true;
        });

        // Register other behaviors
    }

    /**
     * Executes behavior for the given useType.
     *
     * @param useType Type of behavior to execute
     * @param params Parameters needed for behavior execution
     * @return true if executed successfully, false otherwise
     */
    public static boolean execute(String useType, Object... params) {
        Function<Object[], Boolean> behavior = behaviors.get(useType);
        if (behavior != null) {
            return behavior.apply(params);
        }
        return false;
    }
}