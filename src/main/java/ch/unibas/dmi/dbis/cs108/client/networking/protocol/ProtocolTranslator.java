package ch.unibas.dmi.dbis.cs108.client.networking.protocol;

import ch.unibas.dmi.dbis.cs108.client.networking.events.*;
import ch.unibas.dmi.dbis.cs108.shared.protocol.CommunicationAPI;
import ch.unibas.dmi.dbis.cs108.shared.protocol.CommunicationAPI.NetworkProtocol.Commands;
import ch.unibas.dmi.dbis.cs108.shared.protocol.ErrorsAPI.Errors;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.logging.Logger;

public class ProtocolTranslator implements CommunicationAPI {
    private static final Logger LOGGER = Logger.getLogger(ProtocolTranslator.class.getName());
    private static final String DELIMITER = "$";
    private final EventDispatcher eventDispatcher;
    private final Map<String, Consumer<String>> commandHandlers = new HashMap<>();

    public ProtocolTranslator(EventDispatcher dispatcher) {
        this.eventDispatcher = dispatcher;
        registerHandlers();
    }

    @Override
    public void sendMessage(String message) {
        // Implementation would delegate to NetworkController
        // This method is needed to satisfy the interface but since this class
        // only formats messages and doesn't send them, it's left empty
        LOGGER.warning("ProtocolTranslator.sendMessage called, but this class is not responsible for sending messages");
    }

    @Override
    public void processMessage(String received) {
        processIncomingMessage(received);
    }

    private void registerHandlers() {
        commandHandlers.put(Commands.CHATGLOBAL.getCommand(), this::processGlobalChatMessage);
        commandHandlers.put(Commands.CHATLOBBY.getCommand(), this::processLobbyChatMessage);
        commandHandlers.put(Commands.CHATPRIVATE.getCommand(), this::processPrivateChatMessage);
        commandHandlers.put(Commands.JOIN.getCommand(), this::processJoinMessage);
        commandHandlers.put(Commands.LEAVE.getCommand(), this::processLeaveMessage);
        commandHandlers.put("NOTF", this::processNotificationMessage); // Not in Commands enum
        commandHandlers.put(Commands.ERROR.getCommand(), this::processErrorMessage);
        commandHandlers.put(Commands.LISTLOBBIES.getCommand(), this::processLobbyListMessage);
        commandHandlers.put(Commands.OK.getCommand(), this::processSuccessMessage);
        commandHandlers.put(Commands.PING.getCommand(), this::processPingMessage);
        commandHandlers.put(Commands.SHUTDOWN.getCommand(), this::processShutdownMessage);
        commandHandlers.put(Commands.CHANGENAME.getCommand(), this::processNameChangeMessage);
        commandHandlers.put(Commands.STARTTURN.getCommand(), this::processTurnMessage);
        commandHandlers.put(Commands.START.getCommand(), this::processStartGameMessage);
        commandHandlers.put("LBRD", this::processLeaderboardMessage); // Not in Commands enum
    }

    public void processIncomingMessage(String message) {
        if (message == null || message.isEmpty()) return;
        String[] parts = message.split("\\" + DELIMITER, 2); // Split only into command and arguments
        String command = parts[0];
        String args = (parts.length > 1) ? parts[1] : ""; // Handle messages with no arguments
        Consumer<String> handler = commandHandlers.get(command);
        if (handler != null) {
            handler.accept(args); // Pass only arguments to the handler
        } else {
            LOGGER.warning("Unknown message type: " + message);
        }
    }

    // Message handler methods
    public void processTurnMessage(String args) {
        eventDispatcher.dispatchEvent(new ReceiveCommandEvent(
                Commands.STARTTURN.getCommand() + DELIMITER + args,
                Commands.STARTTURN
        ));
    }

    public void processStartGameMessage(String args) {
        eventDispatcher.dispatchEvent(new ReceiveCommandEvent(
                Commands.START.getCommand() + DELIMITER + args,
                Commands.START
        ));
    }

