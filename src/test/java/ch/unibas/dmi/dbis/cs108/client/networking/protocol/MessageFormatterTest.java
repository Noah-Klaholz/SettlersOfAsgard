package ch.unibas.dmi.dbis.cs108.client.networking.protocol;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ch.unibas.dmi.dbis.cs108.client.networking.protocol.MessageFormatter;
import static org.junit.jupiter.api.Assertions.*;

public class MessageFormatterTest {

    private MessageFormatter messageFormatter;

    @BeforeEach
    public void setUp() {
        messageFormatter = new MessageFormatter();
    }

    @Test
    public void testFormatChatMessage() {
        // Testing static method
        String result = MessageFormatter.formatChatMessage("user123", "Hello, world!");
        assertEquals("CHTG$user123$Hello, world!", result);

        // Test with special characters
        result = MessageFormatter.formatChatMessage("user123", "Hello: with; special characters!");
        assertEquals("CHTG$user123$Hello: with; special characters!", result);
    }

    @Test
    public void testFormatNameChange() {
        // This method is not implemented yet, so we expect null
        String result = messageFormatter.formatNameChange("user123", "newName");
        assertNull(result);

        // TODO: Update this test when the method is implemented
    }

    @Test
    public void testFormatDisconnect() {
        String result = messageFormatter.formatDisconnect("user123");
        assertEquals("EXIT$user123", result);

        // Test with empty string
        result = messageFormatter.formatDisconnect("");
        assertEquals("EXIT$", result);
    }

    @Test
    public void testFormatPing() {
        // Current implementation returns "PING:"
        String result = messageFormatter.formatPing("user123");
        assertEquals("PING", result);

        // TODO: Update this test when the method is fully implemented
    }

    @Test
    public void testFormatRegister() {
        String result = messageFormatter.formatRegister("user123", "John Doe");
        assertEquals("JOIN$user123$John Doe", result);

        // Test with special characters
        result = messageFormatter.formatRegister("user:123", "John; Doe");
        assertEquals("JOIN$user:123$John; Doe", result);
    }
}
