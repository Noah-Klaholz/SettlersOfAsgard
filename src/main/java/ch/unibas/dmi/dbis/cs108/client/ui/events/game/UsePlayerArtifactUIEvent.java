package ch.unibas.dmi.dbis.cs108.client.ui.events.game;

import ch.unibas.dmi.dbis.cs108.client.ui.events.UIEvent;

import java.util.Optional;

public class UsePlayerArtifactUIEvent implements UIEvent {
    private final int artifactId;
    private final String useType;
    private final Optional<String> targetPlayer; // Optional for artifacts that don't target a specific player

    public UsePlayerArtifactUIEvent(int artifactId, String useType, String targetPlayer) {
        this.artifactId = artifactId;
        this.useType = useType;
        this.targetPlayer = Optional.ofNullable(targetPlayer);
    }

    public int getArtifactId() {
        return artifactId;
    }

    public String getUseType() {
        return useType;
    }

    public Optional<String> getTargetPlayer() {
        return targetPlayer;
    }

    @Override
    public String getType() {
        return "USE_PLAYER_ARTIFACT";
    }
}
