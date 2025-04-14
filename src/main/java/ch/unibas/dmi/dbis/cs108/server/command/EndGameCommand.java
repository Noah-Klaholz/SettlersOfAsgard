package ch.unibas.dmi.dbis.cs108.server.command;

import ch.unibas.dmi.dbis.cs108.server.core.ClientHandler;
import ch.unibas.dmi.dbis.cs108.server.core.logic.GameLogic;

public class EndGameCommand implements Command {
    private final GameLogic gameLogic;

    public EndGameCommand(GameLogic gameLogic) {
        this.gameLogic = gameLogic;
    }

    @Override
    public void execute(String[] args, ClientHandler client) {
        gameLogic.endGame();
        client.sendMessage("Game ended");
    }

    @Override
    public String getName() {
        return "end_game";
    }
}
