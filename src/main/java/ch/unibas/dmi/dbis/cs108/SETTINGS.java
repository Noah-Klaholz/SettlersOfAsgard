package ch.unibas.dmi.dbis.cs108;

/**
 * The {@code SETTINGS} class contains configuration settings for the game server and client.
 */
public class SETTINGS {
    public enum Config {
        SHOW_SPLASH_SCREEN(1), // 1 is true, 0 is false
        SPLASH_SCREEN_DURATION(3), // Seconds
        PING_INTERVAL(1000),
        TIMEOUT(2 * 1000), // Milliseconds
        MAX_ARTIFACTS(3),
        START_RUNES(50),
        START_ENERGY(0),
        CHANCE_FOR_CURSE(1),
        MIN_RESSOURCE_VALUE(10),
        MAX_RESOURCE_VALUE(20),
        ARTIFACT_CHANCE(10), // Percentage
        PURCHASABLE_TILES_PER_ROUND(3),
        SET_BONUS_MULTIPLIER(2);

        private final int value;

        Config(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }
}
