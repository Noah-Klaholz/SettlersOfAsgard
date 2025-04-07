package ch.unibas.dmi.dbis.cs108.client.core.events;

import ch.unibas.dmi.dbis.cs108.client.core.entities.Player;
import ch.unibas.dmi.dbis.cs108.client.core.entities.Tile;

public class TrapTriggeredEvent implements GameEvent {
    private final Player triggeredPlayer;
    //todo: private final sth trap;
    private final Tile tile;

    public TrapTriggeredEvent(Player triggeredPlayer, Tile tile) {
        this.triggeredPlayer = triggeredPlayer;
        //this.trap = trap;
        this.tile = tile;
    }

    public Player getTriggeredPlayer() {
        return triggeredPlayer;
    }

//    public Trap getTrap() {
//        return trap;
//    }

    public Tile getTile() {
        return tile;
    }
}