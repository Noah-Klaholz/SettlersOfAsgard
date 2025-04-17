package ch.unibas.dmi.dbis.cs108.client.ui.events.admin;

import ch.unibas.dmi.dbis.cs108.client.ui.events.UIEvent;

public class StatusUIEvent implements UIEvent {

    public StatusUIEvent() {
        // No data needed for this event
    }

    @Override
    public String getType() {
        return "STATUS";
    }
}