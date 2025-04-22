package ch.unibas.dmi.dbis.cs108.client.ui.events.game;

import ch.unibas.dmi.dbis.cs108.client.ui.events.UIEvent;

/**
 * Event representing a request to end the current player's turn.
 */
public class EndTurnUIEvent implements UIEvent {

    /**
     * Constructs an EndTurnUIEvent.
     */
    public EndTurnUIEvent() {
        // No data needed for this event
    }

    /**
     * @return the event type identifier
     */
    @Override
    public String getType() {
        return "END_TURN";
    }
}
