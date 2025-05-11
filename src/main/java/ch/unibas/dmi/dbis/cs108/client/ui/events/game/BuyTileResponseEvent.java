package ch.unibas.dmi.dbis.cs108.client.ui.events.game;

import ch.unibas.dmi.dbis.cs108.client.ui.events.UIEvent;

/**
 * Represents the response/result of a BuyTile action.
 */
public class BuyTileResponseEvent implements UIEvent {
    /**
     * Indicates whether the action was successful.
     */

    private final boolean success;
    /**
     * Optional message for success/failure.
     */
    private final String message;
    /**
     * The x-coordinate of the tile.
     */
    private final int x;
    /**
     * The y-coordinate of the tile.
     */
    private final int y;

    /**
     * Constructs a BuyTileResponseEvent.
     *
     * @param success whether the action was successful
     * @param message optional message for success/failure
     * @param x       x-coordinate of the tile
     * @param y       y-coordinate of the tile
     */
    public BuyTileResponseEvent(boolean success, String message, int x, int y) {
        this.success = success;
        this.message = message;
        this.x = x;
        this.y = y;
    }

    /**
     * @return true if the action was successful, false otherwise
     */
    public boolean isSuccess() {
        return success;
    }

    /**
     * @return the message associated with the response
     */
    public String getMessage() {
        return message;
    }

    /**
     * @return the x-coordinate of the tile
     */
    public int getX() {
        return x;
    }

    /**
     * @return the y-coordinate of the tile
     */
    public int getY() {
        return y;
    }

    /**
     * @return the event type identifier
     */
    @Override
    public String getType() {
        return "BUY_TILE_RESPONSE";
    }
}
