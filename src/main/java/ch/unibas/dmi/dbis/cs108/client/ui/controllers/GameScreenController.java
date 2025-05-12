package ch.unibas.dmi.dbis.cs108.client.ui.controllers;

import ch.unibas.dmi.dbis.cs108.SETTINGS;
import ch.unibas.dmi.dbis.cs108.client.app.GameApplication;
import ch.unibas.dmi.dbis.cs108.client.audio.AudioManager;
import ch.unibas.dmi.dbis.cs108.client.audio.AudioTracks;
import ch.unibas.dmi.dbis.cs108.client.core.PlayerIdentityManager;
import ch.unibas.dmi.dbis.cs108.client.core.state.GameState;
import ch.unibas.dmi.dbis.cs108.client.ui.SceneManager;
import ch.unibas.dmi.dbis.cs108.client.ui.components.ChatComponent;
import ch.unibas.dmi.dbis.cs108.client.ui.components.SettingsDialog;
import ch.unibas.dmi.dbis.cs108.client.ui.components.WinScreenDialog;
import ch.unibas.dmi.dbis.cs108.client.ui.components.game.*;
import ch.unibas.dmi.dbis.cs108.client.ui.events.ErrorEvent;
import ch.unibas.dmi.dbis.cs108.client.ui.events.UIEventBus;
import ch.unibas.dmi.dbis.cs108.client.ui.events.admin.*;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.scene.text.Font;
import ch.unibas.dmi.dbis.cs108.client.ui.events.game.*;
import ch.unibas.dmi.dbis.cs108.client.ui.events.lobby.LeaveLobbyRequestEvent;
import ch.unibas.dmi.dbis.cs108.client.ui.events.lobby.LobbyJoinedEvent;
import ch.unibas.dmi.dbis.cs108.client.ui.utils.CardDetails;
import ch.unibas.dmi.dbis.cs108.client.ui.utils.ResourceLoader;
import ch.unibas.dmi.dbis.cs108.shared.entities.EntityRegistry;
import ch.unibas.dmi.dbis.cs108.shared.entities.Findables.Artifact;
import ch.unibas.dmi.dbis.cs108.shared.entities.Findables.Monument;
import ch.unibas.dmi.dbis.cs108.shared.entities.GameEntity;
import ch.unibas.dmi.dbis.cs108.shared.entities.Purchasables.PurchasableEntity;
import ch.unibas.dmi.dbis.cs108.shared.entities.Purchasables.Statues.Statue;
import ch.unibas.dmi.dbis.cs108.shared.game.Player;
import ch.unibas.dmi.dbis.cs108.shared.game.Status;
import ch.unibas.dmi.dbis.cs108.shared.game.Tile;
import javafx.animation.PauseTransition;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.stage.Window;
import javafx.util.Duration;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * JavaFX controller for the in‑game screen.
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

    /**
     * --------------------------------------------------
     * Hex grid dimensions
     * --------------------------------------------------
     */
    /*
     * The number of rows in the hex grid.
     */
    static final int HEX_ROWS = 7;
    /*
     * The number of collums in the hex grid.
     */
    static final int HEX_COLS = 8;
    /*
     * --------------------------------------------------
     * Static configuration
     * --------------------------------------------------
     */
    private static final Logger LOGGER = Logger.getLogger(GameScreenController.class.getName());
    /*
     * --------------------------------------------------
     * Game / UI state
     * --------------------------------------------------
     */
    private final AtomicBoolean uiInitialized = new AtomicBoolean(false);

    /*
     * The player manager is used to manage player identities and their
     * properties.
     */
    private final PlayerIdentityManager playerManager;
    /*
     * The observable list of players in the game.
     */
    private final ObservableList<String> players = FXCollections.observableArrayList();
    // Simplified colour table – replace with proper game state look‑up
    /*
     * The map of player names to their corresponding colors.
     */
    private final Map<String, Color> playerColors = new HashMap<>();
    List<Color> playerColours;
    /*
     * The following fields are package‑private because the adjustment manager
     * accesses them directly.
     */
    /*
     * The size of the hex grid.
     */
    double effectiveHexSize;
    /*
     * The grid offset in the x direction.
     */
    double gridOffsetX;
    /*
     * The grid offset in the y direction.
     */
    double gridOffsetY;
    /*
     * The local player in the game.
     */
    private Player localPlayer;
    /*
     * The game player in the game.
     */
    private Player gamePlayer;
    /*
     * The game state of the game.
     */
    private GameState gameState;
    /*
     * The list of players in the game.
     */
    private List<Artifact> artifacts = new ArrayList<>();
    // Map and grid dimensions calculated at runtime

    /*
     * The width of the map.
     */
    private double scaledMapWidth;
    /*
     * The height of the map.
     */
    private double scaledMapHeight;
    /*
     * The width of the hex grid.
     */
    private double mapOffsetX;
    /*
     * The height of the hex grid.
     */
    private double mapOffsetY;
    /*
     * The vertical spacing between hexes.
     */
    private double vSpacing;
    /*
     * The horizontal spacing between hexes.
     */
    private double hSpacing;
    /*
     * The lobby ID of the game.
     */
    private String currentLobbyId;
    /*
     * The image of the map.
     */
    private Image mapImage;
    /*
     * the check if the map is loaded.
     */
    private boolean isMapLoaded;
    /*
    * the settings dialog.
     */
    private SettingsDialog settingsDialog;
    /*
     * The selected card in the game.
     */
    private Node selectedCard;
    /*
     * The selected statue in the game.
     */
    private CardDetails selectedStatue;

    /*
     * the check if the statue is placed.
     */
    private boolean hasPlacedStatue = false;
    /*
     * The highlighted tile in the game.
     */
    private Tile highlightedTile = null;
    /*
     * The grid adjustment manager.
     */
    private GridAdjustmentManager gridAdjustmentManager;
    // --- Tile tooltip support ---
    /*
     * The current tile tooltip.
     */
    private TileTooltip currentTileTooltip = null;
    /*
     * The last tile tooltip in the row.
     */
    private int lastTooltipRow = -1;
    /*
     * The last tile tooltip in the column.
     */
    private int lastTooltipCol = -1;
    // Add these fields to the class to track hover state and delay
    /*
     * The tooltip show delay.
     */
    private PauseTransition tooltipShowDelay;
    /*
        * the pending tooltip row.
     */
    private int pendingTooltipRow = -1;
    /*
     * The pending tooltip column.
     */
    private int pendingTooltipCol = -1;
    /*
     * check if the tooltip is disabled.
     */
    private boolean isTooltipDisabled = false;

    /** Keeps the last round that has already been rendered. –1 ⇒ not initialised */
    private int lastKnownRound = -1;

    // Artifact Indicator
    /*
     * The artifact indicator.
     */
    private static final Duration ARTIFACT_INDICATOR_DURATION = Duration.seconds(10); // How long to show the indicator
    /*
     * the located artifact screen coordinates.
     */
    private Point2D locatedArtifactScreenCoords = null;
    /*
     * The located artifact ID.
     */
    private int locatedArtifactId = -1;
    /** The trap Coordinates*/
    private Point2D trapLocationScreenCoords;
    /*
     * The trap lost runes.
     */
    private int trapLostRunes;
    /*
     * The trap indicator opdacity.
     */
    private DoubleProperty trapIndicatorOpacityProperty = new SimpleDoubleProperty(1.0);
    /** A double value for the opacity */
    private double trapIndicatorOpacity = 1.0;
    /** The fadeAnimation Timeline */
    private Timeline trapFadeAnimation;
    /*
     * The artifact indicator clear timer.
     */
    private PauseTransition artifactIndicatorClearTimer;

    /*
     * the statue confirmation dialog.
     */
    private StatueConfirmationDialog statueConfirmationDialog;

    /*
     * --------------------------------------------------
     * FXML‑injected UI elements
     * --------------------------------------------------
     */
    /*
     * The game canvas.
     */
    @FXML
    private Canvas gameCanvas;
    /*
     * The energy bar.
     */
    @FXML
    private ProgressBar energyBar;
    /*
     * The runes label.
     */
    @FXML
    private Label runesLabel;
    /*
    * the player list.
     */
    @FXML
    private ListView<String> playersList;
    /*
     * The artifact hand.
     */
    @FXML
    private HBox artifactHand;
    /*
     * The structure hand.
     */
    @FXML
    private FlowPane structureHand;
    /*
     * The connection status label.
     */
    @FXML
    private Label connectionStatusLabel;
    /*
    * the chat container.
     */
    @FXML
    private VBox chatContainer;
    /*
     * The timer root.
     */
    @FXML
    private StackPane timerRoot;
    /*
     * The round label.
     */
    @FXML
    private Label roundLabel;

    /*
    * the chat component controller.
     */
    private ChatComponent chatComponentController;
    /*
    * the resource overview dialog.
     */
    private ResourceOverviewDialog resourceOverviewDialog;
    // Grid‑adjustment overlay controls (created programmatically)
    /*
    * the adjustment mode indicator.
     */
    private Label adjustmentModeIndicator;
    /*
     * The adjustment values label.
     */
    private Label adjustmentValuesLabel;
    /*
    * background canvas for the map and grid.
     */
    private Canvas backgroundCanvas; // map + white grid (static)
    /*
     * The overlay canvas for the hover outline.
     */
    private Canvas overlayCanvas; // yellow hover outline (dynamic)

    /*
    * the timer component.
     */
    private TimerComponent timerComponent;

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

        // Request the gameState from the server
        eventBus.publish(new RequestGameStateEvent());
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
        isTooltipDisabled = false;

        // Initialize the statue confirmation dialog
        statueConfirmationDialog = new StatueConfirmationDialog(resourceLoader);

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
        isTooltipDisabled = false;

        if (localPlayer == null) {
            LOGGER.severe("LocalPlayer is null during GameScreenController initialisation!");
            localPlayer = new Player("ErrorGuest"); // Fail‑safe stub
        }

        createAdjustmentUI();
        gridAdjustmentManager = new GridAdjustmentManager(this, adjustmentModeIndicator, adjustmentValuesLabel,
                this::drawMapAndGrid);

        initialisePlayerColours();
        resourceOverviewDialog = new ResourceOverviewDialog(resourceLoader, playerColors);

        setupUI();
        updateCardImages();
        setupCanvasStack();
        setupCanvasListeners();
        loadMapImage();

        // Attach click sound to all buttons in the scene graph
        AudioManager.attachClickSoundToAllButtons(gameCanvas.getParent().getParent());
    }

    /**
     * Builds the three-layer canvas stack:
     * backgroundCanvas – map & hex lines (repainted rarely)
     * gameCanvas – entities / structures (already exists)
     * overlayCanvas – hover / selection glow (repainted every mouse-move)
     */
    private void setupCanvasStack() {
        StackPane board = (StackPane) gameCanvas.getParent();

        backgroundCanvas = new Canvas();
        overlayCanvas = new Canvas();

        backgroundCanvas.widthProperty().bind(gameCanvas.widthProperty());
        backgroundCanvas.heightProperty().bind(gameCanvas.heightProperty());
        overlayCanvas.widthProperty().bind(gameCanvas.widthProperty());
        overlayCanvas.heightProperty().bind(gameCanvas.heightProperty());

        // order matters: back → middle → front
        board.getChildren().setAll(backgroundCanvas, gameCanvas, overlayCanvas);
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
        eventBus.subscribe(ArtifactLocationEvent.class, this::handleArtifactLocationEvent);
        eventBus.subscribe(TrapLocationEvent.class, this::handleTrapLocationEvent);
        eventBus.subscribe(DebuffEvent.class, this::handleBuffOrDebuff);
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

        updatePlayerColors();
    }

    /**
     * Updates the player colors in the UI.
     * Ensures that each player keeps their color, even after a name change.
     */
    private void updatePlayerColors() {
        // Build a new mapping, preserving existing colors
        Map<String, Color> newColors = new HashMap<>();
        Set<Color> usedColors = new HashSet<>();

        // Always assign GREEN to the local player
        if (localPlayer != null) {
            newColors.put(localPlayer.getName(), Color.GREEN);
            usedColors.add(Color.GREEN);
        }

        // Prepare a list of available colors (excluding GREEN and already used)
        List<Color> availableColors = new ArrayList<>(playerColours);
        availableColors.remove(Color.GREEN);
        usedColors.addAll(playerColors.values());
        availableColors.removeAll(usedColors);

        // Assign colors to other players, preserving previous assignments
        for (Player player : gameState.getPlayers()) {
            String playerName = player.getName();
            if (playerName.equals(localPlayer.getName())) {
                continue; // Already assigned GREEN
            }
            Color prevColor = playerColors.get(playerName);
            if (prevColor != null && !prevColor.equals(Color.GREEN)) {
                newColors.put(playerName, prevColor);
            } else {
                // Assign a new color if available, otherwise fallback to gray
                Color colorToAssign = !availableColors.isEmpty() ? availableColors.remove(0) : Color.GRAY;
                newColors.put(playerName, colorToAssign);
            }
        }

        playerColors.clear();
        playerColors.putAll(newColors);
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

    /**
     * Handles the end game event and shows the win screen dialog.
     */
    private void onConnectionStatus(ConnectionStatusEvent e) {
        if (e == null)
            return;

        Platform.runLater(() -> {
            connectionStatusLabel.setText(Optional.ofNullable(e.getState()).map(Object::toString).orElse("UNKNOWN"));
            if (e.getMessage() != null && !e.getMessage().isEmpty()) {
                chatComponentController.addSystemMessage(e.getMessage());
            }
            if (settingsDialog != null) {
                String status = connectionStatusLabel.getText();
                settingsDialog.setConnectionStatus("Connected".equals(status), status);
            }
        });
    }

    /**
     * Handles the lobby joined event and updates the game state.
     */
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

        Platform.runLater(() -> {
            gameState = updatedState;
            gamePlayer = gameState.findPlayerByName(localPlayer.getName());

            if (gamePlayer == null) {
                LOGGER.warning("Game player not found in game state.");
                return;
            }

            boolean firstSync = uiInitialized.compareAndSet(false, true);
            if (firstSync) {
                LOGGER.info("First GameSyncEvent processed. Proceeding to full UI initialization...");
                initializeUI();

                // For the first sync, use a small delay to ensure initialization completes
                // This is especially important on slower PCs or networks
                PauseTransition delay = new PauseTransition(Duration.millis(200));
                delay.setOnFinished(event -> {
                    LOGGER.info("Performing first UI update after initialization");
                    performUIUpdate();
                });
                delay.play();
            } else {
                // For subsequent syncs, update immediately
                performUIUpdate();
            }
        });
    }

    /**
     * Updates all UI elements after a game sync event.
     * Extracted to avoid code duplication between first and subsequent syncs.
     */
    private void performUIUpdate() {
        updatePlayerColors();
        if (gamePlayer != null) { // Ensure gamePlayer is not null before accessing its properties
            artifacts = gamePlayer.getArtifacts();
            markStatuePlaced(gamePlayer.hasStatue());
        } else {
            artifacts.clear();
            markStatuePlaced(false);
        }


        detectRoundChangeAndRefresh();
        updateRunesAndEnergyBar(); // Updates rune label based on gamePlayer
        updateCardImages(); // Sets up card visuals and UserData based on gamePlayer
        updatePurchasableStates(); // <<< CALL THE NEW CENTRALIZED METHOD HERE
        updatePlayerList();
        updateMap();

        roundLabel.setText("Round: " + (gameState.getGameRound() + 1));
        // Initialize TimerComponent after FXML injection and only once
        if (timerComponent == null && timerRoot != null) {
            LOGGER.info("Initializing TimerComponent...");
            timerComponent = new TimerComponent();
            timerRoot.getChildren().setAll(timerComponent);
        }

        String currentPlayerTurn = gameState.getPlayerTurn();
        if (timerComponent != null) {
            timerComponent.resetIfPlayerChanged(currentPlayerTurn);
        }
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

        settingsDialog.updateAudioProperties();
        settingsDialog.playerNameProperty().set(localPlayer.getName());

        String status = connectionStatusLabel.getText();
        settingsDialog.setConnectionStatus("Connected".equals(status), status);

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
                updatePlayerColors();
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
     * Disables all game board interactions and hides tooltips/popups.
     * Should be called when the WinScreenDialog is shown.
     */
    private void disableGameBoardInteractions() {
        isTooltipDisabled = true;
        if (timerComponent != null) {
            timerComponent.stop();
        }

        // Remove all event handlers from gameCanvas
        gameCanvas.setOnMousePressed(null);
        gameCanvas.setOnMouseClicked(null);
        gameCanvas.setOnMouseMoved(null);
        gameCanvas.setOnMouseEntered(null);
        gameCanvas.setOnMouseExited(null);
        gameCanvas.setOnKeyPressed(null);

        // Optionally, hide or disable the canvas and overlays
        gameCanvas.setDisable(true);
        if (backgroundCanvas != null)
            backgroundCanvas.setDisable(true);
        if (overlayCanvas != null)
            overlayCanvas.setDisable(true);

        // Disable all card hands
        if (artifactHand != null) {
            artifactHand.setDisable(true);
            // Remove all event handlers from artifact cards
            for (Node card : artifactHand.getChildren()) {
                disableCardInteractions(card);
            }
        }

        if (structureHand != null) {
            structureHand.setDisable(true);
            // Remove all event handlers from structure cards
            for (Node card : structureHand.getChildren()) {
                disableCardInteractions(card);
            }
        }

        // Find and disable all game control buttons in the scene
        Scene scene = gameCanvas.getScene();
        if (scene != null) {
            disableAllButtons(scene.getRoot());
        }

        // Remove event handlers added via addEventHandler (for all event types)
        gameCanvas.removeEventHandler(MouseEvent.MOUSE_PRESSED, e -> handleCanvasClick(e.getX(), e.getY()));
        gameCanvas.removeEventHandler(MouseEvent.MOUSE_CLICKED, e -> {
            if (e.getClickCount() == 2)
                handleCanvasDoubleClick(e.getX(), e.getY());
        });
        gameCanvas.removeEventHandler(MouseEvent.MOUSE_ENTERED_TARGET, e -> handleCanvasEntered(e.getX(), e.getY()));
        gameCanvas.removeEventHandler(MouseEvent.MOUSE_MOVED, e -> handleCanvasMouseMove(e.getX(), e.getY()));
        gameCanvas.removeEventHandler(MouseEvent.MOUSE_EXITED_TARGET, e -> {
            clearHighlight();
            highlightedTile = null;
            cancelTooltipDelay();
            hideTileTooltip();
        });

        // Remove event handlers from parent StackPane if present
        if (gameCanvas.getParent() instanceof StackPane parent) {
            parent.setOnMousePressed(null);
            parent.setOnMouseClicked(null);
            parent.setOnMouseMoved(null);
            parent.setOnMouseEntered(null);
            parent.setOnMouseExited(null);

            parent.removeEventHandler(MouseEvent.MOUSE_PRESSED, ev -> {
                Point2D local = gameCanvas.sceneToLocal(ev.getSceneX(), ev.getSceneY());
                if (local.getX() >= 0 && local.getY() >= 0 && local.getX() <= gameCanvas.getWidth()
                        && local.getY() <= gameCanvas.getHeight()) {
                    handleCanvasClick(local.getX(), local.getY());
                    ev.consume();
                }
            });
            parent.removeEventHandler(MouseEvent.MOUSE_CLICKED, ev -> {
                if (ev.getClickCount() == 2) {
                    Point2D local = gameCanvas.sceneToLocal(ev.getSceneX(), ev.getSceneY());
                    if (local.getX() >= 0 && local.getY() >= 0 && local.getX() <= gameCanvas.getWidth()
                            && local.getY() <= gameCanvas.getHeight()) {
                        handleCanvasDoubleClick(local.getX(), local.getY());
                        ev.consume();
                    }
                }
            });
            parent.removeEventHandler(MouseEvent.MOUSE_MOVED, ev -> {
                Point2D local = gameCanvas.sceneToLocal(ev.getSceneX(), ev.getSceneY());
                if (local.getX() >= 0 && local.getY() >= 0 && local.getX() <= gameCanvas.getWidth()
                        && local.getY() <= gameCanvas.getHeight()) {
                    handleCanvasMouseMove(local.getX(), local.getY());
                    handleTileTooltipHover(local.getX(), local.getY());
                    ev.consume();
                }
            });
            parent.removeEventHandler(MouseEvent.MOUSE_ENTERED_TARGET, ev -> {
                Point2D local = gameCanvas.sceneToLocal(ev.getSceneX(), ev.getSceneY());
                if (local.getX() >= 0 && local.getY() >= 0 && local.getX() <= gameCanvas.getWidth()
                        && local.getY() <= gameCanvas.getHeight()) {
                    handleCanvasEntered(local.getX(), local.getY());
                    ev.consume();
                }
            });
            parent.removeEventHandler(MouseEvent.MOUSE_EXITED_TARGET, ev -> {
                Point2D local = gameCanvas.sceneToLocal(ev.getSceneX(), ev.getSceneY());
                boolean trulyExited = local.getX() < 0 || local.getY() < 0 || local.getX() > gameCanvas.getWidth()
                        || local.getY() > gameCanvas.getHeight();

                if (trulyExited) {
                    clearHighlight();
                    highlightedTile = null;
                    hideTileTooltip();
                }
            });
        }

        clearHighlight();
        highlightedTile = null;
        hideTileTooltip();

        // Hide any visible tile tooltip and cancel tooltip delay
        isTooltipDisabled = true;
        cancelTooltipDelay();
        if (currentTileTooltip != null) {
            currentTileTooltip.close(); // Use the close() method instead of just hiding
            currentTileTooltip = null;
        }

        // Reset tooltip tracking variables
        pendingTooltipCol = -1;
        pendingTooltipRow = -1;
        lastTooltipCol = -1;
        lastTooltipRow = -1;
    }

    /**
     * Disables all interactions for a card
     * 
     * @param card The card node to disable
     */
    private void disableCardInteractions(Node card) {
        card.setDisable(true);
        card.setOnMouseEntered(null);
        card.setOnMouseExited(null);
        card.setOnMouseClicked(null);
        card.setOnDragDetected(null);
        card.setOnMouseDragged(null);

        // Apply visual indication that the card is disabled
        card.setOpacity(0.7);
    }

    /**
     * Recursively disables all Button nodes in the scene graph except those in the chat component
     *
     * @param parent The parent node to search from
     */
    private void disableAllButtons(Node parent) {
        if (parent instanceof Button) {
            // Check if this button is part of the chat component before disabling
            if (!isDescendantOf(parent, chatContainer)) {
                ((Button) parent).setDisable(true);
            }
        } else if (parent instanceof Parent) {
            for (Node child : ((Parent) parent).getChildrenUnmodifiable()) {
                disableAllButtons(child);
            }
        }
    }

    /**
     * Checks if a node is a descendant of another node (directly or indirectly)
     *
     * @param node The node to check
     * @param potentialAncestor The potential ancestor node
     * @return true if node is a descendant of potentialAncestor, false otherwise
     */
    private boolean isDescendantOf(Node node, Node potentialAncestor) {
        if (node == null || potentialAncestor == null) {
            return false;
        }

        Node parent = node.getParent();
        while (parent != null) {
            if (parent == potentialAncestor) {
                return true;
            }
            parent = parent.getParent();
        }
        return false;
    }

    /**
     * Shows the WinScreenDialog when the game ends.
     *
     * @param event The event containing the leaderboard data.
     */
    private void handleEndGame(EndGameEvent event) {
        LOGGER.info("Game has ended. Showing win screen.");
        Platform.runLater(() -> {
            disableGameBoardInteractions();
            WinScreenDialog dialog = new WinScreenDialog(event.getLeaderboard());
            AudioManager.getInstance().playMusic(AudioTracks.Track.UPBEAT_WINSCREEN.getFileName());
            dialog.setOnMenuAction(() -> {
                eventBus.publish(new LeaveLobbyRequestEvent(currentLobbyId));
                sceneManager.switchToScene(SceneManager.SceneType.MAIN_MENU);
                chatComponentController.setInGame(false);
                cleanup();
            });
            dialog.setOnLobbyAction(() -> {
                eventBus.publish(new LeaveLobbyRequestEvent(currentLobbyId));
                sceneManager.switchToScene(SceneManager.SceneType.LOBBY);
                chatComponentController.setInGame(false);
                cleanup();
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
     *
     * @param lobbyId The new lobby ID.
                                 */
    public void setCurrentLobbyId(String lobbyId) {
        this.currentLobbyId = lobbyId;
        chatComponentController.setCurrentLobbyId(lobbyId);
    }

    /**
     * Updates the local player reference and forwards it to the chat UI.
     *
     * @param player The local player.
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
        eventBus.unsubscribe(ArtifactLocationEvent.class, this::handleArtifactLocationEvent);

        playerManager.removePlayerUpdateListener(this::handlePlayerUpdate);

        if (settingsDialog != null)
            settingsDialog.close();
        if (gameCanvas != null) {
            gameCanvas.getParent().removeEventHandler(MouseEvent.MOUSE_PRESSED,
                    e -> handleCanvasClick(e.getX(), e.getY()));
            gameCanvas.setOnKeyPressed(null);
        }
        if (timerComponent != null) {
            timerComponent.stop();
            timerComponent = null;
        }
        if (resourceOverviewDialog != null) {
            resourceOverviewDialog.close();
            resourceOverviewDialog = null;
        }
        if (currentTileTooltip != null) {
            currentTileTooltip.close();
            hideTileTooltip();
            pendingTooltipCol = pendingTooltipRow = lastTooltipRow = lastTooltipCol = -1;
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
     *
     *
     */
    public void toggleGridAdjustmentMode() {
        gridAdjustmentManager.toggleGridAdjustmentMode();
    }

    /**
     * Enables or disables grid‑adjustment mode.
     *
     * @param active true to enable, false to disable.
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
        mapImage = resourceLoader.loadImageSync(ResourceLoader.MAP_IMAGE);
        isMapLoaded = mapImage != null;
        if (isMapLoaded)
            drawMapAndGrid();
        else
            LOGGER.severe("Map image missing");
    }

    /**
     * Installs all mouse / keyboard listeners for the board.
     * After the refactor:
     * – the yellow hover outline is drawn on overlayCanvas via showHighlight()
     * – leaving the canvas just clears overlayCanvas via clearHighlight()
     */
    private void setupCanvasListeners() {

        /*
         * ---------------------------------------------------------------------
         * Resize → redraw the static background (map + white grid)
         * ---------------------------------------------------------------------
         */
        ChangeListener<Number> resize = (obs, o, n) -> drawMapAndGrid();

        gameCanvas.widthProperty().addListener(resize);
        gameCanvas.heightProperty().addListener(resize);

        gameCanvas.setFocusTraversable(true);

        /*
         * ---------------------------------------------------------------------
         * Click & double-click are unchanged – they operate on game state
         * ---------------------------------------------------------------------
         */
        gameCanvas.addEventHandler(MouseEvent.MOUSE_PRESSED, e -> handleCanvasClick(e.getX(), e.getY()));

        gameCanvas.addEventHandler(MouseEvent.MOUSE_CLICKED, e -> {
            if (e.getClickCount() == 2) {
                handleCanvasDoubleClick(e.getX(), e.getY());
            }
        });

        /*
         * ---------------------------------------------------------------------
         * Hover-highlight (overlayCanvas) – uses new helper methods
         * ---------------------------------------------------------------------
         */
        gameCanvas.addEventHandler(MouseEvent.MOUSE_ENTERED_TARGET, e -> handleCanvasEntered(e.getX(), e.getY())); // →
        // showHighlight()

        gameCanvas.addEventHandler(MouseEvent.MOUSE_MOVED, e -> handleCanvasMouseMove(e.getX(), e.getY())); // → show /
        // clear

        gameCanvas.addEventHandler(MouseEvent.MOUSE_EXITED_TARGET, e -> {
            clearHighlight();
            highlightedTile = null;
            cancelTooltipDelay(); // Cancel any pending tooltip
            hideTileTooltip(); // Hide any visible tooltip
        });

        /*
         * ---------------------------------------------------------------------
         * Duplicate listeners on the surrounding StackPane so that
         * events coming from the two overlay canvasses are also handled.
         * ---------------------------------------------------------------------
         */
        if (gameCanvas.getParent() instanceof StackPane parent) {

            parent.addEventHandler(MouseEvent.MOUSE_PRESSED, ev -> {
                Point2D local = gameCanvas.sceneToLocal(ev.getSceneX(), ev.getSceneY());
                if (local.getX() >= 0 && local.getY() >= 0 && local.getX() <= gameCanvas.getWidth()
                        && local.getY() <= gameCanvas.getHeight()) {
                    handleCanvasClick(local.getX(), local.getY());
                    ev.consume();
                }
            });

            parent.addEventHandler(MouseEvent.MOUSE_CLICKED, ev -> {
                if (ev.getClickCount() == 2) {
                    Point2D local = gameCanvas.sceneToLocal(ev.getSceneX(), ev.getSceneY());
                    if (local.getX() >= 0 && local.getY() >= 0 && local.getX() <= gameCanvas.getWidth()
                            && local.getY() <= gameCanvas.getHeight()) {
                        handleCanvasDoubleClick(local.getX(), local.getY());
                        ev.consume();
                    }
                }
            });

            parent.addEventHandler(MouseEvent.MOUSE_MOVED, ev -> {
                Point2D local = gameCanvas.sceneToLocal(ev.getSceneX(), ev.getSceneY());
                if (local.getX() >= 0 && local.getY() >= 0 && local.getX() <= gameCanvas.getWidth()
                        && local.getY() <= gameCanvas.getHeight()) {
                    handleCanvasMouseMove(local.getX(), local.getY());
                    handleTileTooltipHover(local.getX(), local.getY());
                    ev.consume();
                }
            });

            parent.addEventHandler(MouseEvent.MOUSE_ENTERED_TARGET, ev -> {
                Point2D local = gameCanvas.sceneToLocal(ev.getSceneX(), ev.getSceneY());
                if (local.getX() >= 0 && local.getY() >= 0 && local.getX() <= gameCanvas.getWidth()
                        && local.getY() <= gameCanvas.getHeight()) {
                    handleCanvasEntered(local.getX(), local.getY());
                    ev.consume();
                }
            });

            parent.addEventHandler(MouseEvent.MOUSE_EXITED_TARGET, ev -> {
                Point2D local = gameCanvas.sceneToLocal(ev.getSceneX(), ev.getSceneY());
                boolean trulyExited = local.getX() < 0 || local.getY() < 0 || local.getX() > gameCanvas.getWidth()
                        || local.getY() > gameCanvas.getHeight();

                if (trulyExited) {
                    clearHighlight(); // <-- replaces redrawSingleTile()
                    highlightedTile = null;
                    hideTileTooltip();
                }
            });
        }

        /*
         * ---------------------------------------------------------------------
         * Keyboard shortcuts for grid-adjustment
         * ---------------------------------------------------------------------
         */
        gameCanvas.setOnKeyPressed(gridAdjustmentManager::handleGridAdjustmentKeys);
    }

    // --- Tile Tooltip logic ---

    /**
     * Handles mouse movement over the canvas and updates the tile tooltip
     * accordingly.
     *
     * @param px The x-coordinate of the mouse pointer.
     * @param py The y-coordinate of the mouse pointer.
     */
    private void handleTileTooltipHover(double px, double py) {
        if (isTooltipDisabled || gridAdjustmentManager.isGridAdjustmentModeActive() || gameState == null) {
            hideTileTooltip();
            cancelTooltipDelay();
            return;
        }

        int[] tile = getHexAt(px, py);
        if (tile == null) {
            cancelTooltipDelay();
            hideTileTooltip();
            lastTooltipRow = -1;
            lastTooltipCol = -1;
            return;
        }

        int row = tile[0], col = tile[1];
        Tile t = getTile(row, col);
        if (t == null) {
            cancelTooltipDelay();
            hideTileTooltip();
            lastTooltipRow = -1;
            lastTooltipCol = -1;
            return;
        }

        // Return early if we're already showing this tooltip
        if (row == lastTooltipRow && col == lastTooltipCol && currentTileTooltip != null) {
            return;
        }

        // Return early if we're already waiting to show this tooltip
        if (row == pendingTooltipRow && col == pendingTooltipCol && tooltipShowDelay != null) {
            return;
        }

        // Cancel any pending tooltip for a different tile
        cancelTooltipDelay();

        // Set up a new delay for this tile
        pendingTooltipRow = row;
        pendingTooltipCol = col;

        // Create a new tooltip delay
        tooltipShowDelay = new PauseTransition(Duration.millis(500));
        tooltipShowDelay.setOnFinished(e -> {
            // Only show if mouse is still over the same tile
            if (row == pendingTooltipRow && col == pendingTooltipCol) {
                showTileTooltip(t, px, py);
                lastTooltipRow = row;
                lastTooltipCol = col;
            }
        });
        tooltipShowDelay.play();
    }

    /**
     * Cancels any pending tooltip delay.
     */
    private void cancelTooltipDelay() {
        if (tooltipShowDelay != null) {
            tooltipShowDelay.stop();
            tooltipShowDelay = null;
        }
        pendingTooltipRow = -1;
        pendingTooltipCol = -1;
    }

    private void showTileTooltip(Tile tile, double px, double py) {
        // Todo: Adjust if statement
        if (isTooltipDisabled || gameState == null) {
            hideTileTooltip();
            return;
        }
        hideTileTooltip();
        currentTileTooltip = new TileTooltip(tile);

        // Create a popup with proper styling
        Tooltip tooltip = currentTileTooltip.getTooltip();

        // Convert canvas coordinates to screen coordinates
        Point2D screen = gameCanvas.localToScreen(px + 16, py + 16);

        // Show the tooltip at the calculated position - no delay here since we've
        // already delayed
        tooltip.show(gameCanvas.getScene().getWindow(), screen.getX(), screen.getY());
    }

    /**
     * Hides the currently displayed tile tooltip.
     */
    private void hideTileTooltip() {
        if (currentTileTooltip != null) {
            currentTileTooltip.getTooltip().hide();
            currentTileTooltip = null;
        }
    }

    /**
     * Handles a physical mouse click on the canvas – selects the hex and triggers
     * a tile click event. Also handles interactions with placed entities.
     */
    private void handleCanvasClick(double px, double py) {
        if (currentTileTooltip != null) {
            hideTileTooltip();
        }
        int[] tileCoords = getHexAt(px, py);
        if (tileCoords == null)
            return;

        int row = tileCoords[0];
        int col = tileCoords[1];
        eventBus.publish(new TileClickEvent(row, col));

        Tile clickedTile = getTile(row, col);
        if (localPlayer != null && clickedTile != null) {
            // --- Card Placement Logic ---
            if (selectedCard != null) {
                CardDetails cardDetails = getCardDetails(selectedCard.getId());
                if (cardDetails != null && cardDetails.getEntity() != null) {
                    GameEntity entityToPlace = cardDetails.getEntity();
                    if (entityToPlace instanceof Artifact a) {
                        if (a.isFieldTarget()) {
                        eventBus.publish(new UseFieldArtifactUIEvent(col, row, cardDetails.getID()));
                        AudioManager.getInstance().playSoundEffect(AudioTracks.Track.USE_ARTIFACT.getFileName());
                    } else if (a.isPlayerTarget() && clickedTile.getOwner() != null) {
                        // Target the tile owner if the artifact is player-targeted
                        eventBus.publish(new UsePlayerArtifactUIEvent(cardDetails.getID(), clickedTile.getOwner()));
                        AudioManager.getInstance().playSoundEffect(AudioTracks.Track.USE_ARTIFACT.getFileName());
                    } else if (a.isPlayerTarget()) {
                        // If tile has no owner, maybe prompt for player or disallow? For now, log.
                        LOGGER.info("Cannot use player-target artifact on unowned tile.");
                    } else {
                        LOGGER.info("Artifact is neither field nor player target: " + cardDetails.getID());
                    }
                    } else if (!clickedTile.hasEntity()) {
                        if (entityToPlace instanceof Statue && cardDetails.getID() == selectedStatue.getID()) {
                            eventBus.publish(new PlaceStatueUIEvent(col, row, cardDetails.getID()));
                        } else if (entityToPlace.isStructure()) {
                            eventBus.publish(new PlaceStructureUIEvent(col, row, cardDetails.getID()));
                        }
                        AudioManager.getInstance().playSoundEffect(AudioTracks.Track.PLACE_STRUCTURE.getFileName());
                    }
                } else {
                    LOGGER.info("Selected card does not exist or does not represent an entity: "
                            + (cardDetails != null ? cardDetails.getID() : "null"));
                }
                // Deselect card after attempting placement/use
                selectedCard.getStyleClass().remove("selected-card");
                selectedCard = null;
                updateCardImages(); // Refresh card visuals (e.g., affordability)
                return; // Prevent further interaction processing after card placement attempt
            }

            // --- Entity Interaction Logic ---
            if (clickedTile.hasEntity() && clickedTile.getOwner() != null
                    && clickedTile.getOwner().equals(localPlayer.getName())) {
                GameEntity entity = clickedTile.getEntity();
                int entityId = entity.getId();

                // Check if it's a structure (e.g., Rune Table ID 1)
                if (entity.isStructure() && !entity.isStatue()) {
                    // Example: Rune Table interaction (if needed)
                    if (entityId == 1) {
                        // Potentially open a specific popup or trigger default action
                        // For now, let double-click handle Rune Table usage
                        LOGGER.fine("Clicked owned Rune Table (ID 1). Double-click to use.");
                    }
                    // Add other structure interactions here if needed
                }
                // Check if it's a statue
                else if (entity instanceof Statue statue) {
                    ContextMenu contextMenu = new ContextMenu();

                    // --- Jörmungandr (ID 30) Specific Interactions ---
                    if (entityId == 30) {
                        // Add menu items for Jörmungandr interactions
                        MenuItem upgradeItem = new MenuItem("Upgrade Statue");
                        upgradeItem.setOnAction(e -> handleJormungandrUpgrade(clickedTile, statue));
                        contextMenu.getItems().add(upgradeItem);

                        // Add "Make Deal" option if the statue is upgraded to level 2 or higher
                        if (statue.getLevel() >= 2) {
                            MenuItem dealItem = new MenuItem("Make a Deal");
                            dealItem.setOnAction(e -> initiateJormungandrDeal(clickedTile));
                            contextMenu.getItems().add(dealItem);
                        }

                        // Add info menu item
                        MenuItem infoItem = new MenuItem("Statue Info");
                        infoItem.setOnAction(e -> showJormungandrInfo(statue));
                        contextMenu.getItems().add(infoItem);
                    }
                    // --- Add other statue interactions here using else if (entityId == ...) ---
                    // else if (entityId == 31) { /* Freyr interactions */ }

                    // Show context menu if it has items
                    if (!contextMenu.getItems().isEmpty()) {
                        // Calculate position for the context menu
                        double screenX = gameCanvas.localToScreen(px, py).getX();
                        double screenY = gameCanvas.localToScreen(px, py).getY();
                        contextMenu.show(gameCanvas, screenX, screenY);
                    }
                }
            }
        }
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
                AudioManager.getInstance().playSoundEffect(AudioTracks.Track.BUY_TILE.getFileName());
            } else {
                showNotification("Not enough runes to buy this tile (Cost: " + price + ").");
            }
        } else if (localPlayer != null && ownerId.equals(localPlayer.getName())) {
            if (getTile(row, col) != null) {
                Tile t = getTile(row, col);
                if (t.hasEntity() && t.getEntity().getId() == 1) {
                    eventBus.publish(new UseStructureUIEvent(col, row, t.getEntity().getId()));
                    AudioManager.getInstance().playSoundEffect(AudioTracks.Track.USE_STRUCTURE.getFileName());
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
     * Handles mouse entering the canvas – shows the highlight for the tile under
     * the mouse.
     */
    private void handleCanvasEntered(double px, double py) {
        int[] t = getHexAt(px, py);
        if (t != null)
            showHighlight(t[0], t[1]);
    }

    /**
     * Handles mouse movement over the canvas – shows the highlight for the tile
     * under the mouse.
     */
    private void handleCanvasMouseMove(double px, double py) {
        int[] t = getHexAt(px, py);
        if (t != null) {
            showHighlight(t[0], t[1]);
        } else {
            clearHighlight();
        }
    }

    /**
     * Repaints only the backgroundCanvas (map + white grid).
     */
    void drawMapAndGrid() {
        if (!isMapLoaded || backgroundCanvas == null)
            return;

        double cW = backgroundCanvas.getWidth();
        double cH = backgroundCanvas.getHeight();
        if (cW <= 0 || cH <= 0)
            return;

        GraphicsContext gc = backgroundCanvas.getGraphicsContext2D();
        gc.clearRect(0, 0, cW, cH);

        // ----------- draw map image --------------------------------------------
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

        // ----------- hex geometry ----------------------------------------------
        double gridW = scaledMapWidth * gridAdjustmentManager.getGridWidthPercentage();
        double gridH = scaledMapHeight * gridAdjustmentManager.getGridHeightPercentage();

        double hLimit = gridW / ((HEX_COLS - 1) * 0.75 + 1);
        double vLimit = gridH / ((HEX_ROWS - 0.5) * 0.866 * 2);
        effectiveHexSize = Math.min(hLimit, vLimit) * 0.5 * gridAdjustmentManager.getGridScaleFactor();

        hSpacing = effectiveHexSize * gridAdjustmentManager.getHorizontalSpacingFactor();
        vSpacing = effectiveHexSize * gridAdjustmentManager.getVerticalSpacingFactor();

        double addHX = gridAdjustmentManager.getGridHorizontalOffset() * scaledMapWidth;
        double addHY = gridAdjustmentManager.getGridVerticalOffset() * scaledMapHeight;

        double totalW = hSpacing * (HEX_COLS - 0.5);
        double totalH = vSpacing * HEX_ROWS;

        double baseX = mapOffsetX + (scaledMapWidth - gridW) / 2;
        double baseY = mapOffsetY + (scaledMapHeight - gridH) / 2;
        gridOffsetX = baseX + (gridW - totalW) / 2 + addHX;
        gridOffsetY = baseY + (gridH - totalH) / 2 + addHY;

        // ----------- draw white grid -------------------------------------------
        gc.setStroke(Color.WHITE);
        gc.setLineWidth(1.5);
        gc.setGlobalAlpha(0.7);

        for (int r = 0; r < HEX_ROWS; r++) {
            for (int c = 0; c < HEX_COLS; c++) {
                double cx = gridOffsetX + c * hSpacing + (r % 2) * (hSpacing / 2);
                double cy = gridOffsetY + r * vSpacing;
                drawHexBackground(gc, cx, cy, effectiveHexSize, r, c);
            }
        }

        gc.setGlobalAlpha(1);

        redrawEntities();
    }

    /**
     * Clears the overlay canvas.
     */
    private void clearHighlight() {
        if (overlayCanvas != null) {
            GraphicsContext g = overlayCanvas.getGraphicsContext2D();
            g.clearRect(0, 0, overlayCanvas.getWidth(), overlayCanvas.getHeight());
        }
    }

    /**
     * Draws the yellow outline for the given tile on the overlay canvas.
     */
    private void showHighlight(int row, int col) {
        if (overlayCanvas == null || effectiveHexSize <= 0)
            return;

        GraphicsContext gc = overlayCanvas.getGraphicsContext2D();
        gc.clearRect(0, 0, overlayCanvas.getWidth(), overlayCanvas.getHeight());

        double cx = gridOffsetX + col * hSpacing + (row % 2) * (hSpacing / 2);
        double cy = gridOffsetY + row * vSpacing;

        double rot = Math.toRadians(gridAdjustmentManager.getHexRotationDegrees());
        double hSquish = gridAdjustmentManager.getHorizontalSquishFactor();
        double vSquish = gridAdjustmentManager.getVerticalSquishFactor();

        double[] xs = new double[6];
        double[] ys = new double[6];
        for (int i = 0; i < 6; i++) {
            double a = rot + 2 * Math.PI / 6 * i;
            xs[i] = cx + effectiveHexSize * Math.cos(a) * hSquish;
            ys[i] = cy + effectiveHexSize * Math.sin(a) * vSquish;
        }

        gc.setStroke(Color.YELLOW);
        gc.setLineWidth(3);
        gc.setGlobalAlpha(1);
        gc.strokePolygon(xs, ys, 6);
    }

    /**
     * Draws a single hexagon, optionally highlighting ownership and selection.
     * Also draws the entity image if present.
     */
    private void drawHex(GraphicsContext gc, double cx, double cy, double size, int row, int col, boolean selected,
            boolean withEntity) {
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
        if (!withEntity) {
            return;
        }

        Tile tile = getTile(row, col);

        if (tile == null) {
            LOGGER.warning("Tile is null for row " + row + ", col " + col);
            return;
        }

        GameEntity entity = tile.getEntity();
        if (entity != null && entity.getId() != 8) { // is an active Trap and should not get shown
            int id = tile.getEntity().getId();
            drawEntityImage(gc, cx, cy, size, gridAdjustmentManager.getHorizontalSquishFactor(), id);
        }
    }

    /**
     * Draws an entity image centered in a hex tile.
     * The image is scaled to fit the hex width while preserving its aspect ratio.
     * Use EntityRegistry.getURL(isCard=true) for loading the image with a red
     * placeholder if missing.
     *
     * @param gc       The graphics context to draw on
     * @param centerX  The x-coordinate of the hex center
     * @param centerY  The y-coordinate of the hex center
     * @param hexSize  The size of the hex
     * @param hSquish  The horizontal squish factor
     * @param entityId The ID of the entity being drawn (for logging)
     */
    private void drawEntityImage(GraphicsContext gc, double centerX, double centerY, double hexSize,
            double hSquish, int entityId) {
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

        // if (imageUrl == null || imageUrl.isEmpty()) {
        // // Log ERROR for missing URL
        // LOGGER.severe(String.format("Missing map image URL for entity ID %d. Drawing
        // red placeholder.", entityId));
        // drawPlaceholder.run();
        // return;
        // }

        String imageUrl = EntityRegistry.getURL(entityId, false);
        GameEntity gm = EntityRegistry.getGameEntityOriginalById(entityId);

        try {
            Image image = getEntityImage(entityId);
            if (image == null || image.isError()) {
                // Log ERROR for image loading failure
                LOGGER.severe(
                        String.format("Failed to load map entity image: %s (Entity ID: %d). Drawing red placeholder.",
                                imageUrl, entityId));
                drawPlaceholder.run();
                return;
            }

            // Save current graphics state
            double oldAlpha = gc.getGlobalAlpha();
            gc.setGlobalAlpha(1.0); // Full opacity for the image

            if (gm instanceof Monument) {
                // Calculate maximum width based on hex size and squish factor
                double maxWidth = (double) SETTINGS.Config.MONUMENT_SIZE.getValue() / 10 * hexSize * hSquish;

                // Calculate scale to fit within both max width and max height
                double scale = maxWidth / image.getWidth();

                // Calculate scaled dimensions
                double scaledWidth = image.getWidth() * scale;
                double scaledHeight = image.getHeight() * scale;

                gc.drawImage(image, centerX - scaledWidth / 2, centerY - 3 * scaledHeight / 4, scaledWidth,
                        scaledHeight);
            } else {
                // Calculate maximum width based on hex size and squish factor
                double maxWidth = (double) SETTINGS.Config.ENTITY_SIZE.getValue() / 10 * hexSize * hSquish;

                // Calculate scale to fit within both max width and max height
                double scale = maxWidth / image.getWidth();

                // Calculate scaled dimensions
                double scaledWidth = image.getWidth() * scale;
                double scaledHeight = image.getHeight() * scale;

                // Draw image centered in the hex
                gc.drawImage(image, centerX - scaledWidth / 2, centerY - 2 * scaledHeight / 3, scaledWidth,
                        scaledHeight);
            }

            // Restore graphics state
            gc.setGlobalAlpha(oldAlpha);
        } catch (Exception e) {
            // Log ERROR for any other exception during drawing
            LOGGER.log(Level.SEVERE,
                    String.format("Error drawing map entity image for ID %d: %s", entityId, e.getMessage()), e);
            drawPlaceholder.run();
        }
    }

    /**
     * Draws the hex background (white outline) for the given tile.
     */
    private void drawHexBackground(GraphicsContext gc, double cx, double cy, double size, int row, int col) {
        drawHex(gc, cx, cy, size, row, col, false, false);
    }

    /**
     * Draws the hex sprite (entity image) for the given tile.
     */
    private void drawHexSprite(GraphicsContext gc, double cx, double cy, double size, int row, int col) {
        drawHex(gc, cx, cy, size, row, col, false, true);
    }

    /**
     * Redraws all entities on the game canvas.
     */
    private void redrawEntities() {
        if (gameCanvas == null || gameState == null || gameState.getBoardManager() == null) {
            return;
        }
        GraphicsContext gc = gameCanvas.getGraphicsContext2D();
        gc.clearRect(0, 0, gameCanvas.getWidth(), gameCanvas.getHeight());

        for (int r = 0; r < HEX_ROWS; r++) {
            for (int c = 0; c < HEX_COLS; c++) {
                Tile tile = getTile(r, c);
                if (tile != null && tile.hasEntity()) {
                    double cx = gridOffsetX + c * hSpacing + (r % 2) * (hSpacing / 2.0);
                    double cy = gridOffsetY + r * vSpacing;
                    drawHexSprite(gc, cx, cy, effectiveHexSize, r, c);
                }
            }
        }

        // Draw artifact location indicator
        if (locatedArtifactScreenCoords != null && locatedArtifactId != -1) { // Add this block
            drawArtifactLocationIndicator(gc, locatedArtifactScreenCoords.getX(), locatedArtifactScreenCoords.getY());
        }

        // Draw Trap location indicator
        if (trapLocationScreenCoords != null) {
            drawTrapLocationIndicator(gc, trapLocationScreenCoords.getX(), trapLocationScreenCoords.getY());
        }
    }

    /**
     * Draws the artifact location indicator on the game canvas.
     */
    private Image getEntityImage(int entityId) {
        String url = EntityRegistry.getURL(entityId, false);
        return resourceLoader.loadImageAsync(url, this::redrawEntities);
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
            if (((ys[i] > py) != (ys[j] > py)) && (px < (xs[j] - xs[i]) * (py - ys[i]) / (ys[j] - ys[i]) + xs[i])) {
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
        String status = connectionStatusLabel.getText();
        settingsDialog.setConnectionStatus("Connected".equals(status), status);
    }

    /**
     * Handles the save button inside the settings dialog.
     */
    private void handleSettingsSave() {
        String requestedName = settingsDialog.playerNameProperty().get();

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
    }

    /*
     * --------------------------------------------------
     * Card tooltip & tile choose handling
     * --------------------------------------------------
     */

    /**
     * Toggle card selection (golden frame) when clicked.
     *
     * @param event The mouse event triggered by the click.
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
            AudioManager.getInstance().playSoundEffect(AudioTracks.Track.SELECT_CARD.getFileName());
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
     *
     * @param event The mouse event triggered by the hover.
     *
     */
    @FXML
    public void handleCardMouseEntered(MouseEvent event) {
        Node card = (Node) event.getSource();
        Tooltip tooltip = createTooltipForCard(card); // Always create a new tooltip
        Tooltip.install(card, tooltip);
        event.consume();
    }

    /**
     * Hides the tooltip once the mouse exits the card.
     *
     * @param event The mouse event triggered by the exit.
     */
    @FXML
    public void handleCardMouseExited(MouseEvent event) {
        Node card = (Node) event.getSource();
        Tooltip.uninstall(card, null); // Uninstall any tooltip
        event.consume();
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
        tooltip.setShowDuration(Duration.INDEFINITE);

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
        content.setMaxWidth(400);
        content.getStyleClass().add("tooltip-content");

        // Only add components with actual content
        if (details.getTitle() != null && !details.getTitle().isEmpty()) {
            Label titleLabel = new Label(details.getTitle());
            titleLabel.getStyleClass().add("tooltip-title");
            titleLabel.setWrapText(true);
            content.getChildren().add(titleLabel);

            // Only add separator if the next section has content
            if ((details.getDescription() != null && !details.getDescription().isEmpty())
                    || (details.getLore() != null && !details.getLore().isEmpty()) || details.getPrice() > 0) {
                content.getChildren().add(new Separator());
            }
        }

        if (details.getDescription() != null && !details.getDescription().isEmpty()) {
            Label descLabel = new Label(details.getDescription());
            descLabel.getStyleClass().add("tooltip-description");
            descLabel.setWrapText(true);
            content.getChildren().add(descLabel);

            // Only add separator if the next section has content
            if ((details.getLore() != null && !details.getLore().isEmpty()) || details.getPrice() > 0) {
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
        tooltip.setMaxWidth(400);
        tooltip.setMaxHeight(300);
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
                if (artifacts == null || listIndex >= artifacts.size() || artifacts.get(listIndex) == null) {
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
                updateArtifactCard(card, getCardDetails(card.getId()));
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
     * If the round number reported by the server changed we are at the start of a
     * brand-new round.
     * All cards must therefore be re-evaluated (price buffs may have expired, a
     * statue could have been sold, …) and the “unaffordable” overlay has to be
     * recalculated from scratch.
     */
    private void detectRoundChangeAndRefresh() {
        if (gameState == null) {
            return;
        }
        int currentRound = gameState.getGameRound(); // <- this is already sent by the server
        if (currentRound != lastKnownRound) {
            lastKnownRound = currentRound;
            LOGGER.info("↻ New round " + currentRound + " detected – resetting card states");

            Platform.runLater(() -> { // always touch the scene graph on the FX thread
                /* 1. clear every temporary style */
                structureHand.getChildren().forEach(node -> {
                    node.getStyleClass().removeAll("selected-card", "unaffordable-card");
                    node.setDisable(false);
                });

                /* 2. rebuild price modifiers and re-evaluate affordability */
                refreshCardAffordability(); // cosmetic (shows/hides grey filter)
                updatePurchasableStates(); // enables / disables & adds CSS class
            });
        }
    }

    /**
     * Centralized method to update the visual state (enabled/disabled,
     * affordable/unaffordable style)
     * of all purchasable structures based on the current player's runes.
     */
    private void updatePurchasableStates() {
        if (gamePlayer == null) {
            LOGGER.warning("Cannot update purchasable states: gamePlayer is null.");
            // Disable all structure cards if gamePlayer is not available
            if (structureHand != null) {
                Platform.runLater(() -> {
                    structureHand.getChildren().forEach(node -> {
                        if (node instanceof Pane) {
                            Pane cardPane = (Pane) node;
                            cardPane.getStyleClass().remove("selected-card"); // Ensure deselected
                            cardPane.getStyleClass().add("unaffordable-card");
                            cardPane.setDisable(true);
                        }
                    });
                });
            }
            // TODO: Handle artifact cards similarly if they are purchasable from a hand
            return;
        }

        int currentRunes = gamePlayer.getRunes();
        LOGGER.fine("Updating purchasable states with current runes: " + currentRunes);

        // Update Structures in structureHand
        if (structureHand != null) {
            Platform.runLater(() -> { // Ensure all UI updates are on the JavaFX Application Thread
                for (Node cardNode : structureHand.getChildren()) {
                    if (cardNode instanceof Pane) {
                        Pane cardPane = (Pane) cardNode;
                        Object userData = cardPane.getUserData();

                        if (userData instanceof CardDetails) {
                            CardDetails cardDetails = (CardDetails) userData;
                            GameEntity entity = cardDetails.getEntity();

                            // Check if it's a purchasable entity and has a cost
                            // Adjust 'Purchasable.class' to your actual base class or interface for items
                            // with a cost
                            if (entity instanceof PurchasableEntity) {
                                PurchasableEntity purchasableEntity = (PurchasableEntity) entity;

                                int cost = purchasableEntity.getPrice();
                                boolean affordable = currentRunes >= cost;

                                if (affordable) {
                                    cardPane.getStyleClass().remove("unaffordable-card");
                                    cardPane.setDisable(false);
                                } else {
                                    // If it becomes unaffordable, also remove 'selected-card' style if present
                                    if (cardPane.getStyleClass().contains("selected-card")) {
                                        cardPane.getStyleClass().remove("selected-card");
                                        if (selectedCard == cardPane) { // also clear the controller's selection state
                                            selectedCard = null;
                                            // TODO: Potentially notify other parts of UI that selection changed
                                        }
                                    }
                                    cardPane.getStyleClass().add("unaffordable-card");
                                    cardPane.setDisable(true);
                                }
                                LOGGER.finer("Structure " + entity.getName() + " (cost: " + cost + ") affordable: "
                                        + affordable + ". Classes: " + cardPane.getStyleClass());
                            } else {
                                // Not a known purchasable entity with a cost, ensure it's enabled and not
                                // styled as unaffordable.
                                cardPane.getStyleClass().remove("unaffordable-card");
                                cardPane.setDisable(false);
                            }
                        } else if (cardPane.isVisible() && cardPane.getUserData() == null) {
                            // If a card pane is visible but has no CardDetails, treat as
                            // unaffordable/disabled
                            LOGGER.warning("Visible structure card pane missing CardDetails in userData: "
                                    + cardPane.getId() + ". Marking as unaffordable.");
                            cardPane.getStyleClass().remove("selected-card");
                            cardPane.getStyleClass().add("unaffordable-card");
                            cardPane.setDisable(true);
                        }
                        // If cardPane is not visible, or userData is not CardDetails but card is not
                        // visible, do nothing.
                    }
                }
            });
        }

        // TODO: If artifactHand contains purchasable artifacts, apply similar logic:
        // if (artifactHand != null) { ... }
    }

    /**
     * Refreshes the affordability of all cards in the structure hand.
     * This is called when the game state changes (e.g., when the player
     * gains or loses runes).
     */
    private void refreshCardAffordability() {
        for (Node card : structureHand.getChildren()) {
            if (card.getId() != null) {
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

            pane.setUserData(details);

            if (imageUrl != null && !imageUrl.isEmpty()) {
                Image image = resourceLoader.getCardImage(details.getID());

                if (image != null && !image.isError()) {
                    ImageView imageView = new ImageView(image);
                    imageView.setPreserveRatio(true);
                    imageView.setFitWidth(75); // Slightly smaller than pane for border
                    imageView.setFitHeight(115);

                    // Center image in pane using a StackPane wrapper
                    StackPane wrapper = new StackPane(imageView);
                    wrapper.setPrefSize(75, 115); // Match ImageView size
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
     * Updates a single card in the artifact hand with the correct image using
     * getCardDetails
     * (isCard=true).
     * Uses a cached red placeholder on failure.
     *
     * @param card The card node (Pane) to update.
     */
    private void updateArtifactCard(Node card, CardDetails details) {
        if (card instanceof Pane pane) {
            pane.getChildren().clear();
            pane.setUserData(details);

            try {
                // Load the statue image first
                String imageUrl = details.getImageUrl();
                if (imageUrl != null && !imageUrl.isEmpty()) {
                    Image image = resourceLoader.getCardImage(details.getID());
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

                // Update tooltip
                Tooltip tooltip = createTooltipForCard(card);
                Tooltip.install(pane, tooltip);

            } catch (Exception e) {
                LOGGER.severe("Error updating statue card: " + e.getMessage());
                addPlaceholderToPane(pane);
            }
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
            pane.setUserData(details);

            try {
                // Load the statue image first
                String imageUrl = details.getImageUrl();
                if (imageUrl != null && !imageUrl.isEmpty()) {
                    Image image = resourceLoader.getCardImage(details.getID());
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

            } catch (Exception e) {
                LOGGER.severe("Error updating statue card: " + e.getMessage());
                addPlaceholderToPane(pane);
            }
        }
    }

    /**
     * Marks that a statue has been placed and disables the statue card.
     *
     * @param hasPlacedStatue true if the statue has been placed, false otherwise.
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

    /**
     * Updates the runes and energy bar display.
     * This method is called when the game state changes.
     *
     *
     */
    public void updateRunesAndEnergyBar() {
        if (gamePlayer != null) {
            runesLabel.setText(gamePlayer.getRunes() + "");
            energyBar.setProgress((double) gamePlayer.getEnergy() / SETTINGS.Config.MAX_ENERGY.getValue());
        } else {
            runesLabel.setText("0");
            energyBar.setProgress(0.0);
        }
        // Re-evaluate cards immediately when my rune total changed
        Platform.runLater(this::updatePurchasableStates);
    }

    /**
     * Updates the map and grid display.
     * This method is called when the game state changes.
     */
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

    /*
    * getter for the tile at the given row and column
    *
    * @param row the row of the tile
    * @param col the column of the tile
     */
    private Tile getTile(int row, int col) {
        return gameState.getBoardManager().getTile(col, row);
    }

    /*
     * getter for the tile at the given row and column
     *
     * @param row the row of the tile
     * @param col the column of the tile
     */
    private boolean isTileOwnedByPlayer(int row, int col) {
        return getTile(row, col).hasEntity();
    }

    /**
     * Checks if the player can afford a card based on its ID.
     *
     * @param cardId The ID of the card to check.
     * @return true if the player can afford the card, false otherwise.
     */
    private boolean canAffordCard(String cardId) {
        int cost = getCardCost(cardId);

        double priceModifier = gamePlayer.getStatus().get(Status.BuffType.SHOP_PRICE);
        double adjusted = cost / Math.max(priceModifier, 0.5); // Prevent divide-by-zero or negative scaling and ensure
        // maximum price of 200% original
        int adjustedPrice = Math.max(0, (int) Math.round(adjusted)); // Ensure price is never negative

        return getPlayerRunes() >= adjustedPrice;
    }

    /**
     * Gets the cost of a card based on its ID.
     *
     * @param cardId The ID of the card to check.
     * @return The cost of the card.
     */
    private int getCardCost(String cardId) {
        CardDetails details = getCardDetails(cardId);
        return details.getPrice();
    }

    /**
     * getter for the player runes
     *
     * @return the player runes
     */
    private int getPlayerRunes() {
        return gamePlayer.getRunes();
    }

    /**
     * getter for the owner of the tile at the given row and column
     *
     * @param row the row of the tile
     *            @param col the column of the tile
     */
    private String getTileOwnerId(int row, int col) {
        Tile tile = getTile(row, col);
        return tile == null ? null : tile.getOwner();
    }

    /**
     * getter for the price of the tile at the given row and column
     *
     * @param row the row of the tile
     * @param col the column of the tile
     */
    private int getTilePrice(int row, int col) {
        Tile tile = getTile(row, col);
        return tile != null ? tile.getPrice() : 0;
    }

    /**
     * Counts the number of structures owned by a player.
     *
     * @param playerId The ID of the player.
     * @return The number of structures owned by the player.
     */
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
        resourceOverviewDialog.updatePlayers(gameState.getPlayers(), gameState.getPlayerTurn(), playerColors);
        showDialogAsOverlay(resourceOverviewDialog, root);
    }

    /**
     * Handles the end turn button click.
     */
    @FXML
    private void handleEndTurn() {
        eventBus.publish(new EndTurnRequestEvent(localPlayer.getName()));
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

    /**
     * Initiates the "Make Deal" interaction for the Jörmungandr statue.
     * Prompts the user to select a target player and confirms the action.
     *
     * @param tile The tile containing the Jörmungandr statue.
     */
    private void initiateJormungandrDeal(Tile tile) {
        if (gameState == null || localPlayer == null)
            return;

        // Get list of other players
        List<String> otherPlayerNames = gameState.getPlayers().stream()
                .map(Player::getName)
                .filter(name -> !name.equals(localPlayer.getName()))
                .toList();

        if (otherPlayerNames.isEmpty()) {
            showNotification("No other players to target.");
            return;
        }

        // First validation - Check if player has at least one structure to sacrifice
        int structureCount = countPlayerStructures(localPlayer.getName());
        if (structureCount == 0) {
            showNotification("You don't have any structures to sacrifice for the deal.");
            return;
        }

        // Player Selection Dialog
        ChoiceDialog<String> dialog = new ChoiceDialog<>(otherPlayerNames.get(0), otherPlayerNames);
        dialog.setTitle("Jörmungandr's Deal");
        dialog.setHeaderText("Choose a player to target.");
        dialog.setContentText("Target Player:");

        Optional<String> result = dialog.showAndWait();

        result.ifPresent(selectedPlayerName -> {
            // Check if target player has structures
            int targetStructureCount = countPlayerStructures(selectedPlayerName);
            if (targetStructureCount == 0) {
                showNotification("Target player has no structures to destroy.");
                return;
            }

            // Create a StatueDetailsWrapper for Jörmungandr
            GameEntity jormungandr = tile.getEntity();
            if (!(jormungandr instanceof Statue)) {
                LOGGER.severe("Entity is not a Statue: " + jormungandr);
                return;
            }

            // Create custom description for Jörmungandr's deal
            String description = "This will destroy 1 random structure of " + selectedPlayerName +
                    " and sacrifice 1 random structure of your own.\n\nProceed?";

            // Use StatueConfirmationDialog instead of Alert
            showJormungandrDealConfirmation((Statue) jormungandr, selectedPlayerName, tile, description);
        });
    }

    /**
     * Shows a confirmation dialog for Jörmungandr's deal using the
     * StatueConfirmationDialog component.
     *
     * @param statue           The Jörmungandr statue
     * @param targetPlayerName The name of the target player
     * @param tile             The tile containing the statue
     * @param description      Description of the deal
     */
    private void showJormungandrDealConfirmation(Statue statue, String targetPlayerName, Tile tile,
            String description) {
        // Initialize the dialog if not already done
        if (statueConfirmationDialog == null) {
            statueConfirmationDialog = new StatueConfirmationDialog(resourceLoader);
        }

        // Configure the confirmation dialog for Jörmungandr's deal
        statueConfirmationDialog
                .withTitle("Confirm Jörmungandr's Deal")
                .withDescription(description)
                .withConfirmButtonText("Make Deal")
                .withoutCost(); // No direct cost since we sacrifice a structure

        // Show the dialog and handle the result
        Window window = gameCanvas.getScene().getWindow();
        statueConfirmationDialog.setOnShown(event -> {
            // Center the dialog
            double centerX = window.getX() + (window.getWidth() / 2) - (statueConfirmationDialog.getWidth() / 2);
            double centerY = window.getY() + (window.getHeight() / 2) - (statueConfirmationDialog.getHeight() / 2);
            statueConfirmationDialog.setX(centerX);
            statueConfirmationDialog.setY(centerY);
        });

        // Set the callback for when the user makes a decision
        statueConfirmationDialog.resultCallback = confirmed -> {
            if (confirmed) {
                // Second validation - Check if conditions are still valid right before
                // execution
                if (!validateJormungandrDealRequirements(localPlayer.getName(), targetPlayerName)) {
                    return;
                }

                // Execute the deal since validation passed
                executeJormungandrDeal(tile, targetPlayerName);
            } else {
                LOGGER.info("Jörmungandr Deal cancelled.");
            }
        };

        // Show the dialog
        statueConfirmationDialog.show(window);
    }

    /**
     * Validates that both the player and target still have structures available for
     * the deal.
     *
     * @param playerName       The name of the player making the deal
     * @param targetPlayerName The name of the target player
     * @return true if both players have structures, false otherwise
     */
    private boolean validateJormungandrDealRequirements(String playerName, String targetPlayerName) {
        // Check if player still has structures to sacrifice
        int playerStructureCount = countPlayerStructures(playerName);
        if (playerStructureCount == 0) {
            showNotification("Cannot complete deal: You no longer have structures to sacrifice.");
            return false;
        }

        // Check if target player still has structures to destroy
        int targetStructureCount = countPlayerStructures(targetPlayerName);
        if (targetStructureCount == 0) {
            showNotification("Cannot complete deal: Target player no longer has structures to destroy.");
            return false;
        }

        return true;
    }

    /**
     * Executes the Jörmungandr deal after all validations have passed.
     *
     * @param tile             The tile containing the Jörmungandr statue
     * @param targetPlayerName The name of the target player
     */
    private void executeJormungandrDeal(Tile tile, String targetPlayerName) {
        try {
            LOGGER.info("Executing Jörmungandr Deal against " + targetPlayerName);

            // Create parameters for the deal (target player)
            String params = "player:" + targetPlayerName;

            // Send UseStatueUIEvent with the parameters
            eventBus.publish(new UseStatueUIEvent(tile.getX(), tile.getY(), tile.getEntity().getId(), params));

            showNotification("Jörmungandr's deal initiated against " + targetPlayerName + ".");
        } catch (Exception e) {
            LOGGER.severe("Error executing Jörmungandr deal: " + e.getMessage());
            showNotification("Error executing deal. Please try again.");
        }
    }

    /**
     * Handles the placement of a statue on a tile.
     *
     * @param tile        The target tile
     * @param statue      The statue entity to place
     * @param cardDetails The card details of the statue
     * @param row         The row index of the tile
     * @param col         The column index of the tile
     */
    private void handleStatuePlacement(Tile tile, Statue statue, CardDetails cardDetails, int row, int col) {
        // Check if player already has a statue
        if (hasPlacedStatue) {
            showNotification("You can only place one statue in the world.");
            return;
        }

        // Check if player has enough resources
        if (!canAffordCard("statue")) {
            showNotification("Not enough runes to place this statue.");
            return;
        }

        // Handle specific statue placement logic
        if (statue.getId() == 30) {
            // Initialize the dialog if not already done
            if (statueConfirmationDialog == null) {
                statueConfirmationDialog = new StatueConfirmationDialog(resourceLoader);
            }

            // Create a wrapper for Jörmungandr to use with StatueConfirmationDialog
            // This is a simplified version since we don't have access to the full
            // StatueDetailsWrapper
            Image statueImage = resourceLoader.getCardImage(statue.getId());

            Window window = gameCanvas.getScene().getWindow();

            // Configure the confirmation dialog
            statueConfirmationDialog
                    .withTitle("Place Jörmungandr")
                    .withDescription("Place Jörmungandr at (" + col + ", " + row + ")?\n\n" +
                            "Jörmungandr represents inevitable destruction and rebirth. " +
                            "Once upgraded, you can make deals to destroy enemy structures.")
                    .withCost("Cost: " + cardDetails.getPrice() + " runes")
                    .withConfirmButtonText("Place Statue");

            // Show the dialog and handle the result
            statueConfirmationDialog.setOnShown(event -> {
                // Center the dialog
                double centerX = window.getX() + (window.getWidth() / 2) - (statueConfirmationDialog.getWidth() / 2);
                double centerY = window.getY() + (window.getHeight() / 2) - (statueConfirmationDialog.getHeight() / 2);
                statueConfirmationDialog.setX(centerX);
                statueConfirmationDialog.setY(centerY);
            });

            // Set the callback for when the user makes a decision
            statueConfirmationDialog.resultCallback = confirmed -> {
                if (confirmed) {
                    // Place the statue
                    // ToDo: Implement the actual placement logic
                    showNotification("Jörmungandr placed at (" + col + ", " + row + ").");
                }
            };

            // Show the dialog
            statueConfirmationDialog.show(window);
        } else {
            // Logic for other statues
            // ToDo: Implement other statue placement logic
            showNotification(statue.getName() + " placed at (" + col + ", " + row + ").");
        }
    }

    /**
     * Handles the upgrade action for Jörmungandr statue.
     *
     * @param tile   The tile containing the statue
     * @param statue The statue entity
     */
    private void handleJormungandrUpgrade(Tile tile, Statue statue) {
        // Calculate upgrade cost - use the upgradePrice from the statue entity
        int upgradeCost = statue.getPrice(); // Use default price if upgrade price is not available

        // Check if player has enough runes
        if (gamePlayer.getRunes() < upgradeCost) {
            showNotification("Not enough runes to upgrade Jörmungandr. Required: " + upgradeCost);
            return;
        }

        // Initialize the dialog if not already done
        if (statueConfirmationDialog == null) {
            statueConfirmationDialog = new StatueConfirmationDialog(resourceLoader);
        }

        String nextLevelEffect;
        if (statue.getLevel() == 1) {
            nextLevelEffect = "At Level 2, you can make a deal: Destroy 1 random structure of a chosen player in exchange for sacrificing 1 of your own structures.";
        } else {
            nextLevelEffect = "Maximum level reached";
        }

        // Configure the confirmation dialog
        statueConfirmationDialog
                .withTitle("Upgrade Jörmungandr")
                .withDescription("Upgrade Jörmungandr to Level " + (statue.getLevel() + 1) + "?\n\n" + nextLevelEffect)
                .withCost("Cost: " + upgradeCost + " runes")
                .withConfirmButtonText("Upgrade");

        Window window = gameCanvas.getScene().getWindow();

        // Show the dialog and handle the result
        statueConfirmationDialog.setOnShown(event -> {
            // Center the dialog
            double centerX = window.getX() + (window.getWidth() / 2) - (statueConfirmationDialog.getWidth() / 2);
            double centerY = window.getY() + (window.getHeight() / 2) - (statueConfirmationDialog.getHeight() / 2);
            statueConfirmationDialog.setX(centerX);
            statueConfirmationDialog.setY(centerY);
        });

        // Set the callback for when the user makes a decision
        statueConfirmationDialog.resultCallback = confirmed -> {
            if (confirmed) {
                try {
                    // Verify the player still has enough runes right before upgrading
                    if (gamePlayer.getRunes() < upgradeCost) {
                        showNotification("Not enough runes to upgrade Jörmungandr. Required: " + upgradeCost);
                        return;
                    }

                    // Publish event to upgrade the statue
                    eventBus.publish(new UpgradeStatueUIEvent(statue.getId(), tile.getX(), tile.getY()));
                    showNotification("Upgrading Jörmungandr...");
                } catch (Exception e) {
                    LOGGER.severe("Error upgrading Jörmungandr: " + e.getMessage());
                    showNotification("Error upgrading statue. Please try again.");
                }
            }
        };

        // Show the dialog
        statueConfirmationDialog.show(window);
    }

    /**
     * Shows detailed information about the Jörmungandr statue.
     *
     * @param statue The Jörmungandr statue
     */
    private void showJormungandrInfo(Statue statue) {
        Alert info = new Alert(AlertType.INFORMATION);
        info.setTitle("Jörmungandr");
        info.setHeaderText("The Midgard Serpent (Level " + statue.getLevel() + ")");

        String description = "Jörmungandr, also known as the Midgard Serpent, is a giant sea serpent, destined to kill Thor during Ragnarök. "
                +
                "He wraps around the world, biting his own tail, symbolizing the cyclicality of life and the boundaries of the known world.";

        String abilities = "\n\nAbilities:";
        if (statue.getLevel() >= 2) {
            abilities += "\n• Make a Deal: Destroys 1 random structure of a chosen player; sacrifices 1 structure of your own.";
        } else {
            abilities += "\n• No abilities available yet. Upgrade to unlock.";
        }

        info.setContentText(description + abilities);
        info.showAndWait();
    }

    /**
     * Counts the number of structures owned by a player.
     *
     * @param playerName The name of the player
     * @return The number of structures
     */
    private int countPlayerStructures(String playerName) {
        int count = 0;
        for (int r = 0; r < HEX_ROWS; r++) {
            for (int c = 0; c < HEX_COLS; c++) {
                Tile tile = getTile(r, c);
                if (tile != null &&
                        tile.getOwner() != null &&
                        tile.getOwner().equals(playerName) &&
                        tile.hasEntity() &&
                        tile.getEntity().isStructure() &&
                        !tile.getEntity().isStatue()) {
                    count++;
                }
            }
        }
        return count;
    }

    // ------------------------------------------------------
    // Notification Event handling
    // ------------------------------------------------------
    /**
     * Handles the artifact location event.
     *
     * @param event The artifact location event
     */
    private void handleArtifactLocationEvent(ArtifactLocationEvent event) {
        if (event == null || gameState == null) {
            LOGGER.warning("Cannot handle ArtifactLocationEvent: event or gameState is null.");
            return;
        }
        if (chatComponentController != null && !event.isArtifactFound()) {
            chatComponentController.addSystemMessage("Odin's Eye could not locate any artifact.");
        }

        Platform.runLater(() -> {
            this.locatedArtifactId = event.getArtifactId();
            int col = event.getTileX(); // X is column
            int row = event.getTileY(); // Y is row

            if (row < 0 || row >= HEX_ROWS || col < 0 || col >= HEX_COLS) {
                LOGGER.warning("Invalid tile coordinates for artifact location: row=" + row + ", col=" + col);
                clearArtifactIndicator();
                return;
            }
            if (hSpacing <= 0 || vSpacing <= 0 || effectiveHexSize <= 0) {
                LOGGER.warning("Grid parameters not initialized, cannot display artifact location.");
                clearArtifactIndicator();
                return;
            }

            double cx = gridOffsetX + col * hSpacing + (row % 2) * (hSpacing / 2.0);
            double cy = gridOffsetY + row * vSpacing;
            this.locatedArtifactScreenCoords = new Point2D(cx, cy);

            LOGGER.fine("Calculated screen coordinates for artifact: " + cx + ", " + cy);
            redrawEntities();

            if (artifactIndicatorClearTimer != null) {
                artifactIndicatorClearTimer.stop();
            }
            artifactIndicatorClearTimer = new PauseTransition(ARTIFACT_INDICATOR_DURATION);
            artifactIndicatorClearTimer.setOnFinished(e -> {
                clearArtifactIndicator();
                LOGGER.info("Artifact location indicator cleared after timeout.");
            });
            artifactIndicatorClearTimer.play();

            if (chatComponentController != null) {
                chatComponentController.addSystemMessage("Odin's Eye reveals an artifact's presence!");
            }
        });
    }

    /**
     * Clears the artifact location indicator.
     */
    private void clearArtifactIndicator() {
        this.locatedArtifactScreenCoords = null;
        this.locatedArtifactId = -1;
        if (gameCanvas != null) {
            redrawEntities();
        }
    }


    /**
     * Draws the artifact location indicator on the canvas.
     *
     * @param gc The GraphicsContext to draw on
     */
    private void drawArtifactLocationIndicator(GraphicsContext gc, double centerX, double centerY) {
        if (effectiveHexSize <= 0) {
            LOGGER.warning("Cannot draw artifact indicator: effectiveHexSize is invalid.");
            return;
        }

        double indicatorRadius = effectiveHexSize * 0.4;
        Paint oldFill = gc.getFill();
        double oldAlpha = gc.getGlobalAlpha();
        Paint oldStroke = gc.getStroke();
        double oldLineWidth = gc.getLineWidth();

        gc.setGlobalAlpha(0.8);

        gc.setStroke(Color.GOLD);
        gc.setLineWidth(4);
        gc.strokeOval(centerX - indicatorRadius, centerY - indicatorRadius, indicatorRadius * 2, indicatorRadius * 2);

        gc.setStroke(Color.rgb(255, 223, 0, 0.7));
        gc.setLineWidth(2);
        gc.strokeOval(centerX - indicatorRadius * 0.7, centerY - indicatorRadius * 0.7, indicatorRadius * 1.4,
                indicatorRadius * 1.4);

        gc.setFill(Color.YELLOW);
        gc.fillOval(centerX - indicatorRadius * 0.2, centerY - indicatorRadius * 0.2, indicatorRadius * 0.4,
                indicatorRadius * 0.4);

        gc.setFill(oldFill);
        gc.setGlobalAlpha(oldAlpha);
        gc.setStroke(oldStroke);
        gc.setLineWidth(oldLineWidth);

        LOGGER.finer("Drawing artifact location indicator at: " + centerX + ", " + centerY + " for artifact ID: "
                + locatedArtifactId);
    }

    /**
     * Handles the trap location event.
     *
     * @param event The trap location event
     */
    private void handleTrapLocationEvent(TrapLocationEvent event) {
        LOGGER.info("Trap location event received: " + event);
        if (event == null || gameState == null) {
            LOGGER.warning("Cannot handle TrapLocationEvent: event or gameState is null.");
            return;
        }

        Platform.runLater(() -> {
            int col = event.getX(); // X is column
            int row = event.getY(); // Y is row
            int lostRunes = event.getLostRunes();

            if (row < 0 || row >= HEX_ROWS || col < 0 || col >= HEX_COLS) {
                LOGGER.warning("Invalid tile coordinates for trap location: row=" + row + ", col=" + col);
                clearTrapIndicator();
                return;
            }
            if (hSpacing <= 0 || vSpacing <= 0 || effectiveHexSize <= 0) {
                LOGGER.warning("Grid parameters not initialized, cannot display trap location.");
                clearTrapIndicator();
                return;
            }

            double cx = gridOffsetX + col * hSpacing + (row % 2) * (hSpacing / 2.0);
            double cy = gridOffsetY + row * vSpacing;
            this.trapLocationScreenCoords = new Point2D(cx, cy);
            this.trapLostRunes = lostRunes;
            this.trapIndicatorOpacity = 1.0; // Start fully visible

            LOGGER.fine("Calculated screen coordinates for trap: " + cx + ", " + cy);
            redrawEntities();

            // Setup fading animation
            if (trapFadeAnimation != null) {
                trapFadeAnimation.stop();
            }
            trapFadeAnimation = new Timeline(
                    new KeyFrame(Duration.ZERO, new KeyValue(trapIndicatorOpacityProperty, 1.0)),
                    new KeyFrame(Duration.seconds(3), new KeyValue(trapIndicatorOpacityProperty, 0.0))
            );
            trapFadeAnimation.setOnFinished(e -> {
                clearTrapIndicator();
                LOGGER.info("Trap location indicator faded out.");
            });
            trapFadeAnimation.play();

            if (chatComponentController != null) {
                chatComponentController.addSystemMessage("You triggered a trap and lost " + lostRunes + " runes!");
            }
        });
    }

    /**
     * Clears the trap location indicator.
     */
    private void clearTrapIndicator() {
        this.trapLocationScreenCoords = null;
        this.trapLostRunes = 0;
        if (gameCanvas != null) {
            redrawEntities();
        }
    }

    /**
     * Draws the trap location indicator on the canvas.
     *
     * @param gc The GraphicsContext to draw on
     * @param centerX The x-coordinate of the trap center
     * @param centerY The y-coordinate of the trap center
     */
    private void drawTrapLocationIndicator(GraphicsContext gc, double centerX, double centerY) {
        if (effectiveHexSize <= 0 || trapIndicatorOpacity <= 0) {
            return;
        }

        double indicatorRadius = effectiveHexSize * 0.4;

        // Save current graphics context state
        Paint oldFill = gc.getFill();
        double oldAlpha = gc.getGlobalAlpha();
        Paint oldStroke = gc.getStroke();
        double oldLineWidth = gc.getLineWidth();
        Font oldFont = gc.getFont();

        // Set opacity based on animation
        gc.setGlobalAlpha(trapIndicatorOpacity);

        // Draw trap indicator (X shape in red)
        gc.setStroke(Color.RED);
        gc.setLineWidth(3);
        gc.strokeLine(centerX - indicatorRadius, centerY - indicatorRadius,
                centerX + indicatorRadius, centerY + indicatorRadius);
        gc.strokeLine(centerX + indicatorRadius, centerY - indicatorRadius,
                centerX - indicatorRadius, centerY + indicatorRadius);

        // Draw circle around X
        gc.strokeOval(centerX - indicatorRadius, centerY - indicatorRadius,
                indicatorRadius * 2, indicatorRadius * 2);

        // Draw lost runes text
        gc.setFill(Color.RED);
        gc.setFont(new Font(Font.getDefault().getFamily(), effectiveHexSize * 0.35));
        gc.fillText("-" + trapLostRunes, centerX + indicatorRadius * 1.2, centerY);

        // Restore graphics context state
        gc.setFill(oldFill);
        gc.setGlobalAlpha(oldAlpha);
        gc.setStroke(oldStroke);
        gc.setLineWidth(oldLineWidth);
        gc.setFont(oldFont);
    }

    /**
     * Handles the displaying of Buffs and Debuffs
     *
     * @param debuffEvent the debuffEvent containing the corresponding message.
     */
    public void handleBuffOrDebuff(DebuffEvent debuffEvent) {
        if (chatComponentController != null) {
            chatComponentController.addSystemMessage(debuffEvent.getMessage());
        }
    }
}
