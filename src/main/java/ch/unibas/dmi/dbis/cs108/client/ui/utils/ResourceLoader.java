package ch.unibas.dmi.dbis.cs108.client.ui.utils;

import ch.unibas.dmi.dbis.cs108.shared.entities.EntityRegistry;
import javafx.application.Platform;
import javafx.scene.image.Image;
import javafx.scene.media.AudioClip;
import javafx.scene.media.Media;

import java.net.URL;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Utility for loading resources such as FXML, images, CSS, fonts, and audio.
 */
public class ResourceLoader {
    /**
     * FXML Paths
     */
    public static final String SPLASH_SCREEN_FXML = "/fxml/splash_screen.fxml";
    /*
    * path to the main menu fxml file
     */
    public static final String MAIN_MENU_FXML = "/fxml/main_menu.fxml";
    /*
    * path to the settings dialog fxml file
     */
    public static final String LOBBY_SCREEN_FXML = "/fxml/lobby_screen.fxml";
    /*
    * path to the settings dialog fxml file
     */
    public static final String GAME_SCREEN_FXML = "/fxml/game_screen.fxml";
    /*
    * path to the settings dialog fxml file
     */
    public static final String MAP_IMAGE = "/images/map.png";
    /*
    * path to the settings dialog fxml file
     */
    public static final String GAME_LOGO = "/images/game-logo.png";
    /*
    * path to the settings dialog fxml file
     */
    // CSS Paths
    /*
    * path to the main menu css file
     */
    public static final String VARIABLES_CSS = "/css/variables.css";
    /*
    * path to the main menu css file
     */
    public static final String COMMON_CSS = "/css/common.css";
    /*
    * path to the main menu css file
     */
    public static final String DEFAULT_THEME_CSS = "/css/main-menu.css";
    /*
    * path to the main menu css file
     */
    public static final String DARK_THEME_CSS = "/css/dark-theme.css";
    /*
    * path to the main menu css file
     */
    public static final String CINZEL_REGULAR = "/fonts/Cinzel/static/Cinzel-Regular.ttf";
    /*
    * path to the main menu css file
     */
    public static final String ROBOTO_REGULAR = "/fonts/Roboto/static/Roboto-Regular.ttf";
    /*
    * path to the main menu css file
     */
    public static final String DIALOG_COMMON_CSS = "/css/dialog-common.css";
    /*
    * path to the main menu css file
     */
    public static final String MAIN_MENU_CSS = "/css/main-menu.css";
    /*
    * path to the main menu css file
     */
    public static final String LOBBY_SCREEN_CSS = "/css/lobby-screen.css";
    /*
    * path to the main menu css file
     */
    public static final String GAME_SCREEN_CSS = "/css/game-screen.css";
    /*
    * path to the main menu css file
     */
    public static final String CHAT_COMPONENT_CSS = "/css/chat-component.css";

    /**
     * path to the main menu css file
     */
    public static final String SETTINGS_DIALOG_CSS = "/css/settings-dialog.css";
    /*
    * path to the main menu css file
     */
    public static final String ABOUT_DIALOG_CSS = "/css/about-dialog.css";
    /*
    * path to the main menu css file
     */
    public static final String DESCRIPTION_DIALOG_CSS = "/css/description-dialog.css";
    /*
    * path to the main menu css file
     */
    private static final Logger LOGGER = Logger.getLogger(ResourceLoader.class.getName());
    /*
        * Cache for images, music, and sound effects.
     */
    private final Map<String, Image> entityImageCache = new ConcurrentHashMap<>();
    /*
        * Cache for images, music, and sound effects.
     */
    private final Map<Integer, Image> cardImageCache = new ConcurrentHashMap<>();
    /*
        * Cache for images, music, and sound effects.
     */
    private final Map<String, Media> musicCache = new ConcurrentHashMap<>();
    /*
        * Cache for images, music, and sound effects.
     */
    private final Map<String, AudioClip> soundEffectCache = new ConcurrentHashMap<>();

    // Private constructor to prevent instantiation
    public ResourceLoader() {}

