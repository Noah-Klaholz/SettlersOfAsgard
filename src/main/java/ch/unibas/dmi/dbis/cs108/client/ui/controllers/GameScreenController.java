package ch.unibas.dmi.dbis.cs108.client.ui.controllers;

import ch.unibas.dmi.dbis.cs108.client.ui.SceneManager;
import ch.unibas.dmi.dbis.cs108.client.ui.events.UIEventBus;
import ch.unibas.dmi.dbis.cs108.client.ui.events.admin.ConnectionStatusEvent;
import ch.unibas.dmi.dbis.cs108.client.ui.events.chat.GlobalChatEvent;
import ch.unibas.dmi.dbis.cs108.client.ui.events.chat.LobbyChatEvent;
import ch.unibas.dmi.dbis.cs108.client.ui.utils.ResourceLoader;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.logging.Logger;

public class GameScreenController extends BaseController {
    private static final Logger LOGGER = Logger.getLogger(GameScreenController.class.getName());
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    private static final int HEX_ROWS = 7;
    private static final int HEX_COLS = 8;
    private static final double HEX_SIZE = 40.0; // Base size of hexagon
    
    // Add fields to track the scaled dimensions
    private double scaledMapWidth;
    private double scaledMapHeight;
    private double mapOffsetX;
    private double mapOffsetY;
    private double effectiveHexSize;

    private final ObservableList<String> chatMessages = FXCollections.observableArrayList();
    private final ObservableList<String> players = FXCollections.observableArrayList();
    private String currentLobbyId; // To store the current lobby ID

    // Game map related fields
    private Image mapImage;
    private boolean isMapLoaded = false;

    // UI Components
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

    public GameScreenController() {
        super(new ResourceLoader(), UIEventBus.getInstance(), SceneManager.getInstance());
    }

    @FXML
    private void initialize() {
        LOGGER.info("Initializing game screen");
        try {
            setupUI();
            setupEventHandlers();
            loadMapImage();
            setupCanvasListeners();
        } catch (Exception e) {
            LOGGER.severe("Failed to initialize game screen: " + e.getMessage());
        }
    }


    private void setupUI() {
        // Set up the chat list
        chatListView.setItems(chatMessages);

        // Set up the players list
        playersList.setItems(players);

        // Select global chat by default
        globalChatButton.setSelected(true);

        // Set initial values
        energyBar.setProgress(0.5);
        runesLabel.setText("0");
        connectionStatusLabel.setText("Connected");

        // Initialize canvas
        gameCanvas.widthProperty().bind(((Region) gameCanvas.getParent()).widthProperty());
        gameCanvas.heightProperty().bind(((Region) gameCanvas.getParent()).heightProperty());

        // TODO: Fetch initial game state instead of using mock data
    }

    private void setupEventHandlers() {
        // TODO: Subscribe to relevant UI events (e.g., GlobalChatEvent, GameStateUpdateEvent, PlayerListUpdateEvent)
        // TODO: Subscribe to ConnectionStatusEvent
        // ... other events
        eventBus.subscribe(GlobalChatEvent.class, this::handleChatMessage);
        eventBus.subscribe(LobbyChatEvent.class, this::handleLobbyChatMessage);
        eventBus.subscribe(ConnectionStatusEvent.class, this::handleConnectionStatus);
        // TODO: Add subscriptions for GameStateUpdateEvent, PlayerListUpdateEvent, etc.

        // Setup input field handler
        chatInputField.setOnAction(event -> handleMessageSend());
    }

    // --- UIEvent Handlers ---

    private void handleChatMessage(GlobalChatEvent event) {
        if (event == null || event.getContent() == null || event.getSender() == null) {
            LOGGER.warning("Received null or incomplete UI chat message event in GameScreen");
            return;
        }

        Platform.runLater(() -> {
            // Format the message including the sender before adding
            String messageWithSender = String.format("%s: %s", event.getSender(), event.getContent());
            addChatMessage(messageWithSender);
        });
    }

    private void handleLobbyChatMessage(LobbyChatEvent event) {
        if (event == null || event.getMessage() == null || event.getSender() == null) {
            LOGGER.warning("Received null or incomplete lobby chat message event in GameScreen");
            return;
        }

        Platform.runLater(() -> {
            // Format the message including the sender before adding
            String messageWithSender = String.format("[Lobby] %s: %s", event.getSender(), event.getMessage());
            addChatMessage(messageWithSender);
        });
    }

