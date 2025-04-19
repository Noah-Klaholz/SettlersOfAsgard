package ch.unibas.dmi.dbis.cs108.client.ui.events.lobby;

import ch.unibas.dmi.dbis.cs108.client.ui.events.UIEvent;

import java.util.Map;

public class GameStartedEvent implements UIEvent {

    public GameStartedEvent() {}

    @Override
    public String getType() {
        return "GAMESTART";
    }
}
