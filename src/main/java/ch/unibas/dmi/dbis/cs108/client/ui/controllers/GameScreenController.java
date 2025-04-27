package ch.unibas.dmi.dbis.cs108.client.ui.controllers;

import ch.unibas.dmi.dbis.cs108.SETTINGS;
import ch.unibas.dmi.dbis.cs108.client.app.GameApplication;
import ch.unibas.dmi.dbis.cs108.client.core.PlayerIdentityManager;
import ch.unibas.dmi.dbis.cs108.client.core.state.GameState;
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
import ch.unibas.dmi.dbis.cs108.client.ui.utils.CardDetails;
import ch.unibas.dmi.dbis.cs108.client.ui.utils.ResourceLoader;
import ch.unibas.dmi.dbis.cs108.shared.entities.EntityRegistry;
import ch.unibas.dmi.dbis.cs108.shared.entities.Findables.Artifact;
import ch.unibas.dmi.dbis.cs108.shared.entities.GameEntity;
import ch.unibas.dmi.dbis.cs108.shared.entities.Purchasables.Statues.Statue;
import ch.unibas.dmi.dbis.cs108.shared.entities.Purchasables.Structure;
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
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;
import javafx.util.Duration;

import java.util.*;
import java.util.LinkedHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Controller for the in-game screen of the application.
 * Handles rendering of the game board, user interactions such as drag-and-drop
 * for game pieces,
 * and communication with the game logic through the UIEventBus.
 */
public class GameScreenController extends BaseController {

    public static final DataFormat CARD_DATA_FORMAT = new DataFormat("application/x-settlers-card");
    private static final Logger LOGGER = Logger.getLogger(GameScreenController.class.getName());
    private static final int HEX_ROWS = 6;
    private static final int HEX_COLS = 7;
    private static final int MAX_STRUCTURES = 5;
    private static final int MAX_ARTIFACTS = 3;
    private static final int MAX_STATUES = 1;
    private final ObservableList<String> players = FXCollections.observableArrayList();
    private final Map<Node, Tooltip> cardTooltips = new HashMap<>();
    private final Map<Integer, Image> entityImageCache = new HashMap<>();
    private final Map<String, Color> playerColors = new HashMap<>();
    private PlayerIdentityManager playerManager;
    private Player localPlayer;
    private Player gamePlayer;
    private GameState gameState;
    private List<Artifact> artifactsInHand = new ArrayList<>();
    private List<Color> playerColours;
    private double scaledMapWidth;
    private double scaledMapHeight;
    private double mapOffsetX;
    private double mapOffsetY;
    private double effectiveHexSize;
    private double gridOffsetX;
    private double gridOffsetY;
    private int selectedRow = -1;
    private int selectedCol = -1;
    private String currentLobbyId;
    private Image mapImage;
    private boolean isMapLoaded;
    private Image placeholderImage;
    private SettingsDialog settingsDialog;
    private Node selectedCard;
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
    private HBox statueHand;
    @FXML
    private Label connectionStatusLabel;
    @FXML
    private VBox chatContainer;
    @FXML
    private StackPane gameBoardContainer;
    @FXML
    private Pane artifact1;
    @FXML
    private Pane artifact2;
    @FXML
    private Pane artifact3;
    @FXML
    private Pane structure1;
    @FXML
    private Pane structure2;
    @FXML
    private Pane structure3;
    @FXML
    private Pane structure4;
    @FXML
    private Pane structure5;
    @FXML
    private Pane statueCard;
    private ChatComponent chatComponentController;
    private Label adjustmentModeIndicator;
    private Label adjustmentValuesLabel;
    private Node draggedCardSource;

    /**
     * Constructs a new GameScreenController.
     * Initializes the player manager, local player, and subscribes to necessary
     * events.
     */
    public GameScreenController() {
        super(new ResourceLoader(), UIEventBus.getInstance(), SceneManager.getInstance());
        playerManager = PlayerIdentityManager.getInstance();
        localPlayer = playerManager.getLocalPlayer();
        if (localPlayer == null) {
            LOGGER.severe("LocalPlayer is null during construction! Using fallback.");
            localPlayer = new Player("ErrorGuest-" + UUID.randomUUID().toString().substring(0, 4));
            PlayerIdentityManager.getInstance().setLocalPlayer(localPlayer);
        }
        playerManager.addPlayerUpdateListener(this::handlePlayerUpdate);
        LOGGER.info("GameScreenController created for player: " + localPlayer.getName());
        gameState = new GameState();
        subscribeEvents();
        LOGGER.info("GameScreenController subscribed to events.");
    }

    /**
     * Initializes the controller after its root element has been completely
     * processed.
     * Sets up the UI components, loads the map image, and prepares the game board.
     */
    @FXML
    private void initialize() {
        LOGGER.setLevel(Level.FINE);
        LOGGER.info("GameScreenController initialisation started for player: " +
                (localPlayer != null ? localPlayer.getName() : "null"));

        if (localPlayer == null) {
            LOGGER.severe("LocalPlayer is STILL null during initialize!");
            localPlayer = PlayerIdentityManager.getInstance().getLocalPlayer();
            if (localPlayer == null) {
                localPlayer = new Player("CriticalErrorGuest");
                LOGGER.severe("Critical Error: Could not establish local player identity.");
            }
        }

        currentLobbyId = GameApplication.getCurrentLobbyId();
        setupUI();
        loadMapImage();
        createPlaceholderImage();
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
        updateCardDisplay();
        LOGGER.info("GameScreenController initialized successfully.");
    }

    /**
     * Sets up the user interface components by binding them to their respective
     * properties
     * and setting initial values.
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
     * Subscribes to various events on the UIEventBus to handle game state changes,
     * player actions, and other game-related events.
     */
    private void subscribeEvents() {
        eventBus.subscribe(ConnectionStatusEvent.class, this::onConnectionStatus);
        eventBus.subscribe(NameChangeResponseEvent.class, this::handleNameChangeResponse);
        eventBus.subscribe(LobbyJoinedEvent.class, this::handleLobbyJoined);
        eventBus.subscribe(TileClickEvent.class, this::onTileClick);
        eventBus.subscribe(GameSyncEvent.class, event -> {
            LOGGER.fine("GameSyncEvent received: " + (event == null ? "null" : "valid"));
            if (event != null && event.getGameState() != null) {
                handleGameSync(event);
            } else {
                LOGGER.warning("Received null or invalid GameSyncEvent");
            }
        });
    }

    /**
     * Initializes the chat component, sets the current player and lobby,
     * and adds it to the chat container in the UI.
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
     * Initializes the player colors used for visualizing player ownership on the
     * game board.
     */
    private void initialisePlayerColours() {
        playerColours = new ArrayList<>();
        playerColours.add(Color.RED);
        playerColours.add(Color.BLUE);
        playerColours.add(Color.GREEN);
        playerColours.add(Color.YELLOW);
        playerColours.add(Color.PURPLE);
        playerColours.add(Color.ORANGE);
        playerColours.add(Color.CYAN);
        playerColours.add(Color.MAGENTA);

        for (String playerName : GameApplication.getPlayers()) {
            Color color = playerColours.remove(0);
            playerColors.put(playerName, color);
        }
    }

    /**
     * Creates the UI elements for grid adjustment mode, including indicators and
     * labels.
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

    /**
     * Handles connection status events by updating the UI and displaying messages.
     *
     * @param e the ConnectionStatusEvent
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

    /**
     * Handles tile click events by logging the event.
     *
     * @param e the TileClickEvent
     */
    private void onTileClick(TileClickEvent e) {
        LOGGER.fine(() -> String.format("Tile clicked externally (row=%d,col=%d)", e.getRow(), e.getCol()));
    }

