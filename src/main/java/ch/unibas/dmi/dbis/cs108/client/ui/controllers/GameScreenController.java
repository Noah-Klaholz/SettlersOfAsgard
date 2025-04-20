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
 * Controller for the in-game view, responsible for rendering the map and hex grid,
 * handling user interactions, and dispatching UI events via the {@link UIEventBus}.
 * Supports a runtime "Grid Adjustment Mode" (toggle with Ctrl+G) for fine-tuning grid parameters.
 */
public class GameScreenController extends BaseController {

    private static final Logger LOGGER = Logger.getLogger(GameScreenController.class.getName());
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");
    private static final int HEX_ROWS = 7;
    private static final int HEX_COLS = 8;

    // Default grid parameters (do not modify unless map artwork changes)
    private static final double DEF_GRID_SCALE = 0.85;
    private static final double DEF_GRID_H_OFFSET = 0.00;
    private static final double DEF_GRID_V_OFFSET = 0.15;
    private static final double DEF_GRID_WIDTH_PCT = 0.90;
    private static final double DEF_GRID_HEIGHT_PCT = 2.06;
    private static final double DEF_ROTATION_DEG = 30.0;
    private static final double DEF_H_SPACING = 1.80;
    private static final double DEF_V_SPACING = 1.33;
    private static final double DEF_H_SQUISH = 1.00;
    private static final double DEF_V_SQUISH = 0.80;

    // UI data models
    private final ObservableList<String> chatMessages = FXCollections.observableArrayList();
    private final ObservableList<String> players = FXCollections.observableArrayList();

    // Computed rendering parameters
    private double scaledMapWidth;
    private double scaledMapHeight;
    private double mapOffsetX;
    private double mapOffsetY;
    private double effectiveHexSize;
    private double gridOffsetX;
    private double gridOffsetY;

    // Adjustable grid parameters
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
    private SettingsDialog settingsDialog;

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

    /**
     * Constructs the controller, initializing the resource loader, event bus, and scene manager.
     */
    public GameScreenController() {
        super(new ResourceLoader(), UIEventBus.getInstance(), SceneManager.getInstance());
    }

    /**
     * Scrolls a ListView to the bottom.
     *
     * @param list the ListView to scroll
     * @param data its data model
     */
    private static void scrollBottom(ListView<String> list, ObservableList<String> data) {
        Platform.runLater(() -> {
            if (!data.isEmpty()) list.scrollTo(data.size() - 1);
        });
    }

    /**
     * FXML initializer: configures UI, listeners, and loads resources.
     */
    @FXML
    private void initialize() {
        LOGGER.setLevel(Level.ALL);
        setupUI();
        subscribeEvents();
        loadMapImage();
        setupCanvasListeners();
        createAdjustmentUI();
        initializeSettingsDialog();
    }

    /**
     * Sets up initial UI state and data bindings.
     */
    private void setupUI() {
        chatListView.setItems(chatMessages);
        playersList.setItems(players);
        globalChatButton.setSelected(true);
        energyBar.setProgress(0.5);
        runesLabel.setText("0");
        connectionStatusLabel.setText("Connected");
        gameCanvas.widthProperty().bind(((Region) gameCanvas.getParent()).widthProperty());
        gameCanvas.heightProperty().bind(((Region) gameCanvas.getParent()).heightProperty());
    }

    /**
     * Subscribes to UI events on the event bus.
     */
    private void subscribeEvents() {
        eventBus.subscribe(GlobalChatEvent.class, this::onGlobalChat);
        eventBus.subscribe(LobbyChatEvent.class, this::onLobbyChat);
        eventBus.subscribe(ConnectionStatusEvent.class, this::onConnectionStatus);
        eventBus.subscribe(TileClickEvent.class, this::onTileClick);
        chatInputField.setOnAction(e -> handleMessageSend());
    }

    /**
     * Handles a global chat event.
     *
     * @param event contains sender and content
     */
    private void onGlobalChat(GlobalChatEvent event) {
        if (event == null || event.getSender() == null || event.getContent() == null) return;
        Platform.runLater(() -> addChat(String.format("%s: %s", event.getSender(), event.getContent())));
    }

    /**
     * Handles a lobby chat event.
     *
     * @param event contains lobby ID, sender, and message
     */
    private void onLobbyChat(LobbyChatEvent event) {
        if (event == null || event.getSender() == null || event.getMessage() == null) return;
        Platform.runLater(() -> addChat(String.format("[Lobby] %s: %s", event.getSender(), event.getMessage())));
    }

