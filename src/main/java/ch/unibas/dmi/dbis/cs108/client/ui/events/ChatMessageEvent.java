package ch.unibas.dmi.dbis.cs108.client.ui.events;

import ch.unibas.dmi.dbis.cs108.client.networking.events.ChatMessageEvent.ChatType;

/**
 * Represents a chat message event in the game.
 * This class encapsulates the details of a chat message, including the content and type of chat.
 */
public class ChatMessageEvent {
    private final String message;
    private final ChatType chatType;

    /**
     * Constructor for ChatMessageEvent.
     *
     * @param message The content of the chat message.
     * @param chatType The type of chat (e.g., GLOBAL, LOBBY, PRIVATE, INFO).
     */
    public ChatMessageEvent(String message, ChatType chatType) {
        this.chatType = chatType;
        this.message = message;
    }

    /**
     * Getter for the content of the chat message.
     * @return The content of the chat message.
     */
    public String getMessage() {
        return message;
    }

    /**
     * Getter for the type of chat.
     * @return The type of chat (e.g., GLOBAL, LOBBY, PRIVATE, INFO).
     */
    public ChatType getType() {
        return chatType;
    }

}