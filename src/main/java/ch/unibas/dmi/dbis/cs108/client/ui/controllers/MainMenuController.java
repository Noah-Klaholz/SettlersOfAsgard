package ch.unibas.dmi.dbis.cs108.client.ui.controllers;

import ch.unibas.dmi.dbis.cs108.client.ui.SceneManager;
import ch.unibas.dmi.dbis.cs108.client.ui.events.ChatMessageEvent;
import ch.unibas.dmi.dbis.cs108.client.ui.events.ConnectionStatusEvent;
import ch.unibas.dmi.dbis.cs108.client.ui.events.UIEventBus;
import ch.unibas.dmi.dbis.cs108.client.ui.utils.ResourceLoader;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.logging.Logger;

public class MainMenuController extends BaseController {
    private static final Logger LOGGER = Logger.getLogger(MainMenuController.class.getName());
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    // Data models
    private final ObservableList<String> chatHistory = FXCollections.observableArrayList();
    private final String playerName = "Guest"; // Would typically come from a user service
    // UI Components
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
    private int onlineUserCount = 0;
    private boolean isConnected = false;

    public MainMenuController() {
        super(new ResourceLoader(), UIEventBus.getInstance(), SceneManager.getInstance());
    }

    @FXML
    private void initialize() {
        LOGGER.info("Initializing main menu");

        // Set version info
        versionLabel.setText("Version 1.0.0");

        // Setup chat history
        globalChatMessages.setItems(chatHistory);

        // Load game logo
        try {
            URL logoUrl = getClass().getResource("/images/game-logo.png");
            if (logoUrl != null) {
                gameLogo.setImage(new Image(logoUrl.toString()));
            } else {
                LOGGER.warning("Game logo resource not found");
            }
        } catch (Exception e) {
            LOGGER.warning("Could not load game logo: " + e.getMessage());
        }

        // Setup connection status (normally would be determined by actual connection state)
        establishServerConnection();

        // Setup event handlers for chat and server events
        setupEventHandlers();

        // Add welcome message to chat
        addSystemMessage("Welcome to Mystic Realms! Join the global chat to talk with other players.");
    }

    private void setupEventHandlers() {
        // Subscribe to specific event types with type-safe handlers
        eventBus.subscribe(ChatMessageEvent.class, this::handleChatMessage);
        eventBus.subscribe(ConnectionStatusEvent.class, this::handleConnectionStatus);
        // Add other event subscriptions as needed
    }

    private void handleChatMessage(ChatMessageEvent event) {
        Platform.runLater(() -> addChatMessage(event.getMessage()));
    }

    private void handleConnectionStatus(ConnectionStatusEvent event) {
        Platform.runLater(() -> {
            updateConnectionStatus(event.getStatus().toString());
            addSystemMessage(event.getMessage());
        });
    }

    private void establishServerConnection() {
        // In a real app, this would attempt to connect to the server
        LOGGER.info("Establishing server connection");

        // Simulate successful connection
        setConnectionStatus(true);
        updateOnlineUserCount("42"); // Mock data
    }

    @FXML
    private void handlePlayGame() {
        LOGGER.info("Play button clicked");
        // Transition to game creation screen
        sceneManager.switchToScene(SceneManager.SceneType.LOBBY);
    }

    @FXML
    private void handleJoinGame() {
        LOGGER.info("Join game button clicked");
        // Transition to lobby browser
        sceneManager.switchToScene(SceneManager.SceneType.LOBBY);
    }

    @FXML
    private void handleSettings() {
        LOGGER.info("Settings button clicked");
        // Would open settings dialog/screen
    }

    @FXML
    private void handleLeaderboard() {
        LOGGER.info("Leaderboard button clicked");
        // Would transition to leaderboard screen
    }

    @FXML
    private void handleAbout() {
        LOGGER.info("About button clicked");
        // Would open about dialog
    }

    @FXML
    private void handleExit() {
        LOGGER.info("Exit button clicked");
        Platform.exit();
    }

    @FXML
    private void handleSendGlobalMessage() {
        String message = globalChatInput.getText().trim();

        if (message.isEmpty() || !isConnected) {
            return;
        }

        // Send message to server
        sendGlobalChatMessage(message);
        globalChatInput.clear();
    }

    // Server communication methods
    private void sendGlobalChatMessage(String message) {
        LOGGER.info("Sending global message: " + message);
        // In real implementation: send to server

        // For demo, add directly to chat
        String formattedMessage = getCurrentTime() + " " + playerName + ": " + message;
        addChatMessage(formattedMessage);
    }

    // UI update methods
    private void addChatMessage(String message) {
        chatHistory.add(message);
        scrollToBottom();
    }

    private void addSystemMessage(String message) {
        chatHistory.add(getCurrentTime() + " System: " + message);
        scrollToBottom();
    }

    private void updateOnlineUserCount(String countData) {
        try {
            onlineUserCount = Integer.parseInt(countData);
        } catch (NumberFormatException e) {
            LOGGER.warning("Invalid user count format: " + countData);
            return;
        }

        onlineUsersLabel.setText("Online: " + onlineUserCount);
    }

    private void updateConnectionStatus(String status) {
        boolean connected = "CONNECTED".equalsIgnoreCase(status);
        setConnectionStatus(connected);
    }

    private void setConnectionStatus(boolean connected) {
        this.isConnected = connected;

        if (connected) {
            connectionStatus.setText("Connected");
            connectionStatus.getStyleClass().remove("disconnected");
        } else {
            connectionStatus.setText("Disconnected");
            connectionStatus.getStyleClass().add("disconnected");

            // Show message in chat
            addSystemMessage("You are disconnected from the server. Reconnecting...");
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
}