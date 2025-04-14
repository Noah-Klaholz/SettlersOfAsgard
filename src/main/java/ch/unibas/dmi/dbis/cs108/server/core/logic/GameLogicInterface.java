package ch.unibas.dmi.dbis.cs108.server.core.logic;

public interface GameLogicInterface {
    void startGame(String[] players);

    void processMessage(String message);

    void endGame();
}