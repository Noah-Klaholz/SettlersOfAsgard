package ch.unibas.dmi.dbis.cs108.client.ui.events.chat;

import ch.unibas.dmi.dbis.cs108.client.ui.events.UIEvent;

import java.time.LocalDateTime;

public class LobbyChatEvent implements UIEvent {
    private final String lobbyId;
    private final String sender;
    private final String message;
    private final LocalDateTime timestamp;

    public LobbyChatEvent(String lobbyId, String sender, String message) {
        this.lobbyId = lobbyId;
        this.sender = sender;
        this.message = message;
        this.timestamp = LocalDateTime.now();
    }

    // Additional constructor for sending messages (server will set the sender)
    public LobbyChatEvent(String lobbyId, String message) {
        this(lobbyId, null, message);
    }

    public String getLobbyId() {
        return lobbyId;
    }

    public String getSender() {
        return sender;
    }

    public String getMessage() {
        return message;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    @Override
    public String getType() {
        return "LOBBY_CHAT_MESSAGE";
    }
}
