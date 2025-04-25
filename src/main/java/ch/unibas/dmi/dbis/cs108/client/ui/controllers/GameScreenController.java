package ch.unibas.dmi.dbis.cs108.client.ui.controllers;

import ch.unibas.dmi.dbis.cs108.client.app.GameApplication;
import ch.unibas.dmi.dbis.cs108.client.ui.SceneManager;
import ch.unibas.dmi.dbis.cs108.client.ui.components.ChatComponent;
import ch.unibas.dmi.dbis.cs108.client.ui.components.SettingsDialog;
import ch.unibas.dmi.dbis.cs108.client.ui.components.game.GridAdjustmentManager;
import ch.unibas.dmi.dbis.cs108.client.ui.events.UIEventBus;
import ch.unibas.dmi.dbis.cs108.client.ui.events.admin.ChangeNameUIEvent;
import ch.unibas.dmi.dbis.cs108.client.ui.events.admin.ConnectionStatusEvent;
import ch.unibas.dmi.dbis.cs108.client.ui.events.admin.NameChangeRequestEvent;
import ch.unibas.dmi.dbis.cs108.client.ui.events.admin.NameChangeResponseEvent;
import ch.unibas.dmi.dbis.cs108.client.ui.events.lobby.LobbyJoinedEvent;
import ch.unibas.dmi.dbis.cs108.client.ui.events.game.BuyTileUIEvent;
import ch.unibas.dmi.dbis.cs108.client.ui.events.game.PlaceStructureUIEvent;
import ch.unibas.dmi.dbis.cs108.client.ui.events.game.TileClickEvent;
import ch.unibas.dmi.dbis.cs108.client.ui.utils.ResourceLoader;
import ch.unibas.dmi.dbis.cs108.shared.game.Player;
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
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.util.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
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
    private Player localPlayer;
    private final ObservableList<String> players = FXCollections.observableArrayList();

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

        localPlayer = GameApplication.getLocalPlayer();
        currentLobbyId = GameApplication.getCurrentLobbyId();

        if (localPlayer == null) {
            LOGGER.severe("LocalPlayer is null during GameScreenController initialisation!");
            localPlayer = new Player("ErrorGuest"); // Fail‑safe stub
        }

        setupUI();
        subscribeEvents();
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
        initialiseTestPlayerColours();
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
     * In absence of real player data this method seeds a small colour table so
     * that ownership highlighting works while developing/debugging.
     */
    private void initialiseTestPlayerColours() {
        playerColors.put(localPlayer.getName(), "#0000FF".equals("#0000FF") ? Color.BLUE : Color.BLUE); // NOP for
                                                                                                        // clarity
        playerColors.put("player2_id", Color.RED);
        playerColors.put("player3_id", Color.GREEN);
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

    /*
     * --------------------------------------------------
     * Navigation & overlay actions (Main‑menu / Settings dialog)
     * --------------------------------------------------
     */

    /** Switches back to the main‑menu scene. */
    @FXML
    private void handleBackToMainMenu() {
        sceneManager.switchToScene(SceneManager.SceneType.MAIN_MENU);
    }

    /** Opens the in‑game {@link SettingsDialog}. */
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
                localPlayer.setName(event.getNewName());
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

    /** Updates the local player reference and forwards it to the chat UI. */
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
        if (chatComponentController != null)
            chatComponentController.cleanup();
        if (settingsDialog != null)
            settingsDialog.close();
        LOGGER.info("GameScreenController resources cleaned up");
    }

    /*
     * --------------------------------------------------
     * Grid‑adjustment API (delegates to GridAdjustmentManager)
     * --------------------------------------------------
     */

    /** Toggles grid‑adjustment mode. */
    public void toggleGridAdjustmentMode() {
        gridAdjustmentManager.toggleGridAdjustmentMode();
    }

    /** Enables or disables grid‑adjustment mode. */
    public void setGridAdjustmentMode(boolean active) {
        gridAdjustmentManager.setGridAdjustmentMode(active);
    }

    /** @return human‑readable description of the current grid parameters. */
    public String getGridSettings() {
        return gridAdjustmentManager.getGridSettings();
    }

    /** Handles global keyboard shortcuts. */
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

    /** Loads the background map image and triggers the initial draw. */
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
        gameCanvas.addEventHandler(MouseEvent.MOUSE_PRESSED, e -> handleCanvasClick(e.getX(), e.getY()));

        if (gameCanvas.getParent() instanceof StackPane parent) {
            parent.addEventHandler(MouseEvent.MOUSE_PRESSED, ev -> {
                Point2D local = gameCanvas.sceneToLocal(ev.getSceneX(), ev.getSceneY());
                if (local.getX() >= 0 && local.getY() >= 0 &&
                        local.getX() <= gameCanvas.getWidth() && local.getY() <= gameCanvas.getHeight()) {
                    handleCanvasClick(local.getX(), local.getY());
                    ev.consume();
                }
            });
        }

        gameCanvas.setOnKeyPressed(gridAdjustmentManager::handleGridAdjustmentKeys);
    }

    /**
     * Handles a physical mouse click on the canvas – selects the hex and triggers
     * purchase/placement logic as appropriate.
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

        // --- Purchase logic (with confirmation popup) -------------------------
        String ownerId = getTileOwnerId(row, col);

        if (ownerId == null) {
            int price = getTilePrice(row, col);

            // Show confirmation dialog
            Alert alert = new Alert(AlertType.CONFIRMATION, "Buy this tile for " + price + " gold?", ButtonType.YES, ButtonType.NO);
            alert.setHeaderText("Purchase Tile");
            alert.setTitle("Confirm Purchase");
            alert.showAndWait().ifPresent(result -> {
                if (result == ButtonType.YES) {
                    int gold = getPlayerGold();
                    if (gold >= price) {
                        // Deduct gold and assign ownership would be handled by server after BuyTileUIEvent
                        eventBus.publish(new BuyTileUIEvent(row, col));
                    } else {
                        showNotification("Not enough gold to buy this tile (Cost: " + price + ").");
                    }
                }
            });
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
        String ownerId = getTileOwnerId(row, col); // Placeholder implementation
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

    /** Point‑in‑polygon test for the current hex shape. */
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

    /** Creates the settings dialog instance and initialises bindings. */
    private void initialiseSettingsDialog() {
        settingsDialog = new SettingsDialog();
        settingsDialog.playerNameProperty().set(localPlayer.getName());
        settingsDialog.setOnSaveAction(this::handleSettingsSave);
        updateSettingsConnectionStatus();
    }

    /** Synchronises the connection indicator shown inside the dialog. */
    private void updateSettingsConnectionStatus() {
        String status = connectionStatusLabel.getText();
        settingsDialog.setConnectionStatus("Connected".equals(status), status);
    }

    /** Handles the save button inside the settings dialog. */
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

    /** Toggles card selection (golden frame) when clicked. */
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

    /** Shows the tooltip for a card after a short delay. */
    @FXML
    public void handleCardMouseEntered(MouseEvent event) {
        Node card = (Node) event.getSource();
        Tooltip tooltip = cardTooltips.computeIfAbsent(card, this::createTooltipForCard);
        Tooltip.install(card, tooltip);
        event.consume();
    }

    /** Hides the tooltip once the mouse exits the card. */
    @FXML
    public void handleCardMouseExited(MouseEvent event) {
        Node card = (Node) event.getSource();
        Tooltip tip = cardTooltips.get(card);
        if (tip != null)
            Tooltip.uninstall(card, tip);
        event.consume();
    }

    // --- Drag‑and‑drop (structures only; artifacts not implemented yet) --------

    /** Starts a drag‑and‑drop gesture when the user begins dragging a card. */
    @FXML
    private void handleCardDragDetected(MouseEvent event) {
        if (!(event.getSource() instanceof Pane src))
            return;
        String cardId = src.getId();
        if (cardId == null || cardId.isEmpty())
            return;
        if (!cardId.startsWith("structure"))
            return; // Only structures for now

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

    /** Continually called while the user drags a card across the canvas. */
    @FXML
    private void handleDragOver(DragEvent event) {
        if (event.getGestureSource() == gameCanvas)
            return;
        if (!event.getDragboard().hasString())
            return;

        int[] target = getHexAt(event.getX(), event.getY());
        String cardId = event.getDragboard().getString();
        boolean isStructure = cardId != null && cardId.startsWith("structure");

        if (target != null && isStructure && isTileOwnedByPlayer(target[0], target[1])) {
            event.acceptTransferModes(TransferMode.MOVE);
        } else {
            event.acceptTransferModes(TransferMode.NONE);
        }
        event.consume();
    }

    /** Finalises the drag‑and‑drop operation (placing the structure). */
    @FXML
    private void handleDragDropped(DragEvent event) {
        Dragboard db = event.getDragboard();
        boolean success = false;
        if (db.hasString()) {
            String cardId = db.getString();
            int[] tile = getHexAt(event.getX(), event.getY());
            if (tile != null && isTileOwnedByPlayer(tile[0], tile[1]) && canAffordCard(cardId)) {
                try {
                    int structureId = Integer.parseInt(cardId.replace("structure", ""));
                    eventBus.publish(new PlaceStructureUIEvent(tile[0], tile[1], structureId));
                    success = true;
                } catch (NumberFormatException ex) {
                    chatComponentController.addSystemMessage("Error placing card: Invalid card data.");
                }
            }
        }
        event.setDropCompleted(success);
        event.consume();
    }

    /** Cleans up after a drag‑and‑drop operation has finished. */
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

    private Tooltip createTooltipForCard(Node card) {
        Tooltip tip = new Tooltip();
        tip.setShowDelay(Duration.millis(500));
        tip.setHideDelay(Duration.millis(200));
        tip.setText(getCardDescription(card.getId()));
        tip.getStyleClass().add("card-tooltip");
        return tip;
    }

    /*
     * --------------------------------------------------
     * Placeholder logic (replace with proper game‑state look‑ups)
     * --------------------------------------------------
     */

    private String getCardDescription(String cardId) {
        if (cardId.startsWith("artifact")) {
            return "Artifact Card\n\nA powerful Norse artifact.\nEffect: Grants special abilities to the owner.";
        }
        return "Structure Card\n\nA building that can be placed on the board.\nCost: 5 Runes\nProvides: +2 Energy per turn";
    }

    // --- Placeholder methods for game state; keep until real model is wired ---

    private boolean isTileOwnedByPlayer(int row, int col) {
        return row % 2 != 0; // TEST: only odd rows are "owned"
    }

    private boolean canAffordCard(String cardId) {
        return getPlayerRunes() >= getCardCost(cardId);
    }

    private int getCardCost(String cardId) {
        return cardId.startsWith("structure") ? 5 : 10;
    }

    private int getPlayerRunes() {
        return 3;
        /* TEST: deliberately low */ }

    private String getTileOwnerId(int row, int col) {
        if (row % 2 == 0)
            return null;
        return (col % 2 == 0) ? localPlayer.getName() : "player2_id";
    }

    private int getTilePrice(int row, int col) {
        return 10;
    }

    private int getPlayerGold() {
        return 50;
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
        /* TODO implement resource overview */ }

    @FXML
    private void handleGameRound() {
        /* TODO implement end‑turn logic */ }

    @FXML
    private void handleLeaderboard() {
        /* TODO implement leaderboard */ }
}
