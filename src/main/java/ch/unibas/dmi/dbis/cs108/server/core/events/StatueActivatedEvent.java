package ch.unibas.dmi.dbis.cs108.server.core.events;

import ch.unibas.dmi.dbis.cs108.client.core.entities.Player;
import ch.unibas.dmi.dbis.cs108.client.core.entities.Statue;

public class StatueActivatedEvent implements GameEvent {
    public enum Outcome { DEAL, BLESSING, CURSE }

    private final Player player;
    private final Statue statue;
    private final Outcome outcome;

    public StatueActivatedEvent(Player player, Statue statue, Outcome outcome) {
        this.player = player;
        this.statue = statue;
        this.outcome = outcome;
    }

    public Player getPlayer() {
        return player;
    }

    public Statue getStatue() {
        return statue;
    }

    public Outcome getOutcome() {
        return outcome;
    }
}
