package ch.unibas.dmi.dbis.cs108.client.ui.events.game;

import ch.unibas.dmi.dbis.cs108.client.ui.events.UIEvent;

/**
 * Represents the response/result of an UpgradeStatue action.
 */
public class UpgradeStatueResponseEvent implements UIEvent {
    /**
     * The x-coordinate of the location where the statue is to be placed.
     */
    private final int x;
    /**
     * The y-coordinate of the location where the statue is to be placed.
     */
    private final int y;
    /**
     * The ID of the statue being purchased.
     */
    private final int statueId;
    private final boolean success;
    private final String message;

    /**
     * Constructor for UpgradeStatueResponseEvent.
     *
     * @param success  Whether the upgrade was successful.
     * @param message  A message detailing the result.
     * @param statueId The ID of the statue being upgraded.
     * @param x        The x-coordinate of the statue's location.
     * @param y        The y-coordinate of the statue's location.
     */
    public UpgradeStatueResponseEvent(boolean success, String message, int statueId, int x, int y) {
        this.x = x;
        this.y = y;
        this.statueId = statueId;
        this.success = success;
        this.message = message;
    }

    /**
     * Returns the ID of the statue being purchased.
     *
     * @return The ID of the statue.
     */
    public int getStatueId() {
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
     * Returns whether the upgrade was successful.
     * 
     * @return true if successful, false otherwise.
     */
    public boolean isSuccess() {
        return success;
    }

    /**
     * Returns the message associated with the response.
     * 
     * @return The message.
     */
    public String getMessage() {
        return message;
    }

    /**
     * Returns the type of this event.
     *
     * @return The type of this event.
     */
    @Override
    public String getType() {
        return "UPGRADE_STATUE_RESPONSE"; // Corrected type
    }
}
