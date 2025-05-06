package ch.unibas.dmi.dbis.cs108.client.networking.events;

import java.time.Instant;
import java.util.List;

/**
 * Event representing a player joining a lobby.
 * This event is triggered when a player successfully joins a lobby in the game.
 */
public class LobbyJoinedEvent implements Event {
    private final String lobbyId;
    private final List<String> players; // the players in the lobby
    private final String player; // the player that just joined the lobby
    private final boolean isHost;
    private final Instant timestamp = Instant.now();

    /**
     * Constructor for LobbyJoinedEvent.
     *
     * @param lobbyId The ID of the lobby that was joined.
     * @param players The players in the lobby, separated by '%'.
     * @param isHost  Indicates if the player is the host of the lobby.
     */
    public LobbyJoinedEvent(String lobbyId, String players, boolean isHost) {
        this.lobbyId = lobbyId;
        if (players.contains("%")) {
            this.players = List.of(players.split("%"));
            this.player = this.players.get(this.players.size() - 1);
        } else { // if only 1 player is in the lobby, then only the playerName gets transmitted
            this.players = List.of(players);
            this.player = players;
        }
        this.isHost = isHost;
    }

    /**
     * getter for the timestamp of the event.
     *
     * @return the timestamp of the event
     */
    @Override
    public Instant getTimestamp() {
        return timestamp;
    }

    /**
     * getter for the logger of the event.
     *
     * @return the logger of the event
     */
    public String getLobbyId() {
        return lobbyId;
    }

    /**
     * getter for the players in the lobby.
     *
     * @return the players in the lobby
     */
    public List<String> getPlayers() {
        return players;
    }

    /**
     * getter for the player that just joined the lobby.
     *
     * @return the player that just joined the lobby
     */
    public String getPlayer() {
        return player;
    }

    /**
     * getter for the host of the lobby.
     *
     * @return true if the player is the host, false otherwise
     */
    public boolean isHost() {
        return isHost;
    }

    /**
     * Returns the type of the event.
     *
     * @return The type of the event as a string.
     */
    public String getType() {
        return "LOBBYJOINED";
    }
}
