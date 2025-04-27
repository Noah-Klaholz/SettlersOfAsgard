package ch.unibas.dmi.dbis.cs108.client.ui.controllers;

import ch.unibas.dmi.dbis.cs108.SETTINGS;
import ch.unibas.dmi.dbis.cs108.client.app.GameApplication;
import ch.unibas.dmi.dbis.cs108.client.core.PlayerIdentityManager;
import ch.unibas.dmi.dbis.cs108.client.core.state.GameState;
import ch.unibas.dmi.dbis.cs108.client.networking.events.EndTurnEvent;
import ch.unibas.dmi.dbis.cs108.client.ui.SceneManager;
import ch.unibas.dmi.dbis.cs108.client.ui.components.ChatComponent;
import ch.unibas.dmi.dbis.cs108.client.ui.components.SettingsDialog;
import ch.unibas.dmi.dbis.cs108.client.ui.components.game.GridAdjustmentManager;
import ch.unibas.dmi.dbis.cs108.client.ui.events.UIEventBus;
import ch.unibas.dmi.dbis.cs108.client.ui.events.admin.ChangeNameUIEvent;
import ch.unibas.dmi.dbis.cs108.client.ui.events.admin.ConnectionStatusEvent;
import ch.unibas.dmi.dbis.cs108.client.ui.events.admin.NameChangeRequestEvent;
import ch.unibas.dmi.dbis.cs108.client.ui.events.admin.NameChangeResponseEvent;
import ch.unibas.dmi.dbis.cs108.client.ui.events.game.*;
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
import javafx.util.Duration;

