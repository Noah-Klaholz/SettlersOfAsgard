package ch.unibas.dmi.dbis.cs108.client.networking.events;

import ch.unibas.dmi.dbis.cs108.client.ui.controllers.LobbyScreenController;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * LobbyListEvent is an event that represents a list of game lobbies.
 * It contains information about the lobbies, including their IDs, names, current players, maximum players, status, and host names.
 * This event is used to update the lobby list in the user interface.
 */
public class LobbyListEvent implements Event {
    /**
     * The timestamp of when the event occurred.
     */
    private final Instant timestamp = Instant.now();
    /**
     * The list of lobbies.
     */
    private final List<LobbyScreenController.GameLobby> lobbies = new ArrayList<>();

    /**
     * Constructor for LobbyListEvent.
     * It takes a message string containing information about the lobbies and parses it to create a list of GameLobby objects.
     *
     * @param message The message string containing lobby information.
     */
    public LobbyListEvent(String message) {
        String[] lobbies = message.split("%");
        Arrays.stream(lobbies).toList().forEach(lobby -> {
            String[] params = lobby.split(":");
            if (params.length >= 5) {
                LobbyScreenController.GameLobby lobbyObject = new LobbyScreenController.GameLobby(
                        params[0], // lobbyId
                        params[0], // name = id
                        Integer.parseInt(params[1]), // currentPlayers
                        Integer.parseInt(params[2]), // maxPlayers
                        params[3], // status
                        params[4] // hostName
                );
                this.lobbies.add(lobbyObject);
            }
        });
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
     * getLobbies returns the list of lobbies.
     *
     * @return The list of lobbies.
     */
    public List<LobbyScreenController.GameLobby> getLobbies() {
        return lobbies;
    }
}