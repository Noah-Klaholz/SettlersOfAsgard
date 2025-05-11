package ch.unibas.dmi.dbis.cs108.client.ui.events.lobby;

import ch.unibas.dmi.dbis.cs108.client.ui.events.UIEvent;

/**
 * UIEvent sent by the host to update a specific lobby setting.
 */
public class UpdateLobbySettingsEvent implements UIEvent {

    /**
     * The ID of the lobby where the settings are being updated.
     */
    private final String lobbyId;
    /**
     * The setting key to be updated (e.g., "maxPlayers").
     */
    private final String settingKey;
    /**
     * The new value for the setting (e.g., "6").
     */
    private final String settingValue;

    /**
     * Constructs an UpdateLobbySettingsEvent.
     *
     * @param lobbyId      the ID of the lobby
     * @param settingKey   the setting key (e.g., "maxPlayers")
     * @param settingValue the setting value (e.g., "6")
     */
    public UpdateLobbySettingsEvent(String lobbyId, String settingKey, String settingValue) {
        this.lobbyId = lobbyId;
        this.settingKey = settingKey;
        this.settingValue = settingValue;
    }

    /**
     * Returns the ID of the lobby.
     *
     * @return lobby ID
     */
    public String getLobbyId() {
        return lobbyId;
    }

    /**
     * Returns the setting key.
     *
     * @return setting key
     */
    public String getSettingKey() {
        return settingKey;
    }

    /**
     * Returns the setting value.
     *
     * @return setting value
     */
    public String getSettingValue() {
        return settingValue;
    }

    /**
     * Returns the event type identifier.
     *
     * @return event type
     */
    @Override
    public String getType() {
        return "UPDATE_LOBBY_SETTINGS";
    }
}
