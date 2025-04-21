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

/**
 * Controller for the lobby screen.
 * <p>
 * Manages lobby list display, player roster, chat functionality, and lobby actions
 * such as create, join, leave, and start game.
 */
public class LobbyScreenController extends BaseController {
    /**
     * Logger for this class.
     */
    private static final Logger LOGGER = Logger.getLogger(LobbyScreenController.class.getName());

    /**
     * Formatter for timestamps in chat messages and system logs.
     */
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    // Data models
    private final ObservableList<GameLobby> allLobbies = FXCollections.observableArrayList();
    private final ObservableList<String> players = FXCollections.observableArrayList();
    private final ObservableList<String> messages = FXCollections.observableArrayList();
    private FilteredList<GameLobby> filteredLobbies;

    // FXML UI components for lobby management
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

    // FXML UI components for player management
    @FXML
    private ListView<String> playerList;
    @FXML
    private VBox hostControlsPanel;
    @FXML
    private ComboBox<Integer> maxPlayersCombo;

    // FXML UI components for chat
    @FXML
    private ListView<String> chatMessages;
    @FXML
    private TextField chatInput;
    @FXML
    private ToggleButton globalChatButton;
    @FXML
    private ToggleButton lobbyChatButton;

    // State fields
    private String currentLobbyId;
    private boolean isHost = false;
    private String playerName = "Guest";

    /**
     * Constructs the controller and sets up dependencies.
     */
    public LobbyScreenController() {
        super(new ResourceLoader(), UIEventBus.getInstance(), SceneManager.getInstance());
    }

    /**
     * Initializes UI components and event subscriptions after FXML loading.
     */
    @FXML
    private void initialize() {
        LOGGER.info("Initializing lobby screen");
        setupTableView();
        setupChat();
        setupPlayerList();
        setupComboBoxes();
        setupSearchField();
        setupEventHandlers();

        playerNameLabel.setText("Player: " + playerName);
        requestLobbies();
    }

    /**
     * Configures the lobby table view and columns, including double-click join behavior.
     */
    private void setupTableView() {
        nameColumn.setCellValueFactory(cell -> cell.getValue().nameProperty());
        playersColumn.setCellValueFactory(cell -> cell.getValue().playerCountProperty());
        statusColumn.setCellValueFactory(cell -> cell.getValue().statusProperty());
        hostColumn.setCellValueFactory(cell -> cell.getValue().hostProperty());

        filteredLobbies = new FilteredList<>(allLobbies, p -> true);
        lobbyTable.setItems(filteredLobbies);
        lobbyTable.setRowFactory(table -> {
            TableRow<GameLobby> row = new TableRow<>();
            row.setOnMouseClicked(evt -> {
                if (evt.getClickCount() == 2 && !row.isEmpty()) {
                    handleJoinLobby();
                }
            });
            return row;
        });
    }

    /**
     * Sets up chat UI bindings and default chat mode.
     */
    private void setupChat() {
        chatMessages.setItems(messages);
        chatInput.setOnAction(evt -> handleSendChatMessage());
        globalChatButton.setSelected(true);
        lobbyChatButton.setSelected(false);
    }

    /**
     * Binds the player list view to the players data model.
     */
    private void setupPlayerList() {
        playerList.setItems(players);
    }

