package ch.unibas.dmi.dbis.cs108.client.ui.events.game;

import ch.unibas.dmi.dbis.cs108.client.ui.events.UIEvent;

import java.util.Map;

/**
 * Carries the game item prices received from the server.
 */
public class PricesResponseEvent implements UIEvent {

    private final Map<String, Integer> prices;

    /**
     * Constructs a PricesResponseEvent.
     *
     * @param prices a map of item IDs to their prices
     */
    public PricesResponseEvent(Map<String, Integer> prices) {
        this.prices = prices;
    }

    /**
     * @return the map of item prices
     */
    public Map<String, Integer> getPrices() {
        return prices;
    }

    /**
     * @return the event type identifier
     */
    @Override
    public String getType() {
        return "PRICES_RESPONSE";
    }
}
