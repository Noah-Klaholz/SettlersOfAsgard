//package ch.unibas.dmi.dbis.cs108.client.ui.controllers;
//
//import ch.unibas.dmi.dbis.cs108.client.core.entities.Player;
//import ch.unibas.dmi.dbis.cs108.client.ui.SceneManager;
//import javafx.fxml.FXML;
//import javafx.scene.control.Label;
//import javafx.scene.control.PasswordField;
//import javafx.scene.control.TextField;
//
//import java.util.logging.Logger;
//
//public class LoginScreenController {
//    private static final Logger logger = Logger.getLogger(LoginScreenController.class.getName());
//
//    @FXML private TextField usernameField;
//    @FXML private PasswordField passwordField;
//    @FXML private Label errorMessage;
//
//    private SceneManager sceneManager;
//
//    public void setSceneManager(SceneManager sceneManager) {
//        this.sceneManager = sceneManager;
//    }
//
//    @FXML
//    public void handleLogin() {
//        String username = usernameField.getText().trim();
//        String password = passwordField.getText(); // Not used for now, but available for future authentication
//
//        if (username.isEmpty()) {
//            errorMessage.setText("Username cannot be empty");
//            return;
//        }
//
//        try {
//            // Default server connection settings
//            String serverIp = "localhost";
//            int serverPort = 8080;
//
//            // Create player entity
//            Player player = new Player(username);
//            sceneManager.setCurrentPlayer(player);
//
//            // Initialize game client and connect to server
//            GameClient gameClient = new GameClient(serverIp, serverPort, player);
//            sceneManager.setGameClient(gameClient);
//
//            if (gameClient.isConnected()) {
//                // On successful connection, navigate to lobby screen
//                sceneManager.showLobbyScreen();
//            } else {
//                errorMessage.setText("Failed to connect to server");
//            }
//        } catch (Exception e) {
//            logger.severe("Login error: " + e.getMessage());
//            errorMessage.setText("Error: " + e.getMessage());
//        }
//    }
//}