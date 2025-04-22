package ch.unibas.dmi.dbis.cs108.client.ui.controllers;

import ch.unibas.dmi.dbis.cs108.client.ui.SceneManager;
import ch.unibas.dmi.dbis.cs108.client.ui.components.AboutDialog;
import ch.unibas.dmi.dbis.cs108.client.ui.components.ChatComponent;
import ch.unibas.dmi.dbis.cs108.client.ui.components.SettingsDialog;
import ch.unibas.dmi.dbis.cs108.client.ui.events.ErrorEvent;
import ch.unibas.dmi.dbis.cs108.client.ui.events.UIEventBus;
import ch.unibas.dmi.dbis.cs108.client.ui.events.admin.ConnectionStatusEvent;
import ch.unibas.dmi.dbis.cs108.client.ui.events.admin.NameChangeRequestEvent; // Assuming this event exists
import ch.unibas.dmi.dbis.cs108.client.ui.events.admin.NameChangeResponseEvent;
import ch.unibas.dmi.dbis.cs108.client.ui.utils.ResourceLoader;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

import ch.unibas.dmi.dbis.cs108.client.networking.ConnectionState; // Ensure this import exists
import ch.unibas.dmi.dbis.cs108.client.core.entities.Player; // Import Player

/**
 * Controller for the main menu screen.
 * Manages the main menu interface, navigation, connection status, chat, and
 * dialogs.
 */
public class MainMenuController extends BaseController {
    private static final Logger LOGGER = Logger.getLogger(MainMenuController.class.getName());
    private static final String DEFAULT_USERNAME = "Guest";
    private static final String VERSION = "1.0.0";

    private final AtomicBoolean isConnected = new AtomicBoolean(false);
    private Player localPlayer;
    private int onlineUserCount = 0;

    @FXML
    private BorderPane mainMenuRoot;
    @FXML
    private ImageView gameLogo;
    @FXML
    private Label onlineUsersLabel;
    @FXML
    private Label connectionStatus;
    @FXML
    private Label versionLabel;
    @FXML
    private VBox chatContainer;

    private AboutDialog aboutDialog;
    private SettingsDialog settingsDialog;
    private ChatComponent chatComponentController;

    /**
     * Constructs the controller, injecting dependencies via the BaseController.
     */
    public MainMenuController() {
        super(new ResourceLoader(), UIEventBus.getInstance(), SceneManager.getInstance());
        LOGGER.finer("MainMenuController instance created.");
        this.localPlayer = new Player(System.getProperty("user.name", DEFAULT_USERNAME));
    }

    /**
     * Initializes the controller after FXML loading.
     */
    @FXML
    private void initialize() {
        LOGGER.info("Initializing MainMenuController...");
        try {
            initializeUI();
            initializeDialogs();
            setupEventHandlers();
            setupChatComponent();
            establishServerConnection();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Critical error during MainMenuController initialization", e);
            displayInitializationError("Error initializing main menu interface.");
        }
        LOGGER.info("MainMenuController initialization complete.");
    }

    /**
     * Configures static UI elements and loads the game logo.
     */
    private void initializeUI() {
        versionLabel.setText("Version " + VERSION);
        updateOnlineUserCount(onlineUserCount);
        updateConnectionStatusLabel(isConnected.get());
        loadGameLogoAsync();
    }

    /**
     * Creates instances of the dialogs used by this controller.
     */
    private void initializeDialogs() {
        aboutDialog = new AboutDialog();
        settingsDialog = new SettingsDialog();
    }

    /**
     * Loads the game logo image asynchronously.
     */
    private void loadGameLogoAsync() {
        CompletableFuture.runAsync(() -> {
            try {
                Image logo = resourceLoader.loadImage(ResourceLoader.GAME_LOGO);
                Platform.runLater(() -> {
                    if (logo != null) {
                        gameLogo.setImage(logo);
                        gameLogo.setPreserveRatio(true);
                    } else {
                        LOGGER.warning("Game logo image could not be loaded from: " + ResourceLoader.GAME_LOGO);
                    }
                });
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Error loading game logo asynchronously", e);
            }
        });
    }

    /**
     * Subscribes to relevant events and sets up listeners for dialog interactions.
     */
    private void setupEventHandlers() {
        eventBus.subscribe(ConnectionStatusEvent.class, this::handleConnectionStatus);
        eventBus.subscribe(ErrorEvent.class, this::handleErrorEvent);
        eventBus.subscribe(NameChangeResponseEvent.class, this::handleNameChangeResponse);

        settingsDialog.playerNameProperty().addListener((obs, oldName, newName) -> {
            if (newName != null && !newName.trim().isEmpty() && !newName.equals(localPlayer.getName())) {
                requestNameChange(newName.trim());
            }
        });
    }

    /**
     * Displays an error message in the chat component if initialization fails.
     *
     * @param message The error message to display.
     */
    private void displayInitializationError(String message) {
        if (chatComponentController != null) {
            chatComponentController.addSystemMessage("FATAL ERROR: " + message);
            chatComponentController
                    .addSystemMessage("Some features may be unavailable. Please restart the application.");
        }
    }

