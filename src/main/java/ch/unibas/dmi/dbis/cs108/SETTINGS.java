package ch.unibas.dmi.dbis.cs108;

/**
 * The {@code SETTINGS} class contains configuration settings for the game
 * server and client.
 */
public class SETTINGS {
    /**
     * The {@code Config} enum defines various configuration settings for the game.
     * Each setting has a default value that can be accessed using the {@code getValue()} method.
     */
    public enum Config {
        /*
         * Configuration settings for the game.
         * Each setting has a default value that can be accessed using the getValue() method.
         */
        SHOW_SPLASH_SCREEN(0), // 1 is true, 0 is false
        /*
         * The duration of the splash screen in seconds.
         * This is used to display a splash screen before the game starts.
         */
        SPLASH_SCREEN_DURATION(3), // Seconds
        /*
         * The duration of the game in seconds.
         * This is used to set the maximum time for each game session.
         */
        PING_INTERVAL(1000),
        /*
         * The interval for sending ping messages to the server.
         * This is used to check the connection status.
         */
        TIMEOUT(3 * 1000), // Milliseconds
        /*
         * The timeout duration for the game.
         * This is used to set the maximum time for each game session.
         */
        MAX_ARTIFACTS(3),
        /*
         * The maximum number of artifacts that can be present in the game.
         * This is used to limit the number of artifacts in the game.
         */
        START_RUNES(50),
        /*
         * The number of runes that each player starts with.
         * This is used to set the initial resources for each player.
         */
        START_ENERGY(0),
        /*
         * The number of energy points that each player starts with.
         * This is used to set the initial resources for each player.
         */
        CHANCE_FOR_CURSE(1),
        /*
         * The chance of getting a curse when using an artifact.
         * This is used to add randomness to the game.
         */
        MIN_RESSOURCE_VALUE(10),
        /*
         * The minimum value of resources that can be generated.
         * This is used to set the lower limit for resource generation.
         */
        MAX_RESOURCE_VALUE(20),
        /*
         * The maximum value of resources that can be generated.
         * This is used to set the upper limit for resource generation.
         */
        ARTIFACT_CHANCE(10), // Percentage
        /*
         * The chance of getting an artifact when using a structure.
         * This is used to add randomness to the game.
         */
        PURCHASABLE_TILES_PER_ROUND(3),
        /*
         * The number of purchasable tiles available each round.
         * This is used to set the number of tiles that can be purchased.
         */
        SET_BONUS_MULTIPLIER(2),
        /*
         * The multiplier for the set bonus.
         * This is used to calculate the bonus for completing a set of tiles.
         */
        MAX_ENERGY(4),
        /*
         * The maximum amount of energy that can be generated.
         * This is used to set the upper limit for energy generation.
         */
        MAX_STRUCTURES(6),
        /*
         * The maximum number of structures that can be placed on the board.
         * This is used to limit the number of structures in the game.
         */
        MONUMENT_SIZE(33),
        /*
         * The size of the monument.
         * This is used to set the dimensions of the monument on the board.
         */
        ENTITY_SIZE(33), // Size of the monument
        /*
         * The size of the entities on the board.
         * This is used to set the dimensions of the entities on the board.
         */
        TURN_TIME(60), // Seconds
        /*
         * The time limit for each turn in seconds.
         * This is used to set the maximum time for each player's turn.
         */
        GRACE_PERIOD(60000), // Milliseconds
        /*
         * The grace period for the game in milliseconds.
         * This is used to set the time limit for each player's turn.
         */
        MAX_RECONNECT_ATTEMPTS(25),
        /*
         * The maximum number of attempts to reconnect to the server.
         * This is used to set the limit for reconnection attempts.
         */
        RECONNECT_DELAYS_MS(2000),
        /*
         * The delay between reconnection attempts in milliseconds.
         * This is used to set the time between reconnection attempts.
         */
        AUDIO_CROSSFADE_DURATION_MS(2500); // Crossfade duration in ms

        /*
         * The duration of the audio crossfade in milliseconds.
         * This is used to set the duration for fading between audio tracks.
         */
        private final int value;

        /**
         * Constructor for the Config enum.
         *
         * @param value The default value for the configuration setting.
         */
        Config(int value) {
            this.value = value;
        }

        /**
         * Gets the default value of the configuration setting.
         *
         * @return The default value of the configuration setting.
         */
        public int getValue() {
            return value;
        }
    }
}
