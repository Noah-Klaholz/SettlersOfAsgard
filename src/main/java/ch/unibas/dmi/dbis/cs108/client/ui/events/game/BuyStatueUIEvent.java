package ch.unibas.dmi.dbis.cs108.client.ui.events.game;

import ch.unibas.dmi.dbis.cs108.client.ui.events.UIEvent;

/**
 * UIEvent representing a request to buy a statue in the game.
 */
public class BuyStatueUIEvent implements UIEvent {
    /**
     * The ID of the statue to be bought.
     */
    private final int statueId;

    /**
     * Constructs a new BuyStatueUIEvent.
     *
     * @param statueId the ID of the statue to be bought
     */
    public BuyStatueUIEvent(int statueId) {
        this.statueId = statueId;
    }

    /**
     * Gets the ID of the statue to be bought.
     *
     * @return the statue ID
     */
    public int getStatueId() {
        return statueId;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getType() {
        return "BUY_STATUE";
    }
}
