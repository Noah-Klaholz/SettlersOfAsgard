package ch.unibas.dmi.dbis.cs108.client.ui.events;

import ch.unibas.dmi.dbis.cs108.client.networking.events.ChatMessageEvent.ChatType;

public class ChatMessageEvent {
    private final String message;
    private final ChatType chatType;

    public ChatMessageEvent(String message, ChatType chatType) {
        this.chatType = chatType;
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public ChatType getType() {
        return chatType;
    }

}