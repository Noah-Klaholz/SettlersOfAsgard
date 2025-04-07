package ch.unibas.dmi.dbis.cs108.client.ui.events;

public class NameChangeRequestEvent {
    private final String newName;

    public NameChangeRequestEvent(String newName) {
        this.newName = newName;
    }

    public String getNewName() {
        return newName;
    }
}