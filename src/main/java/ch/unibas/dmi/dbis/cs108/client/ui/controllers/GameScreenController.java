package ch.unibas.dmi.dbis.cs108.client.ui.controllers;

import ch.unibas.dmi.dbis.cs108.SETTINGS;
import ch.unibas.dmi.dbis.cs108.client.app.GameApplication;
import ch.unibas.dmi.dbis.cs108.client.core.PlayerIdentityManager;
import ch.unibas.dmi.dbis.cs108.client.core.state.GameState;
import ch.unibas.dmi.dbis.cs108.client.networking.events.EndTurnEvent;
import ch.unibas.dmi.dbis.cs108.client.ui.SceneManager;
import ch.unibas.dmi.dbis.cs108.client.ui.components.ChatComponent;
import ch.unibas.dmi.dbis.cs108.client.ui.components.SettingsDialog;
import ch.unibas.dmi.dbis.cs108.client.ui.components.WinScreenDialog;
import ch.unibas.dmi.dbis.cs108.client.ui.components.game.GridAdjustmentManager;
import ch.unibas.dmi.dbis.cs108.client.ui.components.game.StatueSelectionPopup;
import ch.unibas.dmi.dbis.cs108.client.ui.events.ErrorEvent;
import ch.unibas.dmi.dbis.cs108.client.ui.events.UIEventBus;
import ch.unibas.dmi.dbis.cs108.client.ui.events.admin.ChangeNameUIEvent;
import ch.unibas.dmi.dbis.cs108.client.ui.events.admin.ConnectionStatusEvent;
import ch.unibas.dmi.dbis.cs108.client.ui.events.admin.NameChangeRequestEvent;
import ch.unibas.dmi.dbis.cs108.client.ui.events.admin.NameChangeResponseEvent;
import ch.unibas.dmi.dbis.cs108.client.ui.events.game.*;
import ch.unibas.dmi.dbis.cs108.client.ui.events.lobby.LeaveLobbyRequestEvent;
import ch.unibas.dmi.dbis.cs108.client.ui.events.lobby.LobbyJoinedEvent;
import ch.unibas.dmi.dbis.cs108.client.ui.utils.ResourceLoader;
import ch.unibas.dmi.dbis.cs108.client.ui.utils.CardDetails;
import ch.unibas.dmi.dbis.cs108.shared.entities.EntityRegistry;
import ch.unibas.dmi.dbis.cs108.shared.entities.Findables.Artifact;
import ch.unibas.dmi.dbis.cs108.shared.entities.GameEntity;
import ch.unibas.dmi.dbis.cs108.shared.game.Player;
import ch.unibas.dmi.dbis.cs108.shared.game.Status;
import ch.unibas.dmi.dbis.cs108.shared.game.Tile;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

import javax.swing.text.html.parser.Entity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * JavaFX controller for the in‑game screen.
 * <p>
 *
 * <ul>
 * <li>Renders the map and hex‑grid overlay.</li>
 * <li>Dispatches user interaction to the {@link UIEventBus}.</li>
 * <li>Provides grid‑adjustment tooling through
 * {@link GridAdjustmentManager}.</li>
 * <li>Hosts auxiliary UI (chat component, settings overlay, etc.).</li>
 * </ul>
 *
 * <p>
 * Functionality that is not yet implemented is explicitly marked with TODO tags
 * so that future work is easy to track.
 * </p>
 */
public class GameScreenController extends BaseController {

    /*
     * --------------------------------------------------
     * Static configuration
     * --------------------------------------------------
     */
    private static final Logger LOGGER = Logger.getLogger(GameScreenController.class.getName());
    static final int HEX_ROWS = 7;
    static final int HEX_COLS = 8;

    /*
     * --------------------------------------------------
     * Game / UI state
     * --------------------------------------------------
     */
    private final AtomicBoolean uiInitialized = new AtomicBoolean(false);

    private final PlayerIdentityManager playerManager;
    private Player localPlayer;
    private Player gamePlayer;
    private final ObservableList<String> players = FXCollections.observableArrayList();
    private GameState gameState;
    private List<Artifact> artifacts = new ArrayList<>();
    List<Color> playerColours;

    // Map and grid dimensions calculated at runtime
    private double scaledMapWidth;
    private double scaledMapHeight;
    private double mapOffsetX;
    private double mapOffsetY;

    /*
     * The following fields are package‑private because the adjustment manager
     * accesses them directly.
     */
    double effectiveHexSize;
    double gridOffsetX;
    double gridOffsetY;

    private int selectedRow = -1;
    private int selectedCol = -1;

    private String currentLobbyId;

    private Image mapImage;
    private boolean isMapLoaded;

    private SettingsDialog settingsDialog;
    private Node selectedCard;
    private CardDetails selectedStatue;
    private boolean hasPlacedStatue = false;

    // Tooltips for cards are cached to avoid recreating them on every hover event
    private final Map<Node, Tooltip> cardTooltips = new HashMap<>();

    private GridAdjustmentManager gridAdjustmentManager;

    // Card handler registry for extensible drag & drop
    private final Map<String, CardDragHandler> cardHandlers = new HashMap<>();

    // Cached placeholder for missing images
    private Node missingImagePlaceholder = null;

    /*
     * --------------------------------------------------
     * FXML‑injected UI elements
     * --------------------------------------------------
     */
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
    private Label connectionStatusLabel;
    @FXML
    private VBox chatContainer;

    private ChatComponent chatComponentController;

    // Grid‑adjustment overlay controls (created programmatically)
    private Label adjustmentModeIndicator;
    private Label adjustmentValuesLabel;

    // Keeps track of the node that initiated the current drag‑and‑drop gesture
    private Node draggedCardSource;
    private int[] highlightedTile = null;

    // Simplified colour table – replace with proper game state look‑up
    private final Map<String, Color> playerColors = new HashMap<>();

    /*
     * --------------------------------------------------
     * Construction / initialisation
     * --------------------------------------------------
     */

    /**
     * Default constructor – required by the FXMLLoader. The heavy lifting
     * happens in {@link #initialize()} which is invoked automatically once the
     * FXML graph is ready.
     */
    public GameScreenController() {
        super(new ResourceLoader(), UIEventBus.getInstance(), SceneManager.getInstance());
        LOGGER.setLevel(Level.ALL);
        playerManager = PlayerIdentityManager.getInstance();
        playerManager.addPlayerUpdateListener(this::handlePlayerUpdate);

        currentLobbyId = GameApplication.getCurrentLobbyId();
        localPlayer = playerManager.getLocalPlayer();
        // Use localPlayer's name for initial log, gamePlayer might not be set yet
        Logger.getGlobal()
                .info("Game state uses Local Player: " + (localPlayer != null ? localPlayer.getName() : "null"));
        gameState = new GameState();
        subscribeEvents();
        // Initialize selectedStatue safely
        GameEntity defaultStatueEntity = EntityRegistry.getGameEntityOriginalById(38);
        if (defaultStatueEntity != null) {
            selectedStatue = new CardDetails(defaultStatueEntity, true);
        } else {
            LOGGER.severe("Failed to load default statue entity (ID 38).");
            // Handle error case, maybe create a dummy CardDetails or throw exception
            selectedStatue = new CardDetails(38, "Error Statue", "Failed to load", "", "", 0);
        }
        Logger.getGlobal().info("GameScreenController created and subscribed to events.");

        // Register card handlers for extensible drag & drop
        registerCardHandlers();
        createPlaceholderNode(); // Create the reusable placeholder
    }

    /**
     * Creates a reusable red rectangle placeholder node.
     */
    private void createPlaceholderNode() {
        Rectangle rect = new Rectangle(78, 118); // Size for card slots
        rect.setFill(Color.RED);
        missingImagePlaceholder = rect;
    }

    /**
     * Registers handlers for different card types to support extensible drag &
     * drop.
     */
    private void registerCardHandlers() {
        // Register structure handler
        cardHandlers.put("structure", new StructureCardHandler());

        // Artifact and statue handlers will be added later
        // cardHandlers.put("artifact", new ArtifactCardHandler());
        // cardHandlers.put("statue", new StatueCardHandler());
        LOGGER.fine("Registered card drag handlers.");
    }

    /**
     * Invoked by the FXMLLoader after all @FXML fields have been injected.
     * <p>
     * Only light initialization should happen here. Heavy lifting and game-state
     * relevant
     * initialization should be done in the {@link #initializeUI()} method.
     */
    @FXML
    private void initialize() {

        initialiseSettingsDialog();
        initialiseChatComponent();

        Logger.getGlobal().info("GameScreenController initialized");
    }

    /*
     * --------------------------------------------------
     * UI helper initialisation
     * --------------------------------------------------
     */

    /**
     * Initialises the UI components and sets up the game state.
     *
     */
    private void initializeUI() {

        if (localPlayer == null) {
            LOGGER.severe("LocalPlayer is null during GameScreenController initialisation!");
            localPlayer = new Player("ErrorGuest"); // Fail‑safe stub
        }

        initialisePlayerColours();
        setupUI();
        loadMapImage();
        createAdjustmentUI();

        gridAdjustmentManager = new GridAdjustmentManager(
                this,
                adjustmentModeIndicator,
                adjustmentValuesLabel,
                this::drawMapAndGrid);

        setupCanvasListeners();
        updateCardImages();
    }

    /**
     * Wires UI controls to their backing properties and assigns sensible
     * defaults.
     */
    private void setupUI() {
        for (Player player : gameState.getPlayers()) {
            players.add(player.getName());
        }
        playersList.setItems(players);
        energyBar.setProgress(0.5);
        runesLabel.setText("0");
        connectionStatusLabel.setText("Connected");
        gameCanvas.widthProperty().bind(((Region) gameCanvas.getParent()).widthProperty());
        gameCanvas.heightProperty().bind(((Region) gameCanvas.getParent()).heightProperty());
    }

    /**
     * Installs all {@link UIEventBus} subscriptions that this controller relies
     * on.
     */
    private void subscribeEvents() {
        eventBus.subscribe(ConnectionStatusEvent.class, this::onConnectionStatus);
        eventBus.subscribe(NameChangeResponseEvent.class, this::handleNameChangeResponse);
        eventBus.subscribe(LobbyJoinedEvent.class, this::handleLobbyJoined);
        eventBus.subscribe(TileClickEvent.class, this::onTileClick);
        eventBus.subscribe(ErrorEvent.class, this::handleError);
        eventBus.subscribe(EndGameEvent.class, this::handleEndGame);
        // In GameScreenController.subscribeEvents():
        eventBus.subscribe(GameSyncEvent.class, event -> {
            // Log receiving the event
            if (event == null || event.getGameState() == null) {
                LOGGER.severe("Received null GameSyncEvent or GameState. Cannot update UI.");
                return;
            }

            GameState updatedState = event.getGameState();
            // Log details about the received event
            LOGGER.info(
                    String.format("Received GameSyncEvent: Updating game state. Board size: %d tiles. Player turn: %s",
                            updatedState.getBoardManager().getBoard().getTiles().length, updatedState.getPlayerTurn()));

            Platform.runLater(() -> {
                gameState = updatedState;
                LOGGER.info("GameSyncEvent received. Searching for player " + localPlayer.getName());
                gamePlayer = gameState.findPlayerByName(localPlayer.getName());

                if (gamePlayer == null) {
                    LOGGER.warning("Game player not found in game state.");
                    return;
                }

                artifacts = gamePlayer.getArtifacts();

                updateCardImages();
                refreshCardAffordability();
                updateRunesAndEnergyBar();
                updatePlayerList();
                updateMap();

                if (uiInitialized.compareAndSet(false, true)) {
                    LOGGER.info("First GameSyncEvent processed. Proceeding to full UI initialization...");
                    initializeUI();
                }
            });
        });
    }

