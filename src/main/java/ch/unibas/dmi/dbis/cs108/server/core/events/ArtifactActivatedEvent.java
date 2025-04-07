package ch.unibas.dmi.dbis.cs108.server.core.events;

import ch.unibas.dmi.dbis.cs108.client.core.entities.Artefact;
import ch.unibas.dmi.dbis.cs108.client.core.entities.Player;

/**
 * Event representing the activation of an artifact by a player.
 * This event is triggered when a player activates an artifact in the game.
 */
public class ArtifactActivatedEvent implements GameEvent {
    private final Player player;
    private final Artefact artefact;

    /**
     * Constructor for ArtifactActivatedEvent.
     *
     * @param player   The player who activated the artifact.
     * @param artefact The artifact that was activated.
     */
    public ArtifactActivatedEvent(Player player, Artefact artefact) {
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
    public Artefact getArtifact() {
        return artefact;
    }
}
