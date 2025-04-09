package ch.unibas.dmi.dbis.cs108.client.communication;

import ch.unibas.dmi.dbis.cs108.client.core.Game;
import ch.unibas.dmi.dbis.cs108.client.networking.NetworkController;
import ch.unibas.dmi.dbis.cs108.client.networking.events.*;
import ch.unibas.dmi.dbis.cs108.client.networking.events.EventDispatcher.EventListener;
import ch.unibas.dmi.dbis.cs108.client.ui.events.SendChatEvent;
import ch.unibas.dmi.dbis.cs108.client.ui.events.SendCommandEvent;
import ch.unibas.dmi.dbis.cs108.client.ui.events.UIEventBus;

import java.util.Arrays;

/**
 * Mediator class that handles communication between the UI and the network layer.
 * It listens for events from the UI and forwards them to the network layer,
 * and also listens for events from the network layer and forwards them to the UI.
 */
public class CommunicationMediator {
    /**
     * The NetworkController instance used for network communication.
     * This controller handles sending and receiving messages over the network.
     */
    private final NetworkController networkController;
    /**
     * The Game instance representing the current game state.
     * This instance is used to update the game state based on network events.
     */
    private final Game game;

    /**
     * Constructor for CommunicationMediator.
     * Initializes the mediator with the network controller and game instance.
     *
     * @param networkController The network controller to handle network communication.
     * @param game              The game instance to update the game state.
     */
    public CommunicationMediator(NetworkController networkController, Game game) {
        this.networkController = networkController;
        this.game = game;
        registerUIListeners();
        registerNetworkListeners();
    }

    /**
     * Register core listeners for the game.
     * This method is currently a placeholder and can be implemented as needed.
     */
    private void registerCoreListeners() {
        // ToDo: Register core listeners if needed.
    }

    /**
     * Publish an info chat event to the UI.
     * This is used to inform the user about various events.
     * @param message the message to be published
     */
    private void publishInfoChatEvent(String message) {
        UIEventBus.getInstance().publish(new ch.unibas.dmi.dbis.cs108.client.ui.events.ChatMessageEvent(
                message,
                ChatMessageEvent.ChatType.INFO
        ));
    }

    /**
     * Check if the argument at index i is valid.
     * @param args the arguments array
     * @param i index of the argument
     * @return true if the argument is valid, false otherwise.
     */
    private boolean isValidArgument(String[] args, int i) {
        System.out.println(Arrays.toString(args));
        return args != null && args.length == (i+1) && !args[i].trim().isEmpty();
    }

