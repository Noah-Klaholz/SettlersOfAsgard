package ch.unibas.dmi.dbis.cs108.client.ui.events.lobby;

import ch.unibas.dmi.dbis.cs108.client.ui.events.UIEvent;

import java.util.Map;

public class GameStartedEvent implements UIEvent {
    private final Map<String, Object> gameData;

    public GameStartedEvent(Map<String, Object> gameData) {
        this.gameData = gameData;
    }

    public Map<String, Object> getGameData() {
        return gameData;
    }

    @Override
    public String getType() {
        return "GAMESTART";
    }
}
