package ch.unibas.dmi.dbis.cs108.client.ui.events;

public class SendChatEvent {
    private final String message;
    private final ChatType type;
    private final String recipient; // Only for private messages

    public SendChatEvent(String message, ChatType type) {
        this(message, type, null);
    }

    public SendChatEvent(String message, ChatType type, String recipient) {
        this.message = message;
        this.type = type;
        this.recipient = recipient;
    }

    public String getMessage() {
        return message;
    }

    public ChatType getType() {
        return type;
    }

    public String getRecipient() {
        return recipient;
    }

    public enum ChatType {
        GLOBAL, LOBBY, PRIVATE
    }
}