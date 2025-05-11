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
    /**
     * The timestamp of the event.
     */
    private final Instant timestamp = Instant.now();
    /**
     * The list of players in the lobby.
     */
    private final List<String> players = new ArrayList<>();

    private final ListType type;

    /**
     * Constructor for PlayerListEvent.
     * It takes a message string containing information about the players and parses it to create a list of String objects.
     *
     * @param message The message string containing list information.
     *  @param type    The type of the list (SERVER_LIST or LOBBY_LIST).
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
     *
     * @return A list of player names.
     */
    public List<String> getPlayers() {
        return players;
    }

    /**
     * Gets the type of the list (SERVER_LIST or LOBBY_LIST).
     *
     * @return The type of the list.
     */
    public ListType getType() {
        return type;
    }

    /**
     * ListType is an enum that represents the type of player list.
     * It can be either SERVER_LIST or LOBBY_LIST.
     */
    public enum ListType {
        /**
         * Represents a list of players in the server.
         */
        SERVER_LIST,
        /**
         * Represents a list of players in the lobby.
         */
        LOBBY_LIST
    }
}