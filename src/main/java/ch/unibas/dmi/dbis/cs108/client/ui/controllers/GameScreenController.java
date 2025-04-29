package ch.unibas.dmi.dbis.cs108.client.ui.controllers;

import ch.unibas.dmi.dbis.cs108.SETTINGS;
import ch.unibas.dmi.dbis.cs108.client.app.GameApplication;
import ch.unibas.dmi.dbis.cs108.client.core.PlayerIdentityManager;
import ch.unibas.dmi.dbis.cs108.client.core.state.GameState;
import ch.unibas.dmi.dbis.cs108.client.ui.SceneManager;
import ch.unibas.dmi.dbis.cs108.client.ui.components.ChatComponent;
import ch.unibas.dmi.dbis.cs108.client.ui.components.SettingsDialog;
import ch.unibas.dmi.dbis.cs108.client.ui.components.WinScreenDialog;
import ch.unibas.dmi.dbis.cs108.client.ui.components.game.GridAdjustmentManager;
import ch.unibas.dmi.dbis.cs108.client.ui.components.game.ResourceOverviewDialog;
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
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.stage.Window;
import javafx.util.Duration;

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
    private double vSpacing;
    private double hSpacing;

    /*
     * The following fields are package‑private because the adjustment manager
     * accesses them directly.
     */
    double effectiveHexSize;
    double gridOffsetX;
    double gridOffsetY;

    private String currentLobbyId;

    private Image mapImage;
    private boolean isMapLoaded;

    private SettingsDialog settingsDialog;
    private Node selectedCard;
    private CardDetails selectedStatue;
    private boolean hasPlacedStatue = false;
    private Tile highlightedTile = null;
    private int[] lastHighlightedTileCoords = null;

    // Tooltips for cards are cached to avoid recreating them on every hover event
    private final Map<Node, Tooltip> cardTooltips = new HashMap<>();

    private GridAdjustmentManager gridAdjustmentManager;

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
    private ResourceOverviewDialog resourceOverviewDialog;

    // Grid‑adjustment overlay controls (created programmatically)
    private Label adjustmentModeIndicator;
    private Label adjustmentValuesLabel;

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
        GameEntity defaultStatueEntity = EntityRegistry.getGameEntityOriginalById(30);
        if (defaultStatueEntity != null) {
            selectedStatue = new CardDetails(defaultStatueEntity, true);
        } else {
            LOGGER.severe("Failed to load default statue entity (ID 38).");
            selectedStatue = new CardDetails(EntityRegistry.getGameEntityOriginalById(38), true);
        }
        Logger.getGlobal().info("GameScreenController created and subscribed to events.");
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
     */
    private void initializeUI() {

        if (localPlayer == null) {
            LOGGER.severe("LocalPlayer is null during GameScreenController initialisation!");
            localPlayer = new Player("ErrorGuest"); // Fail‑safe stub
        }


        createAdjustmentUI();
        gridAdjustmentManager = new GridAdjustmentManager(
                this,
                adjustmentModeIndicator,
                adjustmentValuesLabel,
                this::drawMapAndGrid);

        initialisePlayerColours();
        resourceOverviewDialog = new ResourceOverviewDialog(resourceLoader, playerColors);

        setupUI();
        loadMapImage();
        updateCardImages();
        setupCanvasListeners();
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
        eventBus.subscribe(GameSyncEvent.class, this::handleGameSync);
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
        chatComponentController.setInGame(true);
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
            markStatuePlaced(gamePlayer.hasStatue());

            updateRunesAndEnergyBar();
            refreshCardAffordability();
            updateCardImages();
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
                chatComponentController.setInGame(false);
            });
            dialog.setOnLobbyAction(() -> {
                eventBus.publish(new LeaveLobbyRequestEvent(currentLobbyId));
                sceneManager.switchToScene(SceneManager.SceneType.LOBBY);
                chatComponentController.setInGame(false);
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
        eventBus.unsubscribe(LobbyJoinedEvent.class, this::handleLobbyJoined);
        eventBus.unsubscribe(ErrorEvent.class, this::handleError);
        eventBus.unsubscribe(EndGameEvent.class, this::handleEndGame);

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
        // Add null check here
        if (gridAdjustmentManager != null && gridAdjustmentManager.handleKeyboardShortcut(e)) {
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
        gameCanvas.addEventHandler(MouseEvent.MOUSE_PRESSED, e ->
                handleCanvasClick(e.getX(), e.getY())
        );

        // Double click handler - for purchases
        gameCanvas.addEventHandler(MouseEvent.MOUSE_CLICKED, e -> {
            if (e.getClickCount() == 2) {
                handleCanvasDoubleClick(e.getX(), e.getY());
            }
        });

        gameCanvas.addEventHandler(MouseEvent.MOUSE_MOVED, e ->
                handleCanvasMouseMove(e.getX(), e.getY())
        );

        gameCanvas.addEventHandler(MouseEvent.MOUSE_ENTERED_TARGET, e ->
                handleCanvasEntered(e.getX(), e.getY())
        );

        gameCanvas.addEventHandler(MouseEvent.MOUSE_EXITED_TARGET, e -> {
            if (lastHighlightedTileCoords != null) {
                redrawSingleTile(lastHighlightedTileCoords[0], lastHighlightedTileCoords[1], false);
            }
            highlightedTile = null;
            lastHighlightedTileCoords = null;
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

            parent.addEventHandler(MouseEvent.MOUSE_MOVED, ev -> {
                        Point2D local = gameCanvas.sceneToLocal(ev.getSceneX(), ev.getSceneY());
                        if (local.getX() >= 0 && local.getY() >= 0 &&
                                local.getX() <= gameCanvas.getWidth() && local.getY() <= gameCanvas.getHeight()) {
                            handleCanvasMouseMove(local.getX(), local.getY());
                            ev.consume();
                        }
                    }
            );

            parent.addEventHandler(MouseEvent.MOUSE_ENTERED_TARGET, ev -> {
                        Point2D local = gameCanvas.sceneToLocal(ev.getSceneX(), ev.getSceneY());
                        if (local.getX() >= 0 && local.getY() >= 0 &&
                                local.getX() <= gameCanvas.getWidth() && local.getY() <= gameCanvas.getHeight()) {
                            handleCanvasEntered(local.getX(), local.getY());
                            ev.consume();
                        }
                    }
            );

            parent.addEventHandler(MouseEvent.MOUSE_EXITED_TARGET, ev -> {
                // Check if the exit event is still within the canvas bounds; if so, ignore.
                // This prevents flicker when moving quickly near the edge.
                Point2D local = gameCanvas.sceneToLocal(ev.getSceneX(), ev.getSceneY());
                boolean trulyExited = local.getX() < 0 || local.getY() < 0 ||
                        local.getX() > gameCanvas.getWidth() || local.getY() > gameCanvas.getHeight();

                if (trulyExited && lastHighlightedTileCoords != null) {
                    redrawSingleTile(lastHighlightedTileCoords[0], lastHighlightedTileCoords[1], false);
                    highlightedTile = null;
                    lastHighlightedTileCoords = null;
                    // Don't consume here, let the canvas handler potentially catch it too if needed.
                }
                // If not truly exited, the MOUSE_MOVED handler will manage the highlight.
            });
        }

        gameCanvas.setOnKeyPressed(gridAdjustmentManager::handleGridAdjustmentKeys);
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
        eventBus.publish(new TileClickEvent(row, col));

        if (localPlayer != null && getTile(row, col) != null) {
            Tile t = getTile(row, col);
            if (selectedCard != null) {
                if (!t.hasEntity()) {
                    CardDetails s = getCardDetails(selectedCard.getId());
                    if (s != null && s.getID() == selectedStatue.getID()) {
                        eventBus.publish(new PlaceStatueUIEvent(col, row, s.getID()));
                    } else if (s != null) {
                        eventBus.publish(new PlaceStructureUIEvent(col, row, s.getID()));
                    }
                    selectedCard.getStyleClass().remove("selected-card");
                    selectedCard = null;
                    updateCardImages();
                }
            }
        }

        /**
         // Handle interaction with structures/statues
         Tile clickedTile = getTile(row, col);
         if (clickedTile != null && clickedTile.hasEntity()) {
         // Only allow interaction if the tile is owned by the current player
         if (localPlayer != null && clickedTile.getOwner() != null &&
         clickedTile.getOwner().equals(localPlayer.getName())) {

         int entityId = clickedTile.getEntity().getId();

         // Check if it's a rune table (structure)
         if (entityId == 1) { // Rune Table id
         RuneTableInteractionPopup popup = new RuneTableInteractionPopup(
         resourceLoader,
         clickedTile,
         localPlayer.getName(),
         this::useRuneTable);
         popup.showNear(gameCanvas);
         }
         // Check if it's a statue
         else if (entityId >= 30 && entityId <= 37) { // Adjust ID range as needed for statues
         StatueInteractionPopup popup = new StatueInteractionPopup(
         resourceLoader,
         clickedTile,
         localPlayer.getName(),
         this::levelUpStatue,
         this::makeStatueDeal,
         this::receiveStatueBlessing);
         popup.showNear(gameCanvas);
         }
         }
         }
         */
    }

    /**
     * Handles a double click on the canvas – attempts to purchase the tile
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
        } else if (localPlayer != null && ownerId.equals(localPlayer.getName())) {
            if (getTile(row, col) != null) {
                Tile t = getTile(row, col);
                if (t.hasEntity() && t.getEntity().getId() == 1) {
                    eventBus.publish(new UseStructureUIEvent(col, row, t.getEntity().getId()));
                }
            } else {
                showNotification("You already own this tile.");
            }
        } else {
            showNotification("This tile is owned by another player.");
        }

        if (selectedCard != null) {
            selectedCard = null;
            updateCardImages();
        }
    }

    /**
     * Handles mouse entering the canvas - highlights the tile under the cursor
     */
    private void handleCanvasEntered(double px, double py) {
        // Directly handle highlight here instead of calling handleCanvasEntered
        int[] tileCoords = getHexAt(px, py);
        if (tileCoords != null) {
            int row = tileCoords[0];
            int col = tileCoords[1];
            Tile tileUnderCursor = getTile(row, col);
            if (tileUnderCursor != null) {
                // Ensure no previous highlight is lingering if mouse exited/entered quickly
                if (lastHighlightedTileCoords != null && (lastHighlightedTileCoords[0] != row || lastHighlightedTileCoords[1] != col)) {
                    redrawSingleTile(lastHighlightedTileCoords[0], lastHighlightedTileCoords[1], false);
                }
                redrawSingleTile(row, col, true);
                highlightedTile = tileUnderCursor;
                lastHighlightedTileCoords = new int[]{row, col};
            }
        } else {
            // Entered canvas but not over a tile, clear any old highlight
            if (lastHighlightedTileCoords != null) {
                redrawSingleTile(lastHighlightedTileCoords[0], lastHighlightedTileCoords[1], false);
                lastHighlightedTileCoords = null;
            }
            highlightedTile = null;
        }
    }

    /**
     * Handles mouse movement within the canvas, highlighting the tile under the cursor
     * by redrawing only the affected tiles.
     */
    private void handleCanvasMouseMove(double px, double py) {
        int[] tileCoords = getHexAt(px, py);

        if (tileCoords == null) {
            // Cursor moved off the grid, remove highlight from the last tile
            if (lastHighlightedTileCoords != null) {
                redrawSingleTile(lastHighlightedTileCoords[0], lastHighlightedTileCoords[1], false);
                highlightedTile = null;
                lastHighlightedTileCoords = null;
            }
            return;
        }

        int row = tileCoords[0];
        int col = tileCoords[1];
        Tile tileUnderCursor = getTile(row, col); // Assuming getTile uses col, row

        // Check if the highlighted tile actually changed
        boolean needsRedraw = false;
        if (highlightedTile == null && tileUnderCursor != null) {
            needsRedraw = true; // Moving onto a tile for the first time
        } else if (highlightedTile != null && tileUnderCursor == null) {
            needsRedraw = true; // Moving off a tile (handled above, but good check)
        } else if (highlightedTile != null && tileUnderCursor != null &&
                (highlightedTile.getX() != tileUnderCursor.getX() || highlightedTile.getY() != tileUnderCursor.getY())) {
            needsRedraw = true; // Moving from one tile to another
        } else if (highlightedTile == null && lastHighlightedTileCoords != null) {
            // Case where cursor re-enters the last highlighted tile after leaving grid
            needsRedraw = true;
        }

        if (needsRedraw) {
            // Remove highlight from the previously highlighted tile
            if (lastHighlightedTileCoords != null) {
                // Avoid redrawing the same tile if it's the target
                if (lastHighlightedTileCoords[0] != row || lastHighlightedTileCoords[1] != col) {
                    redrawSingleTile(lastHighlightedTileCoords[0], lastHighlightedTileCoords[1], false);
                }
            }

            // Add highlight to the new tile
            if (tileUnderCursor != null) {
                redrawSingleTile(row, col, true);
                highlightedTile = tileUnderCursor;
                lastHighlightedTileCoords = new int[]{row, col};
            } else {
                // If tileUnderCursor is null but we determined a redraw is needed,
                // it means we are moving off the grid. Clear the state.
                highlightedTile = null;
                lastHighlightedTileCoords = null;
            }
        }
    }

    /**
     * Redraws a single tile by first restoring the background map image and then redrawing the hex.
     *
     * @param row      The row of the tile.
     * @param col      The column of the tile.
     * @param selected Whether the tile should be drawn with selection highlight.
     */
    private void redrawSingleTile(int row, int col, boolean selected) {
        if (effectiveHexSize <= 0 || gridAdjustmentManager == null || !isMapLoaded) {
            LOGGER.finer("Cannot redraw tile, invalid parameters or map not loaded");
            return;
        }

        GraphicsContext gc = gameCanvas.getGraphicsContext2D();

        // Save ALL original graphics state
        Paint originalFill = gc.getFill();
        Paint originalStroke = gc.getStroke();
        double originalLineWidth = gc.getLineWidth();
        double originalGlobalAlpha = gc.getGlobalAlpha();

        // Calculate positioning
        double cx = gridOffsetX + col * hSpacing + (row % 2) * (hSpacing / 2);
        double cy = gridOffsetY + row * vSpacing;

        double hSquish = gridAdjustmentManager.getHorizontalSquishFactor();
        double vSquish = gridAdjustmentManager.getVerticalSquishFactor();
        double clearRadius = effectiveHexSize * Math.max(hSquish, vSquish) * 1.2; // 20% margin

        double x = cx - clearRadius;
        double y = cy - clearRadius;
        double width = clearRadius * 2;
        double height = clearRadius * 2;

        // Restore the background by drawing from the original map image
        double sourceX = (x - mapOffsetX) / scaledMapWidth * mapImage.getWidth();
        double sourceY = (y - mapOffsetY) / scaledMapHeight * mapImage.getHeight();
        double sourceWidth = width / scaledMapWidth * mapImage.getWidth();
        double sourceHeight = height / scaledMapHeight * mapImage.getHeight();

        // Ensure we stay within bounds for both source and destination
        gc.drawImage(
                mapImage,
                Math.max(0, sourceX), Math.max(0, sourceY),
                Math.min(sourceWidth, mapImage.getWidth() - Math.max(0, sourceX)),
                Math.min(sourceHeight, mapImage.getHeight() - Math.max(0, sourceY)),
                Math.max(0, x), Math.max(0, y),
                Math.min(width, gameCanvas.getWidth() - Math.max(0, x)),
                Math.min(height, gameCanvas.getHeight() - Math.max(0, y))
        );

        // Set exact states as in drawHexGrid()
        gc.setGlobalAlpha(0.7);
        gc.setStroke(Color.WHITE);
        gc.setLineWidth(1.5);

        // Draw the hex with current settings
        drawHex(gc, cx, cy, effectiveHexSize, row, col, selected);

        // Fully restore original state to prevent any state leakage
        gc.setFill(originalFill);
        gc.setStroke(originalStroke);
        gc.setLineWidth(originalLineWidth);
        gc.setGlobalAlpha(originalGlobalAlpha);
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

        hSpacing = size * gridAdjustmentManager.getHorizontalSpacingFactor();
        vSpacing = size * gridAdjustmentManager.getVerticalSpacingFactor();

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
                // Use coordinate comparison for selection
                boolean selected = false;
                if (highlightedTile != null) {
                    Tile t = getTile(r, c);
                    if (t != null &&
                            t.getX() == highlightedTile.getX() &&
                            t.getY() == highlightedTile.getY()) {
                        selected = true;
                    }
                }
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
            double oldWidth = gc.getLineWidth();
            gc.setStroke(Color.YELLOW);
            gc.setLineWidth(3);
            gc.setGlobalAlpha(1.0);
            gc.stroke();
            gc.setStroke(oldStroke);
            gc.setLineWidth(oldWidth);
            gc.setGlobalAlpha(oldAlpha);
        } else {
            gc.stroke();
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
            LOGGER.warning("Tile is null for row " + row + ", col " + col);
        }
    }

    /**
     * Draws an entity image centered in a hex tile.
     * The image is scaled to fit the hex width while preserving its aspect ratio.
     * Use EntityRegistry.getURL(isCard=true) for loading the image with a red
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
        double placeholderX = centerX - placeholderWidth / 2;
        double placeholderY = centerY - placeholderWidth / 2;

        // Helper function to draw placeholder
        Runnable drawPlaceholder = () -> {
            Paint oldFill = gc.getFill();
            double oldAlpha = gc.getGlobalAlpha();
            gc.setFill(Color.RED); // Use red for placeholder
            gc.setGlobalAlpha(1.0); // Ensure placeholder is opaque
            gc.fillRect(placeholderX, placeholderY, placeholderWidth, placeholderWidth);
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
            double maxWidth = 2.3 * hexSize * hSquish;

            // Calculate scale to fit within both max width and max height
            double scale = maxWidth / image.getWidth();

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
     * any tile.
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
                    return new int[]{r, c};
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
     * Card tooltip & tile choose handling
     * --------------------------------------------------
     */

    /**
     * Toggle card selection (golden frame) when clicked.
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
     * Handles the selection of a statue from the popup.
     */
    private void handleStatueSelectButtonClick(MouseEvent event) {
        // Stop event propagation to prevent triggering the card click handler
        event.consume();

        // Get the source button
        Node source = (Node) event.getSource();

        // Create the statue selection popup
        StatueSelectionPopup statuePopup = new StatueSelectionPopup(resourceLoader, this::onStatueSelected);

        // Position and show the popup near the button
        Window window = source.getScene().getWindow();
        Point2D point = source.localToScreen(new Point2D(0, 0));
        statuePopup.show(window, point.getX(), point.getY() + source.getBoundsInLocal().getHeight());

        LOGGER.info("Statue selection popup opened");
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

    /*
     * --------------------------------------------------
     * Tooltip creation helper
     * --------------------------------------------------
     */
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
        CardDetails details;
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

            // Only add separator if the next section has content
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

            // Only add separator if the next section has content
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

            // Only add separator if the next section has content
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
        tooltip.setMaxHeight(200);
        content.setMinHeight(Region.USE_PREF_SIZE);
        content.setPrefHeight(Region.USE_COMPUTED_SIZE);
        content.setMaxHeight(Region.USE_PREF_SIZE);

        tooltip.setGraphic(content);
        tooltip.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        tooltip.getStyleClass().add("card-tooltip");

        return tooltip;
    }


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
        tooltip.setMaxHeight(200);
        content.setMinHeight(Region.USE_PREF_SIZE);
        content.setPrefHeight(Region.USE_COMPUTED_SIZE);
        content.setMaxHeight(Region.USE_PREF_SIZE);

        tooltip.setGraphic(content);
        tooltip.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        tooltip.getStyleClass().add("card-tooltip");

        return tooltip;
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
        refreshCardAffordability();
        // Update artifact cards
        for (Node card : artifactHand.getChildren()) {
            if (card.getId() != null && card.getId().startsWith("artifact")) {
                updateCardImage(card);
            }
        }

        // Update structure cards
        for (Node card : structureHand.getChildren()) {
            if (card.getId() != null && (card.getId().startsWith("structure"))) {
                updateCardImage(card);
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
            if (card.getId() != null && card.getId().startsWith("structure") ||
                    card.getId().startsWith("statue")) {
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
                // TODO set card to clickable
            }
        } else {
            card.getStyleClass().remove("game-card");
            card.getStyleClass().add("unaffordable-card");
            // Remove drag handler for unaffordable cards
            if (id.startsWith("structure") || id.startsWith("statue") && hasPlacedStatue) {
                // TODO set card to not clickable
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
            // Ensure the card is visible even if only placeholder is shown
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
        Node missingImagePlaceholder = createPlaceHolderNode();
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

    /**
     * Creates a red placeholder node for missing images.
     * This is a cached instance to avoid creating multiple identical nodes.
     *
     * @return A red rectangle as a placeholder.
     */
    private Node createPlaceHolderNode() {
        Rectangle placeholder = new Rectangle(78, 118);
        placeholder.setFill(Color.RED);
        placeholder.setOpacity(0.5); // Semi-transparent
        return placeholder;
        // TODO add actual placeholder image
    }

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
            pane.getChildren().clear();

            try {
                // Load the statue image first (your existing code)
                String imageUrl = details.getImageUrl();
                if (imageUrl != null && !imageUrl.isEmpty()) {
                    Image image = resourceLoader.loadImage(imageUrl);
                    if (image != null && !image.isError()) {
                        ImageView imageView = new ImageView(image);
                        imageView.setFitWidth(78);
                        imageView.setFitHeight(118);
                        imageView.setPreserveRatio(true);
                        imageView.setSmooth(true);
                        pane.getChildren().add(imageView);
                    } else {
                        addPlaceholderToPane(pane);
                    }
                } else {
                    addPlaceholderToPane(pane);
                }

                // Create and add the selection indicator button
                Button statueSelectButton = new Button("⚙"); // Gear icon or you can use another symbol
                statueSelectButton.getStyleClass().add("statue-select-button");
                statueSelectButton.setMinSize(24, 24);
                statueSelectButton.setPrefSize(24, 24);
                statueSelectButton.setMaxSize(24, 24);
                statueSelectButton.setTooltip(new Tooltip("Select different statue"));

                // Position in top-right corner
                StackPane.setAlignment(statueSelectButton, Pos.TOP_RIGHT);
                StackPane.setMargin(statueSelectButton, new Insets(2, 2, 0, 0));

                // Add the selection button to the card
                pane.getChildren().add(statueSelectButton);

                // Add event handler to the button
                statueSelectButton.setOnMouseClicked(this::handleStatueSelectButtonClick);

                // Update tooltip
                Tooltip tooltip = createStatueTooltip(details);
                Tooltip.install(pane, tooltip);
                cardTooltips.put(pane, tooltip);

            } catch (Exception e) {
                LOGGER.severe("Error updating statue card: " + e.getMessage());
                addPlaceholderToPane(pane);
            }
        }
    }

    /**
     * Marks that a statue has been placed and disables the statue card.
     */
    public void markStatuePlaced(boolean hasPlacedStatue) {
        this.hasPlacedStatue = hasPlacedStatue;

        // Update the statue card to be non-interactive
        for (Node card : structureHand.getChildren()) {
            if (card.getId() != null && card.getId().startsWith("statue")) {
                if (hasPlacedStatue) {
                    card.getStyleClass().remove("game-card");
                    card.getStyleClass().add("unaffordable-card");
                    card.setOnMouseClicked(null);
                } else {
                    card.getStyleClass().remove("unaffordable-card");
                    card.getStyleClass().add("game-card");
                    card.setOnMouseClicked(this::handleCardClick);
                }
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
     * system might be preferable long term, but this suffices for now.
     */
    private void showNotification(String message) {
        LOGGER.info("Notification: " + message);
        chatComponentController.addSystemMessage("Info: " + message);
    }

    /*
     * --------------------------------------------------------
     * Menu handlers and event handlers for game interactions
     * --------------------------------------------------------
     */

    /**
     * Handles the resource overview button click.
     */
    @FXML
    private void handleResourceOverview() {
        if (gameState == null) {
            showNotification("Game state not available");
            return;
        }

        if (resourceOverviewDialog == null) {
            resourceOverviewDialog = new ResourceOverviewDialog(resourceLoader, playerColors);
        }

        Pane root = (StackPane) gameCanvas.getParent();

        // Update the popup with current players
        resourceOverviewDialog.updatePlayers(gameState.getPlayers(), gameState.getPlayerTurn());
        showDialogAsOverlay(resourceOverviewDialog, root);
    }

    @FXML
    private void handleEndTurn() {
        eventBus.publish(new EndTurnRequestEvent(localPlayer.getName()));
    }

    /**
     * Handles using a rune table structure
     *
     * @param tile The tile containing the rune table
     */
    private void useRuneTable(Tile tile) {
        // Send event to use rune table
        eventBus.publish(new UseStructureUIEvent(tile.getX(), tile.getY(), 1));
        showNotification("Using rune table at (" + tile.getX() + "," + tile.getY() + ")");
    }

    /**
     * Handles leveling up a statue
     *
     * @param tile The tile containing the statue
     */
    private void levelUpStatue(Tile tile) {
        eventBus.publish(new UpgradeStatueUIEvent(tile.getEntity().getId(), tile.getX(), tile.getY()));
        showNotification("Leveling up statue at (" + tile.getX() + "," + tile.getY() + ")");
    }

    /**
     * Handles making a deal with a statue (level 2 interaction)
     *
     * @param tile The tile containing the statue
     */
    private void makeStatueDeal(Tile tile) {
        // This will need statue-specific UI for different deal types
        showNotification("Making a deal with statue at (" + tile.getX() + "," + tile.getY() + ")");
    }

    /**
     * Handles receiving a blessing from a statue (level 3 interaction)
     *
     * @param tile The tile containing the statue
     */
    private void receiveStatueBlessing(Tile tile) {
        // This will need statue-specific UI for different blessing types
        showNotification("Receiving blessing from statue at (" + tile.getX() + "," + tile.getY() + ")");
    }

}