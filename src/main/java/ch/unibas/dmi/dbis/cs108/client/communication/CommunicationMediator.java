package ch.unibas.dmi.dbis.cs108.client.communication;

import ch.unibas.dmi.dbis.cs108.client.core.Game;
import ch.unibas.dmi.dbis.cs108.client.networking.NetworkController;
import ch.unibas.dmi.dbis.cs108.client.networking.events.ChatMessageEvent;
import ch.unibas.dmi.dbis.cs108.client.networking.events.EventDispatcher;
import ch.unibas.dmi.dbis.cs108.client.networking.events.EventDispatcher.EventListener;
import ch.unibas.dmi.dbis.cs108.client.networking.events.NameChangeResponseEvent;
import ch.unibas.dmi.dbis.cs108.client.networking.events.ReceiveCommandEvent;
import ch.unibas.dmi.dbis.cs108.client.ui.events.SendChatEvent;
import ch.unibas.dmi.dbis.cs108.client.ui.events.SendCommandEvent;
import ch.unibas.dmi.dbis.cs108.client.ui.events.UIEventBus;

public class CommunicationMediator {
    private final NetworkController networkController;
    private final Game game;

    public CommunicationMediator(NetworkController networkController, Game game) {
        this.networkController = networkController;
        this.game = game;
        registerUIListeners();
        registerNetworkListeners();
    }

    private void registerCoreListeners() {
        // ToDo: Register core listeners if needed.
    }

    // Subscribes to UI events and forwards them to the network layer.
    private void registerUIListeners() {
        UIEventBus.getInstance().subscribe(SendChatEvent.class, event -> {
            // Forward a global chat message from the UI to the networking controller.
            networkController.sendGlobalChat(event.getMessage());
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
                    if (args.length == 1) {
                        networkController.changeName(args[0]);
                    } else {
                        //TODO handle invalid name
                    }
                    break;
                case JOINLOBBY:
                    if (args.length == 1) {
                        networkController.joinLobby(args[0]);
                    } else {
                        //TODO handle invalid name
                    }
                    break;
                case LEAVELOBBY:
                    networkController.leaveLobby();
                    break;
                case CREATELOBBY:
                    if (args.length == 1) {
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
                    if (args.length == 3) {
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

    // Listens for network events and publishes corresponding UI events.
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