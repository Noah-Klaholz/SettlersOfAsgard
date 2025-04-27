package ch.unibas.dmi.dbis.cs108.client.ui.events.game;

import ch.unibas.dmi.dbis.cs108.client.ui.events.UIEvent;

import java.util.Optional;

/**
 * Event representing a request to use a player artifact.
 * This event is sent from the client to the server when a player wants to use
 * an artifact.
 */
public class UsePlayerArtifactUIEvent implements UIEvent {
    private final int artifactId;
    private final String useType;
    private final Optional<String> targetPlayer; // Optional for artifacts that don't target a specific player

    /**
     * Constructs a UsePlayerArtifactUIEvent.
     *
     * @param artifactId   the ID of the artifact
     * @param useType      the type of use (e.g., "attack", "defend")
     * @param targetPlayer the target player (optional)
     */
    public UsePlayerArtifactUIEvent(int artifactId, String useType, String targetPlayer) {
        this.artifactId = artifactId;
        this.useType = useType;
        this.targetPlayer = Optional.ofNullable(targetPlayer);
    }

    /**
     * gets the ID of the artifact.
     *
     * @return the artifact ID
     */
    public int getArtifactId() {
        return artifactId;
    }

    /**
     * gets the type of use.
     *
     * @return the use type
     */
    public String getUseType() {
        return useType;
    }

    /**
     * gets the target player.
     *
     * @return the target player, if applicable
     */
    public Optional<String> getTargetPlayer() {
        return targetPlayer;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getType() {
        return "USE_PLAYER_ARTIFACT";
    }
}
