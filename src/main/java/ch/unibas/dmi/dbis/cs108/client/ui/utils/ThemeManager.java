package ch.unibas.dmi.dbis.cs108.client.ui.utils;

import javafx.scene.Scene;
import javafx.scene.text.Font;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ThemeManager {
    private static final Logger LOGGER = Logger.getLogger(ThemeManager.class.getName());
    private static ThemeManager instance;
    private final List<Scene> registeredScenes = new ArrayList<>();
    private Theme currentTheme = Theme.DEFAULT;

    private ThemeManager() {
        loadFonts();
    }

    public static ThemeManager getInstance() {
        if (instance == null) {
            instance = new ThemeManager();
        }
        return instance;
    }

    public void registerScene(Scene scene) {
        if (!registeredScenes.contains(scene)) {
            registeredScenes.add(scene);
            applyThemeToScene(scene);
        }
    }

    public void setTheme(Theme theme) {
        if (theme != currentTheme) {
            currentTheme = theme;
            applyThemeToAllScenes();
        }
    }

    private void applyThemeToAllScenes() {
        for (Scene scene : registeredScenes) {
            applyThemeToScene(scene);
        }
    }

    // File: src/main/java/ch/unibas/dmi/dbis/cs108/client/ui/utils/ThemeManager.java
    // Changed from private to public
    public void applyThemeToScene(Scene scene) {
        scene.getStylesheets().clear();

        // Apply the theme stylesheet
        URL themeUrl = getClass().getResource(currentTheme.getPath());
        if (themeUrl != null) {
            try {
                scene.getStylesheets().add(themeUrl.toExternalForm());
                LOGGER.info("Applied theme " + currentTheme + " to scene: " + themeUrl.toExternalForm());
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Error applying theme stylesheet " + currentTheme.getPath() + ": " + e.getMessage(), e);
            }
        } else {
            LOGGER.warning("Theme stylesheet resource not found: " + currentTheme.getPath());
        }
    }

    private void loadFonts() {
        boolean cinzelLoaded = false;
        boolean robotoLoaded = false;

        try {
            // Load Cinzel font and capture return value
            Font cinzelFont = Font.loadFont(
                    getClass().getResourceAsStream(ResourceLoader.CINZEL_REGULAR),
                    12
            );
            cinzelLoaded = cinzelFont != null;

            // Load Roboto font and capture return value
            Font robotoFont = Font.loadFont(
                    getClass().getResourceAsStream(ResourceLoader.ROBOTO_REGULAR),
                    12
            );
            robotoLoaded = robotoFont != null;

            // Verify the fonts are in the system
            boolean cinzelInSystem = Font.getFamilies().contains("Cinzel");
            boolean robotoInSystem = Font.getFamilies().contains("Roboto");

            LOGGER.info("Font loading status:");
            LOGGER.info("- Cinzel: " + (cinzelLoaded ? "Loaded" : "Failed") +
                    ", In system: " + cinzelInSystem);
            LOGGER.info("- Roboto: " + (robotoLoaded ? "Loaded" : "Failed") +
                    ", In system: " + robotoInSystem);

            if (cinzelLoaded && robotoLoaded) {
                LOGGER.info("Fonts loaded successfully");
            } else {
                LOGGER.warning("Some fonts failed to load");
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error loading fonts: " + e.getMessage(), e);
        }
    }

    public enum Theme {
        // Use paths from ResourceLoader for consistency
        DEFAULT(ResourceLoader.DEFAULT_THEME_CSS), // Assuming lobby-screen is default
        DARK(ResourceLoader.DARK_THEME_CSS); // Assuming main-menu is dark
        // Consider adding GAME(ResourceLoader.COMMON_CSS) if needed

        private final String cssPath;

        Theme(String path) {
            this.cssPath = path;
        }

        public String getPath() {
            return cssPath;
        }
    }
}
