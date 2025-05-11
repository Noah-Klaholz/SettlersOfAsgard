package ch.unibas.dmi.dbis.cs108.client.ui.events.game;

import ch.unibas.dmi.dbis.cs108.client.ui.events.UIEvent;

/**
 * Indicates the end of a turn and potentially the start of the next.
 */
public class EndTurnResponseEvent implements UIEvent {

    /**
     * The name of the player whose turn it is now.
     */
    private final String nextPlayerName;

    /**
     * Constructs an EndTurnResponseEvent.
     *
     * @param nextPlayerName the name of the player whose turn it is now
     */
    public EndTurnResponseEvent(String nextPlayerName) {
        this.nextPlayerName = nextPlayerName;
    }

    /**
     * @return the name of the next player
     */
    public String getNextPlayerName() {
        return nextPlayerName;
    }

    /**
     * @return the event type identifier
     */
    @Override
    public String getType() {
        return "END_TURN_RESPONSE";
    }
}
