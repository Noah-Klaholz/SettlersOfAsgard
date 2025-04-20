package ch.unibas.dmi.dbis.cs108.client.ui.controllers;

import ch.unibas.dmi.dbis.cs108.client.ui.SceneManager;
import ch.unibas.dmi.dbis.cs108.client.ui.components.SettingsDialog;
import ch.unibas.dmi.dbis.cs108.client.ui.events.UIEventBus;
import ch.unibas.dmi.dbis.cs108.client.ui.events.admin.ConnectionStatusEvent;
import ch.unibas.dmi.dbis.cs108.client.ui.events.chat.GlobalChatEvent;
import ch.unibas.dmi.dbis.cs108.client.ui.events.chat.LobbyChatEvent;
import ch.unibas.dmi.dbis.cs108.client.ui.events.game.TileClickEvent;
import ch.unibas.dmi.dbis.cs108.client.ui.utils.ResourceLoader;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.util.Duration;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * JavaFX controller for the in‑game screen. This class is responsible for loading and drawing the map, computing and
 * rendering the hexagonal grid overlay, dispatching user interactions (tile clicks, chat input, etc.) through the
 * {@link UIEventBus}, and managing a small settings overlay. <p>
 * <p>
 * A *grid‑adjustment mode* can be toggled with <kbd>Ctrl</kbd>+<kbd>G</kbd> to fine‑tune the grid so that it matches
 * new background artwork without having to rebuild the client. The default values reflect the most current customer
 * measurements and should only be changed when the graphics are updated.
 */
public class GameScreenController extends BaseController {

    /* ── Constants ─────────────────────────────────────────────────────────────────── */

    private static final Logger LOGGER = Logger.getLogger(GameScreenController.class.getName());
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");

    private static final int HEX_ROWS = 7;
    private static final int HEX_COLS = 8;

    // Custom defaults supplied by the design team (do NOT change unless the map artwork changes!)
    private static final double DEF_GRID_SCALE = 0.85;
    private static final double DEF_GRID_H_OFFSET = 0.00;
    private static final double DEF_GRID_V_OFFSET = 0.15;
    private static final double DEF_GRID_WIDTH_PCT = 0.90;   // 90 % of map width
    private static final double DEF_GRID_HEIGHT_PCT = 2.06;  // 206 % of map height (artistic perspective)

    private static final double DEF_ROTATION_DEG = 30.0;
    private static final double DEF_H_SPACING = 1.80;
    private static final double DEF_V_SPACING = 1.33;
    private static final double DEF_H_SQUISH = 1.00;
    private static final double DEF_V_SQUISH = 0.80;

    /* ── UI state ───────────────────────────────────────────────────────────────────── */

    private final ObservableList<String> chatMessages = FXCollections.observableArrayList();
    private final ObservableList<String> players = FXCollections.observableArrayList();

    private double scaledMapWidth;
    private double scaledMapHeight;
    private double mapOffsetX;
    private double mapOffsetY;
    private double effectiveHexSize;
    private double gridOffsetX;
    private double gridOffsetY;

    /* Grid parameters (runtime‑adjustable) */
    private double gridScaleFactor = DEF_GRID_SCALE;
    private double gridHorizontalOffset = DEF_GRID_H_OFFSET;
    private double gridVerticalOffset = DEF_GRID_V_OFFSET;
    private double gridWidthPercentage = DEF_GRID_WIDTH_PCT;
    private double gridHeightPercentage = DEF_GRID_HEIGHT_PCT;

    private double hexRotationDegrees = DEF_ROTATION_DEG;
    private double horizontalSpacingFactor = DEF_H_SPACING;
    private double verticalSpacingFactor = DEF_V_SPACING;
    private double horizontalSquishFactor = DEF_H_SQUISH;
    private double verticalSquishFactor = DEF_V_SQUISH;

    private boolean gridAdjustmentModeActive;
    private Label adjustmentModeIndicator;
    private Label adjustmentValuesLabel;

