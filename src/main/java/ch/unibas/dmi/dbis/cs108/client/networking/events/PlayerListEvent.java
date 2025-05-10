package ch.unibas.dmi.dbis.cs108.client.networking.events;

import ch.unibas.dmi.dbis.cs108.client.ui.controllers.LobbyScreenController;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * PlayerListEvent is an event that represents a list of game players in a lobby.
 */
public class PlayerListEvent implements Event {
    private final Instant timestamp = Instant.now();
    private final List<String> players = new ArrayList<>();
    private final ListType type;

    /**
     * Constructor for PlayerListEvent.
     * It takes a message string containing information about the players and parses it to create a list of String objects.
     *
     * @param message The message string containing list information.
     */
    public PlayerListEvent(String message, ListType type) {
        this.type = type;
        String[] playerArray = message.split(", ");
        players.addAll(Arrays.asList(playerArray));
    }

    /**
     * getTimestamp returns the timestamp of the event.
     *
     * @return The timestamp of the event.
     */
    @Override
    public Instant getTimestamp() {
        return timestamp;
    }


    /**
     * Gets the list of players.
     */
    public List<String> getPlayers() {
        return players;
    }

    /**
     * Gets the type of the list (SERVER_LIST or LOBBY_LIST).
     */
    public ListType getType() {
        return type;
    }

    public enum ListType {
        SERVER_LIST,
        LOBBY_LIST
    }
}