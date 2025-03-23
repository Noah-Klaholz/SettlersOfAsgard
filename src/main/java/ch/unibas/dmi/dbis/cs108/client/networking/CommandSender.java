package ch.unibas.dmi.dbis.cs108.client.networking;

import ch.unibas.dmi.dbis.cs108.client.core.commands.ChatCommand;
import ch.unibas.dmi.dbis.cs108.client.core.commands.PingCommand;
import ch.unibas.dmi.dbis.cs108.client.core.commands.PongCommand;
import ch.unibas.dmi.dbis.cs108.client.core.entities.Player;
import ch.unibas.dmi.dbis.cs108.client.networking.protocol.MessageFormatter;

import java.util.logging.Logger;
/**
 * CommandSender class is responsible for sending commands to the server
 */
public class CommandSender {
    private static final Logger logger = Logger.getLogger(CommandSender.class.getName());
    private final SocketHandler socketHandler;
    private final MessageFormatter formatter;

    /**
     * Constructor
     * @param socketHandler SocketHandler
     */
    public CommandSender(SocketHandler socketHandler) {
        this.socketHandler = socketHandler;
        this.formatter = new MessageFormatter();
    }

    /**
     * Sends a chat command to the server
     * @param chatCommand ChatCommand
     */
    public void sendChatCommand(ChatCommand chatCommand) {
        try {
            String message = MessageFormatter.formatChatMessage(
                    chatCommand.getSender().getName(),
                    chatCommand.getMessage()
            );
            socketHandler.send(message);
        } catch (Exception e) {
            logger.severe("Failed to send chat command: " + e.getMessage());
        }
    }

    /**
     * Sends a change name command to the server
     * @param newName String
     */
    public void sendChangeName(String newName) {
        try {
            String message = formatter.formatNameChange(newName);
            socketHandler.send(message);
        } catch (Exception e) {
            logger.severe("Failed to send change name command: " + e.getMessage());
        }
    }

    public void sendJoinLobby(Player player, String lobbyName) {
        try {
            String message = formatter.formatJoinLobby(player.getName(), lobbyName);
            socketHandler.send(message);
        } catch (Exception e) {
            logger.severe("Failed to send join lobby command: " + e.getMessage());
        }
    }

    public void sendCreateLobby(Player player, String lobbyName) {
        try {
            String message = formatter.formatCreateLobby(player.getName(), lobbyName);
            socketHandler.send(message);
        } catch (Exception e) {
            logger.severe("Failed to send create lobby command: " + e.getMessage());
        }
    }

    /**
     * Sends a disconnect command to the server
     * @param player Player
     */
    public void sendDisconnect(Player player) {
        try {
            String message = formatter.formatDisconnect(player.getName());
            socketHandler.send(message);
        } catch (Exception e) {
            logger.severe("Failed to send disconnect command: " + e.getMessage());
        }
    }

    /**
     * Sends a ping command to the server
     * @param player Player
     */
    public void sendPing(Player player) {
        try {
            String message = formatter.formatPing(player.getName());
            socketHandler.send(message);
        } catch (Exception e) {
            logger.severe("Failed to send ping command: " + e.getMessage());
        }
    }

    /**
     * Sends a register command to the server
     * @param player Player
     */
    public void sendRegister(Player player) {
        try {
            String message = formatter.formatRegister(player.getName());
            socketHandler.send(message);
        } catch (Exception e) {
            logger.severe("Failed to send register command: " + e.getMessage());
        }
    }

    /**
     * Sends a leave lobby command to the server
     * @param localPlayer the Player
     * @param lobbyName the name of the lobby
     */
    public void sendLeaveLobby(Player localPlayer, String lobbyName) {
        try {
            String message = formatter.formatLeaveLobby(localPlayer.getName(), lobbyName);
            socketHandler.send(message);
        } catch (Exception e) {
            logger.severe("Failed to send pong command: " + e.getMessage());
        }
    }

    /**
     * Sends a start game command to the server
     */
    public void sendStartGame() {
        try {
            String message = formatter.formatStartGame();
            socketHandler.send(message);
        } catch (Exception e) {
            logger.severe("Failed to send pong command: " + e.getMessage());
        }
    }

    /**
     * Sends a list lobbies command to the server
     */
    public void sendListLobbies() {
        try {
            String message = formatter.formatListLobbies();
            socketHandler.send(message);
        } catch (Exception e) {
            logger.severe("Failed to send pong command: " + e.getMessage());
        }
    }

    /**
     * Sends a ping command to the server
     */
    public void sendPingCommand(PingCommand pingCommand) {
        try {
            String message = formatter.formatPing(pingCommand.getSender().getName());
            socketHandler.send(message);
        } catch (Exception e) {
            logger.severe("Failed to send ping command: " + e.getMessage());
        }
    }

    /**
     * Sends a pong command to the server
     */
    public void sendPongCommand(PongCommand pongCommand) {
        try {
            String message = formatter.formatPong(pongCommand.getSender().getName());
            socketHandler.send(message);
        } catch (Exception e) {
            logger.severe("Failed to send pong command: " + e.getMessage());
        }
    }
}