    /**
     * Updates the local player reference and refreshes the UI when the player data
     * changes.
     *
     * @param updatedPlayer the updated Player object
     */
    private void handlePlayerUpdate(Player updatedPlayer) {
        localPlayer = updatedPlayer;
        if (chatComponentController != null) {
            chatComponentController.setPlayer(localPlayer);
        }
        LOGGER.info("Player updated in GameScreenController: " + localPlayer.getName());
    }

    /**
     * Handles game synchronization events by updating the game state and refreshing
     * the UI.
     *
     * @param e the GameSyncEvent
     */
    private void handleGameSync(GameSyncEvent e) {
        LOGGER.fine("Handling GameSyncEvent...");
        if (e == null || e.getGameState() == null) {
            LOGGER.warning("Cannot handle null GameSyncEvent or GameState.");
            return;
        }
        this.gameState = e.getGameState();
        LOGGER.fine("GameState updated from sync event.");

        if (localPlayer != null) {
            this.gamePlayer = gameState.findPlayerByName(localPlayer.getName());
            if (this.gamePlayer == null) {
                LOGGER.warning("Local player '" + localPlayer.getName() + "' not found in synced GameState!");
            } else {
                LOGGER.fine("Found gamePlayer instance for '" + localPlayer.getName() + "' in GameState.");
                this.artifactsInHand = new ArrayList<>(gamePlayer.getArtifacts());
            }
        } else {
            LOGGER.severe("LocalPlayer reference is null when handling GameSyncEvent!");
            this.gamePlayer = null;
        }

        Platform.runLater(() -> {
            LOGGER.fine("Updating UI elements after GameSync...");
            updateRunesAndEnergyBar();
            updatePlayerList();
            updateCardDisplay();
            drawMapAndGrid();
            LOGGER.fine("UI update complete.");
        });
    }

    /**
     * Handles the action to go back to the main menu.
     */
    @FXML
    private void handleBackToMainMenu() {
        sceneManager.switchToScene(SceneManager.SceneType.MAIN_MENU);
    }

    /**
     * Handles the settings button click by showing the settings dialog.
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
            LOGGER.info("Settings dialog save requested – Volume: " + volume + ", Muted: " + muted +
                    ", Requested Name: " + requested);

            if (requested != null && !requested.trim().isEmpty() && !requested.equals(localPlayer.getName())) {
                requestNameChange(requested.trim());
            } else if (requested != null && requested.trim().isEmpty()) {
                chatComponentController.addSystemMessage("Error: Player name cannot be empty.");
                settingsDialog.playerNameProperty().set(localPlayer.getName());
            }
            chatComponentController.addSystemMessage(
                    "Audio settings saved. " + (muted ? "Muted." : "Volume: " + (int) volume + "%"));
        });

        showDialogAsOverlay(settingsDialog, root);
    }

    /**
     * Requests a name change for the player by publishing a ChangeNameUIEvent.
     *
     * @param newName the new name requested by the player
     */
    private void requestNameChange(String newName) {
        LOGGER.info("Requesting name change to: " + newName);
        chatComponentController.addSystemMessage("Requesting name change to: " + newName + "...");
        eventBus.publish(new ChangeNameUIEvent(newName));
    }

    /**
     * Handles the response to a name change request, updating the UI and player
     * data accordingly.
     *
     * @param event the NameChangeResponseEvent
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
     * Displays a system message when the local player joins a lobby.
     *
     * @param event the LobbyJoinedEvent
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
     * Updates the lobby identifier for the controller and the chat component.
     *
     * @param lobbyId the new lobby identifier
     */
    public void setCurrentLobbyId(String lobbyId) {
        this.currentLobbyId = lobbyId;
        chatComponentController.setCurrentLobbyId(lobbyId);
    }

    /**
     * Updates the local player reference and synchronizes the game player
     * reference.
     *
     * @param player the new local player
     */
    public void setLocalPlayer(Player player) {
        this.localPlayer = player;
        if (player != null) {
            LOGGER.info("LocalPlayer set to: " + player.getName());
            if (gameState != null) {
                this.gamePlayer = gameState.findPlayerByName(player.getName());
                if (this.gamePlayer == null) {
                    LOGGER.warning("Updated localPlayer '" + player.getName() + "' not found in current GameState.");
                } else {
                    this.artifactsInHand = new ArrayList<>(gamePlayer.getArtifacts());
                }
            }
            if (chatComponentController != null) {
                chatComponentController.setPlayer(player);
            }
            Platform.runLater(() -> {
                if (settingsDialog != null) {
                    settingsDialog.playerNameProperty().set(player.getName());
                }
                updateCardDisplay();
            });
        } else {
            LOGGER.warning("LocalPlayer set to null.");
            this.gamePlayer = null;
        }
    }

    /**
     * Cleans up resources and unsubscribes from events when the controller is
     * disposed.
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
        if (cardTooltips != null) {
            for (Tooltip tooltip : cardTooltips.values()) {
                Tooltip.uninstall(tooltip.getOwnerNode(), tooltip);
            }
            cardTooltips.clear();
        }
        LOGGER.info("GameScreenController resources cleaned up");
    }

    /**
     * Toggles the grid adjustment mode.
     */
    public void toggleGridAdjustmentMode() {
        gridAdjustmentManager.toggleGridAdjustmentMode();
    }

    /**
     * Enables or disables the grid adjustment mode.
     *
     * @param active true to enable, false to disable
     */
    public void setGridAdjustmentMode(boolean active) {
        gridAdjustmentManager.setGridAdjustmentMode(active);
    }

    /**
     * Retrieves a human-readable description of the current grid parameters.
     *
     * @return the grid settings as a string
     */
    public String getGridSettings() {
        return gridAdjustmentManager.getGridSettings();
    }

