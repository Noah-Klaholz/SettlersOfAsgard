package ch.unibas.dmi.dbis.cs108.client.communication;

import ch.unibas.dmi.dbis.cs108.client.networking.NetworkController;
import ch.unibas.dmi.dbis.cs108.client.networking.events.*;
import ch.unibas.dmi.dbis.cs108.client.networking.events.LobbyJoinedEvent;
import ch.unibas.dmi.dbis.cs108.client.ui.events.UIEventBus;
import ch.unibas.dmi.dbis.cs108.client.ui.events.admin.ServerCommandEvent;
import ch.unibas.dmi.dbis.cs108.client.ui.events.lobby.*;
import ch.unibas.dmi.dbis.cs108.shared.protocol.CommunicationAPI;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Mediator class that handles communication between the UI and the network
 * layer.
 * It listens for events from the UI and forwards them to the network layer,
 * and also listens for events from the network layer and forwards them to the
 * UI.
 */
public class CommunicationMediator {
    private static final Logger LOGGER = Logger.getLogger(CommunicationMediator.class.getName()); // Add Logger
    /**
     * The NetworkController instance used for network communication.
     * This controller handles sending and receiving messages over the network.
     */
    private final NetworkController networkController;
    /**
     * The name of the player.
     */
    private String playerName;

