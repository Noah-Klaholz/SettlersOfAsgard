package ch.unibas.dmi.dbis.cs108.client.core.events;

import ch.unibas.dmi.dbis.cs108.shared.game.Player;
import ch.unibas.dmi.dbis.cs108.shared.game.Tile;

/**
 * UIEvent representing the triggering of a trap by a player.
 * This event is triggered when a player triggers a trap in the game.
 */
public class TrapTriggeredEvent implements GameEvent {
    private final Player triggeredPlayer;
    //todo: private final sth trap;
    // This event should only be triggered as a result of a change in gameState (not directly by the client). DO NOT focus on this, its not necessary.
    private final Tile tile;

    /**
     * Constructor for TrapTriggeredEvent.
     *
     * @param triggeredPlayer The player who triggered the trap.
     * @param tile           The tile where the trap was triggered.
     */
    public TrapTriggeredEvent(Player triggeredPlayer, Tile tile) {
        this.triggeredPlayer = triggeredPlayer;
        //this.trap = trap;
        this.tile = tile;
    }

    /**
     * Get the player who triggered the trap.
     *
     * @return The player.
     */
    public Player getTriggeredPlayer() {
        return triggeredPlayer;
    }

    /**
     * Get the tile where the trap was triggered.
     *
     * @return The tile.
     */
    public Tile getTile() {
        return tile;
    }
}