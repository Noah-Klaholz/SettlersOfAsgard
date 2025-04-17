package ch.unibas.dmi.dbis.cs108.client.ui.controllers;

import ch.unibas.dmi.dbis.cs108.client.ui.SceneManager;
import ch.unibas.dmi.dbis.cs108.client.ui.events.admin.NameChangeResponseEvent;
import ch.unibas.dmi.dbis.cs108.client.ui.events.chat.GlobalChatEvent;
import ch.unibas.dmi.dbis.cs108.client.ui.events.admin.ConnectionStatusEvent;
import ch.unibas.dmi.dbis.cs108.client.ui.events.UIEventBus;
import ch.unibas.dmi.dbis.cs108.client.ui.events.admin.LeaderboardResponseUIEvent;
import ch.unibas.dmi.dbis.cs108.client.ui.utils.ResourceLoader;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.geometry.Pos;

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
        eventBus.subscribe(LeaderboardResponseUIEvent.class, this::handleLeaderboardUIResponse);

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
        // TODO: Implement settings screen transition
    }

    @FXML
    private void handleLeaderboard() {
        // ToDo: Implement leaderboard dialog
    }

    @FXML
    private void handleAbout() {
        LOGGER.info("About button clicked");

        // Create and show the about dialog
        ch.unibas.dmi.dbis.cs108.client.ui.components.AboutDialog aboutDialog = new ch.unibas.dmi.dbis.cs108.client.ui.components.AboutDialog();

        // Clear any existing dialog
        mainMenuRoot.getChildren().removeIf(node -> node.getId() != null && node.getId().equals("about-overlay"));
        
        // Set up the StackPane.alignment property to center the dialog
        StackPane.setAlignment(aboutDialog.getView(), Pos.CENTER);
        
        // Make sure dialog takes the full size of the parent
        aboutDialog.getView().prefWidthProperty().bind(mainMenuRoot.widthProperty());
        aboutDialog.getView().prefHeightProperty().bind(mainMenuRoot.heightProperty());
        
        // Add the dialog as a direct child of the BorderPane
        mainMenuRoot.getChildren().add(aboutDialog.getView());
        
        // Set the dialog to be removed when closed
        aboutDialog.setOnCloseAction(() -> {
            mainMenuRoot.getChildren().remove(aboutDialog.getView());
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

    /**
     * Handles leaderboard response events from the server.
     * Updates the leaderboard dialog with the received data.
     *
     * @param event The leaderboard response event
     */
    private void handleLeaderboardUIResponse(LeaderboardResponseUIEvent event) {
        // ToDo: Implement leaderboard response handling
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
        eventBus.unsubscribe(LeaderboardResponseUIEvent.class, this::handleLeaderboardUIResponse);

        // Remove property listeners
        if (chatWidthListener != null) {
            globalChatMessages.widthProperty().removeListener(chatWidthListener);
        }

        LOGGER.info("MainMenuController resources cleaned up");
    }
}
