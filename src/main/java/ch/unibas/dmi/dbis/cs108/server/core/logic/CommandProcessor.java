package ch.unibas.dmi.dbis.cs108.server.core.logic;

import ch.unibas.dmi.dbis.cs108.server.core.model.GameState;
import ch.unibas.dmi.dbis.cs108.server.core.structures.Command;
import ch.unibas.dmi.dbis.cs108.server.core.structures.Lobby;
import ch.unibas.dmi.dbis.cs108.shared.protocol.CommunicationAPI.NetworkProtocol.Commands;
import ch.unibas.dmi.dbis.cs108.shared.protocol.ErrorsAPI;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Thread-safe handler for command parsing and execution with the command pattern
 */
public class CommandProcessor {
    private static final Logger LOGGER = Logger.getLogger(CommandProcessor.class.getName());
    private final GameLogic gameLogic;
    private final GameState gameState;
    private final Lobby lobby;
    private final Map<Commands, Function<Command, String>> commandHandlers = new ConcurrentHashMap<>();

    // Use a single lock for state-changing commands to ensure consistency
    private final Object commandExecutionLock = new Object();

    public CommandProcessor(GameLogic gameLogic) {
        this.gameLogic = gameLogic;
        this.gameState = gameLogic.getGameState();
        this.lobby = gameLogic.getLobby();
        registerCommandHandlers();
    }

    private void registerCommandHandlers() {
        commandHandlers.put(Commands.ENDTURN, this::handleEndTurn);
        commandHandlers.put(Commands.SYNCHRONIZE, this::handleSynchronize);
        commandHandlers.put(Commands.GETGAMESTATUS, this::handleGetGameStatus);
        commandHandlers.put(Commands.BUYTILE, this::handleBuyTile);
        commandHandlers.put(Commands.BUYSTRUCTURE, this::handleBuyStructure);
        commandHandlers.put(Commands.PLACESTRUCTURE, this::handlePlaceStructure);
        commandHandlers.put(Commands.USESTRUCTURE, this::handleUseStructure);
        commandHandlers.put(Commands.BUYSTATUE, this::handleBuyStatue);
        commandHandlers.put(Commands.UPGRADESTATUE, this::handleUpgradeStatue);
        commandHandlers.put(Commands.USESTATUE, this::handleUseStatue);
        commandHandlers.put(Commands.USEFIELDARTIFACT, this::handleUseFieldArtifact);
        commandHandlers.put(Commands.USEPLAYERARTIFACT, this::handleUsePlayerArtifact);
    }

