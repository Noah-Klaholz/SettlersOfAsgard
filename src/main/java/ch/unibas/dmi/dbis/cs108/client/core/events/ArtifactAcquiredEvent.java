package ch.unibas.dmi.dbis.cs108.client.core.events;

import ch.unibas.dmi.dbis.cs108.shared.entities.Findables.Artifact;
import ch.unibas.dmi.dbis.cs108.shared.game.Player;

/**
 * UIEvent representing the acquisition of an artifact by a player.
 * This event is triggered when a player acquires an artifact in the game.
 */
public class ArtifactAcquiredEvent implements GameEvent {

    /**
     * The player who acquired the artifact.
     */
    private final Player player;

    /**
     * The artifact that was acquired.
     */
    private final Artifact artefact;

    /**
     * Constructor for ArtifactAcquiredEvent.
     *
     * @param player   The player who acquired the artifact.
     * @param artefact The artifact that was acquired.
     */
    public ArtifactAcquiredEvent(Player player, Artifact artefact) {
        this.player = player;
        this.artefact = artefact;
    }

    /**
     * Get the player who acquired the artifact.
     *
     * @return The player.
     */
    public Player getPlayer() {
        return player;
    }

    /**
     * Get the artifact that was acquired.
     *
     * @return The artifact.
     */
    public Artifact getArtifact() {
        return artefact;
    }
}
