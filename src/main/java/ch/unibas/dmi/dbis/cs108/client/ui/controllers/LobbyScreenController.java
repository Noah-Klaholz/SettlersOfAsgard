package ch.unibas.dmi.dbis.cs108.client.ui.controllers;

import ch.unibas.dmi.dbis.cs108.client.networking.events.LobbyListEvent;
import ch.unibas.dmi.dbis.cs108.client.ui.SceneManager;
import ch.unibas.dmi.dbis.cs108.client.ui.events.*;
import ch.unibas.dmi.dbis.cs108.client.ui.utils.ResourceLoader;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.logging.Logger;

public class LobbyScreenController extends BaseController {
    private static final Logger LOGGER = Logger.getLogger(LobbyScreenController.class.getName());
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    // Data models
    private final ObservableList<GameLobby> lobbies = FXCollections.observableArrayList();
    private final ObservableList<String> players = FXCollections.observableArrayList();
    private final ObservableList<String> messages = FXCollections.observableArrayList();
    // Lobby management
    @FXML
    private Label playerNameLabel;
    @FXML
    private TextField searchField;
    @FXML
    private TableView<GameLobby> lobbyTable;
    @FXML
    private TableColumn<GameLobby, String> nameColumn;
    @FXML
    private TableColumn<GameLobby, String> playersColumn;
    @FXML
    private TableColumn<GameLobby, String> statusColumn;
    @FXML
    private TableColumn<GameLobby, String> hostColumn;
    @FXML
    private TextField lobbyNameField;
    @FXML
    private Label errorMessage;
    // Player management
    @FXML
    private ListView<String> playerList;
    @FXML
    private VBox hostControlsPanel;
    @FXML
    private ComboBox<Integer> maxPlayersCombo;
    // Chat components
    @FXML
    private ListView<String> chatMessages;
    @FXML
    private TextField chatInput;
    // State
    private String currentLobbyId;
    private boolean isHost = false;
    private String playerName = "Guest";

    public LobbyScreenController() {
        super(new ResourceLoader(), UIEventBus.getInstance(), SceneManager.getInstance());
    }

    @FXML
    private void initialize() {
        LOGGER.info("Initializing lobby screen");
        setupTableView();
        setupChat();
        setupPlayerList();
        setupComboBoxes();
        setupSearchField();
        setupEventHandlers();

        // Set player name
        playerNameLabel.setText("Player: " + playerName);

        // Request lobby data from server
        requestLobbies();
    }

