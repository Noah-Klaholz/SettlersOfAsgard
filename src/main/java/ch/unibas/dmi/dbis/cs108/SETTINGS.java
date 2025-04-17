package ch.unibas.dmi.dbis.cs108;

/**
 * The {@code SETTINGS} class contains configuration settings for the game server and client.
 */
public class SETTINGS {
    public enum Config {
        PING_INTERVAL(1000),
        TIMEOUT(2 * 1000), // Milliseconds
        MAX_ARTIFACTS(3),
        START_RUNES(50),
        START_ENERGY(4),
        CHANCE_FOR_CURSE(1),
        MIN_RESSOURCE_VALUE(10),
        MAX_RESOURCE_VALUE(20),
        ARTIFACT_CHANCE(10); // Percentage

        private final int value;

        Config(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }
}
