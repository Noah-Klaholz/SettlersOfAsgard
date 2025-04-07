package ch.unibas.dmi.dbis.cs108.client.core.events;

import ch.unibas.dmi.dbis.cs108.client.core.entities.Player;
import ch.unibas.dmi.dbis.cs108.client.core.entities.Tile;

public class TrapPlacedEvent implements GameEvent {
    private final Player player;
    //todo: private final sth trap;
    private final Tile tile;

    public TrapPlacedEvent(Player player, Tile tile) {
        this.player = player;
        //this.trap = trap;
        this.tile = tile;
    }

    public Player getPlayer() {
        return player;
    }

//    public Trap getTrap() {
//        return trap;
//    }

    public Tile getTile() {
        return tile;
    }
}
