// File: GameApplication.java
package ch.unibas.dmi.dbis.cs108.client.app;

import ch.unibas.dmi.dbis.cs108.client.communication.CommunicationMediator;
import ch.unibas.dmi.dbis.cs108.client.core.entities.Player;
import ch.unibas.dmi.dbis.cs108.client.networking.NetworkController;
import ch.unibas.dmi.dbis.cs108.client.networking.events.EventDispatcher;
import ch.unibas.dmi.dbis.cs108.client.ui.SceneManager;
import ch.unibas.dmi.dbis.cs108.client.ui.events.SendChatEvent;
import ch.unibas.dmi.dbis.cs108.client.ui.events.UIEventBus;
import javafx.application.Application;
import javafx.stage.Stage;

import java.util.List;

public class GameApplication extends Application {
    private NetworkController networkController;
    private UIEventBus uiEventBus;
    private EventDispatcher networkEventDispatcher;
    private Player localPlayer;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        // Retrieve command-line parameters.
        List<String> params = getParameters().getRaw();
        // Expecting: client <serverip>:<serverport> [username]
        String username = "Player1";
        if (params.size() >= 3) {
            username = params.get(2);
        }
        localPlayer = new Player(username);

        // Extract server ip and port from the second argument.
        if (params.size() >= 2) {
            String serverInfo = params.get(1); // expected format: serverip:serverport
            String[] parts = serverInfo.split(":");
            if (parts.length == 2) {
                String serverIp = parts[0];
                int serverPort = Integer.parseInt(parts[1]);
                networkController = new NetworkController(localPlayer);
                // Assuming your NetworkController has a method to connect to the server.
                networkController.connect(serverIp, serverPort);
            } else {
                System.err.println("Invalid server address format. Expected <serverip>:<serverport>");
                System.exit(1);
            }
        } else {
            System.err.println("Server address not provided");
            System.exit(1);
        }

        uiEventBus = UIEventBus.getInstance();
        networkEventDispatcher = EventDispatcher.getInstance();

        // Initialize CommunicationMediator to wire UI and network messages.
        new CommunicationMediator(networkController);
//      REMOVED: setupUIEventHandlers();

        // Initialize and display the main menu scene.
        SceneManager sceneManager = SceneManager.getInstance();
        sceneManager.setPrimaryStage(primaryStage);
        primaryStage.setTitle("Settlers of Asgard");
        primaryStage.setWidth(1024);
        primaryStage.setHeight(768);
        primaryStage.setMinWidth(800);
        primaryStage.setMinHeight(600);

        // Start with MAIN_MENU instead of directly GAME for proper initialization
        sceneManager.switchToScene(SceneManager.SceneType.GAME);
        primaryStage.show();
    }

    private void setupUIEventHandlers() {
        uiEventBus.subscribe(SendChatEvent.class, event -> {
            switch (event.getType()) {
                case GLOBAL:
                    networkController.sendGlobalChat(event.getMessage());
                    break;
                case LOBBY:
                    networkController.sendLobbyChat(event.getMessage());
                    break;
                case PRIVATE:
                    networkController.sendPrivateChat(event.getRecipient(), event.getMessage());
                    break;
            }
        });
    }

    @Override
    public void stop() throws Exception {
        // Gracefully close the network connection
        if (networkController != null) {
            networkController.disconnect();
        }
        // Perform any additional cleanup tasks if necessary
        super.stop();
    }
}