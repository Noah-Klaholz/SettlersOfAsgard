package ch.unibas.dmi.dbis.cs108.client.ui.controllers;

import ch.unibas.dmi.dbis.cs108.client.app.GameApplication; // Import GameApplication
import ch.unibas.dmi.dbis.cs108.client.core.Player;
import ch.unibas.dmi.dbis.cs108.client.networking.ConnectionState;
import ch.unibas.dmi.dbis.cs108.client.ui.SceneManager;
import ch.unibas.dmi.dbis.cs108.client.ui.components.AboutDialog;
import ch.unibas.dmi.dbis.cs108.client.ui.components.ChatComponent;
import ch.unibas.dmi.dbis.cs108.client.ui.components.SettingsDialog;
import ch.unibas.dmi.dbis.cs108.client.ui.events.ErrorEvent;
import ch.unibas.dmi.dbis.cs108.client.ui.events.UIEventBus;
import ch.unibas.dmi.dbis.cs108.client.ui.events.admin.ConnectionStatusEvent;
import ch.unibas.dmi.dbis.cs108.client.ui.events.admin.NameChangeRequestEvent;
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
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Priority; // Import Priority

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Controller for the main menu screen.
 * Manages the main menu interface, navigation, connection status, chat, and
 * dialogs.
 */
public class MainMenuController extends BaseController {
    private static final Logger LOGGER = Logger.getLogger(MainMenuController.class.getName());
    private static final String VERSION = "1.0.0";

    private final AtomicBoolean isConnected = new AtomicBoolean(false);
    private Player localPlayer; // Keep the field, but don't initialize here
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
    }

    /**
     * Initializes the controller after FXML loading.
     */
    @FXML
    private void initialize() {
        LOGGER.info("Initializing MainMenuController...");
        try {
            this.localPlayer = GameApplication.getLocalPlayer(); // Fetch player instance
            if (this.localPlayer == null) {
                LOGGER.severe("LocalPlayer is null during MainMenuController initialization!");
                // Handle error appropriately, maybe show an error message and disable
                // functionality
                this.localPlayer = new Player("ErrorGuest"); // Fallback to avoid NullPointerExceptions
            }
            initializeUI();
            initializeDialogs();
            setupEventHandlers();
            setupChatComponent(); // Call this after localPlayer is set
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
        // Set initial player name in settings dialog
        if (localPlayer != null) {
            settingsDialog.playerNameProperty().set(localPlayer.getName());
        }
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
        Node chatView = chatComponentController.getView(); // Get the view Node
        chatContainer.getChildren().add(chatView);
        VBox.setVgrow(chatView, Priority.ALWAYS); // Make the chat component grow vertically
        if (localPlayer != null) { // Ensure localPlayer is set before passing
            chatComponentController.setPlayer(localPlayer);
        } else {
            LOGGER.warning("Cannot set player in ChatComponent: localPlayer is null.");
        }
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
                // Update the central player instance
                if (localPlayer != null) {
                    localPlayer.setName(newName);
                    LOGGER.info("Player name successfully changed to: " + localPlayer.getName());
                    if (chatComponentController != null) {
                        chatComponentController.setPlayer(localPlayer); // Update chat component's player context
                        chatComponentController
                                .addSystemMessage("Name successfully changed to: " + localPlayer.getName());
                    }
                    settingsDialog.playerNameProperty().set(localPlayer.getName()); // Update settings dialog
                } else {
                    LOGGER.severe("Cannot update player name: localPlayer is null.");
                }
            } else {
                String failureMsg = event.getMessage() != null ? event.getMessage() : "Unknown reason.";
                LOGGER.warning("Failed to change player name: " + failureMsg);
                if (chatComponentController != null) {
                    chatComponentController.addSystemMessage("Failed to change name: " + failureMsg);
                }
                // Revert name in settings dialog if change failed
                if (localPlayer != null) {
                    settingsDialog.playerNameProperty().set(localPlayer.getName());
                }
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

        settingsDialog.setConnectionStatus(isConnected.get(), isConnected.get() ? "Connected" : "Disconnected");
        if (localPlayer != null) {
            settingsDialog.playerNameProperty().set(this.localPlayer.getName());
        } else {
            LOGGER.warning("Cannot set player name in settings: localPlayer is null.");
            settingsDialog.playerNameProperty().set("ErrorGuest");
        }

        settingsDialog.setOnSaveAction(() -> {
            boolean muted = settingsDialog.muteProperty().get();
            double volume = settingsDialog.volumeProperty().get();
            String requestedName = settingsDialog.playerNameProperty().get();
            LOGGER.info("Settings dialog save requested - Volume: " + volume + ", Muted: " + muted
                    + ", Requested Name: " + requestedName);

            if (localPlayer != null && requestedName != null && !requestedName.trim().isEmpty()
                    && !requestedName.equals(localPlayer.getName())) {
                requestNameChange(requestedName.trim());
            } else if (requestedName != null && requestedName.trim().isEmpty()) {
                LOGGER.warning("Attempted to save empty player name.");
                if (chatComponentController != null) {
                    chatComponentController.addSystemMessage("Error: Player name cannot be empty.");
                }
                if (localPlayer != null) {
                    settingsDialog.playerNameProperty().set(localPlayer.getName());
                }
            }

            if (chatComponentController != null) {
                chatComponentController.addSystemMessage(
                        "Audio settings saved. " + (muted ? "Muted." : "Volume: " + (int) volume + "%"));
            }
        });

        showDialogAsOverlay(settingsDialog, mainMenuRoot);
    }

    @FXML
    private void handleAbout() {
        LOGGER.info("About button clicked.");
        showDialogAsOverlay(aboutDialog, mainMenuRoot);
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
}
