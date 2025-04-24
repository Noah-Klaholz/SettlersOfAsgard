package ch.unibas.dmi.dbis.cs108.client.ui.events.game;

import ch.unibas.dmi.dbis.cs108.client.ui.events.UIEvent;

/**
 * Event representing a request to use a structure on the board.
 */
public class UseStructureUIEvent implements UIEvent {
    private final int row;
    private final int col;
    private final int structureId;
    private final String useType;

    /**
     * Constructs a UseStructureUIEvent.
     *
     * @param row         the row coordinate
     * @param col         the column coordinate
     * @param structureId the ID of the structure
     * @param useType     the type of use (e.g., "PLACE", "ACTIVATE")
     */
    public UseStructureUIEvent(int row, int col, int structureId, String useType) {
        this.row = row;
        this.col = col;
        this.structureId = structureId;
        this.useType = useType;
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
     * @return the use type
     */
    public String getUseType() {
        return useType;
    }

    /**
     * @return the event type identifier
     */
    @Override
    public String getType() {
        return "USE_STRUCTURE";
    }
}