    /**
     * Subscribes to UI events and forwards them to the network layer.
     */
    private void registerUIListeners() {
        UIEventBus.getInstance().subscribe(SendChatEvent.class, event -> {
            switch (event.getType()) {
                case GLOBAL:
                    networkController.sendGlobalChat(event.getMessage());
                    break;
                case LOBBY:
                    networkController.sendLobbyChat(event.getMessage());
                    break;
                case PRIVATE:
                    networkController.sendPrivateChat(event.getRecipient(),event.getMessage());
                    break;
                default:
                    System.err.println("Unknown chat type: " + event.getType());
            }
        });

        // Subscribe to Command events
        UIEventBus.getInstance().subscribe(SendCommandEvent.class, event -> {
            String input = event.getMessage();
            System.out.println("Command Event registered: " + input);
            String[] args = input.replace(event.getType().getCommand(), "").trim().split(" ");
            switch (event.getType()) {
                case EXIT:
                    // TODO handle exit command
                case CHANGENAME:
                    if (isValidArgument(args, 0)) {
                        networkController.changeName(args[0]);
                    } else {
                        publishInfoChatEvent("Invalid name, use: \n /changename <new_name>");
                    }
                    break;
                case JOINLOBBY:
                    if (isValidArgument(args, 0)) {
                        networkController.joinLobby(args[0]);
                    } else {
                        publishInfoChatEvent("Invalid lobbyname, use: \n /joinlobby <lobbyName>");
                    }
                    break;
                case LEAVELOBBY:
                    networkController.leaveLobby();
                    break;
                case CREATELOBBY:
                    if (isValidArgument(args, 0)) {
                        networkController.createLobby(args[0]);
                    } else {
                        publishInfoChatEvent("Invalid lobbyname, use: \n /create <lobbyName>");
                    }
                    break;
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
                    if (isValidArgument(args, 0)) {
                        networkController.listLobbyPlayers(args[0]);
                    } else {
                        publishInfoChatEvent("Invalid lobbyname, use: \n /listlobbyplayers <lobbyName>");
                    }
                    break;
                case GLOBALCHAT:
                    networkController.sendGlobalChat(input.replace("/global ", "")); // Handled differently, because spaces can be included in messages
                    break;
                case WHISPER:
                    if (isValidArgument(args, 1)) {
                        networkController.sendPrivateChat(args[0], input.replace("/whisper " + args[0] + " ", ""));
                    } else {
                        publishInfoChatEvent("Invalid playername, use: \n /whisper <playerName>");
                    }
                    break;
                case HELP:
                    publishInfoChatEvent("The following commands are available: \n" +
                            "/whisper <playername> <message> \n" +
                            "/global <message> \n" +
                            "/changename <newName> \n" +
                            "/joinlobby <lobbyname> \n" +
                            "/leavelobby \n" +
                            "/createlobby <lobbyname> \n" +
                            "/startgame \n" +
                            "/listlobbies \n" +
                            "/listallplayers \n" +
                            "/listlobbyplayers <lobbyname> \n" +
                            "/exit \n" +
                            "/help \n" +
                            "/helpgame");
                    break;
                case HELPGAME:
                    publishInfoChatEvent("The following Game-commands are available: \n" +
                            "/buytile <x> <y> \n" +
                            "/placestructure <x> <y> <structureId> \n" +
                            "/usestructure <x> <y> <structureType> <useType> \n" +
                            "/buystatue <statueId> \n" +
                            "/usestatue <x> <y> <statueType> <targetPlayer> \n" +
                            "/upgradestatue <x> <y> <statueId> \n" +
                            "/usefieldartifact <x> <y> <artifactId> <useType> \n" +
                            "/useplayerartifact <artifactId> <useType> <playerAimedAt> \n" +
                            "/status \n" +
                            "/prices \n" +
                            "Note: Coordinates are 0-indexed and IDs are 0 indexed numbers.");
                    break;
                case BUYTILE:
                    if (isValidArgument(args, 1)) {
                        networkController.buyTile(Integer.parseInt(args[0]), Integer.parseInt(args[1]));
                    } else {
                        publishInfoChatEvent("Invalid coordinates, use: \n /buytile <x> <y>");
                    }
                    break;
                case PLACESTRUCTURE:
                    if (isValidArgument(args, 2)) {
                        networkController.placeStructure(Integer.parseInt(args[0]), Integer.parseInt(args[1]), Integer.parseInt(args[2]));
                    } else {
                        publishInfoChatEvent("Invalid arguments, use: \n /placestructure <x> <y> <structureId>");
                    }
                    break;
                case USESTRUCTURE:
                    if (isValidArgument(args, 3)) {
                        networkController.useStructure(Integer.parseInt(args[0]), Integer.parseInt(args[1]), Integer.parseInt(args[2]), args[3]);
                    } else {
                        publishInfoChatEvent("Invalid arguments, use: \n /usestructure <x> <y> <structureId> <useType>");
                    }
                    break;
                case UPGRADESTATUE:
                    if (isValidArgument(args, 2)) {
                        networkController.upgradeStatue(Integer.parseInt(args[0]), Integer.parseInt(args[1]), Integer.parseInt(args[2]));
                    } else {
                        publishInfoChatEvent("Invalid arguments, use: \n /upgradestatue <x> <y> <statueId> ");
                    }
                    break;
                case BUYSTATUE:
                    if (isValidArgument(args, 2)) {
                        networkController.buyStatue(Integer.parseInt(args[0]));
                    } else {
                        publishInfoChatEvent("Invalid arguments, use: \n /buystatue <statueId>");
                    }
                    break;
                case USESTATUE:
                    if (isValidArgument(args, 3)) {
                        networkController.useStatue(Integer.parseInt(args[0]), Integer.parseInt(args[1]), Integer.parseInt(args[2]), args[3]);
                    } else {
                        publishInfoChatEvent("Invalid arguments, use: \n /usestatue <x> <y> <statueId> <useType> <playerName>");
                    }
                    break;
                case USEFIELDARTIFACT:
                    if (isValidArgument(args, 3)) {
                        networkController.useFieldArtifact(Integer.parseInt(args[0]), Integer.parseInt(args[1]), Integer.parseInt(args[2]), args[3]);
                    } else {
                        publishInfoChatEvent("Invalid arguments, use: \n /usefieldartifact <x> <y> <artifactId> <useType>");
                    }
                    break;
                case USEPLAYERARTIFACT:
                    if (isValidArgument(args, 2)) {
                        networkController.usePlayerArtifact(Integer.parseInt(args[0]), args[1], args[2]);
                    } else {
                        publishInfoChatEvent("Invalid arguments, use: \n /useplayerartifact <artifactId> <useType> <playerAimedAt>");
                    }
                    break;
                case STATUS:
                    networkController.getGameState();
                    break;
                case PRICES:
                    networkController.getPrices();
                    break;
                default:
                    publishInfoChatEvent("Unknown command type: " + event.getType() + "\n Use /help to see available commands.");
            }
        });
    }