    private void processNameChangeMessage(String args) {
        String[] parts = args.split("\\" + DELIMITER, 2); // Expecting oldName$newName
        if (parts.length >= 2) {
            eventDispatcher.dispatchEvent(new ReceiveCommandEvent(
                    Commands.CHANGENAME.getCommand() + DELIMITER + args
            ));
        }
    }

    private void processGlobalChatMessage(String args) {
        String[] parts = args.split("\\" + DELIMITER, 2); // Expecting sender$message
        if (parts.length >= 2) {
            ChatMessageEvent event = new ChatMessageEvent(parts[0], parts[1], ChatMessageEvent.ChatType.GLOBAL);
            eventDispatcher.dispatchEvent(event);
        }
    }

    private void processLobbyChatMessage(String args) {
        String[] parts = args.split("\\" + DELIMITER, 2); // Expecting sender$message
        if (parts.length >= 2) {
            ChatMessageEvent event = new ChatMessageEvent(parts[0], parts[1], ChatMessageEvent.ChatType.LOBBY);
            eventDispatcher.dispatchEvent(event);
        }
    }

    private void processPrivateChatMessage(String args) {
        String[] parts = args.split("\\" + DELIMITER, 3); // Split into sender, receiver, message
        if (parts.length >= 3) {
            ChatMessageEvent event = new ChatMessageEvent(parts[0], parts[2], ChatMessageEvent.ChatType.PRIVATE);
            eventDispatcher.dispatchEvent(event);
        }
    }

    private void processJoinMessage(String args) {
        String[] parts = args.split("\\" + DELIMITER, 2); // Expecting playerName$lobbyName
        if (parts.length >= 2) {
            LobbyEvent event = new LobbyEvent(LobbyEvent.LobbyAction.JOINED, parts[0], parts[1]);
            eventDispatcher.dispatchEvent(event);
        }
    }

    private void processLeaveMessage(String args) {
        String[] parts = args.split("\\" + DELIMITER, 2); // Expecting playerName$lobbyName
        if (parts.length >= 2) {
            LobbyEvent event = new LobbyEvent(LobbyEvent.LobbyAction.LEFT, parts[0], parts[1]);
            eventDispatcher.dispatchEvent(event);
        }
    }

    private void processNotificationMessage(String args) {
        eventDispatcher.dispatchEvent(new NotificationEvent(args));
    }