    private void handleConnectionStatus(ConnectionStatusEvent event) {
        if (event == null) {
            LOGGER.warning("Received null connection status event in GameScreen");
            return;
        }

        Platform.runLater(() -> {
            String statusText = Optional.ofNullable(event.getStatus()).map(Object::toString).orElse("UNKNOWN");
            connectionStatusLabel.setText(statusText);
            // TODO: Add visual indication for connection status (e.g., color change)

            if (event.getMessage() != null && !event.getMessage().isEmpty()) {
                addSystemMessage(event.getMessage());
            }
        });
    }

    // TODO: Implement handlers for GameStateUpdateEvent, PlayerListUpdateEvent, etc.

    // --- UI Update Methods ---

    private void addChatMessage(String message) {
        if (message == null || message.isEmpty()) {
            return;
        }
        String formattedMessage = getCurrentTime() + " " + message;
        chatMessages.add(formattedMessage);
        scrollToBottom(chatListView, chatMessages);
    }

    private void addSystemMessage(String message) {
        if (message == null || message.isEmpty()) {
            return;
        }
        chatMessages.add(getCurrentTime() + " System: " + message);
        scrollToBottom(chatListView, chatMessages);
    }

    private void scrollToBottom(ListView<String> listView, ObservableList<String> list) {
        Platform.runLater(() -> {
            int size = list.size();
            if (size > 0) {
                listView.scrollTo(size - 1);
            }
        });
    }

    private String getCurrentTime() {
        return "[" + LocalDateTime.now().format(TIME_FORMATTER) + "]";
    }

    // --- FXML Action Handlers ---

    @FXML
    private void handleBackToMainMenu() {
        LOGGER.info("Back to main menu requested");
        sceneManager.switchToScene(SceneManager.SceneType.MAIN_MENU);
    }

    @FXML
    private void handleSettings() {
        LOGGER.info("Settings requested");
        // Implementation for settings dialog
    }

    @FXML
    private void handleResourceOverview() {
        LOGGER.info("Resource overview requested");
        // Show resource overview dialog
    }

    @FXML
    private void handleGameRound() {
        LOGGER.info("End turn requested");
        // End current turn logic
    }

    @FXML
    private void handleLeaderboard() {
        LOGGER.info("Leaderboard requested");
        // Show leaderboard dialog
    }

    @FXML
    private void handleGlobalChatSelect() {
        LOGGER.info("Global chat selected");
        globalChatButton.setSelected(true);
        lobbyChatButton.setSelected(false);
    }

    @FXML
    private void handleLobbyChatSelect() {
        LOGGER.info("Lobby chat selected");
        globalChatButton.setSelected(false);
        lobbyChatButton.setSelected(true);

        // If no lobby is joined, inform the user
        if (currentLobbyId == null || currentLobbyId.isEmpty()) {
            addSystemMessage("You are not in a lobby. Messages will not be sent until you join one.");
        }
    }

    @FXML
    private void handleMessageSend() {
        String message = chatInputField.getText().trim();
        if (!message.isEmpty()) {
            LOGGER.info("Sending message: " + message);

            if (globalChatButton.isSelected()) {
                // Send global chat message
                eventBus.publish(new GlobalChatEvent(message, GlobalChatEvent.ChatType.GLOBAL));
            } else if (lobbyChatButton.isSelected()) {
                // Send lobby chat message if in a lobby
                if (currentLobbyId != null && !currentLobbyId.isEmpty()) {
                    eventBus.publish(new LobbyChatEvent(currentLobbyId, null, message));
                } else {
                    addSystemMessage("Cannot send lobby message: You're not in a lobby.");
                }
            } else {
                // Default to global chat if neither is selected
                LOGGER.warning("No chat type selected, defaulting to GLOBAL");
                eventBus.publish(new GlobalChatEvent(message, GlobalChatEvent.ChatType.GLOBAL));
            }

            chatInputField.clear();
        }
    }

    // Method to set the current lobby ID when joining a lobby
    public void setCurrentLobbyId(String lobbyId) {
        this.currentLobbyId = lobbyId;
    }

    // Add cleanup method to unsubscribe from events
    public void cleanup() {
        eventBus.unsubscribe(GlobalChatEvent.class, this::handleChatMessage);
        eventBus.unsubscribe(LobbyChatEvent.class, this::handleLobbyChatMessage);
        eventBus.unsubscribe(ConnectionStatusEvent.class, this::handleConnectionStatus);
        LOGGER.info("GameScreenController resources cleaned up");
    }

