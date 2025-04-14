package ch.unibas.dmi.dbis.cs108.client.core.events;

import ch.unibas.dmi.dbis.cs108.shared.entities.Player;
import ch.unibas.dmi.dbis.cs108.shared.entities.Tile;

public class TilePurchasedEvent implements GameEvent {
    private final Player player;
    private final Tile tile;

    public TilePurchasedEvent(Player player, Tile tile) {
        this.player = player;
        this.tile = tile;
    }

    public Player getPlayer() {
        return player;
    }

    public Tile getTile() {
        return tile;
    }
}
