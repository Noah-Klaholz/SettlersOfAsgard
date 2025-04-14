//package ch.unibas.dmi.dbis.cs108.server.command;
//
//import ch.unibas.dmi.dbis.cs108.server.core.ClientHandler;
//import ch.unibas.dmi.dbis.cs108.server.core.lobby.LobbyManager;
//import ch.unibas.dmi.dbis.cs108.shared.protocol.ErrorsAPI;
//
//public class ChatPrivateCommand implements Command {
//    private final LobbyManager lobbyManager;
//
//    public ChatPrivateCommand(LobbyManager lobbyManager) {
//        this.lobbyManager = lobbyManager;
//    }
//
//    @Override
//    public void execute(String[] args, ClientHandler client) {
//        if (args.length < 3) {
//            client.sendMessage("Usage: chatprivate <recipient> <message>");
//            return;
//        }
//
//        String recipient = args[1];
//
//        // Reconstruct the message from args
//        StringBuilder messageBuilder = new StringBuilder();
//        for (int i = 2; i < args.length; i++) {
//            if (i > 2) messageBuilder.append(" ");
//            messageBuilder.append(args[i]);
//        }
//        String message = messageBuilder.toString();
//
//        // Check if trying to whisper to self
//        if (recipient.equals(client.getPlayerName())) {
//            client.sendMessage(ErrorsAPI.Errors.CANNOT_WHISPER_TO_SELF.getError());
//            return;
//        }
//
//        // Find recipient and send message
//        ClientHandler recipientClient = lobbyManager.findClientByName(recipient);
//        if (recipientClient == null) {
//            client.sendMessage(ErrorsAPI.Errors.PLAYER_DOES_NOT_EXIST.getError());
//            return;
//        }
//
//        // Send to recipient and confirm to sender
//        recipientClient.sendMessage("CHATPRIVATE:" + client.getPlayerName() + ":" + message);
//        client.sendMessage("CHATPRIVATE:TO:" + recipient + ":" + message);
//    }
//
//    @Override
//    public String getName() {
//        return "chatprivate";
//    }
//}