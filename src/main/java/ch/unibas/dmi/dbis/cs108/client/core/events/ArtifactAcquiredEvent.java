package ch.unibas.dmi.dbis.cs108.client.core.events;

import ch.unibas.dmi.dbis.cs108.client.core.entities.Artefact;
import ch.unibas.dmi.dbis.cs108.client.core.entities.Player;

public class ArtifactAcquiredEvent implements GameEvent {
    private final Player player;
    private final Artefact artefact;

    public ArtifactAcquiredEvent(Player player, Artefact artefact) {
        this.player = player;
        this.artefact = artefact;
    }

    public Player getPlayer() {
        return player;
    }

    public Artefact getArtifact() {
        return artefact;
    }
}
