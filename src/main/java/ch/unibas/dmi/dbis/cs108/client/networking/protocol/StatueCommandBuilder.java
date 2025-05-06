package ch.unibas.dmi.dbis.cs108.client.networking.protocol;

import java.util.HashMap;
import java.util.Map;

/**
 * Utility class to build commands for using statues in the game.
 * This class provides methods to create commands for using statues with various parameters.
 */
public class StatueCommandBuilder {
    /**
     * Creates a command to use a statue with complex parameters.
     */
    private static String useStatueComplex(int statueId, int x, int y, Map<String, String> params) {
        StringBuilder cmd = new StringBuilder("USTA$" + x + "$" + y + "$" + statueId + "$");
        for (Map.Entry<String, String> param : params.entrySet()) {
            cmd.append(";").append(param.getKey()).append(":").append(param.getValue());
        }

        return cmd.toString();
    }

    /**
     * Creates a command to use a statue with parameters.
     *
     * @param x        The x-coordinate of the statue
     * @param y        The y-coordinate of the statue
     * @param statueID The ID of the statue
     * @param params   Parameter string in format "KEY1:VALUE1;KEY2:VALUE2"
     * @return The command string to use the statue
     */
    public static String useStatue(int x, int y, int statueID, String params) {
        Map<String, String> paramMap = getMapByStatueID(statueID, params);

        return useStatueComplex(statueID, x, y, paramMap);
    }

    /**
     * Creates a map to hold parameters for a statue based on its ID and parameters string.
     * Parses the input parameters string into a parameter map.
     *
     * @param statueID The ID of the statue
     * @param params   Parameter string in format "KEY1:VALUE1;KEY2:VALUE2"
     * @return A map containing the parsed parameters for the statue
     */
    private static Map<String, String> getMapByStatueID(int statueID, String params) {
        Map<String, String> paramMap = new HashMap<>();

        // Parse the params string if it's not null or empty
        if (params != null && !params.isEmpty()) {
            String[] paramPairs = params.split(";");
            for (String pair : paramPairs) {
                String[] keyValue = pair.split(":", 2);
                if (keyValue.length == 2) {
                    paramMap.put(keyValue[0].toUpperCase(), keyValue[1]);
                }
            }
        }

        // Add default parameters based on statue ID if needed
        switch (statueID) {
            // Examples based on likely statue requirements
            case 31: // Freyr - requires TILE if not provided
                if (!paramMap.containsKey("TILE")) {
                    // Default implementation - in real usage, UI should always provide this
                    System.err.println("Warning: TILE parameter missing for Freyr statue");
                }
                break;

            case 32: // Hel - requires PLAYER if not provided
                if (!paramMap.containsKey("PLAYER")) {
                    System.err.println("Warning: PLAYER parameter missing for Hel statue");
                }
                break;

            case 33: // Dwarf - requires STRUCTURE
                if (!paramMap.containsKey("STRUCTURE")) {
                    System.err.println("Warning: STRUCTURE parameter missing for Dwarf statue");
                }
                break;

            case 34: // Odin - requires ARTIFACT
                if (!paramMap.containsKey("ARTIFACT")) {
                    System.err.println("Warning: ARTIFACT parameter missing for Odin statue");
                }
                break;

            case 35: // Thor - requires PLAYER and TILE
                if (!paramMap.containsKey("PLAYER") || !paramMap.containsKey("TILE")) {
                    System.err.println("Warning: PLAYER or TILE parameter missing for Thor statue");
                }
                break;
        }

        return paramMap;
    }
}