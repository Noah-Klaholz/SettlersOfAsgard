package ch.unibas.dmi.dbis.cs108.client.ui.events.chat;

import ch.unibas.dmi.dbis.cs108.client.ui.events.UIEvent;

import java.time.LocalDateTime;

/**
 * UIEvent representing a private message (whisper) between two players.
 */
public class WhisperChatEvent implements UIEvent {
    /**
     * The sender of the message (null if not specified).
     */
    private final String sender;
    /**
     * The recipient of the message.
     */
    private final String recipient;
    /**
     * The message content.
     */
    private final String message;
    /**
     * The timestamp of when the message was sent.
     */
    private final LocalDateTime timestamp;

    /**
     * Constructs a WhisperChatEvent for receiving a whisper.
     *
     * @param sender    the player who sent the message
     * @param recipient the player who received the message
     * @param message   the message content
     */
    public WhisperChatEvent(String sender, String recipient, String message) {
        this.sender = sender;
        this.recipient = recipient;
        this.message = message;
        this.timestamp = LocalDateTime.now();
    }

    /**
     * Constructs a WhisperChatEvent for sending a whisper from the UI.
     *
     * @param recipient the player to send the message to
     * @param message   the message content
     */
    public WhisperChatEvent(String recipient, String message) {
        this(null, recipient, message);
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
     * Returns the recipient of the message.
     *
     * @return the recipient
     */
    public String getRecipient() {
        return recipient;
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
        return "WHISPER_CHAT_MESSAGE";
    }
}
