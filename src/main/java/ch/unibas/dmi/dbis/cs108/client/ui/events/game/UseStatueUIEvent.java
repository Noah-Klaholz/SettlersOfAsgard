package ch.unibas.dmi.dbis.cs108.client.ui.events.game;

import ch.unibas.dmi.dbis.cs108.client.ui.events.UIEvent;

/**
 * Event representing a request to use a statue on the board.
 */
public class UseStatueUIEvent implements UIEvent {
    /**
     * The x-coordinate where the statue should be used.
     */
    private final int x;
    /**
     * The y-coordinate where the statue should be used.
     */
    private final int y;
    /**
     * The ID of the statue to be used.
     */
    private final int statueId;
    /**
     * The type of use (e.g., "use", "destroy").
     */
    private final String params;

    /**
     * Constructs a UseStatueUIEvent.
     *
     * @param x        the x-coordinate
     * @param y        the y-coordinate
     * @param statueId the ID of the statue
     * @param params   the type of use
     */
    public UseStatueUIEvent(int x, int y, int statueId, String params) {
        this.x = x;
        this.y = y;
        this.statueId = statueId;
        this.params = params;
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
     * @return the statue ID
     */
    public int getStatueId() {
        return statueId;
    }

    /**
     * @return the event type identifier
     */
    @Override
    public String getType() {
        return "USE_STATUE";
    }

    /**
     * @return the type of use (e.g., "use", "destroy")
     */
    public String getParams() {
        return params;
    }
}
