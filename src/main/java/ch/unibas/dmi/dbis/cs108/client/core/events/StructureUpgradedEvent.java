package ch.unibas.dmi.dbis.cs108.client.core.events;

import ch.unibas.dmi.dbis.cs108.shared.entities.Purchasables.Structure;
import ch.unibas.dmi.dbis.cs108.shared.game.Player;

/**
 * UIEvent representing the upgrade of a structure by a player.
 * This event is triggered when a player upgrades a structure in the game.
 */
public class StructureUpgradedEvent implements GameEvent {

    /**
     * The player who upgraded the structure.
     */
    private final Player player;
    /**
     * The structure that was upgraded.
     */
    private final Structure structure;
    /**
     * The previous level of the structure.
     */
    private final int previousLevel;
    /**
     * The new level of the structure.
     */
    private final int newLevel;

    /**
     * Constructor for StructureUpgradedEvent.
     *
     * @param player        The player who upgraded the structure.
     * @param structure     The structure that was upgraded.
     * @param previousLevel The previous level of the structure.
     * @param newLevel      The new level of the structure.
     */
    public StructureUpgradedEvent(Player player, Structure structure, int previousLevel, int newLevel) {
        this.player = player;
        this.structure = structure;
        this.previousLevel = previousLevel;
        this.newLevel = newLevel;
    }

    /**
     * Get the player who upgraded the structure.
     *
     * @return The player.
     */
    public Player getPlayer() {
        return player;
    }

    /**
     * Get the structure that was upgraded.
     *
     * @return The structure.
     */
    public Structure getStructure() {
        return structure;
    }

    /**
     * Get the previous level of the structure.
     *
     * @return The previous level.
     */
    public int getPreviousLevel() {
        return previousLevel;
    }

    /**
     * Get the new level of the structure.
     *
     * @return The new level.
     */
    public int getNewLevel() {
        return newLevel;
    }
}