    private int selectedRow = -1;
    private int selectedCol = -1;
    private String currentLobbyId;

    private Image mapImage;
    private boolean isMapLoaded;

    /* Settings overlay */
    private SettingsDialog settingsDialog;

    /* ── FXML injected nodes ────────────────────────────────────────────────────────── */

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

    /* ── Construction ──────────────────────────────────────────────────────────────── */

    /**
     * Creates a new controller instance and wires the shared singletons. All heavy initialisation is deferred to
     * {@link #initialize()} which is invoked by the JavaFX runtime once the FXML graph has been constructed.
     */
    public GameScreenController() {
        super(new ResourceLoader(), UIEventBus.getInstance(), SceneManager.getInstance());
    }

    /* ── Lifecycle / initialisation ─────────────────────────────────────────────────── */

    /**
     * Convenience helper that keeps a {@link ListView} scrolled to its last element.
     */
    private static void scrollBottom(ListView<String> list, ObservableList<String> data) {
        Platform.runLater(() -> {
            if (!data.isEmpty()) {
                list.scrollTo(data.size() - 1);
            }
        });
    }

    /**
     * Called automatically by the JavaFX loader. Sets up UI bindings, subscribes to the event‑bus, loads resources and
     * prepares event handlers.
     */
    @FXML
    private void initialize() {
        LOGGER.setLevel(Level.ALL);
        setupUI();
        subscribeEvents();
        loadMapImage();
        setupCanvasListeners();
        createAdjustmentUI();
        initialiseSettingsDialog();
    }

    /**
     * Wires UI controls to their backing properties and assigns sensible defaults.
     */
    private void setupUI() {
        chatListView.setItems(chatMessages);
        playersList.setItems(players);
        globalChatButton.setSelected(true);
        energyBar.setProgress(0.5);
        runesLabel.setText("0");
        connectionStatusLabel.setText("Connected");
        // Keep canvas the same size as its parent StackPane
        gameCanvas.widthProperty().bind(((Region) gameCanvas.getParent()).widthProperty());
        gameCanvas.heightProperty().bind(((Region) gameCanvas.getParent()).heightProperty());
    }

    /**
     * Installs all event‑bus subscriptions and UI callbacks.
     */
    private void subscribeEvents() {
        eventBus.subscribe(GlobalChatEvent.class, this::onGlobalChat);
        eventBus.subscribe(LobbyChatEvent.class, this::onLobbyChat);
        eventBus.subscribe(ConnectionStatusEvent.class, this::onConnectionStatus);
        eventBus.subscribe(TileClickEvent.class, this::onTileClick);
        chatInputField.setOnAction(e -> handleMessageSend());
    }

    /* ── Event‑bus callbacks ────────────────────────────────────────────────────────── */

    private void onGlobalChat(GlobalChatEvent e) {
        if (e == null || e.getContent() == null || e.getSender() == null) {
            return;
        }
        Platform.runLater(() -> addChat(String.format("%s: %s", e.getSender(), e.getContent())));
    }

    private void onLobbyChat(LobbyChatEvent e) {
        if (e == null || e.getMessage() == null || e.getSender() == null) {
            return;
        }
        Platform.runLater(() -> addChat(String.format("[Lobby] %s: %s", e.getSender(), e.getMessage())));
    }

    private void onConnectionStatus(ConnectionStatusEvent e) {
        if (e == null) {
            return;
        }
        Platform.runLater(() -> {
            connectionStatusLabel.setText(Optional.ofNullable(e.getStatus()).map(Object::toString).orElse("UNKNOWN"));
            if (e.getMessage() != null && !e.getMessage().isEmpty()) {
                addSystem(e.getMessage());
            }
            if (settingsDialog != null) { // propagate status to the overlay as well
                updateSettingsConnectionStatus();
            }
        });
    }

    /* ── Chat helpers ───────────────────────────────────────────────────────────────── */

