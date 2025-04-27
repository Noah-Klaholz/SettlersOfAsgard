package ch.unibas.dmi.dbis.cs108.client.ui.events.game;

import ch.unibas.dmi.dbis.cs108.client.ui.events.UIEvent;

/**
 * Represents the response/result of a BuyTile action.
 * Coordinates follow UI convention (row, col).
 */
public class BuyTileResponseEvent implements UIEvent {

    private final boolean success;
    private final String message;
    private final int row;
    private final int col;

    /**
     * Constructs a BuyTileResponseEvent.
     *
     * @param success whether the action was successful
     * @param message optional message for success/failure
     * @param row     row coordinate of the tile (UI convention)
     * @param col     column coordinate of the tile (UI convention)
     */
    public BuyTileResponseEvent(boolean success, String message, int row, int col) {
        this.success = success;
        this.message = message;
        this.row = row;
        this.col = col;
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
     * @return the row coordinate of the tile
     */
    public int getRow() {
        return row;
    }

    /**
     * @return the column coordinate of the tile
     */
    public int getCol() {
        return col;
    }

    /**
     * @return the event type identifier
     */
    @Override
    public String getType() {
        return "BUY_TILE_RESPONSE";
    }
}
