package ch.unibas.dmi.dbis.cs108.client.ui.events.admin;

import ch.unibas.dmi.dbis.cs108.client.ui.events.UIEvent;

/**
 * UIEvent representing a request to change the player's name from the UI.
 */
public class ChangeNameUIEvent implements UIEvent {
    private final String newName;

    /**
     * Constructs a new ChangeNameUIEvent.
     *
     * @param newName the desired new name
     */
    public ChangeNameUIEvent(String newName) {
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
        return "CHANGE_NAME";
    }
}