    /**
     * Creates the {@link ChatComponent}, injects it into the placeholder VBox
     * and forwards the current player and lobby context.
     */
    private void initialiseChatComponent() {
        chatContainer.getChildren().clear();

        chatComponentController = new ChatComponent();
        Node chatView = chatComponentController.getView();
        chatContainer.getChildren().add(chatView);
        VBox.setVgrow(chatView, Priority.ALWAYS);

        chatComponentController.setPlayer(localPlayer);
        chatComponentController.setCurrentLobbyId(currentLobbyId);
    }

    /**
     * initialises the player colours used for the hex grid.
     */
    private void initialisePlayerColours() {
        // Create a list of possible player colours
        playerColours = new ArrayList<>();
        playerColours.add(Color.RED);
        playerColours.add(Color.BLUE);
        playerColours.add(Color.YELLOW);
        playerColours.add(Color.PURPLE);
        playerColours.add(Color.ORANGE);
        playerColours.add(Color.CYAN);
        playerColours.add(Color.MAGENTA);

        for (String playerName : GameApplication.getPlayers()) {
            if (playerName.equals(localPlayer.getName())) {
                playerColors.put(playerName, Color.GREEN); // Local Player should always be green
            } else {
                Color color = playerColours.remove(0);
                playerColors.put(playerName, color);
            }
        }
    }

    /**
     * Adds the translucent info panel used while in grid‑adjustment mode.
     */
    private void createAdjustmentUI() {
        adjustmentModeIndicator = new Label("GRID ADJUSTMENT MODE (Press G to exit)");
        adjustmentModeIndicator.setStyle(
                "-fx-background-color: rgba(255,165,0,0.7); -fx-text-fill:white; -fx-padding:5 10; -fx-background-radius:5; -fx-font-weight:bold;");
        adjustmentModeIndicator.setVisible(false);

        adjustmentValuesLabel = new Label();
        adjustmentValuesLabel.setStyle(
                "-fx-background-color: rgba(0,0,0,0.7); -fx-text-fill:white; -fx-padding:5; -fx-font-size:11; -fx-background-radius:3;");
        adjustmentValuesLabel.setVisible(false);

        VBox panel = new VBox(5, adjustmentModeIndicator, adjustmentValuesLabel);
        panel.setAlignment(Pos.TOP_CENTER);
        panel.setPadding(new Insets(10));
        ((StackPane) gameCanvas.getParent()).getChildren().add(panel);
        StackPane.setAlignment(panel, Pos.TOP_CENTER);
    }

    /*
     * --------------------------------------------------
     * Event‑bus callbacks
     * --------------------------------------------------
     */

    private void onConnectionStatus(ConnectionStatusEvent e) {
        if (e == null)
            return;

        Platform.runLater(() -> {
            connectionStatusLabel.setText(Optional.ofNullable(e.getState()).map(Object::toString).orElse("UNKNOWN"));
            if (e.getMessage() != null && !e.getMessage().isEmpty()) {
                chatComponentController.addSystemMessage(e.getMessage());
            }
            if (settingsDialog != null)
                updateSettingsConnectionStatus();
        });
    }

    private void onTileClick(TileClickEvent e) {
        LOGGER.fine(() -> String.format("Tile clicked externally (row=%d,col=%d)", e.getRow(), e.getCol()));
    }

    /**
     * Updates the player list and runes label when the local player changes.
     */
    private void handlePlayerUpdate(Player updatedPlayer) {
        localPlayer = updatedPlayer;
        GameApplication.setLocalPlayer(localPlayer);
        chatComponentController.setPlayer(localPlayer);
        chatComponentController.addSystemMessage("Name successfully changed to: " + localPlayer.getName());
        settingsDialog.playerNameProperty().set(localPlayer.getName());
        updatePlayerList();
        LOGGER.info("Player updated in GameScreenController: " + localPlayer.getName());
    }

    /**
     * Handles the game sync event and updates the game state accordingly.
     */
    private void handleGameSync(GameSyncEvent e) {
        // Log receiving the event
        if (e == null || e.getGameState() == null) {
            LOGGER.severe("Received null GameSyncEvent or GameState. Cannot update UI.");
            return;
        }

        GameState updatedState = e.getGameState();
        // Log details about the received event
        LOGGER.info(String.format("Received GameSyncEvent: Updating game state. Board size: %d tiles. Player turn: %s",
                updatedState.getBoardManager().getBoard().getTiles().length, updatedState.getPlayerTurn()));

        Platform.runLater(() -> {
            gameState = updatedState;
            LOGGER.info("GameSyncEvent received. Searching for player " + localPlayer.getName());
            gamePlayer = gameState.findPlayerByName(localPlayer.getName());

            if (gamePlayer == null) {
                LOGGER.warning("Game player not found in game state.");
                return;
            }

            artifacts = gamePlayer.getArtifacts();

            updateCardImages();
            refreshCardAffordability();
            updateRunesAndEnergyBar();
            updatePlayerList();
            updateMap();

            if (uiInitialized.compareAndSet(false, true)) {
                LOGGER.info("First GameSyncEvent processed. Proceeding to full UI initialization...");
                initializeUI();
            }
        });
    }

    /**
     * Shows an error message in the chat component.
     *
     * @param event The error event containing the error message.
     */
    public void handleError(ErrorEvent event) {
        Objects.requireNonNull(event, "ErrorEvent cannot be null");
        Platform.runLater(() -> {
            String errorMessage = event.getErrorMessage();
            LOGGER.warning("Received error event: " + errorMessage);
            if (chatComponentController != null && errorMessage != null && !errorMessage.isEmpty()) {
                chatComponentController.addSystemMessage("Error: " + errorMessage);
            }
        });
    }

    /*
     * --------------------------------------------------
     * Navigation & overlay actions (Main‑menu / Settings dialog)
     * --------------------------------------------------
     */

    /**
     * Switches back to the main‑menu scene.
     */
    @FXML
    private void handleBackToMainMenu() {
        sceneManager.switchToScene(SceneManager.SceneType.MAIN_MENU);
    }

    /**
     * Opens the in‑game {@link SettingsDialog}.
     */
    @FXML
    private void handleSettings() {
        LOGGER.info("Settings button clicked.");

        Pane root = (StackPane) gameCanvas.getParent();
        if (root == null) {
            LOGGER.warning("Cannot show settings: Root pane (StackPane) not found.");
            return;
        }

        updateSettingsConnectionStatus();
        settingsDialog.playerNameProperty().set(localPlayer.getName());

        settingsDialog.setOnSaveAction(() -> {
            boolean muted = settingsDialog.muteProperty().get();
            double volume = settingsDialog.volumeProperty().get();
            String requested = settingsDialog.playerNameProperty().get();
            LOGGER.info("Settings dialog save requested – Volume: " + volume + ", Muted: " + muted
                    + ", Requested Name: " + requested);

            if (requested != null && !requested.trim().isEmpty() && !requested.equals(localPlayer.getName())) {
                requestNameChange(requested.trim());
            } else if (requested != null && requested.trim().isEmpty()) {
                chatComponentController.addSystemMessage("Error: Player name cannot be empty.");
                settingsDialog.playerNameProperty().set(localPlayer.getName());
            }

            chatComponentController
                    .addSystemMessage("Audio settings saved. " + (muted ? "Muted." : "Volume: " + (int) volume + "%"));
        });

        showDialogAsOverlay(settingsDialog, root);
    }

    /**
     * Publishes a {@link ChangeNameUIEvent} so that the server can validate and
     * apply the new name.
     */
    private void requestNameChange(String newName) {
        LOGGER.info("Requesting name change to: " + newName);
        chatComponentController.addSystemMessage("Requesting name change to: " + newName + "...");
        eventBus.publish(new ChangeNameUIEvent(newName));
    }

    /**
     * Reacts to a positive or negative name‑change response from the server.
     */
    private void handleNameChangeResponse(NameChangeResponseEvent event) {
        Objects.requireNonNull(event, "NameChangeResponseEvent cannot be null");

        Platform.runLater(() -> {
            if (event.isSuccess()) {
                playerManager.updatePlayerName(event.getNewName());
            } else {
                String reason = Optional.ofNullable(event.getMessage()).orElse("Unknown reason.");
                chatComponentController.addSystemMessage("Failed to change name: " + reason);
                settingsDialog.playerNameProperty().set(localPlayer.getName());
            }
        });
    }

    /**
     * Displays a system message once the local player has joined a lobby.
     */
    private void handleLobbyJoined(LobbyJoinedEvent event) {
        Objects.requireNonNull(event, "LobbyJoinedEvent cannot be null");

        Platform.runLater(() -> {
            chatComponentController.setPlayer(localPlayer);
            chatComponentController.setCurrentLobbyId(currentLobbyId);
            chatComponentController.addSystemMessage("You joined lobby: " + event.getLobbyName());
        });
    }

    /**
     * Shows the WinScreenDialog when the game ends.
     *
     * @param event The event containing the leaderboard data.
     */
    private void handleEndGame(EndGameEvent event) {
        Platform.runLater(() -> {
            WinScreenDialog dialog = new WinScreenDialog(event.getLeaderboard());
            dialog.setOnMenuAction(() -> {
                eventBus.publish(new LeaveLobbyRequestEvent(currentLobbyId));
                sceneManager.switchToScene(SceneManager.SceneType.MAIN_MENU);
            });
            dialog.setOnLobbyAction(() -> {
                eventBus.publish(new LeaveLobbyRequestEvent(currentLobbyId));
                sceneManager.switchToScene(SceneManager.SceneType.LOBBY);
            });
            StackPane root = (StackPane) gameCanvas.getParent();
            root.getChildren().add(dialog.getView());
            showDialogAsOverlay(dialog, root);
        });
    }

    /*
     * --------------------------------------------------
     * External setters (used by SceneManager or other controllers)
     * --------------------------------------------------
     */

    /**
     * Updates the lobby identifier for both the controller and the chat
     * component.
     */
    public void setCurrentLobbyId(String lobbyId) {
        this.currentLobbyId = lobbyId;
        chatComponentController.setCurrentLobbyId(lobbyId);
    }

    /**
     * Updates the local player reference and forwards it to the chat UI.
     */
    public void setLocalPlayer(Player player) {
        this.localPlayer = player;
        chatComponentController.setPlayer(player);
    }