    private void onTileClick(TileClickEvent e) {
        LOGGER.fine(() -> String.format("Tile clicked externally (row=%d,col=%d)", e.getRow(), e.getCol()));
    }

    private void addChat(String msg) {
        chatMessages.add(String.format("[%s] %s", LocalDateTime.now().format(TIME_FMT), msg));
        scrollBottom(chatListView, chatMessages);
    }

    private void addSystem(String msg) {
        chatMessages.add(String.format("[%s] System: %s", LocalDateTime.now().format(TIME_FMT), msg));
        scrollBottom(chatListView, chatMessages);
    }

    /* ── FXML‑defined actions ───────────────────────────────────────────────────────── */

    /**
     * Navigates back to the main‑menu scene.
     */
    @FXML
    private void handleBackToMainMenu() {
        sceneManager.switchToScene(SceneManager.SceneType.MAIN_MENU);
    }

    /**
     * Opens the settings dialog overlay.
     */
    @FXML
    private void handleSettings() {
        if (settingsDialog != null) {
            settingsDialog.show();
        } else {
            LOGGER.warning("Settings dialog is not initialised");
        }
    }

    @FXML
    private void handleResourceOverview() { /* not yet implemented */ }

    @FXML
    private void handleGameRound() { /* not yet implemented */ }

    @FXML
    private void handleLeaderboard() { /* not yet implemented */ }

    @FXML
    private void handleGlobalChatSelect() {
        globalChatButton.setSelected(true);
        lobbyChatButton.setSelected(false);
    }

    @FXML
    private void handleLobbyChatSelect() {
        globalChatButton.setSelected(false);
        lobbyChatButton.setSelected(true);
        if (currentLobbyId == null || currentLobbyId.isEmpty()) {
            addSystem("You are not in a lobby.");
        }
    }

    /**
     * Dispatches the text currently in the chat input field to the appropriate chat channel.
     */
    @FXML
    private void handleMessageSend() {
        String msg = chatInputField.getText().trim();
        if (msg.isEmpty()) {
            return;
        }

        if (globalChatButton.isSelected()) {
            eventBus.publish(new GlobalChatEvent(msg, GlobalChatEvent.ChatType.GLOBAL));
        } else if (lobbyChatButton.isSelected()) {
            if (currentLobbyId == null || currentLobbyId.isEmpty()) {
                addSystem("Cannot send lobby message: You're not in a lobby.");
            } else {
                eventBus.publish(new LobbyChatEvent(currentLobbyId, msg));
            }
        }
        chatInputField.clear();
    }

    /* ── Public API ─────────────────────────────────────────────────────────────────── */

    /**
     * Sets the lobby‑identifier used when sending lobby chat messages.
     *
     * @param lobbyId unique identifier of the lobby the user is currently in (may be {@code null}).
     */
    public void setCurrentLobbyId(String lobbyId) {
        this.currentLobbyId = lobbyId;
    }

    /**
     * Unsubscribes from all event‑bus channels. <b>Must</b> be called when the controller is disposed to avoid memory
     * leaks.
     */
    public void cleanup() {
        eventBus.unsubscribe(GlobalChatEvent.class, this::onGlobalChat);
        eventBus.unsubscribe(LobbyChatEvent.class, this::onLobbyChat);
        eventBus.unsubscribe(ConnectionStatusEvent.class, this::onConnectionStatus);
    }

    /* ── Grid‑adjustment mode ───────────────────────────────────────────────────────── */

    /**
     * Toggles grid‑adjustment mode on/off.
     */
    public void toggleGridAdjustmentMode() {
        setGridAdjustmentMode(!gridAdjustmentModeActive);
    }

    /**
     * Enables or disables grid‑adjustment mode.
     *
     * @param active {@code true} to enter adjustment mode, {@code false} to leave it.
     */
    public void setGridAdjustmentMode(boolean active) {
        gridAdjustmentModeActive = active;
        adjustmentModeIndicator.setVisible(active);
        adjustmentValuesLabel.setVisible(active);
        if (active) {
            gameCanvas.requestFocus();
            updateAdjustmentFeedback();
        }
        drawMapAndGrid();
    }

