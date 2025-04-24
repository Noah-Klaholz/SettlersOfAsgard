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
        if (path == null || path.isBlank())
            return null;
        try {
            URL url = getClass().getResource(path);
            if (url == null) {
                LOGGER.warning("Image not found: " + path);
                return null;
            }
            return new Image(url.openStream());
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error loading image: " + path, e);
            return null;
        }
    }
}
