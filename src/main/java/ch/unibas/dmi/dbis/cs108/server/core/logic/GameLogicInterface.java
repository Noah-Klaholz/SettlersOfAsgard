package ch.unibas.dmi.dbis.cs108.server.core.logic;

import ch.unibas.dmi.dbis.cs108.server.core.structures.Command;

/**
 * This interface defines the interactions between Lobby and GameLogic.
 * It provides methods to start the game and process commands.
 */
public interface GameLogicInterface {
    void startGame(String[] players);

    /**
     * Processes a command from a player.
     *
     * @param command The command to process.
     */
    void processCommand(Command command);
}