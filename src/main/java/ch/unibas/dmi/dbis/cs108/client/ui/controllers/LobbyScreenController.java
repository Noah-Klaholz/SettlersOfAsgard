package ch.unibas.dmi.dbis.cs108.client.ui.controllers;

import ch.unibas.dmi.dbis.cs108.client.ui.SceneManager;
import ch.unibas.dmi.dbis.cs108.client.ui.events.ErrorEvent;
import ch.unibas.dmi.dbis.cs108.client.ui.events.UIEventBus;
import ch.unibas.dmi.dbis.cs108.client.ui.events.admin.NameChangeResponseEvent;
import ch.unibas.dmi.dbis.cs108.client.ui.events.chat.GlobalChatEvent;
import ch.unibas.dmi.dbis.cs108.client.ui.events.chat.LobbyChatEvent;
import ch.unibas.dmi.dbis.cs108.client.ui.events.lobby.*;
import ch.unibas.dmi.dbis.cs108.client.ui.utils.ResourceLoader;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
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
    private final ObservableList<GameLobby> allLobbies = FXCollections.observableArrayList();
    private final ObservableList<String> players = FXCollections.observableArrayList();
    private final ObservableList<String> messages = FXCollections.observableArrayList();
    private FilteredList<GameLobby> filteredLobbies;
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
    @FXML
    private ToggleButton globalChatButton;
    @FXML
    private ToggleButton lobbyChatButton;

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

        // Set player name (Consider fetching actual player name if available)
        playerNameLabel.setText("Player: " + playerName);

        // Request lobby data from server
        requestLobbies();
    }

    private void setupTableView() {
        nameColumn.setCellValueFactory(cellData -> cellData.getValue().nameProperty());
        playersColumn.setCellValueFactory(cellData -> cellData.getValue().playerCountProperty());
        statusColumn.setCellValueFactory(cellData -> cellData.getValue().statusProperty());
        hostColumn.setCellValueFactory(cellData -> cellData.getValue().hostProperty());

        filteredLobbies = new FilteredList<>(allLobbies, p -> true);
        lobbyTable.setItems(filteredLobbies);

        // Double-click to join lobby
        lobbyTable.setRowFactory(tv -> {
            TableRow<GameLobby> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    handleJoinLobby();
                }
            });
            return row;
        });
    }

    private void setupChat() {
        chatMessages.setItems(messages);
        chatInput.setOnAction(event -> handleSendChatMessage());
        
        // Set global chat as default
        globalChatButton.setSelected(true);
        lobbyChatButton.setSelected(false);
    }

    private void setupPlayerList() {
        playerList.setItems(players);
    }

    private void setupComboBoxes() {
        maxPlayersCombo.setItems(FXCollections.observableArrayList(2, 3, 4, 5, 6, 8));
        maxPlayersCombo.getSelectionModel().select(Integer.valueOf(4)); // Default 4 players

        maxPlayersCombo.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && isHost && currentLobbyId != null) {
                // Send update to server
                eventBus.publish(new UpdateLobbySettingsEvent(currentLobbyId, "maxPlayers", newVal.toString()));
            }
        });
    }

    private void setupSearchField() {
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredLobbies.setPredicate(lobby -> {
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }

                String lowerCaseFilter = newValue.toLowerCase();
                if (lobby.getName().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                }
                return lobby.getHost().toLowerCase().contains(lowerCaseFilter);
            });
        });
    }

    private void setupEventHandlers() {
        // Subscribe to UI Events (not networking events directly)
        eventBus.subscribe(LobbyListResponseEvent.class, this::handleLobbyListResponse);
        eventBus.subscribe(LobbyJoinedEvent.class, this::handleLobbyJoined);
        eventBus.subscribe(GlobalChatEvent.class, this::handleGlobalChatMessage);
        eventBus.subscribe(LobbyChatEvent.class, this::handleLobbyChatMessage);
        eventBus.subscribe(PlayerJoinedLobbyEvent.class, this::handlePlayerJoinedLobby);
        eventBus.subscribe(LobbyLeftEvent.class, this::handleLobbyLeft);
        eventBus.subscribe(ErrorEvent.class, this::handleError);
        eventBus.subscribe(LeaveLobbyRequestEvent.class, this::handleLeaveLobby);
        eventBus.subscribe(GameStartedEvent.class, this::handleGameStarted);
        eventBus.subscribe(NameChangeResponseEvent.class, this::handleNameChangeResponse);
    }

    // UI Action Handlers

    @FXML
    private void handleBackToMainMenu() {
        if (currentLobbyId != null) {
            // Leave current lobby first
            eventBus.publish(new LeaveLobbyRequestEvent(currentLobbyId));
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
            showError("Please enter a lobby name");
            return;
        }

        // Clear any previous errors
        clearError();

        // Send create lobby request
        eventBus.publish(new CreateLobbyRequestEvent(lobbyName, playerName));
    }

    @FXML
    private void handleJoinLobby() {
        GameLobby selectedLobby = lobbyTable.getSelectionModel().getSelectedItem();
        if (selectedLobby == null) {
            showError("Please select a lobby to join");
            return;
        }

        if ("In Progress".equals(selectedLobby.getStatus())) {
            showError("Cannot join a game in progress");
            return;
        }

        // Clear any previous errors
        clearError();

        // Send join lobby request
        eventBus.publish(new JoinLobbyRequestEvent(selectedLobby.getId(), playerName));
    }

    @FXML
    private void handleSendChatMessage() {
        String message = chatInput.getText().trim();
        if (message.isEmpty()) {
            return;
        }

        // Determine chat type and send appropriate message
        if (globalChatButton.isSelected()) {
            // Send global chat message
            eventBus.publish(new GlobalChatEvent(message, GlobalChatEvent.ChatType.GLOBAL));
        } else if (lobbyChatButton.isSelected()) {
            // Send lobby chat message if in a lobby
            if (currentLobbyId != null && !currentLobbyId.isEmpty()) {
                eventBus.publish(new LobbyChatEvent(currentLobbyId, message));
            } else {
                addSystemMessage("Cannot send lobby message: You're not in a lobby.");
            }
        } else {
            // Default to global chat if neither is selected
            LOGGER.warning("No chat type selected, defaulting to GLOBAL");
            eventBus.publish(new GlobalChatEvent(message, GlobalChatEvent.ChatType.GLOBAL));
        }

        // Clear the input field
        chatInput.clear();
    }

    @FXML
    private void handleStartGame() {
        if (isHost && currentLobbyId != null) {
            if (players.size() < 2) {
                showError("Need at least 2 players to start");
                return;
            }

            // Send start game request
            eventBus.publish(new StartGameRequestEvent(currentLobbyId));
        }
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

    // UIEvent Bus Handlers
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

    private void handleLobbyListResponse(LobbyListResponseEvent event) {
        Platform.runLater(() -> {
            allLobbies.clear();
            allLobbies.addAll(event.getLobbies());
        });
    }

    private void handleLobbyJoined(LobbyJoinedEvent event) {
        Platform.runLater(() -> {
            currentLobbyId = event.getLobbyId();
            isHost = event.isHost();

            // Update UI based on whether the player is host
            hostControlsPanel.setVisible(isHost);
            hostControlsPanel.setManaged(isHost);

            // Reset displays
            players.clear();
            players.addAll(event.getPlayers());
            messages.clear();

            // Add system message
            addSystemMessage("You joined the lobby");
        });
    }

    private void handleGlobalChatMessage(GlobalChatEvent event) {
        Platform.runLater(() -> {
            String formattedMessage = String.format("[%s] %s: %s",
                    TIME_FORMATTER.format(event.getTimestamp()),
                    event.getSender(),
                    event.getContent());
            messages.add(formattedMessage);
        });
    }

    private void handleLobbyChatMessage(LobbyChatEvent event) {
        Platform.runLater(() -> {
            String formattedMessage = String.format("[%s] %s: %s",
                    TIME_FORMATTER.format(event.getTimestamp()),
                    event.getSender(),
                    event.getMessage());
            messages.add(formattedMessage);
        });
    }

    private void handlePlayerJoinedLobby(PlayerJoinedLobbyEvent event) {
        Platform.runLater(() -> {
            if (!players.contains(event.getPlayerName())) {
                players.add(event.getPlayerName());
                addSystemMessage(event.getPlayerName() + " joined the lobby");
            }
        });
    }

    private void handleLobbyLeft(LobbyLeftEvent event) {
        Platform.runLater(() -> {
            players.remove(event.getPlayerName());
            addSystemMessage(event.getPlayerName() + " left the lobby");
        });
    }


    private void handleError(ErrorEvent event) {
        Platform.runLater(() -> {
            showError(event.getErrorMessage());
        });
    }

    private void handleLeaveLobby(LeaveLobbyRequestEvent event) {
        Platform.runLater(() -> {
            currentLobbyId = null;
            isHost = false;
            players.clear();
            messages.clear();
            hostControlsPanel.setVisible(false);
            hostControlsPanel.setManaged(false);

            // Refresh lobbies list
            requestLobbies();
        });
    }

    private void handleGameStarted(GameStartedEvent event) {
        Platform.runLater(() -> {
            // Transition to game screen
            sceneManager.switchToScene(SceneManager.SceneType.GAME);
        });
    }

    // Helper Methods
    private void requestLobbies() {
        eventBus.publish(new LobbyListRequestEvent());
    }

    private void showError(String message) {
        errorMessage.setText(message);
        errorMessage.setVisible(true);
    }

    private void clearError() {
        errorMessage.setText("");
        errorMessage.setVisible(false);
    }

    private void addSystemMessage(String message) {
        String formattedMessage = String.format("[%s] %s",
                TIME_FORMATTER.format(LocalDateTime.now()),
                message);
        messages.add(formattedMessage);
    }

    public static class GameLobby {
        private final String id;
        private final javafx.beans.property.StringProperty name;
        private final javafx.beans.property.StringProperty playerCount;
        private final javafx.beans.property.StringProperty status;
        private final javafx.beans.property.StringProperty host;

        public GameLobby(String id, String name, int currentPlayers, int maxPlayers, String status, String host) {
            this.id = id;
            this.name = new javafx.beans.property.SimpleStringProperty(name);
            this.playerCount = new javafx.beans.property.SimpleStringProperty(currentPlayers + "/" + maxPlayers);
            this.status = new javafx.beans.property.SimpleStringProperty(status);
            this.host = new javafx.beans.property.SimpleStringProperty(host);
        }

        public String getId() {
            return id;
        }

        public String getName() {
            return name.get();
        }

        public javafx.beans.property.StringProperty nameProperty() {
            return name;
        }

        public String getPlayerCount() {
            return playerCount.get();
        }

        public javafx.beans.property.StringProperty playerCountProperty() {
            return playerCount;
        }

        public String getStatus() {
            return status.get();
        }

        public javafx.beans.property.StringProperty statusProperty() {
            return status;
        }

        public String getHost() {
            return host.get();
        }

        public javafx.beans.property.StringProperty hostProperty() {
            return host;
        }
    }
}
