package ch.unibas.dmi.dbis.cs108.server.command;

import ch.unibas.dmi.dbis.cs108.server.core.ClientHandler;
import ch.unibas.dmi.dbis.cs108.server.core.logic.GameLogic;
import ch.unibas.dmi.dbis.cs108.server.util.Logger;
import ch.unibas.dmi.dbis.cs108.shared.protocol.CommunicationAPI.NetworkProtocol.Commands;
import ch.unibas.dmi.dbis.cs108.shared.protocol.ErrorsAPI.Errors;

public class StartGameCommand implements Command {
    private final GameLogic gameLogic;

    public StartGameCommand(GameLogic gameLogic) {
        this.gameLogic = gameLogic;
    }

    @Override
    public void execute(String[] args, ClientHandler client) {
        if (args.length != 1) {
            client.sendMessage(Errors.CANNOT_START_GAME.getError());
            return;
        }

        if (gameLogic.startGame(client)) {
            if (client.broadCastLobby(Commands.OK.getCommand() + Commands.START.getCommand() + "$" + client.getPlayerName())) {
                client.sendMessage(Commands.OK.getCommand() + Commands.START.getCommand() + "$" + client.getPlayerName());
                Logger.info("Game started.");
            } else {
                client.sendMessage(Errors.NOT_IN_LOBBY.getError());
                Logger.info("Game cannot be started, because client is not in a lobby.");
            }
        } else {
            client.sendMessage(Errors.NOT_ENOUGH_PLAYERS.getError());
            Logger.info("Game cannot be started.");
        }
    }

    @Override
    public String getName() {
        return Commands.START.getCommand();
    }
}