    private void processErrorMessage(String args) {
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
            // Try to match with standard errors
            for (Errors error : Errors.values()) {
                if (error.getError().startsWith(errorCode + DELIMITER)) {
                    errorMessage = error.getError().split("\\" + DELIMITER, 2)[1];
                    break;
                }
            }

            ErrorEvent event = new ErrorEvent(errorCode, errorMessage, ErrorEvent.ErrorSeverity.ERROR);
            eventDispatcher.dispatchEvent(event);
        }
    }

    private void processLobbyListMessage(String args) {
        eventDispatcher.dispatchEvent(new LobbyListEvent(args));
    }

    private void processSuccessMessage(String args) {
        if (args.startsWith(Commands.PING.getCommand() + DELIMITER)) {
            // Ping response is handled in the NetworkController
            return;
        } else if (args.startsWith(Commands.CHANGENAME.getCommand() + DELIMITER)) {
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
            eventDispatcher.dispatchEvent(new ReceiveCommandEvent(Commands.OK.getCommand() + DELIMITER + args));
        }
    }

    private void processPingMessage(String args) {
        // Handled in NetworkController
    }

    private void processShutdownMessage(String args) {
        ConnectionEvent event = new ConnectionEvent(
                ConnectionEvent.ConnectionState.DISCONNECTED,
                "Server is shutting down"
        );
        eventDispatcher.dispatchEvent(event);
    }

    private void processLeaderboardMessage(String args) {
        // ToDo: Implement this method to process leaderboard messages.
        if (args == null || args.isEmpty()) {
            LOGGER.warning("Invalid leaderboard message: LBRD" + DELIMITER + " was empty");
            return;
        }
        LOGGER.info("Received leaderboard message: " + args);
    }

    // Protocol formatting methods
    public String formatGetGameStatus() {
        return Commands.GETGAMESTATUS.getCommand() + DELIMITER;
    }

    public String formatGetLeaderboard() {
        return "LBRD" + DELIMITER; // Not in Commands enum
    }

    public String formatGetPrices() {
        return Commands.GETPRICES.getCommand() + DELIMITER;
    }

    public String formatDisconnect(String playerName) {
        return "DISC" + DELIMITER + playerName; // Not in Commands enum
    }

    public String formatPong(String playerName) {
        return Commands.OK.getCommand() + DELIMITER + Commands.PING.getCommand() + DELIMITER + playerName; // Pong response
    }

    public String formatRegister(String playerName) {
        return Commands.REGISTER.getCommand() + DELIMITER + playerName;
    }

    public String formatGlobalChatMessage(String playerName, String content) {
        return Commands.CHATGLOBAL.getCommand() + DELIMITER + playerName + DELIMITER + content;
    }

    public String formatLobbyChatMessage(String playerName, String content) {
        return Commands.CHATLOBBY.getCommand() + DELIMITER + playerName + DELIMITER + content;
    }

    public String formatWhisper(String sender, String recipient, String content) {
        return Commands.CHATPRIVATE.getCommand() + DELIMITER + sender + DELIMITER + recipient + DELIMITER + content;
    }

    public String formatCreateLobby(String playerName, String lobbyName) {
        return Commands.CREATELOBBY.getCommand() + DELIMITER + playerName + DELIMITER + lobbyName;
    }

    public String formatJoinLobby(String playerName, String lobbyName) {
        return Commands.JOIN.getCommand() + DELIMITER + playerName + DELIMITER + lobbyName;
    }

    public String formatLeaveLobby(String playerName) {
        return Commands.LEAVE.getCommand() + DELIMITER + playerName;
    }

    public String formatStartGame() {
        return Commands.START.getCommand() + DELIMITER;
    }

    public String formatListLobbies() {
        return Commands.LISTLOBBIES.getCommand() + DELIMITER;
    }

    public String formatChangeName(String newName) {
        return Commands.CHANGENAME.getCommand() + DELIMITER + newName;
    }

    public String formatListLobbyPlayers(String lobbyName) {
        return Commands.LISTPLAYERS.getCommand() + DELIMITER + lobbyName;
    }

    public String formatListAllPlayers() {
        return "APLR" + DELIMITER; // Not in Commands enum
    }

    public String formatEndTurn() {
        return Commands.ENDTURN.getCommand() + DELIMITER;
    }

    public String formatBuyTile(int x, int y) {
        return Commands.BUYTILE.getCommand() + DELIMITER + x + DELIMITER + y;
    }

    public String formatBuyStatue(int statueID) {
        return Commands.BUYSTATUE.getCommand() + DELIMITER + statueID;
    }

    public String formatUpgradeStatue(int x, int y, int statueID) {
        return Commands.UPGRADESTATUE.getCommand() + DELIMITER + x + DELIMITER + y + DELIMITER + statueID;
    }

    public String formatUseStatue(int x, int y, int statueID, String useType) {
        return Commands.USESTATUE.getCommand() + DELIMITER + x + DELIMITER + y + DELIMITER + statueID + DELIMITER + useType;
    }

    public String formatPlaceStructure(int x, int y, int structureID) {
        return Commands.PLACESTRUCTURE.getCommand() + DELIMITER + x + DELIMITER + y + DELIMITER + structureID;
    }

    public String formatUseStructure(int x, int y, int structureID, String useType) {
        return Commands.USESTRUCTURE.getCommand() + DELIMITER + x + DELIMITER + y + DELIMITER + structureID + DELIMITER + useType;
    }

    public String formatUsePlayerArtifact(int artifactID, String useType, String playerAimedAt) {
        return Commands.USEPLAYERARTIFACT.getCommand() + DELIMITER + artifactID + DELIMITER + useType + DELIMITER + playerAimedAt;
    }

    public String formatUseFieldArtifact(int x, int y, int artifactID, String useType) {
        return Commands.USEFIELDARTIFACT.getCommand() + DELIMITER + x + DELIMITER + y + DELIMITER + artifactID + DELIMITER + useType;
    }
}