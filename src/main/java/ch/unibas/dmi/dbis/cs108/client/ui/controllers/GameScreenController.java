package ch.unibas.dmi.dbis.cs108.client.ui.controllers;

import ch.unibas.dmi.dbis.cs108.shared.protocol.CommunicationAPI.NetworkProtocol.*;
import ch.unibas.dmi.dbis.cs108.client.ui.events.ReceiveCommandEvent;
import ch.unibas.dmi.dbis.cs108.client.ui.SceneManager;
import ch.unibas.dmi.dbis.cs108.client.ui.events.*;
import ch.unibas.dmi.dbis.cs108.client.ui.utils.ResourceLoader;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;

import java.util.Arrays;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Controller for the game screen that handles UI interactions and game rendering.
 */
public class GameScreenController extends BaseController {
    private static final Logger LOGGER = Logger.getLogger(GameScreenController.class.getName());
    private Image mapImage;

    // UI Components
    @FXML
    private ListView<String> chatListView;
    @FXML
    private TextField chatInputField;
    @FXML
    private Canvas gameCanvas;
    @FXML
    private HBox artifactHand;
    @FXML
    private HBox structureHand;
    @FXML
    private Pane artifact1, artifact2, artifact3;
    @FXML
    private Pane structure1, structure2, structure3, structure4, structure5, structure6, structure7, structure8;

    // Game state tracking
    private boolean isInitialized = false;
    private GraphicsContext gc;

    /**
     * Constructor with required dependencies.
     */
    public GameScreenController() {
        super(new ResourceLoader(), UIEventBus.getInstance(), SceneManager.getInstance());
    }

    /**
     * Initializes the controller and UI components.
     */
    @FXML
    private void initialize() {
        try {
            // Initialize UI elements
            initializeChat();
            initializeGameCanvas();
            initializeCardHands();

            // Subscribe to the NameChangeResponseEvent
            eventBus.subscribe(ch.unibas.dmi.dbis.cs108.client.ui.events.NameChangeResponseEvent.class, this::handleNameChangeResponse);

            isInitialized = true;
            LOGGER.info("Game screen controller initialized successfully");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error initializing game screen", e);
            showError("Initialization Error", "Could not initialize game screen properly.");
        }
    }

    /**
     * Set up chat components and event listeners.
     */
// Add to the initializeChat method
    private void initializeChat() {
        chatListView.getItems().add("Welcome to Settlers of Asgard!");
        eventBus.subscribe(ChatMessageEvent.class, this::handleIncomingChatMessage);
        eventBus.subscribe(ReceiveCommandEvent.class, this::handleIncomingCommandMessage);

        // Enhanced cell factory for proper text wrapping
        chatListView.setCellFactory(list -> new ListCell<String>() {
            private final Label wrapLabel = new Label();

            {
                wrapLabel.setWrapText(true);
                wrapLabel.setMaxWidth(chatListView.getWidth() - 20);
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    wrapLabel.setText(item);
                    setGraphic(wrapLabel);
                    // Force the cell to use all available width
                    setPrefWidth(0);
                }
            }
        });

        // Ensure label width updates with the list view width
        chatListView.widthProperty().addListener((obs, oldVal, newVal) -> {
            chatListView.refresh();
        });
    }

    // Add this method to update the energy display
