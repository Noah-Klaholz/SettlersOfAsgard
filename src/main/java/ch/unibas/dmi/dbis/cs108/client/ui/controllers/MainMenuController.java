package ch.unibas.dmi.dbis.cs108.client.ui.controllers;

import ch.unibas.dmi.dbis.cs108.client.ui.SceneManager;
import ch.unibas.dmi.dbis.cs108.client.ui.events.admin.NameChangeResponseEvent;
import ch.unibas.dmi.dbis.cs108.client.ui.events.chat.GlobalChatEvent;
import ch.unibas.dmi.dbis.cs108.client.ui.events.admin.ConnectionStatusEvent;
import ch.unibas.dmi.dbis.cs108.client.ui.events.UIEventBus;
import ch.unibas.dmi.dbis.cs108.client.ui.utils.ResourceLoader;
import ch.unibas.dmi.dbis.cs108.client.ui.components.AboutDialog;
import ch.unibas.dmi.dbis.cs108.client.ui.components.SettingsDialog;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.geometry.Pos;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MainMenuController extends BaseController {
    private static final Logger LOGGER = Logger.getLogger(MainMenuController.class.getName());
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    private static final String DEFAULT_USERNAME = "Guest";
    // Data models
    private final ObservableList<String> chatHistory = FXCollections.observableArrayList();
    private final AtomicBoolean isConnected = new AtomicBoolean(false);
    // UI Components
    @FXML
    private BorderPane mainMenuRoot;
    @FXML
    private ImageView gameLogo;
    @FXML
    private ListView<String> globalChatMessages;
    @FXML
    private TextField globalChatInput;
    @FXML
    private Label onlineUsersLabel;
    @FXML
    private Label connectionStatus;
    @FXML
    private Label versionLabel;
    private String playerName = DEFAULT_USERNAME;
    private int onlineUserCount = 0;
    private ChangeListener<Number> chatWidthListener;
    private AboutDialog aboutDialog;
    private SettingsDialog settingsDialog;

    public MainMenuController() {
        super(new ResourceLoader(), UIEventBus.getInstance(), SceneManager.getInstance());
    }

    @FXML
    private void initialize() {
        LOGGER.info("Initializing main menu");

        try {
            initializeUI();
            setupEventHandlers();
            establishServerConnection()
                    .thenRun(() -> addSystemMessage("Welcome to Settlers of Asgard! Join the global chat to talk with other players."));
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to initialize main menu", e);
            addSystemMessage("Error initializing interface. Some features may not work correctly.");
        }
    }

    private void initializeUI() {
        // Set version info
        versionLabel.setText("Version 1.0.0");

        // Configure chat list with automatic text wrapping
        configureChatListView();

        // Load game logo
        loadGameLogo();

        // Handle window resizing
        setupResizeListeners();
        
        // Initialize about dialog
        aboutDialog = new AboutDialog();
        
        // Initialize settings dialog
        settingsDialog = new SettingsDialog();
    }

    private void configureChatListView() {
        globalChatMessages.setItems(chatHistory);

        globalChatMessages.setCellFactory(list -> {
            return new javafx.scene.control.ListCell<>() {
                private final Label label = new Label();

                {
                    label.setWrapText(true);
                    label.setTextFill(javafx.scene.paint.Color.valueOf("#e8e8e8"));
                }

                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);

                    if (empty || item == null) {
                        setGraphic(null);
                    } else {
                        label.setText(item);
                        label.setMaxWidth(globalChatMessages.getWidth() - 20);
                        setGraphic(label);
                    }
                }
            };
        });

        // Listener to update text wrapping when width changes
        chatWidthListener = (obs, oldVal, newVal) -> globalChatMessages.refresh();
        globalChatMessages.widthProperty().addListener(chatWidthListener);
    }

    private void loadGameLogo() {
        CompletableFuture.runAsync(() -> {
            try {
                final Image logoImage = resourceLoader.loadImage("images/game-logo.png");
                Platform.runLater(() -> {
                    if (logoImage != null) {
                        gameLogo.setImage(logoImage);
                        gameLogo.setPreserveRatio(true);
                    } else {
                        LOGGER.warning("Game logo resource could not be loaded");
                    }
                });
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Error loading game logo", e);
            }
        });
    }

    private void setupResizeListeners() {
        // Adjust UI elements when the window resizes
        mainMenuRoot.widthProperty().addListener((obs, oldVal, newVal) -> {
            // Any resize-specific adjustments can be made here
            globalChatMessages.refresh();
        });
    }

    private void setupEventHandlers() {
        // Subscribe to events with type-safe handlers
        eventBus.subscribe(GlobalChatEvent.class, this::handleChatMessage);
        eventBus.subscribe(ConnectionStatusEvent.class, this::handleConnectionStatus);
        eventBus.subscribe(ch.unibas.dmi.dbis.cs108.client.ui.events.ErrorEvent.class, this::handleErrorEvent);
        eventBus.subscribe(NameChangeResponseEvent.class, this::handleNameChangeResponse);

        // Setup input field handler
        globalChatInput.setOnAction(event -> handleSendGlobalMessage());
    }

    private void handleChatMessage(GlobalChatEvent event) {
        // Check for null event
        if (event == null || event.getContent() == null) {
            LOGGER.warning("Received null or incomplete UI chat message event");
            return;
        }

        Platform.runLater(() -> {
            // Format the message including time and player name (using class field)
            String formattedMessage = String.format("%s %s: %s", getCurrentTime(), playerName, event.getContent());
            chatHistory.add(formattedMessage);
            scrollToBottom();
        });
    }

    private void handleConnectionStatus(ConnectionStatusEvent event) {
        if (event == null) {
            LOGGER.warning("Received null connection status event");
            return;
        }

        Platform.runLater(() -> {
            updateConnectionStatus(Optional.ofNullable(event.getStatus()).map(Object::toString).orElse("UNKNOWN"));

            if (event.getMessage() != null && !event.getMessage().isEmpty()) {
                addSystemMessage(event.getMessage());
            }
        });
    }

    private CompletableFuture<Void> establishServerConnection() {
        LOGGER.info("Establishing server connection");

        return CompletableFuture.runAsync(() -> {
            try {
                // Simulate network delay
                Thread.sleep(500);

                // Update UI on JavaFX thread
                Platform.runLater(() -> {
                    setConnectionStatus(true);
                    updateOnlineUserCount(42); // Mock data
                });
            } catch (InterruptedException e) {
                LOGGER.log(Level.WARNING, "Connection setup interrupted", e);
                Thread.currentThread().interrupt();

                Platform.runLater(() -> {
                    setConnectionStatus(false);
                    addSystemMessage("Failed to connect to server. Please check your connection.");
                });
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Error establishing connection", e);

                Platform.runLater(() -> {
                    setConnectionStatus(false);
                    addSystemMessage("Error connecting to server: " + e.getMessage());
                });
            }
        });
    }

    @FXML
    private void handlePlayGame() {
        LOGGER.info("Play button clicked");
        if (!isConnected.get()) {
            addSystemMessage("Cannot start game while disconnected from server.");
            return;
        }
        sceneManager.switchToScene(SceneManager.SceneType.LOBBY);
    }

    @FXML
    private void handleSettings() {
        LOGGER.info("Settings button clicked");

        // First, ensure the dialog isn't already showing
        if (settingsDialog.getView().getParent() == mainMenuRoot) {
            return;
        }

        // Update connection status in dialog
        settingsDialog.setConnectionStatus(isConnected.get(),
            isConnected.get() ? "Connected" : "Disconnected");

        // Store the current center content so we can restore it later
        Node currentCenter = mainMenuRoot.getCenter();

        // Create a new StackPane that will hold both the current center and our dialog
        StackPane dialogContainer = new StackPane();
        if (currentCenter != null) {
            dialogContainer.getChildren().add(currentCenter);
        }

        // Add the dialog to the stack pane on top
        dialogContainer.getChildren().add(settingsDialog.getView());

        // Ensure the dialog is centered and uses the full space
        StackPane.setAlignment(settingsDialog.getView(), Pos.CENTER);

        // Set the container as the new center
        mainMenuRoot.setCenter(dialogContainer);

        // Set a close handler to restore the original center when dialog is closed
        settingsDialog.setOnCloseAction(() -> {
            // When dialog closes, restore the original center
            mainMenuRoot.setCenter(currentCenter);
        });

        // Set a save handler for when settings are saved
        settingsDialog.setOnSaveAction(() -> {
            // Handle saved settings
            boolean muted = settingsDialog.muteProperty().get();
            double volume = settingsDialog.volumeProperty().get();

            // Log the settings (in a real app, these would be saved)
            LOGGER.info("Settings saved - Volume: " + volume + ", Muted: " + muted);
            addSystemMessage("Audio settings saved. " + (muted ? "Audio muted." : "Volume set to " + volume + "%"));

            // In a real implementation, these settings would be applied to the audio system
        });

        // Show the dialog
        settingsDialog.show();
    }

    @FXML
    private void handleAbout() {
        LOGGER.info("About button clicked");
        
        // First, ensure the dialog isn't already showing
        if (aboutDialog.getView().getParent() == mainMenuRoot) {
            return;
        }
        
        // Remove previous positioning approach that was causing layout issues
        // Instead, properly set it as the CENTER element of the BorderPane with full coverage
        
        // Store the current center content so we can restore it later
        Node currentCenter = mainMenuRoot.getCenter();
        
        // Create a new StackPane that will hold both the current center and our dialog
        StackPane dialogContainer = new StackPane();
        if (currentCenter != null) {
            dialogContainer.getChildren().add(currentCenter);
        }
        
        // Add the dialog to the stack pane on top
        dialogContainer.getChildren().add(aboutDialog.getView());
        
        // Ensure the dialog is centered and uses the full space
        StackPane.setAlignment(aboutDialog.getView(), Pos.CENTER);
        
        // Set the container as the new center
        mainMenuRoot.setCenter(dialogContainer);
        
        // Set a close handler to restore the original center when dialog is closed
        aboutDialog.setOnCloseAction(() -> {
            // When dialog closes, restore the original center
            mainMenuRoot.setCenter(currentCenter);
        });
        
        // Show the dialog
        aboutDialog.show();
    }

    @FXML
    private void handleExit() {
        LOGGER.info("Exit button clicked");

        // Clean up resources
        cleanup();

        // Exit application
        Platform.exit();
    }

    @FXML
    private void handleSendGlobalMessage() {
        String message = globalChatInput.getText().trim();

        if (message.isEmpty()) {
            return;
        }

        if (!isConnected.get()) {
            addSystemMessage("Cannot send message while disconnected.");
            globalChatInput.clear();
            return;
        }

        // Post the message to the event bus as a global chat event
        eventBus.publish(new GlobalChatEvent(message, GlobalChatEvent.ChatType.GLOBAL));
        globalChatInput.clear(); // Clear input after sending

    }

    private void handleErrorEvent(ch.unibas.dmi.dbis.cs108.client.ui.events.ErrorEvent event) {
        if (event == null) {
            LOGGER.warning("Received null error event");
            return;
        }

        Platform.runLater(() -> {
            addSystemMessage("Error: " + event.getErrorMessage());
        });
    }

    private void handleNameChangeResponse(NameChangeResponseEvent event) {
        if (event == null) {
            LOGGER.warning("Received null name change response event");
            return;
        }

        Platform.runLater(() -> {
            if (event.isSuccess()) {
                playerName = event.getNewName();
                addSystemMessage("Name changed to: " + playerName);
            } else {
                addSystemMessage("Failed to change name: " + event.getMessage());
            }
        });
    }

    private void addChatMessage(String message) {
        if (message == null || message.isEmpty()) {
            return;
        }

        // Formatting is now handled in handleChatMessage
        chatHistory.add(message);
        scrollToBottom();
    }

    private void addSystemMessage(String message) {
        if (message == null || message.isEmpty()) {
            return;
        }

        chatHistory.add(getCurrentTime() + " System: " + message);
        scrollToBottom();
    }

    private void updateOnlineUserCount(int count) {
        if (count < 0) {
            LOGGER.warning("Invalid user count: " + count);
            return;
        }

        onlineUserCount = count;
        onlineUsersLabel.setText("Online: " + onlineUserCount);
    }

    private void updateConnectionStatus(String status) {
        boolean connected = "CONNECTED".equalsIgnoreCase(status);
        setConnectionStatus(connected);
    }

    private void setConnectionStatus(boolean connected) {
        isConnected.set(connected);

        if (connected) {
            connectionStatus.setText("Connected");
            connectionStatus.getStyleClass().removeAll("disconnected");
            connectionStatus.getStyleClass().add("status-label");
        } else {
            connectionStatus.setText("Disconnected");
            connectionStatus.getStyleClass().add("disconnected");

            // Show message in chat if it was previously connected
            if (isConnected.get()) {
                addSystemMessage("You have been disconnected from the server. Attempting to reconnect...");
            }
        }
    }

    private void scrollToBottom() {
        Platform.runLater(() -> {
            int size = chatHistory.size();
            if (size > 0) {
                globalChatMessages.scrollTo(size - 1);
            }
        });
    }

    private String getCurrentTime() {
        return "[" + LocalDateTime.now().format(TIME_FORMATTER) + "]";
    }

    public void cleanup() {
        // Unsubscribe from events to prevent memory leaks
        eventBus.unsubscribe(GlobalChatEvent.class, this::handleChatMessage);
        eventBus.unsubscribe(ConnectionStatusEvent.class, this::handleConnectionStatus);
        eventBus.unsubscribe(ch.unibas.dmi.dbis.cs108.client.ui.events.ErrorEvent.class, this::handleErrorEvent);
        eventBus.unsubscribe(NameChangeResponseEvent.class, this::handleNameChangeResponse);

        // Remove property listeners
        if (chatWidthListener != null) {
            globalChatMessages.widthProperty().removeListener(chatWidthListener);
        }
        
        // Make sure to clean up the dialog and restore any layout changes
        if (aboutDialog != null) {
            aboutDialog.close();
        }
        
        // Clean up settings dialog
        if (settingsDialog != null) {
            settingsDialog.close();
        }

        LOGGER.info("MainMenuController resources cleaned up");
    }
}

