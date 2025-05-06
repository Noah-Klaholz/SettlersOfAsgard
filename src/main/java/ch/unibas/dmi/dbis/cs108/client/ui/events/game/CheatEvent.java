package ch.unibas.dmi.dbis.cs108.client.ui.events.game;

import ch.unibas.dmi.dbis.cs108.client.ui.events.UIEvent;

import java.util.Arrays;
import java.util.regex.Pattern;

/**
 * CheatEvent is an event that represents a cheat code entered by the user.
 * It contains the cheat code as a string.
 */
public class CheatEvent implements UIEvent {
    /**
     * The regex pattern for valid cheat codes.
     */
    private static final Pattern CHEAT_PATTERN;

    /**
     * The regex pattern for valid cheat codes.
     * It is built dynamically based on the available cheat codes.
     */
    static {
        // Dynamically build the regex for valid cheat codes
        String cheatCodes = String.join("|",
                Arrays.stream(CheatEvent.Cheat.values())
                        .map(CheatEvent.Cheat::getCode)
                        .toArray(String[]::new)
        );
        CHEAT_PATTERN = Pattern.compile("^/cheatcode\\s+(" + cheatCodes + ")$", Pattern.CASE_INSENSITIVE);
    }

    /**
     * The cheat code entered by the user.
     */
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
     * Returns the regex pattern for valid cheat codes.
     *
     * @return the regex pattern
     */
    public static Pattern getCheatPattern() {
        return CHEAT_PATTERN;
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
        /**
         * Cheat Code for destroying all structures
         */
        RAGNAROK("RAGN"),
        /**
         * Cheat Code for claiming all tiles
         */
        CLAIMALL("CLAM");

        /**
         * The cheat code String
         */
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
         * Returns the cheat code as a string.
         *
         * @return the cheat code
         */
        public static Cheat fromCode(String code) {
            for (Cheat cheat : Cheat.values()) {
                if (cheat.code.equalsIgnoreCase(code)) {
                    return cheat;
                }
            }
            return null;
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