    /**
     * Must be called when the controller is disposed to avoid dangling listeners
     * and memory leaks.
     */
    public void cleanup() {
        eventBus.unsubscribe(ConnectionStatusEvent.class, this::onConnectionStatus);
        eventBus.unsubscribe(TileClickEvent.class, this::onTileClick);
        eventBus.unsubscribe(GameSyncEvent.class, this::handleGameSync);
        eventBus.unsubscribe(NameChangeResponseEvent.class, this::handleNameChangeResponse);

        playerManager.removePlayerUpdateListener(this::handlePlayerUpdate);

        if (chatComponentController != null)
            chatComponentController.cleanup();
        if (settingsDialog != null)
            settingsDialog.close();
        if (gameCanvas != null) {
            gameCanvas.getParent().removeEventHandler(MouseEvent.MOUSE_PRESSED,
                    e -> handleCanvasClick(e.getX(), e.getY()));
            gameCanvas.setOnKeyPressed(null);
        }
        if (draggedCardSource != null) {
            Pane parent = (Pane) draggedCardSource.getParent();
            if (parent != null)
                parent.getChildren().remove(draggedCardSource);
        }
        if (cardTooltips != null) {
            for (Tooltip tooltip : cardTooltips.values()) {
                Tooltip.uninstall(tooltip.getOwnerNode(), tooltip);
            }
            cardTooltips.clear();
        }
        LOGGER.info("GameScreenController resources cleaned up");
    }

    /*
     * --------------------------------------------------
     * Grid‑adjustment API (delegates to GridAdjustmentManager)
     * --------------------------------------------------
     */

    /**
     * Toggles grid‑adjustment mode.
     */
    public void toggleGridAdjustmentMode() {
        gridAdjustmentManager.toggleGridAdjustmentMode();
    }

    /**
     * Enables or disables grid‑adjustment mode.
     */
    public void setGridAdjustmentMode(boolean active) {
        gridAdjustmentManager.setGridAdjustmentMode(active);
    }

    /**
     * @return human‑readable description of the current grid parameters.
     */
    public String getGridSettings() {
        return gridAdjustmentManager.getGridSettings();
    }

    /**
     * Handles global keyboard shortcuts.
     */
    @FXML
    private void handleKeyboardShortcut(KeyEvent e) {
        if (gridAdjustmentManager.handleKeyboardShortcut(e)) {
            e.consume();
        }
    }

    /*
     * --------------------------------------------------
     * Map image & canvas handling
     * --------------------------------------------------
     */

    /**
     * Loads the background map image and triggers the initial draw.
     */
    private void loadMapImage() {
        mapImage = resourceLoader.loadImage(ResourceLoader.MAP_IMAGE);
        isMapLoaded = mapImage != null;
        if (isMapLoaded)
            drawMapAndGrid();
        else
            LOGGER.severe("Map image missing");
    }

    /**
     * Installs listeners so that the grid is redrawn when the canvas size changes
     * and so that mouse and keyboard events are intercepted.
     */
    private void setupCanvasListeners() {
        gameCanvas.widthProperty().addListener((o, ov, nv) -> drawMapAndGrid());
        gameCanvas.heightProperty().addListener((o, ov, nv) -> drawMapAndGrid());
        gameCanvas.setFocusTraversable(true);

        // Single click handler - just selects tile
        gameCanvas.addEventHandler(MouseEvent.MOUSE_PRESSED, e -> handleCanvasClick(e.getX(), e.getY()));

        // Double click handler - for purchases
        gameCanvas.addEventHandler(MouseEvent.MOUSE_CLICKED, e -> {
            if (e.getClickCount() == 2) {
                handleCanvasDoubleClick(e.getX(), e.getY());
            }
        });

        if (gameCanvas.getParent() instanceof StackPane parent) {
            parent.addEventHandler(MouseEvent.MOUSE_PRESSED, ev -> {
                Point2D local = gameCanvas.sceneToLocal(ev.getSceneX(), ev.getSceneY());
                if (local.getX() >= 0 && local.getY() >= 0 &&
                        local.getX() <= gameCanvas.getWidth() && local.getY() <= gameCanvas.getHeight()) {
                    handleCanvasClick(local.getX(), local.getY());
                    ev.consume();
                }
            });

            // Add double-click handler to parent as well
            parent.addEventHandler(MouseEvent.MOUSE_CLICKED, ev -> {
                if (ev.getClickCount() == 2) {
                    Point2D local = gameCanvas.sceneToLocal(ev.getSceneX(), ev.getSceneY());
                    if (local.getX() >= 0 && local.getY() >= 0 &&
                            local.getX() <= gameCanvas.getWidth() && local.getY() <= gameCanvas.getHeight()) {
                        handleCanvasDoubleClick(local.getX(), local.getY());
                        ev.consume();
                    }
                }
            });
        }

        gameCanvas.setOnKeyPressed(gridAdjustmentManager::handleGridAdjustmentKeys);
        gameCanvas.setOnDragOver(this::handleDragOver);
        gameCanvas.setOnDragDropped(this::handleDragDropped);
        gameCanvas.setOnDragExited(this::handleDragExited);
    }

    /**
     * Handles a physical mouse click on the canvas – selects the hex and triggers
     * a tile click event.
     */
    private void handleCanvasClick(double px, double py) {
        int[] tile = getHexAt(px, py);
        if (tile == null)
            return;

        int row = tile[0];
        int col = tile[1];
        selectedRow = row;
        selectedCol = col;
        drawMapAndGrid();
        eventBus.publish(new TileClickEvent(row, col));
    }

    /**
     * Handles a double-click on the canvas – attempts to purchase the tile
     * immediately
     * without a confirmation dialog.
     */
    private void handleCanvasDoubleClick(double px, double py) {
        int[] tile = getHexAt(px, py);
        if (tile == null)
            return;

        int row = tile[0];
        int col = tile[1];

        // Check if tile is purchasable
        String ownerId = getTileOwnerId(row, col);

        if (ownerId == null) {
            int price = getTilePrice(row, col);
            int runes = getPlayerRunes();

            if (runes >= price) {
                // Immediately buy the tile without confirmation
                eventBus.publish(new BuyTileUIEvent(col, row));
            } else {
                showNotification("Not enough runes to buy this tile (Cost: " + price + ").");
            }
        } else if (localPlayer != null && ownerId.equals(localPlayer.getId())) {
            showNotification("You already own this tile.");
        } else {
            showNotification("This tile is owned by another player.");
        }
    }

    /**
     * Repaints the map and hex‑grid. Invoked whenever the canvas is resized or
     * one of the grid parameters changes.
     */
    void drawMapAndGrid() {
        if (!isMapLoaded)
            return;
        double cW = gameCanvas.getWidth();
        double cH = gameCanvas.getHeight();
        if (cW <= 0 || cH <= 0)
            return;

        GraphicsContext gc = gameCanvas.getGraphicsContext2D();
        gc.clearRect(0, 0, cW, cH);

        // --- Draw background map ---------------------------------------------
        double imgRatio = mapImage.getWidth() / mapImage.getHeight();
        double canvasRatio = cW / cH;
        if (canvasRatio > imgRatio) {
            scaledMapHeight = cH;
            scaledMapWidth = scaledMapHeight * imgRatio;
        } else {
            scaledMapWidth = cW;
            scaledMapHeight = scaledMapWidth / imgRatio;
        }
        mapOffsetX = (cW - scaledMapWidth) / 2;
        mapOffsetY = (cH - scaledMapHeight) / 2;
        gc.drawImage(mapImage, mapOffsetX, mapOffsetY, scaledMapWidth, scaledMapHeight);

        // --- Prepare grid dimensions ----------------------------------------
        double gridW = scaledMapWidth * gridAdjustmentManager.getGridWidthPercentage();
        double gridH = scaledMapHeight * gridAdjustmentManager.getGridHeightPercentage();
        double hLimit = gridW / ((HEX_COLS - 1) * 0.75 + 1);
        double vLimit = gridH / ((HEX_ROWS - 0.5) * 0.866 * 2);
        effectiveHexSize = Math.min(hLimit, vLimit) * 0.5 * gridAdjustmentManager.getGridScaleFactor();

        double addHX = gridAdjustmentManager.getGridHorizontalOffset() * scaledMapWidth;
        double addHY = gridAdjustmentManager.getGridVerticalOffset() * scaledMapHeight;

        drawHexGrid(gc, effectiveHexSize, gridW, gridH, addHX, addHY);

        // Ensure adjustment overlay is visible in adjustment mode
        boolean active = gridAdjustmentManager.isGridAdjustmentModeActive();
        adjustmentModeIndicator.setVisible(active);
        adjustmentValuesLabel.setVisible(active);
    }

    /**
     * Draws the complete grid (including ownership highlighting and selection).
     */
    private void drawHexGrid(GraphicsContext gc,
            double size,
            double gridW,
            double gridH,
            double addHX,
            double addHY) {
        gc.setStroke(Color.WHITE);
        gc.setLineWidth(1.5);
        gc.setGlobalAlpha(0.7);

        double hSpacing = size * gridAdjustmentManager.getHorizontalSpacingFactor();
        double vSpacing = size * gridAdjustmentManager.getVerticalSpacingFactor();

        double totalW = hSpacing * (HEX_COLS - 0.5);
        double totalH = vSpacing * HEX_ROWS;

        double baseX = mapOffsetX + (scaledMapWidth - gridW) / 2;
        double baseY = mapOffsetY + (scaledMapHeight - gridH) / 2;
        gridOffsetX = baseX + (gridW - totalW) / 2 + addHX;
        gridOffsetY = baseY + (gridH - totalH) / 2 + addHY;

        for (int r = 0; r < HEX_ROWS; r++) {
            for (int c = 0; c < HEX_COLS; c++) {
                double cx = gridOffsetX + c * hSpacing + (r % 2) * (hSpacing / 2);
                double cy = gridOffsetY + r * vSpacing;
                boolean selected = (r == selectedRow && c == selectedCol);
                drawHex(gc, cx, cy, size, r, c, selected);
            }
        }
        gc.setGlobalAlpha(1);
    }

    /**
     * Draws a single hexagon, optionally highlighting ownership and selection.
     * Also draws the entity image if present.
     */
    private void drawHex(GraphicsContext gc,
            double cx,
            double cy,
            double size,
            int row,
            int col,
            boolean selected) {
        double[] xs = new double[6];
        double[] ys = new double[6];

        double rot = Math.toRadians(gridAdjustmentManager.getHexRotationDegrees());
        double hSquish = gridAdjustmentManager.getHorizontalSquishFactor();
        double vSquish = gridAdjustmentManager.getVerticalSquishFactor();

        for (int i = 0; i < 6; i++) {
            double a = rot + 2 * Math.PI / 6 * i;
            xs[i] = cx + size * Math.cos(a) * hSquish;
            ys[i] = cy + size * Math.sin(a) * vSquish;
        }

        gc.beginPath();
        gc.moveTo(xs[0], ys[0]);
        for (int i = 1; i < 6; i++)
            gc.lineTo(xs[i], ys[i]);
        gc.closePath();

        // Ownership colouring --------------------------------------------------
        String ownerId = getTileOwnerId(row, col);
        Color ownerCol = getPlayerColor(ownerId);
        if (ownerCol != null) {
            Paint oldFill = gc.getFill();
            double oldAlpha = gc.getGlobalAlpha();
            gc.setFill(ownerCol);
            gc.setGlobalAlpha(0.4);
            gc.fill();
            gc.setFill(oldFill);
            gc.setGlobalAlpha(oldAlpha);
        }

        // Selection highlight --------------------------------------------------
        if (selected) {
            Paint oldStroke = gc.getStroke();
            double oldAlpha = gc.getGlobalAlpha();
            gc.setStroke(Color.YELLOW);
            gc.setLineWidth(3);
            gc.setGlobalAlpha(oldAlpha);
            gc.stroke();
            gc.setStroke(oldStroke);
        } else {
            gc.stroke();
        }

        // Add drag target highlight (bright green) ------------------------------
        boolean isDragTarget = highlightedTile != null &&
                highlightedTile[0] == row &&
                highlightedTile[1] == col;
        if (isDragTarget) {
            Paint oldStroke = gc.getStroke();
            double oldLineWidth = gc.getLineWidth();
            double oldAlpha = gc.getGlobalAlpha();

            gc.setStroke(Color.LIME);
            gc.setLineWidth(4);
            gc.setGlobalAlpha(0.8);
            gc.stroke();

            gc.setStroke(oldStroke);
            gc.setLineWidth(oldLineWidth);
            gc.setGlobalAlpha(oldAlpha);
        }

        // Draw the Entity if it exists -----------------------------------------
        Tile tile = getTile(row, col);
        if (tile != null) {
            GameEntity entity = tile.getEntity();
            if (entity != null) {
                // Use isCard=false for entities placed on the board
                String URL = EntityRegistry.getURL(entity.getId(), false);
                drawEntityImage(gc, URL, cx, cy, size, hSquish, entity.getId()); // Pass entity ID for logging
            }
        } else {
            // Log only once or use finer level if this happens often
            // LOGGER.warning("Tile is null for row " + row + ", col " + col);
        }
    }

