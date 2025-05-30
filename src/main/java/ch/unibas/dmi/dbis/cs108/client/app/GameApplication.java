// File: GameApplication.java
package ch.unibas.dmi.dbis.cs108.client.app;

import ch.unibas.dmi.dbis.cs108.SETTINGS;
import ch.unibas.dmi.dbis.cs108.client.communication.CommunicationMediator;
import ch.unibas.dmi.dbis.cs108.client.core.PlayerIdentityManager;
import ch.unibas.dmi.dbis.cs108.client.core.state.GameState;
import ch.unibas.dmi.dbis.cs108.client.core.state.GameStateManager;
import ch.unibas.dmi.dbis.cs108.client.networking.NetworkController;
import ch.unibas.dmi.dbis.cs108.client.ui.SceneManager;
import ch.unibas.dmi.dbis.cs108.shared.game.Player;
import javafx.animation.PauseTransition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.input.KeyCombination;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The main application class for the Settlers of Asgard game.
 * This class initializes the game, sets up the network connection,
 * and manages the main application lifecycle.
 */
public class GameApplication extends Application {
    private static final Logger LOGGER = Logger.getLogger(GameApplication.class.getName());
    /**
     * The Player instance representing the local player. Made static for global
     * access.
     */

    private static List<String> players;
    /**
     * The lobby ID for the current game session. Initialized to null.
     */
    private static String currentLobbyId = null;
    /**
     * The NetworkController instance for managing network communication.
     */
    private NetworkController networkController;
    /**
     * The Player instance representing the local player.
     */
    private Player localPlayer;

    /**
     * The PlayerIdentityManager instance for managing player identities.
     */
    private PlayerIdentityManager playerManager;

    /**
     * The main entry point for the JavaFX application.
     * It launches the application and sets up the primary stage.
     *
     * @param args Command-line arguments passed to the application.
     */
    public static void main(String[] args) {
        launch(args);
    }

    /**
     * Gets the lobby ID for the current game session.
     *
     * @return lobbyId The ID of the lobby.
     */
    public static String getCurrentLobbyId() {
        return currentLobbyId;
    }

    /**
     * Sets the lobby ID for the current game session.
     *
     * @param lobbyId The ID of the lobby to set.
     */
    public static void setCurrentLobbyId(String lobbyId) {
        currentLobbyId = lobbyId;
    }

    /**
     * Gets the list of players in the current game session.
     *
     * @return players The list of players.
     */
    public static List<String> getPlayers() {
        if (players == null) {
            LOGGER.warning("Players list is not initialized. Returning empty List.");
            new ArrayList<>();
        }
        return players;
    }

    /**
     * Sets the list of players in the current game session.
     *
     * @param players The list of players to set.
     */
    public static void setPlayers(List<String> players) {
        GameApplication.players = players;
    }

    /**
     * Gets the local player instance.
     *
     * @return localPlayer The local player instance.
     */
    public static Player getLocalPlayer() {
        return PlayerIdentityManager.getInstance().getLocalPlayer();
    }

    /**
     * Sets the local player instance.
     *
     * @param player The Player instance to set as the local player.
     */
    public static void setLocalPlayer(Player player) {
        PlayerIdentityManager.getInstance().setLocalPlayer(player);
    }

