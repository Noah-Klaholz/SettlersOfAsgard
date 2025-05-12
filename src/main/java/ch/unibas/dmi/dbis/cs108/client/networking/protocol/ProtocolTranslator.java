package ch.unibas.dmi.dbis.cs108.client.networking.protocol;

import ch.unibas.dmi.dbis.cs108.client.networking.events.*;
import ch.unibas.dmi.dbis.cs108.client.ui.events.chat.GlobalChatEvent;
import ch.unibas.dmi.dbis.cs108.shared.protocol.CommunicationAPI;
import ch.unibas.dmi.dbis.cs108.shared.protocol.CommunicationAPI.NetworkProtocol.Commands;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.logging.Logger;

/**
 * This class translates the communication protocol.
 * It formats outgoing messages and handles incoming messages.
 */
public class ProtocolTranslator implements CommunicationAPI {
    /** Logger to log logging */
    private static final Logger LOGGER = Logger.getLogger(ProtocolTranslator.class.getName());
    /** Constant Delimiter used for the protocol */
    private static final String DELIMITER = "$";
    /** EventDispatcher to dispatch events */
    private final EventDispatcher eventDispatcher;
    /** commandHandlers, stored in a hashmap */
    private final Map<String, Consumer<String>> commandHandlers = new HashMap<>();

    /**
     * Creates a new Protocol Translator object.
     * It registers the handlers.
     *
     * @param dispatcher the eventDispatcher to use.
     */
    public ProtocolTranslator(EventDispatcher dispatcher) {
        this.eventDispatcher = dispatcher;
        registerHandlers();
    }

    /**
     * Old method with no use.
     *
     * @param message the message to send
     */
    @Override
    public void sendMessage(String message) {
        // Implementation would delegate to NetworkController
        // This method is needed to satisfy the interface but since this class
        // only formats messages and doesn't send them, it's left empty
        LOGGER.warning("ProtocolTranslator.sendMessage called, but this class is not responsible for sending messages");
    }

    /**
     * This method is responsible for processing incoming messages.
     *
     * @param received the message received
     */
    @Override
    public void processMessage(String received) {
        processIncomingMessage(received);
    }

    /**
     * Adds the following to the HashMap:
     * String command -> Method responsible for handling it.
     */
    private void registerHandlers() {
        commandHandlers.put(Commands.CHATGLOBAL.getCommand(), this::processGlobalChatMessage);
        commandHandlers.put(Commands.CHATLOBBY.getCommand(), this::processLobbyChatMessage);
        commandHandlers.put(Commands.CHATPRIVATE.getCommand(), this::processPrivateChatMessage);
        commandHandlers.put(Commands.JOIN.getCommand(), this::processJoinMessage);
        commandHandlers.put(Commands.LEAVE.getCommand(), this::processLeaveMessage);
        commandHandlers.put(Commands.INFO.getCommand(), this::processNotificationMessage);
        commandHandlers.put(Commands.ERROR.getCommand(), this::processErrorMessage);
        commandHandlers.put(Commands.LISTLOBBIES.getCommand(), this::processLobbyListMessage);
        commandHandlers.put(Commands.LISTPLAYERS.getCommand(), this::processPlayerListMessage);
        commandHandlers.put(Commands.OK.getCommand(), this::processSuccessMessage);
        commandHandlers.put(Commands.PING.getCommand(), this::processPingMessage);
        commandHandlers.put(Commands.SHUTDOWN.getCommand(), this::processShutdownMessage);
        commandHandlers.put(Commands.CHANGENAME.getCommand(), this::processNameChangeMessage);
        commandHandlers.put(Commands.STARTTURN.getCommand(), this::processTurnMessage);
        commandHandlers.put(Commands.START.getCommand(), this::processStartGameMessage);
        commandHandlers.put(Commands.CREATELOBBY.getCommand(), this::processCreateLobbyMessage);
        commandHandlers.put(Commands.DISCONNECT.getCommand(), this::processDisconnectMessage);
        commandHandlers.put(Commands.ENDGAME.getCommand(), this::processEndGameMessage);
        commandHandlers.put(Commands.SYNCHRONIZE.getCommand(), this::processSyncMessage);
        commandHandlers.put(Commands.LEADERBOARD.getCommand(), this::processLeaderboard);
        commandHandlers.put(Commands.RECONNECT.getCommand(), this::processReconnectMessage);
    }