    /**
     * Draws an entity image centered in a hex tile.
     * The image is scaled to fit the hex width while preserving its aspect ratio.
     * Uses EntityRegistry.getURL(isCard=true) for loading the image, with a red
     * placeholder if missing.
     *
     * @param gc       The graphics context to draw on
     * @param imageUrl The URL of the image to draw (obtained with isCard=false)
     * @param centerX  The x-coordinate of the hex center
     * @param centerY  The y-coordinate of the hex center
     * @param hexSize  The size of the hex
     * @param hSquish  The horizontal squish factor
     * @param entityId The ID of the entity being drawn (for logging)
     */
    private void drawEntityImage(GraphicsContext gc, String imageUrl, double centerX, double centerY,
            double hexSize, double hSquish, int entityId) {
        // Calculate placeholder size relative to hex (adjust as needed for map
        // entities)
        double placeholderSizeRatio = 0.7; // Make placeholder 70% of hex width
        double placeholderWidth = 1.7 * hexSize * hSquish * placeholderSizeRatio;
        // Maintain aspect ratio 1:1 for square placeholder, adjust if needed
        double placeholderHeight = placeholderWidth;
        double placeholderX = centerX - placeholderWidth / 2;
        double placeholderY = centerY - placeholderHeight / 2;

        // Helper function to draw placeholder
        Runnable drawPlaceholder = () -> {
            Paint oldFill = gc.getFill();
            double oldAlpha = gc.getGlobalAlpha();
            gc.setFill(Color.RED); // Use red for placeholder
            gc.setGlobalAlpha(1.0); // Ensure placeholder is opaque
            gc.fillRect(placeholderX, placeholderY, placeholderWidth, placeholderHeight);
            gc.setFill(oldFill);
            gc.setGlobalAlpha(oldAlpha);
        };

        if (imageUrl == null || imageUrl.isEmpty()) {
            // Log ERROR for missing URL
            LOGGER.severe(String.format("Missing map image URL for entity ID %d. Drawing red placeholder.", entityId));
            drawPlaceholder.run();
            return;
        }

        try {
            Image image = resourceLoader.loadImage(imageUrl);
            if (image == null || image.isError()) {
                // Log ERROR for image loading failure
                LOGGER.severe(
                        String.format("Failed to load map entity image: %s (Entity ID: %d). Drawing red placeholder.",
                                imageUrl, entityId));
                drawPlaceholder.run();
                return;
            }

            // Calculate maximum width based on hex size and squish factor
            double maxWidth = 1.7 * hexSize * hSquish;
            // Calculate maximum height based on hex size (approx sqrt(3)*size)
            double maxHeight = 1.732 * hexSize; // Approximation

            // Calculate scale to fit within both max width and max height
            double scale = Math.min(maxWidth / image.getWidth(), maxHeight / image.getHeight());

            // Calculate scaled dimensions
            double scaledWidth = image.getWidth() * scale;
            double scaledHeight = image.getHeight() * scale;

            // Save current graphics state
            double oldAlpha = gc.getGlobalAlpha();
            gc.setGlobalAlpha(1.0); // Full opacity for the image

            // Draw image centered in the hex
            gc.drawImage(image,
                    centerX - scaledWidth / 2,
                    centerY - scaledHeight / 2,
                    scaledWidth,
                    scaledHeight);

            // Restore graphics state
            gc.setGlobalAlpha(oldAlpha);
        } catch (Exception e) {
            // Log ERROR for any other exception during drawing
            LOGGER.log(Level.SEVERE,
                    String.format("Error drawing map entity image for ID %d: %s", entityId, e.getMessage()), e);
            drawPlaceholder.run();
        }
    }

    /*
     * --------------------------------------------------
     * Hit‑testing helpers
     * --------------------------------------------------
     */

    /**
     * Transforms canvas coordinates to logical grid coordinates.
     *
     * @return {@code int[]{row,col}} or {@code null} if the point is not inside
     *         any tile.
     */
    int[] getHexAt(double px, double py) {
        if (!isMapLoaded || effectiveHexSize <= 0)
            return null;

        double hSpacing = effectiveHexSize * gridAdjustmentManager.getHorizontalSpacingFactor();
        double vSpacing = effectiveHexSize * gridAdjustmentManager.getVerticalSpacingFactor();

        for (int r = 0; r < HEX_ROWS; r++) {
            for (int c = 0; c < HEX_COLS; c++) {
                double cx = gridOffsetX + c * hSpacing + (r % 2) * (hSpacing / 2);
                double cy = gridOffsetY + r * vSpacing;
                if (pointInHex(px, py, cx, cy, effectiveHexSize)) {
                    return new int[] { r, c };
                }
            }
        }
        return null;
    }

    /**
     * Point‑in‑polygon test for the current hex shape.
     */
    boolean pointInHex(double px, double py, double cx, double cy, double size) {
        double rot = Math.toRadians(gridAdjustmentManager.getHexRotationDegrees());
        double hSquish = gridAdjustmentManager.getHorizontalSquishFactor();
        double vSquish = gridAdjustmentManager.getVerticalSquishFactor();

        double[] xs = new double[6];
        double[] ys = new double[6];
        for (int i = 0; i < 6; i++) {
            double a = rot + 2 * Math.PI / 6 * i;
            xs[i] = cx + size * Math.cos(a) * hSquish;
            ys[i] = cy + size * Math.sin(a) * vSquish;
        }

        boolean inside = false;
        for (int i = 0, j = 5; i < 6; j = i++) {
            if (((ys[i] > py) != (ys[j] > py)) &&
                    (px < (xs[j] - xs[i]) * (py - ys[i]) / (ys[j] - ys[i]) + xs[i])) {
                inside = !inside;
            }
        }
        return inside;
    }

    /*
     * --------------------------------------------------
     * Settings dialog helper methods
     * --------------------------------------------------
     */

    /**
     * Creates the settings dialog instance and initialises bindings.
     */
    private void initialiseSettingsDialog() {
        settingsDialog = new SettingsDialog();
        settingsDialog.playerNameProperty().set(localPlayer.getName());
        settingsDialog.setOnSaveAction(this::handleSettingsSave);
        updateSettingsConnectionStatus();
    }

    /**
     * Synchronises the connection indicator shown inside the dialog.
     */
    private void updateSettingsConnectionStatus() {
        String status = connectionStatusLabel.getText();
        settingsDialog.setConnectionStatus("Connected".equals(status), status);
    }

    /**
     * Handles the save button inside the settings dialog.
     */
    private void handleSettingsSave() {
        String newName = settingsDialog.playerNameProperty().get().trim();
        if (!newName.isEmpty() && !newName.equals(localPlayer.getName())) {
            eventBus.publish(new NameChangeRequestEvent(newName));
        } else if (newName.isEmpty()) {
            settingsDialog.playerNameProperty().set(localPlayer.getName());
        }
    }

    /*
     * --------------------------------------------------
     * Card tooltip & drag‑and‑drop handling
     * --------------------------------------------------
     */

    /**
     * Toggles card selection (golden frame) when clicked.
     */
    @FXML
    public void handleCardClick(MouseEvent event) {
        Node card = (Node) event.getSource();
        if (card == selectedCard) {
            card.getStyleClass().remove("selected-card");
            selectedCard = null;
        } else {
            if (selectedCard != null)
                selectedCard.getStyleClass().remove("selected-card");
            card.getStyleClass().add("selected-card");
            selectedCard = card;
        }
        event.consume();
    }

    /**
     * Handles click on the statue card, showing the statue selection popup.
     */
    @FXML
    public void handleStatueCardClick(MouseEvent event) {
        if (hasPlacedStatue) {
            // Player already placed a statue, ignore click
            return;
        }

        // Check if this is the statue card
        Node card = (Node) event.getSource();
        if (card.getId() != null && card.getId().startsWith("statue")) {
            // Create and show the statue selection popup
            StatueSelectionPopup popup = new StatueSelectionPopup(resourceLoader, this::onStatueSelected);
            popup.show(card,
                    event.getScreenX() - 175, // Center horizontally
                    event.getScreenY() - 200); // Position above the cursor

            event.consume();
        }
    }

    /**
     * Shows the tooltip for a card after a short delay.
     */
    @FXML
    public void handleCardMouseEntered(MouseEvent event) {
        Node card = (Node) event.getSource();
        Tooltip tooltip = cardTooltips.computeIfAbsent(card, this::createTooltipForCard);
        Tooltip.install(card, tooltip);
        event.consume();
    }

    /**
     * Hides the tooltip once the mouse exits the card.
     */
    @FXML
    public void handleCardMouseExited(MouseEvent event) {
        Node card = (Node) event.getSource();
        Tooltip tip = cardTooltips.get(card);
        if (tip != null)
            Tooltip.uninstall(card, tip);
        event.consume();
    }

    // --- Drag‑and‑drop (Refactored for Extensibility) ---

    /**
     * Helper method to extract the card type from its ID.
     * Returns "structure", "artifact", "statue", or an empty string.
     */
    private String getCardType(String cardId) {
        if (cardId == null)
            return "";
        if (cardId.startsWith("structure"))
            return "structure";
        if (cardId.startsWith("artifact"))
            return "artifact";
        if (cardId.startsWith("statue"))
            return "statue";
        return ""; // Unknown type
    }

    /**
     * Handles the start of a drag operation from a card Pane.
     * Delegates to the appropriate CardDragHandler based on card type.
     */
    @FXML
    private void handleCardDragDetected(MouseEvent event) {
        Node sourceNode = (Node) event.getSource();
        String cardId = sourceNode.getId();

        if (cardId == null || cardId.isEmpty()) {
            LOGGER.warning("Drag detected on node with no ID.");
            event.consume(); // Prevent drag if ID is missing
            return;
        }

        // Determine card type prefix (structure, artifact, statue)
        String cardType = getCardType(cardId);
        CardDragHandler handler = cardHandlers.get(cardType);

        if (handler == null) {
            // Log if no handler is found for a known type, otherwise ignore (e.g., dragging
            // non-card elements)
            if (!cardType.isEmpty()) {
                LOGGER.warning("No handler registered for card type: " + cardType + " (Card ID: " + cardId + ")");
            }
            event.consume(); // Prevent drag if no handler
            return;
        }

        // Let the appropriate handler handle the drag start
        handler.handleDragDetected(sourceNode, cardId, event);
        // Event consumption is handled within the handler
    }

