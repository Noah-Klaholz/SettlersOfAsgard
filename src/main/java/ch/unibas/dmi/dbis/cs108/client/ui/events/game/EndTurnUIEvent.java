package ch.unibas.dmi.dbis.cs108.client.ui.events.game;

import ch.unibas.dmi.dbis.cs108.client.ui.events.UIEvent;

public class EndTurnUIEvent implements UIEvent {

    public EndTurnUIEvent() {
        // No data needed for this event
    }

    @Override
    public String getType() {
        return "ENDTURN";
    }
}