package ch.unibas.dmi.dbis.cs108.server.core.actions;

import ch.unibas.dmi.dbis.cs108.server.core.model.GameState;

import java.util.concurrent.locks.ReadWriteLock;

public class StatueActionHandler {
    private final GameState gameState;
    private final ReadWriteLock gameLock;

    public StatueActionHandler(GameState gameState, ReadWriteLock gameLock) {
        this.gameState = gameState;
        this.gameLock = gameLock;
    }

    public boolean buyStatue(String statueId, String playerName) {
        // Implementation will be added later
        return false;
    }

    public boolean upgradeStatue(int x, int y, String statueId, String playerName) {
        // Implementation will be added later
        return false;
    }

    public boolean useStatue(int x, int y, int statueId, String useType, String playerName) {
        // Implementation will be added later
        return false;
    }
}