package ch.unibas.dmi.dbis.cs108.client.ui.controllers;

import ch.unibas.dmi.dbis.cs108.shared.game.Player;
import ch.unibas.dmi.dbis.cs108.client.ui.SceneManager;
import ch.unibas.dmi.dbis.cs108.client.ui.components.ChatComponent;
import ch.unibas.dmi.dbis.cs108.client.ui.components.DescriptionDialog;
import ch.unibas.dmi.dbis.cs108.client.ui.components.SettingsDialog;
import ch.unibas.dmi.dbis.cs108.client.ui.components.game.GridAdjustmentManager;
import ch.unibas.dmi.dbis.cs108.client.ui.events.UIEventBus;
import ch.unibas.dmi.dbis.cs108.client.ui.events.admin.ConnectionStatusEvent;
import ch.unibas.dmi.dbis.cs108.client.ui.events.game.TileClickEvent;
import ch.unibas.dmi.dbis.cs108.client.ui.utils.ResourceLoader;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.Image;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.Node; // Import Node
import javafx.scene.layout.Priority; // Import Priority
import javafx.scene.layout.Region; // Import Region

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
 * Grid adjustment logic is handled by {@link GridAdjustmentManager}.
 */
public class GameScreenController extends BaseController {

    private static final Logger LOGGER = Logger.getLogger(GameScreenController.class.getName());
    static final int HEX_ROWS = 7;
    static final int HEX_COLS = 8;

    private Player localPlayer;
    private final ObservableList<String> players = FXCollections.observableArrayList();

    // Keep map/drawing related fields
    private double scaledMapWidth;
    private double scaledMapHeight;
    private double mapOffsetX;
    private double mapOffsetY;
    double effectiveHexSize; // Made package-private or public for GridAdjustmentManager
    double gridOffsetX; // Made package-private or public for GridAdjustmentManager
    double gridOffsetY; // Made package-private or public for GridAdjustmentManager

    private int selectedRow = -1;
    private int selectedCol = -1;
    private String currentLobbyId;

    private Image mapImage;
    private boolean isMapLoaded;

    private SettingsDialog settingsDialog;
    private DescriptionDialog descriptionDialog;
    private Pane currentlySelectedCard;

    // Add field for the GridAdjustmentManager
    private GridAdjustmentManager gridAdjustmentManager;

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

    // Add these fields for grid adjustment UI
    private Label adjustmentModeIndicator;
    private Label adjustmentValuesLabel;

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
        createAdjustmentUI(); // Create UI elements first
        // Instantiate GridAdjustmentManager after UI elements are created
        this.gridAdjustmentManager = new GridAdjustmentManager(this, adjustmentModeIndicator, adjustmentValuesLabel,
                this::drawMapAndGrid);
        setupCanvasListeners(); // Setup listeners after manager is created
        initialiseSettingsDialog();
        initialiseDescriptionDialog();

        // --- ChatComponent setup ---
        chatComponentController = new ChatComponent();
        chatComponentController.setPlayer(localPlayer);
        chatComponentController.setCurrentLobbyId(currentLobbyId);
        chatComponentController.addSystemMessage("Game interface initialized successfully!");
        if (chatContainer != null) {
            chatContainer.getChildren().clear();
            Node chatView = chatComponentController.getView(); // Get the view Node
            chatContainer.getChildren().add(chatView);
            VBox.setVgrow(chatView, Priority.ALWAYS); // Make the chat component grow vertically
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
        if (settingsDialog == null) {
            LOGGER.warning("Settings dialog is not initialised");
            return;
        }
        Pane root = (StackPane) gameCanvas.getParent();
        if (root == null) {
            LOGGER.warning("Cannot show settings: Root pane (StackPane) not found.");
            return;
        }

        updateSettingsConnectionStatus();

        showDialogAsOverlay(settingsDialog, root);
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
            descriptionDialog.close();
        LOGGER.info("GameScreenController resources cleaned up");
    }

    /**
     * Toggles grid-adjustment mode on/off using the GridAdjustmentManager.
     */
    public void toggleGridAdjustmentMode() {
        gridAdjustmentManager.toggleGridAdjustmentMode();
    }

