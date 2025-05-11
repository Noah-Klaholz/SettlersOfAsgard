package ch.unibas.dmi.dbis.cs108.client.ui.events.game;

/**
 * Represents a trap location event in the game.
 */
public class TrapLocationEvent {

    private final int x;
    private final int y;
    private final int lostRunes;

    /**
     * Constructor for TrapLocationEvent.
     *
     * @param x          The x-coordinate of the trap location.
     * @param y          The y-coordinate of the trap location.
     * @param lostRunes  The number of runes lost due to the trap.
     */
    public TrapLocationEvent(int x, int y, int lostRunes) {
        this.x = x;
        this.y = y;
        this.lostRunes = lostRunes;
    }

    /**
     * Gets the x-coordinate of the trap location.
     *
     * @return The x-coordinate.
     */
    public int getX() {
        return x;
    }

    /**
     * Gets the y-coordinate of the trap location.
     *
     * @return The y-coordinate.
     */
    public int getY() {
        return y;
    }

    /**
     * Gets the number of runes lost due to the trap.
     *
     * @return The number of lost runes.
     */
    public int getLostRunes() {
        return lostRunes;
    }
}