    /**
     * Continually called while the user drags a card across the canvas.
     * Delegates validation to the appropriate CardDragHandler.
     *
     * @param event The drag event containing the current mouse position
     */
    @FXML
    private void handleDragOver(DragEvent event) {
        // Basic checks: ensure drag comes from a different source and has string data
        if (event.getGestureSource() == gameCanvas || !event.getDragboard().hasString()) {
            event.acceptTransferModes(TransferMode.NONE); // Explicitly reject
            event.consume();
            return;
        }

        String cardId = event.getDragboard().getString();
        String cardType = getCardType(cardId);
        CardDragHandler handler = cardHandlers.get(cardType);

        if (handler == null) {
            // No handler for this type, reject the drag
            event.acceptTransferModes(TransferMode.NONE);
            event.consume();
            return;
        }

        // Get the tile at the cursor position
        int[] tileCoords = getHexAt(event.getX(), event.getY());

        boolean isValidTarget = false;
        if (tileCoords != null) {
            // Let the appropriate handler validate if drop is allowed here
            isValidTarget = handler.canDropAt(tileCoords[0], tileCoords[1], cardId);
        }

        // Update highlight and accept/reject transfer mode
        if (isValidTarget) {
            // Valid drop target - highlight the tile if not already highlighted
            if (highlightedTile == null || highlightedTile[0] != tileCoords[0] || highlightedTile[1] != tileCoords[1]) {
                highlightedTile = tileCoords;
                drawMapAndGrid(); // Redraw to show highlight
            }
            event.acceptTransferModes(TransferMode.MOVE);
        } else {
            // Invalid drop target - clear highlight if currently shown
            if (highlightedTile != null) {
                highlightedTile = null;
                drawMapAndGrid(); // Redraw to remove highlight
            }
            event.acceptTransferModes(TransferMode.NONE);
        }

        event.consume();
    }

    /**
     * Clears the highlighted tile when the drag operation exits the canvas.
     *
     * @param event The drag event containing the current mouse position
     */
    @FXML
    private void handleDragExited(DragEvent event) {
        // Clear the highlighted tile when drag exits canvas boundaries
        if (highlightedTile != null) {
            highlightedTile = null;
            drawMapAndGrid(); // Redraw to remove highlight
        }
        event.consume();
    }

    /**
     * Finalises the drag‑and‑drop operation (placing the item).
     * Delegates processing to the appropriate CardDragHandler.
     *
     * @param event The drag event containing the current mouse position
     */
    @FXML
    private void handleDragDropped(DragEvent event) {
        Dragboard db = event.getDragboard();
        boolean success = false;

        // Ensure data is present
        if (db.hasString()) {
            String cardId = db.getString();
            String cardType = getCardType(cardId);
            CardDragHandler handler = cardHandlers.get(cardType);

            if (handler != null) {
                int[] tileCoords = getHexAt(event.getX(), event.getY());

                if (tileCoords != null) {
                    int row = tileCoords[0];
                    int col = tileCoords[1];

                    // Let the appropriate handler process the drop
                    // The handler performs final validation and publishes events
                    success = handler.handleDrop(row, col, cardId);
                } else {
                    LOGGER.fine("Drag dropped outside of any valid tile.");
                }
            } else {
                LOGGER.warning("Drop detected for unknown card type: " + cardType + " (Card ID: " + cardId + ")");
            }
        } else {
            LOGGER.warning("Drag dropped with no string data in dragboard.");
        }

        // Clear highlight regardless of success/failure
        if (highlightedTile != null) {
            highlightedTile = null;
            drawMapAndGrid();
        }

        // Inform the system whether the drop was successful
        event.setDropCompleted(success);
        event.consume();
    }

    /**
     * Cleans up after a drag‑and‑drop operation has finished (on the source node).
     * Delegates to the appropriate CardDragHandler.
     *
     * @param event The drag event containing the current mouse position
     */
    @FXML
    private void handleCardDragDone(DragEvent event) {
        String cardId = null;
        // Identify the source card ID from the event source if possible
        if (event.getGestureSource() instanceof Node sourceNode) {
            cardId = sourceNode.getId();
        } else {
            LOGGER.warning("handleCardDragDone: Could not identify source node.");
            event.consume();
            return;
        }

        if (cardId != null) {
            String cardType = getCardType(cardId);
            CardDragHandler handler = cardHandlers.get(cardType);

            if (handler != null) {
                // Let the handler perform cleanup actions (e.g., removing card from hand if
                // needed)
                handler.handleDragDone(cardId, event.getTransferMode() == TransferMode.MOVE);
            } else {
                LOGGER.warning("handleCardDragDone: No handler found for card type: " + cardType + " (Card ID: "
                        + cardId + ")");
            }
        }

        // Clear reference to the dragged source node
        draggedCardSource = null; // Reset draggedCardSource here

        // Clear highlight just in case it wasn't cleared by DragExited/Dropped
        if (highlightedTile != null) {
            highlightedTile = null;
            drawMapAndGrid();
        }

        event.consume();
    }

    /*
     * --------------------------------------------------
     * Tooltip creation helper
     * --------------------------------------------------
     */

    /**
     * Creates an appropriate tooltip for a card based on its type
     */
    private Tooltip createTooltipForCard(Node card) {
        Tooltip tooltip = new Tooltip();
        tooltip.setShowDelay(Duration.millis(500));
        tooltip.setHideDelay(Duration.millis(200));

        String id = card.getId();
        CardDetails details = null;
        try {
            details = getCardDetails(id);
        } catch (Exception e) {
            LOGGER.warning("Could not get card details for tooltip: " + id + " - " + e.getMessage());
            // Create a default tooltip or return null/empty tooltip
            tooltip.setText("Error loading details");
            return tooltip;
        }

        // Create a layout with styled sections
        VBox content = new VBox(5);
        content.setPadding(new Insets(8));
        content.setMaxWidth(300);
        content.getStyleClass().add("tooltip-content");

        // Only add components with actual content
        if (details.getTitle() != null && !details.getTitle().isEmpty()) {
            Label titleLabel = new Label(details.getTitle());
            titleLabel.getStyleClass().add("tooltip-title");
            titleLabel.setWrapText(true);
            content.getChildren().add(titleLabel);

            // Only add separator if next section has content
            if ((details.getDescription() != null && !details.getDescription().isEmpty()) ||
                    (details.getLore() != null && !details.getLore().isEmpty()) ||
                    details.getPrice() > 0) {
                content.getChildren().add(new Separator());
            }
        }

        if (details.getDescription() != null && !details.getDescription().isEmpty()) {
            Label descLabel = new Label(details.getDescription());
            descLabel.getStyleClass().add("tooltip-description");
            descLabel.setWrapText(true);
            content.getChildren().add(descLabel);

            // Only add separator if next section has content
            if ((details.getLore() != null && !details.getLore().isEmpty()) ||
                    details.getPrice() > 0) {
                content.getChildren().add(new Separator());
            }
        }

        if (details.getLore() != null && !details.getLore().isEmpty()) {
            Label loreLabel = new Label(details.getLore());
            loreLabel.getStyleClass().add("tooltip-lore");
            loreLabel.setWrapText(true);
            content.getChildren().add(loreLabel);

            // Only add separator if next section has content
            if (details.getPrice() > 0) {
                content.getChildren().add(new Separator());
            }
        }

        if (details.getPrice() > 0) {
            Label priceLabel = new Label("Price: " + details.getPrice() + " runes");
            priceLabel.getStyleClass().add("tooltip-price");
            priceLabel.setWrapText(true);
            content.getChildren().add(priceLabel);
        }

        // If no content was added, show a default message
        if (content.getChildren().isEmpty()) {
            Label defaultLabel = new Label("No information available");
            defaultLabel.getStyleClass().add("tooltip-description");
            content.getChildren().add(defaultLabel);
        }

        // Set the tooltip properties
        tooltip.setMaxWidth(300);
        tooltip.setMaxHeight(Region.USE_PREF_SIZE); // Allow height to adjust
        content.setMinHeight(Region.USE_PREF_SIZE);
        content.setPrefHeight(Region.USE_COMPUTED_SIZE);
        content.setMaxHeight(Region.USE_PREF_SIZE);

        tooltip.setGraphic(content);
        tooltip.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        tooltip.getStyleClass().add("card-tooltip");

        return tooltip;
    }

    // --- End of Tooltip creation helper section ---

    /**
     * Create a custom tooltip for the selected statue.
     */
    private Tooltip createStatueTooltip(CardDetails details) {
        Tooltip tooltip = new Tooltip();
        tooltip.setShowDelay(Duration.millis(500));
        tooltip.setHideDelay(Duration.millis(200));

        VBox content = new VBox(5);
        content.setPadding(new Insets(8));
        content.setMaxWidth(300);
        content.getStyleClass().add("tooltip-content");

        if (details.getTitle() != null && !details.getTitle().isEmpty()) {
            Label titleLabel = new Label(details.getTitle());
            titleLabel.getStyleClass().add("tooltip-title");
            titleLabel.setWrapText(true);
            content.getChildren().add(titleLabel);
            content.getChildren().add(new Separator());
        }

        if (details.getDescription() != null && !details.getDescription().isEmpty()) {
            Label descLabel = new Label(details.getDescription());
            descLabel.getStyleClass().add("tooltip-description");
            descLabel.setWrapText(true);
            content.getChildren().add(descLabel);
            content.getChildren().add(new Separator());
        }

        if (details.getLore() != null && !details.getLore().isEmpty()) {
            Label loreLabel = new Label(details.getLore());
            loreLabel.getStyleClass().add("tooltip-lore");
            loreLabel.setWrapText(true);
            content.getChildren().add(loreLabel);
            content.getChildren().add(new Separator());
        }

        if (details.getPrice() > 0) {
            Label priceLabel = new Label("Price: " + details.getPrice() + " runes");
            priceLabel.getStyleClass().add("tooltip-price");
            priceLabel.setWrapText(true);
            content.getChildren().add(priceLabel);
        }

        tooltip.setMaxWidth(300);
        tooltip.setMaxHeight(Region.USE_PREF_SIZE); // Adjust height
        content.setMinHeight(Region.USE_PREF_SIZE);
        content.setPrefHeight(Region.USE_COMPUTED_SIZE);
        content.setMaxHeight(Region.USE_PREF_SIZE);

        tooltip.setGraphic(content);
        tooltip.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        tooltip.getStyleClass().add("card-tooltip");

        return tooltip;
    }

    /**
     * Adds drag handlers to the statue card.
     */
    private void setStatueDragHandlers(Node card) {
        // TODO: Implement statue drag handling using a dedicated StatueCardHandler
        // For now, link to the generic handler which will prevent drag if no statue
        // handler exists
        card.setOnDragDetected(this::handleCardDragDetected);
        card.setOnDragDone(this::handleCardDragDone);
    }