    /**
     * Listens for network events and publishes corresponding UI events.
     */
    private void registerNetworkListeners() {
        EventDispatcher.getInstance().registerListener(ChatMessageEvent.class, new EventListener<ChatMessageEvent>() {
            @Override
            public void onEvent(ChatMessageEvent networkEvent) {
                // Transform the networking chat event into a UI chat event.
                ch.unibas.dmi.dbis.cs108.client.ui.events.ChatMessageEvent uiEvent =
                        new ch.unibas.dmi.dbis.cs108.client.ui.events.ChatMessageEvent(
                                networkEvent.getSender() + ": " + networkEvent.getContent(), networkEvent.getType()
                        );
                UIEventBus.getInstance().publish(uiEvent);
            }

            @Override
            public Class<ChatMessageEvent> getEventType() {
                return ChatMessageEvent.class;
            }
        });

        EventDispatcher.getInstance().registerListener(ErrorEvent.class, new EventListener<ErrorEvent>() {
            @Override
            public void onEvent(ErrorEvent networkEvent) {
                // Transform the networking chat event into a UI chat event.
                ch.unibas.dmi.dbis.cs108.client.ui.events.ErrorEvent uiEvent =
                        new ch.unibas.dmi.dbis.cs108.client.ui.events.ErrorEvent(
                                networkEvent.getErrorCode(),
                                networkEvent.getErrorMessage(),
                                networkEvent.getSeverity()
                        );
                UIEventBus.getInstance().publish(uiEvent);
            }

            @Override
            public Class<ErrorEvent> getEventType() {
                return ErrorEvent.class;
            }
        });

        EventDispatcher.getInstance().registerListener(ReceiveCommandEvent.class, new EventListener<ReceiveCommandEvent>() {
            @Override
            public void onEvent(ReceiveCommandEvent networkEvent) {
                // Transform the networking chat event into a UI chat event.
                ch.unibas.dmi.dbis.cs108.client.ui.events.ReceiveCommandEvent uiEvent =
                        new ch.unibas.dmi.dbis.cs108.client.ui.events.ReceiveCommandEvent(
                                networkEvent.getMessage()
                        );
                UIEventBus.getInstance().publish(uiEvent);
            }

            @Override
            public Class<ReceiveCommandEvent> getEventType() {
                return ReceiveCommandEvent.class;
            }
        });

        EventDispatcher.getInstance().registerListener(NameChangeResponseEvent.class, new EventListener<NameChangeResponseEvent>() {
            @Override
            public void onEvent(NameChangeResponseEvent event) {
                if (event.isSuccess()) {
                    // Update game core with new name
                     game.updatePlayerName(event.getNewName());

                    // Create a UI event to inform UI components
                    UIEventBus.getInstance().publish(new ch.unibas.dmi.dbis.cs108.client.ui.events.NameChangeResponseEvent(
                            true,
                            event.getNewName(),
                            event.getMessage()
                    ));
                } else {
                    // Just inform UI of the failure
                    UIEventBus.getInstance().publish(new ch.unibas.dmi.dbis.cs108.client.ui.events.NameChangeResponseEvent(
                            false,
                            null,
                            event.getMessage()
                    ));
                }
            }

            @Override
            public Class<NameChangeResponseEvent> getEventType() {
                return NameChangeResponseEvent.class;
            }
        });
    }

    // ToDo: Implement this method to update the game state and UI based on network events.
//    private void updateGameFromNetworkEvent(GameStateUpdateEvent event) {
//        // Logic to update the game based on network events
//        // For example: update resources, board state, etc.
//    }
}