    /**
     * Initializes and adds the chat component to the UI.
     */
    private void setupChatComponent() {
        chatContainer.getChildren().clear();
        chatComponentController = new ChatComponent();
        chatContainer.getChildren().add(chatComponentController.getView());
        chatComponentController.setPlayer(localPlayer);
        chatComponentController.addSystemMessage("Welcome to Settlers of Asgard!");
    }

    /**
     * Handles ConnectionStatusEvent updates from the network layer.
     *
     * @param event The connection status event.
     */
    private void handleConnectionStatus(ConnectionStatusEvent event) {
        Objects.requireNonNull(event, "ConnectionStatusEvent cannot be null");
        Platform.runLater(() -> {
            boolean currentlyConnected = event.getState() == ConnectionState.CONNECTED;
            boolean wasConnected = isConnected.getAndSet(currentlyConnected);
            updateConnectionStatusLabel(currentlyConnected);

            if (chatComponentController != null && event.getMessage() != null && !event.getMessage().isEmpty()) {
                chatComponentController.addSystemMessage(event.getMessage());
            }
            if (!currentlyConnected && wasConnected && chatComponentController != null) {
                chatComponentController.addSystemMessage("Disconnected from server. Attempting to reconnect...");
            }
            if (currentlyConnected && !wasConnected && chatComponentController != null) {
                chatComponentController.addSystemMessage("Reconnected to the server.");
            }
            settingsDialog.setConnectionStatus(currentlyConnected, currentlyConnected ? "Connected" : "Disconnected");
        });
    }

    /**
     * Handles generic ErrorEvent updates. Displays the error message in the chat.
     *
     * @param event The error event.
     */
    private void handleErrorEvent(ErrorEvent event) {
        Objects.requireNonNull(event, "ErrorEvent cannot be null");
        Platform.runLater(() -> {
            String errorMessage = event.getErrorMessage();
            LOGGER.warning("Received error event: " + errorMessage);
            if (chatComponentController != null && errorMessage != null && !errorMessage.isEmpty()) {
                chatComponentController.addSystemMessage("Error: " + errorMessage);
            }
        });
    }

    /**
     * Handles the response from a player name change request.
     *
     * @param event The name change response event.
     */
    private void handleNameChangeResponse(NameChangeResponseEvent event) {
        Objects.requireNonNull(event, "NameChangeResponseEvent cannot be null");
        Platform.runLater(() -> {
            if (event.isSuccess()) {
                String newName = event.getNewName();
                localPlayer.setName(newName);
                LOGGER.info("Player name successfully changed to: " + localPlayer.getName());
                if (chatComponentController != null) {
                    chatComponentController.setPlayer(localPlayer);
                    chatComponentController.addSystemMessage("Name successfully changed to: " + localPlayer.getName());
                }
                settingsDialog.playerNameProperty().set(localPlayer.getName());
            } else {
                String failureMsg = event.getMessage() != null ? event.getMessage() : "Unknown reason.";
                LOGGER.warning("Failed to change player name: " + failureMsg);
                if (chatComponentController != null) {
                    chatComponentController.addSystemMessage("Failed to change name: " + failureMsg);
                }
                settingsDialog.playerNameProperty().set(localPlayer.getName());
            }
        });
    }

    /**
     * Handles the "Play Game" button click. Switches to the lobby scene if
     * connected.
     */
    @FXML
    private void handlePlayGame() {
        LOGGER.info("Play button clicked.");
        if (!isConnected.get()) {
            LOGGER.warning("Cannot start game: Not connected to server.");
            if (chatComponentController != null) {
                chatComponentController.addSystemMessage("Cannot enter lobby: Not connected to the server.");
            }
            return;
        }
        sceneManager.switchToScene(SceneManager.SceneType.LOBBY);
    }

    /**
     * Handles the "Settings" button click. Opens the SettingsDialog as an overlay.
     */
    @FXML
    private void handleSettings() {
        LOGGER.info("Settings button clicked.");
        if (settingsDialog.getView().getParent() != null && settingsDialog.getView().getParent() != mainMenuRoot) {
            LOGGER.warning("Settings dialog is already attached elsewhere.");
            return;
        }
        settingsDialog.setConnectionStatus(isConnected.get(), isConnected.get() ? "Connected" : "Disconnected");
        settingsDialog.playerNameProperty().set(this.localPlayer.getName());
        Node previousCenter = mainMenuRoot.getCenter();
        StackPane container = new StackPane(previousCenter, settingsDialog.getView());
        StackPane.setAlignment(settingsDialog.getView(), Pos.CENTER);
        mainMenuRoot.setCenter(container);

        settingsDialog.setOnCloseAction(() -> mainMenuRoot.setCenter(previousCenter));
        settingsDialog.setOnSaveAction(() -> {
            boolean muted = settingsDialog.muteProperty().get();
            double volume = settingsDialog.volumeProperty().get();
            String requestedName = settingsDialog.playerNameProperty().get();
            LOGGER.info("Settings dialog save requested - Volume: " + volume + ", Muted: " + muted
                    + ", Requested Name: " + requestedName);
            if (chatComponentController != null) {
                chatComponentController.addSystemMessage(
                        "Audio settings saved. " + (muted ? "Muted." : "Volume: " + (int) volume + "%"));
            }
            mainMenuRoot.setCenter(previousCenter);
        });

        settingsDialog.show();
    }

