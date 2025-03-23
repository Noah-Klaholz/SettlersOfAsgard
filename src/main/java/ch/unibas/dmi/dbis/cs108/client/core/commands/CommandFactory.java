package ch.unibas.dmi.dbis.cs108.client.core.commands;

import ch.unibas.dmi.dbis.cs108.client.core.entities.Player;

/**
 * CommandFactory class is responsible for creating commands
 */
public class CommandFactory {
    /**
     * Creates a command
     *
     * @param type   Type of command
     * @param player Player who initiated the command
     * @param args   Additional arguments for the command
     * @return Command
     */
    public static Command createCommand(String type, Player player, String... args) {
        return switch (type) {
            case "CHAT" -> new ChatCommand(player, args[0]);
            case "PING" -> new PingCommand(player);
            case "PONG" -> new PongCommand(player, args.length > 0 ? args[0] : "server");
            case "JOIN" -> new JoinLobbyCommand(player, args[0]);
            case "CREA" -> new CreateLobbyCommand(player, args[0]);
            default -> null;
        };
    }
}