    /**
     * Starts the JavaFX application.
     * It retrieves command-line parameters, initializes the game,
     * sets up the network connection, and displays the main menu.
     *
     * @param primaryStage The primary stage for this application.
     */
    @Override
    public void start(Stage primaryStage) {
        LOGGER.info("Starting GameApplication...");
        // Retrieve command-line parameters.
        List<String> params = getParameters().getRaw();
        // Expecting: client <serverip>:<serverport> [username]
        String username;
        if (params.size() >= 3 && !params.get(2).isEmpty()) {
            username = params.get(2);
            LOGGER.info("Username provided via command line: " + username);
        } else {
            username = System.getProperty("user.name", "Guest"); // Default to system username or "Guest"
            LOGGER.info("Using system username or default: " + username);
        }

        // Initialize the PlayerIdentityManager and localPlayer
        playerManager = PlayerIdentityManager.getInstance();
        localPlayer = getLocalPlayer();
        playerManager.addPlayerUpdateListener(this::handlePlayerUpdate);

        // Update the players with the username
        PlayerIdentityManager.getInstance().updatePlayerName(username);

        // Extract server ip and port from the second argument.
        if (params.size() >= 2) {
            String serverInfo = params.get(1); // expected format: serverip:serverport
            String[] parts = serverInfo.split(":");
            if (parts.length == 2) {
                try {
                    String serverIp = parts[0];
                    int serverPort = Integer.parseInt(parts[1]);
                    LOGGER.info("Connecting to server at " + serverIp + ":" + serverPort);
                    // Pass the static localPlayer instance
                    networkController = new NetworkController(localPlayer);

                    GameState gameState = new GameState();
                    GameStateManager gameStateManager = new GameStateManager(gameState);

                    // Initialize CommunicationMediator to wire UI and network messages.
                    new CommunicationMediator(networkController, gameStateManager, localPlayer);
                    LOGGER.info("CommunicationMediator initialized.");

                    networkController.connect(serverIp, serverPort);
                } catch (NumberFormatException e) {
                    LOGGER.log(Level.SEVERE, "Invalid server port format: " + parts[1], e);
                    System.err.println("Invalid server port format. Port must be a number.");
                    Platform.exit(); // Use Platform.exit() for graceful shutdown
                    System.exit(1);
                } catch (Exception e) { // Catch broader exceptions during connection setup
                    LOGGER.log(Level.SEVERE, "Failed to initialize or connect network controller.", e);
                    System.err.println("Error initializing network connection: " + e.getMessage());
                    Platform.exit();
                    System.exit(1);
                }
            } else {
                LOGGER.severe("Invalid server address format. Expected <serverip>:<serverport>, got: " + serverInfo);
                System.err.println("Invalid server address format. Expected <serverip>:<serverport>");
                Platform.exit();
                System.exit(1);
            }
        } else {
            LOGGER.severe("Server address not provided via command line.");
            System.err.println("Server address not provided. Usage: client <serverip>:<serverport> [username]");
            Platform.exit();
            System.exit(1);
        }

        // Initialize and display the main menu scene.
        SceneManager sceneManager = SceneManager.getInstance();
        sceneManager.setPrimaryStage(primaryStage);
        primaryStage.setTitle("Settlers of Asgard");

        if (SETTINGS.Config.SHOW_SPLASH_SCREEN.getValue() == 1) {
            LOGGER.info("Splash screen is enabled.");

            // True Fullscreen Mode
            primaryStage.setFullScreen(true);
            primaryStage.setFullScreenExitHint(""); // Remove the exit hint text
            primaryStage.setFullScreenExitKeyCombination(KeyCombination.NO_MATCH);

            // Set the focus to the primary stage for the duration of the splash screen
            primaryStage.setAlwaysOnTop(true);
            AtomicBoolean splashActive = new AtomicBoolean(true);
            primaryStage.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
                if (!isNowFocused && splashActive.get()) {
                    Platform.runLater(() -> {
                        primaryStage.toFront();
                        primaryStage.requestFocus();
                    });
                }
            });

            // Start with Splash Screen / Intro
            sceneManager.switchToScene(SceneManager.SceneType.SPLASH);

            // show and schedule splash timeout
            primaryStage.show();
            PauseTransition delay = new PauseTransition(Duration.millis(SETTINGS.Config.SPLASH_SCREEN_DURATION.getValue()));
            delay.setOnFinished(e -> {
                splashActive.set(false);
                primaryStage.setAlwaysOnTop(false);
            });
            delay.play();
        } else {
            LOGGER.info("Splash screen is disabled.");

            // Windowed Mode
            primaryStage.setFullScreen(false);
            primaryStage.setWidth(1024);
            primaryStage.setHeight(720);


            // Start with MAIN_MENU
            sceneManager.switchToScene(SceneManager.SceneType.MAIN_MENU);

            // Show the primary stage
            primaryStage.show();
        }

        // Set minimum size fallbacks if exiting fullscreen
        primaryStage.setMinWidth(800);
        primaryStage.setMinHeight(600);

        // Set handler for close request (e.g., clicking the window's X button)
        primaryStage.setOnCloseRequest(event -> {
            LOGGER.info("Application close requested. Cleaning up...");
            cleanup();
            // Allow the default close behavior to proceed
        });

        // final bring‑to‑front just in case
        Platform.runLater(() -> {
            primaryStage.toFront();
            primaryStage.requestFocus();
        });
    }

    /**
     * Handles player updates by updating the local player instance.
     *
     * @param updatedPlayer The updated Player instance.
     */
    public void handlePlayerUpdate(Player updatedPlayer) {
        localPlayer = updatedPlayer;
    }

    /**
     * Stops the JavaFX application and performs cleanup.
     */
    @Override
    public void stop() {
        LOGGER.info("Application stopping...");
        cleanup();
        // No need to call super.stop() explicitly, it's handled by the framework.
        LOGGER.info("Application stopped.");
    }

    /**
     * Performs cleanup tasks like disconnecting the network controller.
     */
    private void cleanup() {
        // Gracefully close the network connection
        if (networkController != null) {
            LOGGER.info("Disconnecting network controller...");
            networkController.disconnect();
            networkController = null; // Help GC
        }
        playerManager.removePlayerUpdateListener(this::handlePlayerUpdate);
        // Perform any additional cleanup tasks if necessary (e.g., closing resources)
        Platform.exit();
        System.exit(0);
        LOGGER.info("Cleanup complete.");
    }
}
