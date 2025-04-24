package ch.unibas.dmi.dbis.cs108.client.ui.controllers;

import ch.unibas.dmi.dbis.cs108.client.core.Player;
import ch.unibas.dmi.dbis.cs108.client.ui.SceneManager;
import ch.unibas.dmi.dbis.cs108.client.ui.components.ChatComponent;
import ch.unibas.dmi.dbis.cs108.client.ui.components.DescriptionDialog;
import ch.unibas.dmi.dbis.cs108.client.ui.components.SettingsDialog;
import ch.unibas.dmi.dbis.cs108.client.ui.events.UIEventBus;
import ch.unibas.dmi.dbis.cs108.client.ui.events.admin.ConnectionStatusEvent;
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

import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * JavaFX controller for the in-game screen.
 * Responsible for loading and drawing the map, rendering the hexagonal grid
 * overlay,
 * dispatching user interactions (tile clicks, chat input, etc.) through the
 * {@link UIEventBus},
 * and managing overlays such as settings and card descriptions.
 */
public class GameScreenController extends BaseController {

    private static final Logger LOGGER = Logger.getLogger(GameScreenController.class.getName());
    private static final int HEX_ROWS = 7;
    private static final int HEX_COLS = 8;

    // Default grid parameters
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

    private Player localPlayer;

    private final ObservableList<String> players = FXCollections.observableArrayList();

    private double scaledMapWidth;
    private double scaledMapHeight;
    private double mapOffsetX;
    private double mapOffsetY;
    private double effectiveHexSize;
    private double gridOffsetX;
    private double gridOffsetY;

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
    private DescriptionDialog descriptionDialog;
    private Pane currentlySelectedCard;

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
    private VBox chatContainer; // Add this field for the chat container

    private ChatComponent chatComponentController; // Now managed programmatically

    /**
     * Creates a new controller instance and wires the shared singletons.
     */
    public GameScreenController() {
        super(new ResourceLoader(), UIEventBus.getInstance(), SceneManager.getInstance());
        this.localPlayer = new Player(System.getProperty("user.name", "Guest"));
    }

    /**
     * Called automatically by the JavaFX loader. Sets up UI bindings, subscribes to
     * the event-bus, loads resources and prepares event handlers.
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
        initialiseDescriptionDialog();

        // --- ChatComponent setup ---
        chatComponentController = new ChatComponent();
        chatComponentController.setPlayer(localPlayer);
        chatComponentController.setCurrentLobbyId(currentLobbyId);
        chatComponentController.addSystemMessage("Game interface initialized successfully!");
        if (chatContainer != null) {
            chatContainer.getChildren().clear();
            chatContainer.getChildren().add(chatComponentController.getView());
        } else {
            LOGGER.severe("chatContainer VBox is null! Check FXML for fx:id=\"chatContainer\"");
        }
        // --- end ChatComponent setup ---
    }

    /**
     * Wires UI controls to their backing properties and assigns sensible defaults.
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
     * Installs all event-bus subscriptions and UI callbacks.
     */
    private void subscribeEvents() {
        eventBus.subscribe(ConnectionStatusEvent.class, this::onConnectionStatus);
        eventBus.subscribe(TileClickEvent.class, this::onTileClick);
    }

    private void onConnectionStatus(ConnectionStatusEvent e) {
        if (e == null) {
            return;
        }
        Platform.runLater(() -> {
            connectionStatusLabel.setText(Optional.ofNullable(e.getState()).map(Object::toString).orElse("UNKNOWN"));
            if (e.getMessage() != null && !e.getMessage().isEmpty() && chatComponentController != null) {
                chatComponentController.addSystemMessage(e.getMessage());
            }
            if (settingsDialog != null) {
                updateSettingsConnectionStatus();
            }
        });
    }

    private void onTileClick(TileClickEvent e) {
        LOGGER.fine(() -> String.format("Tile clicked externally (row=%d,col=%d)", e.getRow(), e.getCol()));
    }

    /**
     * Navigates back to the main-menu scene.
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
    private void handleResourceOverview() {
    }

    @FXML
    private void handleGameRound() {
    }

    @FXML
    private void handleLeaderboard() {
    }

    /**
     * Sets the lobby-identifier used when sending lobby chat messages.
     *
     * @param lobbyId unique identifier of the lobby the user is currently in (may
     *                be {@code null}).
     */
    public void setCurrentLobbyId(String lobbyId) {
        this.currentLobbyId = lobbyId;
        if (chatComponentController != null) {
            chatComponentController.setCurrentLobbyId(lobbyId);
        } else {
            LOGGER.warning("Cannot set lobby ID: Chat component controller is null");
        }
    }