    /* Convenience setters that validate input and trigger redraws */

    public void setGridScaleFactor(double f) {
        if (f > 0.3 && f < 1.5) {
            gridScaleFactor = f;
            drawMapAndGrid();
        }
    }

    public void setGridHorizontalOffset(double p) {
        if (p >= -0.3 && p <= 0.3) {
            gridHorizontalOffset = p;
            drawMapAndGrid();
        }
    }

    public void setGridVerticalOffset(double p) {
        if (p >= -0.3 && p <= 0.3) {
            gridVerticalOffset = p;
            drawMapAndGrid();
        }
    }

    public void setHexRotation(double d) {
        hexRotationDegrees = Math.max(0, Math.min(60, d));
        drawMapAndGrid();
    }

    public void setHorizontalSpacing(double f) {
        if (f >= 1) {
            horizontalSpacingFactor = f;
            drawMapAndGrid();
        }
    }

    public void setVerticalSpacing(double f) {
        if (f >= 1) {
            verticalSpacingFactor = f;
            drawMapAndGrid();
        }
    }

    public void setHorizontalSquish(double f) {
        if (f >= 0.5 && f <= 2) {
            horizontalSquishFactor = f;
            drawMapAndGrid();
        }
    }

    public void setVerticalSquish(double f) {
        if (f >= 0.5 && f <= 2) {
            verticalSquishFactor = f;
            drawMapAndGrid();
        }
    }

    /**
     * Returns a formatted string with all grid parameters. Mainly intended for diagnostics/debugging.
     */
    public String getGridSettings() {
        return String.format(
                "Grid settings: scale=%.2f, hOffset=%.2f, vOffset=%.2f, width=%.2f%%, height=%.2f%%, rotation=%.1f°, " +
                        "hSpacing=%.2f, vSpacing=%.2f, hSquish=%.2f, vSquish=%.2f",
                gridScaleFactor, gridHorizontalOffset, gridVerticalOffset, gridWidthPercentage * 100,
                gridHeightPercentage * 100, hexRotationDegrees, horizontalSpacingFactor, verticalSpacingFactor,
                horizontalSquishFactor, verticalSquishFactor);
    }

    /* ── Keyboard handling (global shortcuts) ───────────────────────────────────────── */

    /**
     * Handles shortcuts registered on the parent FXML root.
     */
    @FXML
    private void handleKeyboardShortcut(KeyEvent e) {
        if (e.getCode() == KeyCode.ESCAPE && gridAdjustmentModeActive) {
            setGridAdjustmentMode(false);
            e.consume();
        }
        if (e.isControlDown() && e.getCode() == KeyCode.G) {
            toggleGridAdjustmentMode();
            e.consume();
        }
    }

    /* ── Internal helpers ───────────────────────────────────────────────────────────── */

    /**
     * Adds the translucent info panel used while in adjustment mode.
     */
    private void createAdjustmentUI() {
        adjustmentModeIndicator = new Label("GRID ADJUSTMENT MODE (Press G to exit)");
        adjustmentModeIndicator.setStyle("-fx-background-color: rgba(255,165,0,0.7); -fx-text-fill:white; " +
                "-fx-padding:5 10; -fx-background-radius:5; -fx-font-weight:bold;");
        adjustmentModeIndicator.setVisible(false);

        adjustmentValuesLabel = new Label();
        adjustmentValuesLabel.setStyle("-fx-background-color: rgba(0,0,0,0.7); -fx-text-fill:white; " +
                "-fx-padding:5; -fx-font-size:11; -fx-background-radius:3;");
        adjustmentValuesLabel.setVisible(false);

        VBox panel = new VBox(5, adjustmentModeIndicator, adjustmentValuesLabel);
        panel.setAlignment(Pos.TOP_CENTER);
        panel.setPadding(new Insets(10));
        ((StackPane) gameCanvas.getParent()).getChildren().add(panel);
        StackPane.setAlignment(panel, Pos.TOP_CENTER);
    }

