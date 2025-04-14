package ch.unibas.dmi.dbis.cs108.client.networking.protocol;

import ch.unibas.dmi.dbis.cs108.client.networking.events.*;
import ch.unibas.dmi.dbis.cs108.shared.protocol.CommunicationAPI.NetworkProtocol.Commands;


import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.logging.Logger;

public class ProtocolTranslator {
    private static final Logger LOGGER = Logger.getLogger(ProtocolTranslator.class.getName());
    private final EventDispatcher eventDispatcher;
    private final Map<String, Consumer<String>> commandHandlers = new HashMap<>();

    public ProtocolTranslator(EventDispatcher dispatcher) {
        this.eventDispatcher = dispatcher;
        registerHandlers();
    }

    private void registerHandlers() {
        commandHandlers.put("CHTG", this::processGlobalChatMessage);
        commandHandlers.put("CHTL", this::processLobbyChatMessage);
        commandHandlers.put("CHTP", this::processPrivateChatMessage);
        commandHandlers.put("JOIN", this::processJoinMessage);
        commandHandlers.put("LEAV", this::processLeaveMessage);
        commandHandlers.put("NOTF", this::processNotificationMessage);
        commandHandlers.put("ERR", this::processErrorMessage);
        commandHandlers.put("LIST", this::processLobbyListMessage);
        commandHandlers.put("OK", this::processSuccessMessage);
        commandHandlers.put("PING", this::processPingMessage);
        commandHandlers.put("STDN", this::processShutdownMessage);
        commandHandlers.put("CHAN", this::processNameChangeMessage);
        commandHandlers.put("TURN", this::processTurnMessage);
        commandHandlers.put("STRT", this::processStartGameMessage);
    }

    public void processIncomingMessage(String message) {
        if (message == null || message.isEmpty()) return;
        String[] parts = message.split("\\$", 2);
        String command = parts[0];
        Consumer<String> handler = commandHandlers.get(command);
        if (handler != null) {
            handler.accept(message);
        } else {
            LOGGER.warning("Unknown message type: " + message);
        }
    }

    public void processTurnMessage(String message) {
        String[] parts = message.split("\\$", 2);
        if (parts.length > 2) {
            eventDispatcher.dispatchEvent(new ReceiveCommandEvent(message, Commands.STARTTURN));
        }
    }

    public void processStartGameMessage(String message) {
        String[] parts = message.split("\\$", 2);
        if (parts.length > 1) {
            eventDispatcher.dispatchEvent(new ReceiveCommandEvent(message, Commands.START));
        }
    }

    private void processNameChangeMessage(String message) {
        String[] parts = message.split("\\$", 3);
        if (parts.length >= 3) {
            eventDispatcher.dispatchEvent(new ReceiveCommandEvent(message));
        }
    }

    private void processGlobalChatMessage(String message) {
        String[] parts = message.split("\\$", 3);
        if (parts.length >= 3) {
            ChatMessageEvent event = new ChatMessageEvent(parts[1], parts[2], ChatMessageEvent.ChatType.GLOBAL);
            eventDispatcher.dispatchEvent(event);
        }
    }

    private void processLobbyChatMessage(String message) {
        String[] parts = message.split("\\$", 3);
        if (parts.length >= 3) {
            ChatMessageEvent event = new ChatMessageEvent(parts[1], parts[2], ChatMessageEvent.ChatType.LOBBY);
            eventDispatcher.dispatchEvent(event);
        }
    }

    private void processPrivateChatMessage(String message) {
        String[] parts = message.split("\\$", 3);
        if (parts.length >= 3) {
            ChatMessageEvent event = new ChatMessageEvent(parts[1], parts[2], ChatMessageEvent.ChatType.PRIVATE);
            eventDispatcher.dispatchEvent(event);
        }
    }

    private void processJoinMessage(String message) {
        String[] parts = message.split("\\$", 3);
        if (parts.length >= 3) {
            LobbyEvent event = new LobbyEvent(LobbyEvent.LobbyAction.JOINED, parts[1], parts[2]);
            eventDispatcher.dispatchEvent(event);
        }
    }

    private void processLeaveMessage(String message) {
        String[] parts = message.split("\\$", 3);
        if (parts.length >= 3) {
            LobbyEvent event = new LobbyEvent(LobbyEvent.LobbyAction.LEFT, parts[1], parts[2]);
            eventDispatcher.dispatchEvent(event);
        }
    }

    private void processNotificationMessage(String message) {
        String content = message.substring("NOTF$".length());
        eventDispatcher.dispatchEvent(new NotificationEvent(content));
    }

