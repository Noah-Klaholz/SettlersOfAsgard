package ch.unibas.dmi.dbis.cs108.client.ui.events.admin;

import ch.unibas.dmi.dbis.cs108.client.ui.events.UIEvent;

import java.util.List;

/**
 * UIEvent carrying the response to a player list request.
 */
public class PlayerListResponseEvent implements UIEvent {

    private final List<String> playerNames;

    /**
     * Constructs a new PlayerListResponseEvent.
     *
     * @param playerNames a list of player names
     */
    public PlayerListResponseEvent(List<String> playerNames) {
        this.playerNames = playerNames;
    }

    /**
     * Gets the list of player names.
     *
     * @return a list of player names
     */
    public List<String> getPlayerNames() {
        return playerNames;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getType() {
        return "PLAYER_LIST_RESPONSE";
    }
}
