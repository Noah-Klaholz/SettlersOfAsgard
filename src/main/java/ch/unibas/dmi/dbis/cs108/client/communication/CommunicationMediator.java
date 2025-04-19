package ch.unibas.dmi.dbis.cs108.client.communication;

import ch.unibas.dmi.dbis.cs108.client.core.Game;
import ch.unibas.dmi.dbis.cs108.client.networking.NetworkController;
import ch.unibas.dmi.dbis.cs108.client.networking.events.*;
import ch.unibas.dmi.dbis.cs108.client.ui.events.UIEventBus;
import ch.unibas.dmi.dbis.cs108.client.ui.events.lobby.CreateLobbyResponseEvent;
import ch.unibas.dmi.dbis.cs108.client.ui.events.lobby.LobbyLeftEvent;
import ch.unibas.dmi.dbis.cs108.client.ui.events.lobby.LobbyListResponseEvent;

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
     *
     * @param message the message to be published
     */
    private void publishInfoChatEvent(String message) {
        // ToDo: Implement this method to publish info chat events.
        UIEventBus.getInstance().publish(new ch.unibas.dmi.dbis.cs108.client.ui.events.chat.GlobalChatEvent(
                message,
                ch.unibas.dmi.dbis.cs108.client.ui.events.chat.GlobalChatEvent.ChatType.SYSTEM
        ));
    }

    /**
     * Check if the argument at index i is valid.
     *
     * @param args the arguments array
     * @param i    index of the argument
     * @return true if the argument is valid, false otherwise.
     */
    private boolean isValidArgument(String[] args, int i) {
        System.out.println(Arrays.toString(args));
        return args != null && args.length == (i + 1) && !args[i].trim().isEmpty();
    }

    private void registerUIListeners() {
        // Chat Events
        UIEventBus.getInstance().subscribe(ch.unibas.dmi.dbis.cs108.client.ui.events.chat.GlobalChatEvent.class, event -> {
            // Only send if the sender is null, indicating it's a user-initiated message, not one received from the network.
            if (event.getSender() == null) {
                networkController.sendGlobalChat(event.getContent());
            }
        });

        UIEventBus.getInstance().subscribe(ch.unibas.dmi.dbis.cs108.client.ui.events.chat.LobbyChatEvent.class, event -> {
            if (event.getSender() == null) {
                networkController.sendLobbyChat(event.getMessage());
            }
        });

        UIEventBus.getInstance().subscribe(ch.unibas.dmi.dbis.cs108.client.ui.events.chat.WhisperChatEvent.class, event ->
                networkController.sendPrivateChat(event.getRecipient(), event.getMessage()));

        // Lobby Events
        UIEventBus.getInstance().subscribe(ch.unibas.dmi.dbis.cs108.client.ui.events.lobby.CreateLobbyRequestEvent.class, event ->
                networkController.createLobby(event.getLobbyName()));

        UIEventBus.getInstance().subscribe(ch.unibas.dmi.dbis.cs108.client.ui.events.lobby.JoinLobbyRequestEvent.class, event ->
                networkController.joinLobby(event.getLobbyId()));

        UIEventBus.getInstance().subscribe(ch.unibas.dmi.dbis.cs108.client.ui.events.lobby.LeaveLobbyRequestEvent.class, event ->
                networkController.leaveLobby());

        UIEventBus.getInstance().subscribe(ch.unibas.dmi.dbis.cs108.client.ui.events.lobby.LobbyListRequestEvent.class, event ->
                networkController.listLobbies());

        UIEventBus.getInstance().subscribe(ch.unibas.dmi.dbis.cs108.client.ui.events.lobby.StartGameRequestEvent.class, event ->
                networkController.startGame());

        // Game Events
        UIEventBus.getInstance().subscribe(ch.unibas.dmi.dbis.cs108.client.ui.events.game.BuyTileUIEvent.class, event ->
                networkController.buyTile(event.getX(), event.getY()));

        UIEventBus.getInstance().subscribe(ch.unibas.dmi.dbis.cs108.client.ui.events.game.PlaceStructureUIEvent.class, event ->
                networkController.placeStructure(event.getX(), event.getY(), event.getStructureId()));

        UIEventBus.getInstance().subscribe(ch.unibas.dmi.dbis.cs108.client.ui.events.game.UseStructureUIEvent.class, event ->
                networkController.useStructure(event.getX(), event.getY(), event.getStructureId(), event.getUseType()));

        UIEventBus.getInstance().subscribe(ch.unibas.dmi.dbis.cs108.client.ui.events.game.BuyStatueUIEvent.class, event ->
                networkController.buyStatue(event.getStatueId()));

        UIEventBus.getInstance().subscribe(ch.unibas.dmi.dbis.cs108.client.ui.events.game.UseStatueUIEvent.class, event ->
                networkController.useStatue(event.getX(), event.getY(), event.getStatueId(), event.getUseType()));

        UIEventBus.getInstance().subscribe(ch.unibas.dmi.dbis.cs108.client.ui.events.game.UseFieldArtifactUIEvent.class, event ->
                networkController.useFieldArtifact(event.getX(), event.getY(), event.getArtifactId(), event.getUseType()));

        UIEventBus.getInstance().subscribe(ch.unibas.dmi.dbis.cs108.client.ui.events.game.UsePlayerArtifactUIEvent.class, event ->
                networkController.usePlayerArtifact(event.getArtifactId(), event.getUseType(),
                        event.getTargetPlayer().orElse(null)));

        UIEventBus.getInstance().subscribe(ch.unibas.dmi.dbis.cs108.client.ui.events.game.EndTurnUIEvent.class, event ->
                networkController.endTurn());

        UIEventBus.getInstance().subscribe(ch.unibas.dmi.dbis.cs108.client.ui.events.game.PricesUIEvent.class, event ->
                networkController.getPrices());

        // Admin Events
        UIEventBus.getInstance().subscribe(ch.unibas.dmi.dbis.cs108.client.ui.events.admin.ChangeNameUIEvent.class, event ->
                networkController.changeName(event.getNewName()));

        UIEventBus.getInstance().subscribe(ch.unibas.dmi.dbis.cs108.client.ui.events.admin.StatusUIEvent.class, event ->
                networkController.getGameState());

        UIEventBus.getInstance().subscribe(ch.unibas.dmi.dbis.cs108.client.ui.events.admin.LeaderboardRequestUIEvent.class, event ->
                networkController.getLeaderboard());
    }

    /**
     * Listens for network events and publishes corresponding UI events.
     */
    private void registerNetworkListeners() {
        // Chat Events
        EventDispatcher.getInstance().registerListener(ChatMessageEvent.class, new EventDispatcher.EventListener<ChatMessageEvent>() {
            @Override
            public void onEvent(ChatMessageEvent event) {
                ch.unibas.dmi.dbis.cs108.client.ui.events.chat.GlobalChatEvent.ChatType uiChatType;

                switch (event.getType()) {
                    case INFO:
                        uiChatType = ch.unibas.dmi.dbis.cs108.client.ui.events.chat.GlobalChatEvent.ChatType.SYSTEM;
                        UIEventBus.getInstance().publish(new ch.unibas.dmi.dbis.cs108.client.ui.events.chat.GlobalChatEvent(event.getContent(), event.getSender(), uiChatType));
                        break;
                    case LOBBY:
                        UIEventBus.getInstance().publish(new ch.unibas.dmi.dbis.cs108.client.ui.events.chat.LobbyChatEvent(event.getSender(), event.getSender(), event.getContent()));
                        break;
                    case PRIVATE:
                        //ToDo: Handle private chat messages
                        UIEventBus.getInstance().publish(new ch.unibas.dmi.dbis.cs108.client.ui.events.chat.WhisperChatEvent(event.getSender(), null, event.getContent()));
                        break;
                    default:
                        uiChatType = ch.unibas.dmi.dbis.cs108.client.ui.events.chat.GlobalChatEvent.ChatType.GLOBAL;
                        UIEventBus.getInstance().publish(new ch.unibas.dmi.dbis.cs108.client.ui.events.chat.GlobalChatEvent(event.getContent(), event.getSender(), uiChatType));
                }
            }

            @Override
            public Class<ChatMessageEvent> getEventType() {
                return ChatMessageEvent.class;
            }
        });

        // Error Events
        EventDispatcher.getInstance().registerListener(ErrorEvent.class, new EventDispatcher.EventListener<ErrorEvent>() {
            @Override
            public void onEvent(ErrorEvent event) {
                // Assuming ErrorEvent exists in UI package with similar structure
                // You may need to create this class if it doesn't exist
                UIEventBus.getInstance().publish(new ch.unibas.dmi.dbis.cs108.client.ui.events.ErrorEvent(
                        event.getErrorCode(), event.getErrorMessage(), event.getSeverity()));
            }

            @Override
            public Class<ErrorEvent> getEventType() {
                return ErrorEvent.class;
            }
        });

        // Command Events (ReceiveCommandEvent)
        // Create a proper UI event class for commands if needed

        // Connection Events
        EventDispatcher.getInstance().registerListener(ConnectionEvent.class, new EventDispatcher.EventListener<ConnectionEvent>() {
            @Override
            public void onEvent(ConnectionEvent event) {
                UIEventBus.getInstance().publish(new ch.unibas.dmi.dbis.cs108.client.ui.events.admin.ConnectionStatusEvent(
                        event.getState(), event.getMessage()));
            }

            @Override
            public Class<ConnectionEvent> getEventType() {
                return ConnectionEvent.class;
            }
        });

        // Name Change Events
        EventDispatcher.getInstance().registerListener(NameChangeResponseEvent.class, new EventDispatcher.EventListener<NameChangeResponseEvent>() {
            @Override
            public void onEvent(NameChangeResponseEvent event) {
                if (event.isSuccess()) {
                    // Update game core with new name
                    game.updatePlayerName(event.getNewName());
                }

                // Create a UI event to inform UI components
                UIEventBus.getInstance().publish(new ch.unibas.dmi.dbis.cs108.client.ui.events.admin.NameChangeResponseEvent(
                        event.isSuccess(), event.getNewName(), event.getMessage()));
            }

            @Override
            public Class<NameChangeResponseEvent> getEventType() {
                return NameChangeResponseEvent.class;
            }
        });

        EventDispatcher.getInstance().registerListener(LobbyListEvent.class, new EventDispatcher.EventListener<LobbyListEvent>() {
            @Override
            public void onEvent(LobbyListEvent event) {
                    UIEventBus.getInstance().publish(new LobbyListResponseEvent(event.getLobbies()));
                }

            @Override
            public Class<LobbyListEvent> getEventType() {
                return LobbyListEvent.class;
            }
        });

        EventDispatcher.getInstance().registerListener(LobbyEvent.class, new EventDispatcher.EventListener<LobbyEvent>() {
            @Override
            public void onEvent(LobbyEvent event) {
                switch (event.getAction()) {
                    case LEFT:
                        UIEventBus.getInstance().publish(new LobbyLeftEvent(event.getPlayerName()));
                        break;
                    case CREATED:
                        UIEventBus.getInstance().publish(new CreateLobbyResponseEvent(event.getLobbyName(), event.getPlayerName()));
                        break;
                    default:
                        break;
                }
            }

            @Override
            public Class<LobbyEvent> getEventType() {
                return LobbyEvent.class;
            }
        });

        EventDispatcher.getInstance().registerListener(LobbyJoinedEvent.class, new EventDispatcher.EventListener<LobbyJoinedEvent>() {
            @Override
            public void onEvent(LobbyJoinedEvent event) {
                UIEventBus.getInstance().publish(new ch.unibas.dmi.dbis.cs108.client.ui.events.lobby.LobbyJoinedEvent(
                        event.getLobbyId(), event.getPlayers(), event.isHost()));
            }

            @Override
            public Class<LobbyJoinedEvent> getEventType() {
                return LobbyJoinedEvent.class;
            }
        });
    }


    // ToDo: Implement this method to update the game state and UI based on network events.
//    private void updateGameFromNetworkEvent(GameStateUpdateEvent event) {
//        // Logic to update the game based on network events
//        // For example: update resources, board state, etc.
//    }
}