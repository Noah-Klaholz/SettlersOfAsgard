package ch.unibas.dmi.dbis.cs108.client.ui;

import ch.unibas.dmi.dbis.cs108.client.ui.utils.ResourceLoader;
import ch.unibas.dmi.dbis.cs108.client.ui.utils.ThemeManager;
import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
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

/**
 * Manages scene transitions and caching for the JavaFX application.
 * <p>
 * Supports lazy loading of FXML views, caching of root nodes and controllers,
 * and smooth fade animations between scenes.
 */
public class SceneManager {
    private static final Logger LOGGER = Logger.getLogger(SceneManager.class.getName());
    private static volatile SceneManager instance;

    private final Map<SceneType, NodeHolder> nodeCache = new ConcurrentHashMap<>();
    private final ResourceLoader resourceLoader;
    private Stage primaryStage;

    /**
     * Private constructor to enforce singleton pattern.
     *
     * @param resourceLoader utility for loading FXML resources
     */
    private SceneManager(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    /**
     * Returns the singleton instance of the SceneManager.
     * Uses double-checked locking for thread-safe initialization.
     *
     * @return the SceneManager instance
     */
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

    /**
     * Sets the primary {@link Stage} for scene management.
     *
     * @param stage the primary stage
     */
    public void setPrimaryStage(Stage stage) {
        this.primaryStage = stage;
    }

    /**
     * Switches the application to the specified scene type.
     * If the scene is not yet loaded, it is loaded and cached.
     * Performs a fade transition between scenes.
     *
     * @param sceneType the target scene type
     */
    public void switchToScene(SceneType sceneType) {
        NodeHolder holder = nodeCache.computeIfAbsent(sceneType, this::loadNodeAndController);
        Parent newRoot = holder.getNode();
        Scene currentScene = primaryStage.getScene();

        if (currentScene == null) {
            Scene scene = new Scene(newRoot, Color.rgb(26, 33, 51));
            primaryStage.setScene(scene);
            LOGGER.info("Initial scene set: " + sceneType);
            ThemeManager.getInstance().registerScene(scene);
            createFadeTransition(newRoot, 0.0, 1.0, 150)
                    .setInterpolator(Interpolator.EASE_OUT);
            createFadeTransition(newRoot, 0.0, 1.0, 150).play();
        } else {
            Parent oldRoot = currentScene.getRoot();
            FadeTransition fadeOut = createFadeTransition(oldRoot, 1.0, 0.2, 100);

            newRoot.setOpacity(0.0);
            FadeTransition fadeIn = createFadeTransition(newRoot, 0.0, 1.0, 150);

            fadeOut.setOnFinished(event -> {
                currentScene.setRoot(newRoot);
                ThemeManager.getInstance().applyThemeToScene(currentScene);
                LOGGER.info("Scene switched to: " + sceneType);
                fadeIn.play();
            });

            fadeOut.play();
        }
    }

    /**
     * Loads the FXML for the given scene type and returns its root node and controller.
     *
     * @param sceneType the type of scene to load
     * @return a {@link NodeHolder} containing the loaded node and controller
     * @throws RuntimeException if the FXML resource is not found or fails to load
     */
    private NodeHolder loadNodeAndController(SceneType sceneType) {
        URL fxmlUrl = getClass().getResource(sceneType.getPath());
        if (fxmlUrl == null) {
            throw new RuntimeException("FXML not found: " + sceneType.getPath());
        }

        try {
            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent root = loader.load();
            LOGGER.info("Loaded scene: " + sceneType);
            return new NodeHolder(root, loader.getController());
        } catch (IOException e) {
            throw new RuntimeException("Unable to load FXML: " + sceneType.getPath(), e);
        }
    }

    /**
     * Retrieves the controller for a given scene type, loading it if necessary.
     *
     * @param sceneType the scene type whose controller is requested
     * @param <T>       the expected type of the controller
     * @return the controller instance
     * @throws IllegalStateException if the controller cannot be loaded or retrieved
     */
    @SuppressWarnings("unchecked")
    public <T> T getController(SceneType sceneType) {
        NodeHolder holder = nodeCache.computeIfAbsent(sceneType, this::loadNodeAndController);
        if (holder == null || holder.getController() == null) {
            throw new IllegalStateException("Controller not available for: " + sceneType);
        }
        return (T) holder.getController();
    }

    /**
     * Creates a {@link FadeTransition} for the given node.
     *
     * @param node       the target node
     * @param fromValue  starting opacity
     * @param toValue    ending opacity
     * @param durationMs duration in milliseconds
     * @return the configured FadeTransition
     */
    private FadeTransition createFadeTransition(Parent node, double fromValue, double toValue, int durationMs) {
        FadeTransition transition = new FadeTransition(Duration.millis(durationMs), node);
        transition.setFromValue(fromValue);
        transition.setToValue(toValue);
        return transition;
    }

    /**
     * Enumeration of application scenes and their FXML paths.
     */
    public enum SceneType {
        MAIN_MENU(ResourceLoader.MAIN_MENU_FXML),
        LOBBY(ResourceLoader.LOBBY_SCREEN_FXML),
        GAME(ResourceLoader.GAME_SCREEN_FXML);

        private final String path;

        SceneType(String path) {
            this.path = path;
        }

        /**
         * Returns the resource path to the FXML file.
         *
         * @return the FXML path
         */
        public String getPath() {
            return path;
        }
    }

    /**
     * Holder for a scene's root node and its controller.
     */
    private static class NodeHolder {
        private final Parent node;
        private final Object controller;

        /**
         * Constructs a new NodeHolder.
         *
         * @param node       the root node of the scene
         * @param controller the associated controller
         */
        public NodeHolder(Parent node, Object controller) {
            this.node = node;
            this.controller = controller;
        }

        /**
         * Returns the root node.
         *
         * @return the root node
         */
        public Parent getNode() {
            return node;
        }

        /**
         * Returns the controller object.
         *
         * @return the controller
         */
        public Object getController() {
            return controller;
        }
    }
}
