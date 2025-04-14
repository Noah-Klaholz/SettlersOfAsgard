package ch.unibas.dmi.dbis.cs108.server.core.logic;

import ch.unibas.dmi.dbis.cs108.server.core.structures.Command;
import ch.unibas.dmi.dbis.cs108.shared.entities.Player;

public interface GameLogicInterface {
    void startGame(String[] players);

    void processCommand(Command command);

    void endGame();
}