package ch.unibas.dmi.dbis.cs108.client.networking.events;

import java.time.Instant;

/**
 * Class representing an event related to lobby actions.
 * This class implements the Event interface and provides details about the lobby action.
 */
public class LobbyEvent implements Event {
    private final Instant timestamp = Instant.now();
    private final LobbyAction action;
    private final String playerName;
    private final String lobbyName;

    /**
     * Constructor for LobbyEvent.
     *
     * @param action     The action performed in the lobby (e.g., LEFT, CREATED).
     * @param playerName The name of the player involved in the action.
     * @param lobbyName  The name of the lobby where the action took place.
     */
    public LobbyEvent(LobbyAction action, String playerName, String lobbyName) {
        this.action = action;
        this.playerName = playerName;
        this.lobbyName = lobbyName;
    }

    /**
     * Gets the timestamp of the event.
     *
     * @return The timestamp of the event.
     */
    @Override
    public Instant getTimestamp() {
        return timestamp;
    }

    /**
     * Gets the action performed in the lobby.
     *
     * @return The action performed in the lobby.
     */
    public LobbyAction getAction() {
        return action;
    }

    /**
     * Gets the name of the player involved in the action.
     *
     * @return The name of the player.
     */
    public String getPlayerName() {
        return playerName;
    }

    /**
     * Gets the name of the lobby where the action took place.
     *
     * @return The name of the lobby.
     */
    public String getLobbyName() {
        return lobbyName;
    }

    /**
     * Enum representing the possible actions that can occur in a lobby.
     */
    public enum LobbyAction {
        LEFT, CREATED
    }
}
