package ch.unibas.dmi.dbis.cs108.client.audio;

public class AudioTracks {
    public enum Track {
        // Sound effects
        INTRO_EFFECT("effect_logo.mp3"), // placeHolder
        BUTTON_CLICK("effect_button.mp3"),
        PLACE_STRUCTURE("effect_placeStructure.mp3"),
        USE_ARTIFACT("effect_useArtifact.mp3"),
        FANFARE("effect_fanfare.mp3"),

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
