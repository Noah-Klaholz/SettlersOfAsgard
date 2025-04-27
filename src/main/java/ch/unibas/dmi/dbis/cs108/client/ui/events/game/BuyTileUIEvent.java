package ch.unibas.dmi.dbis.cs108.client.ui.events.game;

import ch.unibas.dmi.dbis.cs108.client.ui.events.UIEvent;

/**
 * Event representing a request to buy a tile on the board.
 */
public class BuyTileUIEvent implements UIEvent {
    private final int x;
    private final int y;

    /**
     * Constructs a BuyTileUIEvent.
     *
     * @param x the x-coordinate
     * @param y the y-coordinate
     */
    public BuyTileUIEvent(int x, int y) {
        this.x = x;
        this.y = y;
    }

    /**
     * @return the x-coordinate
     */
    public int getX() {
        return x;
    }

    /**
     * @return the y-coordinate
     */
    public int getY() {
        return y;
    }

    /**
     * @return the event type identifier
     */
    @Override
    public String getType() {
        return "BUY_TILE";
    }
}
