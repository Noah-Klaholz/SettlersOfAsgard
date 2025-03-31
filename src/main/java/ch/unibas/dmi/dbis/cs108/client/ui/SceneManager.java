package ch.unibas.dmi.dbis.cs108.client.ui;

import ch.unibas.dmi.dbis.cs108.client.core.entities.Player;
import ch.unibas.dmi.dbis.cs108.client.networking.GameClient;
import ch.unibas.dmi.dbis.cs108.client.ui.controllers.GameScreenController;
import ch.unibas.dmi.dbis.cs108.client.ui.controllers.LobbyScreenController;
import ch.unibas.dmi.dbis.cs108.client.ui.controllers.LoginScreenController;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.logging.Logger;

public class SceneManager {
    private static final Logger logger = Logger.getLogger(SceneManager.class.getName());

    private final Stage primaryStage;
    private GameClient gameClient;
    private Player currentPlayer;

    // Controllers
    private GameScreenController gameController;
    private LoginScreenController loginController;
    private LobbyScreenController lobbyController;

    public SceneManager(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    public void showLoginScreen() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login_screen.fxml"));
        Parent root = loader.load();

        loginController = loader.getController();
        loginController.setSceneManager(this);

        setScene(root, "Login");
    }

    public void showLobbyScreen() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/lobby_screen.fxml"));
        Parent root = loader.load();

        lobbyController = loader.getController();
        lobbyController.setSceneManager(this);
        lobbyController.setGameClient(gameClient);

        setScene(root, "Game Lobby");
    }

    public void showGameScreen() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/game_screen.fxml"));
        Parent root = loader.load();

        gameController = loader.getController();
        gameController.setSceneManager(this);
        gameController.setGameClient(gameClient);

        // Register the controller as a listener for game events
        if (gameClient != null) {
            gameClient.registerGameEventListener(gameController);
        }

        setScene(root, "Arcane Conquest - Game");
    }

    private void setScene(Parent root, String title) {
        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.setTitle(title);
        primaryStage.centerOnScreen();
    }

    public GameClient getGameClient() {
        return gameClient;
    }

    public void setGameClient(GameClient client) {
        this.gameClient = client;
    }

    public Player getCurrentPlayer() {
        return currentPlayer;
    }

    public void setCurrentPlayer(Player player) {
        this.currentPlayer = player;
    }
}