package ch.unibas.dmi.dbis.cs108.client.core.events;

import ch.unibas.dmi.dbis.cs108.shared.entities.Findables.Artifact;
import ch.unibas.dmi.dbis.cs108.shared.game.Player;

/**
 * UIEvent representing the activation of an artifact by a player.
 * This event is triggered when a player activates an artifact in the game.
 */
public class ArtifactActivatedEvent implements GameEvent {

    /**
     * The player who activated the artifact.
     */
    private final Player player;

    /**
     * The artifact that was activated.
     */
    private final Artifact artefact;

    /**
     * Constructor for ArtifactActivatedEvent.
     *
     * @param player   The player who activated the artifact.
     * @param artefact The artifact that was activated.
     */
    public ArtifactActivatedEvent(Player player, Artifact artefact) {
        this.player = player;
        this.artefact = artefact;
    }

    /**
     * Get the player who activated the artifact.
     *
     * @return The player.
     */
    public Player getPlayer() {
        return player;
    }

    /**
     * Get the artifact that was activated.
     *
     * @return The artifact.
     */
    public Artifact getArtifact() {
        return artefact;
    }
}
