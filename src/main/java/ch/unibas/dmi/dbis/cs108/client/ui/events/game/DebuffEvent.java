package ch.unibas.dmi.dbis.cs108.client.ui.events.game;

import ch.unibas.dmi.dbis.cs108.client.ui.events.UIEvent;

/**
 * This class handles the UI showing a debuff
 */
public class DebuffEvent implements UIEvent {

    /**
     * A String representing the message that should get shown in the UI
     */
    private final String message;

    /**
     * Constructor for DebuffEvent
     *
     * @param message the message that should be displayed
     */
    public DebuffEvent(String message) {
        this.message = message;
    }

    /**
     * Getter for the message
     */
    public String getMessage() {
        return message;
    }

    /**
     * Returns the event type string.
     *
     * @return event type string
     */
    @Override
    public String getType() {
        return "Debuff";
    }
}
