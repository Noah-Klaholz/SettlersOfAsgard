package ch.unibas.dmi.dbis.cs108.client.ui.events.game;

import ch.unibas.dmi.dbis.cs108.client.ui.events.UIEvent;

/**
 * Event representing the action of placing a bought statue in the game.
 * This event is triggered when a player confirms placement on a tile.
 */
public class PlaceStatueUIEvent implements UIEvent {
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
     * Constructor for PlaceStatueUIEvent.
     *
     * @param x        The x-coordinate of the location where the statue is to be
     *                 placed.
     * @param y        The y-coordinate of the location where the statue is to be
     *                 placed.
     * @param statueId The ID of the statue being placed.
     */
    public PlaceStatueUIEvent(int x, int y, int statueId) {
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
        return "PLACE_STATUE"; // Corrected type
    }
}
