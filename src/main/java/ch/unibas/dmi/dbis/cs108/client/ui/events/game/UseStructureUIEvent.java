package ch.unibas.dmi.dbis.cs108.client.ui.events.game;

import ch.unibas.dmi.dbis.cs108.client.ui.events.UIEvent;

/**
 * Event representing a request to use a structure on the board.
 */
public class UseStructureUIEvent implements UIEvent {
    /**
     * The row coordinate where the structure is located.
     */
    private final int row;
    /**
     * The column coordinate where the structure is located.
     */
    private final int col;
    /**
     * The ID of the structure to be used.
     */
    private final int structureId;

    /**
     * Constructs a UseStructureUIEvent.
     *
     * @param row         the row coordinate
     * @param col         the column coordinate
     * @param structureId the ID of the structure
     */
    public UseStructureUIEvent(int row, int col, int structureId) {
        this.row = row;
        this.col = col;
        this.structureId = structureId;
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
     * @return the structure ID
     */
    public int getStructureId() {
        return structureId;
    }


    /**
     * @return the event type identifier
     */
    @Override
    public String getType() {
        return "USE_STRUCTURE";
    }
}