    /**
     * Populates and configures the max players combo box for lobby hosts.
     */
    private void setupComboBoxes() {
        maxPlayersCombo.setItems(FXCollections.observableArrayList(2, 3, 4, 5, 6, 8));
        maxPlayersCombo.getSelectionModel().select(Integer.valueOf(4));
        maxPlayersCombo.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && isHost && currentLobbyId != null) {
                eventBus.publish(new UpdateLobbySettingsEvent(currentLobbyId, "maxPlayers", newVal.toString()));
            }
        });
    }

    /**
     * Adds filtering behavior to the lobby search field.
     */
    private void setupSearchField() {
        searchField.textProperty().addListener((obs, oldVal, newVal) -> filteredLobbies.setPredicate(lobby -> {
            if (newVal == null || newVal.isEmpty()) {
                return true;
            }
            String filter = newVal.toLowerCase();
            return lobby.getName().toLowerCase().contains(filter) || lobby.getHost().toLowerCase().contains(filter);
        }));
    }

    /**
     * Subscribes to relevant UI events from the event bus.
     */
    private void setupEventHandlers() {
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

    /**
     * Navigates back to the main menu, leaving any joined lobby.
     */
    @FXML
    private void handleBackToMainMenu() {
        if (currentLobbyId != null) {
            eventBus.publish(new LeaveLobbyRequestEvent(currentLobbyId));
        }
        sceneManager.switchToScene(SceneManager.SceneType.MAIN_MENU);
    }

    /**
     * Requests an updated list of lobbies from the server.
     */
    @FXML
    private void handleRefreshLobbies() {
        requestLobbies();
    }

    /**
     * Creates a new lobby with the provided name.
     */
    @FXML
    private void handleCreateLobby() {
        String name = lobbyNameField.getText().trim();
        if (name.isEmpty()) {
            showError("Please enter a lobby name");
            return;
        }
        clearError();
        eventBus.publish(new CreateLobbyRequestEvent(name, playerName));
    }

    /**
     * Attempts to join the selected lobby.
     */
    @FXML
    private void handleJoinLobby() {
        GameLobby lobby = lobbyTable.getSelectionModel().getSelectedItem();
        if (lobby == null) {
            showError("Please select a lobby to join");
            return;
        }
        if ("In Progress".equals(lobby.getStatus())) {
            showError("Cannot join a game in progress");
            return;
        }
        clearError();
        eventBus.publish(new JoinLobbyRequestEvent(lobby.getId(), playerName));
    }

    /**
     * Sends the current chat message to the appropriate channel.
     */
    @FXML
    private void handleSendChatMessage() {
        String msg = chatInput.getText().trim();
        if (msg.isEmpty()) return;

        if (globalChatButton.isSelected()) {
            eventBus.publish(new GlobalChatEvent(msg, GlobalChatEvent.ChatType.GLOBAL));
        } else if (lobbyChatButton.isSelected()) {
            if (currentLobbyId != null) {
                eventBus.publish(new LobbyChatEvent(currentLobbyId, msg));
            } else {
                addSystemMessage("Cannot send lobby message: You're not in a lobby.");
            }
        } else {
            LOGGER.warning("No chat type selected, defaulting to GLOBAL");
            eventBus.publish(new GlobalChatEvent(msg, GlobalChatEvent.ChatType.GLOBAL));
        }
        chatInput.clear();
    }

    /**
     * Starts the game if the user is the host and enough players are present.
     */
    @FXML
    private void handleStartGame() {
        if (isHost && currentLobbyId != null) {
            if (players.size() < 2) {
                showError("Need at least 2 players to start");
                return;
            }
            eventBus.publish(new StartGameRequestEvent(currentLobbyId));
        }
    }

    /**
     * Switches to global chat mode.
     */
    @FXML
    private void handleGlobalChatSelect() {
        LOGGER.info("Global chat selected");
        globalChatButton.setSelected(true);
        lobbyChatButton.setSelected(false);
    }

    /**
     * Switches to lobby chat mode and warns if not in a lobby.
     */
    @FXML
    private void handleLobbyChatSelect() {
        LOGGER.info("Lobby chat selected");
        globalChatButton.setSelected(false);
        lobbyChatButton.setSelected(true);
        if (currentLobbyId == null) {
            addSystemMessage("You are not in a lobby. Messages will not be sent until you join one.");
        }
    }

    /**
     * Processes the name change response event.
     *
     * @param event the name change response event
     */
    private void handleNameChangeResponse(NameChangeResponseEvent event) {
        if (event == null) {
            LOGGER.warning("Received null name change response event");
            return;
        }
        Platform.runLater(() -> {
            if (event.isSuccess()) {
                playerName = event.getNewName();
                Logger.getGlobal().info("Player name changed: " + playerName);
                addSystemMessage("Name changed to: " + playerName);
            } else {
                addSystemMessage("Failed to change name: " + event.getMessage());
            }
        });
    }

    /**
     * Updates the lobby list with data from the server.
     *
     * @param event the lobby list response event
     */
    private void handleLobbyListResponse(LobbyListResponseEvent event) {
        Platform.runLater(() -> {
            allLobbies.setAll(event.getLobbies());
        });
    }

    /**
     * Handles post-join lobby UI updates.
     *
     * @param event the lobby joined event
     */
    private void handleLobbyJoined(LobbyJoinedEvent event) {
        Platform.runLater(() -> {
            currentLobbyId = event.getLobbyId();
            isHost = event.isHost();
            hostControlsPanel.setVisible(isHost);
            hostControlsPanel.setManaged(isHost);
            players.setAll(event.getPlayers());
            messages.clear();
            addSystemMessage("You joined the lobby");
        });
    }

    /**
     * Appends a global chat message to the chat view.
     *
     * @param event the global chat event
     */
    private void handleGlobalChatMessage(GlobalChatEvent event) {
        Platform.runLater(() -> {
            String formatted = String.format("[%s] %s: %s", TIME_FORMATTER.format(event.getTimestamp()), event.getSender(), event.getContent());
            messages.add(formatted);
        });
    }

    /**
     * Appends a lobby chat message to the chat view.
     *
     * @param event the lobby chat event
     */
    private void handleLobbyChatMessage(LobbyChatEvent event) {
        Platform.runLater(() -> {
            String formatted = String.format("[%s] %s: %s", TIME_FORMATTER.format(event.getTimestamp()), event.getSender(), event.getMessage());
            messages.add(formatted);
        });
    }

    /**
     * Adds a player to the roster and logs the join.
     *
     * @param event the player joined lobby event
     */
    private void handlePlayerJoinedLobby(PlayerJoinedLobbyEvent event) {
        Platform.runLater(() -> {
            String name = event.getPlayerName();
            if (!players.contains(name)) {
                players.add(name);
                addSystemMessage(name + " joined the lobby");
            }
        });
    }

    /**
     * Removes a player from the roster and logs the leave.
     *
     * @param event the lobby left event
     */
    private void handleLobbyLeft(LobbyLeftEvent event) {
        Platform.runLater(() -> {
            players.remove(event.getPlayerName());
            addSystemMessage(event.getPlayerName() + " left the lobby");
        });
    }

    /**
     * Displays an error message from an ErrorEvent.
     *
     * @param event the error event
     */
    private void handleError(ErrorEvent event) {
        Platform.runLater(() -> showError(event.getErrorMessage()));
    }

    /**
     * Cleans up UI after leaving a lobby and refreshes the lobby list.
     *
     * @param event the leave lobby request event
     */
    private void handleLeaveLobby(LeaveLobbyRequestEvent event) {
        Platform.runLater(() -> {
            currentLobbyId = null;
            isHost = false;
            players.clear();
            messages.clear();
            hostControlsPanel.setVisible(false);
            hostControlsPanel.setManaged(false);
            requestLobbies();
        });
    }

    /**
     * Switches to the game screen when the game starts.
     *
     * @param event the game started event
     */
    private void handleGameStarted(GameStartedEvent event) {
        Platform.runLater(() -> sceneManager.switchToScene(SceneManager.SceneType.GAME));
    }

    /**
     * Sends a request for the current lobby list.
     */
    private void requestLobbies() {
        eventBus.publish(new LobbyListRequestEvent());
    }

    /**
     * Shows an error message in the UI.
     *
     * @param message the error text to display
     */
    private void showError(String message) {
        errorMessage.setText(message);
        errorMessage.setVisible(true);
    }

    /**
     * Clears any existing error message from the UI.
     */
    private void clearError() {
        errorMessage.setText("");
        errorMessage.setVisible(false);
    }

    /**
     * Adds a system log message to the chat view with timestamp.
     *
     * @param message the system message content
     */
    private void addSystemMessage(String message) {
        String formatted = String.format("[%s] %s", TIME_FORMATTER.format(LocalDateTime.now()), message);
        messages.add(formatted);
    }

    /**
     * Represents a game lobby displayed in the lobby table.
     */
    public static class GameLobby {
        private final String id;
        private final javafx.beans.property.StringProperty name;
        private final javafx.beans.property.StringProperty playerCount;
        private final javafx.beans.property.StringProperty status;
        private final javafx.beans.property.StringProperty host;

        /**
         * Constructs a GameLobby instance.
         *
         * @param id             unique lobby identifier
         * @param name           display name of the lobby
         * @param currentPlayers current player count
         * @param maxPlayers     maximum player capacity
         * @param status         current lobby status
         * @param host           name of the lobby host
         */
        public GameLobby(String id, String name, int currentPlayers, int maxPlayers, String status, String host) {
            this.id = id;
            this.name = new javafx.beans.property.SimpleStringProperty(name);
            this.playerCount = new javafx.beans.property.SimpleStringProperty(currentPlayers + "/" + maxPlayers);
            this.status = new javafx.beans.property.SimpleStringProperty(status);
            this.host = new javafx.beans.property.SimpleStringProperty(host);
        }

        /**
         * @return the lobby ID
         */
        public String getId() {
            return id;
        }

        /**
         * @return the lobby name property
         */
        public javafx.beans.property.StringProperty nameProperty() {
            return name;
        }

        /**
         * @return the lobby name
         */
        public String getName() {
            return name.get();
        }

        /**
         * @return the player count property
         */
        public javafx.beans.property.StringProperty playerCountProperty() {
            return playerCount;
        }

        /**
         * @return the player count text
         */
        public String getPlayerCount() {
            return playerCount.get();
        }

        /**
         * @return the status property
         */
        public javafx.beans.property.StringProperty statusProperty() {
            return status;
        }

        /**
         * @return the status text
         */
        public String getStatus() {
            return status.get();
        }

        /**
         * @return the host property
         */
        public javafx.beans.property.StringProperty hostProperty() {
            return host;
        }

        /**
         * @return the host name
         */
        public String getHost() {
            return host.get();
        }
    }
}
