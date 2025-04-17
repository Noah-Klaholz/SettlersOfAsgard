package ch.unibas.dmi.dbis.cs108.client.ui.events.admin;

import ch.unibas.dmi.dbis.cs108.client.ui.events.UIEvent;

public class ChangeNameUIEvent implements UIEvent {
    private final String newName;

    public ChangeNameUIEvent(String newName) {
        this.newName = newName;
    }

    public String getNewName() {
        return newName;
    }

    @Override
    public String getType() {
        return "CHANGENAME";
    }
}