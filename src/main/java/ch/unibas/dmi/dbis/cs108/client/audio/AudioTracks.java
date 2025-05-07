package ch.unibas.dmi.dbis.cs108.client.audio;

public class AudioTracks {
    public enum Track {
        // Sound effects
        INTRO_EFFECT("track2"), // placeHolder


        // Music tracks
        MAIN_MENU_CHOIR("music_mainMenuChoir"),
        MAIN_MENU_EPIC("music_mainMenuEpic"),
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
