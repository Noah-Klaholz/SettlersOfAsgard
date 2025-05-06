package ch.unibas.dmi.dbis.cs108.client.core.events;

import ch.unibas.dmi.dbis.cs108.shared.entities.Purchasables.Statues.Statue;
import ch.unibas.dmi.dbis.cs108.shared.game.Player;

/**
 * UIEvent representing the activation of a statue by a player.
 * This event is triggered when a player activates a statue in the game.
 */
public class StatueActivatedEvent implements GameEvent {
    private final Player player;
    private final Statue statue;
    private final Outcome outcome;
    /**
     * Constructor for StatueActivatedEvent.
     *
     * @param player  The player who activated the statue.
     * @param statue  The statue that was activated.
     * @param outcome The outcome of the activation (DEAL, BLESSING, CURSE).
     */
    public StatueActivatedEvent(Player player, Statue statue, Outcome outcome) {
        this.player = player;
        this.statue = statue;
        this.outcome = outcome;
    }

    /**
     * Get the player who activated the statue.
     *
     * @return The player.
     */
    public Player getPlayer() {
        return player;
    }

    /**
     * Get the statue that was activated.
     *
     * @return The statue.
     */
    public Statue getStatue() {
        return statue;
    }

    /**
     * Get the outcome of the activation.
     *
     * @return The outcome (DEAL, BLESSING, CURSE).
     */
    public Outcome getOutcome() {
        return outcome;
    }

    public enum Outcome {DEAL, BLESSING, CURSE}
}
