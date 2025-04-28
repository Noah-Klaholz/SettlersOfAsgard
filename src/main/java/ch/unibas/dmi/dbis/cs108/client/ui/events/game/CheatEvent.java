package ch.unibas.dmi.dbis.cs108.client.ui.events.game;

import ch.unibas.dmi.dbis.cs108.client.ui.events.UIEvent;

/**
 * CheatEvent is an event that represents a cheat code entered by the user.
 * It contains the cheat code as a string.
 */
public class CheatEvent implements UIEvent {
    /** The cheat code entered by the user. */
    private final Cheat cheatCode;

    /**
     * Constructs a CheatEvent with the specified cheat code.
     *
     * @param cheatCode the cheat code entered by the user
     */
    public CheatEvent(Cheat cheatCode) {
        this.cheatCode = cheatCode;
    }

    /**
     * Returns the cheat code entered by the user.
     *
     * @return the cheat code
     */
    public String getCheatCode() {
        return cheatCode.code;
    }

    /**
     * Returns the cheat code enum.
     *
     * @return the cheat code enum
     */
    public Cheat getCheat() {
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

    public enum Cheat {
        /**Cheat Code for destroying all structures*/
        RAGNAROK("RAGN"),
        /**Cheat Code for claiming all tiles*/
        CLAIMALL("CLAM");

        /**The cheat code String*/
        private final String code;

        /**
         * Constructs a Cheat with the specified cheat code.
         *
         * @param code the cheat code
         */
        Cheat(String code) {
            this.code = code;
        }

        /**
         * Returns the cheat code.
         *
         * @return the cheat code
         */
        public String getCode() {
            return code;
        }
    }
}
