package ch.unibas.dmi.dbis.cs108.client.ui.events.game;

import ch.unibas.dmi.dbis.cs108.client.ui.events.UIEvent;

/**
 * CheatEvent is an event that represents a cheat code entered by the user.
 * It contains the cheat code as a string.
 */
public class CheatEvent implements UIEvent {
    /** The cheat code entered by the user. */
    private final String cheatCode;

    /**
     * Constructs a CheatEvent with the specified cheat code.
     *
     * @param cheatCode the cheat code entered by the user
     */
    public CheatEvent(String cheatCode) {
        this.cheatCode = cheatCode;
    }

    /**
     * Returns the cheat code entered by the user.
     *
     * @return the cheat code
     */
    public String getCheatCode() {
        return cheatCode;
    }

    /**
     * Returns the event type string.
     *
     * @return event type string
     */
    @Override
    public String getType() {
        return "CheatEvent";
    }
}