    /**
     * This method processes incoming messages.
     * It ensures correct syntax.
     *
     * @param message the message to process
     */
    public void processIncomingMessage(String message) {
        if (message == null || message.isEmpty()) return;
        String[] parts = message.split("\\" + DELIMITER, 2); // Split only into command and arguments
        String command = parts[0];
        String args = (parts.length > 1) ? parts[1] : ""; // Handle messages with no arguments
        Consumer<String> handler = commandHandlers.get(command);
        if (handler != null) {
            handler.accept(args);
        } else {
            LOGGER.warning("Unknown message type: " + message);
        }
    }

    /**
     * This method invokes a new Event based on the message.
     *
     * @param args the args of the message.
     */
    public void processTurnMessage(String args) {
        eventDispatcher.dispatchEvent(new EndTurnEvent(args));
    }

    /**
     * This method invokes a new Event based on the message.
     *
     * @param args the args of the message.
     */
    public void processDisconnectMessage(String args) {
        eventDispatcher.dispatchEvent(
                new ConnectionEvent(ConnectionEvent.ConnectionState.DISCONNECTED, "Player " + args + " has disconnected. ", false)
        ); // args is the player name
    }

    /**
     * This method invokes a new Event based on the message.
     *
     * @param args the args of the message.
     */
    public void processReconnectMessage(String args) {
        eventDispatcher.dispatchEvent(
                new ConnectionEvent(ConnectionEvent.ConnectionState.CONNECTED, "Player " + args + " has reconnected. ", false)
        );
    }

    /**
     * This method invokes a new Event based on the message.
     *
     * @param message the args of the message.
     */
    public void processSyncMessage(String message) {
        eventDispatcher.dispatchEvent(new GameSyncEvent(message));
    }

    /**
     * This method invokes a new Event based on the message.
     *
     * @param args the args of the message.
     */
    public void processStartGameMessage(String args) {
        eventDispatcher.dispatchEvent(new StartGameEvent());
    }

    /**
     * This method invokes a new Event based on the message.
     *
     * @param args the args of the message.
     */
    public void processEndGameMessage(String args) {
        eventDispatcher.dispatchEvent(new EndGameEvent(args));
    }

    /**
     * This method invokes a new Event based on the message.
     *
     * @param args the args of the message.
     */
    private void processNameChangeMessage(String args) {
        Logger.getGlobal().info("ProcessedNameChange" + args);
        String[] parts = args.split("\\" + DELIMITER, 2); // Expecting oldName$newName
        if (parts.length >= 2) {
            eventDispatcher.dispatchEvent(new ReceiveCommandEvent(
                    Commands.CHANGENAME.getCommand() + DELIMITER + args
            ));
        }
    }

    /**
     * This method invokes a new Event based on the message.
     *
     * @param args the args of the message.
     */
    private void processGlobalChatMessage(String args) {
        String[] parts = args.split("\\" + DELIMITER, 2); // Expecting sender$message
        if (parts.length >= 2) {
            ChatMessageEvent event = new ChatMessageEvent(parts[0], parts[1], ChatMessageEvent.ChatType.GLOBAL);
            eventDispatcher.dispatchEvent(event);
        }
    }

    /**
     * This method invokes a new Event based on the message.
     *
     * @param args the args of the message.
     */
    private void processLobbyChatMessage(String args) {
        String[] parts = args.split("\\" + DELIMITER, 2); // Expecting sender$message
        if (parts.length >= 2) {
            ChatMessageEvent event = new ChatMessageEvent(parts[0], parts[1], ChatMessageEvent.ChatType.LOBBY);
            eventDispatcher.dispatchEvent(event);
        }
    }

    /**
     * This method invokes a new Event based on the message.
     *
     * @param args the args of the message.
     */
    private void processPrivateChatMessage(String args) {
        String[] parts = args.split("\\" + DELIMITER, 3); // Split into sender, receiver, message
        if (parts.length >= 2) {
            ChatMessageEvent event = new ChatMessageEvent(parts[0], parts[1], ChatMessageEvent.ChatType.PRIVATE);
            eventDispatcher.dispatchEvent(event);
        }
    }

    /**
     * This method invokes a new Event based on the message.
     *
     * @param args the args of the message.
     */
    private void processJoinMessage(String args) {
        Logger.getGlobal().info("Processing join message: " + args);
        String[] parts = args.split("\\" + DELIMITER, 3); // Expecting lobbyName$players$isHost
        if (parts.length >= 3) {
            LobbyJoinedEvent event = new LobbyJoinedEvent(parts[0], parts[1], Boolean.parseBoolean(parts[2]));
            eventDispatcher.dispatchEvent(event);
        } else {
            LOGGER.warning("Invalid JOIN message format: " + args);
        }
    }

