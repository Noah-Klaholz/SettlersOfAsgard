package ch.unibas.dmi.dbis.cs108.client.networking.protocol;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import ch.unibas.dmi.dbis.cs108.client.networking.protocol.MessageParser;

public class MessageParserTest {

    private MessageParser messageParser;

    @BeforeEach
    public void setUp() {
        messageParser = new MessageParser();
    }

    @Test
    public void testParseChatMessage() {
        // Test valid chat message format
        String result = messageParser.parseChatMessage("CHTG$John$Hello, world!");
        assertEquals("John: Hello, world!", result);

        // Test with special characters
        result = messageParser.parseChatMessage("CHTG$Alice$Hello: with; special$ characters!");
        assertEquals("Alice: Hello: with; special$ characters!", result);

        // Test with insufficient parts
        result = messageParser.parseChatMessage("CHTG$John");
        assertEquals("Invalid chat message format", result);

        // Test with empty message
        result = messageParser.parseChatMessage("CHTG$John$");
        assertEquals("John: ", result);
    }

    @Test
    public void testParseRegistrationResponse() {
        // Test successful registration
        String result = messageParser.parseRegistrationResponse("JOIN$SUCCESS");
        assertEquals("SUCCESS", result);

        // Test failed registration
        result = messageParser.parseRegistrationResponse("JOIN$USERNAME_TAKEN");
        assertEquals("USERNAME_TAKEN", result);

        // Test invalid format
        result = messageParser.parseRegistrationResponse("JOIN");
        assertEquals("Unknown", result);

        // Test with empty response
        result = messageParser.parseRegistrationResponse("JOIN$");
        assertEquals("", result);
    }

    @Test
    public void testParsePingResponse() {
        // Test valid timestamp
        long result = messageParser.parsePingResponse("PING$1234567890");
        assertEquals(1234567890L, result);

        // Test invalid number format
        result = messageParser.parsePingResponse("PING$abc");
        assertEquals(0L, result);

        // Test missing timestamp
        result = messageParser.parsePingResponse("PING");
        assertEquals(0L, result);

        // Test empty timestamp
        result = messageParser.parsePingResponse("PING$");
        assertEquals(0L, result);
    }
}
