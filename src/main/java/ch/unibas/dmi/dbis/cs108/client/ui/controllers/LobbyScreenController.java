package ch.unibas.dmi.dbis.cs108.client.ui.controllers;

import ch.unibas.dmi.dbis.cs108.client.app.GameApplication;
import ch.unibas.dmi.dbis.cs108.client.core.PlayerIdentityManager;
import ch.unibas.dmi.dbis.cs108.client.networking.events.ConnectionEvent;
import ch.unibas.dmi.dbis.cs108.client.ui.events.admin.ChangeNameUIEvent;
import ch.unibas.dmi.dbis.cs108.client.ui.events.admin.ConnectionStatusEvent;
import ch.unibas.dmi.dbis.cs108.shared.game.Player;
import ch.unibas.dmi.dbis.cs108.client.ui.SceneManager;
import ch.unibas.dmi.dbis.cs108.client.ui.components.ChatComponent;
import ch.unibas.dmi.dbis.cs108.client.ui.components.SettingsDialog;
import ch.unibas.dmi.dbis.cs108.client.ui.events.ErrorEvent;
import ch.unibas.dmi.dbis.cs108.client.ui.events.UIEventBus;
import ch.unibas.dmi.dbis.cs108.client.ui.events.admin.NameChangeRequestEvent;
import ch.unibas.dmi.dbis.cs108.client.ui.events.admin.NameChangeResponseEvent;
import ch.unibas.dmi.dbis.cs108.client.ui.events.lobby.*;
import ch.unibas.dmi.dbis.cs108.client.ui.utils.ResourceLoader;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Priority;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Controller for the lobby screen.
 * Manages the display and interaction with game lobbies, including lobby list,
 * player list, host controls, and chat.
 */
public class LobbyScreenController extends BaseController {
    private static final Logger LOGGER = Logger.getLogger(LobbyScreenController.class.getName());

    private final ObservableList<GameLobby> allLobbies = FXCollections.observableArrayList();
    private final ObservableList<String> playersInCurrentLobby = FXCollections.observableArrayList();
    private FilteredList<GameLobby> filteredLobbies;

    @FXML
    private BorderPane rootPane; // Add reference to the root pane
    @FXML
    private Label playerNameLabel;
    @FXML
    private Label connectionStatus;
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
    @FXML
    private Button leaveLobbyButton;
    @FXML
    private ListView<String> playerList;
    @FXML
    private VBox hostControlsPanel;
    @FXML
    private ComboBox<Integer> maxPlayersCombo;
    @FXML
    private ComboBox<Integer> maxPlayersHostCombo;
    @FXML
    private VBox chatContainer;
    @FXML
    private Button createLobbyButton;
    @FXML
    private Button startGameButton;

    private String currentLobbyId;
    private boolean isHost = false;
    private Player localPlayer;
    private PlayerIdentityManager playerManager;
    private ChatComponent chatComponentController;
    private SettingsDialog settingsDialog; // Declare SettingsDialog
    private final AtomicBoolean isConnected = new AtomicBoolean(false);

    /**
     * Constructs the controller, injecting dependencies via the BaseController.
     */
    public LobbyScreenController() {
        super(new ResourceLoader(), UIEventBus.getInstance(), SceneManager.getInstance());
        // REMOVED: this.localPlayer = new Player(System.getProperty("user.name",
        // "Guest"));
        LOGGER.finer("LobbyScreenController instance created.");
    }

    /**
     * Initializes the controller after FXML loading.
     */
    @FXML
    private void initialize() {
        LOGGER.info("Initializing LobbyScreenController...");
        try {
            this.localPlayer = GameApplication.getLocalPlayer(); // Fetch player instance
            if (this.localPlayer == null) {
                LOGGER.severe("LocalPlayer is null during LobbyScreenController initialization!");
                // Handle error appropriately
                this.localPlayer = new Player("ErrorGuest"); // Fallback
            }
            isConnected.set(true); // When LobbyScreen gets initialized, we are connected, because it wouldnt get shown if not connected.
            setupLobbyTable();
            setupPlayerList();
            setupLobbyControls();
            setupHostControls();
            setupSearchFilter();
            setupChatComponent(); // Call after localPlayer is set
            setupSettingsDialog(); // Call after localPlayer is set
            setupEventHandlers();
            setupButtonStyles();
            playerNameLabel.setText("Player: " + localPlayer.getName()); // Update label
            leaveLobbyButton.setDisable(true); // Disabled by default until joining a lobby
            errorMessage.setVisible(false);
            errorMessage.setManaged(false);
            requestLobbyList();

            playerManager = PlayerIdentityManager.getInstance();
            localPlayer = playerManager.getLocalPlayer();
            playerManager.addPlayerUpdateListener(this::handlePlayerUpdate);

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Critical error during LobbyScreenController initialization", e);
            showError("Failed to initialize lobby screen. Please try returning to the main menu.");
            lobbyTable.setDisable(true);
            lobbyNameField.setDisable(true);
        }
        LOGGER.info("LobbyScreenController initialization complete.");
    }