    private void setupTableView() {
        nameColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getName()));
        playersColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getPlayerCount()));
        statusColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getStatus()));
        hostColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getHostName()));

        lobbyTable.setItems(lobbies);
        lobbyTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                clearErrorMessage();
            }
        });
    }

    private void setupChat() {
        chatMessages.setItems(messages);
        chatInput.setOnAction(event -> handleSendChatMessage());
    }

    private void setupPlayerList() {
        playerList.setItems(players);
    }

    private void setupComboBoxes() {
        maxPlayersCombo.setItems(FXCollections.observableArrayList(2, 3, 4, 5, 6));
        maxPlayersCombo.getSelectionModel().select(Integer.valueOf(4)); // Default to 4 players
    }

    private void setupSearchField() {
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filterLobbies(newValue);
        });
    }

    private void setupEventHandlers() {
        // Use type-safe event subscriptions
        eventBus.subscribe(LobbyListEvent.class, this::handleLobbyList);
        eventBus.subscribe(LobbyJoinEvent.class, this::handleLobbyJoin);
        eventBus.subscribe(ChatMessageEvent.class, this::handleChatMessage);
        eventBus.subscribe(PlayerJoinedEvent.class, this::handlePlayerJoined);
        eventBus.subscribe(PlayerLeftEvent.class, this::handlePlayerLeft);
        eventBus.subscribe(ErrorEvent.class, this::handleError);
    }

    private void handleLobbyList(LobbyListEvent event) {
        Platform.runLater(() -> updateLobbies(event.getLobbies()));
    }

    private void handleLobbyJoin(LobbyJoinEvent event) {
        Platform.runLater(() -> handleSuccessfulJoin(event.getLobbyData()));
    }

    private void handleChatMessage(ChatMessageEvent event) {
        Platform.runLater(() -> addChatMessage(event.getMessage()));
    }

    private void handlePlayerJoined(PlayerJoinedEvent event) {
        Platform.runLater(() -> addPlayer(event.getPlayerName()));
    }

    private void handlePlayerLeft(PlayerLeftEvent event) {
        Platform.runLater(() -> removePlayer(event.getPlayerName()));
    }

    private void handleError(ErrorEvent event) {
        Platform.runLater(() -> showErrorMessage(event.getErrorMessage()));
    }

    @FXML
    private void handleBackToMainMenu() {
        if (currentLobbyId != null) {
            // Send leave lobby request to server
            sendLeaveLobbyRequest();
        }
        sceneManager.switchToScene(SceneManager.SceneType.MAIN_MENU);
    }

    @FXML
    private void handleRefreshLobbies() {
        requestLobbies();
    }

    @FXML
    private void handleCreateLobby() {
        String lobbyName = lobbyNameField.getText().trim();

        if (lobbyName.isEmpty()) {
            showErrorMessage("Please enter a lobby name.");
            return;
        }

        // Send create lobby request to server
        sendCreateLobbyRequest(lobbyName);
        clearErrorMessage();
    }

    @FXML
    private void handleJoinLobby() {
        GameLobby selectedLobby = lobbyTable.getSelectionModel().getSelectedItem();

        if (selectedLobby == null) {
            showErrorMessage("Please select a lobby to join.");
            return;
        }

        if ("Full".equals(selectedLobby.getStatus()) || "In Progress".equals(selectedLobby.getStatus())) {
            showErrorMessage("Cannot join this lobby. It is either full or in progress.");
            return;
        }

        // Send join lobby request to server
        sendJoinLobbyRequest(selectedLobby.getId());
        clearErrorMessage();
    }

    @FXML
    private void handleSendChatMessage() {
        String message = chatInput.getText().trim();

        if (message.isEmpty() || currentLobbyId == null) {
            return;
        }

        // Send chat message to server
        sendChatMessage(message);
        chatInput.clear();
    }

    @FXML
    private void handleStartGame() {
        if (!isHost || currentLobbyId == null) {
            return;
        }

        if (players.size() < 2) {
            showErrorMessage("At least 2 players are needed to start the game.");
            return;
        }

        // Send start game request to server
        sendStartGameRequest();
    }

    // Server communication methods
    private void requestLobbies() {
        // Mock implementation - in a real app, this would communicate with the server
        LOGGER.info("Requesting lobbies from server");

        // Clear existing lobbies for refresh
        lobbies.clear();

        // Add mock data (would be replaced with actual server data)
        lobbies.addAll(
                new GameLobby("lobby1", "Thor's Arena", "2/4", "Open", "Thor"),
                new GameLobby("lobby2", "Odin's Hall", "4/4", "Full", "Odin"),
                new GameLobby("lobby3", "Loki's Game", "1/6", "Open", "Loki"),
                new GameLobby("lobby4", "Valkyrie Battle", "2/2", "In Progress", "Freya")
        );
    }

    private void sendCreateLobbyRequest(String name) {
        LOGGER.info("Creating lobby: " + name);
        // In real implementation: send request to server

        // Mock behavior - simulate success
        currentLobbyId = "new_lobby_" + System.currentTimeMillis();
        isHost = true;

        // Clear and setup the lobby UI
        messages.clear();
        players.clear();

        // Show host controls
        hostControlsPanel.setVisible(true);
        hostControlsPanel.setManaged(true);

        // Add self as player
        addPlayer(playerName + " (Host)");

        // Add welcome message
        addSystemMessage("Lobby created successfully. Waiting for players to join.");
    }

    private void sendJoinLobbyRequest(String lobbyId) {
        LOGGER.info("Joining lobby: " + lobbyId);
        // In real implementation: send request to server

        // Mock behavior - simulate success
        GameLobby selectedLobby = lobbyTable.getSelectionModel().getSelectedItem();
        if (selectedLobby != null) {
            currentLobbyId = lobbyId;
            isHost = false;

            // Clear and setup the lobby UI
            messages.clear();
            players.clear();

            // Hide host controls
            hostControlsPanel.setVisible(false);
            hostControlsPanel.setManaged(false);

            // Add players (mock)
            addPlayer(selectedLobby.getHostName() + " (Host)");
            addPlayer(playerName);

            // Add welcome message
            addSystemMessage("You've joined " + selectedLobby.getName() + ". Welcome!");
        }
    }

    private void sendLeaveLobbyRequest() {
        LOGGER.info("Leaving lobby: " + currentLobbyId);
        // In real implementation: send request to server

        currentLobbyId = null;
        isHost = false;
    }

    private void sendChatMessage(String message) {
        LOGGER.info("Sending chat message in lobby " + currentLobbyId);
        // In real implementation: send to server

        // For demo purposes, add the message directly
        String formattedMessage = getCurrentTime() + " " + playerName + ": " + message;
        messages.add(formattedMessage);
        scrollToBottom(chatMessages);
    }

    private void sendStartGameRequest() {
        LOGGER.info("Starting game in lobby " + currentLobbyId);
        // In real implementation: send to server

        // For demo, just switch to the game scene
        sceneManager.switchToScene(SceneManager.SceneType.GAME);
    }

    // UI update methods
    private void handleSuccessfulJoin(String lobbyData) {
        // Parse lobby data and update UI accordingly
        LOGGER.info("Successfully joined lobby: " + lobbyData);
    }

    private void addChatMessage(String message) {
        messages.add(message);
        scrollToBottom(chatMessages);
    }

    private void addSystemMessage(String message) {
        messages.add(getCurrentTime() + " System: " + message);
        scrollToBottom(chatMessages);
    }

    private void addPlayer(String player) {
        if (!players.contains(player)) {
            players.add(player);
            addSystemMessage(player + " joined the lobby");
        }
    }

    private void removePlayer(String player) {
        boolean removed = players.remove(player);
        if (removed) {
            addSystemMessage(player + " left the lobby");
        }
    }

    private void updateLobbies(String lobbyData) {
        // Parse lobby data and update the table
        // In a real implementation, this would deserialize JSON/other format
        LOGGER.info("Updating lobbies with data: " + lobbyData);
    }

    private void filterLobbies(String searchText) {
        // In a real app, this might filter from a larger dataset
        // For this example, we're just filtering the current observable list
        if (searchText == null || searchText.isEmpty()) {
            requestLobbies(); // Reset to all lobbies
            return;
        }

        String lowerCaseSearch = searchText.toLowerCase();

        ObservableList<GameLobby> filteredList = lobbies.filtered(lobby ->
                lobby.getName().toLowerCase().contains(lowerCaseSearch) ||
                        lobby.getHostName().toLowerCase().contains(lowerCaseSearch)
        );

        lobbyTable.setItems(filteredList);
    }

    private void showErrorMessage(String message) {
        errorMessage.setText(message);
        errorMessage.setVisible(true);
    }

    private void clearErrorMessage() {
        errorMessage.setText("");
        errorMessage.setVisible(false);
    }

    private <T> void scrollToBottom(ListView<T> listView) {
        Platform.runLater(() -> {
            int size = listView.getItems().size();
            if (size > 0) {
                listView.scrollTo(size - 1);
            }
        });
    }

    private String getCurrentTime() {
        return "[" + LocalDateTime.now().format(TIME_FORMATTER) + "]";
    }

    // Model classes
    public static class GameLobby {
        private final String id;
        private final String name;
        private final String playerCount;
        private final String status;
        private final String hostName;

        public GameLobby(String id, String name, String playerCount, String status, String hostName) {
            this.id = id;
            this.name = name;
            this.playerCount = playerCount;
            this.status = status;
            this.hostName = hostName;
        }

        public String getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public String getPlayerCount() {
            return playerCount;
        }

        public String getStatus() {
            return status;
        }

        public String getHostName() {
            return hostName;
        }
    }
}