package ch.unibas.dmi.dbis.cs108.client.networking.events;

import java.time.Instant;

/**
 * Represents a chat message event in the game.
 * This class encapsulates the details of a chat message, including the sender, content, and type of chat.
 */
public class ChatMessageEvent implements Event {
    private final Instant timestamp = Instant.now();
    private final String sender;
    private final String content;
    private final ChatType type;

    /**
     * Constructor for GlobalChatEvent.
     *
     * @param sender  The sender of the chat message.
     * @param content The content of the chat message.
     * @param type    The type of chat (e.g., GLOBAL, LOBBY, PRIVATE, INFO).
     */
    public ChatMessageEvent(String sender, String content, ChatType type) {
        this.sender = sender;
        this.content = content;
        this.type = type;
    }

    /**
     * Getter for the timestamp of the event.
     *
     * @return The timestamp of the event.
     */
    @Override
    public Instant getTimestamp() {
        return timestamp;
    }

    /**
     * Getter for the sender of the chat message.
     *
     * @return The sender of the chat message.
     */
    public String getSender() {
        return sender;
    }

    /**
     * Getter for the content of the chat message.
     *
     * @return The content of the chat message.
     */
    public String getContent() {
        return content;
    }

    /**
     * Getter for the type of chat.
     *
     * @return The type of chat (e.g., GLOBAL, LOBBY, PRIVATE, INFO).
     */
    public ChatType getType() {
        return type;
    }

    /**
     * Enum representing the type of chat.
     */
    public enum ChatType {
        /**
         * Messages sent to all players in the game.
         */
        GLOBAL,

        /**
         * Messages sent only to players in the same lobby.
         */
        LOBBY,

        /**
         * Private messages sent between individual players.
         */
        PRIVATE,

        /**
         * System information messages not sent by any player.
         */
        INFO
    }
}