    /**
     * Updates connection status display and shows system messages.
     *
     * @param event contains status and optional message
     */
    private void onConnectionStatus(ConnectionStatusEvent event) {
        if (event == null) return;
        Platform.runLater(() -> {
            connectionStatusLabel.setText(
                    Optional.ofNullable(event.getStatus()).map(Object::toString).orElse("UNKNOWN")
            );
            if (event.getMessage() != null && !event.getMessage().isEmpty()) {
                addSystem(event.getMessage());
            }
            if (settingsDialog != null) {
                updateSettingsConnectionStatus();
            }
        });
    }

    /**
     * Logs external tile click events.
     *
     * @param event source of tile click
     */
    private void onTileClick(TileClickEvent event) {
        LOGGER.fine(() -> String.format("Tile clicked externally (row=%d, col=%d)", event.getRow(), event.getCol()));
    }

    /**
     * Adds a timestamped chat message.
     *
     * @param msg message content
     */
    private void addChat(String msg) {
        chatMessages.add(String.format("[%s] %s", LocalDateTime.now().format(TIME_FMT), msg));
        scrollBottom(chatListView, chatMessages);
    }

    /**
     * Adds a timestamped system message.
     *
     * @param msg system message content
     */
    private void addSystem(String msg) {
        chatMessages.add(String.format("[%s] System: %s", LocalDateTime.now().format(TIME_FMT), msg));
        scrollBottom(chatListView, chatMessages);
    }

    /**
     * Loads the map image and triggers initial drawing.
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
     * Configures canvas event listeners for resize, click, and key events.
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
        gameCanvas.setOnKeyPressed(this::handleGridAdjustmentKeys);
    }

    /**
     * Initializes and embeds the settings dialog.
     */
    private void initializeSettingsDialog() {
        settingsDialog = new SettingsDialog();
        StackPane root = (StackPane) gameCanvas.getParent();
        if (!root.getChildren().contains(settingsDialog.getView())) {
            root.getChildren().add(settingsDialog.getView());
        }
        updateSettingsConnectionStatus();
        settingsDialog.setOnSaveAction(() -> LOGGER.info(
                "Settings saved: Volume=" + settingsDialog.volumeProperty().get() +
                        ", Mute=" + settingsDialog.muteProperty().get()
        ));
    }

    /**
     * Updates connection status in the settings dialog.
     */
    private void updateSettingsConnectionStatus() {
        String status = connectionStatusLabel.getText();
        settingsDialog.setConnectionStatus("Connected".equals(status), status);
    }

    /**
     * Creates UI overlays for grid adjustment feedback.
     */
    private void createAdjustmentUI() {
        adjustmentModeIndicator = new Label("GRID ADJUSTMENT MODE (Press G to exit)");
        adjustmentModeIndicator.setStyle(
                "-fx-background-color:rgba(255,165,0,0.7); -fx-text-fill:white;" +
                        " -fx-padding:5 10; -fx-background-radius:5; -fx-font-weight:bold;"
        );
        adjustmentModeIndicator.setVisible(false);

        adjustmentValuesLabel = new Label();
        adjustmentValuesLabel.setStyle(
                "-fx-background-color:rgba(0,0,0,0.7); -fx-text-fill:white;" +
                        " -fx-padding:5; -fx-font-size:11; -fx-background-radius:3;"
        );
        adjustmentValuesLabel.setVisible(false);

        VBox panel = new VBox(5, adjustmentModeIndicator, adjustmentValuesLabel);
        panel.setAlignment(Pos.TOP_CENTER);
        panel.setPadding(new Insets(10));
        ((StackPane) gameCanvas.getParent()).getChildren().add(panel);
        StackPane.setAlignment(panel, Pos.TOP_CENTER);
    }

    /**
     * Handles canvas clicks: detects hex and publishes a TileClickEvent.
     *
     * @param px x-coordinate of click
     * @param py y-coordinate of click
     */
    private void handleCanvasClick(double px, double py) {
        int[] tile = getHexAt(px, py);
        if (tile != null) {
            selectedRow = tile[0];
            selectedCol = tile[1];
            drawMapAndGrid();
            eventBus.publish(new TileClickEvent(selectedRow, selectedCol));
        }
    }