    /**
     * Loads the map image from resources and triggers the initial draw.
     */
    private void loadMapImage() {
        mapImage = resourceLoader.loadImage(ResourceLoader.MAP_IMAGE);
        isMapLoaded = mapImage != null;
        if (isMapLoaded) {
            drawMapAndGrid();
        } else {
            LOGGER.severe("Map image missing");
        }
    }

    /**
     * Installs listeners so that the grid is redrawn when the canvas size changes and so that mouse/keyboard events are
     * captured even when transparent overlay nodes intercept them.
     */
    private void setupCanvasListeners() {
        // Redraw on resize
        gameCanvas.widthProperty().addListener((o, ov, nv) -> drawMapAndGrid());
        gameCanvas.heightProperty().addListener((o, ov, nv) -> drawMapAndGrid());

        // Canvas must be focusable for key controls
        gameCanvas.setFocusTraversable(true);

        // Mouse events directly on the canvas
        gameCanvas.addEventHandler(MouseEvent.MOUSE_PRESSED, e -> handleCanvasClick(e.getX(), e.getY()));

        // Mouse events falling through transparent overlay nodes
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

        // Key controls active while the canvas has focus
        gameCanvas.setOnKeyPressed(this::handleGridAdjustmentKeys);
    }

    /**
     * Handles a physical mouse click on the canvas.
     */
    private void handleCanvasClick(double px, double py) {
        int[] tile = getHexAt(px, py);
        if (tile != null) {
            selectedRow = tile[0];
            selectedCol = tile[1];
            drawMapAndGrid();
            LOGGER.finer(() -> String.format("Clicked hex at row=%d, col=%d", selectedRow, selectedCol));
            eventBus.publish(new TileClickEvent(selectedRow, selectedCol));
        }
    }

    /**
     * Processes key presses while in grid‑adjustment mode.
     */
    @FXML
    private void handleGridAdjustmentKeys(KeyEvent e) {
        if (!gridAdjustmentModeActive && !(e.isControlDown() && e.getCode() == KeyCode.G)) {
            return;
        }

        switch (e.getCode()) {
            case UP -> gridVerticalOffset -= 0.01;
            case DOWN -> gridVerticalOffset += 0.01;
            case LEFT -> gridHorizontalOffset -= 0.01;
            case RIGHT -> gridHorizontalOffset += 0.01;
            case PLUS, ADD -> gridScaleFactor += 0.05;
            case MINUS, SUBTRACT -> gridScaleFactor -= 0.05;
            case W -> gridHeightPercentage -= 0.02;
            case S -> gridHeightPercentage += 0.02;
            case A -> gridWidthPercentage -= 0.02;
            case D -> gridWidthPercentage += 0.02;
            case R -> hexRotationDegrees = (hexRotationDegrees + 10) % 60;
            case T -> hexRotationDegrees = (hexRotationDegrees - 10 + 60) % 60;
            case F -> horizontalSpacingFactor = Math.max(1, horizontalSpacingFactor - 0.1);
            case G -> horizontalSpacingFactor += 0.1;
            case V -> verticalSpacingFactor = Math.max(1, verticalSpacingFactor - 0.1);
            case B -> verticalSpacingFactor += 0.1;
            case H -> horizontalSquishFactor = Math.max(0.5, horizontalSquishFactor - 0.1);
            case J -> horizontalSquishFactor = Math.min(2, horizontalSquishFactor + 0.1);
            case K -> verticalSquishFactor = Math.max(0.5, verticalSquishFactor - 0.1);
            case L -> verticalSquishFactor = Math.min(2, verticalSquishFactor + 0.1);
            case BACK_SPACE -> resetDefaults();
            case P -> LOGGER.info(getGridSettings());
            case C -> copyGridSettingsToClipboard();
            default -> {
                return; // nothing changed
            }
        }
        drawMapAndGrid();
        updateAdjustmentFeedback();
        e.consume();
    }

