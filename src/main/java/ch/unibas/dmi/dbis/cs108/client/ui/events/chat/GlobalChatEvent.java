package ch.unibas.dmi.dbis.cs108.client.ui.events.chat;

import ch.unibas.dmi.dbis.cs108.client.ui.events.UIEvent;

import java.time.LocalDateTime;

public class GlobalChatEvent implements UIEvent {
    private final String content;
    private final ChatType chatType;
    private final LocalDateTime timestamp;
    private String sender;

    public GlobalChatEvent(String content, ChatType chatType) {
        this.content = content;
        this.chatType = chatType;
        this.sender = null;
        this.timestamp = LocalDateTime.now();
    }

    public GlobalChatEvent(String content, String sender, ChatType chatType) {
        this.content = content;
        this.sender = sender;
        this.chatType = chatType;
        this.timestamp = LocalDateTime.now();
    }

    public String getContent() {
        return content;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public ChatType getChatType() {
        return chatType;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    // Removed redundant getMessage() method

    @Override
    public String getType() {
        return "CHAT_MESSAGE";
    }

    public enum ChatType {
        GLOBAL,
        SYSTEM,
    }
}
