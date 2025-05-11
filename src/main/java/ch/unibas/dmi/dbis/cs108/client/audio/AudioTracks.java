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
        INTRO_EFFECT("effect_logo"),
        BUTTON_CLICK("effect_button"),
        MUTE_UNMUTE("effect_muteUnmute"),
        PLACE_STRUCTURE("effect_placeStructure"),
        USE_STRUCTURE("effect_useStructure"),
        USE_ARTIFACT("effect_useArtifact"),
        FANFARE("effect_fanfare"),
        BUY_TILE("effect_buyTile"),
        SELECT_CARD("effect_selectCard"),
        MESSAGE_SENT("effect_messageSent"),

        // Music tracks
        MAIN_MENU_CHOIR("music_mainMenuChoir"),
        LOBBY_SCREEN_EPIC("music_mainMenuEpic"),
        GAME_BACKGROUND_PRIMARY("music_gameBackgroundPrimary"),
        GAME_BACKGROUND_SECONDARY("music_gameBackgroundSecondary"),
        UPBEAT_WINSCREEN("music_upbeatWinscreen");

        private final String fileName;

        Track(String fileName) {
            this.fileName = fileName;
        }

        public String getFileName() {
            return fileName;
        }
    }
}
