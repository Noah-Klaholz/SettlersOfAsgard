package ch.unibas.dmi.dbis.cs108.server.core.logic;

import ch.unibas.dmi.dbis.cs108.server.core.structures.Command;

public interface GameLogicInterface {
    void startGame(String[] players);

    void processCommand(Command command);
}