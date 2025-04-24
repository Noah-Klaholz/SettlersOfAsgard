package ch.unibas.dmi.dbis.cs108.client.ui.events.game;

import ch.unibas.dmi.dbis.cs108.client.ui.events.UIEvent;

/**
 * Event representing the action of buying a statue in the game.
 * This event is triggered when a player attempts to purchase a statue.
 */
public class BuyStatueUIEvent implements UIEvent {
    /**
     * The ID of the statue being purchased.
     */
    private final int statueId;

    /**
     * Constructor for BuyStatueUIEvent.
     *
     * @param statueId The ID of the statue being purchased.
     */
    public BuyStatueUIEvent(int statueId) {
        this.statueId = statueId;
    }

    /**
     * Returns the ID of the statue being purchased.
     *
     * @return The ID of the statue.
     */
    public int getStatueId() {
        return statueId;
    }

    /**
     * Returns the type of this event.
     *
     * @return The type of this event.
     */
    @Override
    public String getType() {
        return "BUY_STATUE";
    }
}
