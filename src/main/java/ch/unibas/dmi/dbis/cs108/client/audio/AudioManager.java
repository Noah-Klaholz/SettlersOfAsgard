package ch.unibas.dmi.dbis.cs108.client.audio;

import ch.unibas.dmi.dbis.cs108.SETTINGS;
import javafx.application.Platform;
import javafx.scene.media.AudioClip;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

import java.io.File;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Singleton class to manage audio playback in the application.
 */
public class AudioManager {
    /** Singleton instance of AudioManager */
    private static AudioManager instance;
    /** Map of music tracks, keyed by their names */
    private final Map<String, Media> musicTracks = new LinkedHashMap<>();
    /** Map of sound effects, keyed by their names */
    private final Map<String, AudioClip> soundEffects = new ConcurrentHashMap<>();
    /** List of music track names for easy access */
    private final List<String> musicTrackNames = new ArrayList<>();
    /** Media player for current music */
    private MediaPlayer currentMusicPlayer;
    /** Media player for next music (for crossfade) */
    private MediaPlayer nextMusicPlayer;
    /** Index of the currently playing music track */
    private int currentMusicIndex = 0;
    /** Volume level (0.0 to 1.0) */
    private double volume = 0.5;
    /** Flag to indicate if audio is muted */
    private boolean muted = false;

    /** Private constructor to prevent instantiation */
    private AudioManager() {
        preloadAudio();
    }

    /**
     * Returns the singleton instance of AudioManager.
     *
     * @return the singleton instance
     */
    public static AudioManager getInstance() {
        if (instance == null) {
            instance = new AudioManager();
        }
        return instance;
    }

    /**
     * Preloads audio files from the resources directory.
     * This method scans the "sounds" directory for audio files and loads them into memory.
     */
    private void preloadAudio() {
        File soundsDir = new File("src/main/resources/sounds");
        if (!soundsDir.exists() || !soundsDir.isDirectory()) return;

        for (File file : Objects.requireNonNull(soundsDir.listFiles())) {
            String name = file.getName();
            if (name.startsWith("music_") && (name.endsWith(".wav") || name.endsWith(".mp3"))) {
                String key = name.substring(0, name.length() - 4); // remove .wav
                Media media = new Media(file.toURI().toString());
                musicTracks.put(key, media);
                musicTrackNames.add(key);
            } else if (name.startsWith("effect_") && (name.endsWith(".wav") || name.endsWith(".mp3"))) {
                String key = name.substring(0, name.length() - 4);
                AudioClip clip = new AudioClip(file.toURI().toString());
                soundEffects.put(key, clip);
            }
        }
    }

    /**
     * Plays a music track by its name.
     *
     * @param name the name of the music track to play
     */
    public void playMusic(String name) {
        Media media = musicTracks.get(name);
        if (media == null) return;
        if (currentMusicPlayer != null) {
            crossfadeTo(media);
        } else {
            currentMusicPlayer = new MediaPlayer(media);
            applyMusicSettings(currentMusicPlayer);
            currentMusicPlayer.setCycleCount(MediaPlayer.INDEFINITE);
            currentMusicPlayer.play();
            currentMusicIndex = musicTrackNames.indexOf(name);
        }
    }

    /**
     * Plays the next music track in the list.
     * If the current track is the last one, it loops back to the first track.
     */
    public void playNextMusic() {
        if (musicTrackNames.isEmpty()) return;
        int nextIndex = (currentMusicIndex + 1) % musicTrackNames.size();
        playMusic(musicTrackNames.get(nextIndex));
    }

    /**
     * Plays the previous music track in the list.
     * If the current track is the first one, it loops back to the last track.
     */
    private void crossfadeTo(Media nextMedia) {
        if (currentMusicPlayer == null) {
            currentMusicPlayer = new MediaPlayer(nextMedia);
            applyMusicSettings(currentMusicPlayer);
            currentMusicPlayer.setCycleCount(MediaPlayer.INDEFINITE);
            currentMusicPlayer.play();
            return;
        }
        nextMusicPlayer = new MediaPlayer(nextMedia);
        applyMusicSettings(nextMusicPlayer);
        nextMusicPlayer.setCycleCount(MediaPlayer.INDEFINITE);
        nextMusicPlayer.setVolume(0);
        nextMusicPlayer.play();

        double fadeDuration = SETTINGS.Config.AUDIO_CROSSFADE_DURATION_MS.getValue() / 1000.0;
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            double t = 0;
            @Override
            public void run() {
                t += 0.05;
                double progress = Math.min(t / fadeDuration, 1.0);
                Platform.runLater(() -> {
                    if (currentMusicPlayer != null) {
                        currentMusicPlayer.setVolume(volume * (1 - progress) * (muted ? 0 : 1));
                    }
                    if (nextMusicPlayer != null) {
                        nextMusicPlayer.setVolume(volume * progress * (muted ? 0 : 1));
                    }
                });
                if (progress >= 1.0) {
                    timer.cancel();
                    Platform.runLater(() -> {
                        if (currentMusicPlayer != null) {
                            currentMusicPlayer.stop();
                            currentMusicPlayer.dispose();
                        }
                        currentMusicPlayer = nextMusicPlayer;
                        nextMusicPlayer = null;
                        currentMusicPlayer.setVolume(volume * (muted ? 0 : 1));
                    });
                }
            }
        }, 0, 50);
    }

    /**
     * Stops the currently playing music.
     *
     * @param player the MediaPlayer to apply settings to
     */
    private void applyMusicSettings(MediaPlayer player) {
        player.setVolume(volume * (muted ? 0 : 1));
    }

    /**
     * Plays a sound effect by its name.
     * If a previous sound effect is still playing, it will be interrupted.
     *
     * @param name the name of the sound effect to play
     */
    public void playSoundEffect(String name) {
        AudioClip clip = soundEffects.get(name);
        if (clip == null) return;
        clip.stop(); // Interrupt previous if still playing
        clip.setVolume(volume * (muted ? 0 : 1));
        clip.play();
    }

    /**
     * Sets the volume level for all audio playbacks.
     *
     *
     * @param volume the volume level (0.0 to 1.0)
     */
    public void setVolume(double volume) {
        this.volume = volume;
        if (currentMusicPlayer != null) {
            currentMusicPlayer.setVolume(volume * (muted ? 0 : 1));
        }
        // AudioClips get volume set on play
    }

    /**
     * Sets the mute state for the current audio playback.
     *
     * @param mute true to mute audio, false to unmute
     */
    public void setMute(boolean mute) {
        this.muted = mute;
        if (currentMusicPlayer != null) {
            currentMusicPlayer.setVolume(volume * (muted ? 0 : 1));
        }
    }

    /**
     * Gets the Set of available music tracks.
     *
     * @return a Set of music track names
     */
    public Set<String> getAvailableMusic() {
        return Collections.unmodifiableSet(musicTracks.keySet());
    }

    /**
     * Gets the Set of available sound effects.
     *
     * @return a Set of sound effect names
     */
    public Set<String> getAvailableEffects() {
        return Collections.unmodifiableSet(soundEffects.keySet());
    }
}