import javax.swing.text.html.parser.Entity;
import java.util.*;
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

        playerManager = PlayerIdentityManager.getInstance();
        localPlayer = playerManager.getLocalPlayer();
        playerManager.addPlayerUpdateListener(this::handlePlayerUpdate);

        Logger.getGlobal().info("Game state uses Local Player: " + localPlayer.getName());
        gameState = new GameState();
        subscribeEvents();
        Logger.getGlobal().info("GameScreenController created and subscribed to events.");
    }

    /**
     * Invoked by the FXMLLoader after all @FXML fields have been injected.
     * <p>
     * This method wires UI bindings, subscribes to the event bus, initialises
     * helper classes and loads the map image.
     */
    @FXML
    private void initialize() {
        LOGGER.setLevel(Level.ALL);
        LOGGER.info("GameScreenController initialisation started");

        if (localPlayer == null) {
            LOGGER.severe("LocalPlayer is null during GameScreenController initialisation!");
            localPlayer = new Player("ErrorGuest"); // Fail‑safe stub
        }

        currentLobbyId = GameApplication.getCurrentLobbyId();

        setupUI();
        loadMapImage();
        createAdjustmentUI();

        gridAdjustmentManager = new GridAdjustmentManager(
                this,
                adjustmentModeIndicator,
                adjustmentValuesLabel,
                this::drawMapAndGrid);

        setupCanvasListeners();
        initialiseSettingsDialog();
        initialiseChatComponent();
        initialisePlayerColours();
        updateCardImages();

        Logger.getGlobal().info("GameScreenController initialized");
    }

    /*
     * --------------------------------------------------
     * UI helper initialisation
     * --------------------------------------------------
     */

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
        // In GameScreenController.subscribeEvents():
        eventBus.subscribe(GameSyncEvent.class, event -> {
            LOGGER.info("GameSyncEvent received: " + (event == null ? "null" : event.toString()));
            if (event != null) {
                handleGameSync(event);
            } else {
                LOGGER.severe("Received null GameSyncEvent");
            }
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
                playerColours.add(Color.GREEN); // Local Player should always be green
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
        if (chatComponentController != null) {
            chatComponentController.setPlayer(localPlayer);
        }
        LOGGER.info("Player updated in GameScreenController: " + localPlayer.getName());
    }

    /**
     * Handles the game sync event and updates the game state accordingly.
     */
    private void handleGameSync(GameSyncEvent e) {
        LOGGER.fine(() -> "GameSyncEvent received: " + e);
        if (e == null || e.getGameState() == null) {
            LOGGER.warning("Received null GameSyncEvent or GameState");
            return;
        }
        // Update the game State with the new game state
        gameState = e.getGameState();
        LOGGER.info("GameSyncEvent received. Searching for player " + localPlayer.getName());
        gamePlayer = gameState.findPlayerByName(localPlayer.getName());

        if (gamePlayer == null) {
            LOGGER.warning("Game player not found in game state.");
            return;
        } else {
            LOGGER.fine(() -> "Game player found: " + gamePlayer.getName() + " Runes: " + gamePlayer.getRunes());
        }

        // Update the artifacts list
        artifacts = gamePlayer.getArtifacts();

        // Update card images after game state changes
        Platform.runLater(() -> {
            updateCardImages();
            refreshCardAffordability();
            updateRunesAndEnergyBar();
            updatePlayerList();
            updateMap();
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
                localPlayer.setName(event.getNewName());
                GameApplication.setLocalPlayer(localPlayer);
                chatComponentController.setPlayer(localPlayer);
                chatComponentController.addSystemMessage("Name successfully changed to: " + localPlayer.getName());
                settingsDialog.playerNameProperty().set(localPlayer.getName());
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
            gameCanvas.getParent().removeEventHandler(MouseEvent.MOUSE_PRESSED, e -> handleCanvasClick(e.getX(), e.getY()));
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
     * Handles a double-click on the canvas – attempts to purchase the tile immediately
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
                eventBus.publish(new BuyTileUIEvent(row, col));
            } else {
                showNotification("Not enough gold to buy this tile (Cost: " + price + ").");
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
                String URL = EntityRegistry.getURL(entity.getId(), false);
                drawEntityImage(gc, URL, cx, cy, size, hSquish);
            }
        } else {
            LOGGER.warning("Tile is null for row " + row + ", col " + col);
        }
    }

    /**
     * Draws an entity image centered in a hex tile.
     * The image is scaled to fit the hex width while preserving its aspect ratio.
     *
     * @param gc The graphics context to draw on
     * @param imageUrl The URL of the image to draw
     * @param centerX The x-coordinate of the hex center
     * @param centerY The y-coordinate of the hex center
     * @param hexSize The size of the hex
     * @param hSquish The horizontal squish factor
     */
    private void drawEntityImage(GraphicsContext gc, String imageUrl, double centerX, double centerY,
                                 double hexSize, double hSquish) {
        if (imageUrl == null || imageUrl.isEmpty()) {
            return; // No image to draw
        }

        try {
            Image image = resourceLoader.loadImage(imageUrl);
            if (image == null || image.isError()) {
                LOGGER.fine("Failed to load entity image: " + imageUrl);
                return;
            }

            // Calculate maximum width based on hex size and squish factor
            // The width of a hex is approximately 2 * size
            double maxWidth = 1.7 * hexSize * hSquish;

            // Calculate scale to fit width
            double scale = maxWidth / image.getWidth();

            // Calculate scaled dimensions
            double scaledWidth = image.getWidth() * scale;
            double scaledHeight = image.getHeight() * scale;

            // Save current graphics state
            double oldAlpha = gc.getGlobalAlpha();
            gc.setGlobalAlpha(1.0); // Full opacity for the image

            // Draw image centered in the hex
            gc.drawImage(image,
                    centerX - scaledWidth/2,
                    centerY - scaledHeight/2,
                    scaledWidth,
                    scaledHeight);

            // Restore graphics state
            gc.setGlobalAlpha(oldAlpha);
        } catch (Exception e) {
            LOGGER.warning("Error drawing entity image: " + e.getMessage());
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

    // --- Drag‑and‑drop (structures only; artifacts not implemented yet) --------

    /**
     * Starts a drag‑and‑drop gesture when the user begins dragging a card.
     */
    @FXML
    private void handleCardDragDetected(MouseEvent event) {
        if (!(event.getSource() instanceof Pane src))
            return;
        String cardId = src.getId();
        if (cardId == null || cardId.isEmpty())
            return;
        if (!cardId.startsWith("structure"))
            return; // Only structures for now

        // Don't start drag operation if player can't afford the card
        if (!canAffordCard(cardId)) {
            event.consume();
            return;
        }

        draggedCardSource = src;
        Dragboard db = src.startDragAndDrop(TransferMode.MOVE);
        ClipboardContent content = new ClipboardContent();
        content.putString(cardId);
        db.setContent(content);

        SnapshotParameters params = new SnapshotParameters();
        params.setFill(Color.TRANSPARENT);
        WritableImage snapshot = src.snapshot(params, null);
        db.setDragView(snapshot, event.getX(), event.getY());

        event.consume();
    }

    /**
     * Continually called while the user drags a card across the canvas.
     * It checks if the target tile is valid for placing a structure.
     *
     * @param event The drag event containing the current mouse position
     */
    @FXML
    private void handleDragOver(DragEvent event) {
        if (event.getGestureSource() == gameCanvas)
            return;
        if (!event.getDragboard().hasString())
            return;

        int[] target = getHexAt(event.getX(), event.getY());
        String cardId = event.getDragboard().getString();
        boolean isStructure = cardId != null && cardId.startsWith("structure");

        boolean validTarget = false;
        if (target != null && isStructure) {
            // Check if player owns the tile and the tile doesn't already have an entity
            Tile tile = getTile(target[0], target[1]);
            boolean tileOwnedByPlayer = (tile != null &&
                    tile.getOwner() != null &&
                    gamePlayer != null &&
                    tile.getOwner().equals(gamePlayer.getName()));
            boolean tileEmpty = tile != null &&
                    (tile.getEntity() == null ||
                            tile.getEntity().isArtifact());

            validTarget = tileOwnedByPlayer && tileEmpty;
        }

        // Update the highlighted tile
        if (validTarget) {
            highlightedTile = target;
            drawMapAndGrid(); // Redraw to show highlight
            event.acceptTransferModes(TransferMode.MOVE);
        } else {
            // Clear highlight if not a valid target
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
        // Clear the highlighted tile when drag exits canvas
        if (highlightedTile != null) {
            highlightedTile = null;
            drawMapAndGrid();
        }
        event.consume();
    }

    /**
     * Finalises the drag‑and‑drop operation (placing the structure).
     *
     * @param event The drag event containing the current mouse position
     */
    @FXML
    private void handleDragDropped(DragEvent event) {
        Dragboard db = event.getDragboard();
        boolean success = false;
        if (db.hasString()) {
            String cardId = db.getString();
            int[] tile = getHexAt(event.getX(), event.getY());

            // Check if the target is valid
            if (tile != null && canAffordCard(cardId)) {
                Tile gameTile = getTile(tile[0], tile[1]);
                boolean tileOwnedByPlayer = (gameTile != null &&
                        gameTile.getOwner() != null &&
                        gamePlayer != null &&
                        gameTile.getOwner().equals(gamePlayer.getName()));
                boolean tileEmpty = gameTile != null &&
                        (gameTile.getEntity() == null ||
                                gameTile.getEntity().isArtifact());

                if (tileOwnedByPlayer && tileEmpty) {
                    try {
                        // Get the correct entity ID using the existing method
                        int structureId = getEntityID(cardId);
                        LOGGER.info("Placing structure " + structureId + " at tile " + tile[0] + "," + tile[1]);
                        eventBus.publish(new PlaceStructureUIEvent(tile[0], tile[1], structureId));
                        success = true;
                    } catch (NumberFormatException ex) {
                        chatComponentController.addSystemMessage("Error placing card: Invalid card data.");
                        LOGGER.warning("Error parsing structure ID: " + ex.getMessage());
                    }
                }
            }
        }

        // Clear highlight
        highlightedTile = null;
        drawMapAndGrid();

        event.setDropCompleted(success);
        event.consume();
    }

    /**
     * Cleans up after a drag‑and‑drop operation has finished.
     *
     * @param event The drag event containing the current mouse position
     */
    @FXML
    private void handleCardDragDone(DragEvent event) {
        if (draggedCardSource != null && event.getTransferMode() == TransferMode.MOVE) {
            Pane parent = (Pane) draggedCardSource.getParent();
            if (parent != null)
                parent.getChildren().remove(draggedCardSource);
        }
        draggedCardSource = null;
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
        CardDetails details = getCardDetails(id);

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
            if (details.getDescription() != null && !details.getDescription().isEmpty() ||
                    details.getLore() != null && !details.getLore().isEmpty() ||
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
            if (details.getLore() != null && !details.getLore().isEmpty() ||
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
     *
     * @param id The ID of the card.
     * @return The CardDetails object containing the card's details.
     */
    public CardDetails getCardDetails(String id) {
        int entityID = getEntityID(id);
        GameEntity entity = EntityRegistry.getGameEntityOriginalById(entityID); // DO NOT Make changes to this entity, READ-ONLY
        String URL = EntityRegistry.getURL(entityID, true);
        String title = entity.getName();
        String description = entity.getUsage();
        String lore = entity.getDescription();
        int actualPrice = 0;
        int price = entity.getPrice();
        if (price != 0) {
            double priceModifier = gamePlayer.getStatus().get(Status.BuffType.SHOP_PRICE);
            double adjusted = price / Math.max(priceModifier, 0.5); // Prevent divide-by-zero or negative scaling, set maximum shop price to 200%
            actualPrice = Math.max(0, (int) Math.round(adjusted)); // Ensure price is never negative
        }
        return new CardDetails(title, description, lore, URL, actualPrice);
    }

    /**
     * Maps card IDs to their corresponding entity IDs.
     *
     * @param id The ID of the card.
     * @return The corresponding entity ID.
     */
    private int getEntityID(String id) {
        if (id.startsWith("artifact")) {
            int i = Integer.parseInt(id.replace("artifact", ""));
            if (i < 0 || i >= artifacts.size() || artifacts.isEmpty()) {
                Logger.getGlobal().fine("Invalid artifact ID or artifacts are null: " + id); // This is expected, since the player might not have all artifact slots filled
                return 22; // ID of the artifact which holds the description for the card (should the slot be empty)
            } else {
                return artifacts.get(i).getId();
            }
        } else if (id.startsWith("structure")) {
            return Integer.parseInt(id.replace("structure", "")); // Structure IDs are numbered 1-9 (but 7-9 cannot be bought)
        } else if (id.startsWith("statue")) {
            return 38; // ID of the statue which holds the description for the card
        } else {
            throw new IllegalArgumentException("Invalid card ID: " + id);
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
            if (card.getId() != null && (card.getId().startsWith("structure") || card.getId().startsWith("statue"))) {
                updateCardImage(card);
                updateCardAffordability(card);
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
        if (id == null) return;

        boolean canAfford = canAffordCard(id);

        // Apply or remove the CSS class for unaffordable cards
        if (canAfford) {
            card.getStyleClass().remove("unaffordable-card");
            card.getStyleClass().add("game-card");
            // Make sure it's draggable for structures
            if (id.startsWith("structure")) {
                card.addEventHandler(MouseEvent.DRAG_DETECTED, this::handleCardDragDetected);
            }
        } else {
            card.getStyleClass().remove("game-card");
            card.getStyleClass().add("unaffordable-card");
            // Remove drag handler for unaffordable cards
            if (id.startsWith("structure")) {
                card.removeEventHandler(MouseEvent.DRAG_DETECTED, this::handleCardDragDetected);
            }
        }
    }

    /**
     * Updates a single card with the correct image.
     *
     * @param card The card node to update.
     */
    private void updateCardImage(Node card) {
        String id = card.getId();
        if (id == null) return;

        try {
            CardDetails details = getCardDetails(id);
            String imageUrl = details.getImageUrl();

            if (card instanceof Pane pane) {
                // Clear existing content first
                pane.getChildren().clear();

                // Set fixed dimensions
                pane.setMinSize(80, 120);
                pane.setPrefSize(80, 120);
                pane.setMaxSize(80, 120);

                // Add a border
                pane.setStyle("-fx-border-color: #444444; -fx-border-width: 1px; -fx-border-radius: 5px;");

                if (imageUrl != null && !imageUrl.isEmpty()) {
                    Image image = resourceLoader.loadImage(imageUrl);

                    if (image != null && !image.isError()) {

                        // Create an ImageView with proper sizing
                        ImageView imageView = new ImageView(image);
                        imageView.setPreserveRatio(true);
                        imageView.setFitWidth(78);
                        imageView.setFitHeight(118);

                        // Center image in pane
                        StackPane wrapper = new StackPane(imageView);
                        wrapper.setPrefSize(78, 118);

                        pane.getChildren().add(wrapper);
                    } else {
                        LOGGER.warning("Failed to load image for card " + id);
                        addPlaceholder(pane, details.getTitle());
                    }
                } else {
                    LOGGER.warning("No image URL for card " + id);
                    addPlaceholder(pane, details.getTitle());
                }

                // Ensure card is visible
                pane.setVisible(true);
                pane.setOpacity(1.0);
            }
        } catch (Exception e) {
            LOGGER.warning("Failed to update card " + id + ": " + e.getMessage());
        }
    }

    private void addPlaceholder(Pane pane, String title) {
        Label placeholder = new Label(title != null ? title : "Card");
        placeholder.setWrapText(true);
        placeholder.setAlignment(Pos.CENTER);
        placeholder.setPrefSize(78, 118);
        placeholder.setStyle("-fx-background-color: #333344; -fx-text-fill: white; -fx-alignment: center;");

        pane.getChildren().add(placeholder);
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
        LOGGER.info("Updating player list");

        String currentPlayerName = gameState.getPlayerTurn();
        Logger.getGlobal().info("Current player turn: " + currentPlayerName);

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
    }

    // Helper methods for better gameState access

    private Tile getTile(int row, int col) {
        return gameState.getBoardManager().getTile(col, row);
    }

    private boolean isTileOwnedByPlayer(int row, int col) {
        return gameState.getBoardManager().getTile(col, row).hasEntity();
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
        Tile tile = gameState.getBoardManager().getTile(col, row);
        return tile == null ? null : tile.getOwner();
    }

    private int getTilePrice(int row, int col) {
        Tile tile = gameState.getBoardManager().getTile(col, row);
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
}