    /**
     * Sets the local player object for this controller and updates the chat
     * component.
     *
     * @param player The local player object.
     */
    public void setLocalPlayer(Player player) {
        if (player != null) {
            this.localPlayer = player;
            if (chatComponentController != null) {
                chatComponentController.setPlayer(player);
            } else {
                LOGGER.warning("Cannot set player: Chat component controller is null");
            }
        }
    }

    /**
     * Unsubscribes from all event-bus channels. Must be called when the controller
     * is disposed to avoid memory leaks.
     */
    public void cleanup() {
        eventBus.unsubscribe(ConnectionStatusEvent.class, this::onConnectionStatus);
        eventBus.unsubscribe(TileClickEvent.class, this::onTileClick);
        if (chatComponentController != null) {
            chatComponentController.cleanup();
        }
        if (settingsDialog != null)
            settingsDialog.close();
        if (descriptionDialog != null)
            descriptionDialog.hide();
        LOGGER.info("GameScreenController resources cleaned up");
    }

    /**
     * Toggles grid-adjustment mode on/off.
     */
    public void toggleGridAdjustmentMode() {
        setGridAdjustmentMode(!gridAdjustmentModeActive);
    }

    /**
     * Enables or disables grid-adjustment mode.
     *
     * @param active {@code true} to enter adjustment mode, {@code false} to leave
     *               it.
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
     * Returns a formatted string with all grid parameters.
     *
     * @return A string with the current grid settings.
     */
    public String getGridSettings() {
        return String.format(
                "Grid settings: scale=%.2f, hOffset=%.2f, vOffset=%.2f, width=%.2f%%, height=%.2f%%, rotation=%.1f°, " +
                        "hSpacing=%.2f, vSpacing=%.2f, hSquish=%.2f, vSquish=%.2f",
                gridScaleFactor, gridHorizontalOffset, gridVerticalOffset, gridWidthPercentage * 100,
                gridHeightPercentage * 100, hexRotationDegrees, horizontalSpacingFactor, verticalSpacingFactor,
                horizontalSquishFactor, verticalSquishFactor);
    }

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
     * Installs listeners so that the grid is redrawn when the canvas size changes
     * and so that mouse/keyboard events are captured.
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
     * Processes key presses while in grid-adjustment mode.
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
                return;
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
     * Copies the grid parameter string to the system clipboard and flashes a green
     * background as feedback.
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

    /**
     * Redraws the background image and the hex overlay using the current
     * parameters.
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

        drawHexGrid(gc, effectiveHexSize, gridW, gridH, addHX, addHY);
    }

    /**
     * Iterates over the logical grid, draws each hex and highlights the currently
     * selected one.
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

    /**
     * Returns the row/column index of the hex at the given canvas coordinates or
     * {@code null} when the point is not inside any tile.
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
                    return new int[] { r, c };
                }
            }
        }
        return null;
    }

    /**
     * Standard ray-casting point-in-polygon test for the current hex shape.
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

    /**
     * Creates the settings dialog instance and adds it as an overlay to the root
     * StackPane.
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

    /**
     * Handles a click on a card in the player's hand.
     *
     * @param event The mouse event from the clicked card.
     */
    @FXML
    private void handleCardClick(MouseEvent event) {
        if (!(event.getSource() instanceof Pane clickedCard)) {
            return;
        }
        if (currentlySelectedCard != null) {
            currentlySelectedCard.getStyleClass().remove("selected-card");
        }
        currentlySelectedCard = clickedCard;
        clickedCard.getStyleClass().add("selected-card");
        String cardId = clickedCard.getId();
        boolean isArtifact = cardId.startsWith("artifact");
        String cardType = isArtifact ? "Artifact" : "Structure";
        int cardIndex = Integer.parseInt(cardId.replaceAll("[^0-9]", ""));
        String title = cardType + " #" + cardIndex;
        String description = getMockCardDescription(cardType, cardIndex);
        showCardDescription(title, description);
        event.consume();
    }

