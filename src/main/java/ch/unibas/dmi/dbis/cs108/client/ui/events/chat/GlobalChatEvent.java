package ch.unibas.dmi.dbis.cs108.client.ui.events.chat;

import ch.unibas.dmi.dbis.cs108.client.ui.events.UIEvent;

import java.time.LocalDateTime;

/**
 * UIEvent representing a global or system chat message.
 */
public class GlobalChatEvent implements UIEvent {
    private final String content;
    private final ChatType chatType;
    private final LocalDateTime timestamp;
    private String sender;

    /**
     * Constructs a GlobalChatEvent for sending a message (sender will be set by
     * server).
     *
     * @param content  the message content
     * @param chatType the type of chat message
     */
    public GlobalChatEvent(String content, ChatType chatType) {
        this.content = content;
        this.chatType = chatType;
        this.sender = null;
        this.timestamp = LocalDateTime.now();
    }

    /**
     * Constructs a GlobalChatEvent with all fields specified.
     *
     * @param content  the message content
     * @param sender   the sender of the message
     * @param chatType the type of chat message
     */
    public GlobalChatEvent(String content, String sender, ChatType chatType) {
        this.content = content;
        this.sender = sender;
        this.chatType = chatType;
        this.timestamp = LocalDateTime.now();
    }

    /**
     * Returns the message content.
     *
     * @return the message content
     */
    public String getContent() {
        return content;
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
     * Sets the sender of the message.
     *
     * @param sender the sender
     */
    public void setSender(String sender) {
        this.sender = sender;
    }

    /**
     * Returns the type of chat message.
     *
     * @return the chat type
     */
    public ChatType getChatType() {
        return chatType;
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
        return "CHAT_MESSAGE";
    }

    /**
     * Enum representing the type of chat message.
     */
    public enum ChatType {
        GLOBAL,
        SYSTEM
    }
}
