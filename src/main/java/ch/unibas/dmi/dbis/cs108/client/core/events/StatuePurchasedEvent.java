package ch.unibas.dmi.dbis.cs108.client.core.events;

import ch.unibas.dmi.dbis.cs108.shared.entities.Purchasables.Statues.Statue;
import ch.unibas.dmi.dbis.cs108.shared.game.Player;
import ch.unibas.dmi.dbis.cs108.shared.game.Tile;

/**
 * UIEvent representing the purchase of a statue by a player.
 * This event is triggered when a player purchases a statue in the game.
 */
public class StatuePurchasedEvent implements GameEvent {

    /**
     * The player who purchased the statue.
     */
    private final Player player;
    /**
     * The tile where the statue was purchased.
     */
    private final Tile tile;
    /**
     * The statue that was purchased.
     */
    private final Statue statue;

    /**
     * Constructor for StatuePurchasedEvent.
     *
     * @param player The player who purchased the statue.
     * @param tile   The tile where the statue was purchased.
     * @param statue The statue that was purchased.
     */
    public StatuePurchasedEvent(Player player, Tile tile, Statue statue) {
        this.player = player;
        this.tile = tile;
        this.statue = statue;
    }

    /**
     * Get the player who purchased the statue.
     *
     * @return The player.
     */
    public Player getPlayer() {
        return player;
    }

    /**
     * Get the tile where the statue was purchased.
     *
     * @return The tile.
     */
    public Tile getTile() {
        return tile;
    }

    /**
     * Get the statue that was purchased.
     *
     * @return The statue.
     */
    public Statue getStatue() {
        return statue;
    }
}
