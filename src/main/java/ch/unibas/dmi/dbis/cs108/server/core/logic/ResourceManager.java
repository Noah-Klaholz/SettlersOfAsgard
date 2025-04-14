package ch.unibas.dmi.dbis.cs108.server.core.logic;

import ch.unibas.dmi.dbis.cs108.shared.entities.Player;
import ch.unibas.dmi.dbis.cs108.shared.entities.Tile;
import ch.unibas.dmi.dbis.cs108.shared.entities.Structure;

public class ResourceManager {

    public void distributeResources(Player player) {
        // Tile income (Runes)
        for (Tile tile : player.getOwnedTiles()) {
            player.addRunes(tile.getResourceValue());
        }

        // Structure income (Energy/Runes)
        for (Structure structure : player.getOwnedStructures()) {
            int resourceValue = calculateStructureResourceValue(structure);
            if (resourceValue <= 4) {
                player.addEnergy(resourceValue);
            } else {
                player.addRunes(resourceValue);
            }
        }
    }

    /**
     * Calculate resource value for a structure based on its type and level
     *
     * @param structure The structure to calculate resource value for
     * @return The resource value produced by this structure
     */
    private int calculateStructureResourceValue(Structure structure) {
        // Default implementation - should be replaced with proper calculation
        // based on structure type, level, and other attributes
        // Todo: Implement actual logic for different structure types
        return 2;
    }
}