    private void loadMapImage() {
        LOGGER.info("Loading map image");
        try {
            // Load the map image from resources
            mapImage = resourceLoader.loadImage(ResourceLoader.MAP_IMAGE);

            if (mapImage != null) {
                isMapLoaded = true;
                LOGGER.info("Map image loaded successfully");
                // Initial draw of the map and grid
                drawMapAndGrid();
            } else {
                LOGGER.severe("Failed to load map image");
                isMapLoaded = false;
            }
        } catch (Exception e) {
            LOGGER.severe("Error loading map image: " + e.getMessage());
            isMapLoaded = false;
        }
    }

    private void setupCanvasListeners() {
        // Redraw map and grid when canvas size changes
        gameCanvas.widthProperty().addListener((obs, oldVal, newVal) -> drawMapAndGrid());
        gameCanvas.heightProperty().addListener((obs, oldVal, newVal) -> drawMapAndGrid());

        // Add mouse event handlers for tile selection
        gameCanvas.setOnMouseClicked(e -> {
            double x = e.getX();
            double y = e.getY();
            // Convert mouse position to hex grid coordinates
            int[] hexCoords = getHexCoordinatesFromPixel(x, y);
            if (hexCoords != null) {
                LOGGER.info("Clicked on hex: row=" + hexCoords[0] + ", col=" + hexCoords[1]);
                // TODO: Handle tile selection logic
            }
        });
    }

    private void drawMapAndGrid() {
        if (!isMapLoaded || gameCanvas == null) return;

        double canvasWidth = gameCanvas.getWidth();
        double canvasHeight = gameCanvas.getHeight();

        if (canvasWidth <= 0 || canvasHeight <= 0) return;

        GraphicsContext gc = gameCanvas.getGraphicsContext2D();
        gc.clearRect(0, 0, canvasWidth, canvasHeight);

        // Calculate image dimensions to maintain aspect ratio
        double imageWidth = mapImage.getWidth();
        double imageHeight = mapImage.getHeight();
        double imageRatio = imageWidth / imageHeight;
        double canvasRatio = canvasWidth / canvasHeight;

        if (canvasRatio > imageRatio) {
            // Canvas is wider than image ratio - height is limiting factor
            scaledMapHeight = canvasHeight;
            scaledMapWidth = scaledMapHeight * imageRatio;
        } else {
            // Canvas is taller than image ratio - width is limiting factor
            scaledMapWidth = canvasWidth;
            scaledMapHeight = scaledMapWidth / imageRatio;
        }

        // Calculate offsets to center the image
        mapOffsetX = (canvasWidth - scaledMapWidth) / 2;
        mapOffsetY = (canvasHeight - scaledMapHeight) / 2;

        // Draw the map background at the calculated size and position
        gc.drawImage(mapImage, mapOffsetX, mapOffsetY, scaledMapWidth, scaledMapHeight);

        // Calculate hexagon size based on scaled map dimensions
        double hexWidthLimit = scaledMapWidth / (HEX_COLS * 0.75 + 0.25);
        double hexHeightLimit = scaledMapHeight / (HEX_ROWS * 0.75 + 0.25);
        effectiveHexSize = Math.min(hexWidthLimit / 2, hexHeightLimit / 2);

        // Draw hexagonal grid with proper scaling
        drawHexGrid(gc, effectiveHexSize, scaledMapWidth, scaledMapHeight);
    }

    private void drawHexGrid(GraphicsContext gc, double hexSize, double mapWidth, double mapHeight) {
        // Set line properties for the grid
        gc.setStroke(Color.WHITE);
        gc.setLineWidth(1.5);
        gc.setGlobalAlpha(0.7); // Semi-transparent grid

        // Calculate grid spacing
        double horizontalSpacing = hexSize * Math.sqrt(3);
        double verticalSpacing = hexSize * 1.5;

        // Calculate total grid dimensions
        double gridWidth = horizontalSpacing * HEX_COLS;
        double gridHeight = verticalSpacing * HEX_ROWS + hexSize / 2;

        // Calculate grid offset to center it on the map
        double gridOffsetX = mapOffsetX + (mapWidth - gridWidth) / 2;
        double gridOffsetY = mapOffsetY + (mapHeight - gridHeight) / 2;

        // Draw each hexagon in the grid
        for (int row = 0; row < HEX_ROWS; row++) {
            for (int col = 0; col < HEX_COLS; col++) {
                // Calculate the center of this hexagon
                double x = gridOffsetX + col * horizontalSpacing + (row % 2) * (horizontalSpacing / 2);
                double y = gridOffsetY + row * verticalSpacing;

                // Draw the hexagon
                drawHexagon(gc, x, y, hexSize);
            }
        }

        // Reset transparency
        gc.setGlobalAlpha(1.0);
    }

