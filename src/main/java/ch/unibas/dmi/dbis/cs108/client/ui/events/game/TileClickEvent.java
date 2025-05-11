package ch.unibas.dmi.dbis.cs108.client.ui.events.game;

import ch.unibas.dmi.dbis.cs108.client.ui.events.UIEvent;

/**
 * Event indicating a player clicked on a specific tile (hex) on the game board.
 */
public class TileClickEvent implements UIEvent {

    /**
     * The row coordinate of the clicked tile.
     */
    private final int row;
    /**
     * The column coordinate of the clicked tile.
     */
    private final int col;
    /**
     * The timestamp of when the event occurred.
     */
    private final long timestamp;

    /**
     * Constructs a TileClickEvent.
     *
     * @param row the row coordinate
     * @param col the column coordinate
     */
    public TileClickEvent(int row, int col) {
        this.row = row;
        this.col = col;
        this.timestamp = System.currentTimeMillis();
    }

    /**
     * @return the row coordinate
     */
    public int getRow() {
        return row;
    }

    /**
     * @return the column coordinate
     */
    public int getCol() {
        return col;
    }

    /**
     * @return the timestamp of the click event
     */
    public long getTimestamp() {
        return timestamp;
    }

    /**
     * @return the event type identifier
     */
    @Override
    public String getType() {
        return "TILE_CLICK";
    }

    /**
     * Returns a string representation of the TileClickEvent.
     *
     * @return a string representation of the event
     */
    @Override
    public String toString() {
        return "TileClickEvent{" +
                "row=" + row +
                ", col=" + col +
                ", timestamp=" + timestamp +
                '}';
    }
}