    /**
     * Renders the map background and hex grid overlay.
     */
    private void drawMapAndGrid() {
        if (!isMapLoaded) return;
        double cW = gameCanvas.getWidth();
        double cH = gameCanvas.getHeight();
        if (cW <= 0 || cH <= 0) return;
        GraphicsContext gc = gameCanvas.getGraphicsContext2D();
        gc.clearRect(0, 0, cW, cH);

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

        double gridW = scaledMapWidth * gridWidthPercentage;
        double gridH = scaledMapHeight * gridHeightPercentage;
        double hLim = gridW / ((HEX_COLS - 1) * 0.75 + 1);
        double vLim = gridH / ((HEX_ROWS - 0.5) * 0.866 * 2);
        effectiveHexSize = Math.min(hLim, vLim) * 0.5 * gridScaleFactor;
        double addHX = gridHorizontalOffset * scaledMapWidth;
        double addHY = gridVerticalOffset * scaledMapHeight;
        drawHexGrid(gc, effectiveHexSize, gridW, gridH, addHX, addHX);
    }

    /**
     * Draws the hex grid.
     */
    private void drawHexGrid(GraphicsContext gc, double size, double gridW, double gridH, double addHX, double addHY) {
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
                drawHex(gc, cx, cy, size, (r == selectedRow && c == selectedCol));
            }
        }
        gc.setGlobalAlpha(1.0);
    }

    /**
     * Draws a single hexagon at the specified center.
     */
    private void drawHex(GraphicsContext gc, double cx, double cy, double size, boolean selected) {
        double[] xs = new double[6];
        double[] ys = new double[6];
        double rot = Math.toRadians(hexRotationDegrees);
        for (int i = 0; i < 6; i++) {
            double angle = rot + 2 * Math.PI / 6 * i;
            xs[i] = cx + size * Math.cos(angle) * horizontalSquishFactor;
            ys[i] = cy + size * Math.sin(angle) * verticalSquishFactor;
        }
        gc.beginPath();
        gc.moveTo(xs[0], ys[0]);
        for (int i = 1; i < 6; i++) gc.lineTo(xs[i], ys[i]);
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

    /**
     * Determines the hex under the given point, or null if none.
     */
    private int[] getHexAt(double px, double py) {
        if (!isMapLoaded || effectiveHexSize <= 0) return null;
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
     * Ray-casting algorithm to test if point lies within a hexagon.
     */
    private boolean pointInHex(double px, double py, double cx, double cy, double size) {
        double[] xs = new double[6];
        double[] ys = new double[6];
        double rot = Math.toRadians(hexRotationDegrees);
        for (int i = 0; i < 6; i++) {
            double angle = rot + 2 * Math.PI / 6 * i;
            xs[i] = cx + size * Math.cos(angle) * horizontalSquishFactor;
            ys[i] = cy + size * Math.sin(angle) * verticalSquishFactor;
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

    /**
     * Handles grid adjustment keyboard shortcuts.
     */
    @FXML
    private void handleGridAdjustmentKeys(KeyEvent e) {
        if (!gridAdjustmentModeActive && !(e.isControlDown() && e.getCode() == KeyCode.G)) return;
        switch (e.getCode()) {
            case ESCAPE -> setGridAdjustmentMode(false);
            case G -> toggleGridAdjustmentMode();
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
            case H -> horizontalSquishFactor = Math.max(0.5, horizontalSquishFactor - 0.1);
            case J -> horizontalSquishFactor = Math.min(2, horizontalSquishFactor + 0.1);
            case V -> verticalSpacingFactor = Math.max(1, verticalSpacingFactor - 0.1);
            case B -> verticalSpacingFactor += 0.1;
            case K -> verticalSquishFactor = Math.max(0.5, verticalSquishFactor - 0.1);
            case L -> verticalSquishFactor = Math.min(2, verticalSquishFactor + 0.1);
            case BACK_SPACE -> resetDefaults();
            case P -> LOGGER.info(getGridSettings());
            case C -> copyGridSettingsToClipboard();
            default -> LOGGER.fine(() -> String.format("Ignored key event: %s", e.getCode()));
        }
        drawMapAndGrid();
        updateAdjustmentFeedback();
        e.consume();
    }

    /**
     * Toggles grid adjustment mode.
     */
    public void toggleGridAdjustmentMode() {
        setGridAdjustmentMode(!gridAdjustmentModeActive);
    }

    /**
     * Enables or disables grid adjustment mode.
     *
     * @param active true to enable, false to disable
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

    private void updateAdjustmentFeedback() {
        adjustmentValuesLabel.setText(String.format(
                "Scale: %.2f | Offset:(%.2f,%.2f) | Size: %.0f%%x%.0f%% | Rot: %.1f° | Spacing:(%.2f,%.2f) | Squish:(%.2f,%.2f)",
                gridScaleFactor, gridHorizontalOffset, gridVerticalOffset,
                gridWidthPercentage * 100, gridHeightPercentage * 100,
                hexRotationDegrees, horizontalSpacingFactor, verticalSpacingFactor,
                horizontalSquishFactor, verticalSquishFactor
        ));
    }

    private void copyGridSettingsToClipboard() {
        ClipboardContent content = new ClipboardContent();
        content.putString(getGridSettings());
        Clipboard.getSystemClipboard().setContent(content);
        adjustmentValuesLabel.setStyle(
                adjustmentValuesLabel.getStyle() + " -fx-background-color:rgba(0,128,0,0.8);"
        );
        PauseTransition pause = new PauseTransition(Duration.seconds(0.5));
        pause.setOnFinished(e -> adjustmentValuesLabel.setStyle(
                "-fx-background-color: rgba(0,0,0,0.7);"
        ));
        pause.play();
    }

    /**
     * Returns a descriptive string of current grid settings.
     */
    public String getGridSettings() {
        return String.format(
                "Grid settings: scale=%.2f, hOffset=%.2f, vOffset=%.2f, width=%.2f%%, height=%.2f%%, " +
                        "rotation=%.1f°, hSpacing=%.2f, vSpacing=%.2f, hSquish=%.2f, vSquish=%.2f",
                gridScaleFactor, gridHorizontalOffset, gridVerticalOffset,
                gridWidthPercentage * 100, gridHeightPercentage * 100,
                hexRotationDegrees, horizontalSpacingFactor, verticalSpacingFactor,
                horizontalSquishFactor, verticalSquishFactor
        );
    }

    /**
     * Sets the lobby ID for sending lobby messages.
     */
    public void setCurrentLobbyId(String lobbyId) {
        this.currentLobbyId = lobbyId;
    }

    /**
     * Sends a chat message based on current mode (global or lobby).
     */
    @FXML
    private void handleMessageSend() {
        String msg = chatInputField.getText().trim();
        if (msg.isEmpty()) return;
        if (globalChatButton.isSelected()) {
            eventBus.publish(new GlobalChatEvent(msg, GlobalChatEvent.ChatType.GLOBAL));
        } else {
            if (currentLobbyId == null || currentLobbyId.isEmpty()) {
                addSystem("Cannot send lobby message: You're not in a lobby.");
            } else {
                eventBus.publish(new LobbyChatEvent(currentLobbyId, msg));
            }
        }
        chatInputField.clear();
    }

    /**
     * FXML action: switch back to main menu.
     */
    @FXML
    private void handleBackToMainMenu() {
        sceneManager.switchToScene(SceneManager.SceneType.MAIN_MENU);
    }

    /**
     * FXML action: show settings dialog.
     */
    @FXML
    private void handleSettings() {
        if (settingsDialog != null) {
            settingsDialog.show();
        } else {
            LOGGER.warning("Settings dialog not initialized");
        }
    }

    /**
     * Placeholder for resource overview.
     */
    @FXML
    private void handleResourceOverview() {
        // TODO: implement
    }

    /**
     * Placeholder for game round UI.
     */
    @FXML
    private void handleGameRound() {
        // TODO: implement
    }

    /**
     * Placeholder for leaderboard UI.
     */
    @FXML
    private void handleLeaderboard() {
        // TODO: implement
    }

    /**
     * Enables global chat mode.
     */
    @FXML
    private void handleGlobalChatSelect() {
        globalChatButton.setSelected(true);
        lobbyChatButton.setSelected(false);
    }

    /**
     * Enables lobby chat mode and warns if not in a lobby.
     */
    @FXML
    private void handleLobbyChatSelect() {
        globalChatButton.setSelected(false);
        lobbyChatButton.setSelected(true);
        if (currentLobbyId == null || currentLobbyId.isEmpty()) {
            addSystem("You are not in a lobby.");
        }
    }

    /**
     * Unsubscribes from event bus; call on controller disposal.
     */
    public void cleanup() {
        eventBus.unsubscribe(GlobalChatEvent.class, this::onGlobalChat);
        eventBus.unsubscribe(LobbyChatEvent.class, this::onLobbyChat);
        eventBus.unsubscribe(ConnectionStatusEvent.class, this::onConnectionStatus);
        eventBus.unsubscribe(TileClickEvent.class, this::onTileClick);
    }
}
