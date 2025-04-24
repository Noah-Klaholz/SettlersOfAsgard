package ch.unibas.dmi.dbis.cs108.client.ui.events.game;

import ch.unibas.dmi.dbis.cs108.client.ui.events.UIEvent;

/**
 * Event for requesting or updating game prices.
 */
public class PricesUIEvent implements UIEvent {

    /**
     * Constructs a PricesUIEvent.
     */
    public PricesUIEvent() {
        // No specific data needed for this event currently.
    }

    /**
     * @return the event type identifier
     */
    @Override
    public String getType() {
        return "PRICES_REQUEST";
    }
}
