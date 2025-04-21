package ch.unibas.dmi.dbis.cs108.client.ui.controllers;

import ch.unibas.dmi.dbis.cs108.client.ui.SceneManager;
import ch.unibas.dmi.dbis.cs108.client.ui.components.AboutDialog;
import ch.unibas.dmi.dbis.cs108.client.ui.components.SettingsDialog;
import ch.unibas.dmi.dbis.cs108.client.ui.events.ErrorEvent;
import ch.unibas.dmi.dbis.cs108.client.ui.events.UIEventBus;
import ch.unibas.dmi.dbis.cs108.client.ui.events.admin.ConnectionStatusEvent;
import ch.unibas.dmi.dbis.cs108.client.ui.events.admin.NameChangeResponseEvent;
import ch.unibas.dmi.dbis.cs108.client.ui.events.chat.GlobalChatEvent;
import ch.unibas.dmi.dbis.cs108.client.ui.utils.ResourceLoader;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Controller for the main menu screen.
 * <p>
 * Manages global chat, connection status, and navigation to other scenes.
 */
public class MainMenuController extends BaseController {
    private static final Logger LOGGER = Logger.getLogger(MainMenuController.class.getName());
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    private static final String DEFAULT_USERNAME = "Guest";

    // Data models
    private final ObservableList<String> chatHistory = FXCollections.observableArrayList();
    private final AtomicBoolean isConnected = new AtomicBoolean(false);

    // FXML UI components
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
    private int onlineUserCount;
    private ChangeListener<Number> chatWidthListener;
    private AboutDialog aboutDialog;
    private SettingsDialog settingsDialog;

    /**
     * Constructs the controller and initializes dependencies.
     */
    public MainMenuController() {
        super(new ResourceLoader(), UIEventBus.getInstance(), SceneManager.getInstance());
    }

    /**
     * Initializes UI components, event handlers, and server connection.
     */
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

    /**
     * Configures UI elements such as version label, chat list, and dialogs.
     */
    private void initializeUI() {
        versionLabel.setText("Version 1.0.0");
        configureChatListView();
        loadGameLogo();
        setupResizeListeners();
        aboutDialog = new AboutDialog();
        settingsDialog = new SettingsDialog();
    }

    /**
     * Configures the chat list view with word wrapping and dynamic width adjustment.
     */
    private void configureChatListView() {
        globalChatMessages.setItems(chatHistory);
        globalChatMessages.setCellFactory(list -> new ListCell<>() {
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
        });
        chatWidthListener = (obs, oldVal, newVal) -> globalChatMessages.refresh();
        globalChatMessages.widthProperty().addListener(chatWidthListener);
    }

