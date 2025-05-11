package ch.unibas.dmi.dbis.cs108.client.ui.events.admin;

import ch.unibas.dmi.dbis.cs108.client.ui.events.UIEvent;

/**
 * UIEvent representing the response to a name change request.
 */
public class NameChangeResponseEvent implements UIEvent {
    /**
     * Indicates if the name change was successful.
     */
    private final boolean success;
    /**
     * The previous name.
     */
    private final String oldName;
    /**
     * The new name.
     */
    private final String newName;

    /**
     * An optional message associated with the name change response.
     */
    private final String message;

    /**
     * Constructs a new NameChangeResponseEvent.
     *
     * @param success whether the name change was successful
     * @param oldName the previous name
     * @param newName the new name
     * @param message an optional message
     */
    public NameChangeResponseEvent(boolean success, String oldName, String newName, String message) {
        this.success = success;
        this.oldName = oldName;
        this.newName = newName;
        this.message = message;
    }

    /**
     * Indicates if the name change was successful.
     *
     * @return true if successful, false otherwise
     */
    public boolean isSuccess() {
        return success;
    }

    /**
     * Gets the previous name.
     *
     * @return the old name
     */
    public String getOldName() {
        return oldName;
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
     * Gets the response message.
     *
     * @return the message
     */
    public String getMessage() {
        return message;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getType() {
        return "NAME_CHANGE_RESPONSE";
    }
}
