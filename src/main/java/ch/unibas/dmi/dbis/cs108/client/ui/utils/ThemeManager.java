package ch.unibas.dmi.dbis.cs108.client.ui.utils;

import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.text.Font;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;

/**
 * Singleton manager for application themes (CSS stylesheets).
 * Handles registering scenes and applying the current theme to all.
 */
public class ThemeManager {
    /**
     * Logger for ThemeManager.
     */
    private static final Logger LOGGER = Logger.getLogger(ThemeManager.class.getName());
    /**
     * Singleton instance of ThemeManager.
     */
    private static volatile ThemeManager instance;

    /**
     * List of registered scenes for theme management.
     */
    private final List<Scene> registeredScenes = new ArrayList<>();
    /**
     * Current theme CSS resource path.
     */
    private String currentTheme = ResourceLoader.DEFAULT_THEME_CSS;

    /**
     * Private constructor to prevent instantiation.
     * Loads default fonts.
     */
    private ThemeManager() {
        loadFont(ResourceLoader.CINZEL_REGULAR);
        loadFont(ResourceLoader.ROBOTO_REGULAR);
    }

    /**
     * @return singleton instance of ThemeManager
     */
    public static ThemeManager getInstance() {
        if (instance == null) {
            synchronized (ThemeManager.class) {
                if (instance == null) {
                    instance = new ThemeManager();
                }
            }
        }
        return instance;
    }

    /**
     * Register a scene for theme management.
     *
     * @param scene Scene to register
     */
    public void registerScene(Scene scene) {
        Objects.requireNonNull(scene);
        if (!registeredScenes.contains(scene)) {
            registeredScenes.add(scene);
            applyThemeToScene(scene);
        }
    }

    /**
     * Set the current theme and apply to all registered scenes.
     *
     * @param themePath CSS resource path
     */
    public void setTheme(String themePath) {
        Objects.requireNonNull(themePath);
        this.currentTheme = themePath;
        applyThemeToAllScenes();
    }

    /**
     * Apply the current theme to all registered scenes.
     */
    private void applyThemeToAllScenes() {
        for (Scene scene : registeredScenes) {
            applyThemeToScene(scene);
        }
    }

    /**
     * Apply the current theme to a specific scene.
     *
     * @param scene Scene to apply theme to
     */
    public void applyThemeToScene(Scene scene) {
        Objects.requireNonNull(scene);
        Platform.runLater(() -> {
            scene.getStylesheets().clear();

            // Use StylesheetLoader to load base styles and theme
            scene.getStylesheets().clear();
            StylesheetLoader.loadStylesheet(scene.getRoot(), ResourceLoader.VARIABLES_CSS);
            StylesheetLoader.loadStylesheet(scene.getRoot(), ResourceLoader.COMMON_CSS);
            StylesheetLoader.loadStylesheet(scene.getRoot(), currentTheme);
        });
    }

    /**
     * Loads a font from the given resource path.
     *
     * @param fontPath Font resource path
     */
    private void loadFont(String fontPath) {
        try {
            URL url = getClass().getResource(fontPath);
            if (url != null) {
                Font.loadFont(url.toExternalForm(), 10);
            }
        } catch (Exception e) {
            LOGGER.warning("Failed to load font: " + fontPath);
        }
    }
}