    /**
     * Loads the game logo asynchronously.
     */
    private void loadGameLogo() {
        CompletableFuture.runAsync(() -> {
            try {
                Image logo = resourceLoader.loadImage("images/game-logo.png");
                Platform.runLater(() -> {
                    if (logo != null) {
                        gameLogo.setImage(logo);
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

    /**
     * Sets up listeners to adjust UI on window resize.
     */
    private void setupResizeListeners() {
        mainMenuRoot.widthProperty().addListener((obs, oldVal, newVal) -> globalChatMessages.refresh());
    }

    /**
     * Subscribes to event bus events and configures input handlers.
     */
    private void setupEventHandlers() {
        eventBus.subscribe(GlobalChatEvent.class, this::handleChatMessage);
        eventBus.subscribe(ConnectionStatusEvent.class, this::handleConnectionStatus);
        eventBus.subscribe(ErrorEvent.class, this::handleErrorEvent);
        eventBus.subscribe(NameChangeResponseEvent.class, this::handleNameChangeResponse);
        globalChatInput.setOnAction(evt -> handleSendGlobalMessage());
    }

    /**
     * Handles incoming global chat messages.
     *
     * @param event the chat event
     */
    private void handleChatMessage(GlobalChatEvent event) {
        if (event == null || event.getContent() == null) {
            LOGGER.warning("Received null or incomplete chat event");
            return;
        }
        Platform.runLater(() -> {
            String msg = String.format("%s %s: %s", getCurrentTime(), event.getSender(), event.getContent());
            chatHistory.add(msg);
            scrollToBottom();
        });
    }

    /**
     * Updates connection status and posts system messages as needed.
     *
     * @param event the connection status event
     */
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

    /**
     * Establishes the server connection asynchronously.
     *
     * @return future that completes when connection attempt finishes
     */
    private CompletableFuture<Void> establishServerConnection() {
        LOGGER.info("Establishing server connection");
        return CompletableFuture.runAsync(() -> {
            try {
                Thread.sleep(500); // simulate network delay
                Platform.runLater(() -> {
                    setConnectionStatus(true);
                    updateOnlineUserCount(42); // placeholder value
                });
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                LOGGER.log(Level.WARNING, "Connection setup interrupted", e);
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

    /**
     * Handles Play button click and navigates to the lobby.
     */
    @FXML
    private void handlePlayGame() {
        LOGGER.info("Play button clicked");
        if (!isConnected.get()) {
            addSystemMessage("Cannot start game while disconnected from server.");
            return;
        }
        sceneManager.switchToScene(SceneManager.SceneType.LOBBY);
    }

    /**
     * Opens the settings dialog.
     */
    @FXML
    private void handleSettings() {
        LOGGER.info("Settings button clicked");
        if (settingsDialog.getView().getParent() == mainMenuRoot) return;

        settingsDialog.setConnectionStatus(isConnected.get(), isConnected.get() ? "Connected" : "Disconnected");
        Node previousCenter = mainMenuRoot.getCenter();
        StackPane container = new StackPane(previousCenter, settingsDialog.getView());
        StackPane.setAlignment(settingsDialog.getView(), Pos.CENTER);
        mainMenuRoot.setCenter(container);

        settingsDialog.setOnCloseAction(() -> mainMenuRoot.setCenter(previousCenter));
        settingsDialog.setOnSaveAction(() -> {
            boolean muted = settingsDialog.muteProperty().get();
            double volume = settingsDialog.volumeProperty().get();
            LOGGER.info("Settings saved - Volume: " + volume + ", Muted: " + muted);
            addSystemMessage("Audio settings saved. " + (muted ? "Muted." : "Volume: " + volume + "%"));
        });
        settingsDialog.show();
    }

    /**
     * Opens the about dialog.
     */
    @FXML
    private void handleAbout() {
        LOGGER.info("About button clicked");
        if (aboutDialog.getView().getParent() == mainMenuRoot) return;

        Node previousCenter = mainMenuRoot.getCenter();
        StackPane container = new StackPane(previousCenter, aboutDialog.getView());
        StackPane.setAlignment(aboutDialog.getView(), Pos.CENTER);
        mainMenuRoot.setCenter(container);

        aboutDialog.setOnCloseAction(() -> mainMenuRoot.setCenter(previousCenter));
        aboutDialog.show();
    }

    /**
     * Exits the application gracefully.
     */
    @FXML
    private void handleExit() {
        LOGGER.info("Exit button clicked");
        cleanup();
        Platform.exit();
    }

    /**
     * Sends the global chat message.
     */
    @FXML
    private void handleSendGlobalMessage() {
        String msg = globalChatInput.getText().trim();
        if (msg.isEmpty()) return;
        if (!isConnected.get()) {
            addSystemMessage("Cannot send message while disconnected.");
        } else {
            eventBus.publish(new GlobalChatEvent(msg, GlobalChatEvent.ChatType.GLOBAL));
        }
        globalChatInput.clear();
    }

    /**
     * Handles error events.
     *
     * @param event the error event
     */
    private void handleErrorEvent(ErrorEvent event) {
        if (event == null) {
            LOGGER.warning("Received null error event");
            return;
        }
        Platform.runLater(() -> addSystemMessage("Error: " + event.getErrorMessage()));
    }

    /**
     * Updates the player name on name change response.
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
                addSystemMessage("Name changed to: " + playerName);
            } else {
                addSystemMessage("Failed to change name: " + event.getMessage());
            }
        });
    }

    /**
     * Adds a custom chat message to history.
     *
     * @param message the message to add
     */
    private void addChatMessage(String message) {
        if (message != null && !message.isEmpty()) {
            chatHistory.add(message);
            scrollToBottom();
        }
    }

    /**
     * Adds a system message to chat history.
     *
     * @param message the system message content
     */
    private void addSystemMessage(String message) {
        if (message != null && !message.isEmpty()) {
            chatHistory.add(getCurrentTime() + " System: " + message);
            scrollToBottom();
        }
    }

    /**
     * Updates the displayed count of online users.
     *
     * @param count the new user count
     */
    private void updateOnlineUserCount(int count) {
        if (count < 0) {
            LOGGER.warning("Invalid user count: " + count);
            return;
        }
        onlineUserCount = count;
        onlineUsersLabel.setText("Online: " + onlineUserCount);
    }

    /**
     * Updates the connection status label and style.
     *
     * @param status the connection status string
     */
    private void updateConnectionStatus(String status) {
        boolean connected = "CONNECTED".equalsIgnoreCase(status);
        setConnectionStatus(connected);
    }

    /**
     * Applies connection status UI changes.
     *
     * @param connected true if connected, false otherwise
     */
    private void setConnectionStatus(boolean connected) {
        boolean wasConnected = isConnected.get();
        isConnected.set(connected);
        connectionStatus.setText(connected ? "Connected" : "Disconnected");
        connectionStatus.getStyleClass().remove("disconnected");
        if (!connected) connectionStatus.getStyleClass().add("disconnected");
        if (!connected && wasConnected) {
            addSystemMessage("You have been disconnected from the server. Attempting to reconnect...");
        }
    }

    /**
     * Scrolls the chat list to the bottom.
     */
    private void scrollToBottom() {
        Platform.runLater(() -> {
            if (!chatHistory.isEmpty()) {
                globalChatMessages.scrollTo(chatHistory.size() - 1);
            }
        });
    }

    /**
     * Returns the current time formatted for chat messages.
     *
     * @return formatted current time
     */
    private String getCurrentTime() {
        return "[" + LocalDateTime.now().format(TIME_FORMATTER) + "]";
    }

    /**
     * Cleans up resources, unsubscribes listeners and closes dialogs.
     */
    public void cleanup() {
        eventBus.unsubscribe(GlobalChatEvent.class, this::handleChatMessage);
        eventBus.unsubscribe(ConnectionStatusEvent.class, this::handleConnectionStatus);
        eventBus.unsubscribe(ErrorEvent.class, this::handleErrorEvent);
        eventBus.unsubscribe(NameChangeResponseEvent.class, this::handleNameChangeResponse);
        if (chatWidthListener != null) {
            globalChatMessages.widthProperty().removeListener(chatWidthListener);
        }
        if (aboutDialog != null) aboutDialog.close();
        if (settingsDialog != null) settingsDialog.close();
        LOGGER.info("MainMenuController resources cleaned up");
    }
}
