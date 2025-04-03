//package ch.unibas.dmi.dbis.cs108.client.ui.controllers;
//
//import ch.unibas.dmi.dbis.cs108.client.core.observer.GameEventListener;
//import ch.unibas.dmi.dbis.cs108.client.ui.SceneManager;
//import ch.unibas.dmi.dbis.cs108.client.ui.game.GameCanvas;
//import ch.unibas.dmi.dbis.cs108.shared.protocol.CommunicationAPI;
//import javafx.application.Platform;
//import javafx.fxml.FXML;
//import javafx.scene.canvas.Canvas;
//import javafx.scene.control.Label;
//import javafx.scene.control.ListView;
//import javafx.scene.control.TextField;
//import javafx.scene.layout.FlowPane;
//
//import java.util.logging.Logger;
//
//public class GameScreenController implements GameEventListener {
//    private static final Logger logger = Logger.getLogger(GameScreenController.class.getName());
//
//    @FXML
//    private Canvas gameCanvas;
//    @FXML
//    private ListView<String> chatMessages;
//    @FXML
//    private TextField chatInput;
//    @FXML
//    private Label runesCount;
//    @FXML
//    private Label energyCount;
//    @FXML
//    private FlowPane artifactsContainer;
//    @FXML
//    private FlowPane structuresContainer;
//
//    private GameCanvas canvas;
//    private SceneManager sceneManager;
//    private GameClient gameClient;
//
//    @FXML
//    public void initialize() {
//        canvas = new GameCanvas(gameCanvas);
//        canvas.initializeMap();
//
//        // Set initial resource values
//        runesCount.setText("0");
//        energyCount.setText("0");
//    }
//
//    public void setSceneManager(SceneManager sceneManager) {
//        this.sceneManager = sceneManager;
//    }
//
//    public void setGameClient(GameClient gameClient) {
//        this.gameClient = gameClient;
//    }
//
//    @FXML
//    public void sendChatMessage() {
//        String message = chatInput.getText().trim();
//        if (!message.isEmpty()) {
//            // Send message to server using communication protocol
//            if (gameClient != null) {
//                gameClient.sendLobbyChat(message);
//                chatInput.clear();
//            }
//        }
//    }
//
//    @FXML
//    public void endTurn() {
//        //ToDo: Implement end turn logic
//    }
//
//    @FXML
//    public void leaveGame() {
//        //ToDo: Implement leave game logic
//    }
//
//    @Override
//    public void onMessageReceived(String serverMessage) {
//        // Ensure UI updates happen on the JavaFX thread
//        Platform.runLater(() -> {
//            // Parse the server message and update UI accordingly
//            String[] parts = serverMessage.split("[:$]", 2);
//            if (parts.length >= 2) {
//                String command = parts[0];
//                String content = parts[1];
//
//                try {
//                    CommunicationAPI.NetworkProtocol.Commands cmd =
//                            CommunicationAPI.NetworkProtocol.Commands.fromCommand(command);
//
//                    switch (cmd) {
//                        case CHATGLOBAL:
//                        case CHATLOBBY:
//                        case CHATPRIVATE:
//                            chatMessages.getItems().add(content);
//                            break;
//
//                        case RESOURCEBALANCE:
//                            updateResources(content);
//                            break;
//
//                        case SYNCHRONIZE:
//                            canvas.updateGameState(content);
//                            break;
//
//                        case BUYHEXFIELD:
//                        case BUILDSTRUCTURE:
//                        case UPGRADESTRUCTURE:
//                            // Force refresh of the game state
//                            requestGameSync();
//                            break;
//
//                        case ERROR:
//                            handleError(content);
//                            break;
//
//                        default:
//                            logger.info("Unhandled command: " + command + " with content: " + content);
//                    }
//                } catch (IllegalArgumentException e) {
//                    logger.warning("Unknown command received: " + command);
//                }
//            }
//        });
//    }
//
//    private void requestGameSync() {
//        //ToDo: Implement game synchronization request
//    }
//
//    private void updateResources(String content) {
//        // Parse resource update from server and update UI
//        // Expected format: "runes,energy"
//        try {
//            String[] resources = content.split(",");
//            if (resources.length >= 2) {
//                runesCount.setText(resources[0]);
//                energyCount.setText(resources[1]);
//            }
//        } catch (Exception e) {
//            logger.warning("Failed to parse resource update: " + e.getMessage());
//        }
//    }
//
//    private void handleError(String errorMessage) {
//        // Add error message to chat with distinctive style
//        chatMessages.getItems().add("[ERROR] " + errorMessage);
//    }
//}