//    private void updateEnergyDisplay(int current, int max) {
//        // Update progress bar fill
//        double ratio = (double) current / max;
//        double width = 180 * ratio; // 180 is the width of the background
//        energyBarFill.setWidth(width);
//
//        // Update label
//        energyLabel.setText(current + "/" + max);
//
//        // Update orbs
//        energyOrb1.setVisible(current >= 1);
//        energyOrb2.setVisible(current >= 2);
//        energyOrb3.setVisible(current >= 3);
//        energyOrb4.setVisible(current >= 4);
//    }

    /**
     * Set up the game canvas for rendering.
     */
    private void initializeGameCanvas() {
        if (gameCanvas == null) {
            LOGGER.severe("Game canvas is null! Check FXML loading.");
            return;
        }

        // Get the parent container of the canvas
        StackPane canvasParent = (StackPane) gameCanvas.getParent();
        if (canvasParent == null) {
            LOGGER.severe("Canvas parent is null! Check FXML structure.");
            return;
        }

        // Bind canvas size to parent container size
        gameCanvas.widthProperty().bind(canvasParent.widthProperty());
        gameCanvas.heightProperty().bind(canvasParent.heightProperty());

        // Load map image
        try {
            mapImage = new Image(Objects.requireNonNull(getClass().getResourceAsStream(ResourceLoader.MAP_IMAGE)));
            if (mapImage.isError()) {
                LOGGER.log(Level.WARNING, "Error loading map image");
                mapImage = null;
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to load map image", e);
        }

        // Get graphics context for drawing
        gc = gameCanvas.getGraphicsContext2D();

        // Add listeners to redraw when canvas size changes
        gameCanvas.widthProperty().addListener((obs, oldVal, newVal) -> drawGame());
        gameCanvas.heightProperty().addListener((obs, oldVal, newVal) -> drawGame());

        // Initial draw
        drawGame();
    }

    /**
     * Initialize card hands with default styling.
     */
    private void initializeCardHands() {
        // Set up artifact cards
        setupCardPane(artifact1, "Mjölnir");
        setupCardPane(artifact2, "Gungnir");
        setupCardPane(artifact3, "Draupnir");

        // Set up structure cards
        setupCardPane(structure1, "Longhouse");
        setupCardPane(structure2, "Forge");
        setupCardPane(structure3, "Mead Hall");
        setupCardPane(structure4, "Runestone");
        setupCardPane(structure5, "Watchtower");
        setupCardPane(structure6, "Barracks");
        setupCardPane(structure7, "Temple");
        setupCardPane(structure8, "Market");
    }

    /**
     * Configure a card pane with hover effects and tooltips.
     */
    private void setupCardPane(Pane cardPane, String cardName) {
        cardPane.setUserData(cardName);
        cardPane.setOnMouseEntered(e -> {
            cardPane.setScaleX(1.05);
            cardPane.setScaleY(1.05);
            LOGGER.info("Mouse entered: " + cardName);
        });
        cardPane.setOnMouseExited(e -> {
            cardPane.setScaleX(1.0);
            cardPane.setScaleY(1.0);
            LOGGER.info("Mouse exited: " + cardName);
        });
        cardPane.setOnMouseClicked(e -> {
            handleCardSelection(cardPane);
            LOGGER.info("Card clicked: " + cardName);
        });
    }

    /**
     * Handle when a card is selected.
     */
    private void handleCardSelection(Pane cardPane) {
        String cardName = (String) cardPane.getUserData();
        LOGGER.info("Selected card: " + cardName);
        // Future implementation: Card selection logic
    }

    /**
     * Draw/redraw the game map and elements on the canvas.
     */
    private void drawGame() {
        if (gc == null) return;

        double canvasWidth = gameCanvas.getWidth();
        double canvasHeight = gameCanvas.getHeight();

        // Clear the canvas
        gc.clearRect(0, 0, canvasWidth, canvasHeight);

        // Draw background
        gc.setFill(Color.rgb(44, 51, 71));
        gc.fillRect(0, 0, canvasWidth, canvasHeight);

        // Draw map image if available
        if (mapImage != null && !mapImage.isError()) {
            // Calculate scale factor to fit the entire image within the canvas
            double scaleX = canvasWidth / mapImage.getWidth();
            double scaleY = canvasHeight / mapImage.getHeight();
            double scale = Math.min(scaleX, scaleY); // Use min to fit entire image

            // Calculate position to center the image
            double x = (canvasWidth - mapImage.getWidth() * scale) / 2;
            double y = (canvasHeight - mapImage.getHeight() * scale) / 2;

            // Draw the image at calculated position and size
            gc.drawImage(mapImage, x, y, mapImage.getWidth() * scale, mapImage.getHeight() * scale);
        }

        // Draw game grid on top of the map
        drawGameGrid();

        // Future: Draw game pieces, player positions, etc.
    }

    /**
     * Draw the game board grid.
     */
    // File: src/main/java/ch/unibas/dmi/dbis/cs108/client/ui/controllers/GameScreenController.java
    private void drawGameGrid() {
        double width = gameCanvas.getWidth();
        double height = gameCanvas.getHeight();

        // Limit cell size to prevent excessive grid lines
        double cellSize = Math.min(width, height) / 20;

        // Hard cap on maximum grid lines to prevent memory issues
        int maxLines = 50;

        gc.setStroke(Color.rgb(76, 83, 102));
        gc.setLineWidth(1);

        // Draw vertical lines with safety limit
        int vLines = Math.min(maxLines, (int) (width / cellSize) + 1);
        for (int i = 0; i <= vLines; i++) {
            double x = i * cellSize;
            gc.strokeLine(x, 0, x, height);
        }

        // Draw horizontal lines with safety limit
        int hLines = Math.min(maxLines, (int) (height / cellSize) + 1);
        for (int i = 0; i <= hLines; i++) {
            double y = i * cellSize;
            gc.strokeLine(0, y, width, y);
        }
    }

    /**
     * Draw decorative elements at grid intersections.
     */
    private void drawGridDecorations(double cellSize) {
        // Implementation for decorative elements at grid points
        // This would be expanded in the actual game implementation
    }

    /**
     * Handle incoming chat messages from the event bus.
     */
    private void handleIncomingChatMessage(ChatMessageEvent event) {
        Platform.runLater(() -> {
            try {
                String message = event.getMessage();
                if (message == null || message.trim().isEmpty()) return;

                chatListView.getItems().add(message);

                // Auto-scroll to bottom
                chatListView.scrollTo(chatListView.getItems().size() - 1);
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Error handling chat message", e);
            }
        });
    }

    private void handleIncomingCommandMessage(ReceiveCommandEvent event) {
        Platform.runLater(() -> {
            try {
                String message = event.getMessage();
                System.out.println("Received from server " + message);
                if (message == null || message.trim().isEmpty()) return;

                Commands type = event.getType();
                String[] args = message.replaceAll("OK\\$", "").trim().split("\\$");
                System.out.println(Arrays.toString(args));

                switch (type) {
                    case LISTPLAYERS:
                        if(args.length > 1) {
                            if (args[1].equals("LOBBY")) {
                                message = "Players in lobby: " + args[2];
                            } else if (args[1].equals("SERVER")) {
                                message = "Players in server: " + args[2];
                            }
                            chatListView.getItems().add(message);
                        }
                        break;
                    case LISTLOBBIES:
                        if (args.length > 1) {
                            message = "Lobbies: \n" + args[1];
                            chatListView.getItems().add(message);
                        }
                        break;
                    case CREATELOBBY:
                        message = "Created lobby: " + args[2];
                        chatListView.getItems().add(message);
                    case JOIN:
                        if (args.length > 1) {
                            message = "You joined lobby: " + args[1];
                            chatListView.getItems().add(message);
                        }
                        break;
                    case LEAVE:
                        if (args.length > 1) {
                            message = "You left lobby: " + args[1];
                            chatListView.getItems().add(message);
                        }
                        break;
                    case START:
                        chatListView.getItems().add("Game started!");
                        break;
                    case CHANGENAME:
                        if (args.length > 1) {
                            message = args[1] + " changed their name to: " + args[2];
                            chatListView.getItems().add(message);
                        }
                        break;
                    case GETGAMESTATUS:
                        if (args.length > 1) {
                            chatListView.getItems().add("Game status: " + args[2]);
                        }
                        break;
                    case GETPRICES:
                        if (args.length > 1) {
                            chatListView.getItems().add("Prices: " + args[2]);
                        }
                        break;
                    default:
                        chatListView.getItems().add(message);
                        LOGGER.warning("GameScreen Controller: Unknown command type: " + type);
                }

                // Auto-scroll to bottom
                chatListView.scrollTo(chatListView.getItems().size() - 1);
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Error handling command message", e);
            }
        });
    }

    /**
     * Handle sending a chat message.
     */
    @FXML
    private void handleMessageSend() {
        try {
            String message = chatInputField.getText().trim();
            if (!message.isEmpty()) {
                System.out.println(message);
                // Only publish the event to send through the network
                // Don't add to UI here - wait for server echo
                if (message.startsWith("/")) {
                    System.out.println("Command: " + message);
                    eventBus.publish(new SendCommandEvent(message.toLowerCase()));
                } else {
                    eventBus.publish(new SendChatEvent(message, SendChatEvent.ChatType.GLOBAL));
                }

                // Add a visual indicator that message is being sent
                // Optional: chatListView.getItems().add("Sending: " + message);

                chatInputField.clear();
                chatInputField.requestFocus();
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error sending chat message", e);
            showError("Communication Error", "Failed to send chat message.");
        }
    }

    /**
     * Handle resource overview button click.
     */
    @FXML
    private void handleResourceOverview() {
        LOGGER.info("Opening resource overview");
        // Future implementation: Show resource dialog/panel
    }

    /**
     * Handle game round button click.
     */
    @FXML
    private void handleGameRound() {
        LOGGER.info("Processing game round");
        // Future implementation: Process game round
    }

    /**
     * Handle other action button click.
     */
    @FXML
    private void handleOtherAction() {
        LOGGER.info("Other action triggered");
        // Future implementation: Other game action
    }

    private void handleNameChangeResponse(ch.unibas.dmi.dbis.cs108.client.ui.events.NameChangeResponseEvent event) {
        Platform.runLater(() -> {
            try {
                if (event.isSuccess()) {
                    chatListView.getItems().add("Name successfully changed to: " + event.getNewName());
                } else {
                    chatListView.getItems().add("Failed to change name: " + event.getMessage());
                }

                // Auto-scroll chat to bottom
                chatListView.scrollTo(chatListView.getItems().size() - 1);
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Error handling name change response", e);
            }
        });
    }

    /**
     * Clean up resources when controller is no longer needed.
     */
    public void cleanup() {
        // Unsubscribe from events to prevent memory leaks
        if (eventBus != null) {
            eventBus.unsubscribe(ChatMessageEvent.class, this::handleIncomingChatMessage);
            eventBus.unsubscribe(ReceiveCommandEvent.class, this::handleIncomingCommandMessage);
            eventBus.unsubscribe(ch.unibas.dmi.dbis.cs108.client.ui.events.NameChangeResponseEvent.class, this::handleNameChangeResponse);
        }

        // Release resources
        gc = null;
        isInitialized = false;

        LOGGER.info("Game screen controller cleaned up");
    }

    /**
     * Display an error dialog.
     */
    private void showError(String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }
}