    /**
     * Loads an image from the given resource path.
     *
     * @param path Resource path
     * @return Image or null if not found
     */
    public Image loadImage(String path) {
        if (path == null || path.isEmpty()) {
            LOGGER.warning("Empty image path provided");
            return null;
        }

        try {
            String correctedPath = correctResourcePath(path);

            LOGGER.info("Loading image from: " + correctedPath);

            // First approach: direct loading via getResourceAsStream
            URL resourceUrl = getClass().getResource(correctedPath);
            if (resourceUrl != null) {
                return new Image(resourceUrl.toExternalForm());
            }

            // Second approach: fallback to input stream
            var inputStream = getClass().getResourceAsStream(correctedPath);
            if (inputStream != null) {
                Image image = new Image(inputStream);
                inputStream.close();
                return image;
            }

            // Third approach: try without slash
            if (correctedPath.startsWith("/")) {
                String pathWithoutSlash = correctedPath.substring(1);
                inputStream = getClass().getResourceAsStream(pathWithoutSlash);
                if (inputStream != null) {
                    Image image = new Image(inputStream);
                    inputStream.close();
                    return image;
                }
            }

            LOGGER.warning("Image not found after all attempts: " + path);
            return null;
        } catch (Exception e) {
            LOGGER.warning("Error loading image: " + path + " (" + e.getMessage() + ")");
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Loads a card image based on the entity ID.
     *
     * @param entityId The ID of the entity
     * @return Image object or null if not found
     */
    public Image getCardImage(int entityId) {
        return cardImageCache.computeIfAbsent(entityId, id -> {
            String url = EntityRegistry.getURL(id, true);
            return loadImage(url);
        });
    }

    /**
     * Loads a music track from the given resource path.
     *
     * @param path Resource path (e.g., "/sounds/music_mainMenu.mp3")
     * @return Media object or null if not found
     */
    public Media loadMusic(String path) {
        if (path == null || path.isEmpty()) {
            LOGGER.warning("Empty music path provided");
            return null;
        }

        try {
            String correctedPath = correctResourcePath(path);
            LOGGER.info("Loading music from: " + correctedPath);

            URL resourceUrl = getClass().getResource(correctedPath);
            if (resourceUrl != null) {
                try {
                    return new Media(resourceUrl.toExternalForm());
                } catch (Exception e) {
                    // Check if this is a file access exception (common when multiple clients run)
                    if (e.getCause() != null && e.getCause().getMessage() != null &&
                        e.getCause().getMessage().contains("FileAlreadyExistsException")) {
                        LOGGER.info("Media file access conflict - this is normal when running multiple clients");
                        // Still return the Media object - it might work despite the error
                        return new Media(resourceUrl.toExternalForm());
                    }
                    throw e;
                }
            }

            LOGGER.warning("Music file not found: " + path);
            return null;
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error loading music: " + path, e);
            return null;
        }
    }

    /**
     * Loads a sound effect from the given resource path.
     *
     * @param path Resource path (e.g., "/sounds/effect_click.mp3")
     * @return AudioClip object or null if not found
     */
    public AudioClip loadSoundEffect(String path) {
        if (path == null || path.isEmpty()) {
            LOGGER.warning("Empty sound effect path provided");
            return null;
        }

        try {
            String correctedPath = correctResourcePath(path);
            LOGGER.info("Loading sound effect from: " + correctedPath);

            URL resourceUrl = getClass().getResource(correctedPath);
            if (resourceUrl != null) {
                return new AudioClip(resourceUrl.toExternalForm());
            }

            LOGGER.warning("Sound effect not found: " + path);
            return null;
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error loading sound effect: " + path, e);
            return null;
        }
    }

    /**
     * Loads a music track and caches it for future use.
     *
     * @param path Resource path
     * @return Media object or null if not found
     */
    public Media loadMusicCached(String path) {
        return musicCache.computeIfAbsent(path, this::loadMusic);
    }

    /**
     * Loads a sound effect and caches it for future use.
     *
     * @param path Resource path
     * @return AudioClip object or null if not found
     */
    public AudioClip loadSoundEffectCached(String path) {
        return soundEffectCache.computeIfAbsent(path, this::loadSoundEffect);
    }

    /**
     * Corrects a resource path to ensure it starts with a single slash.
     *
     * @param path The resource path to correct
     * @return The corrected path
     */
    private String correctResourcePath(String path) {
        String correctedPath = path;

        // Remove "resources/" prefix if it exists
        if (correctedPath.startsWith("resources/")) {
            correctedPath = correctedPath.substring("resources/".length());
        }

        // Ensure path starts with a single slash
        if (!correctedPath.startsWith("/")) {
            correctedPath = "/" + correctedPath;
        } else if (correctedPath.startsWith("//")) {
            correctedPath = correctedPath.substring(1); // Remove one of the slashes
        }

        return correctedPath;
    }

    /**
     * Low-level helper that never blocks the FX thread.
     */
    private Image createImage(String url, boolean background) {
        return new Image(url, background);
    }

    /**
     * Load an image *synchronously* (blocking) and cache it.
     * Use this for the background map or other “must-have before paint” assets.
     *
     * @param url The URL of the image to load
     *            @return The loaded Image object
     */
    public Image loadImageSync(String url) {
        return entityImageCache.computeIfAbsent(url, u -> createImage(u, /*background*/ false));
    }

    /**
     * Load an image in the background (non-blocking) and cache it.
     * If you pass an onReady-callback, it is invoked on the **FX thread**
     * exactly once when the image is completely decoded.
     *
     * @param url The URL of the image to load
     *            @param onReady A callback to be executed when the image is ready (can be null)
     *
     *            @return The loaded Image object
     */
    public Image loadImageAsync(String url, Runnable onReady /* nullable */) {

        Image img = entityImageCache.computeIfAbsent(url, u -> createImage(u, /*background*/ true));

        if (onReady != null && img.getProgress() < 1.0) {
            img.progressProperty().addListener((o, ov, nv) -> {
                if (nv.doubleValue() >= 1.0) {
                    Platform.runLater(onReady);
                }
            });
        }
        return img;
    }
}
