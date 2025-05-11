package ch.unibas.dmi.dbis.cs108.client.ui.events.lobby;

import ch.unibas.dmi.dbis.cs108.client.ui.events.UIEvent;

/**
 * Event fired when host status transfers to a new player.
 */
public class HostTransferEvent implements UIEvent {
    private final String lobbyId;
    private final String newHostName;

    public HostTransferEvent(String lobbyId, String newHostName) {
        this.lobbyId = lobbyId;
        this.newHostName = newHostName;
    }

    public String getLobbyId() {
        return lobbyId;
    }

    public String getNewHostName() {
        return newHostName;
    }

    @Override
    public String getType() {
        return "HostTransferEvent";
    }
}