    /**
     * Enables or disables grid-adjustment mode using the GridAdjustmentManager.
     *
     * @param active {@code true} to enter adjustment mode, {@code false} to leave
     *               it.
     */
    public void setGridAdjustmentMode(boolean active) {
        gridAdjustmentManager.setGridAdjustmentMode(active);
    }

    /**
     * Returns a formatted string with all grid parameters from the
     * GridAdjustmentManager.
     */
    public String getGridSettings() {
        return gridAdjustmentManager.getGridSettings();
    }

    /**
     * Handles shortcuts registered on the parent FXML root.
     * Delegates grid adjustment toggling to the manager.
     */
    @FXML
    private void handleKeyboardShortcut(KeyEvent e) {
        // Delegate grid adjustment toggle
        if (gridAdjustmentManager.handleKeyboardShortcut(e)) {
            e.consume();
        }
        // Handle other shortcuts if needed
    }

    /**
     * Adds the translucent info panel used while in adjustment mode.
     * Stores the created labels in fields.
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
     * Delegates key presses to GridAdjustmentManager.
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

        // Delegate key presses to the manager
        gameCanvas.setOnKeyPressed(gridAdjustmentManager::handleGridAdjustmentKeys);
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
     * Redraws the background image and the hex overlay using the current
     * parameters from GridAdjustmentManager.
     */
    // Make drawMapAndGrid public or package-private if needed by
    // GridAdjustmentManager
    void drawMapAndGrid() {
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

        // Get parameters from the manager
        double gridW = scaledMapWidth * gridAdjustmentManager.getGridWidthPercentage();
        double gridH = scaledMapHeight * gridAdjustmentManager.getGridHeightPercentage();
        double hLim = gridW / ((HEX_COLS - 1) * 0.75 + 1);
        double vLim = gridH / ((HEX_ROWS - 0.5) * 0.866 * 2);
        effectiveHexSize = Math.min(hLim, vLim) * 0.5 * gridAdjustmentManager.getGridScaleFactor();

        double addHX = gridAdjustmentManager.getGridHorizontalOffset() * scaledMapWidth;
        double addHY = gridAdjustmentManager.getGridVerticalOffset() * scaledMapHeight;

        drawHexGrid(gc, effectiveHexSize, gridW, gridH, addHX, addHY);

        // Ensure adjustment UI is visible if mode is active
        if (gridAdjustmentManager.isGridAdjustmentModeActive()) {
            adjustmentModeIndicator.setVisible(true);
            adjustmentValuesLabel.setVisible(true);
        }
    }

    /**
     * Iterates over the logical grid, draws each hex and highlights the currently
     * selected one. Uses parameters from GridAdjustmentManager.
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

        // Get parameters from the manager
        double hSpacing = size * gridAdjustmentManager.getHorizontalSpacingFactor();
        double vSpacing = size * gridAdjustmentManager.getVerticalSpacingFactor();

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
     * Draws a single hexagon centred at the given coordinates. Uses parameters from
     * GridAdjustmentManager.
     */
    private void drawHex(GraphicsContext gc, double cx, double cy, double size, boolean selected) {
        double[] xs = new double[6];
        double[] ys = new double[6];
        // Get parameters from the manager
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
     * {@code null} when the point is not inside any tile. Uses parameters from
     * GridAdjustmentManager.
     */
    // Make package-private or public if needed by GridAdjustmentManager
    int[] getHexAt(double px, double py) {
        if (!isMapLoaded || effectiveHexSize <= 0) {
            return null;
        }
        // Get parameters from the manager
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
     * Standard ray-casting point-in-polygon test for the current hex shape. Uses
     * parameters from GridAdjustmentManager.
     */
    // Make package-private or public if needed by GridAdjustmentManager
    boolean pointInHex(double px, double py, double cx, double cy, double size) {
        // Get parameters from the manager
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

    /**
     * Creates the settings dialog instance. Does not add it to the scene graph yet.
     */
    private void initialiseSettingsDialog() {
        settingsDialog = new SettingsDialog();
        // Set save action specific to game screen context
        settingsDialog.setOnSaveAction(() -> LOGGER.info(() -> String.format(
                "Settings saved in-game: Volume=%s, Mute=%s",
                settingsDialog.volumeProperty().get(), settingsDialog.muteProperty().get())));
        // BaseController's showDialogAsOverlay will handle the onCloseAction for layout
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

    public Canvas getGameCanvas() {
        return gameCanvas;
    }
}
