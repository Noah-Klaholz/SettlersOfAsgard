package ch.unibas.dmi.dbis.cs108.client.core.entities;

/**
 * Class representing an active trap in the game.
 * A trap SET by a player - not a findable one: that would be an artifact
 */
public class ClientActiveTrap {
    private final int trapID;
    private final int lostRunes;

    /**
     * Constructor for ActiveTrap.
     *
     * @param trapID   The ID of the trap.
     * @param lostRunes The number of runes lost when the trap is triggered.
     */
    public ClientActiveTrap(int trapID, int lostRunes) {
        this.trapID = trapID;
        this.lostRunes = lostRunes;
    }
}
