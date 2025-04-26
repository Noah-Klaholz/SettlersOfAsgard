package ch.unibas.dmi.dbis.cs108.client.ui.events.game;

import ch.unibas.dmi.dbis.cs108.client.ui.events.UIEvent;

public class EndTurnRequestEvent implements UIEvent {

    /**
     * The name of the player who wants to end his turn.
     */
    private final String playerName;

    /**
     * Constructs an EndTurnRequestEvent.
     *
     * @param playerName the name of the player who wants to end his turn
     */
    public EndTurnRequestEvent(String playerName) {
        this.playerName = playerName;
    }

    /**
     * Returns the name of the player who wants to end his turn.
     *
     * @return the name of the player
     */
    public String getPlayerName() {
        return playerName;
    }

    /**
     * Returns the event type string.
     *
     * @return event type string
     */
    @Override
    public String getType() {
        return "EndTurnRequestEvent";
    }
}
