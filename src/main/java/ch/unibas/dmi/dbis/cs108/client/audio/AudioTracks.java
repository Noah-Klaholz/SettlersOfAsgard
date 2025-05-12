package ch.unibas.dmi.dbis.cs108.client.audio;

/**
 * This class contains the audio tracks used in the application.
 * Each track is represented by an enum constant with a corresponding file name.
 * The file names are used to load the audio files from the resources directory.
 */
public class AudioTracks {
    /**
     * Enum representing the audio tracks used in the application.
     * Each enum constant corresponds to a specific audio track.
     */
    public enum Track {
        // Sound effects
        /**
         * The intro effect is played when the game starts.
         * It is a short sound that indicates the game is loading.
         */
        INTRO_EFFECT("effect_logo"),
        /**
         * The button click effect is played when a button is clicked.
         * It provides feedback to the user that their action has been registered.
         */
        BUTTON_CLICK("effect_button"),
        /**
         * The mute/unmute effect is played when the user mutes or unmutes the audio.
         * It indicates the change in audio state.
         */
        MUTE_UNMUTE("effect_muteUnmute"),
        /**
         * The place structure effect is played when a structure is placed on the game board.
         * It provides feedback to the user that their action has been registered.
         */
        PLACE_STRUCTURE("effect_placeStructure"),
        /**
         * The use structure effect is played when a structure is used.
         * It provides feedback to the user that their action has been registered.
         */
        USE_STRUCTURE("effect_useStructure"),
        /**
         * The use artifact effect is played when an artifact is used.
         * It provides feedback to the user that their action has been registered.
         */
        USE_ARTIFACT("effect_useArtifact"),
        /**
         * The fanfare effect is played when a significant event occurs in the game.
         * It provides feedback to the user that something important has happened.
         */
        FANFARE("effect_fanfare"),
        /**
         * The buy tile effect is played when a tile is purchased.
         * It provides feedback to the user that their action has been registered.
         */
        BUY_TILE("effect_buyTile"),
        /**
         * The select card effect is played when a card is selected.
         * It provides feedback to the user that their action has been registered.
         */
        SELECT_CARD("effect_selectCard"),
        /**
         * The message sent effect is played when a message is sent in the game.
         * It provides feedback to the user that their action has been registered.
         */
        MESSAGE_SENT("effect_messageSent"),

        // Music tracks
        /**
         * Music tracks are used for various background music in the game.
         * These include the main menu music, lobby music, and game background music.
         */
        MAIN_MENU_CHOIR("music_mainMenuChoir"),
        /**
         * The main menu choir music is played in the main menu.
         * It provides a pleasant background ambiance for the user.
         */
        LOBBY_SCREEN_EPIC("music_mainMenuEpic"),
        /**
         * The lobby screen epic music is played in the lobby.
         * It provides a pleasant background ambiance for the user.
         */
        GAME_BACKGROUND_PRIMARY("music_gameBackgroundPrimary"),
        /**
         * The game background primary music is played during the game.
         * It provides a pleasant background ambiance for the user.
         */
        GAME_BACKGROUND_SECONDARY("music_gameBackgroundSecondary"),
        /**
         * The game background secondary music is played during the game.
         * It provides a pleasant background ambiance for the user.
         */
        UPBEAT_WINSCREEN("music_upbeatWinscreen");

        /**
         * The upbeat winscreen music is played when the user wins the game.
         * It provides a pleasant background ambiance for the user.
         */
        private final String fileName;

        /**
         * Constructor for the Track enum.
         *
         * @param fileName The file name of the audio track.
         */
        Track(String fileName) {
            this.fileName = fileName;
        }

        /**
         * Returns the file name of the audio track.
         *
         * @return The file name of the audio track.
         */
        public String getFileName() {
            return fileName;
        }
    }
}
