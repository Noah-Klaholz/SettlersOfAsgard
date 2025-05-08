package ch.unibas.dmi.dbis.cs108.client.audio;

import ch.unibas.dmi.dbis.cs108.SETTINGS;
import ch.unibas.dmi.dbis.cs108.client.ui.utils.ResourceLoader;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.media.AudioClip;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.control.Button;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Singleton class to manage audio playback in the application.
 */
public class AudioManager {
    private static final Logger LOGGER = Logger.getLogger(AudioManager.class.getName());
    private static final String SOUNDS_DIRECTORY = "/sounds";

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
    private double musicVolume = 0.5;
    /** Effects volume level (0.0 to 1.0) */
    private double effectsVolume = 0.5;
    /** Flag to indicate if audio is muted */
    private boolean muted = false;
    /** Resource loader for loading audio files */
    private final ResourceLoader resourceLoader = new ResourceLoader();
    /** Flag to track if audio playback is available */
    private boolean audioPlaybackAvailable = true;

    /** Private constructor to prevent instantiation */
    private AudioManager() {
        try {
            preloadAudio();
            LOGGER.info("AudioManager initialized. Music tracks loaded: " + musicTracks.keySet());
            LOGGER.info("AudioManager initialized. Sound effects loaded: " + soundEffects.keySet());
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error initializing AudioManager", e);
            audioPlaybackAvailable = false;
        }
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
     * This method uses the ResourceLoader to load audio files in a way that works
     * both in development environment and when packaged in a JAR.
     */
    private void preloadAudio() {
        LOGGER.info("Preloading audio from " + SOUNDS_DIRECTORY);

        try {
            // Since we can't reliably list resources in a JAR, load directly from AudioTracks enum
            loadTracksFromEnum();

            // If no tracks were loaded from enum, try the directory listing approach
            if (musicTracks.isEmpty() && soundEffects.isEmpty()) {
                LOGGER.info("No tracks loaded from enum, trying directory listing");
                loadTracksFromDirectory();
            }

            if (musicTracks.isEmpty() && soundEffects.isEmpty()) {
                LOGGER.warning("No audio files could be loaded. Make sure audio files exist in the resources/sounds directory.");
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error preloading audio files", e);
        }
    }

    /**
     * Loads audio tracks defined in the AudioTracks enum using ResourceLoader.
     */
    private void loadTracksFromEnum() {
        LOGGER.info("Loading audio tracks from AudioTracks enum");

        for (AudioTracks.Track track : AudioTracks.Track.values()) {
            String fileName = track.getFileName();
            LOGGER.info("Attempting to load track: " + fileName);

            // Try MP3 first, then WAV
            if (tryLoadAudioFile(fileName, ".mp3")) {
                LOGGER.info("Successfully loaded " + fileName + " as MP3");
            } else if (tryLoadAudioFile(fileName, ".wav")) {
                LOGGER.info("Successfully loaded " + fileName + " as WAV");
            } else {
                LOGGER.warning("Failed to load track: " + fileName + " (neither MP3 nor WAV found)");
            }
        }
    }

    /**
     * Tries to load an audio file with the given name and extension using ResourceLoader.
     *
     * @param fileName Base file name without extension
     * @param extension File extension including the dot (e.g., ".mp3")
     * @return true if loading was successful, false otherwise
     */
    private boolean tryLoadAudioFile(String fileName, String extension) {
        String resourcePath = SOUNDS_DIRECTORY + "/" + fileName + extension;

        if (fileName.startsWith("music_")) {
            Media media = resourceLoader.loadMusicCached(resourcePath);
            if (media != null) {
                musicTracks.put(fileName, media);
                if (!musicTrackNames.contains(fileName)) {
                    musicTrackNames.add(fileName);
                }
                LOGGER.info("Added music track: " + fileName);
                return true;
            }
        } else if (fileName.startsWith("effect_")) {
            AudioClip clip = resourceLoader.loadSoundEffectCached(resourcePath);
            if (clip != null) {
                soundEffects.put(fileName, clip);
                LOGGER.info("Added sound effect: " + fileName);
                return true;
            }
        }

        return false;
    }

    /**
     * Attempts to load audio files by listing the contents of the sounds directory.
     * This is a fallback method and may not work reliably in a JAR.
     */
    private void loadTracksFromDirectory() {
        List<String> audioFiles = listResourceFiles(SOUNDS_DIRECTORY);

        if (audioFiles.isEmpty()) {
            LOGGER.warning("No audio files found in " + SOUNDS_DIRECTORY);
            return;
        }

        LOGGER.info("Found " + audioFiles.size() + " audio files in " + SOUNDS_DIRECTORY);

        for (String filePath : audioFiles) {
            try {
                String fileName = filePath.substring(filePath.lastIndexOf('/') + 1);

                if (!fileName.endsWith(".mp3") && !fileName.endsWith(".wav")) {
                    continue; // Skip non-audio files
                }

                String key = fileName.substring(0, fileName.lastIndexOf('.'));

                if (fileName.startsWith("music_")) {
                    Media media = resourceLoader.loadMusicCached(filePath);
                    if (media != null) {
                        musicTracks.put(key, media);
                        musicTrackNames.add(key);
                        LOGGER.info("Added music track from directory: " + key);
                    }
                } else if (fileName.startsWith("effect_")) {
                    AudioClip clip = resourceLoader.loadSoundEffectCached(filePath);
                    if (clip != null) {
                        soundEffects.put(key, clip);
                        LOGGER.info("Added sound effect from directory: " + key);
                    }
                }
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Failed to load audio file: " + filePath, e);
            }
        }
    }

    /**
     * Lists all resource files in a directory path.
     * Works both in a development environment and in a JAR file.
     *
     * @param directoryPath the path to the directory in resources
     * @return a list of resource file paths
     */
    private List<String> listResourceFiles(String directoryPath) {
        List<String> fileNames = new ArrayList<>();

        try {
            URL directoryUrl = getClass().getResource(directoryPath);
            if (directoryUrl == null) {
                LOGGER.warning("Directory not found in resources: " + directoryPath);
                return fileNames;
            }

            // Use the class loader to get a list of all resources
            URI uri = directoryUrl.toURI();
            LOGGER.info("Resource directory URI: " + uri);

            // Try JAR approach first
            try {
                InputStream is = getClass().getResourceAsStream(directoryPath);
                if (is != null) {
                    try (BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
                        String resource;
                        while ((resource = br.readLine()) != null) {
                            fileNames.add(directoryPath + "/" + resource);
                        }
                    }
                }
            } catch (Exception e) {
                LOGGER.log(Level.INFO, "Could not list JAR resources, trying file system", e);
            }

            // If we couldn't get resources from JAR, try the file system (development environment)
            if (fileNames.isEmpty() && "file".equals(uri.getScheme())) {
                try {
                    Path dirPath = Paths.get(uri);
                    LOGGER.info("Trying to list files from directory: " + dirPath);

                    Files.list(dirPath).forEach(path -> {
                        String relativePath = directoryPath + "/" + path.getFileName().toString();
                        fileNames.add(relativePath);
                    });
                } catch (Exception e) {
                    LOGGER.log(Level.WARNING, "Failed to list files in directory: " + uri, e);
                }
            }

            LOGGER.info("Found resources in directory: " + fileNames);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error listing resources in directory: " + directoryPath, e);
        }

        return fileNames;
    }

    /**
     * Plays a music track by its name.
     *
     * @param name the name of the music track to play
     */
    public void playMusic(String name) {
        if (!audioPlaybackAvailable) {
            LOGGER.info("Audio playback unavailable, skipping playMusic: " + name);
            return;
        }

        LOGGER.info("Request to play music: " + name);

        Media media = musicTracks.get(name);
        if (media == null) {
            LOGGER.warning("Music track not found: " + name + ". Available tracks: " + musicTracks.keySet());
            return;
        }

        try {
            if (currentMusicPlayer != null) {
                LOGGER.info("Crossfading to track: " + name);
                crossfadeTo(media);
            } else {
                LOGGER.info("Starting new music track: " + name);
                currentMusicPlayer = new MediaPlayer(media);

                currentMusicPlayer.setOnError(() -> {
                    LOGGER.severe("MediaPlayer error: " + currentMusicPlayer.getError());
                    if (currentMusicPlayer.getError() != null) {
                        LOGGER.severe("Error details: " + currentMusicPlayer.getError().getMessage());
                        audioPlaybackAvailable = false;
                    }
                });

                currentMusicPlayer.setOnReady(() -> {
                    LOGGER.info("MediaPlayer ready for track: " + name);
                    applyMusicSettings(currentMusicPlayer);
                    currentMusicPlayer.setCycleCount(MediaPlayer.INDEFINITE);
                    try {
                        currentMusicPlayer.play();
                        LOGGER.info("Started playing track: " + name);
                    } catch (Exception e) {
                        LOGGER.log(Level.SEVERE, "Error playing track: " + name, e);
                        audioPlaybackAvailable = false;
                    }
                });

                currentMusicIndex = musicTrackNames.indexOf(name);
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error playing music track: " + name, e);
            audioPlaybackAvailable = false;
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
                        currentMusicPlayer.setVolume(musicVolume * (1 - progress) * (muted ? 0 : 1));
                    }
                    if (nextMusicPlayer != null) {
                        nextMusicPlayer.setVolume(musicVolume * progress * (muted ? 0 : 1));
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
                        currentMusicPlayer.setVolume(musicVolume * (muted ? 0 : 1));
                    });
                }
            }
        }, 0, 50);
    }

    /**
     * Applies audio Settings for music
     *
     * @param player the MediaPlayer to apply settings to
     */
    private void applyMusicSettings(MediaPlayer player) {
        try {
            double effectiveVolume = musicVolume * (muted ? 0 : 1);
            LOGGER.info("Applying settings: volume=" + musicVolume + ", muted=" + muted +
                    ", effective volume=" + effectiveVolume);
            player.setVolume(effectiveVolume);
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error applying music settings", e);
        }
    }

    /**
     * Plays a sound effect by its name.
     * If a previous sound effect is still playing, it will be interrupted.
     *
     * @param name the name of the sound effect to play
     */
    public void playSoundEffect(String name) {
        if (!audioPlaybackAvailable) {
            LOGGER.info("Audio playback unavailable, skipping playSoundEffect: " + name);
            return;
        }

        LOGGER.info("Request to play sound effect: " + name);

        AudioClip clip = soundEffects.get(name);
        if (clip == null) {
            LOGGER.warning("Sound effect not found: " + name + ". Available effects: " + soundEffects.keySet());
            return;
        }

        try {
            clip.stop(); // Interrupt previous if still playing
            clip.setVolume(effectsVolume * (muted ? 0 : 1));
            clip.play();
            LOGGER.info("Playing sound effect: " + name);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error playing sound effect: " + name, e);
            audioPlaybackAvailable = false;
        }
    }

    /**
     * Sets the volume level for all audio playbacks.
     *
     * @param musicVolume the volume level (0.0 to 1.0)
     */
    public void setMusicVolume(double musicVolume) {
        this.musicVolume = musicVolume;
        if (currentMusicPlayer != null) {
            currentMusicPlayer.setVolume(musicVolume * (muted ? 0 : 1));
        }
        // AudioClips get volume set on play
    }

    /**
     * Sets the volume level for sound effects.
     *
     * @param effectsVolume the volume level (0.0 to 1.0)
     */
    public void setEffectsVolume(double effectsVolume) {
        this.effectsVolume = effectsVolume;
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
            currentMusicPlayer.setVolume(musicVolume * (muted ? 0 : 1));
        }
    }

    /**
     * Gets the current volume level.
     *
     * @return the current volume level (0.0 to 1.0)
     */
    public double getMusicVolume() {
        return musicVolume;
    }

    /**
     * Gets the current effects volume level.
     *
     * @return the current effects volume level (0.0 to 1.0)
     */
    public double getEffectsVolume() {
        return effectsVolume;
    }

    /**
     * Gets the current mute state.
     *
     * @return true if audio is muted, false otherwise
     */
    public boolean isMuted() {
        return muted;
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

    /**
     * Checks if audio playback is available in this application instance.
     *
     * @return true if audio playback is available, false otherwise
     */
    public boolean isAudioPlaybackAvailable() {
        return audioPlaybackAvailable;
    }

    /**
     * Recursively attaches click sound to all Button nodes in the given parent node.
     */
    public static void attachClickSoundToAllButtons(Parent parent) {
        for (Node node : parent.getChildrenUnmodifiable()) {
            if (node instanceof Button) {
                AudioManager.attachClickSound((Button) node);
            } else if (node instanceof Parent) {
                attachClickSoundToAllButtons((Parent) node);
            }
        }
    }

    /**
     * Static utility to attach a click sound effect to a button.
     */
    public static void attachClickSound(Button button) {
        if (button != null) {
            button.addEventHandler(javafx.event.ActionEvent.ACTION, event -> {
                AudioManager.getInstance().playSoundEffect(AudioTracks.Track.BUTTON_CLICK.getFileName());
            });
        }
    }
}
