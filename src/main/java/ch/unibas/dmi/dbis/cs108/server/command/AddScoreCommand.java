package ch.unibas.dmi.dbis.cs108.server.command;

import ch.unibas.dmi.dbis.cs108.server.core.ClientHandler;
import ch.unibas.dmi.dbis.cs108.server.core.logic.GameLogicInterface;
import ch.unibas.dmi.dbis.cs108.server.core.model.GameState;
import ch.unibas.dmi.dbis.cs108.server.util.Logger;
import ch.unibas.dmi.dbis.cs108.shared.protocol.CommunicationAPI;

/**
 * Command to add points to the game score.
 * Usage: addScore <value>
 */
public class AddScoreCommand implements Command {

    private final GameLogicInterface gameLogic;
    private final GameState gameState;

    public AddScoreCommand(GameLogicInterface gameLogic, GameState gameState) {
        this.gameLogic = gameLogic;
        this.gameState = gameState;
    }

    @Override
    public void execute(String[] args, ClientHandler client) {
        if (args.length < 2) {
            client.sendMessage("Usage: addScore <value>");
            return;
        }

        try {
            // Parse the score value
            int scoreValue = Integer.parseInt(args[1]);

            // Get active player and add score to them
            String playerName = gameState.getTurnManager().getPlayerTurn();
            if (playerName != null) {
                // Add the score using PlayerManager
                gameState.getPlayerManager().addRunes(scoreValue, playerName);

                String scoreCommand = CommunicationAPI.NetworkProtocol.Commands.SYNCHRONIZE.getCommand();

                // Use the proper interface method
                gameLogic.processMessage(scoreCommand);

                // Get the player's current score
                int currentScore = gameState.getPlayerManager().getPlayers().stream()
                        .filter(p -> p.getName().equals(playerName))
                        .map(p -> p.getRunes())
                        .findFirst()
                        .orElse(0);

                client.sendMessage("Added " + scoreValue + " points to player " + playerName +
                                  ". Current score: " + currentScore);
            } else {
                client.sendMessage("No active player to add score to.");
            }
        } catch (NumberFormatException e) {
            client.sendMessage("Invalid score value. Please provide a number.");
        } catch (Exception e) {
            Logger.error("Error adding score: " + e.getMessage());
            client.sendMessage("Error adding score.");
        }
    }

    @Override
    public String getName() {
        return "addScore";
    }
}