    /**
     * Constructor for CommunicationMediator.
     * Initializes the mediator with the network controller and game instance.
     *
     * @param networkController The network controller to handle network
     *                          communication.
     */
    public CommunicationMediator(NetworkController networkController) {
        this.networkController = networkController;
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
                ch.unibas.dmi.dbis.cs108.client.ui.events.chat.GlobalChatEvent.ChatType.SYSTEM));
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
        UIEventBus.getInstance().subscribe(ch.unibas.dmi.dbis.cs108.client.ui.events.chat.GlobalChatEvent.class,
                event -> {
                    // Only send if the sender is null, indicating it's a user-initiated message,
                    // not one received from the network.
                    if (event.getSender() == null) {
                        networkController.sendGlobalChat(event.getContent());
                    }
                });

        UIEventBus.getInstance().subscribe(ch.unibas.dmi.dbis.cs108.client.ui.events.chat.LobbyChatEvent.class,
                event -> {
                    if (event.getSender() == null) {
                        networkController.sendLobbyChat(event.getMessage());
                    }
                });

        UIEventBus.getInstance().subscribe(ch.unibas.dmi.dbis.cs108.client.ui.events.chat.WhisperChatEvent.class,
                event -> networkController.sendPrivateChat(event.getRecipient(), event.getMessage()));

        // Lobby Events
        UIEventBus.getInstance().subscribe(
                ch.unibas.dmi.dbis.cs108.client.ui.events.lobby.CreateLobbyRequestEvent.class,
                event -> networkController.createLobby(event.getLobbyName()));

        UIEventBus.getInstance().subscribe(ch.unibas.dmi.dbis.cs108.client.ui.events.lobby.JoinLobbyRequestEvent.class,
                event -> networkController.joinLobby(event.getLobbyId()));

        UIEventBus.getInstance().subscribe(ch.unibas.dmi.dbis.cs108.client.ui.events.lobby.LeaveLobbyRequestEvent.class,
                event -> networkController.leaveLobby());

        UIEventBus.getInstance().subscribe(ch.unibas.dmi.dbis.cs108.client.ui.events.lobby.LobbyListRequestEvent.class,
                event -> networkController.listLobbies());

        UIEventBus.getInstance().subscribe(ch.unibas.dmi.dbis.cs108.client.ui.events.lobby.StartGameRequestEvent.class,
                event -> networkController.startGame());

        UIEventBus.getInstance().subscribe(
                ch.unibas.dmi.dbis.cs108.client.ui.events.lobby.UpdateLobbySettingsEvent.class,
                event -> {
                    // Assuming NetworkController has a method like updateLobbySetting
                    // networkController.updateLobbySetting(event.getSettingKey(),
                    // event.getSettingValue());
                    LOGGER.log(Level.INFO,
                            "UI Event: Update Lobby Settings requested (Key: {0}, Value: {1}) - Network call placeholder",
                            new Object[] { event.getSettingKey(), event.getSettingValue() });
                    // Example: If settingKey is "maxPlayers"
                    if ("maxPlayers".equals(event.getSettingKey())) {
                        try {
                            int maxPlayers = Integer.parseInt(event.getSettingValue());
                            // networkController.setMaxPlayers(maxPlayers); // Assuming such a method exists
                            LOGGER.log(Level.INFO, "Placeholder: NetworkController.setMaxPlayers({0})", maxPlayers);
                        } catch (NumberFormatException e) {
                            LOGGER.log(Level.WARNING,
                                    "Invalid maxPlayers value received from UI: " + event.getSettingValue(), e);
                        }
                    }
                });

        // Game Events
        UIEventBus.getInstance().subscribe(ch.unibas.dmi.dbis.cs108.client.ui.events.game.BuyTileUIEvent.class,
                event -> networkController.buyTile(event.getX(), event.getY()));

        UIEventBus.getInstance().subscribe(ch.unibas.dmi.dbis.cs108.client.ui.events.game.PlaceStructureUIEvent.class,
                event -> networkController.placeStructure(event.getX(), event.getY(), event.getStructureId()));

        UIEventBus.getInstance().subscribe(ch.unibas.dmi.dbis.cs108.client.ui.events.game.UseStructureUIEvent.class,
                event -> networkController.useStructure(event.getRow(), event.getCol(), event.getStructureId(), // Use
                        // getRow(),
                        // getCol()
                        event.getUseType()));

        UIEventBus.getInstance().subscribe(ch.unibas.dmi.dbis.cs108.client.ui.events.game.BuyStatueUIEvent.class,
                event -> networkController.buyStatue(event.getStatueId()));

        UIEventBus.getInstance().subscribe(ch.unibas.dmi.dbis.cs108.client.ui.events.game.UseStatueUIEvent.class,
                event -> networkController.useStatue(event.getX(), event.getY(), event.getStatueId(), // Use
                        // getRow(),
                        // getCol()
                        event.getUseType()));

        UIEventBus.getInstance().subscribe(ch.unibas.dmi.dbis.cs108.client.ui.events.game.UseFieldArtifactUIEvent.class,
                event -> networkController.useFieldArtifact(event.getX(), event.getY(), event.getArtifactId(), // Use
                        // getRow(),
                        // getCol()
                        event.getUseType()));

        UIEventBus.getInstance().subscribe(
                ch.unibas.dmi.dbis.cs108.client.ui.events.game.UsePlayerArtifactUIEvent.class,
                event -> networkController.usePlayerArtifact(event.getArtifactId(), event.getUseType(),
                        event.getTargetPlayer().orElse(null)));

        UIEventBus.getInstance().subscribe(ch.unibas.dmi.dbis.cs108.client.ui.events.game.EndTurnUIEvent.class,
                event -> networkController.endTurn());

        UIEventBus.getInstance().subscribe(ch.unibas.dmi.dbis.cs108.client.ui.events.game.PricesUIEvent.class,
                event -> networkController.getPrices());

        // Admin Events
        UIEventBus.getInstance().subscribe(ch.unibas.dmi.dbis.cs108.client.ui.events.admin.ChangeNameUIEvent.class,
                event -> networkController.changeName(event.getNewName()));

        UIEventBus.getInstance().subscribe(ch.unibas.dmi.dbis.cs108.client.ui.events.admin.StatusUIEvent.class,
                event -> networkController.getGameState());

        UIEventBus.getInstance().subscribe(
                ch.unibas.dmi.dbis.cs108.client.ui.events.admin.LeaderboardRequestUIEvent.class,
                event -> networkController.getLeaderboard());
    }

    /**
     * Listens for network events and publishes corresponding UI events.
     */
    private void registerNetworkListeners() {
        // Chat Events
        EventDispatcher.getInstance().registerListener(ChatMessageEvent.class,
                new EventDispatcher.EventListener<ChatMessageEvent>() {
                    @Override
                    public void onEvent(ChatMessageEvent event) {
                        switch (event.getType()) {
                            case INFO:
                                UIEventBus.getInstance()
                                        .publish(new ch.unibas.dmi.dbis.cs108.client.ui.events.chat.GlobalChatEvent(
                                                event.getContent(), event.getSender(),
                                                ch.unibas.dmi.dbis.cs108.client.ui.events.chat.GlobalChatEvent.ChatType.SYSTEM));
                                break;
                            case LOBBY:
                                // Assuming the UI knows the current lobby ID contextually
                                UIEventBus.getInstance()
                                        .publish(new ch.unibas.dmi.dbis.cs108.client.ui.events.chat.LobbyChatEvent(
                                                null, event.getSender(), event.getContent())); // Lobby ID is null,
                                // sender is provided
                                break;
                            case PRIVATE:
                                // Recipient should be the local player when receiving
                                UIEventBus.getInstance()
                                        .publish(new ch.unibas.dmi.dbis.cs108.client.ui.events.chat.WhisperChatEvent(
                                                event.getSender(), playerName, event.getContent())); // Sender,
                                // Recipient
                                // (self), Message
                                break;
                            default: // Treat as GLOBAL
                                UIEventBus.getInstance()
                                        .publish(new ch.unibas.dmi.dbis.cs108.client.ui.events.chat.GlobalChatEvent(
                                                event.getContent(), event.getSender(),
                                                ch.unibas.dmi.dbis.cs108.client.ui.events.chat.GlobalChatEvent.ChatType.GLOBAL));
                        }
                    }

                    @Override
                    public Class<ChatMessageEvent> getEventType() {
                        return ChatMessageEvent.class;
                    }
                });

        // Error Events
        EventDispatcher.getInstance().registerListener(ErrorEvent.class,
                new EventDispatcher.EventListener<ErrorEvent>() {
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
        EventDispatcher.getInstance().registerListener(ReceiveCommandEvent.class,
                new EventDispatcher.EventListener<ReceiveCommandEvent>() {
                    @Override
                    public void onEvent(ReceiveCommandEvent event) {
                        LOGGER.log(Level.INFO, "Received command: {0} with message: {1}",
                                new Object[] { event.getType(), event.getMessage() });
                        // Parse command and publish specific UI events
                        // Example: Handle a KICK command
                        if (event.getType() == CommunicationAPI.NetworkProtocol.Commands.EXIT) {
                            // Assuming message format is "PlayerName$Reason" or just "Reason"
                            String[] parts = event.getMessage().split("\\$", 2);
                            String reason = parts.length > 1 ? parts[1] : "No reason specified.";
                            String targetPlayer = parts[0]; // May or may not be the local player

                            // Publish a generic server command event for the UI to handle
                            UIEventBus.getInstance().publish(new ServerCommandEvent(
                                    "KICK", List.of(targetPlayer, reason), "You have been kicked: " + reason));
                            // UI needs to check if targetPlayer matches local player and disconnect if so.
                        } else if (event.getType() == CommunicationAPI.NetworkProtocol.Commands.LEAVE) {
                            // Assuming message is "LobbyID$PlayerName"
                            String[] parts = event.getMessage().split("\\$", 2);
                            if (parts.length == 2) {
                                UIEventBus.getInstance().publish(new PlayerLeftLobbyEvent(parts[0], parts[1]));
                            }
                        }
                        // Add more command handlers as needed (e.g., GAME_UPDATE, SHUTDOWN_NOTICE)
                    }

                    @Override
                    public Class<ReceiveCommandEvent> getEventType() {
                        return ReceiveCommandEvent.class;
                    }
                });

        // Connection Events
        EventDispatcher.getInstance().registerListener(ConnectionEvent.class,
                new EventDispatcher.EventListener<ConnectionEvent>() {
                    @Override
                    public void onEvent(ConnectionEvent event) {
                        // ToDo: Handle connection events
                    }

                    @Override
                    public Class<ConnectionEvent> getEventType() {
                        return ConnectionEvent.class;
                    }
                });

        // Name Change Events
        EventDispatcher.getInstance().registerListener(NameChangeResponseEvent.class,
                new EventDispatcher.EventListener<NameChangeResponseEvent>() {
                    @Override
                    public void onEvent(NameChangeResponseEvent event) {
                        Logger.getGlobal().info("NameChangeResponseEvent: " + event.getMessage());
                        if (event.isSuccess()) {
                            // Update game core with new name
                            playerName = event.getNewName();
                        }

                        // Create a UI event to inform UI components
                        // ToDo: Create a UI event class for name change response
                    }

                    @Override
                    public Class<NameChangeResponseEvent> getEventType() {
                        return NameChangeResponseEvent.class;
                    }
                });

        EventDispatcher.getInstance().registerListener(LobbyListEvent.class,
                new EventDispatcher.EventListener<LobbyListEvent>() {
                    @Override
                    public void onEvent(LobbyListEvent event) {
                        UIEventBus.getInstance().publish(new LobbyListResponseEvent(event.getLobbies()));
                    }

                    @Override
                    public Class<LobbyListEvent> getEventType() {
                        return LobbyListEvent.class;
                    }
                });

        // Lobby Event (Handles CREATED confirmation, LEFT is handled by PLAYERLEFT
        // command)
        EventDispatcher.getInstance().registerListener(LobbyEvent.class,
                new EventDispatcher.EventListener<LobbyEvent>() {
                    @Override
                    public void onEvent(LobbyEvent event) {
                        switch (event.getAction()) {
                            // LEFT case removed as it's handled by PLAYERLEFT command
                            case CREATED:
                                // Lobby creation confirmed. A LobbyJoinedEvent should follow for the creator.
                                LOGGER.log(Level.INFO, "Lobby Created Event (Network): Player={0}",
                                        new Object[] { event.getPlayerName() });
                                // No direct UI event needed here, LobbyJoinedEvent handles the state change.
                                break;
                            default:
                                LOGGER.log(Level.FINE, "Unhandled LobbyEvent action: {0}", event.getAction());
                                break;
                        }
                    }

                    @Override
                    public Class<LobbyEvent> getEventType() {
                        return LobbyEvent.class;
                    }
                });

        // Lobby Joined Event (Handles self joining and others joining)
        EventDispatcher.getInstance().registerListener(LobbyJoinedEvent.class,
                new EventDispatcher.EventListener<LobbyJoinedEvent>() {
                    @Override
                    public void onEvent(LobbyJoinedEvent event) {
                        // Check if the player who joined is the local player
                        // Ensure playerName (local player's name) is updated via
                        // NameChangeResponseEvent
                        if (playerName != null && playerName.equals(event.getPlayer())) {
                            // It's the local player who joined
                            UIEventBus.getInstance()
                                    .publish(new ch.unibas.dmi.dbis.cs108.client.ui.events.lobby.LobbyJoinedEvent(
                                            event.getLobbyId(),
                                            event.getLobbyId(), // Using lobbyId as name placeholder - TODO: Get actual
                                            // name if possible
                                            event.getPlayer(),
                                            event.isHost(),
                                            event.getPlayers()));
                        } else {
                            // Another player joined the lobby the local player is in
                            UIEventBus.getInstance()
                                    .publish(new PlayerJoinedLobbyEvent(event.getLobbyId(), event.getPlayer()));
                        }
                    }

                    @Override
                    public Class<LobbyJoinedEvent> getEventType() {
                        return LobbyJoinedEvent.class;
                    }
                });

        // Start Game Event
        EventDispatcher.getInstance().registerListener(StartGameEvent.class,
                new EventDispatcher.EventListener<StartGameEvent>() {
                    @Override
                    public void onEvent(StartGameEvent event) {
                        // Publish UI event. Pass null as lobbyId since StartGameEvent doesn't provide
                        // it.
                        // TODO: Verify if GameStartedEvent requires a non-null lobbyId.
                        UIEventBus.getInstance().publish(new GameStartedEvent(null)); // Pass null instead of
                                                                                      // event.getLobbyId()
                        LOGGER.info(
                                "Game Started event received, publishing to UI."); // Removed lobbyId from log
                    }

                    @Override
                    public Class<StartGameEvent> getEventType() {
                        return StartGameEvent.class;
                    }
                });

        // Notification Events
        EventDispatcher.getInstance().registerListener(NotificationEvent.class,
                new EventDispatcher.EventListener<NotificationEvent>() {
                    @Override
                    public void onEvent(NotificationEvent event) {
                        // Publish as a system chat message
                        UIEventBus.getInstance()
                                .publish(new ch.unibas.dmi.dbis.cs108.client.ui.events.chat.GlobalChatEvent(
                                        event.getMessage(), null, // Sender is null for system messages
                                        ch.unibas.dmi.dbis.cs108.client.ui.events.chat.GlobalChatEvent.ChatType.SYSTEM));
                    }

                    @Override
                    public Class<NotificationEvent> getEventType() {
                        return NotificationEvent.class;
                    }
                });

        // Removed LeaderboardResponseEvent listener block as it was
        // incomplete/placeholder

    }

    // ToDo: Implement this method to update the game state and UI based on network
    // events.
    // private void updateGameFromNetworkEvent(GameStateUpdateEvent event) {
    // // Logic to update the game based on network events
    // // For example: update resources, board state, etc.
    // }
}
