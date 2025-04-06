// File: GameApplication.java
package ch.unibas.dmi.dbis.cs108.client.app;

import ch.unibas.dmi.dbis.cs108.client.communication.CommunicationMediator;
import ch.unibas.dmi.dbis.cs108.client.core.entities.Player;
import ch.unibas.dmi.dbis.cs108.client.networking.NetworkController;
import ch.unibas.dmi.dbis.cs108.client.networking.events.EventDispatcher;
import ch.unibas.dmi.dbis.cs108.client.ui.SceneManager;
import ch.unibas.dmi.dbis.cs108.client.ui.events.SendChatEvent;
import ch.unibas.dmi.dbis.cs108.client.ui.events.SendCommandEvent;
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
        // Subscribe to Chat events
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

        // Subscribe to Command events
        uiEventBus.subscribe(SendCommandEvent.class, event -> {
            String input = event.getMessage();
            System.out.println(input);
            String[] args = input.replace(event.getType().getCommand(), "").trim().split(" ");
            switch (event.getType()) {
                case EXIT:
                    // TODO handle exit command
                case CHANGENAME:
                    if(args.length == 1) {
                        networkController.changeName(args[0]);
                    } else {
                        //TODO handle invalid name
                    }
                    break;
                case JOINLOBBY:
                    if(args.length == 1) {
                        networkController.joinLobby(args[0]);
                    } else {
                        //TODO handle invalid name
                    }
                    break;
                case LEAVELOBBY:
                    networkController.leaveLobby();
                    break;
                case CREATELOBBY:
                    if(args.length == 1) {
                        networkController.createLobby(args[0]);
                    } else {
                        //TODO handle invalid name
                    }
                case STARTGAME:
                    networkController.startGame();
                    break;
                case LISTLOBBIES:
                    networkController.listLobbies();
                    break;
                case LISTALLPLAYERS:
                    networkController.listAllPlayers();
                    break;
                case LISTLOBBYPLAYERS:
                    networkController.listLobbyPlayers();
                    break;
                case GLOBALCHAT:
                    networkController.sendGlobalChat(input.replace("/global ", "")); // Handled differently, because spaces can be included in messages
                    break;
                case HELP:
                    // TODO handle help command -> Maybe generalize this in a method in network controller
                    break;
                case BUYTILE:
                    if (args.length == 2) {
                        networkController.buyTile(Integer.parseInt(args[0]), Integer.parseInt(args[1]));
                    } else {
                        //TODO handle invalid coordinates
                    }
                    break;
                case PLACESTRUCTURE:
                    if (args.length == 3) {
                        networkController.placeStructure(Integer.parseInt(args[0]), Integer.parseInt(args[1]), args[2]);
                    } else {
                        //TODO handle invalid arguments
                    }
                    break;
                case USESTRUCTURE:
                    if (args.length == 4) {
                        networkController.useStructure(Integer.parseInt(args[0]), Integer.parseInt(args[1]), args[2], args[3]);
                    } else {
                        //TODO handle invalid arguments
                    }
                    break;
                case UPGRADESTATUE:
                    if(args.length == 3) {
                        networkController.upgradeStatue(Integer.parseInt(args[0]), Integer.parseInt(args[1]), args[2]);
                    } else {
                        //TODO handle invalid arguments
                    }
                case USESTATUE:
                    if (args.length == 4) {
                        networkController.useStatue(Integer.parseInt(args[0]), Integer.parseInt(args[1]), args[2], args[3]);
                    } else {
                        //TODO handle invalid arguments
                    }
                case USEFIELDARTIFACT:
                    if (args.length == 4) {
                        networkController.useFieldArtifact(Integer.parseInt(args[0]), Integer.parseInt(args[1]), Integer.parseInt(args[2]), args[3]);
                    } else {
                        //TODO handle invalid arguments
                    }
                case USEPLAYERARTIFACT:
                    if (args.length == 4) {
                        networkController.usePlayerArtifact(Integer.parseInt(args[0]), args[1]);
                    } else {
                        //TODO handle invalid arguments
                    }
                case STATUS:
                    networkController.getGameState();
                    break;
                case PRICES:
                    networkController.getPrices();
                    break;
                default:
                    System.err.println("Unknown command type: " + event.getType());
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