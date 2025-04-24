package ch.unibas.dmi.dbis.cs108.client.ui.events.game;

import ch.unibas.dmi.dbis.cs108.client.ui.events.UIEvent;

/**
 * Represents the response/result of a BuyStatue action.
 */
public class PlaceStatueResponseEvent implements UIEvent {
    /**
     * The x-coordinate of the location where the statue is to be placed.
     */
    private final int x;
    /**
     * The y-coordinate of the location where the statue is to be placed.
     */
    private final int y;
    private final boolean success;
    private final String message;
    private final String statueId;

    /**
     * Constructs a BuyStatueResponseEvent.
     *
     * @param success  whether the action was successful
     * @param message  optional message for success/failure
     * @param statueId the ID of the statue
     */
    public PlaceStatueResponseEvent(boolean success, String message, String statueId, int x, int y) {
        this.x = x;
        this.y = y;
        this.success = success;
        this.message = message;
        this.statueId = statueId;
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
     * @return the ID of the statue
     */
    public String getStatueId() {
        return statueId;
    }

    /**
     * Returns the x-coordinate of the location where the statue is to be placed.
     *
     * @return The x-coordinate.
     */
    public int getX() {
        return x;
    }

    /**
     * Returns the y-coordinate of the location where the statue is to be placed.
     *
     * @return The y-coordinate.
     */
    public int getY() {
        return y;
    }

    /**
     * @return the event type identifier
     */
    @Override
    public String getType() {
        return "BUY_STATUE_RESPONSE";
    }
}
