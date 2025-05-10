package ch.unibas.dmi.dbis.cs108.client.ui.events.lobby;

import ch.unibas.dmi.dbis.cs108.client.networking.events.PlayerListEvent;
import ch.unibas.dmi.dbis.cs108.client.ui.events.UIEvent;

import java.util.List;

/**
 * PlayerListResponseUIEvent is an event that represents a response containing a list of players in a lobby.
 * It implements the UIEvent interface and contains information about the players and the type of event.
 */
public class PlayerListResponseUIEvent implements UIEvent {
    /** The list of players in the lobby. */
    private final List<String> playerList;
    /** The type of the event (LOBBY_LIST). */
    private final PlayerListEvent.ListType listType;

    /**
     * Constructor for PlayerListResponseUIEvent.
     *
     * @param playerList The list of players in the lobby.
     */
    public PlayerListResponseUIEvent(List<String> playerList, PlayerListEvent.ListType listType) {
        this.playerList = playerList;
        this.listType = listType;
    }

    /**
     * Gets the list of players in the lobby.
     *
     * @return The list of players.
     */
    public List<String> getPlayerList() {
        return playerList;
    }

    /**
     * Gets the type of the event.
     *
     * @return The type of the event.
     */
    public PlayerListEvent.ListType getListType() {
        return PlayerListEvent.ListType.LOBBY_LIST;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getType() {
        return "PLAYER_LIST_RESPONSE";
    }
}