    /**
     * Restores all grid parameters to their factory defaults.
     */
    private void resetDefaults() {
        gridScaleFactor = DEF_GRID_SCALE;
        gridHorizontalOffset = DEF_GRID_H_OFFSET;
        gridVerticalOffset = DEF_GRID_V_OFFSET;
        gridWidthPercentage = DEF_GRID_WIDTH_PCT;
        gridHeightPercentage = DEF_GRID_HEIGHT_PCT;
        hexRotationDegrees = DEF_ROTATION_DEG;
        horizontalSpacingFactor = DEF_H_SPACING;
        verticalSpacingFactor = DEF_V_SPACING;
        horizontalSquishFactor = DEF_H_SQUISH;
        verticalSquishFactor = DEF_V_SQUISH;
    }

    /**
     * Presents the current parameter values while adjusting.
     */
    private void updateAdjustmentFeedback() {
        adjustmentValuesLabel.setText(String.format(
                "Scale: %.2f | Offset:(%.2f,%.2f) | Size: %.0f%%x%.0f%% | Rot: %.1f° | " +
                        "Spacing:(%.2f,%.2f) | Squish:(%.2f,%.2f)",
                gridScaleFactor, gridHorizontalOffset, gridVerticalOffset,
                gridWidthPercentage * 100, gridHeightPercentage * 100, hexRotationDegrees,
                horizontalSpacingFactor, verticalSpacingFactor, horizontalSquishFactor, verticalSquishFactor));
    }

    /**
     * Copies the grid parameter string to the system clipboard and flashes a green background as feedback.
     */
    private void copyGridSettingsToClipboard() {
        ClipboardContent content = new ClipboardContent();
        content.putString(getGridSettings());
        Clipboard.getSystemClipboard().setContent(content);

        adjustmentValuesLabel.setStyle(adjustmentValuesLabel.getStyle() + "-fx-background-color:rgba(0,128,0,0.8);");
        PauseTransition flash = new PauseTransition(Duration.seconds(0.5));
        flash.setOnFinished(ev -> adjustmentValuesLabel.setStyle("-fx-background-color: rgba(0,0,0,0.7);"));
        flash.play();
    }

    /* ── Drawing helpers ─────────────────────────────────────────────────────────────── */

    /**
     * Redraws the background image and the hex overlay using the current parameters.
     */
    private void drawMapAndGrid() {
        if (!isMapLoaded) {
            return;
        }
        double cW = gameCanvas.getWidth();
        double cH = gameCanvas.getHeight();
        if (cW <= 0 || cH <= 0) {
            return;
        }

        GraphicsContext gc = gameCanvas.getGraphicsContext2D();
        gc.clearRect(0, 0, cW, cH);

        /* Scale the background while preserving its aspect ratio */
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

        /* Compute grid metrics */
        double gridW = scaledMapWidth * gridWidthPercentage;
        double gridH = scaledMapHeight * gridHeightPercentage;
        double hLim = gridW / ((HEX_COLS - 1) * 0.75 + 1);
        double vLim = gridH / ((HEX_ROWS - 0.5) * 0.866 * 2);
        effectiveHexSize = Math.min(hLim, vLim) * 0.5 * gridScaleFactor;

        double addHX = gridHorizontalOffset * scaledMapWidth;
        double addHY = gridVerticalOffset * scaledMapHeight;

        drawHexGrid(gc, effectiveHexSize, gridW, gridH, addHX, addHY);
    }

