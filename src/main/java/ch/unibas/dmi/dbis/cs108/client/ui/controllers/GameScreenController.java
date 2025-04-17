package ch.unibas.dmi.dbis.cs108.client.ui.controllers;

import ch.unibas.dmi.dbis.cs108.client.ui.SceneManager;
import ch.unibas.dmi.dbis.cs108.client.ui.events.UIEventBus;
import ch.unibas.dmi.dbis.cs108.client.ui.events.admin.ConnectionStatusEvent;
import ch.unibas.dmi.dbis.cs108.client.ui.events.chat.GlobalChatEvent;
import ch.unibas.dmi.dbis.cs108.client.ui.events.chat.LobbyChatEvent;
import ch.unibas.dmi.dbis.cs108.client.ui.utils.ResourceLoader;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.logging.Logger;

public class GameScreenController extends BaseController {
    private static final Logger LOGGER = Logger.getLogger(GameScreenController.class.getName());
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    private final ObservableList<String> chatMessages = FXCollections.observableArrayList();
    private final ObservableList<String> players = FXCollections.observableArrayList();
    private String currentLobbyId; // To store the current lobby ID
    // UI Components
    @FXML
    private Canvas gameCanvas;
    @FXML
    private ProgressBar energyBar;
    @FXML
    private Label runesLabel;
    @FXML
    private ListView<String> playersList;
    @FXML
    private HBox artifactHand;
    @FXML
    private FlowPane structureHand;
    @FXML
    private ListView<String> chatListView;
    @FXML
    private TextField chatInputField;
    @FXML
    private ToggleButton globalChatButton;
    @FXML
    private ToggleButton lobbyChatButton;
    @FXML
    private Label connectionStatusLabel;

    public GameScreenController() {
        super(new ResourceLoader(), UIEventBus.getInstance(), SceneManager.getInstance());
    }

    @FXML
    private void initialize() {
        LOGGER.info("Initializing game screen");
        try {
            setupUI();
            setupEventHandlers();
        } catch (Exception e) {
            LOGGER.severe("Failed to initialize game screen: " + e.getMessage());
        }
    }


    private void setupUI() {
        // Set up the chat list
        chatListView.setItems(chatMessages);

        // Set up the players list
        playersList.setItems(players);

        // Select global chat by default
        globalChatButton.setSelected(true);

        // Set initial values
        energyBar.setProgress(0.5);
        runesLabel.setText("0");
        connectionStatusLabel.setText("Connected");

        // Initialize canvas
        gameCanvas.widthProperty().bind(((Region) gameCanvas.getParent()).widthProperty());
        gameCanvas.heightProperty().bind(((Region) gameCanvas.getParent()).heightProperty());

        // TODO: Fetch initial game state instead of using mock data
    }

    private void setupEventHandlers() {
        // TODO: Subscribe to relevant UI events (e.g., GlobalChatEvent, GameStateUpdateEvent, PlayerListUpdateEvent)
        // TODO: Subscribe to ConnectionStatusEvent
        // ... other events
        eventBus.subscribe(GlobalChatEvent.class, this::handleChatMessage);
        eventBus.subscribe(LobbyChatEvent.class, this::handleLobbyChatMessage);
        eventBus.subscribe(ConnectionStatusEvent.class, this::handleConnectionStatus);
        // TODO: Add subscriptions for GameStateUpdateEvent, PlayerListUpdateEvent, etc.

        // Setup input field handler
        chatInputField.setOnAction(event -> handleMessageSend());
    }

    // --- UIEvent Handlers ---

    private void handleChatMessage(GlobalChatEvent event) {
        if (event == null || event.getContent() == null || event.getSender() == null) {
            LOGGER.warning("Received null or incomplete UI chat message event in GameScreen");
            return;
        }

        Platform.runLater(() -> {
            // Format the message including the sender before adding
            String messageWithSender = String.format("%s: %s", event.getSender(), event.getContent());
            addChatMessage(messageWithSender);
        });
    }

    private void handleLobbyChatMessage(LobbyChatEvent event) {
        if (event == null || event.getMessage() == null || event.getSender() == null) {
            LOGGER.warning("Received null or incomplete lobby chat message event in GameScreen");
            return;
        }

        Platform.runLater(() -> {
            // Format the message including the sender before adding
            String messageWithSender = String.format("[Lobby] %s: %s", event.getSender(), event.getMessage());
            addChatMessage(messageWithSender);
        });
    }

