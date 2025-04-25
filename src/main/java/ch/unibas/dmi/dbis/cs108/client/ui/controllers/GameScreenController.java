package ch.unibas.dmi.dbis.cs108.client.ui.controllers;

import ch.unibas.dmi.dbis.cs108.SETTINGS;
import ch.unibas.dmi.dbis.cs108.client.app.GameApplication;
import ch.unibas.dmi.dbis.cs108.client.ui.events.admin.ChangeNameUIEvent;
import ch.unibas.dmi.dbis.cs108.client.ui.events.admin.NameChangeRequestEvent;
import ch.unibas.dmi.dbis.cs108.client.ui.events.admin.NameChangeResponseEvent;
import ch.unibas.dmi.dbis.cs108.client.ui.events.lobby.LobbyJoinedEvent;
import ch.unibas.dmi.dbis.cs108.shared.game.Player;
import ch.unibas.dmi.dbis.cs108.client.ui.SceneManager;
import ch.unibas.dmi.dbis.cs108.client.ui.components.ChatComponent;
import ch.unibas.dmi.dbis.cs108.client.ui.components.SettingsDialog;
import ch.unibas.dmi.dbis.cs108.client.ui.components.game.GridAdjustmentManager;
import ch.unibas.dmi.dbis.cs108.client.ui.events.UIEventBus;
import ch.unibas.dmi.dbis.cs108.client.ui.events.admin.ConnectionStatusEvent;
import ch.unibas.dmi.dbis.cs108.client.ui.events.game.PlaceStructureUIEvent; // Import the event
import ch.unibas.dmi.dbis.cs108.client.ui.events.game.TileClickEvent;
import ch.unibas.dmi.dbis.cs108.client.ui.utils.ResourceLoader;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
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
import javafx.scene.Node; // Import Node
import javafx.scene.layout.Priority; // Import Priority
import javafx.scene.layout.Region; // Import Region
import javafx.util.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
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
    private Node selectedCard;

    // For tooltip display with delay
    private final Map<Node, Tooltip> cardTooltips = new HashMap<>();

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

    // Keep track of the original card node being dragged
    private Node draggedCardSource = null;

    /**
     * Creates a new controller instance and wires the shared singletons.
     */
    public GameScreenController() {
        super(new ResourceLoader(), UIEventBus.getInstance(), SceneManager.getInstance());
    }

    /**
     * Called automatically by the JavaFX loader. Sets up UI bindings, subscribes to
     * the event-bus, loads resources and prepares event handlers.
     */
    @FXML
    private void initialize() {
        LOGGER.setLevel(Level.ALL);
        this.localPlayer = GameApplication.getLocalPlayer(); // Fetch player instance
        this.currentLobbyId = GameApplication.getCurrentLobbyId(); // Fetch lobby ID
        if (this.localPlayer == null) {
            LOGGER.severe("LocalPlayer is null during MainMenuController initialization!");
            // Handle error appropriately, maybe show an error message and disable
            // functionality
            this.localPlayer = new Player("ErrorGuest"); // Fallback to avoid NullPointerExceptions
        }
        setupUI();
        subscribeEvents();
        loadMapImage();
        createAdjustmentUI(); // Create UI elements first
        // Instantiate GridAdjustmentManager after UI elements are created
        this.gridAdjustmentManager = new GridAdjustmentManager(this, adjustmentModeIndicator, adjustmentValuesLabel,
                this::drawMapAndGrid);
        setupCanvasListeners(); // Setup listeners after manager is created
        initialiseSettingsDialog();

        // --- ChatComponent setup ---
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
        chatComponentController.setCurrentLobbyId(currentLobbyId);
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
        eventBus.subscribe(NameChangeResponseEvent.class, this::handleNameChangeResponse);
        eventBus.subscribe(LobbyJoinedEvent.class, this::handleLobbyJoined);
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
     * Handles the "Settings" button click. Opens the SettingsDialog as an overlay.
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
        if (localPlayer != null) {
            settingsDialog.playerNameProperty().set(this.localPlayer.getName());
        } else {
            LOGGER.warning("Cannot set player name in settings: localPlayer is null.");
            settingsDialog.playerNameProperty().set("ErrorGuest");
        }

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

        showDialogAsOverlay(settingsDialog, root);
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

    @FXML
    private void handleResourceOverview() {
        // TODO: Implement resource overview logic
    }

    @FXML
    private void handleGameRound() {
        // TODO: Implement end turn logic
    }

    @FXML
    private void handleLeaderboard() {
        // TODO: Implement leaderboard logic
    }

    /**
     * Handles card click events - toggles selection with golden frame
     */
    @FXML
    public void handleCardClick(MouseEvent event) {
        Node clickedCard = (Node) event.getSource();

        // Toggle selection - if clicking already selected card, deselect it
        if (clickedCard == selectedCard) {
            clickedCard.getStyleClass().remove("selected-card");
            selectedCard = null;
        } else {
            // Deselect previous card if any
            if (selectedCard != null) {
                selectedCard.getStyleClass().remove("selected-card");
            }

            // Select new card
            clickedCard.getStyleClass().add("selected-card");
            selectedCard = clickedCard;
        }

        event.consume();
    }

    /**
     * Handles mouse entering a card - shows tooltip using JavaFX's built-in
     * mechanism
     */
    @FXML
    public void handleCardMouseEntered(MouseEvent event) {
        Node card = (Node) event.getSource();

        // Get or create tooltip for this card
        Tooltip tooltip = cardTooltips.computeIfAbsent(card, this::createTooltipForCard);

        // Install tooltip if not already installed
        Tooltip.install(card, tooltip);

        event.consume();
    }

    /**
     * Handles mouse exiting a card - hides tooltip
     */
    @FXML
    public void handleCardMouseExited(MouseEvent event) {
        Node card = (Node) event.getSource();

        // Uninstall tooltip
        Tooltip tooltip = cardTooltips.get(card);
        if (tooltip != null) {
            Tooltip.uninstall(card, tooltip);
        }

        event.consume();
    }

    /**
     * Handles the response from a player name change request.
     *
     * @param event The name change response event.
     */
    private void handleNameChangeResponse(NameChangeResponseEvent event) {
        Objects.requireNonNull(event, "NameChangeResponseEvent cannot be null");
        Platform.runLater(() -> {
            if (event.isSuccess()) {
                String newName = event.getNewName();
                // Update the central player instance
                if (localPlayer != null) {
                    localPlayer.setName(newName);
                    LOGGER.info("Player name successfully changed to: " + localPlayer.getName());
                    if (chatComponentController != null) {
                        chatComponentController.setPlayer(localPlayer); // Update chat component's player context
                        chatComponentController
                                .addSystemMessage("Name successfully changed to: " + localPlayer.getName());
                    }
                    settingsDialog.playerNameProperty().set(localPlayer.getName()); // Update settings dialog
                } else {
                    LOGGER.severe("Cannot update player name: localPlayer is null.");
                }
            } else {
                String failureMsg = event.getMessage() != null ? event.getMessage() : "Unknown reason.";
                LOGGER.warning("Failed to change player name: " + failureMsg);
                if (chatComponentController != null) {
                    chatComponentController.addSystemMessage("Failed to change name: " + failureMsg);
                }
                // Revert name in settings dialog if change failed
                if (localPlayer != null) {
                    settingsDialog.playerNameProperty().set(localPlayer.getName());
                }
            }
        });
    }

    /**
     * Handles the event when a player joins a lobby.
     *
     * @param event The lobby joined event.
     */
    private void handleLobbyJoined(LobbyJoinedEvent event) {
        Objects.requireNonNull(event, "LobbyJoinedEvent cannot be null");
        Platform.runLater(() -> {
            if (chatComponentController != null) {
                if (localPlayer != null) { // Ensure player is set before updating chat
                    chatComponentController.setPlayer(localPlayer);
                }
                chatComponentController.setCurrentLobbyId(currentLobbyId); // Set lobby ID for chat
                chatComponentController.addSystemMessage("You joined lobby: " + event.getLobbyName());
            }
        });
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
     *
     * @return A string with the current grid settings.
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
     * Initializes the SettingsDialog and adds it to the root pane.
     */
    private void initialiseSettingsDialog() {
        settingsDialog = new SettingsDialog();
        if (gameCanvas == null) {
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
        updateSettingsConnectionStatus();
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
            settingsDialog.playerNameProperty().set(localPlayer.getName()); // Revert in dialog
        }
        // Audio settings could be handled here if implemented
    }

    /**
     * Creates an appropriate tooltip for a card based on its type
     */
    private Tooltip createTooltipForCard(Node card) {
        Tooltip tooltip = new Tooltip();

        // Use JavaFX's built-in delay mechanism (500ms is standard)
        tooltip.setShowDelay(Duration.millis(500));
        tooltip.setHideDelay(Duration.millis(200)); // Quick hide when mouse exits

        String id = card.getId();
        String description = getCardDescription(id);

        tooltip.setText(description);
        tooltip.getStyleClass().add("card-tooltip");

        return tooltip;
    }

    /**
     * Gets card description based on its ID
     * In a real implementation, this would pull from your game data
     */
    private String getCardDescription(String cardId) {
        // Replace with actual card data from your game model
        if (cardId.startsWith("artifact")) {
            return "Artifact Card\n\nA powerful Norse artifact.\nEffect: Grants special abilities to the owner.";
        } else {
            return "Structure Card\n\nA building that can be placed on the board.\nCost: 5 Runes\nProvides: +2 Energy per turn";
        }
    }

    /**
     * Handles the start of a drag operation on a card.
     * Puts the card's ID onto the dragboard and sets a drag view.
     *
     * @param event The mouse event that triggered the drag detection.
     */
    @FXML
    private void handleCardDragDetected(MouseEvent event) {
        if (!(event.getSource() instanceof Pane sourcePane)) {
            return;
        }
        String cardId = sourcePane.getId();
        if (cardId == null || cardId.isEmpty()) {
            LOGGER.warning("Dragged card has no ID.");
            return;
        }

        // Only allow dragging structure cards for now
        if (!cardId.startsWith("structure")) {
            LOGGER.fine("Dragging non-structure card (" + cardId + ") is not implemented yet.");
            return;
        }

        LOGGER.fine("Drag detected on card: " + cardId);
        draggedCardSource = sourcePane; // Store the source node
        Dragboard db = sourcePane.startDragAndDrop(TransferMode.MOVE);

        ClipboardContent content = new ClipboardContent();
        content.putString(cardId); // Put the card's ID onto the dragboard
        db.setContent(content);

        // Create a snapshot of the card for the drag view
        SnapshotParameters params = new SnapshotParameters();
        params.setFill(Color.TRANSPARENT);
        WritableImage snapshot = sourcePane.snapshot(params, null);
        db.setDragView(snapshot, event.getX(), event.getY()); // Offset relative to cursor

        // Optionally hide the original card while dragging (can cause flicker, consider
        // alternatives)
        // sourcePane.setVisible(false);

        event.consume();
    }

    /**
     * Handles drag events over the game canvas.
     * Accepts the transfer if it contains a string (card ID) and the tile is valid.
     *
     * @param event The drag event.
     */
    @FXML
    private void handleDragOver(DragEvent event) {
        if (event.getGestureSource() != gameCanvas && event.getDragboard().hasString()) {
            // Check if the tile under the cursor is valid for dropping
            int[] targetTile = getHexAt(event.getX(), event.getY());
            // Check if the card being dragged is a structure card (or implement logic for
            // other types)
            String cardId = event.getDragboard().getString();
            boolean isStructureCard = cardId != null && cardId.startsWith("structure");

            if (targetTile != null && isStructureCard && isTileOwnedByPlayer(targetTile[0], targetTile[1])) {
                // Optional: Add visual feedback, e.g., highlight the hex
                // drawMapAndGrid(); // Redraw to potentially show highlight
                // drawHighlight(targetTile[0], targetTile[1], Color.GREEN); // Example
                // highlight
                event.acceptTransferModes(TransferMode.MOVE);
            } else {
                // Optional: Visual feedback for invalid tile (e.g., red highlight or default
                // cursor)
                // drawMapAndGrid(); // Redraw to clear previous highlight
                // drawHighlight(targetTile[0], targetTile[1], Color.RED); // Example highlight
                event.acceptTransferModes(TransferMode.NONE); // Indicate invalid drop target
            }
        }
        event.consume();
    }

    /**
     * Handles dropped events on the game canvas.
     * If a card was dropped, determines the target tile, checks ownership and
     * affordability,
     * and potentially triggers a game action by publishing an event.
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
                int row = targetTile[0];
                int col = targetTile[1];
                LOGGER.fine("Card " + cardId + " dropped on potential tile: row=" + row + ", col=" + col);

                // 1. Check Tile Ownership
                if (isTileOwnedByPlayer(row, col)) {
                    // 2. Check Affordability
                    if (canAffordCard(cardId)) {
                        LOGGER.info(
                                "Card " + cardId + " successfully placed on owned tile: row=" + row + ", col=" + col);

                        // 3. Publish event to place the structure/use the card
                        // Assuming cardId maps to a structureId for PlaceStructureUIEvent
                        try {
                            // Placeholder: Extract structure ID from card ID (e.g., "structure1" -> 1)
                            // Adjust parsing based on your actual card ID format
                            int structureId = Integer.parseInt(cardId.replace("structure", ""));
                            eventBus.publish(new PlaceStructureUIEvent(row, col, structureId));
                            success = true; // Mark drop as successful
                        } catch (NumberFormatException | IndexOutOfBoundsException e) {
                            LOGGER.severe("Could not parse structure ID from card ID: " + cardId);
                            if (chatComponentController != null) {
                                chatComponentController.addSystemMessage("Error placing card: Invalid card data.");
                            }
                        }

                    } else {
                        LOGGER.warning("Cannot place card " + cardId + ": Not enough resources (Cost: "
                                + getCardCost(cardId) + ", Have: " + getPlayerRunes() + ")");
                        if (chatComponentController != null) {
                            chatComponentController.addSystemMessage("Cannot place card: Not enough runes.");
                        }
                    }
                } else {
                    LOGGER.warning("Cannot place card " + cardId + ": Tile (row=" + row + ", col=" + col
                            + ") is not owned by the player.");
                    if (chatComponentController != null) {
                        chatComponentController.addSystemMessage("Cannot place card: You do not own this tile.");
                    }
                }
            } else {
                LOGGER.fine("Card " + cardId + " dropped outside of any valid tile.");
            }
        }
        // Complete the drag-and-drop gesture
        event.setDropCompleted(success);
        event.consume();
    }

    /**
     * Handles the completion of a drag-and-drop operation originating from a card.
     * If the drop was successful (MOVE transfer mode), remove the card from the
     * hand.
     * If it failed, make the original card visible again (if it was hidden).
     *
     * @param event The drag event.
     */
    @FXML
    private void handleCardDragDone(DragEvent event) {
        LOGGER.fine("Drag done for card. Transfer mode: " + event.getTransferMode());
        if (draggedCardSource != null) {
            if (event.getTransferMode() == TransferMode.MOVE) {
                // Drop was successful, remove card from hand visually
                Pane parentPane = (Pane) draggedCardSource.getParent();
                if (parentPane != null) {
                    parentPane.getChildren().remove(draggedCardSource);
                    LOGGER.fine("Removed successfully placed card (" + draggedCardSource.getId() + ") from hand.");
                    // TODO: Update game state model to reflect card removal from hand
                } else {
                    LOGGER.warning("Could not find parent pane to remove dragged card from.");
                }
            } else {
                // Drop failed or was cancelled, make original card visible again if it was
                // hidden
                // draggedCardSource.setVisible(true); // Uncomment if you hide the card during
                // drag
                LOGGER.fine("Card drop failed or cancelled, card (" + draggedCardSource.getId() + ") remains in hand.");
            }
            draggedCardSource = null; // Reset the tracked source
        }
        event.consume();
    }

    // --- Placeholder Methods for Game State ---

    /**
     * Placeholder: Checks if the tile at the given coordinates is owned by the
     * local player.
     * Replace with actual game state logic.
     *
     * @param row The tile row.
     * @param col The tile column.
     * @return True if the player owns the tile, false otherwise.
     */
    private boolean isTileOwnedByPlayer(int row, int col) {
        // TODO: Implement actual check against GameStateManager or similar
        LOGGER.finer("Placeholder check: Is tile (" + row + "," + col + ") owned? Returning true for now.");
        // Example: return GameStateManager.getInstance().getTile(row,
        // col).getOwnerId().equals(localPlayer.getId());
        return true; // Placeholder
    }

    /**
     * Placeholder: Checks if the player can afford the card with the given ID.
     * Replace with actual game state logic.
     *
     * @param cardId The ID of the card.
     * @return True if the player has enough resources, false otherwise.
     */
    private boolean canAffordCard(String cardId) {
        // TODO: Implement actual check against player resources and card cost
        int cost = getCardCost(cardId);
        int playerRunes = getPlayerRunes();
        boolean canAfford = playerRunes >= cost;
        LOGGER.finer("Placeholder check: Can afford card " + cardId + "? Cost=" + cost + ", Have=" + playerRunes
                + " -> " + canAfford);
        // Example: return
        // GameStateManager.getInstance().getLocalPlayerState().getRunes() >=
        // CardDatabase.getCard(cardId).getCost();
        return canAfford; // Placeholder
    }

    /**
     * Placeholder: Gets the cost of the card with the given ID.
     * Replace with actual game data lookup.
     *
     * @param cardId The ID of the card.
     * @return The cost of the card (e.g., in runes).
     */
    private int getCardCost(String cardId) {
        // TODO: Implement actual lookup for card cost from game data/model
        // Example: return CardDatabase.getCard(cardId).getCost();
        if (cardId != null && cardId.startsWith("structure")) {
            return 5; // Placeholder cost for structure cards
        }
        return 10; // Placeholder cost for other cards (e.g., artifacts if implemented)
    }

    /**
     * Placeholder: Gets the current amount of runes (or relevant currency) the
     * local player has.
     * Replace with actual game state logic.
     *
     * @return The player's current rune count.
     */
    private int getPlayerRunes() {
        // TODO: Implement actual check against GameStateManager or PlayerState object
        // Example: return
        // GameStateManager.getInstance().getLocalPlayerState().getRunes();
        try {
            // Attempt to read from the label if it's updated elsewhere
            return Integer.parseInt(runesLabel.getText());
        } catch (NumberFormatException e) {
            LOGGER.warning("Could not parse runes from label, returning placeholder value.");
            return 100; // Placeholder value if label parsing fails
        }
    }

    // --- End Placeholder Methods ---

    /**
     * Returns the game canvas used for drawing the map and grid.
     * Needed by GridAdjustmentManager.
     *
     * @return The game canvas.
     */
    public Canvas getGameCanvas() {
        return gameCanvas;
    }
}
