package ch.unibas.dmi.dbis.cs108.server.core.entities;

import ch.unibas.dmi.dbis.cs108.client.core.commands.Command;
import ch.unibas.dmi.dbis.cs108.client.core.events.EventDispatcher;
import ch.unibas.dmi.dbis.cs108.client.core.state.GameState;

import java.util.ArrayList;
import java.util.List;

public class PlayerRound {
    private int number;
    private Player activePlayer;
    private List<Command> commands;

    public PlayerRound() {
        this.number = 0;
        this.activePlayer = null;
        commands = new ArrayList<Command>();
    }

    public Player getActivePlayer() {
        return activePlayer;
    }

    public void setActivePlayer(Player activePlayer) {
        this.activePlayer = activePlayer;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public List<Command> getCommands() {
        return commands;
    }

    public void addCommand(Command command) {
        commands.add(command);
    }

    public void executeCommands(GameState gameState, EventDispatcher dispatcher) {
        for (Command command : commands) {
            command.execute(gameState, dispatcher);
        }
    }

    public void nextRound() {
        this.number++;
        //todo: change active player
    }
}