    private void drawHexagon(GraphicsContext gc, double centerX, double centerY, double size) {
        // Calculate the six points of the hexagon
        double[] xPoints = new double[6];
        double[] yPoints = new double[6];

        for (int i = 0; i < 6; i++) {
            double angle = 2 * Math.PI / 6 * i + Math.PI / 6; // Rotate 30 degrees to point up
            xPoints[i] = centerX + size * Math.cos(angle);
            yPoints[i] = centerY + size * Math.sin(angle);
        }

        // Draw the hexagon outline
        gc.beginPath();
        gc.moveTo(xPoints[0], yPoints[0]);

        for (int i = 1; i < 6; i++) {
            gc.lineTo(xPoints[i], yPoints[i]);
        }

        gc.closePath();
        gc.stroke();
    }

    private int[] getHexCoordinatesFromPixel(double pixelX, double pixelY) {
        // Early exit if we're outside the map area completely
        if (pixelX < mapOffsetX || pixelX > mapOffsetX + scaledMapWidth ||
            pixelY < mapOffsetY || pixelY > mapOffsetY + scaledMapHeight) {
            return null;
        }

        // Calculate hex grid parameters using the effective hex size
        double horizontalSpacing = effectiveHexSize * Math.sqrt(3);
        double verticalSpacing = effectiveHexSize * 1.5;

        // Calculate total grid dimensions
        double gridWidth = horizontalSpacing * HEX_COLS;
        double gridHeight = verticalSpacing * HEX_ROWS + effectiveHexSize / 2;

        // Calculate grid offset within the map
        double gridOffsetX = mapOffsetX + (scaledMapWidth - gridWidth) / 2;
        double gridOffsetY = mapOffsetY + (scaledMapHeight - gridHeight) / 2;

        // Adjust coordinates relative to the grid offset
        double relX = pixelX - gridOffsetX;
        double relY = pixelY - gridOffsetY;

        // Calculate approximate row and column
        int row = (int) Math.floor(relY / verticalSpacing);
        int col;
        
        if (row % 2 == 0) { // Even rows
            col = (int) Math.floor(relX / horizontalSpacing);
        } else { // Odd rows with offset
            col = (int) Math.floor((relX - horizontalSpacing/2) / horizontalSpacing);
        }

        // Fine-tune position using hex math for more accuracy
        // This helps with the gaps between hexagons
        double centerX = col * horizontalSpacing + (row % 2) * (horizontalSpacing / 2);
        double centerY = row * verticalSpacing;
        double dx = relX - centerX;
        double dy = relY - centerY;
        double distance = Math.sqrt(dx * dx + dy * dy);
        
        // Check if the point is within the hexagon's radius
        if (distance > effectiveHexSize * 1.2) { // Allow a bit of leeway with 1.2 factor
            // Find closest hex if we're in the gap
            int[] closestHex = findClosestHex(relX, relY, row, col, horizontalSpacing, verticalSpacing);
            if (closestHex != null) {
                row = closestHex[0];
                col = closestHex[1];
            }
        }

        // Check if the coordinates are valid
        if (row >= 0 && row < HEX_ROWS && col >= 0 && col < HEX_COLS) {
            return new int[]{row, col};
        }

        return null; // Outside the grid
    }

    // Helper method to find the closest hex when clicking near borders
    private int[] findClosestHex(double x, double y, int baseRow, int baseCol, 
                                double horizontalSpacing, double verticalSpacing) {
        int[] result = new int[2];
        double closestDistance = Double.MAX_VALUE;
        
        // Check neighboring hexes
        for (int r = Math.max(0, baseRow - 1); r <= Math.min(HEX_ROWS - 1, baseRow + 1); r++) {
            for (int c = Math.max(0, baseCol - 1); c <= Math.min(HEX_COLS - 1, baseCol + 1); c++) {
                // Skip the base hex itself as we already checked it
                if (r == baseRow && c == baseCol) continue;
                
                // Calculate hex center
                double centerX = c * horizontalSpacing + (r % 2) * (horizontalSpacing / 2);
                double centerY = r * verticalSpacing;
                
                // Calculate distance
                double dx = x - centerX;
                double dy = y - centerY;
                double distance = Math.sqrt(dx * dx + dy * dy);
                
                if (distance < closestDistance) {
                    closestDistance = distance;
                    result[0] = r;
                    result[1] = c;
                }
            }
        }
        
        return result;
    }
}
