package ch.unibas.dmi.dbis.cs108.client.ui.events.admin;

import ch.unibas.dmi.dbis.cs108.client.ui.events.UIEvent;

/**
 * UIEvent representing a status update in the admin UI.
 */
public class RequestGameStateEvent implements UIEvent {

    /**
     * Constructs a new StatusUIEvent.
     */
    public RequestGameStateEvent() {
        // No data needed for this event
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getType() {
        return "STATUS";
    }
}
