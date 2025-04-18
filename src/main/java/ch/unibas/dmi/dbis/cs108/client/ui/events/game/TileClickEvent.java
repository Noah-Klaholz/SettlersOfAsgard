package ch.unibas.dmi.dbis.cs108.client.ui.events.game;

import ch.unibas.dmi.dbis.cs108.client.ui.events.UIEvent;

/**
 * Event fired when a tile on the game grid is clicked.
 */
public class TileClickEvent implements UIEvent {
    private final int row;
    private final int col;
    private final long timestamp;

    /**
     * Creates a new tile click event with the specified grid coordinates.
     *
     * @param row The row index of the clicked tile
     * @param col The column index of the clicked tile
     */
    public TileClickEvent(int row, int col) {
        this.row = row;
        this.col = col;
        this.timestamp = System.currentTimeMillis();
    }

    /**
     * Gets the row index of the clicked tile.
     *
     * @return The row index
     */
    public int getRow() {
        return row;
    }

    /**
     * Gets the column index of the clicked tile.
     *
     * @return The column index
     */
    public int getCol() {
        return col;
    }

    /**
     * Gets the timestamp when the event was created.
     *
     * @return The timestamp in milliseconds
     */
    public long getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        return "TileClickEvent{" +
                "row=" + row +
                ", col=" + col +
                ", timestamp=" + timestamp +
                '}';
    }

    @Override
    public String getType() {
        return "TILECLICK";
    }
}
