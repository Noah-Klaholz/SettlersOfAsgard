package ch.unibas.dmi.dbis.cs108.client.core.events;

import ch.unibas.dmi.dbis.cs108.client.core.entities.Artifact;
import ch.unibas.dmi.dbis.cs108.client.core.entities.Player;

public class ArtifactActivatedEvent implements GameEvent {
    private final Player player;
    private final Artifact artifact;

    public ArtifactActivatedEvent(Player player, Artifact artifact) {
        this.player = player;
        this.artifact = artifact;
    }

    public Player getPlayer() {
        return player;
    }

    public Artifact getArtifact() {
        return artifact;
    }
}
