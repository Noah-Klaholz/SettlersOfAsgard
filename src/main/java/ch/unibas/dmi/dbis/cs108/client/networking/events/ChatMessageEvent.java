package ch.unibas.dmi.dbis.cs108.client.networking.events;

import java.time.Instant;

public class ChatMessageEvent implements Event {
    private final Instant timestamp = Instant.now();
    private final String sender;
    private final String content;
    private final ChatType type;

    public ChatMessageEvent(String sender, String content, ChatType type) {
        this.sender = sender;
        this.content = content;
        this.type = type;
    }

    @Override
    public Instant getTimestamp() {
        return timestamp;
    }

    public String getSender() {
        return sender;
    }

    public String getContent() {
        return content;
    }

    public ChatType getType() {
        return type;
    }

    public enum ChatType {
        GLOBAL, LOBBY, PRIVATE
    }
}