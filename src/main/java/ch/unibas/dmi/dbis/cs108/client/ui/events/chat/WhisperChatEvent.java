package ch.unibas.dmi.dbis.cs108.client.ui.events.chat;

import ch.unibas.dmi.dbis.cs108.client.ui.events.UIEvent;

import java.time.LocalDateTime;

public class WhisperChatEvent implements UIEvent {
    private final String sender;
    private final String recipient;
    private final String message;
    private final LocalDateTime timestamp;

    public WhisperChatEvent(String sender, String recipient, String message) {
        this.sender = sender;
        this.recipient = recipient;
        this.message = message;
        this.timestamp = LocalDateTime.now();
    }

    public String getSender() {
        return sender;
    }

    public String getRecipient() {
        return recipient;
    }

    public String getMessage() {
        return message;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    @Override
    public String getType() {
        return "WHISPER_MESSAGE";
    }
}