    /**
     * This method invokes a new Event based on the message.
     *
     * @param args the args of the message.
     */
    private void processLeaveMessage(String args) {
        String[] parts = args.split("\\" + DELIMITER, 2); // Expecting playerName$lobbyName
        if (parts.length >= 2) {
            LobbyEvent event = new LobbyEvent(LobbyEvent.LobbyAction.LEFT, parts[0], parts[1]);
            eventDispatcher.dispatchEvent(event);
        }
    }

    /**
     * This method invokes a new Event based on the message.
     *
     * @param args the args of the message.
     */
    private void processCreateLobbyMessage(String args) {
        String[] parts = args.split("\\" + DELIMITER, 2); // Expecting playerName$lobbyName
        if (parts.length >= 2) {
            LobbyEvent event = new LobbyEvent(LobbyEvent.LobbyAction.CREATED, parts[0], parts[1]);
            eventDispatcher.dispatchEvent(event);
        }
    }

    /**
     * This method invokes a new Event based on the message.
     *
     * @param args the args of the message.
     */
    private void processNotificationMessage(String args) {
        if (args.startsWith("DEBUFF$")) {
            eventDispatcher.dispatchEvent(new DebuffEvent(args.replace("DEBUFF", "")));
        } else {
            eventDispatcher.dispatchEvent(new NotificationEvent(args));
        }
    }

    /**
     * This method invokes a new Event based on the message.
     *
     * @param args the args of the message.
     */
    private void processErrorMessage(String args) {
        Logger.getGlobal().info("Processing error message: " + args);
        String errorCode;
        String errorMessage;

        if (args.contains(DELIMITER)) {
            String[] parts = args.split("\\" + DELIMITER, 2);
            errorCode = parts[0];
            errorMessage = parts[1];
        } else {
            errorCode = "UNKNOWN";
            errorMessage = args;
        }

        // Handle specific error cases
        if (errorCode.equals("CHAN")) {
            eventDispatcher.dispatchEvent(new NameChangeResponseEvent(false, null, errorMessage));
        } else {
            ErrorEvent event = new ErrorEvent(errorCode, errorMessage, ErrorEvent.ErrorSeverity.ERROR);
            eventDispatcher.dispatchEvent(event);
        }
    }

    /**
     * This method invokes a new Event based on the message.
     *
     * @param args the args of the message.
     */
    private void processLobbyListMessage(String args) {
        eventDispatcher.dispatchEvent(new LobbyListEvent(args));
    }

    /**
     * This method invokes a new Event based on the message.
     *
     * @param args the args of the message.
     */
    private void processSuccessMessage(String args) {
        if (args.startsWith(Commands.PING.getCommand() + DELIMITER)) {
            // Ping response is handled in the NetworkController
        } else if (args.startsWith(Commands.CHANGENAME.getCommand() + DELIMITER)) {
            Logger.getGlobal().info("Successfully processed changeName message: " + args);
            String chanArgs = args.substring((Commands.CHANGENAME.getCommand() + DELIMITER).length());
            String[] parts = chanArgs.split("\\" + DELIMITER);
            if (parts.length >= 1) {
                String newName = parts[parts.length - 1];
                eventDispatcher.dispatchEvent(
                        new NameChangeResponseEvent(true, newName, "Name changed successfully")
                );
            } else {
                LOGGER.warning("Invalid OK" + DELIMITER + "CHAN" + DELIMITER + " format: " + args);
            }
        } else if (args.startsWith(Commands.REGISTER.getCommand() + DELIMITER)) {
            Logger.getGlobal().info("Successfully processed register message: " + args);
            String regArgs = args.substring((Commands.REGISTER.getCommand() + DELIMITER).length());
            String[] parts = regArgs.split("\\" + DELIMITER);
            if (parts.length >= 1) {
                eventDispatcher.dispatchEvent(
                        new NameChangeResponseEvent(true, parts[0], "Registered successfully")
                );
            } else {
                LOGGER.warning("Invalid OK" + DELIMITER + "RGST" + DELIMITER + " format: " + args);
            }
        } else {
            processIncomingMessage(args); // If it's not a special case, process it normally -> OK gets removed in this case
        }
    }

    /**
     * This method invokes a new Event based on the message.
     *
     * @param args the args of the message.
     */
    private void processLeaderboard(String args) {
        Logger.getGlobal().info("Processing leaderboard message: " + args);
        eventDispatcher.dispatchEvent(new LeaderboardResponseEvent(args));
    }

