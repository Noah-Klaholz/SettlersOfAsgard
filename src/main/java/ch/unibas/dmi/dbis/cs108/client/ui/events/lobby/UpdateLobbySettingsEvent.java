package ch.unibas.dmi.dbis.cs108.client.ui.events.lobby;

import ch.unibas.dmi.dbis.cs108.client.ui.events.UIEvent;

public class UpdateLobbySettingsEvent implements UIEvent {
    private final String lobbyId;
    private final String settingKey;
    private final String settingValue;

    public UpdateLobbySettingsEvent(String lobbyId, String settingKey, String settingValue) {
        this.lobbyId = lobbyId;
        this.settingKey = settingKey;
        this.settingValue = settingValue;
    }

    public String getLobbyId() {
        return lobbyId;
    }

    public String getSettingKey() {
        return settingKey;
    }

    public String getSettingValue() {
        return settingValue;
    }

    @Override
    public String getType() {
        return "UPDATELOBBYSETTINGS";
    }
}