package ch.unibas.dmi.dbis.cs108.server.core.logic;

/**
 * This interface defines the interactions between Lobby and GameLogic.
 */
public interface GameEventNotifier {

    /**
     * Sends a message to all active players.
     *
     * @param message The message to send.
     */
    void broadcastMessage(String message);

    /**
     * Ends the game.
     */
    void endGame();

    /**
     * Manually ends the Turn of a player.
     *
     * @return true if the action was successful.
     */
    boolean manualEndTurn();

    /**
     * Sends a message to a specific player.
     *
     * @param player  The player to send the message to.
     * @param message The message to send.
     */
    void sendMessageToPlayer(String player, String message);
}
