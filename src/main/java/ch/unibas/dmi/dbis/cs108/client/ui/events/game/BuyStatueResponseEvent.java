package ch.unibas.dmi.dbis.cs108.client.ui.events.game;

import ch.unibas.dmi.dbis.cs108.client.ui.events.UIEvent;

/**
 * Represents the response/result of a BuyStatue action.
 */
public class BuyStatueResponseEvent implements UIEvent {

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
    public BuyStatueResponseEvent(boolean success, String message, String statueId) {
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
     * @return the event type identifier
     */
    @Override
    public String getType() {
        return "BUY_STATUE_RESPONSE";
    }
}
