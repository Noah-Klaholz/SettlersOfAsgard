package ch.unibas.dmi.dbis.cs108.client.ui.events.game;

import ch.unibas.dmi.dbis.cs108.client.ui.events.UIEvent;

/**
 * Represents the response/result of a UseStatue action.
 */
public class UseStatueResponseEvent implements UIEvent {

    /**
     * Indicates whether the action was successful.
     */
    private final boolean success;
    /**
     * Optional message for success/failure.
     */
    private final String message;
    /**
     * The x-coordinate of the statue.
     */
    private final int x;
    /**
     * The y-coordinate of the statue.
     */
    private final int y;
    /**
     * The ID of the statue.
     */
    private final String statueId;

    /**
     * Constructs a UseStatueResponseEvent.
     *
     * @param success  whether the action was successful
     * @param message  optional message for success/failure
     * @param x        x-coordinate of the statue
     * @param y        y-coordinate of the statue
     * @param statueId the ID of the statue
     */
    public UseStatueResponseEvent(boolean success, String message, int x, int y, String statueId) {
        this.success = success;
        this.message = message;
        this.x = x;
        this.y = y;
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
     * @return the x-coordinate of the statue
     */
    public int getX() {
        return x;
    }

    /**
     * @return the y-coordinate of the statue
     */
    public int getY() {
        return y;
    }

    /**
     * @return the ID of the statue
     */
    public String getStatueId() {
        return statueId;
    }

    /**
     * @return the event type identifier
     */
    @Override
    public String getType() {
        return "USE_STATUE_RESPONSE";
    }
}