    /**
     * Handles drag detection for statue cards.
     * TODO: This logic should move into a dedicated StatueCardHandler.
     */
    private void handleStatueDragDetected(MouseEvent event) {
        // This is currently incorrectly linked directly.
        // It should be handled by handleCardDragDetected -> StatueCardHandler.
        // For now, prevent statue drag until handler is implemented.
        LOGGER.info("Statue drag detected, but handler not yet implemented. Cancelling drag.");
        event.consume();

        /*
         * --- Logic to move to StatueCardHandler ---
         * if (selectedStatue == null || hasPlacedStatue || !canAffordCard("statue")) {
         * event.consume();
         * return;
         * }
         * 
         * Node src = (Node) event.getSource();
         * draggedCardSource = src;
         * 
         * Dragboard db = src.startDragAndDrop(TransferMode.MOVE);
         * ClipboardContent content = new ClipboardContent();
         * // Store the entity ID of the selected statue
         * content.putString("statue" + getEntityIDFromCardDetails(selectedStatue));
         * db.setContent(content);
         * 
         * SnapshotParameters params = new SnapshotParameters();
         * params.setFill(Color.TRANSPARENT);
         * WritableImage snapshot = src.snapshot(params, null);
         * db.setDragView(snapshot, event.getX(), event.getY());
         * 
         * event.consume();
         */
    }

    /**
     * Retrieves the card details for a given card ID.
     * Uses EntityRegistry.getURL(isCard=true) for the card image URL.
     *
     * @param id The ID of the card (e.g., "structure1", "artifact2").
     * @return The CardDetails object containing the card's details.
     * @throws IllegalArgumentException if the card ID is invalid or entity cannot
     *                                  be found.
     */
    public CardDetails getCardDetails(String id) throws IllegalArgumentException {
        int entityID = getEntityID(id); // Can throw IllegalArgumentException
        GameEntity entity = EntityRegistry.getGameEntityOriginalById(entityID);

        if (entity == null) {
            throw new IllegalArgumentException(
                    "Entity not found in registry for ID: " + entityID + " (derived from card ID: " + id + ")");
        }

        // Use isCard=true for the small image shown in the hand
        String cardImageUrl = EntityRegistry.getURL(entityID, true);
        String title = entity.getName();
        String description = entity.getUsage();
        String lore = entity.getDescription();
        int actualPrice = 0;
        int basePrice = entity.getPrice();

        if (basePrice != 0 && gamePlayer != null) { // Check gamePlayer null status
            double priceModifier = gamePlayer.getStatus().get(Status.BuffType.SHOP_PRICE);
            // Ensure modifier is reasonable (e.g., prevent division by zero or negative)
            double effectiveModifier = Math.max(priceModifier, 0.1); // Minimum modifier 0.1 (10x price increase max)
            double adjusted = basePrice / effectiveModifier;
            actualPrice = Math.max(0, (int) Math.round(adjusted)); // Ensure price is never negative
        } else if (basePrice != 0) {
            actualPrice = basePrice; // Use base price if gamePlayer not available yet
        }

        return new CardDetails(entityID, title, description, lore, cardImageUrl, actualPrice);
    }

