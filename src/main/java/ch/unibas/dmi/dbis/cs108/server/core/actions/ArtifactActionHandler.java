package ch.unibas.dmi.dbis.cs108.server.core.actions;

import ch.unibas.dmi.dbis.cs108.server.core.model.GameState;

import java.util.concurrent.locks.ReadWriteLock;

public class ArtifactActionHandler {
    private final GameState gameState;
    private final ReadWriteLock gameLock;

    public ArtifactActionHandler(GameState gameState, ReadWriteLock gameLock) {
        this.gameState = gameState;
        this.gameLock = gameLock;
    }

    public boolean useFieldArtifact(int x, int y, int artifactId, String playerName) {
        // Implementation will be added later
        return false;
    }

    public boolean usePlayerArtifact(int artifactId, String targetPlayer, String playerName) {
        // Implementation will be added later
        return false;
    }
}