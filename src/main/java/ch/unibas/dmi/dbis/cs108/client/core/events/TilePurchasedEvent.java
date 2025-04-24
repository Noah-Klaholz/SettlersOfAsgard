package ch.unibas.dmi.dbis.cs108.client.core.events;

import ch.unibas.dmi.dbis.cs108.shared.game.Player;
import ch.unibas.dmi.dbis.cs108.shared.game.Tile;

/**
 * UIEvent representing the purchase of a tile by a player.
 * This event is triggered when a player purchases a tile in the game.
 */
public class TilePurchasedEvent implements GameEvent {
    private final Player player;
    private final Tile tile;

    /**
     * Constructor for TilePurchasedEvent.
     *
     * @param player The player who purchased the tile.
     * @param tile   The tile that was purchased.
     */
    public TilePurchasedEvent(Player player, Tile tile) {
        this.player = player;
        this.tile = tile;
    }

    /**
     * Get the player who purchased the tile.
     *
     * @return The player.
     */
    public Player getPlayer() {
        return player;
    }

    /**
     * Get the tile that was purchased.
     *
     * @return The tile.
     */
    public Tile getTile() {
        return tile;
    }
}
