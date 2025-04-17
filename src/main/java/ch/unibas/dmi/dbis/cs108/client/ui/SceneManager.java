package ch.unibas.dmi.dbis.cs108.client.ui;

import ch.unibas.dmi.dbis.cs108.client.ui.utils.ResourceLoader;
import ch.unibas.dmi.dbis.cs108.client.ui.utils.ThemeManager;
import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.SequentialTransition;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
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
    // Cache now stores Parent nodes and controllers, not Scenes
    private final Map<SceneType, NodeHolder> nodeCache = new ConcurrentHashMap<>();
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
        // Load the Parent node and controller from FXML or retrieve from cache
        NodeHolder holder = nodeCache.computeIfAbsent(sceneType, this::loadNodeAndController);
        Parent newRootNode = holder.getNode(); // Get the newly loaded or cached root node

        Scene currentScene = primaryStage.getScene();
        if (currentScene == null) {
            // First time setup: create a new scene with the loaded root
            currentScene = new Scene(newRootNode, Color.rgb(26, 33, 51)); // Use dark background
            primaryStage.setScene(currentScene);
            LOGGER.info("Initial scene created and set to: " + sceneType);
            // Register the newly created scene for theme management
            ThemeManager.getInstance().registerScene(currentScene);
            
            // Add simple fade-in for initial scene (reduced from 300ms to 150ms)
            FadeTransition fadeIn = createFadeTransition(newRootNode, 0.0, 1.0, 150);
            fadeIn.setInterpolator(Interpolator.EASE_OUT);
            fadeIn.play();
        } else {
            // Get current root before switching
            Parent currentRoot = currentScene.getRoot();
            
            // Create fade-out transition for current scene (reduced from 220ms to 100ms)
            FadeTransition fadeOut = createFadeTransition(currentRoot, 1.0, 0.2, 100);
            fadeOut.setInterpolator(Interpolator.EASE_IN);
            
            // Prepare fade-in for new scene (reduced from 280ms to 150ms)
            newRootNode.setOpacity(0.0); // Start completely transparent
            FadeTransition fadeIn = createFadeTransition(newRootNode, 0.0, 1.0, 150);
            fadeIn.setInterpolator(Interpolator.EASE_OUT);
            
            // Capture the current scene in a final variable for use in lambda
            final Scene finalCurrentScene = currentScene;
            
            // Set up sequential transition: fade out current, then switch roots, then fade in new
            fadeOut.setOnFinished(event -> {
                // After fade out completes, switch the root and apply theme
                finalCurrentScene.setRoot(newRootNode);
                ThemeManager.getInstance().applyThemeToScene(finalCurrentScene);
                LOGGER.info("Switched scene root to: " + sceneType);
                
                // Now play the fade in
                fadeIn.play();
            });
            
            // Start the fade out sequence
            fadeOut.play();
        }
    }

    private FadeTransition createFadeTransition(Parent node, double fromValue, double toValue, int durationMs) {
        FadeTransition fadeTransition = new FadeTransition(Duration.millis(durationMs), node);
        fadeTransition.setFromValue(fromValue);
        fadeTransition.setToValue(toValue);
        return fadeTransition;
    }

    // Loads only the Parent node and controller, does not create a Scene here.
    private NodeHolder loadNodeAndController(SceneType sceneType) {
        URL fxmlUrl = getClass().getResource(sceneType.getPath());
        if (fxmlUrl == null) {
            throw new RuntimeException("FXML resource not found: " + sceneType.getPath());
        }
        try {
            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent root = loader.load();
            LOGGER.info("Loaded FXML node and controller for: " + sceneType);
            return new NodeHolder(root, loader.getController());
        } catch (IOException e) {
            throw new RuntimeException("Failed to load FXML for scene: " + sceneType.getPath(), e);
        }
    }

    public <T> T getController(SceneType sceneType) {
        NodeHolder holder = nodeCache.get(sceneType);
        if (holder == null) {
            // If controller is requested for a node not yet loaded,
            // load it first. This might happen if controller logic is needed before display.
            LOGGER.warning("Controller requested for node not yet loaded: " + sceneType + ". Loading node and controller now.");
            holder = nodeCache.computeIfAbsent(sceneType, this::loadNodeAndController);
            // Note: This doesn't display the scene, just loads the node/controller into the cache.
        }
        // It's possible the holder is still null if loading failed, though loadNodeAndController throws RuntimeException
        if (holder == null) {
             throw new IllegalStateException("Failed to load or retrieve node holder for: " + sceneType);
        }
        // Unchecked cast, assumes the caller knows the correct controller type.
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

    // Renamed SceneHolder to NodeHolder to reflect it holds a Parent node, not a Scene
    private static class NodeHolder {
        private final Parent node;
        private final Object controller;

        public NodeHolder(Parent node, Object controller) {
            this.node = node;
            this.controller = controller;
        }

        public Parent getNode() {
            return node;
        }

        public Object getController() {
            return controller;
        }
    }
}
