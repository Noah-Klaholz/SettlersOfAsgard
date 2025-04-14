package ch.unibas.dmi.dbis.cs108.client.core.events;

import ch.unibas.dmi.dbis.cs108.shared.game.Player;
import ch.unibas.dmi.dbis.cs108.shared.game.Tile;
import ch.unibas.dmi.dbis.cs108.shared.entities.Purchasables.Statue;

public class StatuePurchasedEvent implements GameEvent {
    private final Player player;
    private final Tile tile;
    private final Statue statue;

    public StatuePurchasedEvent(Player player, Tile tile, Statue statue) {
        this.player = player;
        this.tile = tile;
        this.statue = statue;
    }

    public Player getPlayer() {
        return player;
    }

    public Tile getTile() {
        return tile;
    }

    public Statue getStatue() {
        return statue;
    }
}
