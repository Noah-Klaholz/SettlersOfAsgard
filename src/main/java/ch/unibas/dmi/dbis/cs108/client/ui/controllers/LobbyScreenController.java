package ch.unibas.dmi.dbis.cs108.client.ui.controllers;

import ch.unibas.dmi.dbis.cs108.client.core.observer.GameEventListener;
import ch.unibas.dmi.dbis.cs108.client.networking.GameClient;
import ch.unibas.dmi.dbis.cs108.client.ui.SceneManager;
import ch.unibas.dmi.dbis.cs108.shared.protocol.CommunicationAPI;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;

import java.io.IOException;
import java.util.logging.Logger;

public class LobbyScreenController implements GameEventListener {
    private static final Logger logger = Logger.getLogger(LobbyScreenController.class.getName());

    @FXML
    private ListView<String> lobbyList;
    @FXML
    private TextField lobbyNameField;
    @FXML
    private Label errorMessage;

    private SceneManager sceneManager;
    private GameClient gameClient;

    public void setSceneManager(SceneManager sceneManager) {
        this.sceneManager = sceneManager;
    }

    public void setGameClient(GameClient gameClient) {
        this.gameClient = gameClient;

        // Register as event listener to receive server updates
        if (gameClient != null) {
            gameClient.registerGameEventListener(this);

            // Request list of available lobbies
            gameClient.listLobbies();
        }
    }

    @FXML
    public void handleCreateLobby() {
        String lobbyName = lobbyNameField.getText().trim();

        if (lobbyName.isEmpty()) {
            errorMessage.setText("Lobby name cannot be empty");
            return;
        }

        if (gameClient != null) {
            gameClient.createLobby(lobbyName);
            lobbyNameField.clear();
        }
    }

    @FXML
    public void handleJoinLobby() {
        String selectedLobby = lobbyList.getSelectionModel().getSelectedItem();

        if (selectedLobby == null) {
            errorMessage.setText("Please select a lobby to join");
            return;
        }

        if (gameClient != null) {
            // Parse lobby name from selection (format may vary based on server response)
            String lobbyName = selectedLobby.split(":")[0].trim();
            gameClient.joinLobby(lobbyName);
        }
    }

   @Override
    public void onMessageReceived(String serverMessage) {
        // Process incoming server messages
        Platform.runLater(() -> {
            String[] parts = serverMessage.split("[:$]", 2);
            if (parts.length >= 2) {
                String command = parts[0];
                String content = parts[1];

                try {
                    CommunicationAPI.NetworkProtocol.Commands cmd =
                            CommunicationAPI.NetworkProtocol.Commands.fromCommand(command);

                    switch (cmd) {
                        case LISTLOBBIES:
                            updateLobbyList(content);
                            break;

                        case JOIN:
                            // Successfully joined a lobby, try to go to game screen
                            try {
                                sceneManager.showGameScreen();
                            } catch (IOException e) {
                                logger.severe("Error loading game screen: " + e.getMessage());
                                errorMessage.setText("Error loading game screen");
                            }
                            break;

                        case ERROR:
                            errorMessage.setText(content);
                            break;

                        default:
                            // Other messages can be logged
                            logger.info("Received message: " + command + " - " + content);
                    }
                } catch (IllegalArgumentException e) {
                    logger.warning("Unknown command received: " + command);
                }
            }
        });
    }

    private void updateLobbyList(String lobbiesData) {
        lobbyList.getItems().clear();

        // Parse lobby list from server response
        String[] lobbies = lobbiesData.split(",");
        for (String lobby : lobbies) {
            if (!lobby.trim().isEmpty()) {
                lobbyList.getItems().add(lobby.trim());
            }
        }
    }
}