    /**
     * Iterates over the logical grid, draws each hex and highlights the currently selected one.
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

        double hSpacing = size * horizontalSpacingFactor;
        double vSpacing = size * verticalSpacingFactor;

        double totW = hSpacing * (HEX_COLS - 0.5);
        double totH = vSpacing * HEX_ROWS;

        double baseX = mapOffsetX + (scaledMapWidth - gridW) / 2;
        double baseY = mapOffsetY + (scaledMapHeight - gridH) / 2;
        gridOffsetX = baseX + (gridW - totW) / 2 + addHX;
        gridOffsetY = baseY + (gridH - totH) / 2 + addHY;

        for (int r = 0; r < HEX_ROWS; r++) {
            for (int c = 0; c < HEX_COLS; c++) {
                double cx = gridOffsetX + c * hSpacing + (r % 2) * (hSpacing / 2);
                double cy = gridOffsetY + r * vSpacing;
                drawHex(gc, cx, cy, size, r == selectedRow && c == selectedCol);
            }
        }
        gc.setGlobalAlpha(1);
    }

    /**
     * Draws a single hexagon centred at the given coordinates.
     */
    private void drawHex(GraphicsContext gc, double cx, double cy, double size, boolean selected) {
        double[] xs = new double[6];
        double[] ys = new double[6];
        double rot = Math.toRadians(hexRotationDegrees);
        for (int i = 0; i < 6; i++) {
            double a = rot + 2 * Math.PI / 6 * i;
            xs[i] = cx + size * Math.cos(a) * horizontalSquishFactor;
            ys[i] = cy + size * Math.sin(a) * verticalSquishFactor;
        }

        gc.beginPath();
        gc.moveTo(xs[0], ys[0]);
        for (int i = 1; i < 6; i++) {
            gc.lineTo(xs[i], ys[i]);
        }
        gc.closePath();

        if (selected) {
            double oldAlpha = gc.getGlobalAlpha();
            Paint oldFill = gc.getFill();
            gc.setGlobalAlpha(0.3);
            gc.setFill(Color.YELLOW);
            gc.fill();
            gc.setGlobalAlpha(oldAlpha);
            gc.setFill(oldFill);
        }
        gc.stroke();
    }

    /* ── Hit detection ──────────────────────────────────────────────────────────────── */

    /**
     * Returns the row/column index of the hex at the given canvas coordinates or {@code null} when the point is not
     * inside any tile. The implementation mirrors the drawing algorithm and thus stays correct when rotation or
     * squish parameters change.
     */
    private int[] getHexAt(double px, double py) {
        if (!isMapLoaded || effectiveHexSize <= 0) {
            return null;
        }
        double hSpacing = effectiveHexSize * horizontalSpacingFactor;
        double vSpacing = effectiveHexSize * verticalSpacingFactor;
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
     * Standard ray‑casting point‑in‑polygon test for the current hex shape.
     */
    private boolean pointInHex(double px, double py, double cx, double cy, double size) {
        double rot = Math.toRadians(hexRotationDegrees);
        double[] xs = new double[6];
        double[] ys = new double[6];
        for (int i = 0; i < 6; i++) {
            double a = rot + 2 * Math.PI / 6 * i;
            xs[i] = cx + size * Math.cos(a) * horizontalSquishFactor;
            ys[i] = cy + size * Math.sin(a) * verticalSquishFactor;
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

    /* ── Settings overlay helpers ───────────────────────────────────────────────────── */

    /**
     * Creates the settings dialog instance and adds it as an overlay to the root {@link StackPane}.
     */
    private void initialiseSettingsDialog() {
        settingsDialog = new SettingsDialog();

        StackPane root = (StackPane) gameCanvas.getParent();
        if (!root.getChildren().contains(settingsDialog.getView())) {
            root.getChildren().add(settingsDialog.getView());
        }
        updateSettingsConnectionStatus();

        settingsDialog.setOnSaveAction(() -> LOGGER.info(() -> String.format(
                "Settings saved: Volume=%s, Mute=%s",
                settingsDialog.volumeProperty().get(), settingsDialog.muteProperty().get())));
    }

    /**
     * Updates the connection status indicator inside the settings overlay.
     */
    private void updateSettingsConnectionStatus() {
        String status = connectionStatusLabel.getText();
        boolean isConnected = "Connected".equals(status);
        settingsDialog.setConnectionStatus(isConnected, status);
    }
}