    /**
     * This method invokes a new Event based on the message.
     *
     * @param args the args of the message.
     */
    private void processPlayerListMessage(String args) {
        String[] parts = args.split("\\" + DELIMITER, 2);
        if (parts.length >= 2) {
            PlayerListEvent.ListType type = Objects.equals(parts[0], "LOBBY") ? PlayerListEvent.ListType.LOBBY_LIST : PlayerListEvent.ListType.SERVER_LIST;
            String playerList = parts[1];
            eventDispatcher.dispatchEvent(new PlayerListEvent(playerList, type));
        } else {
            LOGGER.warning("Invalid PLAYER_LIST message format: " + args);
        }
    }

    /**
     * This method invokes a new Event based on the message.
     *
     * @param args the args of the message.
     */
    private void processPingMessage(String args) {
        // Handled in NetworkController
    }

    /**
     * This method invokes a new Event based on the message.
     *
     * @param args the args of the message.
     */
    private void processShutdownMessage(String args) {
        ConnectionEvent event = new ConnectionEvent(
                ConnectionEvent.ConnectionState.DISCONNECTED,
                "Server is shutting down" ,true
        );
        eventDispatcher.dispatchEvent(event);
    }

    /**
     * Formats the message based on the type.
     *
     * @return the formatted messages.
     */
    public String formatGetGameStatus() {
        return Commands.GETGAMESTATUS.getCommand() + DELIMITER;
    }

    /**
     * Formats the message based on the type.
     *
     * @return the formatted messages.
     */
    public String formatGetLeaderboard() {
        return Commands.LEADERBOARD.getCommand() + DELIMITER;
    }

    /**
     * Formats the message based on the type.
     *
     * @param playerName the name of the player
     * @return the formatted messages.
     */
    public String formatPong(String playerName) {
        return Commands.OK.getCommand() + DELIMITER + Commands.PING.getCommand() + DELIMITER + playerName; // Pong response
    }

    /**
     * Formats the message based on the type.
     *
     * @param playerName the name of the player
     * @return the formatted messages.
     */
    public String formatExit(String playerName) {
        return Commands.EXIT + DELIMITER + playerName;
    }

    /**
     * Formats the message based on the type.
     *
     * @param args the args of the message.
     * @return the formatted messages.
     */
    public String formatReconnect(String args) {
        return Commands.RECONNECT.getCommand() + DELIMITER + args;
    }

    /**
     * Formats the message based on the type.
     *
     * @param playerName the name of the player
     * @return the formatted messages.
     */
    public String formatRegister(String playerName) {
        return Commands.REGISTER.getCommand() + DELIMITER + playerName;
    }

    /**
     * Formats the message based on the type.
     *
     * @param playerName the name of the player
     * @param content the content
     * @return the formatted messages.
     */
    public String formatGlobalChatMessage(String playerName, String content) {
        return Commands.CHATGLOBAL.getCommand() + DELIMITER + playerName + DELIMITER + content;
    }

    /**
     * Formats the message based on the type.
     *
     * @param playerName the name of the player
     * @param content the content
     * @return the formatted messages.
     */
    public String formatLobbyChatMessage(String playerName, String content) {
        return Commands.CHATLOBBY.getCommand() + DELIMITER + playerName + DELIMITER + content;
    }

    /**
     * Formats the message based on the type.
     *
     * @param sender the sender
     * @param recipient the recipient
     * @param content the content
     * @return the formatted messages.
     */
    public String formatWhisper(String sender, String recipient, String content) {
        return Commands.CHATPRIVATE.getCommand() + DELIMITER + sender + DELIMITER + recipient + DELIMITER + content;
    }

    /**
     * Formats the message based on the type.
     *
     * @param playerName the name of the player
     * @param lobbyName the name of the lobby
     * @param maxPlayers the amount of maximum players
     * @return the formatted messages.
     */
    public String formatCreateLobby(String playerName, String lobbyName, int maxPlayers) {
        return Commands.CREATELOBBY.getCommand() + DELIMITER + playerName + DELIMITER + lobbyName + DELIMITER + maxPlayers;
    }

    /**
     * Formats the message based on the type.
     *
     * @param playerName the name of the player
     * @param lobbyName the name of the lobby
     * @return the formatted messages.
     */
    public String formatJoinLobby(String playerName, String lobbyName) {
        return Commands.JOIN.getCommand() + DELIMITER + playerName + DELIMITER + lobbyName;
    }

    /**
     * Formats the message based on the type.
     *
     * @param playerName the name of the player
     * @return the formatted messages.
     */
    public String formatLeaveLobby(String playerName) {
        return Commands.LEAVE.getCommand() + DELIMITER + playerName;
    }

