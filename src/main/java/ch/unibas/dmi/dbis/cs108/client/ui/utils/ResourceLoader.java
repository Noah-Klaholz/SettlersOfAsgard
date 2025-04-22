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
    public static final String COMMON_CSS = "/css/common.css";
    public static final String DEFAULT_THEME_CSS = "/css/default-theme.css";
    public static final String DARK_THEME_CSS = "/css/dark-theme.css";
    public static final String CINZEL_REGULAR = "/fonts/Cinzel/static/Cinzel-Regular.ttf";
    public static final String ROBOTO_REGULAR = "/fonts/Roboto/static/Roboto-Regular.ttf";

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
