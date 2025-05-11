package ch.unibas.dmi.dbis.cs108.client.ui.events.game;

import ch.unibas.dmi.dbis.cs108.client.ui.events.UIEvent;

/**
 * Represents the response/result of a UseStructure action.
 */
public class UseStructureResponseEvent implements UIEvent {

    /**
     * Indicates whether the action was successful.
     */
    private final boolean success;
    /**
     * Optional message for success/failure.
     */
    private final String message;
    /**
     * The x-coordinate of the structure.
     */
    private final int x;
    /**
     * The y-coordinate of the structure.
     */
    private final int y;
    /**
     * The ID of the structure.
     */
    private final String structureId;

    /**
     * Constructs a UseStructureResponseEvent.
     *
     * @param success     whether the action was successful
     * @param message     optional message for success/failure
     * @param x           x-coordinate of the structure
     * @param y           y-coordinate of the structure
     * @param structureId the ID of the structure
     */
    public UseStructureResponseEvent(boolean success, String message, int x, int y, String structureId) {
        this.success = success;
        this.message = message;
        this.x = x;
        this.y = y;
        this.structureId = structureId;
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
     * @return the x-coordinate of the structure
     */
    public int getX() {
        return x;
    }

    /**
     * @return the y-coordinate of the structure
     */
    public int getY() {
        return y;
    }

    /**
     * @return the ID of the structure
     */
    public String getStructureId() {
        return structureId;
    }

    /**
     * @return the event type identifier
     */
    @Override
    public String getType() {
        return "USE_STRUCTURE_RESPONSE";
    }
}
