package ch.unibas.dmi.dbis.cs108.client.audio;

import ch.unibas.dmi.dbis.cs108.client.ui.SceneManager;

import java.util.*;
import java.util.logging.Logger;

/**
 * Manages music playback for different game scenes.
 * Maps scenes to collections of music tracks and handles selection logic.
 */
public class MusicManager {
    private static final Logger LOGGER = Logger.getLogger(MusicManager.class.getName());
    private static MusicManager instance;
    
    // Map each scene type to a list of possible music tracks
    private final Map<SceneManager.SceneType, List<AudioTracks.Track>> sceneMusicMap;
    private final Random random = new Random();
    
    // Keep track of the currently playing track for each scene
    private final Map<SceneManager.SceneType, Integer> currentTrackIndices = new HashMap<>();
    
    // Singleton constructor
    private MusicManager() {
        sceneMusicMap = new EnumMap<>(SceneManager.SceneType.class);
        initializeMusicMap();
    }
    
    /**
     * Gets the singleton instance of MusicManager.S
     *
     * @return The MusicManager instance
     */
    public static synchronized MusicManager getInstance() {
        if (instance == null) {
            instance = new MusicManager();
        }
        return instance;
    }
    
    /**
     * Initialize the mapping between scenes and their available music tracks.
     */
    private void initializeMusicMap() {
        // Main menu can play both main menu tracks
        sceneMusicMap.put(SceneManager.SceneType.MAIN_MENU, Arrays.asList(
            AudioTracks.Track.MAIN_MENU_CHOIR
        ));
        
        // Lobby scene has its dedicated epic track
        sceneMusicMap.put(SceneManager.SceneType.LOBBY, Arrays.asList(
            AudioTracks.Track.LOBBY_SCREEN_EPIC
        ));
        
        // Game scene can alternate between primary and secondary tracks
        sceneMusicMap.put(SceneManager.SceneType.GAME, Arrays.asList(
            AudioTracks.Track.GAME_BACKGROUND_PRIMARY,
            AudioTracks.Track.GAME_BACKGROUND_SECONDARY
        ));
        
        // Splash screen: no music, so do not add SPLASH to the map
    }
    
    /**
     * Changes the music based on the scene type.
     * 
     * @param sceneType The scene type to play music for
     * @param selectionMode How to select from available tracks (random or sequential)
     */
    public void changeMusic(SceneManager.SceneType sceneType, SelectionMode selectionMode) {
        List<AudioTracks.Track> tracks = sceneMusicMap.get(sceneType);
        
        if (tracks == null || tracks.isEmpty()) {
            LOGGER.warning("No music tracks defined for scene type: " + sceneType);
            return;
        }
        
        AudioTracks.Track selectedTrack;
        
        // Select a track based on the selection mode
        if (selectionMode == SelectionMode.RANDOM) {
            selectedTrack = getRandomTrack(sceneType);
        } else {
            selectedTrack = getNextTrack(sceneType);
        }
        
        // Play the selected track
        AudioManager.getInstance().playMusic(selectedTrack.getFileName(), sceneType, selectionMode);
        LOGGER.info("Changed music to " + selectedTrack.name() + " for scene: " + sceneType);
    }
    
    /**
     * Gets a random track for the specified scene type.
     * 
     * @param sceneType The scene type
     * @return A randomly selected AudioTracks.Track
     */
    public AudioTracks.Track getRandomTrack(SceneManager.SceneType sceneType) {
        List<AudioTracks.Track> tracks = sceneMusicMap.get(sceneType);
        if (tracks == null || tracks.isEmpty()) {
            LOGGER.warning("No tracks available for scene: " + sceneType);
            return null;
        }
        
        int index = random.nextInt(tracks.size());
        return tracks.get(index);
    }
    
    /**
     * Gets the next track in sequence for the specified scene type.
     * 
     * @param sceneType The scene type
     * @return The next AudioTracks.Track in sequence
     */
    public AudioTracks.Track getNextTrack(SceneManager.SceneType sceneType) {
        List<AudioTracks.Track> tracks = sceneMusicMap.get(sceneType);
        if (tracks == null || tracks.isEmpty()) {
            LOGGER.warning("No tracks available for scene: " + sceneType);
            return null;
        }
        
        // Get the current index for this scene type, defaulting to -1
        int currentIndex = currentTrackIndices.getOrDefault(sceneType, -1);
        
        // Move to the next track, cycling back to the beginning if needed
        int nextIndex = (currentIndex + 1) % tracks.size();
        currentTrackIndices.put(sceneType, nextIndex);
        
        return tracks.get(nextIndex);
    }
    
    /**
     * Add a track to a scene's playlist.
     * 
     * @param sceneType The scene type to add a track for
     * @param track The track to add
     */
    public void addTrackToScene(SceneManager.SceneType sceneType, AudioTracks.Track track) {
        List<AudioTracks.Track> tracks = sceneMusicMap.computeIfAbsent(
            sceneType, k -> new ArrayList<>());
        if (!tracks.contains(track)) {
            tracks.add(track);
        }
    }
    
    /**
     * Remove a track from a scene's playlist.
     * 
     * @param sceneType The scene type to remove a track from
     * @param track The track to remove
     * @return true if the track was removed, false otherwise
     */
    public boolean removeTrackFromScene(SceneManager.SceneType sceneType, AudioTracks.Track track) {
        List<AudioTracks.Track> tracks = sceneMusicMap.get(sceneType);
        if (tracks != null) {
            return tracks.remove(track);
        }
        return false;
    }
    
    /**
     * Set the complete list of tracks for a scene.
     * 
     * @param sceneType The scene type
     * @param tracks The list of tracks to set
     */
    public void setTracksForScene(SceneManager.SceneType sceneType, List<AudioTracks.Track> tracks) {
        sceneMusicMap.put(sceneType, new ArrayList<>(tracks));
    }
    
    /**
     * Defines how tracks should be selected from a scene's playlist.
     */
    public enum SelectionMode {
        SEQUENTIAL,
        RANDOM
    }
}