    /**
     * Handles the start of a drag operation on a card.
     * Puts the card's ID onto the dragboard.
     *
     * @param event The mouse event that triggered the drag detection.
     */
    @FXML
    private void handleCardDragDetected(MouseEvent event) {
        if (!(event.getSource() instanceof Pane draggedCard)) {
            return;
        }
        String cardId = draggedCard.getId();
        if (cardId == null || cardId.isEmpty()) {
            LOGGER.warning("Dragged card has no ID.");
            return;
        }

        LOGGER.fine("Drag detected on card: " + cardId);
        Dragboard db = draggedCard.startDragAndDrop(TransferMode.MOVE);

        ClipboardContent content = new ClipboardContent();
        content.putString(cardId); // Put the card's ID onto the dragboard
        db.setContent(content);

        // Optional: Set a drag view (snapshot of the card)
        // db.setDragView(draggedCard.snapshot(null, null));

        event.consume();
    }

    /**
     * Shows the card description dialog on top of the chat panel.
     *
     * @param title       The card title
     * @param description The card description text
     */
    private void showCardDescription(String title, String description) {
        if (descriptionDialog != null) {
            descriptionDialog.show(title, description);
        }
    }

    /**
     * Provides mock card descriptions for demonstration purposes.
     */
    private String getMockCardDescription(String cardType, int index) {
        if ("Artifact".equals(cardType)) {
            return switch (index) {
                case 1 ->
                    "Mjölnir, the legendary hammer of Thor. Grants +2 attack power and the ability to strike enemies with lightning.";
                case 2 -> "Gungnir, Odin's enchanted spear. Never misses its target and grants wisdom to its bearer.";
                case 3 -> "Draupnir, the magical ring that creates eight duplicates of itself every ninth night.";
                default -> "A mysterious artifact with unknown powers.";
            };
        } else {
            return switch (index) {
                case 1 -> "Mead Hall: Increases morale and provides +1 rune per turn.";
                case 2 -> "Forge: Allows crafting of enhanced weapons and tools.";
                case 3 -> "Watchtower: Improves visibility and provides early warning of attacks.";
                case 4 -> "Runestone: Increases magical energy production by 15%.";
                case 5 -> "Barracks: Allows training of elite warriors.";
                case 6 -> "Market: Enables trading with other settlements.";
                case 7 -> "Temple: Provides divine blessings and special abilities.";
                case 8 -> "Wall: Improves settlement defense against raids.";
                default -> "A basic structure that provides shelter.";
            };
        }
    }

    /**
     * Creates and initializes the description dialog.
     */
    private void initialiseDescriptionDialog() {
        descriptionDialog = new DescriptionDialog();
        StackPane root = (StackPane) gameCanvas.getParent();
        if (descriptionDialog.getView() != null && root != null) {
            if (!root.getChildren().contains(descriptionDialog.getView())) {
                root.getChildren().add(descriptionDialog.getView());
            }
            StackPane.setAlignment(descriptionDialog.getView(), Pos.TOP_RIGHT);
            StackPane.setMargin(descriptionDialog.getView(), new Insets(10, 300, 10, 10));
        } else {
            LOGGER.severe("Failed to add description dialog to root pane");
        }
    }

    /**
     * Handles drag events over the game canvas.
     * Accepts the transfer if it contains a string (e.g., card ID).
     *
     * @param event The drag event.
     */
    @FXML
    private void handleDragOver(DragEvent event) {
        if (event.getGestureSource() != gameCanvas && event.getDragboard().hasString()) {
            // Allow for moving
            event.acceptTransferModes(TransferMode.MOVE);
        }
        event.consume();
    }

    /**
     * Handles dropped events on the game canvas.
     * If a card was dropped, determines the target tile and potentially triggers a
     * game action.
     *
     * @param event The drag event.
     */
    @FXML
    private void handleDragDropped(DragEvent event) {
        Dragboard db = event.getDragboard();
        boolean success = false;
        if (db.hasString()) {
            String cardId = db.getString();
            double dropX = event.getX();
            double dropY = event.getY();
            int[] targetTile = getHexAt(dropX, dropY);

            if (targetTile != null) {
                LOGGER.info("Card " + cardId + " dropped on tile: row=" + targetTile[0] + ", col=" + targetTile[1]);
                // TODO: Implement logic to handle card drop on tile (e.g., publish an event)
                // Example: eventBus.publish(new CardDroppedOnTileEvent(cardId, targetTile[0],
                // targetTile[1]));
                success = true;
            } else {
                LOGGER.warning("Card " + cardId + " dropped outside of any valid tile.");
            }
        }
        // Complete the drag-and-drop gesture
        event.setDropCompleted(success);
        event.consume();
    }
}
