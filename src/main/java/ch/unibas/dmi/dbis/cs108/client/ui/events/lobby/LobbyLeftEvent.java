package ch.unibas.dmi.dbis.cs108.client.ui.events.lobby;

import ch.unibas.dmi.dbis.cs108.client.ui.events.UIEvent;

public class LobbyLeftEvent implements UIEvent {
    private final String playerName;
    
    public LobbyLeftEvent(String playerName) {
        this.playerName = playerName;
    }
    
    public String getPlayerName() {
        return playerName;
    }
    
    @Override
    public String getType() {
        return "LOBBYLEFT";
    }
}
