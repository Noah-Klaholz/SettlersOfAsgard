package ch.unibas.dmi.dbis.cs108.client.ui;

import ch.unibas.dmi.dbis.cs108.client.ui.utils.ResourceLoader;
import ch.unibas.dmi.dbis.cs108.client.ui.utils.ThemeManager;
import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.scene.paint.Color; // Import Color

import java.io.IOException;
import java.net.URL;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Manages scene transitions and caching for the JavaFX application.
 * Handles loading FXML views, caching their root nodes and controllers,
 * and performing fade transitions between scenes. Singleton pattern.
 */
public class SceneManager {
    private static final Logger LOGGER = Logger.getLogger(SceneManager.class.getName());
    private static volatile SceneManager instance;

    private final Map<SceneType, NodeHolder> nodeCache = new ConcurrentHashMap<>();
    private final ResourceLoader resourceLoader;
    private Stage primaryStage;
    private static final Color BACKGROUND_COLOR = Color.rgb(30, 30, 40); // Dark blue-grey background

    /**
     * Private constructor for singleton.
     * 
     * @param resourceLoader Utility for loading resources.
     */
    private SceneManager(ResourceLoader resourceLoader) {
        this.resourceLoader = Objects.requireNonNull(resourceLoader, "ResourceLoader cannot be null");
    }

    /**
     * Returns the singleton instance of SceneManager.
     * 
     * @return SceneManager instance
     */
    public static SceneManager getInstance() {
        SceneManager result = instance;
        if (result == null) {
            synchronized (SceneManager.class) {
                result = instance;
                if (result == null) {
                    instance = result = new SceneManager(new ResourceLoader());
                }
            }
        }
        return result;
    }

    /**
     * Sets the primary Stage for the application.
     * 
     * @param stage Primary stage
     */
    public void setPrimaryStage(Stage stage) {
        this.primaryStage = Objects.requireNonNull(stage, "Primary stage cannot be null");
    }

    /**
     * Switches the application's view to the specified scene type.
     * Loads and caches FXML if needed. Applies fade transition.
     * 
     * @param sceneType Target scene type
     */
    public void switchToScene(SceneType sceneType) {
        Objects.requireNonNull(sceneType, "SceneType cannot be null");
        if (primaryStage == null) {
            throw new IllegalStateException("Primary stage has not been set. Call setPrimaryStage() first.");
        }
        if (!Platform.isFxApplicationThread()) {
            Platform.runLater(() -> switchToSceneInternal(sceneType));
        } else {
            switchToSceneInternal(sceneType);
        }
    }

    /**
     * Internal implementation for switching scenes, called on FX thread.
     * 
     * @param sceneType Target scene type
     */
    private void switchToSceneInternal(SceneType sceneType) {
        NodeHolder holder = nodeCache.computeIfAbsent(sceneType, this::loadNodeAndController);
        if (holder == null || holder.getNode() == null) {
            LOGGER.severe("Failed to load or retrieve node holder for scene: " + sceneType);
            return;
        }
        Parent newRoot = holder.getNode();
        Scene currentScene = primaryStage.getScene();

        if (currentScene == null) {
            Scene scene = new Scene(newRoot);
            scene.setFill(BACKGROUND_COLOR); // Set background color for new scene
            primaryStage.setScene(scene);
            ThemeManager.getInstance().registerScene(scene);
            LOGGER.info("Initial scene set: " + sceneType);
            newRoot.setOpacity(0.0);
            createFadeTransition(newRoot, 0.0, 1.0, 250).play();
        } else {
            Parent oldRoot = currentScene.getRoot();
            if (oldRoot == newRoot) {
                LOGGER.fine("Attempted to switch to the same scene: " + sceneType);
                return;
            }
            currentScene.setFill(BACKGROUND_COLOR); // Ensure background color is set on existing scene
            FadeTransition fadeOut = createFadeTransition(oldRoot, 1.0, 0.0, 150);
            FadeTransition fadeIn = createFadeTransition(newRoot, 0.0, 1.0, 200);
            newRoot.setOpacity(0.0);
            fadeOut.setOnFinished(event -> {
                currentScene.setRoot(newRoot);
                LOGGER.info("Scene switched to: " + sceneType);
                fadeIn.play();
            });
            fadeOut.play();
        }
    }

    /**
     * Loads the FXML file associated with the given scene type.
     * Caches the loaded root node and its controller.
     * 
     * @param sceneType Scene type to load
     * @return NodeHolder with root node and controller, or null on failure
     */
    private NodeHolder loadNodeAndController(SceneType sceneType) {
        URL fxmlUrl = getClass().getResource(sceneType.getPath());
        if (fxmlUrl == null) {
            LOGGER.log(Level.SEVERE, "FXML resource not found: " + sceneType.getPath());
            return null;
        }
        try {
            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent root = loader.load();
            Object controller = loader.getController();
            LOGGER.info("Successfully loaded scene: " + sceneType);
            return new NodeHolder(root, controller);
        } catch (IOException | IllegalStateException e) {
            LOGGER.log(Level.SEVERE, "Unable to load FXML: " + sceneType.getPath(), e);
            return null;
        }
    }

    /**
     * Retrieves the controller associated with a given scene type.
     * Loads and caches if not already loaded.
     * 
     * @param sceneType Scene type
     * @param <T>       Expected controller type
     * @return Controller instance
     */
    @SuppressWarnings("unchecked")
    public <T> T getController(SceneType sceneType) {
        Objects.requireNonNull(sceneType, "SceneType cannot be null");
        NodeHolder holder = nodeCache.computeIfAbsent(sceneType, this::loadNodeAndController);
        if (holder == null || holder.getController() == null) {
            throw new IllegalStateException("Controller not available or loading failed for: " + sceneType);
        }
        try {
            return (T) holder.getController();
        } catch (ClassCastException e) {
            throw new IllegalStateException("Controller for " + sceneType + " is not of the expected type.", e);
        }
    }

    /**
     * Creates a FadeTransition for animating the opacity of a node.
     * 
     * @param node       Target node
     * @param fromValue  Starting opacity
     * @param toValue    Ending opacity
     * @param durationMs Duration in ms
     * @return FadeTransition
     */
    private FadeTransition createFadeTransition(Parent node, double fromValue, double toValue, int durationMs) {
        Objects.requireNonNull(node, "Node for transition cannot be null");
        FadeTransition transition = new FadeTransition(Duration.millis(durationMs), node);
        transition.setFromValue(fromValue);
        transition.setToValue(toValue);
        transition.setInterpolator(Interpolator.EASE_BOTH);
        return transition;
    }

    /**
     * Enumeration defining the different scenes (views) in the application
     * and their corresponding FXML file paths.
     */
    public enum SceneType {
        SPLASH(ResourceLoader.SPLASH_SCREEN_FXML),
        MAIN_MENU(ResourceLoader.MAIN_MENU_FXML),
        LOBBY(ResourceLoader.LOBBY_SCREEN_FXML),
        GAME(ResourceLoader.GAME_SCREEN_FXML);

        private final String path;

        SceneType(String path) {
            this.path = path;
        }

        /**
         * @return FXML resource path string
         */
        public String getPath() {
            return path;
        }
    }

    /**
     * Helper class to hold a scene's root node and its associated controller.
     */
    private static class NodeHolder {
        private final Parent node;
        private final Object controller;

        public NodeHolder(Parent node, Object controller) {
            this.node = Objects.requireNonNull(node, "Node cannot be null");
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
