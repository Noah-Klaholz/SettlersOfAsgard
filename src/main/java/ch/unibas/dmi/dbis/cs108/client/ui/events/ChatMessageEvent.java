package ch.unibas.dmi.dbis.cs108.client.ui.events;

public class ChatMessageEvent {
    private final String message;

    public ChatMessageEvent(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}