    /**
     * Handles global keyboard shortcuts for grid adjustment.
     *
     * @param e the KeyEvent to process
     */
    @FXML
    private void handleKeyboardShortcut(KeyEvent e) {
        if (gridAdjustmentManager.handleKeyboardShortcut(e)) {
            e.consume();
        }
    }

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
     * Sets up listeners for canvas size changes and input events.
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
     * Handles a mouse click on the canvas, selecting tiles or initiating purchases.
     *
     * @param px the x-coordinate of the click
     * @param py the y-coordinate of the click
     */
    private void handleCanvasClick(double px, double py) {
        if (gridAdjustmentManager.isGridAdjustmentModeActive()) {
            gridAdjustmentManager.handleCanvasClick(px, py);
            return;
        }

        int[] tileCoords = getHexAt(px, py);
        if (tileCoords == null) {
            selectedRow = -1;
            selectedCol = -1;
            drawMapAndGrid(); // Redraw to deselect
            return;
        }

        int row = tileCoords[0];
        int col = tileCoords[1];

        if (selectedRow != row || selectedCol != col) {
            selectedRow = row;
            selectedCol = col;
            drawMapAndGrid(); // Redraw to show selection
            eventBus.publish(new TileClickEvent(row, col));
            LOGGER.fine("Tile selected: (" + row + ", " + col + ")");
        }

        // Tile Purchase Logic
        Tile clickedTile = getTile(row, col); // Use helper which accesses BoardManager correctly
        if (clickedTile != null && clickedTile.getOwner() == null && !clickedTile.hasEntity()) {
            int price = clickedTile.getPrice();
            Alert alert = new Alert(AlertType.CONFIRMATION);
            alert.setTitle("Confirm Purchase");
            alert.setHeaderText("Purchase Tile (" + row + ", " + col + ")");
            alert.setContentText("Buy this tile for " + price + " runes?");
            alert.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO);

            alert.showAndWait().ifPresent(result -> {
                if (result == ButtonType.YES) {
                    if (canAffordTile(price)) {
                        LOGGER.info("Player confirmed purchase for tile (" + row + ", " + col
                                + "). Publishing BuyTileUIEvent.");
                        // Publish event with UI coordinates (row, col)
                        eventBus.publish(new BuyTileUIEvent(row, col));
                    } else {
                        LOGGER.warning("Player cannot afford tile (" + row + ", " + col + "). Cost: " + price);
                        showNotification("Not enough runes to buy this tile (Cost: " + price + ").");
                    }
                }
            });
        } else if (clickedTile != null) {
            String ownerName = clickedTile.getOwner();
            if (ownerName != null) {
                if (localPlayer != null && ownerName.equals(localPlayer.getName())) {
                    if (clickedTile.hasEntity()) {
                        showNotification("You own this tile with a " + clickedTile.getEntity().getName() + " on it.");
                    } else {
                        showNotification("You own this tile.");
                    }
                } else {
                    showNotification("This tile is owned by " + ownerName + ".");
                }
            } else if (clickedTile.hasEntity()) { // Unowned but has entity (shouldn't happen?)
                showNotification("This tile contains a " + clickedTile.getEntity().getName() + ".");
            }
        }
    }

    /**
     * Draws the map, hex grid, and placed entities on the canvas.
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

        double gridW = scaledMapWidth * gridAdjustmentManager.getGridWidthPercentage();
        double gridH = scaledMapHeight * gridAdjustmentManager.getGridHeightPercentage();
        double hHexLimit = gridW / ((HEX_COLS - 1) * 0.75 + 1);
        double vHexLimit = gridH / ((HEX_ROWS - 0.5) * Math.sqrt(3.0) / 2.0 * 2.0);
        effectiveHexSize = Math.min(hHexLimit, vHexLimit) * 0.5 * gridAdjustmentManager.getGridScaleFactor();

        if (effectiveHexSize <= 0) {
            LOGGER.warning("Calculated effectiveHexSize is zero or negative. Skipping grid draw.");
            return;
        }

        double addHX = gridAdjustmentManager.getGridHorizontalOffset() * scaledMapWidth;
        double addHY = gridAdjustmentManager.getGridVerticalOffset() * scaledMapHeight;

        drawHexGrid(gc, effectiveHexSize, gridW, gridH, addHX, addHY);
        drawPlacedEntities(gc, effectiveHexSize);

        boolean active = gridAdjustmentManager.isGridAdjustmentModeActive();
        adjustmentModeIndicator.setVisible(active);
        adjustmentValuesLabel.setVisible(active);
    }

    /**
     * Draws the hexagonal grid with ownership and selection highlights.
     *
     * @param gc    the GraphicsContext to draw on
     * @param size  the size of each hexagon
     * @param gridW the width of the grid
     * @param gridH the height of the grid
     * @param addHX additional horizontal offset
     * @param addHY additional vertical offset
     */
    private void drawHexGrid(GraphicsContext gc, double size, double gridW, double gridH, double addHX, double addHY) {
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
     * Draws a single hexagon with ownership and selection highlights.
     *
     * @param gc       the GraphicsContext to draw on
     * @param cx       the x-coordinate of the hexagon center
     * @param cy       the y-coordinate of the hexagon center
     * @param size     the size of the hexagon
     * @param row      the row index
     * @param col      the column index
     * @param selected whether the hexagon is selected
     */
    private void drawHex(GraphicsContext gc, double cx, double cy, double size, int row, int col, boolean selected) {
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

        // Use getTileOwnerName which exists and returns the name string
        String ownerName = getTileOwnerName(row, col);
        Color ownerCol = getPlayerColor(ownerName); // Use the implemented getPlayerColor
        if (ownerCol != null) {
            Paint oldFill = gc.getFill();
            double oldAlpha = gc.getGlobalAlpha();
            gc.setFill(ownerCol);
            gc.setGlobalAlpha(0.4);
            gc.fill();
            gc.setFill(oldFill);
            gc.setGlobalAlpha(oldAlpha);
        }

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

    /**
     * Draws entities placed on the game board.
     * Corrected to use (col, row) when accessing BoardManager.
     * Refined image check to handle null explicitly.
     *
     * @param gc      the GraphicsContext to draw on
     * @param hexSize the size of each hexagon
     */
    private void drawPlacedEntities(GraphicsContext gc, double hexSize) {
        if (gameState == null || gameState.getBoardManager() == null)
            return;

        double hSpacing = hexSize * gridAdjustmentManager.getHorizontalSpacingFactor();
        double vSpacing = hexSize * gridAdjustmentManager.getVerticalSpacingFactor();

        for (int r = 0; r < HEX_ROWS; r++) {
            for (int c = 0; c < HEX_COLS; c++) {
                // Access BoardManager using (col, row)
                Tile tile = gameState.getBoardManager().getTile(c, r);
                if (tile != null && tile.hasEntity() && tile.getEntity() != null) {
                    GameEntity entity = tile.getEntity();
                    int entityId = entity.getId();
                    Image entityImage = getEntityImage(entityId); // Uses cache and placeholder logic

                    // Refined check: Handle null image explicitly along with placeholder
                    if (entityImage != null && entityImage != placeholderImage && !entityImage.isError()) {
                        // Calculate center based on UI's (row, col)
                        double cx = gridOffsetX + c * hSpacing + (r % 2) * (hSpacing / 2);
                        double cy = gridOffsetY + r * vSpacing;
                        // Scale image to fit hex size reasonably (adjust multiplier as needed)
                        double imgDrawWidth = hexSize * 1.5 * gridAdjustmentManager.getHorizontalSquishFactor();
                        // Handle potential division by zero if image width is 0
                        double imgDrawHeight = (entityImage.getWidth() > 0)
                                ? entityImage.getHeight() * (imgDrawWidth / entityImage.getWidth())
                                : hexSize * 1.5 * gridAdjustmentManager.getVerticalSquishFactor(); // Fallback height
                        // Center the image
                        double drawX = cx - imgDrawWidth / 2;
                        double drawY = cy - imgDrawHeight / 2;
                        gc.drawImage(entityImage, drawX, drawY, imgDrawWidth, imgDrawHeight);
                    } else {
                        // Log which case happened (null, placeholder, or error)
                        String reason = (entityImage == null) ? "null image"
                                : (entityImage == placeholderImage) ? "placeholder image" : "error image";
                        LOGGER.fine("Drawing fallback for entity ID: " + entityId + " on tile (" + r + "," + c
                                + ") because of " + reason);
                        // Draw a visual indicator even for placeholder/failed load
                        double cx = gridOffsetX + c * hSpacing + (r % 2) * (hSpacing / 2);
                        double cy = gridOffsetY + r * vSpacing;
                        gc.setFill(Color.DARKRED); // Use a different color for placeholder indication
                        gc.fillOval(cx - hexSize * 0.2, cy - hexSize * 0.2, hexSize * 0.4, hexSize * 0.4);
                    }
                }
            }
        }
    }

    /**
     * Retrieves or loads the image for an entity, caching it for future use.
     *
     * @param entityId the ID of the entity
     * @return the Image for the entity, or placeholder if unavailable
     */
    private Image getEntityImage(int entityId) {
        return entityImageCache.computeIfAbsent(entityId, id -> {
            String imageUrl = EntityRegistry.getURL(id, false);
            if (imageUrl != null && !imageUrl.isEmpty()) {
                Image img = resourceLoader.loadImage(imageUrl);
                if (img != null && !img.isError()) {
                    return img;
                } else {
                    LOGGER.warning("Failed to load image for entity ID: " + id + " from URL: " + imageUrl);
                    return placeholderImage;
                }
            } else {
                LOGGER.warning("No image URL for entity ID: " + id);
                return placeholderImage;
            }
        });
    }

    /**
     * Converts canvas coordinates to grid coordinates.
     *
     * @param px the x-coordinate on the canvas
     * @param py the y-coordinate on the canvas
     * @return an array of [row, col] or null if outside the grid
     */
    private int[] getHexAt(double px, double py) {
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
     * Tests if a point lies within a hexagon.
     *
     * @param px   the x-coordinate of the point
     * @param py   the y-coordinate of the point
     * @param cx   the x-coordinate of the hexagon center
     * @param cy   the y-coordinate of the hexagon center
     * @param size the size of the hexagon
     * @return true if the point is inside the hexagon, false otherwise
     */
    private boolean pointInHex(double px, double py, double cx, double cy, double size) {
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
     * Initializes the settings dialog and sets up its bindings.
     */
    private void initialiseSettingsDialog() {
        settingsDialog = new SettingsDialog();
        settingsDialog.playerNameProperty().set(localPlayer.getName());
        settingsDialog.setOnSaveAction(this::handleSettingsSave);
        updateSettingsConnectionStatus();
    }

    /**
     * Updates the connection status displayed in the settings dialog.
     */
    private void updateSettingsConnectionStatus() {
        String status = connectionStatusLabel.getText();
        settingsDialog.setConnectionStatus("Connected".equals(status), status);
    }

    /**
     * Handles the save action in the settings dialog.
     */
    private void handleSettingsSave() {
        String newName = settingsDialog.playerNameProperty().get().trim();
        if (!newName.isEmpty() && !newName.equals(localPlayer.getName())) {
            eventBus.publish(new NameChangeRequestEvent(newName));
        } else if (newName.isEmpty()) {
            settingsDialog.playerNameProperty().set(localPlayer.getName());
        }
    }

    /**
     * Handles the end turn action by publishing an EndTurnRequestEvent.
     * This allows the current player to end their turn.
     */
    @FXML
    private void handleEndTurn() {
        LOGGER.info("End turn button clicked");
        if (localPlayer == null) {
            LOGGER.warning("Cannot end turn: Local player is null");
            showNotification("Error: Cannot end turn due to missing player identity.");
            return;
        }

        if (gameState == null || !localPlayer.getName().equals(gameState.getPlayerTurn())) {
            LOGGER.fine("End turn not allowed: Not player's turn");
            showNotification("It's not your turn.");
            return;
        }

        LOGGER.info("Publishing EndTurnRequestEvent for player: " + localPlayer.getName());
        eventBus.publish(new EndTurnRequestEvent(localPlayer.getName()));
        showNotification("Turn ended. Waiting for next player...");
    }

    /**
     * Handles a card click event to toggle its selection state.
     *
     * @param event the MouseEvent triggered by the click
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
     * Displays a tooltip when the mouse enters a card.
     *
     * @param event the MouseEvent triggered by mouse entry
     */
    @FXML
    public void handleCardMouseEntered(MouseEvent event) {
        Node card = (Node) event.getSource();
        Tooltip tooltip = cardTooltips.computeIfAbsent(card, this::createTooltipForCard);
        Tooltip.install(card, tooltip);
        event.consume();
    }

    /**
     * Hides the tooltip when the mouse exits a card.
     *
     * @param event the MouseEvent triggered by mouse exit
     */
    @FXML
    public void handleCardMouseExited(MouseEvent event) {
        Node card = (Node) event.getSource();
        Tooltip tip = cardTooltips.get(card);
        if (tip != null)
            Tooltip.uninstall(card, tip);
        event.consume();
    }

    /**
     * Creates a Tooltip for a given card Node.
     *
     * @param card the Node representing the card
     * @return a configured Tooltip instance
     */
    private Tooltip createTooltipForCard(Node card) {
        CardDetails details = getCardDetails(card.getId());
        Tooltip tooltip = new Tooltip();
        tooltip.setShowDelay(Duration.millis(200));
        tooltip.setHideDelay(Duration.millis(100));

        VBox tooltipContent = new VBox(5);
        tooltipContent.setPadding(new Insets(5));

        Label titleLabel = new Label(details.getTitle());
        titleLabel.setStyle("-fx-font-weight: bold;");

        Label costLabel = new Label("Cost: " + details.getPrice() + " Runes");

        Label descriptionLabel = new Label(details.getDescription());
        descriptionLabel.setWrapText(true);
        descriptionLabel.setMaxWidth(200); // Limit width to prevent overly large tooltips

        tooltipContent.getChildren().addAll(titleLabel, costLabel, descriptionLabel);

        if (details.getLore() != null && !details.getLore().isEmpty()) {
            Label loreLabel = new Label("Lore: " + details.getLore());
            loreLabel.setWrapText(true);
            loreLabel.setMaxWidth(200);
            loreLabel.setStyle("-fx-font-style: italic;");
            tooltipContent.getChildren().add(loreLabel);
        }

        tooltip.setGraphic(tooltipContent);
        return tooltip;
    }

    /**
     * Initiates a drag-and-drop operation for a card.
     *
     * @param event the MouseEvent triggered by drag detection
     */
    @FXML
    private void handleCardDragDetected(MouseEvent event) {
        if (!(event.getSource() instanceof Pane cardPane)) {
            LOGGER.warning("Drag detected on non-Pane source.");
            return;
        }

        CardInfo cardInfo = getCardInfo(cardPane);
        if (cardInfo.type == CardType.UNKNOWN) {
            LOGGER.warning("Drag detected on card with unknown type or invalid ID: " + cardPane.getId());
            return;
        }

        if (!canAffordCard(cardInfo.id)) {
            LOGGER.fine("Cannot afford card " + cardInfo.id + ". Drag cancelled.");
            showNotification("You cannot afford this card (Cost: " + getCardCost(cardInfo.id) + ").");
            event.consume();
            return;
        }

        if (gameState == null || localPlayer == null || !localPlayer.getName().equals(gameState.getPlayerTurn())) {
            showNotification("It's not your turn!");
            event.consume();
            return;
        }

        draggedCardSource = cardPane;
        Dragboard db = cardPane.startDragAndDrop(TransferMode.MOVE);
        ClipboardContent content = new ClipboardContent();
        Map<DataFormat, Object> cardData = new HashMap<>();
        cardData.put(CARD_DATA_FORMAT, cardInfo.type.name() + ":" + cardInfo.id);
        content.putAll(cardData);
        db.setContent(content);

        SnapshotParameters params = new SnapshotParameters();
        params.setFill(Color.TRANSPARENT);
        WritableImage snapshot = cardPane.snapshot(params, null);
        db.setDragView(snapshot, event.getX(), event.getY());

        LOGGER.fine("Drag started for card type: " + cardInfo.type + ", ID: " + cardInfo.id);
        event.consume();
    }

    /**
     * Handles the drag-over event when a card is dragged over the canvas.
     * Accepts the transfer only if the drop is potentially valid.
     *
     * @param event the DragEvent
     */
    @FXML
    private void handleDragOver(DragEvent event) {
        LOGGER.finest("handleDragOver triggered."); // Log entry

        // Check 1: Is the source the card being dragged?
        if (event.getGestureSource() != draggedCardSource) {
            LOGGER.finest("DragOver rejected: Gesture source mismatch.");
            event.acceptTransferModes(TransferMode.NONE);
            event.consume();
            return;
        }

        // Check 2: Does the dragboard have the correct data format?
        Dragboard db = event.getDragboard();
        if (!db.hasContent(CARD_DATA_FORMAT)) {
            LOGGER.finest("DragOver rejected: No card data format found.");
            event.acceptTransferModes(TransferMode.NONE);
            event.consume();
            return;
        }

        // Check 3: Get card info and target coordinates
        CardInfo cardInfo = getCardInfoFromDragboard(db);
        int[] targetCoords = getHexAt(event.getX(), event.getY());

        // Check 4: Are coordinates and card info valid?
        if (targetCoords == null || cardInfo.type == CardType.UNKNOWN) {
            LOGGER.finest("DragOver rejected: Invalid target coordinates or unknown card type.");
            event.acceptTransferModes(TransferMode.NONE);
            event.consume();
            return;
        }

        // Check 5: Validate the potential placement
        int row = targetCoords[0];
        int col = targetCoords[1];
        ValidationResult validation = validatePlacement(cardInfo, row, col);
        LOGGER.finest("DragOver validation for card " + cardInfo.id + " at (" + row + "," + col + "): " + validation);

        if (validation.isValid()) {
            LOGGER.finest("DragOver accepting MOVE transfer.");
            event.acceptTransferModes(TransferMode.MOVE);
        } else {
            LOGGER.finest("DragOver rejecting transfer due to validation failure.");
            event.acceptTransferModes(TransferMode.NONE);
        }
        event.consume(); // Consume the event regardless of acceptance
    }

    /**
     * Handles the drop event when a card is dropped onto the canvas.
     * Validates the drop and publishes the appropriate UI event if valid.
     *
     * @param event the DragEvent
     */
    @FXML
    private void handleDragDropped(DragEvent event) {
        LOGGER.fine("handleDragDropped triggered."); // Log entry
        Dragboard db = event.getDragboard();
        boolean success = false; // Assume failure initially

        // Check 1: Does the dragboard have the correct data format?
        if (db.hasContent(CARD_DATA_FORMAT)) {
            LOGGER.fine("Dragboard has CARD_DATA_FORMAT.");
            CardInfo cardInfo = getCardInfoFromDragboard(db);
            int[] targetCoords = getHexAt(event.getX(), event.getY());

            // Check 2: Are coordinates and card info valid?
            if (targetCoords != null && cardInfo.type != CardType.UNKNOWN) {
                int row = targetCoords[0]; // UI Row
                int col = targetCoords[1]; // UI Column
                LOGGER.fine("Drop detected for card type: " + cardInfo.type + ", ID: " + cardInfo.id + " at coords: ("
                        + row + ", " + col + ")");

                // Check 3: Validate the placement
                ValidationResult validation = validatePlacement(cardInfo, row, col);
                LOGGER.fine("Validation result for drop: " + validation);

                if (validation.isValid()) {
                    LOGGER.info("Validation PASSED. Attempting to publish event for " + cardInfo.type + " (ID: "
                            + cardInfo.id + ") on tile (" +
                            row + ", " + col + ")");
                    try {
                        // Check 4: Publish the correct event based on card type
                        switch (cardInfo.type) {
                            case STRUCTURE:
                                LOGGER.fine("[PUBLISHING] PlaceStructureUIEvent for (" + row + ", " + col + "), ID: "
                                        + cardInfo.id);
                                eventBus.publish(new PlaceStructureUIEvent(row, col, cardInfo.id));
                                success = true;
                                break;
                            case STATUE:
                                // Statue placement requires selection first
                                Optional<Integer> selectedStatueId = showStatueSelectionPopup();
                                if (selectedStatueId.isPresent()) {
                                    int statueId = selectedStatueId.get();
                                    // Re-validate affordability for the *specific* statue chosen
                                    if (canAffordCard(statueId)) {
                                        LOGGER.fine("[PUBLISHING] PlaceStatueUIEvent for (" + row + ", " + col
                                                + "), ID: " + statueId);
                                        eventBus.publish(new PlaceStatueUIEvent(statueId, row, col));
                                        success = true;
                                    } else {
                                        LOGGER.warning("Cannot afford selected statue ID: " + statueId);
                                        showNotification("You cannot afford the selected statue (Cost: "
                                                + getCardCost(statueId) + ").");
                                        // success remains false
                                    }
                                } else {
                                    LOGGER.info("Statue placement cancelled by user.");
                                    showNotification("Statue placement cancelled.");
                                    // success remains false
                                }
                                break;
                            case ARTIFACT:
                                GameEntity entity = EntityRegistry.getGameEntityOriginalById(cardInfo.id);
                                if (entity instanceof Artifact artifactEntity) {
                                    String useType = artifactEntity.getUseType().getType();
                                    if ("Field".equalsIgnoreCase(useType)) {
                                        LOGGER.fine("[PUBLISHING] UseFieldArtifactUIEvent for (" + row + ", " + col
                                                + "), ID: " + cardInfo.id);
                                        eventBus.publish(new UseFieldArtifactUIEvent(row, col, cardInfo.id, useType));
                                        success = true;
                                    } else if ("Player".equalsIgnoreCase(useType)) {
                                        // Determine target player based on tile ownership
                                        String targetPlayerName = getTileOwnerName(row, col); // Uses (row, col)
                                        // If tile unowned or owned by self, target self
                                        if (targetPlayerName == null || (localPlayer != null
                                                && targetPlayerName.equals(localPlayer.getName()))) {
                                            targetPlayerName = (localPlayer != null) ? localPlayer.getName() : null; // Target
                                                                                                                     // self
                                            LOGGER.fine("Targeting self for Player artifact: " + targetPlayerName);
                                        } else {
                                            LOGGER.fine(
                                                    "Targeting other player for Player artifact: " + targetPlayerName);
                                        }

                                        if (targetPlayerName != null) {
                                            LOGGER.fine("[PUBLISHING] UsePlayerArtifactUIEvent for target: "
                                                    + targetPlayerName + ", ID: " + cardInfo.id);
                                            eventBus.publish(new UsePlayerArtifactUIEvent(cardInfo.id, useType,
                                                    targetPlayerName));
                                            success = true;
                                        } else {
                                            LOGGER.warning(
                                                    "Could not determine target player for Player artifact (localPlayer is null?).");
                                            showNotification("Error: Could not determine target player.");
                                            // success remains false
                                        }
                                    } else {
                                        LOGGER.warning("Artifact with unhandled useType '" + useType
                                                + "' cannot be used on the board.");
                                        showNotification("This artifact type cannot be used on the board.");
                                        // success remains false
                                    }
                                } else {
                                    LOGGER.severe("ARTIFACT card entity is not an Artifact for ID: " + cardInfo.id);
                                    // success remains false
                                }
                                break;
                            default:
                                LOGGER.severe("Unhandled card type in DragDropped: " + cardInfo.type);
                                // success remains false
                                break;
                        }
                        // Log success state after attempting to publish
                        if (success) {
                            LOGGER.info("Event published successfully for " + cardInfo.type + " on tile (" + row + ", "
                                    + col + ")");
                        } else {
                            LOGGER.warning("Event publication step resulted in failure or cancellation for "
                                    + cardInfo.type + " on tile (" + row + ", " + col + ")");
                        }
                    } catch (Exception e) {
                        LOGGER.log(Level.SEVERE, "Error publishing placement event for card " + cardInfo.id, e);
                        showNotification("Error processing card placement. See logs.");
                        success = false; // Ensure success is false on exception
                    }
                } else {
                    // Validation failed
                    LOGGER.warning("Drop rejected on tile (" + row + ", " + col + ") due to validation failure: "
                            + validation.reason());
                    showNotification("Cannot place card here: " + validation.reason());
                    success = false;
                }
            } else {
                // Invalid coordinates or card type
                if (targetCoords == null)
                    LOGGER.warning("Drop failed: Invalid drop location (outside grid).");
                else
                    LOGGER.warning("Drop failed: Unknown card type from dragboard.");
                success = false;
            }
        } else {
            LOGGER.warning("Drop failed: No valid card data found in dragboard.");
            success = false;
        }

        // Check 5: Finalize the drop event
        LOGGER.fine("Setting drop completed status to: " + success);
        event.setDropCompleted(success);
        event.consume(); // Consume the event to indicate it's handled
        LOGGER.fine("handleDragDropped finished.");
    }

    /**
     * Handles the completion of a drag-and-drop operation.
     * Logs the outcome.
     *
     * @param event the DragEvent
     */
    @FXML
    private void handleCardDragDone(DragEvent event) {
        // Log based on whether the drop was accepted and completed (TransferMode.MOVE)
        if (event.getTransferMode() == TransferMode.MOVE) {
            LOGGER.fine("Card drag done: Successful drop (MOVE transfer mode).");
            // Optionally clear selection or provide other feedback if needed
            if (draggedCardSource != null && draggedCardSource == selectedCard) {
                selectedCard.getStyleClass().remove("selected-card");
                selectedCard = null;
            }
        } else {
            LOGGER.fine("Card drag done: Drop failed or cancelled (Transfer mode: " + event.getTransferMode() + ").");
            // Optionally restore visual state if needed
        }
        // Always clear the source reference
        draggedCardSource = null;
        event.consume(); // Consume the event
    }

    /**
     * Displays a popup for selecting a statue to place.
     * Uses a ChoiceDialog with Strings for display.
     *
     * @return an Optional containing the selected statue ID, or empty if cancelled
     */
    private Optional<Integer> showStatueSelectionPopup() {
        // Get available placeable statues (IDs 31-37 typically)
        List<Statue> availableStatues = EntityRegistry.getAllStatues().stream()
                .filter(s -> s.getId() >= 31 && s.getId() <= 37) // Filter for placeable statues
                .collect(Collectors.toList());

        if (availableStatues.isEmpty()) {
            LOGGER.severe("No placeable statues found in EntityRegistry!");
            showNotification("Error: No statues available to place.");
            return Optional.empty();
        }

        // Create a map from display string to statue ID
        Map<String, Integer> statueOptions = new LinkedHashMap<>(); // Use LinkedHashMap to preserve order
        for (Statue statue : availableStatues) {
            // Calculate actual price for display
            int actualPrice = calculateActualPrice(statue.getPrice());
            String displayName = statue.getName() + " (Cost: " + actualPrice + ")";
            statueOptions.put(displayName, statue.getId());
        }

        // Use ChoiceDialog<String> instead of ChoiceDialog<Statue>
        ChoiceDialog<String> dialog = new ChoiceDialog<>(
                statueOptions.keySet().iterator().next(), // Default selection
                statueOptions.keySet()); // Available choices
        dialog.setTitle("Choose Statue");
        dialog.setHeaderText("Select the statue you want to place:");
        dialog.setContentText("Statue:");

        // No setConverter needed for String dialog

        Optional<String> result = dialog.showAndWait();

        // Map the selected display string back to the statue ID using the map
        return result.map(statueOptions::get);
    }

    /**
     * Retrieves detailed information about a card.
     *
     * @param cardFxId the FXML ID of the card
     * @return the CardDetails object
     */
    public CardDetails getCardDetails(String cardFxId) {
        CardInfo cardInfo = getCardInfoFromFxId(cardFxId);
        if (cardInfo.type == CardType.UNKNOWN) {
            LOGGER.warning("getCardDetails called with unknown card FxId: " + cardFxId);
            int placeholderId = 22;
            if (cardFxId != null) {
                if (cardFxId.startsWith("structure"))
                    placeholderId = 1;
                else if (cardFxId.startsWith("statue"))
                    placeholderId = 38;
            }
            GameEntity placeholderEntity = EntityRegistry.getGameEntityOriginalById(placeholderId);
            if (placeholderEntity != null) {
                return new CardDetails(placeholderEntity.getName(), placeholderEntity.getUsage(),
                        placeholderEntity.getDescription(), EntityRegistry.getURL(placeholderId, true), 0);
            } else {
                return new CardDetails("Unknown Card", "Details unavailable.", "", null, 0);
            }
        }

        GameEntity entity = EntityRegistry.getGameEntityOriginalById(cardInfo.id);
        if (entity == null) {
            LOGGER.severe("EntityRegistry returned null for card ID: " + cardInfo.id);
            return new CardDetails("Error", "Could not load details.", "", null, 0);
        }

        String url = EntityRegistry.getURL(cardInfo.id, true);
        String title = entity.getName();
        String description = entity.getUsage();
        String lore = entity.getDescription();
        int basePrice = entity.getPrice();
        int actualPrice = calculateActualPrice(basePrice);

        return new CardDetails(title, description, lore, url, actualPrice);
    }

    /**
     * Calculates the actual price of an entity considering player buffs.
     *
     * @param basePrice the base price of the entity
     * @return the adjusted price
     */
    private int calculateActualPrice(int basePrice) {
        if (basePrice == 0)
            return 0;
        if (gamePlayer == null)
            return basePrice;
        double priceModifier = gamePlayer.getStatus().get(Status.BuffType.SHOP_PRICE);
        double adjusted = basePrice * Math.max(priceModifier, 0.1);
        return Math.max(0, (int) Math.round(adjusted));
    }

    /**
     * Retrieves the entity ID from an FXML ID.
     *
     * @param fxId the FXML ID
     * @return the corresponding entity ID
     */
    private int getEntityID(String fxId) {
        CardInfo info = getCardInfoFromFxId(fxId);
        return info.id;
    }

    /**
     * Retrieves card information from a Node.
     *
     * @param card the card Node
     * @return the CardInfo containing type and ID
     */
    private CardInfo getCardInfo(Node card) {
        if (card == null || card.getId() == null) {
            return new CardInfo(CardType.UNKNOWN, -1);
        }
        return getCardInfoFromFxId(card.getId());
    }

    /**
     * Retrieves card information from an FXML ID.
     *
     * @param fxId the FXML ID
     * @return the CardInfo containing type and ID
     */
    private CardInfo getCardInfoFromFxId(String fxId) {
        if (fxId == null || fxId.isEmpty()) {
            return new CardInfo(CardType.UNKNOWN, -1);
        }
        try {
            if (fxId.startsWith("artifact")) {
                int index = Integer.parseInt(fxId.replace("artifact", "")) - 1;
                if (artifactsInHand != null && index >= 0 && index < artifactsInHand.size()) {
                    return new CardInfo(CardType.ARTIFACT, artifactsInHand.get(index).getId());
                } else {
                    return new CardInfo(CardType.ARTIFACT, 22);
                }
            } else if (fxId.startsWith("structure")) {
                int structureNum = Integer.parseInt(fxId.replace("structure", ""));
                if (structureNum >= 1 && structureNum <= 5) {
                    return new CardInfo(CardType.STRUCTURE, structureNum);
                } else {
                    LOGGER.warning("Unexpected structure fx:id: " + fxId);
                    return new CardInfo(CardType.UNKNOWN, -1);
                }
            } else if (fxId.equals("statueCard")) {
                return new CardInfo(CardType.STATUE, 38);
            } else {
                LOGGER.warning("Unrecognized card fx:id format: " + fxId);
                return new CardInfo(CardType.UNKNOWN, -1);
            }
        } catch (NumberFormatException e) {
            LOGGER.warning("Failed to parse number from card fx:id: " + fxId);
            return new CardInfo(CardType.UNKNOWN, -1);
        }
    }

    /**
     * Retrieves card information from the dragboard.
     *
     * @param db the Dragboard
     * @return the CardInfo containing type and ID
     */
    private CardInfo getCardInfoFromDragboard(Dragboard db) {
        if (db.hasContent(CARD_DATA_FORMAT)) {
            String data = (String) db.getContent(CARD_DATA_FORMAT);
            String[] parts = data.split(":", 2);
            if (parts.length == 2) {
                try {
                    CardType type = CardType.valueOf(parts[0]);
                    int id = Integer.parseInt(parts[1]);
                    return new CardInfo(type, id);
                } catch (IllegalArgumentException e) {
                    LOGGER.warning("Failed to parse card data from dragboard: " + data);
                }
            }
        }
        return new CardInfo(CardType.UNKNOWN, -1);
    }

    /**
     * Updates the display of all card slots with current images and affordability.
     */
    private void updateCardDisplay() {
        LOGGER.fine("Updating all card displays...");
        List<Pane> artifactPanes = List.of(artifact1, artifact2, artifact3);
        for (int i = 0; i < artifactPanes.size(); i++) {
            Pane cardPane = artifactPanes.get(i);
            int entityId = (artifactsInHand != null && i < artifactsInHand.size()) ? artifactsInHand.get(i).getId()
                    : 22;
            updateSingleCardView(cardPane, entityId);
        }

        List<Pane> structurePanes = List.of(structure1, structure2, structure3, structure4, structure5);
        for (int i = 0; i < structurePanes.size(); i++) {
            Pane cardPane = structurePanes.get(i);
            int entityId = i + 1;
            updateSingleCardView(cardPane, entityId);
        }

        updateSingleCardView(statueCard, 38);
        LOGGER.fine("Card display update finished.");
    }

    /**
     * Updates the visual representation of a single card.
     *
     * @param cardPane the Pane representing the card
     * @param entityId the ID of the entity
     */
    private void updateSingleCardView(Pane cardPane, int entityId) {
        if (cardPane == null) {
            LOGGER.warning("Attempted to update a null card pane.");
            return;
        }
        String imageUrl = EntityRegistry.getURL(entityId, true);
        Image image = (imageUrl != null && !imageUrl.isEmpty()) ? resourceLoader.loadImage(imageUrl) : null;
        if (image == null || image.isError()) {
            LOGGER.warning("Failed to load card image for entity ID: " + entityId +
                    (imageUrl != null ? " from URL: " + imageUrl : ""));
            image = placeholderImage;
        }

        cardPane.getChildren().clear();
        if (image != null) {
            ImageView imageView = new ImageView(image);
            imageView.setPreserveRatio(true);
            imageView.setFitWidth(78);
            imageView.setFitHeight(118);
            StackPane wrapper = new StackPane(imageView);
            wrapper.setPrefSize(80, 120);
            wrapper.setAlignment(Pos.CENTER);
            cardPane.getChildren().add(wrapper);
        } else {
            Label errorLabel = new Label("ERR");
            errorLabel.setStyle("-fx-text-fill: red; -fx-alignment: center;");
            cardPane.getChildren().add(errorLabel);
        }

        boolean canAfford = canAffordCard(entityId);
        CardType type = getCardInfoFromFxId(cardPane.getId()).type;
        cardPane.getStyleClass().removeAll("affordable-card", "unaffordable-card", "game-card");

        if (canAfford) {
            cardPane.getStyleClass().add("affordable-card");
            if (type == CardType.STRUCTURE || type == CardType.STATUE || type == CardType.ARTIFACT) {
                cardPane.removeEventHandler(MouseEvent.DRAG_DETECTED, this::handleCardDragDetected);
                cardPane.addEventHandler(MouseEvent.DRAG_DETECTED, this::handleCardDragDetected);
            } else {
                cardPane.removeEventHandler(MouseEvent.DRAG_DETECTED, this::handleCardDragDetected);
            }
        } else {
            cardPane.getStyleClass().add("unaffordable-card");
            cardPane.removeEventHandler(MouseEvent.DRAG_DETECTED, this::handleCardDragDetected);
        }
        cardPane.getStyleClass().add("game-card");
        cardPane.setVisible(true);
        cardPane.setOpacity(1.0);
    }

    /**
     * Updates the runes label and energy bar based on the current game state.
     */
    public void updateRunesAndEnergyBar() {
        if (gamePlayer != null) {
            runesLabel.setText("Runes: " + gamePlayer.getRunes());
            energyBar.setProgress((double) gamePlayer.getEnergy() / SETTINGS.Config.MAX_ENERGY.getValue());
        } else {
            runesLabel.setText("Runes: 0");
            energyBar.setProgress(0.0);
        }
    }

    /**
     * Updates the player list display, highlighting the active player.
     */
    private void updatePlayerList() {
        if (gameState == null) {
            LOGGER.warning("Game state is null");
            return;
        }
        LOGGER.info("Updating player list");
        players.clear();
        String currentPlayerName = gameState.getPlayerTurn();

        for (Player player : gameState.getPlayers()) {
            players.add(player.getName());
        }

        playersList.setCellFactory(listView -> new ListCell<>() {
            @Override
            protected void updateItem(String playerName, boolean empty) {
                super.updateItem(playerName, empty);
                if (empty || playerName == null) {
                    setText(null);
                    setGraphic(null);
                    getStyleClass().remove("current-turn-player");
                } else {
                    setText(playerName);
                    Player player = gameState.findPlayerByName(playerName);
                    getStyleClass().remove("current-turn-player");
                    if (player != null && currentPlayerName != null && player.getName().equals(currentPlayerName)) { // Added
                                                                                                                     // null
                                                                                                                     // check
                                                                                                                     // for
                                                                                                                     // currentPlayerName
                        getStyleClass().add("current-turn-player");
                    }
                    if (player != null) {
                        // Use GameScreenController.this for clarity accessing outer class method
                        Color playerColor = GameScreenController.this.getPlayerColor(player.getName());
                        if (playerColor != null) {
                            Circle colorIndicator = new Circle(6);
                            colorIndicator.setFill(playerColor);
                            setGraphic(colorIndicator);
                        } else {
                            setGraphic(null); // Ensure no old graphic remains if color is null
                        }
                    } else {
                        setGraphic(null); // Ensure no old graphic remains if player is null
                    }
                }
            }
        });
    }

    /**
     * Checks if the player can afford a tile.
     *
     * @param cost the cost of the tile
     * @return true if affordable, false otherwise
     */
    private boolean canAffordTile(int cost) {
        return getPlayerRunes() >= cost;
    }

    /**
     * Checks if the player can afford a card.
     *
     * @param entityId the ID of the card's entity
     * @return true if affordable, false otherwise
     */
    private boolean canAffordCard(int entityId) {
        int cost = getCardCost(entityId);
        return getPlayerRunes() >= cost;
    }

    /**
     * Retrieves the cost of a card.
     *
     * @param entityId the ID of the card's entity
     * @return the cost of the card
     */
    private int getCardCost(int entityId) {
        GameEntity entity = EntityRegistry.getGameEntityOriginalById(entityId);
        if (entity == null)
            return Integer.MAX_VALUE;
        return calculateActualPrice(entity.getPrice());
    }

    /**
     * Retrieves the current player's runes.
     *
     * @return the number of runes
     */
    private int getPlayerRunes() {
        return (gamePlayer != null) ? gamePlayer.getRunes() : 0;
    }

    /**
     * Checks if the tile is owned by the local player.
     *
     * @param row the row index
     * @param col the column index
     * @return true if owned by the local player, false otherwise
     */
    private boolean isTileOwnedByPlayer(int row, int col) {
        if (localPlayer == null)
            return false;
        Tile tile = getTile(row, col); // Uses helper with coordinate swap
        return tile != null && localPlayer.getName().equals(tile.getOwner());
    }

    /**
     * Checks if the tile is occupied by an entity or artifact.
     *
     * @param row the row index
     * @param col the column index
     * @return true if occupied, false otherwise
     */
    private boolean isTileOccupied(int row, int col) {
        Tile tile = getTile(row, col); // Uses helper with coordinate swap
        // A tile is considered occupied if it has a structure/statue OR an artifact
        return tile != null && (tile.hasEntity() || tile.getArtifact() != null);
    }

    /**
     * Retrieves the owner name of a tile.
     *
     * @param row the row index
     * @param col the column index
     * @return the owner's name, or null if unowned
     */
    private String getTileOwnerName(int row, int col) {
        Tile tile = getTile(row, col); // Uses helper with coordinate swap
        return (tile != null) ? tile.getOwner() : null;
    }

    /**
     * Helper method to get a Tile using UI (row, col) coordinates.
     * Handles the translation to BoardManager's (col, row).
     *
     * @param row UI row index.
     * @param col UI column index.
     * @return The Tile object or null if not found or state invalid.
     */
    private Tile getTile(int row, int col) {
        if (gameState == null || gameState.getBoardManager() == null) {
            LOGGER.warning("Attempted to get tile with null GameState or BoardManager.");
            return null;
        }
        // Swap coordinates for BoardManager access
        return gameState.getBoardManager().getTile(col, row);
    }

    /**
     * Checks if the player can place another structure.
     *
     * @return true if allowed, false otherwise
     */
    private boolean canPlaceStructure() {
        if (gamePlayer == null)
            return false;
        long structureCount = gamePlayer.getPurchasableEntities().stream()
                .filter(e -> e instanceof Structure)
                .count();
        return structureCount < MAX_STRUCTURES;
    }

    /**
     * Checks if the player can place a statue.
     *
     * @return true if allowed, false otherwise
     */
    private boolean canPlaceStatue() {
        if (gamePlayer == null)
            return false;
        long statueCount = gamePlayer.getPurchasableEntities().stream()
                .filter(e -> e instanceof Statue)
                .count();
        return statueCount < MAX_STATUES;
    }

    /**
     * Validates if a card can be placed on a tile.
     * Uses helper methods that handle coordinate swapping internally.
     *
     * @param cardInfo the card information
     * @param row      the row index (UI convention)
     * @param col      the column index (UI convention)
     * @return the ValidationResult
     */
    private ValidationResult validatePlacement(CardInfo cardInfo, int row, int col) {
        if (gameState == null || localPlayer == null || !localPlayer.getName().equals(gameState.getPlayerTurn())) {
            return ValidationResult.invalid("It's not your turn.");
        }

        // Ownership check needed for Structures and Statues
        if (cardInfo.type == CardType.STRUCTURE || cardInfo.type == CardType.STATUE) {
            if (!isTileOwnedByPlayer(row, col)) { // Uses helper with coordinate swap
                return ValidationResult.invalid("You do not own this tile.");
            }
        }
        // Field artifacts also require ownership
        if (cardInfo.type == CardType.ARTIFACT) {
            GameEntity entity = EntityRegistry.getGameEntityOriginalById(cardInfo.id);
            if (entity instanceof Artifact artifactEntity
                    && "Field".equalsIgnoreCase(artifactEntity.getUseType().getType())) {
                if (!isTileOwnedByPlayer(row, col)) { // Uses helper with coordinate swap
                    return ValidationResult.invalid("You must own the tile to use a field artifact.");
                }
            }
        }

        // Occupancy check: Structures/Statues cannot be placed on occupied tiles.
        // Field Artifacts *can* be placed on tiles with structures/statues, but not
        // other artifacts.
        if (isTileOccupied(row, col)) { // Uses helper with coordinate swap
            Tile targetTile = getTile(row, col); // Uses helper with coordinate swap
            if (targetTile != null) {
                if (cardInfo.type == CardType.STRUCTURE || cardInfo.type == CardType.STATUE) {
                    // Cannot place structure```java
                    // Cannot place structure/statue if *anything* is there
                    if (targetTile.hasEntity()) {
                        return ValidationResult
                                .invalid("Tile is already occupied by " + targetTile.getEntity().getName() + ".");
                    }
                    if (targetTile.getArtifact() != null) {
                        return ValidationResult.invalid(
                                "Tile already contains an artifact (" + targetTile.getArtifact().getName() + ").");
                    }
                } else if (cardInfo.type == CardType.ARTIFACT) {
                    // Cannot place artifact if another artifact is already there
                    if (targetTile.getArtifact() != null) {
                        return ValidationResult.invalid(
                                "Tile already contains an artifact (" + targetTile.getArtifact().getName() + ").");
                    }
                    // Placing artifact on tile with structure/statue is allowed by game rules.
                }
            }
        }

        // Limits and Affordability checks remain the same
        switch (cardInfo.type) {
            case STRUCTURE:
                if (!canPlaceStructure()) {
                    return ValidationResult.invalid("Maximum number of structures (" + MAX_STRUCTURES + ") reached.");
                }
                break;
            case STATUE:
                if (!canPlaceStatue()) {
                    return ValidationResult.invalid("You can only place one statue.");
                }
                break;
            case ARTIFACT:
                // No placement limit for artifacts (hand limit is implicit)
                break;
            default:
                return ValidationResult.invalid("Unknown card type.");
        }

        if (!canAffordCard(cardInfo.id)) {
            return ValidationResult.invalid("You cannot afford this card (Cost: " + getCardCost(cardInfo.id) + ").");
        }

        return ValidationResult.valid();
    }

    /**
     * Retrieves the owner ID of a tile.
     * Uses the getTile helper which handles coordinate swapping.
     * Note: This method seems unused now, replaced by getTileOwnerName.
     * Keeping it for potential internal use or future refactoring.
     *
     * @param row the row index (UI convention)
     * @param col the column index (UI convention)
     * @return the owner's ID (name), or null if unowned or tile not found
     */
    private String getTileOwnerId(int row, int col) {
        Tile tile = getTile(row, col); // Uses helper with coordinate swap
        return (tile != null) ? tile.getOwner() : null;
    }

    /**
     * Retrieves the color associated with a player.
     *
     * @param playerId the player's ID
     * @return the player's Color, or null if not found
     */
    private Color getPlayerColor(String playerId) {
        return playerColors.get(playerId);
    }

    /**
     * Creates a simple red square placeholder image.
     */
    private void createPlaceholderImage() {
        int width = 32;
        int height = 32;
        WritableImage img = new WritableImage(width, height);
        PixelWriter pw = img.getPixelWriter();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                pw.setColor(x, y, Color.RED);
            }
        }
        this.placeholderImage = img;
        LOGGER.fine("Placeholder image created.");
    }

    /**
     * Handles the resource overview action (not implemented).
     */
    @FXML
    private void handleResourceOverview() {
        LOGGER.info("Resource overview not implemented.");
    }

    /**
     * Handles the game round action (not implemented).
     */
    @FXML
    private void handleGameRound() {
        LOGGER.info("Game round logic not implemented.");
    }

    /**
     * Handles the leaderboard action (not implemented).
     */
    @FXML
    private void handleLeaderboard() {
        LOGGER.info("Leaderboard not implemented.");
    }

    /**
     * Displays a notification message to the user, typically in the chat area.
     *
     * @param message The message to display.
     */
    private void showNotification(String message) {
        if (chatComponentController != null) {
            Platform.runLater(() -> chatComponentController.addSystemMessage(message));
        } else {
            LOGGER.warning("Cannot show notification, chat component is null: " + message);
            // Fallback: Log or show an Alert if chat is unavailable
            // Platform.runLater(() -> {
            // Alert alert = new Alert(AlertType.INFORMATION);
            // alert.setTitle("Notification");
            // alert.setHeaderText(null);
            // alert.setContentText(message);
            // alert.showAndWait();
            // });
        }
    }

    private enum CardType {
        STRUCTURE, STATUE, ARTIFACT, UNKNOWN
    }

    private record CardInfo(CardType type, int id) {
    }

    private record ValidationResult(boolean isValid, String reason) {
        static ValidationResult valid() {
            return new ValidationResult(true, null);
        }

        static ValidationResult invalid(String reason) {
            return new ValidationResult(false, reason);
        }
    }

    public Node getGameCanvas() {
        return gameCanvas;
    }

}