    /**
     * Formats the message based on the type.
     *
     * @return the formatted messages.
     */
    public String formatStartGame() {
        return Commands.START.getCommand() + DELIMITER;
    }

    /**
     * Formats the message based on the type.
     *
     * @return the formatted messages.
     */
    public String formatListLobbies() {
        return Commands.LISTLOBBIES.getCommand() + DELIMITER;
    }

    /**
     * Formats the message based on the type.
     *
     * @param newName the new name
     * @return the formatted messages.
     */
    public String formatChangeName(String newName) {
        return Commands.CHANGENAME.getCommand() + DELIMITER + newName;
    }

    /**
     * Formats the message based on the type.
     *
     * @param lobbyName the lobbyName
     * @return the formatted messages.
     */
    public String formatListLobbyPlayers(String lobbyName) {
        return Commands.LISTPLAYERS.getCommand() + DELIMITER + "LOBBY" + DELIMITER + lobbyName;
    }

    /**
     * Formats the message based on the type.
     *
     * @return the formatted messages.
     */
    public String formatListAllPlayers() {
        return Commands.LISTPLAYERS.getCommand() + DELIMITER + "SERVER";
    }

    /**
     * Formats the message based on the type.
     *
     * @return the formatted messages.
     */
    public String formatEndTurn() {
        return Commands.ENDTURN.getCommand() + DELIMITER;
    }

    /**
     * Formats the message based on the type.
     *
     * @param x the x-coordinate
     * @param y the y-coordinate
     * @return the formatted messages.
     */
    public String formatBuyTile(int x, int y) {
        return Commands.BUYTILE.getCommand() + DELIMITER + x + DELIMITER + y;
    }

    /**
     * Formats the message based on the type.
     *
     * @param x the x-coordinate
     * @param y the y-coordinate
     * @param statueID the statueID
     * @return the formatted messages.
     */
    public String formatPlaceStatue(int x, int y, int statueID) {
        return Commands.PLACESTATUE.getCommand() + DELIMITER + x + DELIMITER + y + DELIMITER + statueID;
    }

    /**
     * Formats the message based on the type.
     *
     * @param x the x-coordinate
     * @param y the y-coordinate
     * @param statueID the statueID
     * @return the formatted messages.
     */
    public String formatUpgradeStatue(int x, int y, int statueID) {
        return Commands.UPGRADESTATUE.getCommand() + DELIMITER + x + DELIMITER + y + DELIMITER + statueID;
    }

    /**
     * Formats the message based on the type.
     *
     * @param x the x-coordinate
     * @param y the y-coordinate
     * @param statueID the statueID
     * @param params the params
     * @return the formatted messages.
     */
    public String formatUseStatue(int x, int y, int statueID, String params) {
        return StatueCommandBuilder.useStatue(x, y, statueID, params);
    }

    /**
     * Formats the message based on the type.
     *
     * @param x the x-coordinate
     * @param y the y-coordinate
     * @param structureID the structureID
     * @return the formatted messages.
     */
    public String formatPlaceStructure(int x, int y, int structureID) {
        return Commands.PLACESTRUCTURE.getCommand() + DELIMITER + x + DELIMITER + y + DELIMITER + structureID;
    }

    /**
     * Formats the message based on the type.
     *
     * @param x the x-coordinate
     * @param y the y-coordinate
     * @param structureID the structureID
     * @return the formatted messages.
     */
    public String formatUseStructure(int x, int y, int structureID) {
        return Commands.USESTRUCTURE.getCommand() + DELIMITER + x + DELIMITER + y + DELIMITER + structureID;
    }

    /**
     * Formats the message based on the type.
     *
     * @param artifactID the artifactID
     * @param playerAimedAt the name of the player aimed at
     * @return the formatted messages.
     */
    public String formatUsePlayerArtifact(int artifactID, String playerAimedAt) {
        return Commands.USEPLAYERARTIFACT.getCommand() + DELIMITER + artifactID + DELIMITER + playerAimedAt;
    }

    /**
     * Formats the message based on the type.
     *
     * @param x the x-coordinate
     * @param y the y-coordinate
     * @param artifactID the artifactID
     * @return the formatted messages.
     */
    public String formatUseFieldArtifact(int x, int y, int artifactID) {
        return Commands.USEFIELDARTIFACT.getCommand() + DELIMITER + x + DELIMITER + y + DELIMITER + artifactID;
    }

    /**
     * Formats the message based on the type.
     *
     * @param cheatCode the cheatCode.
     * @return the formatted messages.
     */
    public String formatCheatCode(String cheatCode) {
        return Commands.CHEAT.getCommand() + DELIMITER + cheatCode;
    }
}