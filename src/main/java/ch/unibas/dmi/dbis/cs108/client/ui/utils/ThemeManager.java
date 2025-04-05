package ch.unibas.dmi.dbis.cs108.client.ui.utils;

import javafx.scene.Scene;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class ThemeManager {
    private static ThemeManager instance;
    private final List<Scene> registeredScenes = new ArrayList<>();
    //    private final List<String> commonStylesheets = List.of("/css/common.css");
    private Theme currentTheme = Theme.DEFAULT;

    private ThemeManager() {
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

    public enum Theme {
        DEFAULT("/css/style.css");

        private final String cssPath;

        Theme(String path) {
            this.cssPath = path;
        }

        public String getPath() {
            return cssPath;
        }
    }
}