    /**
     * Maps card IDs (e.g., "structure1", "artifact2") to their corresponding entity
     * IDs.
     *
     * @param id The ID of the card node from FXML.
     * @return The corresponding entity ID.
     * @throws IllegalArgumentException if the card ID format is invalid.
     */
    private int getEntityID(String id) throws IllegalArgumentException {
        if (id == null || id.isEmpty()) {
            throw new IllegalArgumentException("Card ID cannot be null or empty.");
        }

        try {
            if (id.startsWith("artifact")) {
                int index = Integer.parseInt(id.replace("artifact", ""));
                // Artifact indices are 0-based (artifact1 -> index 0)
                if (index < 1 || index > 3) { // Assuming artifact IDs are artifact1, artifact2, artifact3
                    throw new IllegalArgumentException("Invalid artifact index in ID: " + id);
                }
                // Adjust index to be 0-based for list access
                int listIndex = index - 1;
                if (artifacts == null || listIndex < 0 || listIndex >= artifacts.size()
                        || artifacts.get(listIndex) == null) {
                    // If artifact slot is empty or invalid, return a placeholder ID (e.g., empty
                    // slot visual)
                    LOGGER.fine("Artifact slot " + index + " is empty or invalid.");
                    return 22; // ID for the "Empty Artifact Slot" description/visual
                } else {
                    return artifacts.get(listIndex).getId();
                }
            } else if (id.startsWith("structure")) {
                // Structure IDs are 1-based (structure1 -> entity ID 1)
                int structureNum = Integer.parseInt(id.replace("structure", ""));
                if (structureNum < 1 || structureNum > 9) { // Assuming structures 1-9 exist
                    throw new IllegalArgumentException("Invalid structure number in ID: " + id);
                }
                return structureNum; // Structure number directly maps to entity ID
            } else if (id.startsWith("statue")) {
                // If it's the generic statue card placeholder in the UI
                if (id.equals("statue")) {
                    // Return the ID of the selected statue if available, otherwise a
                    // default/placeholder
                    return (selectedStatue != null) ? selectedStatue.getID() : 38; // 38 is a default statue ID
                } else {
                    // If the ID contains a specific statue ID (e.g., "statue39" from drag content)
                    return Integer.parseInt(id.replace("statue", ""));
                }
            } else {
                throw new IllegalArgumentException("Invalid card ID prefix: " + id);
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid number format in card ID: " + id, e);
        } catch (IndexOutOfBoundsException e) {
            throw new IllegalArgumentException("Artifact index out of bounds for ID: " + id, e);
        }
    }

    /*
     * --------------------------------------------------
     * Card Image loading and helpers
     * --------------------------------------------------
     */

    /**
     * Updates the cards in the hands with the correct images.
     */
    private void updateCardImages() {
        // Update artifact cards
        for (Node card : artifactHand.getChildren()) {
            if (card.getId() != null && card.getId().startsWith("artifact")) {
                updateCardImage(card);
                updateCardAffordability(card);
            }
        }

        // Update structure cards
        for (Node card : structureHand.getChildren()) {
            if (card.getId() != null && (card.getId().startsWith("structure"))) {
                updateCardImage(card);
                updateCardAffordability(card);
            } else if (card.getId() != null && card.getId().startsWith("statue")) {
                updateStatueCard(card, selectedStatue);
            }
        }
    }

    /**
     * Refreshes the affordability of all cards in the structure hand.
     * This is called when the game state changes (e.g., when the player
     * gains or loses runes).
     */
    private void refreshCardAffordability() {
        for (Node card : structureHand.getChildren()) {
            if (card.getId() != null && card.getId().startsWith("structure")) {
                updateCardAffordability(card);
            }
        }
    }

    /**
     * Updates a card's visual state based on whether the player can afford it.
     * Makes unaffordable cards gray and non-draggable.
     *
     * @param card The card node to update.
     */
    private void updateCardAffordability(Node card) {
        String id = card.getId();
        if (id == null)
            return;

        boolean canAfford = canAffordCard(id);

        // Apply or remove the CSS class for unaffordable cards
        if (canAfford) {
            card.getStyleClass().remove("unaffordable-card");
            card.getStyleClass().add("game-card");
            // Make sure it's draggable for structures
            if (id.startsWith("structure") || id.startsWith("statue") && !hasPlacedStatue) {
                card.addEventHandler(MouseEvent.DRAG_DETECTED, this::handleCardDragDetected);
            }
        } else {
            card.getStyleClass().remove("game-card");
            card.getStyleClass().add("unaffordable-card");
            // Remove drag handler for unaffordable cards
            if (id.startsWith("structure") || id.startsWith("statue") && hasPlacedStatue) {
                card.removeEventHandler(MouseEvent.DRAG_DETECTED, this::handleCardDragDetected);
            }
        }
    }

    /**
     * Updates a single card in the hand with the correct image using getCardDetails
     * (isCard=true).
     * Uses a cached red placeholder on failure.
     *
     * @param card The card node (Pane) to update.
     */
    private void updateCardImage(Node card) {
        String id = card.getId();
        if (id == null || !(card instanceof Pane pane)) {
            return; // Ignore if ID is null or node is not a Pane
        }

        // Clear existing content first
        pane.getChildren().clear();

        // Set fixed dimensions and default style
        pane.setMinSize(80, 120);
        pane.setPrefSize(80, 120);
        pane.setMaxSize(80, 120);
        pane.setStyle("-fx-border-color: #444444; -fx-border-width: 1px; -fx-border-radius: 5px;"); // Base style

        try {
            CardDetails details = getCardDetails(id); // Uses isCard=true internally
            String imageUrl = details.getImageUrl();

            if (imageUrl != null && !imageUrl.isEmpty()) {
                Image image = resourceLoader.loadImage(imageUrl);

                if (image != null && !image.isError()) {
                    ImageView imageView = new ImageView(image);
                    imageView.setPreserveRatio(true);
                    imageView.setFitWidth(78); // Slightly smaller than pane for border
                    imageView.setFitHeight(118);

                    // Center image in pane using a StackPane wrapper
                    StackPane wrapper = new StackPane(imageView);
                    wrapper.setPrefSize(78, 118); // Match ImageView size
                    StackPane.setAlignment(imageView, Pos.CENTER);

                    pane.getChildren().add(wrapper);
                } else {
                    // Log ERROR for image loading failure
                    LOGGER.severe(String.format("Failed to load card image: %s (Card ID: %s). Using placeholder.",
                            imageUrl, id));
                    addPlaceholderToPane(pane); // Use red placeholder
                }
            } else {
                // Log WARNING/ERROR for missing image URL (might be expected for empty slots)
                if (id.startsWith("artifact") && details.getID() == 22) { // ID 22 is the empty slot placeholder
                    LOGGER.fine(String.format("No image URL for empty artifact slot ID %s. Using placeholder.", id));
                } else {
                    LOGGER.severe(String.format("Missing image URL for card ID %s (Entity ID: %d). Using placeholder.",
                            id, details.getID()));
                }
                addPlaceholderToPane(pane); // Use red placeholder
            }
        } catch (IllegalArgumentException e) {
            // Log ERROR if getCardDetails fails (e.g., invalid ID format, entity not found)
            LOGGER.severe(String.format("Error getting card details for ID '%s': %s. Cannot update card image.", id,
                    e.getMessage()));
            addPlaceholderToPane(pane); // Show error placeholder
        } catch (Exception e) {
            // Log ERROR for any other unexpected exception
            LOGGER.log(Level.SEVERE,
                    String.format("Unexpected error updating card image for ID '%s': %s", id, e.getMessage()), e);
            addPlaceholderToPane(pane); // Show error placeholder
        } finally {
            // Ensure card is visible even if only placeholder is shown
            pane.setVisible(true);
            pane.setOpacity(1.0);
        }
    }

    /**
     * Adds the cached red placeholder node to the given Pane.
     *
     * @param pane The target Pane.
     */
    private void addPlaceholderToPane(Pane pane) {
        if (missingImagePlaceholder != null) {
            // Ensure placeholder is not already parented elsewhere
            if (missingImagePlaceholder.getParent() != null) {
                ((Pane) missingImagePlaceholder.getParent()).getChildren().remove(missingImagePlaceholder);
            }
            // Add the single placeholder instance
            pane.getChildren().add(missingImagePlaceholder);
            // Center it if the pane is a StackPane, otherwise default alignment
            if (pane instanceof StackPane) {
                StackPane.setAlignment(missingImagePlaceholder, Pos.CENTER);
            }
        } else {
            // Fallback if placeholder creation failed
            Label errorLabel = new Label("ERR");
            errorLabel.setStyle("-fx-background-color: red; -fx-text-fill: white; -fx-alignment: center;");
            errorLabel.setPrefSize(78, 118);
            pane.getChildren().add(errorLabel);
        }
    }

    // Remove the old addPlaceholder methods as they created new nodes each time
    // private void addPlaceholder(Pane pane, String title) { ... }
    // private void addPlaceholder(Pane pane, String title, Color bgColor) { ... }

    /**
     * Called when a statue is selected from the popup.
     */
    private void onStatueSelected(CardDetails statueDetails) {
        this.selectedStatue = statueDetails;

        // Update the statue card with the selected statue image
        for (Node card : structureHand.getChildren()) {
            if (card.getId() != null && card.getId().startsWith("statue")) {
                updateStatueCard(card, statueDetails);
            }
        }
    }

    /**
     * Updates the statue card with selected statue image and details.
     */
    private void updateStatueCard(Node card, CardDetails details) {
        if (card instanceof Pane pane) {
            // Clear existing content
            pane.getChildren().clear();

            // Set dimensions
            pane.setMinSize(80, 120);
            pane.setPrefSize(80, 120);
            pane.setMaxSize(80, 120);

            // Add border
            pane.setStyle("-fx-border-color: #444444; -fx-border-width: 1px; -fx-border-radius: 5px;");

            String imageUrl = details.getImageUrl();
            if (imageUrl != null && !imageUrl.isEmpty()) {
                Image image = resourceLoader.loadImage(imageUrl);

                if (image != null && !image.isError()) {
                    ImageView imageView = new ImageView(image);
                    imageView.setPreserveRatio(true);
                    imageView.setFitWidth(78);
                    imageView.setFitHeight(118);

                    StackPane wrapper = new StackPane(imageView);
                    wrapper.setPrefSize(78, 118);

                    pane.getChildren().add(wrapper);
                } else {
                    LOGGER.warning("Failed to load image for statue");
                    addPlaceholderToPane(pane); // Use red placeholder
                }
            } else {
                LOGGER.warning("No image URL for statue");
                addPlaceholderToPane(pane); // Use red placeholder
            }

            // Update tooltip
            Tooltip tooltip = cardTooltips.computeIfAbsent(card, this::createTooltipForCard);
            Tooltip.uninstall(card, tooltip);
            cardTooltips.remove(card);
            cardTooltips.put(card, createStatueTooltip(details));

            // Make sure the card has drag-and-drop handlers
            if (!hasPlacedStatue && canAffordCard("statue")) {
                setStatueDragHandlers(card);
                card.getStyleClass().remove("unaffordable-card");
                card.getStyleClass().add("game-card");
            }
        }
    }

    /**
     * Marks that a statue has been placed and disables the statue card.
     */
    public void markStatuePlaced() {
        hasPlacedStatue = true;

        // Update the statue card to be non-interactive
        for (Node card : structureHand.getChildren()) {
            if (card.getId() != null && card.getId().startsWith("statue")) {
                card.getStyleClass().remove("game-card");
                card.getStyleClass().add("unaffordable-card");
                card.setOnDragDetected(null);
                card.setOnMouseClicked(null);
            }
        }
    }

    /*
     * --------------------------------------------------
     * Update helper methods for drawing the gameState
     * --------------------------------------------------
     */

    public void updateRunesAndEnergyBar() {
        if (gamePlayer != null) {
            runesLabel.setText(gamePlayer.getRunes() + "");
            energyBar.setProgress((double) gamePlayer.getEnergy() / SETTINGS.Config.MAX_ENERGY.getValue());
        } else {
            runesLabel.setText("0");
            energyBar.setProgress(0.0);
        }
    }

    public void updateMap() {
        if (gameState == null) {
            LOGGER.warning("Game state is null");
            return;
        }
        LOGGER.info("Updating map");
        drawMapAndGrid();
    }

    /**
     * Updates the player list display and highlights the active player.
     */
    private void updatePlayerList() {
        if (gameState == null) {
            LOGGER.warning("Game state is null");
            return;
        }

        Platform.runLater(() -> {
            try {
                LOGGER.info("Updating player list with " + gameState.getPlayers().size() + " players");

                // Get current player turn
                String currentPlayerName = gameState.getPlayerTurn();

                // Create a fresh list from game state to avoid stale data
                List<String> currentPlayers = new ArrayList<>();
                for (Player player : gameState.getPlayers()) {
                    currentPlayers.add(player.getName());
                }

                // Clear and rebuild the observable list
                players.clear();
                players.addAll(currentPlayers);

                // Ensure the ListView has the players-list class
                if (!playersList.getStyleClass().contains("players-list")) {
                    playersList.getStyleClass().add("players-list");
                }

                // Set a custom cell factory to highlight the current player
                playersList.setCellFactory(listView -> new ListCell<>() {
                    @Override
                    protected void updateItem(String playerName, boolean empty) {
                        super.updateItem(playerName, empty);

                        if (empty || playerName == null) {
                            setText(null);
                            setGraphic(null);
                            getStyleClass().removeAll("current-player");
                        } else {
                            setText(playerName);
                            Player player = gameState.findPlayerByName(playerName);

                            // Reset styling
                            getStyleClass().removeAll("current-player");

                            // Apply special styling for the current player
                            if (player != null && player.getName().equals(currentPlayerName)) {
                                getStyleClass().add("current-player");
                            }

                            // Add a color indicator for the player
                            if (player != null) {
                                Color playerColor = getPlayerColor(player.getName());
                                if (playerColor != null) {
                                    Circle colorIndicator = new Circle(6);
                                    colorIndicator.setFill(playerColor);
                                    setGraphic(colorIndicator);
                                }
                            }
                        }
                    }
                });

                // Force refresh and ensure visibility
                playersList.refresh();
                playersList.setVisible(true);

                // Check parent containers are visible too
                Node parent = playersList.getParent();
                while (parent != null) {
                    parent.setVisible(true);
                    parent = parent.getParent();
                }

            } catch (Exception e) {
                LOGGER.severe("Error updating player list: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    // Helper methods for better gameState access

    private Tile getTile(int row, int col) {
        return gameState.getBoardManager().getTile(col, row);
    }

    private boolean isTileOwnedByPlayer(int row, int col) {
        return getTile(row, col).hasEntity();
    }

    private boolean canAffordCard(String cardId) {
        int cost = getCardCost(cardId);
        return getPlayerRunes() >= cost;
    }

    private int getCardCost(String cardId) {
        CardDetails details = getCardDetails(cardId);
        return details.getPrice();
    }

    private int getPlayerRunes() {
        return gamePlayer.getRunes();
    }

    private String getTileOwnerId(int row, int col) {
        Tile tile = getTile(row, col);
        return tile == null ? null : tile.getOwner();
    }

    private int getTilePrice(int row, int col) {
        Tile tile = getTile(row, col);
        return tile != null ? tile.getPrice() : 0;
    }

    private Color getPlayerColor(String playerId) {
        return playerId == null ? null : playerColors.getOrDefault(playerId, Color.GRAY);
    }

    /*
     * --------------------------------------------------
     * Convenience API for other components
     * --------------------------------------------------
     */

    /**
     * @return the canvas used for game rendering (needed by the adjustment tool).
     */
    public Canvas getGameCanvas() {
        return gameCanvas;
    }

    /**
     * Shows a transient notification via the chat component. A dedicated toast
     * system might be preferable long‑term, but this suffices for now.
     */
    private void showNotification(String message) {
        LOGGER.info("Notification: " + message);
        chatComponentController.addSystemMessage("Info: " + message);
    }

    /*
     * --------------------------------------------------
     * Unused menu handlers – retained for feature parity
     * --------------------------------------------------
     */

    @FXML
    private void handleResourceOverview() {
        /* TODO implement resource overview */
    }

    @FXML
    private void handleEndTurn() {
        eventBus.publish(new EndTurnRequestEvent(localPlayer.getName()));
    }

    @FXML
    private void handleLeaderboard() {
        /* TODO implement leaderboard */
    }

    /*
     * ==================================================
     * Inner Classes for Extensible Drag & Drop Handling
     * ==================================================
     */

    /**
     * Interface for card drag-and-drop handlers.
     * Allows for extensible handling of different card types.
     */
    private interface CardDragHandler {
        /**
         * Handles the drag detection for a card. Sets up the Dragboard.
         * Consumes the event if drag should start, otherwise lets it propagate.
         */
        void handleDragDetected(Node sourceNode, String cardId, MouseEvent event);

        /**
         * Checks if the card can be dropped at the specified tile during DragOver.
         * Performs validation checks (ownership, occupancy, limits, cost).
         */
        boolean canDropAt(int row, int col, String cardId);

        /**
         * Processes the drop of a card onto a tile. Performs final validation
         * and publishes the relevant event if successful.
         * Returns true if the drop was successfully processed (event published), false
         * otherwise.
         */
        boolean handleDrop(int row, int col, String cardId);

        /**
         * Handles the completion of the drag-and-drop operation on the source node.
         * Called when the drag gesture finishes (successfully or not).
         */
        void handleDragDone(String cardId, boolean wasDroppedSuccessfully);
    }

    /**
     * Implementation of CardDragHandler for structure cards.
     */
    private class StructureCardHandler implements CardDragHandler {
        @Override
        public void handleDragDetected(Node sourceNode, String cardId, MouseEvent event) {
            // 1. Check Affordability
            int cost = -1; // Default to invalid cost
            int runes = -1; // Default to invalid runes
            try {
                cost = getCardCost(cardId); // Can throw IllegalArgumentException
                runes = getPlayerRunes(); // Can be null if gamePlayer not set
                if (runes < cost) {
                    // Log warning and show notification on FX thread
                    final int finalCost = cost; // Need final variable for lambda
                    final int finalRunes = runes;
                    Platform.runLater(() -> {
                        LOGGER.warning(String.format(
                                "Player [%s] cannot afford structure (Card: %s, Cost: %d runes, Has: %d runes). Drag cancelled.",
                                (localPlayer != null ? localPlayer.getName() : "Unknown"), cardId, finalCost,
                                finalRunes));
                        showNotification("You cannot afford this structure (Cost: " + finalCost + " runes).");
                    });
                    event.consume(); // Prevent drag from starting
                    return;
                }
            } catch (IllegalArgumentException e) {
                LOGGER.severe(String.format("Error getting cost for card '%s' during drag start: %s. Drag cancelled.",
                        cardId, e.getMessage()));
                event.consume();
                return;
            } catch (NullPointerException e) {
                LOGGER.warning(String.format(
                        "Cannot check affordability for card '%s': gamePlayer not initialized yet. Drag cancelled.",
                        cardId));
                event.consume();
                return;
            }

            // 2. Log Drag Start
            try {
                int entityId = getEntityID(cardId); // Can throw IllegalArgumentException
                GameEntity entity = EntityRegistry.getGameEntityOriginalById(entityId);
                String entityName = (entity != null) ? entity.getName() : "Unknown Entity";

                LOGGER.info(String.format(
                        "Player [%s] started dragging structure [%s] (Card ID: %s, Entity ID: %d)",
                        (localPlayer != null ? localPlayer.getName() : "Unknown"), entityName, cardId, entityId));

                // 3. Setup Dragboard
                draggedCardSource = sourceNode; // Keep track of the source
                Dragboard db = sourceNode.startDragAndDrop(TransferMode.MOVE);
                ClipboardContent content = new ClipboardContent();
                content.putString(cardId); // Put the FXML ID of the card
                db.setContent(content);

                // Create a snapshot for the drag view
                SnapshotParameters params = new SnapshotParameters();
                params.setFill(Color.TRANSPARENT);
                WritableImage snapshot = sourceNode.snapshot(params, null);
                db.setDragView(snapshot, event.getX(), event.getY()); // Position relative to cursor

                event.consume(); // Consume event to indicate drag has started

            } catch (IllegalArgumentException e) {
                LOGGER.severe(
                        String.format("Error getting entity ID for card '%s' during drag start: %s. Drag cancelled.",
                                cardId, e.getMessage()));
                event.consume(); // Prevent drag if ID is invalid
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, String.format("Unexpected error during structure drag start for card '%s': %s",
                        cardId, e.getMessage()), e);
                event.consume(); // Prevent drag on unexpected error
            }
        }

        @Override
        public boolean canDropAt(int row, int col, String cardId) {
            // Basic null checks
            if (localPlayer == null || gameState == null || gamePlayer == null) {
                LOGGER.warning(
                        "canDropAt check failed: Crucial game state (localPlayer, gameState, or gamePlayer) is null.");
                return false;
            }

            Tile targetTile = getTile(row, col);
            if (targetTile == null) {
                // This might happen if coordinates are slightly off, log finely if needed
                // LOGGER.finest("canDropAt check failed: Target tile [%d,%d] is null in game
                // state.", row, col);
                return false;
            }

            // --- Validation in Strict Order ---

            // 1. Ownership check
            String tileOwnerId = targetTile.getOwner();
            if (tileOwnerId == null || !tileOwnerId.equals(localPlayer.getId())) {
                // Silently fail during drag over, log on drop attempt
                return false;
            }

            // 2. Occupancy check (allow placing over artifacts)
            if (targetTile.hasEntity() && !targetTile.getEntity().isArtifact()) {
                // Silently fail during drag over, log on drop attempt
                return false;
            }

            // 3. Structure Limit
            // TODO: Verify if getOwnedTiles().size() correctly counts placed structures.
            // It might count all owned tiles, which would be incorrect for this limit.
            int currentCount = localPlayer.getOwnedTiles().size();
            int maxAllowed = SETTINGS.Config.MAX_STRUCTURES.getValue();
            if (currentCount >= maxAllowed) {
                LOGGER.warning(String.format(
                        "Placement failed: Player [%s] attempted to place structure [%s] but already has %d/%d structures",
                        localPlayer.getName(), currentCount, maxAllowed));
                Platform.runLater(() -> showNotification(
                        "You have reached the maximum number of structures (" + maxAllowed + ")."));
                return false;
            }

            // 4. Affordability check (redundant if checked on drag start, but good safety
            // check)
            try {
                if (!canAffordCard(cardId)) {
                    // Silently fail during drag over, log on drop attempt
                    return false;
                }
            } catch (Exception e) {
                LOGGER.warning("Error checking affordability during canDropAt: " + e.getMessage());
                return false; // Fail safe if cost check fails
            }

            // All checks passed
            return true;
        }

        @Override
        public boolean handleDrop(int row, int col, String cardId) {
            // Perform final validation checks before publishing event
            if (localPlayer == null || gameState == null || gamePlayer == null) {
                // Log ERROR: Critical state missing
                LOGGER.severe(String.format(
                        "Failed to send PlaceStructureUIEvent: Critical game state missing (Player: %s, GameState: %s, GamePlayer: %s) for CardID [%s] at Tile (%d, %d)",
                        localPlayer != null ? localPlayer.getName() : "null",
                        gameState != null ? "present" : "null",
                        gamePlayer != null ? "present" : "null",
                        cardId, col, row));
                return false;
            }

            int entityId;
            GameEntity entity;
            try {
                entityId = getEntityID(cardId);
                entity = EntityRegistry.getGameEntityOriginalById(entityId);
                if (entity == null)
                    throw new NullPointerException("Entity not found in registry for ID: " + entityId);
            } catch (IllegalArgumentException | NullPointerException e) {
                // Log ERROR: Invalid entity data
                LOGGER.severe(String.format(
                        "Failed to send PlaceStructureUIEvent: Invalid entity data for CardID [%s] at Tile (%d, %d). Error: %s",
                        cardId, col, row, e.getMessage()));
                Platform.runLater(() -> showNotification("Error: Invalid structure data."));
                return false;
            }

            String entityName = entity.getName();
            Tile targetTile = getTile(row, col);

            // Should not happen if canDropAt was checked, but validate again
            if (targetTile == null) {
                // Log ERROR: Tile became null
                LOGGER.severe(String.format(
                        "Failed to send PlaceStructureUIEvent: Target Tile (%d, %d) became null for CardID [%s]",
                        col, row, cardId));
                return false;
            }

            // --- Validation in Strict Order (with Logging and Notifications) ---

            // 1. Ownership
            String tileOwnerName = targetTile.getOwner();
            if (tileOwnerName == null || !tileOwnerName.equals(localPlayer.getId())) {
                String ownerName = "None";
                if (tileOwnerName != null) {
                    Player owner = gameState.findPlayerByName(tileOwnerName);
                    ownerName = (owner != null) ? owner.getName() : "Unknown (" + tileOwnerName + ")";
                }
                // Log ERROR: Ownership validation failed
                LOGGER.severe(String.format(
                        "Failed to send PlaceStructureUIEvent: Player [%s] attempted to place EntityID [%d] (%s) on Tile (%d, %d) owned by [%s]",
                        localPlayer.getName(), entityId, entityName, col, row, ownerName));
                Platform.runLater(() -> showNotification("You can only place structures on tiles you own."));
                return false;
            }

            // 2. Occupancy (allow placing over artifacts)
            if (targetTile.hasEntity() && !targetTile.getEntity().isArtifact()) {
                GameEntity existingEntity = targetTile.getEntity();
                String existingName = (existingEntity != null) ? existingEntity.getName() : "Unknown";
                // Log ERROR: Occupancy validation failed
                LOGGER.severe(String.format(
                        "Failed to send PlaceStructureUIEvent: Player [%s] attempted to place EntityID [%d] (%s) on Tile (%d, %d) already occupied by [%s]",
                        localPlayer.getName(), entityId, entityName, col, row, existingName));
                Platform.runLater(() -> showNotification("This tile is already occupied by a structure or statue."));
                return false;
            }

            // 3. Structure Limit
            // TODO: Verify if getOwnedTiles().size() correctly counts placed structures.
            // It might count all owned tiles, which would be incorrect for this limit.
            int currentCount = localPlayer.getOwnedTiles().size();
            int maxAllowed = SETTINGS.Config.MAX_STRUCTURES.getValue();
            if (currentCount >= maxAllowed) {
                // Log ERROR: Structure limit validation failed
                LOGGER.severe(String.format(
                        "Failed to send PlaceStructureUIEvent: Player [%s] attempted to place EntityID [%d] (%s) on Tile (%d, %d) but already has %d/%d structures",
                        localPlayer.getName(), entityId, entityName, col, row, currentCount, maxAllowed));
                Platform.runLater(() -> showNotification(
                        "You have reached the maximum number of structures (" + maxAllowed + ")."));
                return false;
            }

            // 4. Affordability
            int cost = -1;
            int runes = -1;
            try {
                cost = getCardCost(cardId);
                runes = getPlayerRunes();
                if (runes < cost) {
                    // Log ERROR: Affordability validation failed
                    LOGGER.severe(String.format(
                            "Failed to send PlaceStructureUIEvent: Player [%s] cannot afford EntityID [%d] (%s) on Tile (%d, %d) (Cost: %d, Has: %d)",
                            localPlayer.getName(), entityId, entityName, col, row, cost, runes));
                    final int finalCost = cost; // For lambda
                    Platform.runLater(
                            () -> showNotification("You cannot afford this structure (Cost: " + finalCost + ")."));
                    return false;
                }
            } catch (Exception e) {
                // Log ERROR: Cost check failed
                LOGGER.severe(String.format(
                        "Failed to send PlaceStructureUIEvent: Error checking cost for EntityID [%d] (%s) on Tile (%d, %d). Error: %s",
                        entityId, entityName, col, row, e.getMessage()));
                Platform.runLater(() -> showNotification("Error checking structure cost."));
                return false;
            }

            // All checks passed - Log sending the event
            LOGGER.info(String.format(
                    "Sending PlaceStructureUIEvent: Player [%s] placing EntityID [%d] (%s) on Tile (%d, %d)",
                    localPlayer.getName(), entityId, entityName, col, row));

            // Publish event on the event bus (assumed to handle communication)
            // Note: Event uses (x,y) which corresponds to (col,row)
            eventBus.publish(new PlaceStructureUIEvent(col, row, entityId));

            // UI update (drawing the entity) will happen automatically when GameSyncEvent
            // is received and processed.
            // The structure card in the hand remains.

            return true; // Indicate successful processing
        }

        @Override
        public void handleDragDone(String cardId, boolean wasDroppedSuccessfully) {
            // This method is called on the source node after the drop completes.
            // For structures, the card remains in the hand. Log the outcome.
            // NOTE: If structures should be consumed, logic needs to be added here
            // to remove/disable the card from the hand visually or functionally.
            try {
                int entityId = getEntityID(cardId);
                GameEntity entity = EntityRegistry.getGameEntityOriginalById(entityId);
                String entityName = (entity != null) ? entity.getName() : "Unknown";

                if (wasDroppedSuccessfully) {
                    LOGGER.info(String.format(
                            "Structure drag successful for [%s] (Card ID: %s, Entity ID: %d) by player [%s]",
                            entityName, cardId, entityId, (localPlayer != null ? localPlayer.getName() : "Unknown")));
                    // Refresh affordability in case costs changed or runes were spent
                    Platform.runLater(GameScreenController.this::refreshCardAffordability);
                } else {
                    LOGGER.info(String.format(
                            "Structure drag cancelled/failed for [%s] (Card ID: %s, Entity ID: %d) by player [%s]",
                            entityName, cardId, entityId, (localPlayer != null ? localPlayer.getName() : "Unknown")));
                }
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE,
                        String.format("Error during structure drag done for card '%s': %s", cardId, e.getMessage()), e);
            }
            // Resetting draggedCardSource is handled in the main handleCardDragDone method
        }
    } // End of StructureCardHandler

    // TODO: Implement ArtifactCardHandler following the CardDragHandler interface
    // Add logging for sending UseFieldArtifactUIEvent / UsePlayerArtifactUIEvent
    // here
    // private class ArtifactCardHandler implements CardDragHandler { ... }

    // TODO: Implement StatueCardHandler following the CardDragHandler interface
    // Add logging for sending PlaceStatueUIEvent here
    // private class StatueCardHandler implements CardDragHandler { ... }

} // End of GameScreenController