    private void processErrorMessage(String message) {
        System.out.println("Error message: " + message);
        String errorPart = message.substring("ERR$".length());
        String errorCode;
        String errorMessage;
        if (errorPart.contains("$")) {
            String[] parts = errorPart.split("\\$", 2);
            errorCode = parts[0];
            errorMessage = parts[1];
        } else {
            errorCode = "UNKNOWN";
            errorMessage = errorPart;
        }

        // Add name change error handling
        if (errorCode.equals("CHAN")) {
            eventDispatcher.dispatchEvent(new NameChangeResponseEvent(false, null, errorMessage));
        } else {
            ErrorEvent event = new ErrorEvent(errorCode, errorMessage, ErrorEvent.ErrorSeverity.ERROR);
            eventDispatcher.dispatchEvent(event);
        }
    }

    private void processLobbyListMessage(String message) {
        String lobbies = message.substring("LIST$".length());
        eventDispatcher.dispatchEvent(new LobbyListEvent(lobbies));
    }

    private void processSuccessMessage(String message) {
        if (message.startsWith("OK$PING$")) {
            // Ping response is handled in the NetworkController.
            return;
        } else if (message.startsWith("OK$CHAN$")) {

            // Extract just the new name - adjust parsing based on server format
            String[] args = message.replace("OK$CHAN$", "").trim().split("\\$");
            String newName = args[1].trim();
            // Dispatch the event with the correct name
            eventDispatcher.dispatchEvent(new NameChangeResponseEvent(true, newName, "Name changed successfully"));
       } else if (message.startsWith("OK$RGST$")) {
            String[] args = message.replace("OK$RGST$", "").trim().split("\\$");
            String newName = args[0].trim();
            eventDispatcher.dispatchEvent(new NameChangeResponseEvent(true, newName, "Name changed successfully"));
        } else if(message.startsWith("OK$")) {
            eventDispatcher.dispatchEvent(new ReceiveCommandEvent(message));
        }
    }

    private void processPingMessage(String message) {
        // Typically handled by the network controller.
    }

    private void processShutdownMessage(String message) {
        ConnectionEvent event = new ConnectionEvent(ConnectionEvent.ConnectionState.DISCONNECTED, "Server is shutting down");
        eventDispatcher.dispatchEvent(event);
    }

    // Formatters for outgoing messages

    public String formatGlobalChatMessage(String playerName, String message) {
        return "CHTG$" + playerName + "$" + message;
    }

    public String formatLobbyChatMessage(String playerName, String message) {
        return "CHTL$" + playerName + "$" + message;
    }

    public String formatWhisper(String senderName, String receiver, String message) {
        return "CHTP$" + senderName + "$" + receiver + "$" + message;
    }

    public String formatJoinLobby(String playerName, String lobbyName) {
        return "JOIN$" + playerName + "$" + lobbyName;
    }

    public String formatCreateLobby(String playerName, String lobbyName) {
        return "CREA$" + playerName + "$" + lobbyName;
    }

    public String formatLeaveLobby(String playerName) {
        return "LEAV$" + playerName + "$";
    }

    public String formatStartGame() {
        return "STRT$";
    }

    public String formatListLobbies() {
        return "LIST$";
    }

    public String formatRegister(String playerName) {
        return "RGST$" + playerName;
    }

    public String formatDisconnect(String playerName) {
        return "EXIT$" + playerName;
    }

    public String formatPong(String playerName) {
        return Commands.PING.getCommand();
    }

    public String formatChangeName(String newName) {
        return "CHAN$" + newName;
    }

    public String formatListLobbyPlayers(String lobbyName) {
        return "LSTP$LOBBY$" + lobbyName;
    }

    public String formatListAllPlayers() {
        return "LSTP$SERVER";
    }

    public String formatEndTurn() {
        return "ENDT$";
    }

    public String formatBuyTile(int x, int y) {
        return "BUYT$" + x + "$" + y;
    }

    public String formatPlaceStructure(int x, int y, int structureID) {
        return "PLST$" + x + "$" + y + "$" + structureID;
    }

    public String formatUseStructure(int x, int y, int structureID, String useType) {
        return "USSR$" + x + "$" + y + "$" + structureID + "$" + useType;
    }

    public String formatBuyStatue(int statueID) {
        return "BYST$" + statueID;
    }

    public String formatUpgradeStatue(int x, int y, int statueID) {
        return "UPST$" + x + "$" + y + "$" + statueID;
    }

    public String formatUseStatue(int x, int y, int statueID, String useType) {
        return "USTA$" + x + "$" + y + "$" + statueID + "$" + useType;
    }

    public String formatUsePlayerArtifact(int artifactId, String useType, String playerAimedAt) {
        return "USPA$" + artifactId + "$" + useType + "$" + playerAimedAt;
    }

    public String formatUseFieldArtifact(int x, int y, int artifactId, String useType) {
        return "USFA$" + artifactId + "$" + useType;
    }

    // Temporäre Methode für terminal feedback zum GameState
    public String formatGetGameStatus() {
        return "GSTS$";
    }

    // Temporäre Methode für terminal feedback zu den Preisen der Karten
    public String formatGetPrices() {
        return "GPRC$";
    }

}