    /**
     * Process a command message and return the response
     */
    public String processCommand(Command command) {

        Function<Command, String> handler = commandHandlers.get(command.getCommandType());
        if (handler == null) {
            return formatError(ErrorsAPI.Errors.UNHANDLED_COMMAND.getError() + command);
        }

        try {
            if (isStateChangingCommand(command.getCommandType())) {
                synchronized (commandExecutionLock) {
                    if (!gameLogic.getTurnManager().getPlayerTurn().equals(command.getPlayer().getName())) {
                        return formatError(ErrorsAPI.Errors.NOT_PLAYER_TURN.getError());
                    }
                    return handler.apply(command);
                }
            } else {
                return handler.apply(command);
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error processing command: " + command, e);
            return formatError(e.getMessage());
        }
    }

    /**
     * Determines if a command modifies game state and requires synchronization
     */
    private boolean isStateChangingCommand(Commands command) {
        return command != Commands.SYNCHRONIZE && command != Commands.GETGAMESTATUS;
    }

    /**
     * End the current player's turn
     */
    private String handleEndTurn(Command cmd) {
        try {
            String playerName = cmd.getPlayer().getName();
            if (playerName == null || playerName.isEmpty()) {
                return formatError(ErrorsAPI.Errors.PLAYER_DOES_NOT_EXIST.getError());
            }

            // Validate it's this player's turn
            if (!playerName.equals(gameState.getTurnManager().getPlayerTurn())) {
                return formatError(ErrorsAPI.Errors.NOT_PLAYER_TURN.getError());
            }

            boolean success = lobby.manualEndTurn();
            if (!success) {
                return formatError(ErrorsAPI.Errors.GAME_COMMAND_FAILED.getError());
            }
            return formatSuccess(Commands.STARTTURN.getCommand() + "$" + playerName + "$" +
                    gameState.getTurnManager().getPlayerTurn());
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error ending turn", e);
            return formatError(e.getMessage());
        }
    }

    /**
     * Synchronize game state
     */
    private String handleSynchronize(Command cmd) {
        return gameState.createStateMessage();
    }

    /**
     * Get detailed game status
     */
    private String handleGetGameStatus(Command cmd) {
        return gameState.createDetailedStatusMessage();
    }

    /**
     * Process buy tile command
     */
    private String handleBuyTile(Command cmd) {
        try {
            String[] params = cmd.getArgs();
            if (params.length != 2) {
                return formatError(ErrorsAPI.Errors.INVALID_PARAMETERS.getError() + "$BUYTILE");
            }

            int x = Integer.parseInt(params[0]);
            int y = Integer.parseInt(params[1]);
            String playerName = cmd.getPlayer().getName();

            boolean success = gameLogic.buyTile(x, y, playerName);
            return success ?
                    formatSuccess(Commands.BUYTILE.getCommand() + "$" + x + "$" + y + "$" + playerName) :
                    formatError(ErrorsAPI.Errors.GAME_COMMAND_FAILED.getError() + "$BUYTILE");
        } catch (NumberFormatException e) {
            return formatError(ErrorsAPI.Errors.INVALID_PARAMETERS.getError() + "$BUYTILE");
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error buying tile", e);
            return formatError(e.getMessage());
        }
    }

    /**
     * Process buy structure command
     */
    private String handleBuyStructure(Command cmd) {
        try {
            String[] parts = cmd.getArgs();
            if (parts.length != 1) {
                return formatError(ErrorsAPI.Errors.INVALID_PARAMETERS.getError() + "$BUYSTRUCTURE");
            }

            int structureId = Integer.parseInt(parts[0]);
            String playerName = cmd.getPlayer().getName();

            boolean success = gameLogic.buyStructure(structureId, playerName);
            return success ?
                    formatSuccess(Commands.BUYSTRUCTURE.getCommand() + "$" + structureId + "$" + playerName) :
                    formatError(ErrorsAPI.Errors.GAME_COMMAND_FAILED.getError() + "$BUYSTRUCTURE");
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error buying structure", e);
            return formatError(e.getMessage());
        }
    }

    /**
     * Process place structure command
     */
    private String handlePlaceStructure(Command cmd) {
        try {
            String[] parts = cmd.getArgs();
            if (parts.length != 3) {
                return formatError(ErrorsAPI.Errors.INVALID_PARAMETERS.getError() + "$PLACESTRUCTURE");
            }

            int x = Integer.parseInt(parts[0]);
            int y = Integer.parseInt(parts[1]);
            int structureId = Integer.parseInt(parts[2]);
            String playerName = cmd.getPlayer().getName();

            boolean success = gameLogic.placeStructure(x, y, structureId, playerName);
            return success ?
                    formatSuccess(Commands.PLACESTRUCTURE.getCommand() + "$" + x + "$" + y + "$"  + structureId + "$" + playerName) :
                    formatError(ErrorsAPI.Errors.GAME_COMMAND_FAILED.getError() + "$PLACESTRUCTURE");
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error placing structure", e);
            return formatError(e.getMessage());
        }
    }

    /**
     * Process use structure command
     */
    private String handleUseStructure(Command cmd) {
        try {
            String[] parts = cmd.getArgs();
            if (parts.length != 3) {
                return formatError(ErrorsAPI.Errors.INVALID_PARAMETERS.getError() + "$USESTRUCTURE");
            }

            int x = Integer.parseInt(parts[0]);
            int y = Integer.parseInt(parts[1]);
            int structureId = Integer.parseInt(parts[2]);
            String playerName = cmd.getPlayer().getName();

            boolean success = gameLogic.useStructure(x, y, structureId, playerName);
            return success ? formatSuccess(Commands.USESTRUCTURE.getCommand() + "$" + x + "$" + y + "$" + structureId + "$" + playerName) :
                    formatError(ErrorsAPI.Errors.GAME_COMMAND_FAILED.getError() + "$USESTRUCTURE");
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error using structure", e);
            return formatError(e.getMessage());
        }
    }

    /**
     * Process buy statue command
     */
    private String handleBuyStatue(Command cmd) {
        try {
            String[] parts = cmd.getArgs();
            if (parts.length != 1) {
                return formatError(ErrorsAPI.Errors.INVALID_PARAMETERS.getError() + "$BUYSTATUE");
            }

            String statueId = parts[0];
            String playerName = cmd.getPlayer().getName();

            boolean success = gameLogic.buyStatue(statueId, playerName);
            return success ?
                    formatSuccess(Commands.BUYSTATUE.getCommand() + "$" + statueId + "$" + playerName) :
                    formatError(ErrorsAPI.Errors.GAME_COMMAND_FAILED.getError() + "$BUYSTATUE");
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error buying statue", e);
            return formatError(e.getMessage());
        }
    }

    /**
     * Process upgrade statue command
     */
    private String handleUpgradeStatue(Command cmd) {
        try {
            String[] parts = cmd.getArgs();
            if (parts.length != 3) {
                return formatError(ErrorsAPI.Errors.INVALID_PARAMETERS.getError() + "$UPGRADESTATUE");
            }

            int x = Integer.parseInt(parts[0]);
            int y = Integer.parseInt(parts[1]);
            String statueId = parts[2];
            String playerName = cmd.getPlayer().getName();

            boolean success = gameLogic.upgradeStatue(x, y, statueId, playerName);
            return success ? formatSuccess(Commands.BUYSTATUE.getCommand() + "$" + x + "$" + y + "$" + statueId + "$" + playerName) :
                    formatError(ErrorsAPI.Errors.GAME_COMMAND_FAILED.getError() + "$UPGRADESTATUE");
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error upgrading statue", e);
            return formatError(e.getMessage());
        }
    }

    /**
     * Process use statue command
     */
    private String handleUseStatue(Command cmd) {
        try {
            String[] parts = cmd.getArgs();
            if (parts.length != 3) {
                return formatError(ErrorsAPI.Errors.INVALID_PARAMETERS + "$USESTATUE");
            }

            int x = Integer.parseInt(parts[0]);
            int y = Integer.parseInt(parts[1]);
            int statueId = Integer.parseInt(parts[2]);
            String playerName = cmd.getPlayer().getName();

            boolean success = gameLogic.useStatue(x, y, statueId, playerName);
            return success ? formatSuccess(Commands.USESTATUE.getCommand() + "$" + x + "$" + y + "$" + statueId + "$" + playerName) :
                    formatError(ErrorsAPI.Errors.GAME_COMMAND_FAILED.getError() + "$USESTATUE");
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error using statue", e);
            return formatError(e.getMessage());
        }
    }

    /**
     * Process use field artifact command
     */
    private String handleUseFieldArtifact(Command cmd) {
        try {
            String[] parts = cmd.getArgs();
            if (parts.length != 3) {
                return formatError(ErrorsAPI.Errors.INVALID_PARAMETERS + "$USEFIELDARTIFACT");
            }

            int x = Integer.parseInt(parts[0]);
            int y = Integer.parseInt(parts[1]);
            int artifactId = Integer.parseInt(parts[2]);
            String playerName = cmd.getPlayer().getName();

            boolean success = gameLogic.useFieldArtifact(x, y, artifactId, playerName);
            return success ?
                    formatSuccess(Commands.USEFIELDARTIFACT.getCommand() + "$" + x + "$" + y + "$" + artifactId + "$" + playerName) :
                    formatError(ErrorsAPI.Errors.GAME_COMMAND_FAILED.getError() + "$USEFIELDARTIFACT");
        } catch (NumberFormatException e) {
            return formatError("Invalid coordinates or artifact ID");
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error using field artifact", e);
            return formatError(e.getMessage());
        }
    }

    /**
     * Process use player artifact command
     */
    private String handleUsePlayerArtifact(Command cmd) {
        try {
            String[] parts = cmd.getArgs();
            if (parts.length != 2) {
                return formatError(ErrorsAPI.Errors.INVALID_PARAMETERS + "$USEPLAYERARTIFACT");
            }

            int artifactId = Integer.parseInt(parts[0]);
            String targetPlayer = parts[1];
            String playerName = cmd.getPlayer().getName();

            boolean success = gameLogic.usePlayerArtifact(artifactId, targetPlayer, playerName);
            return success ?
                    formatSuccess(Commands.USEPLAYERARTIFACT.getCommand() + "$" + artifactId + "$" + targetPlayer + "$" + playerName) :
                    formatError(ErrorsAPI.Errors.GAME_COMMAND_FAILED.getError() + "$USEPLAYERARTIFACT");
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error using player artifact", e);
            return formatError(e.getMessage());
        }
    }

    /**
     * Helper method to format success responses
     */
    private String formatSuccess(String message) {
        return Commands.OK.getCommand() + "$" + message;
    }

    /**
     * Helper method to format error responses
     */
    private String formatError(String message) {
        return Commands.ERROR.getCommand() + "$" + message;
    }
}