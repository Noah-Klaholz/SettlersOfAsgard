package ch.unibas.dmi.dbis.cs108.client.ui.events.admin;

import ch.unibas.dmi.dbis.cs108.client.ui.events.UIEvent;

/**
 * UIEvent representing a request to change the player's name.
 */
public class NameChangeRequestEvent implements UIEvent {
    private final String newName;

    /**
     * Constructs a new NameChangeRequestEvent.
     *
     * @param newName the desired new name
     */
    public NameChangeRequestEvent(String newName) {
        this.newName = newName;
    }

    /**
     * Gets the new name.
     *
     * @return the new name
     */
    public String getNewName() {
        return newName;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getType() {
        return "NAME_CHANGE_REQUEST";
    }
}
