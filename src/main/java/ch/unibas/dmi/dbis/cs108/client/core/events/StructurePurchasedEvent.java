package ch.unibas.dmi.dbis.cs108.client.core.events;

import ch.unibas.dmi.dbis.cs108.shared.entities.Purchasables.Structure;
import ch.unibas.dmi.dbis.cs108.shared.game.Player;
import ch.unibas.dmi.dbis.cs108.shared.game.Tile;

/**
 * UIEvent representing the purchase of a structure by a player.
 * This event is triggered when a player purchases a structure in the game.
 */
public class StructurePurchasedEvent implements GameEvent {
    private final Player player;
    private final Tile tile;
    private final Structure structure;

    /**
     * Constructor for StructurePurchasedEvent.
     *
     * @param player    The player who purchased the structure.
     * @param tile      The tile where the structure was purchased.
     * @param structure The structure that was purchased.
     */
    public StructurePurchasedEvent(Player player, Tile tile, Structure structure) {
        this.player = player;
        this.tile = tile;
        this.structure = structure;
    }

    /**
     * Get the player who purchased the structure.
     *
     * @return The player.
     */
    public Player getPlayer() {
        return player;
    }

    /**
     * Get the tile where the structure was purchased.
     *
     * @return The tile.
     */
    public Tile getTile() {
        return tile;
    }

    /**
     * Get the structure that was purchased.
     *
     * @return The structure.
     */
    public Structure getStructure() {
        return structure;
    }
}
