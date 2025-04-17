package ch.unibas.dmi.dbis.cs108.client.ui.utils;

import javafx.scene.image.Image;

import java.util.logging.Logger;

public class ResourceLoader {
    private static final Logger LOGGER = Logger.getLogger(ResourceLoader.class.getName());
    // Centralized FXML paths
    public static final String MAIN_MENU_FXML = "/fxml/main_menu.fxml";
    public static final String LOBBY_SCREEN_FXML = "/fxml/lobby_screen.fxml";
    public static final String GAME_SCREEN_FXML = "/fxml/game_screen.fxml";

    // Images
    public static final String MAP_IMAGE = "/images/map.png";

    // CSS files
    public static final String COMMON_CSS = "/css/game-screen.css";
    public static final String DEFAULT_THEME_CSS = "/css/lobby-screen.css";
    public static final String DARK_THEME_CSS = "/css/main-menu.css";

    // Fontes
    public static final String CINZEL_REGULAR = "/fonts/Cinzel/static/Cinzel-Regular.ttf";
    public static final String ROBOTO_REGULAR = "/fonts/Roboto/static/Roboto-Regular.ttf";

    public Image loadImage(String path) {
        try {
            var resource = getClass().getResource(path);
            if (resource == null) {
                LOGGER.warning("Could not find image resource: " + path);
                return null;
            }
            return new Image(resource.toExternalForm());
        } catch (Exception e) {
            LOGGER.warning("Error loading image: " + path + " - " + e.getMessage());
            return null;
        }
    }
}