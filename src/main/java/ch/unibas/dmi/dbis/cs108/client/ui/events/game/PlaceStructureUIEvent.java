package ch.unibas.dmi.dbis.cs108.client.ui.events.game;

import ch.unibas.dmi.dbis.cs108.client.ui.events.UIEvent;

/**
 * Event representing a request to place a structure on the board.
 */
public class PlaceStructureUIEvent implements UIEvent {
    /**
     * The x-coordinate where the structure should be placed.
     */
    private final int x;
    /**
     * The y-coordinate where the structure should be placed.
     */
    private final int y;
    /**
     * The ID of the structure to be placed.
     */
    private final int structureId;

    /**
     * Constructs a PlaceStructureUIEvent.
     *
     * @param x           the x-coordinate
     * @param y           the y-coordinate
     * @param structureId the ID of the structure
     */
    public PlaceStructureUIEvent(int x, int y, int structureId) {
        this.x = x;
        this.y = y;
        this.structureId = structureId;
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
        return "PLACE_STRUCTURE";
    }
}
