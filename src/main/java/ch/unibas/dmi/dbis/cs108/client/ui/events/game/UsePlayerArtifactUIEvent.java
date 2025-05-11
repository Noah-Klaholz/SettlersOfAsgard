package ch.unibas.dmi.dbis.cs108.client.ui.events.game;

import ch.unibas.dmi.dbis.cs108.client.ui.events.UIEvent;

import java.util.Optional;

/**
 * Event representing a request to use a player artifact.
 * This event is sent from the client to the server when a player wants to use an artifact.
 */
public class UsePlayerArtifactUIEvent implements UIEvent {
    /**
     * The ID of the artifact to be used.
     */
    private final int artifactId;
    /**
     * The targeted player for the artifact, if applicable.
     * This is optional because some artifacts may not target a specific player.
     */
    private final Optional<String> targetPlayer; // Optional for artifacts that don't target a specific player

    /**
     * Constructs a UsePlayerArtifactUIEvent.
     *
     * @param artifactId   the ID of the artifact
     * @param targetPlayer the targeted player for the artifact
     */
    public UsePlayerArtifactUIEvent(int artifactId, String targetPlayer) {
        this.artifactId = artifactId;
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