    /**
     * Configures the lobby table view, columns, data binding, and row factory for
     * double-click joining.
     */
    private void setupLobbyTable() {
        nameColumn.setCellValueFactory(cellData -> cellData.getValue().nameProperty());
        playersColumn.setCellValueFactory(cellData -> cellData.getValue().playerCountProperty());
        statusColumn.setCellValueFactory(cellData -> cellData.getValue().statusProperty());
        hostColumn.setCellValueFactory(cellData -> cellData.getValue().hostProperty());
        filteredLobbies = new FilteredList<>(allLobbies, p -> true);
        lobbyTable.setItems(filteredLobbies);
        lobbyTable.setRowFactory(tv -> {
            TableRow<GameLobby> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (!row.isEmpty() && event.getClickCount() == 2) {
                    handleJoinLobby();
                }
            });
            return row;
        });
        lobbyTable.setPlaceholder(new Label("No lobbies available. Create one or refresh."));
    }

    /**
     * Binds the player list view to the observable list of players in the current
     * lobby.
     */
    private void setupPlayerList() {
        playerList.setItems(playersInCurrentLobby);
        playerList.setPlaceholder(new Label("No players in lobby."));
    }

    /**
     * Configures the host-specific controls, like the max players combo box.
     */
    private void setupLobbyControls() {
        maxPlayersCombo.setItems(FXCollections.observableArrayList(2, 3, 4, 5, 6, 8));
        maxPlayersCombo.getSelectionModel().select(Integer.valueOf(4));
        maxPlayersCombo.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && isHost && currentLobbyId != null && !newVal.equals(oldVal)) {
                LOGGER.info("Host changed max players to: " + newVal);
                eventBus.publish(new UpdateLobbySettingsEvent(currentLobbyId, "maxPlayers", newVal.toString()));
            }
        });
        hostControlsPanel.setVisible(false);
        hostControlsPanel.setManaged(false);
    }

    /**
     * Configures the host controls, including showing the max players combo box.
     */
    private void setupHostControls() {
        // Set up max players display in host controls (read-only)
        maxPlayersHostCombo.setItems(FXCollections.observableArrayList(2, 3, 4, 5, 6, 8));
        maxPlayersHostCombo.getSelectionModel().select(maxPlayersCombo.getValue());
        maxPlayersHostCombo.setDisable(true); // Make it non-editable

        maxPlayersHostCombo.setVisible(false);
        maxPlayersHostCombo.setManaged(false);
    }

    /**
     * Sets up the search field to filter the lobby table in real-time.
     */
    private void setupSearchFilter() {
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredLobbies.setPredicate(lobby -> {
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }
                String lowerCaseFilter = newValue.toLowerCase();
                return lobby.getName().toLowerCase().contains(lowerCaseFilter) ||
                        lobby.getHost().toLowerCase().contains(lowerCaseFilter);
            });
            lobbyTable.refresh();
        });
    }

    /**
     * Initializes the injected ChatComponent controller.
     */
    private void setupChatComponent() {
        chatContainer.getChildren().clear();
        chatComponentController = new ChatComponent();
        Node chatView = chatComponentController.getView(); // Get the view Node
        chatContainer.getChildren().add(chatView);
        VBox.setVgrow(chatView, Priority.ALWAYS); // Make the chat component grow vertically
        if (localPlayer != null) { // Ensure localPlayer is set
            chatComponentController.setPlayer(localPlayer);
        } else {
            LOGGER.warning("Cannot set player in ChatComponent: localPlayer is null.");
        }
        chatComponentController.setCurrentLobbyId(null);
        chatComponentController.addSystemMessage("Lobby system initialized. Select or create a lobby.");
    }

    /**
     * Initializes the SettingsDialog and adds it to the root pane.
     */
    private void setupSettingsDialog() {
        settingsDialog = new SettingsDialog();
        if (rootPane == null) {
            LOGGER.warning("Root pane is null, cannot add SettingsDialog.");
            return;
        }

        if (localPlayer != null) { // Ensure localPlayer is set
            settingsDialog.playerNameProperty().set(localPlayer.getName());
        } else {
            LOGGER.warning("Cannot set initial player name in settings: localPlayer is null.");
            settingsDialog.playerNameProperty().set("ErrorGuest"); // Fallback
        }
        settingsDialog.setOnSaveAction(this::handleSettingsSave);
        settingsDialog.setConnectionStatus(isConnected.get(), isConnected.get() ? "Connected" : "Disconnected");
    }

    /**
     * Sets up the button styles based on the current state of the lobby name field
     * and player count.
     */
    private void setupButtonStyles() {
        // Create lobby button style updates based on lobby name field
        lobbyNameField.textProperty().addListener((observable, oldValue, newValue) -> {
            updateCreateLobbyButtonStyle();
        });

        // Start game button style updates based on player count
        playersInCurrentLobby.addListener((ListChangeListener<String>) change -> {
            updateStartGameButtonStyle();
        });
    }

    /**
     * Subscribes to relevant events from the UIEventBus.
     */
    private void setupEventHandlers() {
        eventBus.subscribe(LobbyListResponseEvent.class, this::handleLobbyListResponse);
        eventBus.subscribe(LobbyJoinedEvent.class, this::handleLobbyJoined);
        eventBus.subscribe(PlayerJoinedLobbyEvent.class, this::handlePlayerJoinedLobby);
        eventBus.subscribe(PlayerLeftLobbyEvent.class, this::handlePlayerLeftLobby);
        eventBus.subscribe(LobbyLeftEvent.class, this::handleSelfLeftLobby);
        eventBus.subscribe(GameStartedEvent.class, this::handleGameStarted);
        eventBus.subscribe(LobbyUpdateEvent.class, this::handleLobbyUpdate);
        eventBus.subscribe(ErrorEvent.class, this::handleError);
        eventBus.subscribe(NameChangeResponseEvent.class, this::handleNameChangeResponse);
        eventBus.subscribe(ConnectionStatusEvent.class, this::handleConnectionStatus);
    }

    /**
     * Handles the "Back" button click. Leaves the current lobby (if any) and
     * navigates back to the main menu scene.
     */
    @FXML
    private void handleBackToMainMenu() {
        LOGGER.info("Back to Main Menu button clicked.");
        leaveCurrentLobby();
        sceneManager.switchToScene(SceneManager.SceneType.MAIN_MENU);
    }

    /**
     * Handles the "Leave Lobby" button click. Leaves the current lobby and
     * refreshes the lobby list.
     */
    @FXML
    private void handleLeaveLobby() {
        LOGGER.info("Leave Lobby button clicked.");
        leaveCurrentLobby();
        // Don't switch to main menu, just stay on the lobby screen
    }

    /**
     * Handles the "Refresh" button click. Requests an updated list of lobbies.
     */
    @FXML
    private void handleRefreshLobbies() {
        LOGGER.fine("Refresh Lobbies button clicked.");
        requestLobbyList();
        clearError();
    }

    /**
     * Handles the "Create Lobby" button click. Validates the lobby name and
     * publishes a request to create a new lobby.
     */
    @FXML
    private void handleCreateLobby() {
        String name = lobbyNameField.getText().trim();
        LOGGER.info("Create Lobby button clicked with name: '" + name + "'");
        if (name.isEmpty()) {
            showError("Please enter a name for the lobby.");
            return;
        }
        if (name.length() > 30) {
            showError("Lobby name cannot exceed 30 characters.");
            return;
        }
        // Get selected max players value
        Integer maxPlayers = maxPlayersCombo.getValue();
        if (maxPlayers == null) {
            maxPlayers = 4; // Fallback if somehow not selected
        }
        clearError();
        eventBus.publish(new CreateLobbyRequestEvent(name, localPlayer.getName(), maxPlayers));
        lobbyNameField.clear();
        requestLobbyList();
    }

    /**
     * Handles the "Join Lobby" button click (or double-click on table row).
     */
    @FXML
    private void handleJoinLobby() {
        GameLobby selectedLobby = lobbyTable.getSelectionModel().getSelectedItem();
        LOGGER.fine("Join Lobby action triggered.");
        if (selectedLobby == null) {
            showError("Please select a lobby from the list to join.");
            return;
        }
        LOGGER.info("Attempting to join lobby: " + selectedLobby.getName() + " (ID: " + selectedLobby.getId() + ")");
        if (currentLobbyId != null) {
            showError("You are already in a lobby. Leave it first to join another.");
            return;
        }
        if ("In Progress".equalsIgnoreCase(selectedLobby.getStatus())) {
            showError("Cannot join '" + selectedLobby.getName() + "': Game is already in progress.");
            return;
        }
        clearError();
        eventBus.publish(new JoinLobbyRequestEvent(selectedLobby.getId(), localPlayer.getName()));
        requestLobbyList();
    }

    /**
     * Handles the "Start Game" button click (host only).
     */
    @FXML
    private void handleStartGame() {
        LOGGER.info("Start Game button clicked.");
        if (!isHost) {
            showError("Only the lobby host can start the game.");
            LOGGER.warning("Non-host attempted to start the game.");
            return;
        }
        if (currentLobbyId == null) {
            showError("Cannot start game: Not currently in a lobby.");
            LOGGER.warning("Attempted to start game while not in a lobby.");
            return;
        }
        int minPlayers = 2;
        if (playersInCurrentLobby.size() < minPlayers) {
            showError("Need at least " + minPlayers + " players to start the game.");
            return;
        }
        clearError();
        LOGGER.info("Host is starting the game for lobby: " + currentLobbyId);
        eventBus.publish(new StartGameRequestEvent(currentLobbyId));
    }

    /**
     * Handles the response containing the list of available lobbies.
     */
    private void handleLobbyListResponse(LobbyListResponseEvent event) {
        Objects.requireNonNull(event, "LobbyListResponseEvent cannot be null");
        Platform.runLater(() -> {
            List<GameLobby> lobbies = event.getLobbies().stream()
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
            allLobbies.setAll(lobbies);
            lobbyTable.refresh();
            LOGGER.info("Lobby list updated with " + lobbies.size() + " lobbies.");
        });
    }

    /**
     * Handles the confirmation that the player has successfully joined a lobby.
     */
    private void handleLobbyJoined(LobbyJoinedEvent event) {
        Objects.requireNonNull(event, "LobbyJoinedEvent cannot be null");
        Platform.runLater(() -> {
            currentLobbyId = event.getLobbyId();
            GameApplication.setCurrentLobbyId(currentLobbyId);
            isHost = event.isHost();
            // Update the central player instance if the name from the server differs
            if (localPlayer != null && !localPlayer.getName().equals(event.getPlayerName())) {
                localPlayer.setName(event.getPlayerName());
                LOGGER.info("Local player name updated by server on join: " + localPlayer.getName());
                playerNameLabel.setText("Player: " + localPlayer.getName()); // Update label
                if (settingsDialog != null) {
                    settingsDialog.playerNameProperty().set(localPlayer.getName()); // Update settings dialog
                }
            } else if (localPlayer == null) {
                LOGGER.severe("Cannot update player name on join: localPlayer is null.");
            }

            LOGGER.info("Successfully joined lobby: " + currentLobbyId + " (Host: " + isHost + ")");
            // playerNameLabel.setText("Player: " + localPlayer.getName()); // Already
            // updated above if needed
            playersInCurrentLobby.setAll(event.getPlayers());
            hostControlsPanel.setVisible(isHost);
            hostControlsPanel.setManaged(isHost);
            updateStartGameButtonStyle();
            leaveLobbyButton.setDisable(false);
            lobbyNameField.clear();
            clearError();
            if (chatComponentController != null) {
                if (localPlayer != null) { // Ensure player is set before updating chat
                    chatComponentController.setPlayer(localPlayer);
                }
                chatComponentController.setCurrentLobbyId(currentLobbyId); // Set lobby ID for chat
                chatComponentController.addSystemMessage("You joined lobby: " + event.getLobbyName());
            }
            lobbyNameField.setDisable(true);
            lobbyTable.getSelectionModel().clearSelection();
        });
        requestLobbyList();
    }

    /**
     * Handles notification that another player has joined the current lobby.
     */
    private void handlePlayerJoinedLobby(PlayerJoinedLobbyEvent event) {
        Objects.requireNonNull(event, "PlayerJoinedLobbyEvent cannot be null");
        if (currentLobbyId != null && currentLobbyId.equals(event.getLobbyId())) {
            Platform.runLater(() -> {
                String joinedPlayerName = event.getPlayerName();
                if (!playersInCurrentLobby.contains(joinedPlayerName)) {
                    playersInCurrentLobby.add(joinedPlayerName);
                    LOGGER.info(joinedPlayerName + " joined the lobby.");
                    if (chatComponentController != null) {
                        chatComponentController.addSystemMessage(joinedPlayerName + " joined the lobby.");
                    }
                    updateLobbyPlayerCountInTable(currentLobbyId, playersInCurrentLobby.size());
                } else {
                    LOGGER.warning("Received PlayerJoinedLobbyEvent for player already in list: " + joinedPlayerName);
                }
            });
        }
        requestLobbyList();
    }

    /**
     * Handles notification that another player has left the current lobby.
     */
    private void handlePlayerLeftLobby(PlayerLeftLobbyEvent event) {
        Objects.requireNonNull(event, "PlayerLeftLobbyEvent cannot be null");
        if (currentLobbyId != null && currentLobbyId.equals(event.getLobbyId())) {
            Platform.runLater(() -> {
                String leftPlayerName = event.getPlayerName();
                if (playersInCurrentLobby.remove(leftPlayerName)) {
                    LOGGER.info(leftPlayerName + " left the lobby.");
                    if (chatComponentController != null) {
                        chatComponentController.addSystemMessage(leftPlayerName + " left the lobby.");
                    }
                    updateLobbyPlayerCountInTable(currentLobbyId, playersInCurrentLobby.size());
                } else {
                    LOGGER.warning("Received PlayerLeftLobbyEvent for player not in list: " + leftPlayerName);
                }
            });
        }
        requestLobbyList();
    }

    /**
     * Handles confirmation that the current player has left the lobby.
     */
    private void handleSelfLeftLobby(LobbyLeftEvent event) {
        Objects.requireNonNull(event, "LobbyLeftEvent cannot be null");
        if (currentLobbyId != null && currentLobbyId.equals(event.getLobbyId())) {
            Platform.runLater(() -> {
                LOGGER.info("Left lobby: " + currentLobbyId);
                resetLobbyState();
                requestLobbyList();
                if (chatComponentController != null) {
                    chatComponentController.addSystemMessage("You left the lobby.");
                }
            });
        } else {
            LOGGER.warning("Received SelfLeftLobby event for a lobby mismatch. Current: " + currentLobbyId + ", Event: "
                    + event.getLobbyId());
            Platform.runLater(this::resetLobbyState);
        }
        requestLobbyList();
    }

    /**
     * Handles the updating of the player name in the lobbyScreen.
     *
     * @param updatedPlayer
     */
    private void handlePlayerUpdate(Player updatedPlayer) {
        this.localPlayer = updatedPlayer;
        if (chatComponentController != null) {
            chatComponentController.setPlayer(localPlayer);
        }
    }

    /**
     * Handles notification that the game has started for the current lobby.
     */
    private void handleGameStarted(GameStartedEvent event) {
        // Objects.requireNonNull(event, "GameStart edEvent cannot be null");
        LOGGER.info("Game started for lobby: " + currentLobbyId + ". Switching to game screen.");
        GameApplication.setPlayers(playersInCurrentLobby.stream().toList());
        Platform.runLater(() -> {
            sceneManager.switchToScene(SceneManager.SceneType.GAME);
        });
    }

    /**
     * Handles updates to lobby information (e.g., status change, player count).
     */
    private void handleLobbyUpdate(LobbyUpdateEvent event) {
        Objects.requireNonNull(event, "LobbyUpdateEvent cannot be null");
        Platform.runLater(() -> {
            String updatedLobbyId = event.getLobbyId();
            LOGGER.fine("Received update for lobby: " + updatedLobbyId);
            allLobbies.stream()
                    .filter(lobby -> lobby.getId().equals(updatedLobbyId))
                    .findFirst()
                    .ifPresent(lobby -> {
                        if (event.getNewStatus() != null) {
                            lobby.setStatus(event.getNewStatus());
                        }
                        if (event.getCurrentPlayers() >= 0 && event.getMaxPlayers() > 0) {
                            lobby.setPlayerCount(event.getCurrentPlayers(), event.getMaxPlayers());
                        }
                        lobbyTable.refresh();
                        LOGGER.fine("Updated lobby details for: " + updatedLobbyId);
                    });
        });
    }

    /**
     * Handles generic ErrorEvent updates. Displays the error message.
     */
    private void handleError(ErrorEvent event) {
        Objects.requireNonNull(event, "ErrorEvent cannot be null");
        Platform.runLater(() -> {
            String msg = event.getErrorMessage();
            LOGGER.warning("Received error: " + msg);
            showError(msg);
            if (chatComponentController != null) {
                chatComponentController.addSystemMessage("Error: " + msg);
            }
        });
    }

    /**
     * Handles the response from a player name change request.
     */
    private void handleNameChangeResponse(NameChangeResponseEvent event) {
        Objects.requireNonNull(event, "NameChangeResponseEvent cannot be null");
        Platform.runLater(() -> {
            if (event.isSuccess()) {
                playerManager.updatePlayerName(event.getNewName());
                String oldName = localPlayer != null ? localPlayer.getName() : "Unknown";
                String newName = event.getNewName();
                // Update the central player instance
                if (localPlayer != null) {
                    localPlayer.setName(newName);
                    GameApplication.setLocalPlayer(localPlayer); // Update the static reference
                    LOGGER.info("Player name successfully changed to: " + localPlayer.getName());
                    playerNameLabel.setText("Player: " + localPlayer.getName()); // Update label
                    if (settingsDialog != null) {
                        settingsDialog.playerNameProperty().set(newName); // Update dialog as well
                    }
                    if (chatComponentController != null) {
                        chatComponentController.setPlayer(localPlayer); // Update chat component
                        chatComponentController
                                .addSystemMessage("Name successfully changed to: " + localPlayer.getName());
                    }
                    // Update host name in lobby list if this player was a host
                    allLobbies.stream()
                            .filter(lobby -> lobby.getHost().equals(oldName))
                            .forEach(lobby -> lobby.setHost(newName));
                    lobbyTable.refresh();
                } else {
                    LOGGER.severe("Cannot update player name: localPlayer is null.");
                }
            } else {
                String failureMsg = event.getMessage() != null ? event.getMessage() : "Unknown reason.";
                LOGGER.warning("Failed to change player name: " + failureMsg);
                showError("Failed to change name: " + failureMsg);
                if (settingsDialog != null && localPlayer != null) {
                    // Revert name in dialog if change failed
                    settingsDialog.playerNameProperty().set(localPlayer.getName());
                }
                if (chatComponentController != null) {
                    chatComponentController.addSystemMessage("Failed to change name: " + failureMsg);
                }
            }
        });
    }

    /**
     * Handles ConnectionStatusEvent updates from the network layer.
     *
     * @param event The connection status event.
     */
    private void handleConnectionStatus(ConnectionStatusEvent event) {
        Objects.requireNonNull(event, "ConnectionStatusEvent cannot be null");
        Platform.runLater(() -> {
            boolean currentlyConnected = event.getState() == ConnectionEvent.ConnectionState.CONNECTED;
            boolean wasConnected = isConnected.getAndSet(currentlyConnected);
            settingsDialog.setConnectionStatus(currentlyConnected, currentlyConnected ? "Connected" : "Disconnected");
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
        });
    }

    /**
     * Sends a request to the server (via event bus) to get the latest lobby list.
     */
    private void requestLobbyList() {
        LOGGER.fine("Requesting updated lobby list...");
        eventBus.publish(new LobbyListRequestEvent());
    }

    /**
     * Sends a request to the server (via event bus) for the player to leave the
     * current lobby.
     */
    private void leaveCurrentLobby() {
        if (currentLobbyId != null) {
            LOGGER.info("Requesting to leave lobby: " + currentLobbyId);
            eventBus.publish(new LeaveLobbyRequestEvent(currentLobbyId));
        }
    }

    /**
     * Resets the UI and internal state related to being in a lobby.
     */
    private void resetLobbyState() {
        currentLobbyId = null;
        isHost = false;
        playersInCurrentLobby.clear();
        hostControlsPanel.setVisible(false);
        hostControlsPanel.setManaged(false);
        leaveLobbyButton.setDisable(true);
        clearError();
        lobbyNameField.setDisable(false);
        if (chatComponentController != null) {
            chatComponentController.setCurrentLobbyId(null);
        }
        LOGGER.fine("Lobby state reset.");
    }

    /**
     * Updates the player count display for a specific lobby in the table.
     *
     * @param lobbyId        The ID of the lobby to update.
     * @param newPlayerCount The new current player count.
     */
    private void updateLobbyPlayerCountInTable(String lobbyId, int newPlayerCount) {
        allLobbies.stream()
                .filter(lobby -> lobby.getId().equals(lobbyId))
                .findFirst()
                .ifPresent(lobby -> {
                    int maxPlayers = lobby.getMaxPlayers();
                    if (maxPlayers > 0) {
                        lobby.setPlayerCount(newPlayerCount, maxPlayers);
                        lobbyTable.refresh();
                    }
                });
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
     * Displays an error message in the dedicated error label.
     *
     * @param message The error message to display.
     */
    private void showError(String message) {
        if (message == null || message.trim().isEmpty()) {
            clearError();
            return;
        }
        errorMessage.setText(message);
        errorMessage.setVisible(true);
        errorMessage.setManaged(true);
        LOGGER.warning("Displaying error: " + message);
    }

    /**
     * Clears the error message label.
     */
    private void clearError() {
        errorMessage.setText("");
        errorMessage.setVisible(false);
        errorMessage.setManaged(false);
    }

    /**
     * Handles the "Settings" button click. Shows the settings dialog.
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
        settingsDialog.setConnectionStatus(isConnected.get(), isConnected.get() ? "Connected" : "Disconnected");

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

        showDialogAsOverlay(settingsDialog, rootPane);
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
        eventBus.publish(new ChangeNameUIEvent(newName));
    }

    /**
     * Handles the save action from the SettingsDialog.
     * Checks if the player name has changed and sends an update request.
     */
    private void handleSettingsSave() {
        LOGGER.fine("Settings save action triggered.");
        if (settingsDialog == null) {
            LOGGER.warning("SettingsDialog is null during save action.");
            return;
        }
        String newName = settingsDialog.playerNameProperty().get().trim();
        if (localPlayer != null && !newName.isEmpty() && !newName.equals(localPlayer.getName())) {
            LOGGER.info("Requesting name change from settings dialog to: " + newName);
            eventBus.publish(new NameChangeRequestEvent(newName));
        } else if (localPlayer != null && newName.isEmpty()) {
            LOGGER.warning("Attempted to save empty player name from settings.");
            showError("Player name cannot be empty.");
            settingsDialog.playerNameProperty().set(localPlayer.getName()); // Revert in dialog
        }
        // Audio settings could be handled here if implemented
    }

    /**
     * Updates the style of the "Create Lobby" button based on the lobby name field
     * input.
     */
    private void updateCreateLobbyButtonStyle() {
        String name = lobbyNameField.getText().trim();
        boolean isValid = !name.isEmpty() && name.length() <= 30;

        if (isValid) {
            createLobbyButton.setStyle("-fx-background-color: #5cb85c;"); // Bootstrap-like green
        } else {
            createLobbyButton.setStyle(""); // Reset to default style
        }
    }

    /**
     * Updates the style of the "Start Game" button based on the current lobby
     * conditions.
     */
    private void updateStartGameButtonStyle() {
        int minPlayers = 2;
        boolean isValid = isHost && currentLobbyId != null && playersInCurrentLobby.size() >= minPlayers;

        if (isValid) {
            startGameButton.setStyle("-fx-background-color: #5cb85c;"); // Bootstrap-like green
        } else {
            startGameButton.setStyle(""); // Reset to default style
        }
    }

    /**
     * Cleans up resources used by this controller, primarily unsubscribing from
     * events.
     */
    public void cleanup() {
        LOGGER.info("Cleaning up LobbyScreenController resources...");
        eventBus.unsubscribe(LobbyListResponseEvent.class, this::handleLobbyListResponse);
        eventBus.unsubscribe(LobbyJoinedEvent.class, this::handleLobbyJoined);
        eventBus.unsubscribe(PlayerJoinedLobbyEvent.class, this::handlePlayerJoinedLobby);
        eventBus.unsubscribe(PlayerLeftLobbyEvent.class, this::handlePlayerLeftLobby);
        eventBus.unsubscribe(LobbyLeftEvent.class, this::handleSelfLeftLobby);
        eventBus.unsubscribe(GameStartedEvent.class, this::handleGameStarted);
        eventBus.unsubscribe(LobbyUpdateEvent.class, this::handleLobbyUpdate);
        eventBus.unsubscribe(ErrorEvent.class, this::handleError);
        eventBus.unsubscribe(NameChangeResponseEvent.class, this::handleNameChangeResponse);
        eventBus.unsubscribe(ConnectionStatusEvent.class, this::handleConnectionStatus);

        if (chatComponentController != null) {
            chatComponentController.cleanup();
        }
        if (settingsDialog != null) {
            settingsDialog.close(); // Ensure dialog is closed/removed
        }
        LOGGER.info("LobbyScreenController cleanup finished.");
    }

    /**
     * Represents a game lobby entry displayed in the lobby table.
     * Uses JavaFX properties for easy binding with TableView columns.
     */
    public static class GameLobby {
        private final String id;
        private final StringProperty name;
        private final StringProperty playerCount;
        private final StringProperty status;
        private final StringProperty host;
        private int currentPlayersCount;
        private int maxPlayersCount;

        /**
         * Constructs a GameLobby instance.
         *
         * @param id             Unique identifier for the lobby.
         * @param name           Display name of the lobby.
         * @param currentPlayers Current number of players in the lobby.
         * @param maxPlayers     Maximum player capacity of the lobby.
         * @param status         Current status of the lobby.
         * @param host           Name of the player hosting the lobby.
         */
        public GameLobby(String id, String name, int currentPlayers, int maxPlayers, String status, String host) {
            this.id = Objects.requireNonNull(id, "Lobby ID cannot be null");
            this.name = new SimpleStringProperty(Objects.requireNonNull(name, "Lobby name cannot be null"));
            this.status = new SimpleStringProperty(Objects.requireNonNull(status, "Lobby status cannot be null"));
            this.host = new SimpleStringProperty(Objects.requireNonNull(host, "Lobby host cannot be null"));
            this.playerCount = new SimpleStringProperty();
            setPlayerCount(currentPlayers, maxPlayers);
        }

        /**
         * Gets the unique identifier of the lobby.
         *
         * @return The lobby ID.
         */
        public String getId() {
            return id;
        }

        /**
         * Gets the display name of the lobby.
         *
         * @return The lobby name.
         */
        public String getName() {
            return name.get();
        }

        /**
         * Gets the current status of the lobby.
         *
         * @return The lobby status.
         */
        public String getStatus() {
            return status.get();
        }

        /**
         * Gets the host of the lobby.
         *
         * @return The host name.
         */
        public String getHost() {
            return host.get();
        }

        /**
         * Gets the current number of players in the lobby.
         *
         * @return The current player count.
         */
        public int getCurrentPlayers() {
            return currentPlayersCount;
        }

        /**
         * Gets the maximum number of players allowed in the lobby.
         *
         * @return The maximum player count.
         */
        public int getMaxPlayers() {
            return maxPlayersCount;
        }

        public StringProperty nameProperty() {
            return name;
        }

        public StringProperty playerCountProperty() {
            return playerCount;
        }

        public StringProperty statusProperty() {
            return status;
        }

        public StringProperty hostProperty() {
            return host;
        }

        /**
         * Sets the status of the lobby.
         *
         * @param newStatus The new status to set.
         */
        public void setStatus(String newStatus) {
            if (newStatus != null) {
                this.status.set(newStatus);
            }
        }

        /**
         * Sets the host of the lobby.
         *
         * @param newHost The new host to set.
         */
        public void setHost(String newHost) {
            if (newHost != null) {
                this.host.set(newHost);
            }
        }

        /**
         * Sets the current and maximum player count for the lobby.
         *
         * @param current The current number of players.
         * @param max     The maximum number of players allowed.
         */
        public void setPlayerCount(int current, int max) {
            if (current >= 0 && max > 0) {
                this.currentPlayersCount = current;
                this.maxPlayersCount = max;
                this.playerCount.set(current + "/" + max);
            } else {
                LOGGER.warning("Invalid player count update for lobby " + id + ": current=" + current + ", max=" + max);
            }
        }

        /**
         * Gets the player count property for binding.
         *
         * @return The player count property.
         */
        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (o == null || getClass() != getClass())
                return false;
            GameLobby gameLobby = (GameLobby) o;
            return id.equals(gameLobby.id);
        }

        /**
         * Returns a hash code for the lobby based on its ID.
         *
         * @return The hash code of the lobby.
         */
        @Override
        public int hashCode() {
            return Objects.hash(id);
        }

        /**
         * Returns a string representation of the GameLobby object.
         *
         * @return A string containing the lobby's ID, name, player count, status, and host.
         */
        @Override
        public String toString() {
            return "GameLobby{" +
                    "id='" + id + '\'' +
                    ", name='" + name.get() + '\'' +
                    ", playerCount='" + playerCount.get() + '\'' +
                    ", status='" + status.get() + '\'' +
                    ", host='" + host.get() + '\'' +
                    '}';
        }
    }
}
