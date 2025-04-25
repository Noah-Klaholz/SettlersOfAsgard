package ch.unibas.dmi.dbis.cs108.client.ui.utils;

import javafx.scene.image.Image;

import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Utility for loading resources such as FXML, images, CSS, and fonts.
 */
public class ResourceLoader {
    private static final Logger LOGGER = Logger.getLogger(ResourceLoader.class.getName());

    public static final String SPLASH_SCREEN_FXML = "/fxml/splash_screen.fxml";
    public static final String MAIN_MENU_FXML = "/fxml/main_menu.fxml";
    public static final String LOBBY_SCREEN_FXML = "/fxml/lobby_screen.fxml";
    public static final String GAME_SCREEN_FXML = "/fxml/game_screen.fxml";
    public static final String MAP_IMAGE = "/images/map.png";
    public static final String GAME_LOGO = "/images/game-logo.png";
    // CSS Paths
    public static final String VARIABLES_CSS = "/css/variables.css";
    public static final String COMMON_CSS = "/css/common.css";
    public static final String DEFAULT_THEME_CSS = "/css/main-menu.css";
    public static final String DARK_THEME_CSS = "/css/dark-theme.css";
    public static final String CINZEL_REGULAR = "/fonts/Cinzel/static/Cinzel-Regular.ttf";
    public static final String ROBOTO_REGULAR = "/fonts/Roboto/static/Roboto-Regular.ttf";
    public static final String DIALOG_COMMON_CSS = "/css/dialog-common.css";
    public static final String MAIN_MENU_CSS = "/css/main-menu.css";
    public static final String LOBBY_SCREEN_CSS = "/css/lobby-screen.css";
    public static final String GAME_SCREEN_CSS = "/css/game-screen.css";
    public static final String CHAT_COMPONENT_CSS = "/css/chat-component.css";
    public static final String SETTINGS_DIALOG_CSS = "/css/settings-dialog.css";
    public static final String ABOUT_DIALOG_CSS = "/css/about-dialog.css";
    public static final String DESCRIPTION_DIALOG_CSS = "/css/description-dialog.css";

    // Private constructor to prevent instantiation
    public ResourceLoader() {
    }

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
            // Remove "resources/" prefix if it exists
            String correctedPath = path;
            if (correctedPath.startsWith("resources/")) {
                correctedPath = correctedPath.substring("resources/".length());
            }

            // Ensure path starts with a single slash
            if (!correctedPath.startsWith("/")) {
                correctedPath = "/" + correctedPath;
            } else if (correctedPath.startsWith("//")) {
                correctedPath = correctedPath.substring(1); // Remove one of the slashes
            }

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
}
