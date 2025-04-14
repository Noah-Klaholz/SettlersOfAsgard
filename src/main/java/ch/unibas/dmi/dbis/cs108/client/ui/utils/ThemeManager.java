package ch.unibas.dmi.dbis.cs108.client.ui.utils;

import javafx.scene.Scene;
import javafx.scene.text.Font;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class ThemeManager {
    private static ThemeManager instance;
    private final List<Scene> registeredScenes = new ArrayList<>();
    //    private final List<String> commonStylesheets = List.of("/css/common.css");
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
    private void applyThemeToScene(Scene scene) {
        scene.getStylesheets().clear();

        // Apply the theme stylesheet
        URL themeUrl = getClass().getResource(currentTheme.getPath());
        if (themeUrl != null) {
            scene.getStylesheets().add(themeUrl.toExternalForm());
        } else {
            System.err.println("Theme stylesheet " + currentTheme.getPath() + " not found.");
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

            System.out.println("Font loading status:");
            System.out.println("- Cinzel: " + (cinzelLoaded ? "Loaded" : "Failed") +
                    ", In system: " + cinzelInSystem);
            System.out.println("- Roboto: " + (robotoLoaded ? "Loaded" : "Failed") +
                    ", In system: " + robotoInSystem);

            if (cinzelLoaded && robotoLoaded) {
                System.out.println("Fonts loaded successfully");
            } else {
                System.err.println("Some fonts failed to load");
            }
        } catch (Exception e) {
            System.err.println("Error loading fonts: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public enum Theme {
        DEFAULT("/css/game-screen.css");

        private final String cssPath;

        Theme(String path) {
            this.cssPath = path;
        }

        public String getPath() {
            return cssPath;
        }
    }
}