    private void handleConnectionStatus(ConnectionStatusEvent event) {
        if (event == null) {
            LOGGER.warning("Received null connection status event in GameScreen");
            return;
        }

        Platform.runLater(() -> {
            String statusText = Optional.ofNullable(event.getStatus()).map(Object::toString).orElse("UNKNOWN");
            connectionStatusLabel.setText(statusText);
            // TODO: Add visual indication for connection status (e.g., color change)

            if (event.getMessage() != null && !event.getMessage().isEmpty()) {
                addSystemMessage(event.getMessage());
            }
        });
    }

    // TODO: Implement handlers for GameStateUpdateEvent, PlayerListUpdateEvent, etc.

    // --- UI Update Methods ---

    private void addChatMessage(String message) {
        if (message == null || message.isEmpty()) {
            return;
        }
        String formattedMessage = getCurrentTime() + " " + message;
        chatMessages.add(formattedMessage);
        scrollToBottom(chatListView, chatMessages);
    }

    private void addSystemMessage(String message) {
        if (message == null || message.isEmpty()) {
            return;
        }
        chatMessages.add(getCurrentTime() + " System: " + message);
        scrollToBottom(chatListView, chatMessages);
    }

    private void scrollToBottom(ListView<String> listView, ObservableList<String> list) {
        Platform.runLater(() -> {
            int size = list.size();
            if (size > 0) {
                listView.scrollTo(size - 1);
            }
        });
    }

    private String getCurrentTime() {
        return "[" + LocalDateTime.now().format(TIME_FORMATTER) + "]";
    }

    // --- FXML Action Handlers ---

    @FXML
    private void handleBackToMainMenu() {
        LOGGER.info("Back to main menu requested");
        sceneManager.switchToScene(SceneManager.SceneType.MAIN_MENU);
    }

    @FXML
    private void handleSettings() {
        LOGGER.info("Settings requested");
        // Implementation for settings dialog
    }

    @FXML
    private void handleResourceOverview() {
        LOGGER.info("Resource overview requested");
        // Show resource overview dialog
    }

    @FXML
    private void handleGameRound() {
        LOGGER.info("End turn requested");
        // End current turn logic
    }

    @FXML
    private void handleLeaderboard() {
        LOGGER.info("Leaderboard requested");
        // Show leaderboard dialog
    }

    @FXML
    private void handleGlobalChatSelect() {
        LOGGER.info("Global chat selected");
        globalChatButton.setSelected(true);
        lobbyChatButton.setSelected(false);
    }

    @FXML
    private void handleLobbyChatSelect() {
        LOGGER.info("Lobby chat selected");
        globalChatButton.setSelected(false);
        lobbyChatButton.setSelected(true);

        // If no lobby is joined, inform the user
        if (currentLobbyId == null || currentLobbyId.isEmpty()) {
            addSystemMessage("You are not in a lobby. Messages will not be sent until you join one.");
        }
    }

    @FXML
    private void handleMessageSend() {
        String message = chatInputField.getText().trim();
        if (!message.isEmpty()) {
            LOGGER.info("Sending message: " + message);

            if (globalChatButton.isSelected()) {
                // Send global chat message
                eventBus.publish(new GlobalChatEvent(message, GlobalChatEvent.ChatType.GLOBAL));
            } else if (lobbyChatButton.isSelected()) {
                // Send lobby chat message if in a lobby
                if (currentLobbyId != null && !currentLobbyId.isEmpty()) {
                    eventBus.publish(new LobbyChatEvent(currentLobbyId, null, message));
                } else {
                    addSystemMessage("Cannot send lobby message: You're not in a lobby.");
                }
            } else {
                // Default to global chat if neither is selected
                LOGGER.warning("No chat type selected, defaulting to GLOBAL");
                eventBus.publish(new GlobalChatEvent(message, GlobalChatEvent.ChatType.GLOBAL));
            }

            chatInputField.clear();
        }
    }

    // Method to set the current lobby ID when joining a lobby
    public void setCurrentLobbyId(String lobbyId) {
        this.currentLobbyId = lobbyId;
    }

    // Add cleanup method to unsubscribe from events
    public void cleanup() {
        eventBus.unsubscribe(GlobalChatEvent.class, this::handleChatMessage);
        eventBus.unsubscribe(LobbyChatEvent.class, this::handleLobbyChatMessage);
        eventBus.unsubscribe(ConnectionStatusEvent.class, this::handleConnectionStatus);
        LOGGER.info("GameScreenController resources cleaned up");
    }
}
