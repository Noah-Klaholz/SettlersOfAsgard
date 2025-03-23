package ch.unibas.dmi.dbis.cs108;

/**
 * The {@code SETTINGS} class contains configuration settings for the game server and client.
 */
public class SETTINGS {
    public enum Config {
        PING_INTERVAL(1000),
        TIMEOUT(2 * 1000); // Milliseconds

        private final int value;

        Config(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }
}
