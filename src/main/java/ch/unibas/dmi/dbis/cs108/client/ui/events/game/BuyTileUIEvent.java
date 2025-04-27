package ch.unibas.dmi.dbis.cs108.client.ui.events.game;

import ch.unibas.dmi.dbis.cs108.client.ui.events.UIEvent;

/**
 * Event representing a request to buy a tile on the board.
 * Coordinates follow UI convention (row, col).
 */
public class BuyTileUIEvent implements UIEvent {
    private final int row; // Renamed from x
    private final int col; // Renamed from y

    /**
     * Constructs a BuyTileUIEvent.
     *
     * @param row the row coordinate (UI convention)
     * @param col the column coordinate (UI convention)
     */
    public BuyTileUIEvent(int row, int col) { // Renamed parameters
        this.row = row;
        this.col = col;
    }

    /**
     * @return the row coordinate
     */
    public int getRow() { // Renamed from getX
        return row;
    }

    /**
     * @return the column coordinate
     */
    public int getCol() { // Renamed from getY
        return col;
    }

    /**
     * @return the event type identifier
     */
    @Override
    public String getType() {
        return "BUY_TILE";
    }
}
