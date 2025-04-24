package ch.unibas.dmi.dbis.cs108.client.core.events;

import ch.unibas.dmi.dbis.cs108.shared.game.Player;
import ch.unibas.dmi.dbis.cs108.shared.game.Tile;

/**
 * UIEvent representing the placement of a trap by a player.
 * This event is triggered when a player places a trap in the game.
 */
public class TrapPlacedEvent implements GameEvent {
    private final Player player;
    //todo: private final sth trap;
    private final Tile tile;

    /**
     * Constructor for TrapPlacedEvent.
     *
     * @param player The player who placed the trap.
     * @param tile   The tile where the trap was placed.
     */
    public TrapPlacedEvent(Player player, Tile tile) {
        this.player = player;
        //this.trap = trap;
        this.tile = tile;
    }

    /**
     * Get the player who placed the trap.
     *
     * @return The player.
     */
    public Player getPlayer() {
        return player;
    }

    /**
     * Get the tile where the trap was placed.
     *
     * @return The tile.
     */
    public Tile getTile() {
        return tile;
    }
}
