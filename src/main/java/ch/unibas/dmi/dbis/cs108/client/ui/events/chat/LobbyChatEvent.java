package ch.unibas.dmi.dbis.cs108.client.ui.events.chat;

import ch.unibas.dmi.dbis.cs108.client.ui.events.UIEvent;

import java.time.LocalDateTime;

/**
 * UIEvent representing a chat message within a lobby.
 */
public class LobbyChatEvent implements UIEvent {
    /**
     * The ID of the lobby where the message was sent.
     */
    private final String lobbyId;
    /**
     * The sender of the message (null if not specified).
     */
    private final String sender;
    /**
     * The message content.
     */
    private final String message;
    /**
     * The timestamp of when the message was sent.
     */
    private final LocalDateTime timestamp;

    /**
     * Constructs a LobbyChatEvent with all fields specified.
     *
     * @param lobbyId the ID of the lobby
     * @param sender  the sender of the message
     * @param message the message content
     */
    public LobbyChatEvent(String lobbyId, String sender, String message) {
        this.lobbyId = lobbyId;
        this.sender = sender;
        this.message = message;
        this.timestamp = LocalDateTime.now();
    }

    /**
     * Constructs a LobbyChatEvent for sending a message (sender will be set by
     * server).
     *
     * @param lobbyId the ID of the lobby
     * @param message the message content
     */
    public LobbyChatEvent(String lobbyId, String message) {
        this(lobbyId, null, message);
    }

    /**
     * Returns the lobby ID.
     *
     * @return the lobby ID
     */
    public String getLobbyId() {
        return lobbyId;
    }

    /**
     * Returns the sender of the message.
     *
     * @return the sender
     */
    public String getSender() {
        return sender;
    }

    /**
     * Returns the message content.
     *
     * @return the message
     */
    public String getMessage() {
        return message;
    }

    /**
     * Returns the timestamp of the message.
     *
     * @return the timestamp
     */
    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    /**
     * Returns the type of this event.
     *
     * @return the event type
     */
    @Override
    public String getType() {
        return "LOBBY_CHAT_MESSAGE";
    }
}
