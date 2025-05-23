package ch.unibas.dmi.dbis.cs108.client.ui.events.game;

import ch.unibas.dmi.dbis.cs108.client.ui.events.UIEvent;

/**
 * Event representing the action of upgrading a statue in the game.
 * This event is triggered when a player attempts to upgrade a statue.
 */
public class UpgradeStatueUIEvent implements UIEvent {
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

    /**
     * Constructor for UpgradeStatueUIEvent.
     *
     * @param statueId The ID of the statue being upgraded.
     * @param x        The x-coordinate of the statue's location.
     * @param y        The y-coordinate of the statue's location.
     */
    public UpgradeStatueUIEvent(int statueId, int x, int y) {
        this.x = x;
        this.y = y;
        this.statueId = statueId;
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
     * Returns the type of this event.
     *
     * @return The type of this event.
     */
    @Override
    public String getType() {
        return "UPGRADE_STATUE"; // Corrected type
    }
}
