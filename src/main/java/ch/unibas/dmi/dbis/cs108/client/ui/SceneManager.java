package ch.unibas.dmi.dbis.cs108.client.ui;

import ch.unibas.dmi.dbis.cs108.client.ui.utils.ResourceLoader;
import ch.unibas.dmi.dbis.cs108.client.ui.utils.ThemeManager;
import javafx.animation.FadeTransition;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

public class SceneManager {
    private static final Logger LOGGER = Logger.getLogger(SceneManager.class.getName());
    private static volatile SceneManager instance;
    private final Map<SceneType, SceneHolder> sceneCache = new ConcurrentHashMap<>();
    private final ResourceLoader resourceLoader;
    private Stage primaryStage;

    // Using double-checked locking for thread-safe lazy initialization.
    private SceneManager(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    public static SceneManager getInstance() {
        if (instance == null) {
            synchronized (SceneManager.class) {
                if (instance == null) {
                    instance = new SceneManager(new ResourceLoader());
                }
            }
        }
        return instance;
    }

    public void setPrimaryStage(Stage stage) {
        this.primaryStage = stage;
    }

    public void switchToScene(SceneType sceneType) {
        SceneHolder holder = sceneCache.computeIfAbsent(sceneType, this::loadScene);
        Parent newRoot = holder.getScene().getRoot(); // The root is loaded within loadScene

        // Apply fade transition to the new root
        FadeTransition fadeTransition = new FadeTransition(Duration.millis(500), newRoot);
        fadeTransition.setFromValue(0.0);
        fadeTransition.setToValue(1.0);
        fadeTransition.play();

        Scene currentScene = primaryStage.getScene();
        if (currentScene == null) {
            // First time setting a scene
            primaryStage.setScene(holder.getScene());
            LOGGER.info("Set initial scene: " + sceneType);
            // Automatically register the scene for theme management.
            ThemeManager.getInstance().registerScene(holder.getScene());
        } else {
            // Reuse existing scene, just change the root
            currentScene.setRoot(newRoot);
            LOGGER.info("Switched scene root to: " + sceneType);
            // ThemeManager should already be aware of the scene, no need to re-register
        }
    }

    public SceneHolder loadScene(SceneType sceneType) {
        URL fxmlUrl = getClass().getResource(sceneType.getPath());
        if (fxmlUrl == null) {
            throw new RuntimeException("FXML resource not found: " + sceneType.getPath());
        }
        try {
            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent root = loader.load();
            Scene scene = new Scene(root);
            return new SceneHolder(scene, loader.getController());
        } catch (IOException e) {
            throw new RuntimeException("Failed to load scene: " + sceneType.getPath(), e);
        }
    }

    public <T> T getController(SceneType sceneType) {
        SceneHolder holder = sceneCache.get(sceneType);
        if (holder == null) {
            switchToScene(sceneType);
            holder = sceneCache.get(sceneType);
        }
        return (T) holder.getController();
    }

    public enum SceneType {
        MAIN_MENU(ResourceLoader.MAIN_MENU_FXML),
        LOBBY(ResourceLoader.LOBBY_SCREEN_FXML),
        GAME(ResourceLoader.GAME_SCREEN_FXML);

        private final String fxmlPath;

        SceneType(String fxmlPath) {
            this.fxmlPath = fxmlPath;
        }

        public String getPath() {
            return fxmlPath;
        }
    }

    private static class SceneHolder {
        private final Scene scene;
        private final Object controller;

        public SceneHolder(Scene scene, Object controller) {
            this.scene = scene;
            this.controller = controller;
        }

        public Scene getScene() {
            return scene;
        }

        public Object getController() {
            return controller;
        }
    }
}