package ch.unibas.dmi.dbis.cs108.client.ui.events.admin;

public class NameChangeResponseEvent {
    private final boolean success;
    private final String newName;
    private final String message;

    public NameChangeResponseEvent(boolean success, String newName, String message) {
        this.success = success;
        this.newName = newName;
        this.message = message;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getNewName() {
        return newName;
    }

    public String getMessage() {
        return message;
    }
}