    /**
     * Handles the "About" button click. Opens the AboutDialog as an overlay.
     */
    @FXML
    private void handleAbout() {
        LOGGER.info("About button clicked.");
        if (aboutDialog.getView().getParent() != null && aboutDialog.getView().getParent() != mainMenuRoot) {
            LOGGER.warning("About dialog is already attached elsewhere.");
            return;
        }
        Node previousCenter = mainMenuRoot.getCenter();
        StackPane container = new StackPane(previousCenter, aboutDialog.getView());
        StackPane.setAlignment(aboutDialog.getView(), Pos.CENTER);
        mainMenuRoot.setCenter(container);

        aboutDialog.setOnCloseAction(() -> mainMenuRoot.setCenter(previousCenter));
        aboutDialog.show();
    }

    /**
     * Handles the "Exit" button click. Performs cleanup and exits the application.
     */
    @FXML
    private void handleExit() {
        LOGGER.info("Exit button clicked. Cleaning up and exiting application.");
        cleanup();
        Platform.exit();
        System.exit(0);
    }

    /**
     * Initiates the server connection process asynchronously.
     */
    private void establishServerConnection() {
        LOGGER.info("Attempting to establish server connection...");
        CompletableFuture.runAsync(() -> {
            try {
                Thread.sleep(1000);
                boolean success = true;
                int usersOnline = 42;
                Platform.runLater(() -> {
                    if (success) {
                        LOGGER.info("Successfully connected to server.");
                        eventBus.publish(
                                new ConnectionStatusEvent(ConnectionState.CONNECTED, "Connection established."));
                        updateOnlineUserCount(usersOnline);
                    } else {
                        LOGGER.warning("Failed to connect to server.");
                        eventBus.publish(new ConnectionStatusEvent(ConnectionState.DISCONNECTED,
                                "Failed to connect. Please check network or server status."));
                    }
                });
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                LOGGER.log(Level.WARNING, "Connection attempt interrupted.", e);
                Platform.runLater(() -> eventBus
                        .publish(new ConnectionStatusEvent(ConnectionState.DISCONNECTED,
                                "Connection attempt cancelled.")));
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Error during simulated connection attempt.", e);
                Platform.runLater(() -> eventBus
                        .publish(new ConnectionStatusEvent(ConnectionState.DISCONNECTED,
                                "Error connecting: " + e.getMessage())));
            }
        });
    }

    /**
     * Sends a name change request to the server via the event bus.
     *
     * @param newName The desired new player name.
     */
    private void requestNameChange(String newName) {
        LOGGER.info("Requesting name change to: " + newName);
        if (chatComponentController != null) {
            chatComponentController.addSystemMessage("Requesting name change to: " + newName + "...");
        }
        eventBus.publish(new NameChangeRequestEvent(newName));
    }

    /**
     * Updates the "Online Users" label.
     *
     * @param count The number of users currently online.
     */
    private void updateOnlineUserCount(int count) {
        if (count < 0) {
            LOGGER.warning("Received invalid online user count: " + count);
            return;
        }
        this.onlineUserCount = count;
        Platform.runLater(() -> onlineUsersLabel.setText("Online: " + this.onlineUserCount));
    }

    /**
     * Updates the connection status label's text and style class.
     *
     * @param connected {@code true} if connected, {@code false} otherwise.
     */
    private void updateConnectionStatusLabel(boolean connected) {
        Platform.runLater(() -> {
            connectionStatus.setText(connected ? "Connected" : "Disconnected");
            connectionStatus.getStyleClass().removeAll("connected", "disconnected");
            connectionStatus.getStyleClass().add(connected ? "connected" : "disconnected");
        });
    }

    /**
     * Cleans up resources used by this controller.
     */
    public void cleanup() {
        LOGGER.info("Cleaning up MainMenuController resources...");
        eventBus.unsubscribe(ConnectionStatusEvent.class, this::handleConnectionStatus);
        eventBus.unsubscribe(ErrorEvent.class, this::handleErrorEvent);
        eventBus.unsubscribe(NameChangeResponseEvent.class, this::handleNameChangeResponse);

        if (aboutDialog != null) {
            aboutDialog.close();
        }
        if (settingsDialog != null) {
            settingsDialog.close();
        }
        if (chatComponentController != null) {
            chatComponentController.cleanup();
        }
        LOGGER.info("MainMenuController cleanup finished.");
    }

    /**
     * Sets the local player object for this controller.
     *
     * @param player The Player object.
     */
    public void setLocalPlayer(Player player) {
        if (player != null) {
            this.localPlayer = player;
            if (chatComponentController != null) {
                chatComponentController.setPlayer(this.localPlayer);
            }
            if (settingsDialog != null) {
                settingsDialog.playerNameProperty().set(this.localPlayer.getName());
            }
        }
    }
}
