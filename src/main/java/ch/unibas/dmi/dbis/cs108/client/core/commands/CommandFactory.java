package ch.unibas.dmi.dbis.cs108.client.core.commands;

/**
 * CommandFactory class is responsible for creating commands
 */
public class CommandFactory {
    /**
     * Creates a command
     *
     * @param type Type of command
     * @param data Data of command
     * @return Command
     */
    public static Command createCommand(String type, String player, String[] args) {
        return switch (type) {
            case "CHAT" -> new ChatCommand(player, args[0]);
            case "PING" -> new PingCommand(player);
            case "PONG" -> new PongCommand(player, args[0]);
            default -> null;
        };
    }
}
