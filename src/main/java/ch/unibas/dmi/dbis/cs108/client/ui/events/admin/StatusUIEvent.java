package ch.unibas.dmi.dbis.cs108.client.ui.events.admin;

import ch.unibas.dmi.dbis.cs108.client.ui.events.UIEvent;

/**
 * UIEvent representing a status update in the admin UI.
 */
public class StatusUIEvent implements UIEvent {

    /**
     * Constructs a new StatusUIEvent.
     */
    public StatusUIEvent() {
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
