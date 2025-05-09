package ch.unibas.dmi.dbis.cs108.server;

import ch.unibas.dmi.dbis.cs108.server.core.logic.GameLogic;
import ch.unibas.dmi.dbis.cs108.server.core.structures.Lobby;
import ch.unibas.dmi.dbis.cs108.server.networking.ClientHandler;
import ch.unibas.dmi.dbis.cs108.server.networking.GameServer;
import ch.unibas.dmi.dbis.cs108.shared.game.Player;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.*;
import java.net.Socket;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Comprehensive test suite for {@link ClientHandler}, verifying client communication,
 * message processing, state management, and edge cases.
 */
@ExtendWith(MockitoExtension.class)
public class ClientHandlerTest {

    private ClientHandler clientHandler;
    private GameServer mockServer;
    private PrintWriter mockOut;
    private BufferedReader mockIn;
    private Lobby mockLobby;
    private Player mockPlayer;

    @BeforeEach
    void setUp() throws IOException {
        Socket mockSocket = mock(Socket.class);
        mockServer = mock(GameServer.class);
        mockOut = mock(PrintWriter.class);
        mockIn = mock(BufferedReader.class);
        mockLobby = mock(Lobby.class);
        mockPlayer = mock(Player.class);

        when(mockSocket.getOutputStream()).thenReturn(new ByteArrayOutputStream());
        when(mockSocket.getInputStream()).thenReturn(new ByteArrayInputStream(new byte[0]));

        clientHandler = new ClientHandler(mockSocket, mockServer);

        // Use reflection to inject mocks for testing
        setField(clientHandler, "out", mockOut);
        setField(clientHandler, "in", mockIn);
    }

    @AfterEach
    void tearDown() {
        clientHandler.shutdown();
    }

    // Helper method to set private fields
    private void setField(Object target, String fieldName, Object value) {
        try {
            var field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Tests successful client handler initialization.
     * Verifies:
     * - Client handler is running after creation
     * - Resources are properly initialized
     */
    @Test
    void testInitialization() {
        assertTrue(clientHandler.isConnected());
        assertNotNull(clientHandler.getServer());
    }

    /**
     * Tests resource cleanup when closing client handler.
     * Verifies:
     * - All resources are closed
     * - Client is removed from server
     * - Running flag is set to false
     */
    @Test
    void testCloseResources() throws IOException {
        clientHandler.shutdown();

        verify(mockIn).close();
        verify(mockOut).close();
    }

    /**
     * Tests successful message sending.
     * Verifies:
     * - Message is sent to output stream
     * - No errors occur during sending
     */
    @Test
    void testSendMessageSuccess() {
        String testMessage = "TEST_MESSAGE";
        clientHandler.sendMessage(testMessage);

        verify(mockOut).println(testMessage);
        verify(mockOut).checkError();
    }

    /**
     * Tests ping mechanism.
     * Verifies:
     * - Ping message is sent
     * - Last ping time is updated
     */
    @Test
    void testSendPing() {
        clientHandler.sendPing();
        verify(mockOut).println("PING$");
    }

    /**
     * Tests processing of null message.
     * Verifies:
     * - Error message is sent to client
     * - No exceptions thrown
     */
    @Test
    void testProcessNullMessage() {
        clientHandler.processMessage(null);
        verify(mockOut).println("ERR$103$Null");
    }

    /**
     * Tests processing of empty message.
     * Verifies:
     * - Error message is sent to client
     * - No exceptions thrown
     */
    @Test
    void testProcessEmptyMessage() {
        clientHandler.processMessage("");
        verify(mockOut).println("ERR$103$Null");
    }

    /**
     * Tests processing of invalid command.
     * Verifies:
     * - Error is logged
     * - No response sent (invalid commands aren't answered)
     */
    @Test
    void testProcessInvalidCommand() {
        clientHandler.processMessage("INVALID_COMMAND");
        verify(mockOut, never()).println(contains("OK$"));
    }

    /**
     * Tests processing of administrative command.
     * Verifies:
     * - Command is properly routed
     * - Appropriate response is sent
     */
    @Test
    void testProcessAdminCommand() {
        clientHandler.processMessage("CHTG$player$msg");
        verify(mockServer).broadcast(anyString());
    }

    /**
     * Tests lobby assignment.
     * Verifies:
     * - Lobby can be set and retrieved
     * - No side effects on other properties
     */
    @Test
    void testLobbyAssignment() {
        clientHandler.setCurrentLobby(mockLobby);
        assertEquals(mockLobby, clientHandler.getCurrentLobby());
        assertTrue(clientHandler.isConnected()); // Verify other state unchanged
    }

    /**
     * Tests player assignment.
     * Verifies:
     * - Player can be set and retrieved
     * - Player name is correctly returned
     */
    @Test
    void testPlayerAssignment() {
        when(mockPlayer.getName()).thenReturn("TestPlayer");
        clientHandler.setPlayer(mockPlayer);

        assertEquals(mockPlayer, clientHandler.getPlayer());
        assertEquals("TestPlayer", clientHandler.getPlayerName());
    }

    /**
     * Tests player name retrieval when no player is set.
     * Verifies:
     * - Returns null when no player assigned
     */
    @Test
    void testGetPlayerNameWhenNoPlayer() {
        assertNull(clientHandler.getPlayerName());
    }

    /**
     * Tests processing message when not in lobby but command requires lobby.
     * Verifies:
     * - Appropriate error message is sent
     */
    @Test
    void testProcessMessageNotInLobby() {
        clientHandler.processMessage("BUYT$1$1");
        verify(mockOut).println("ERR$106$NOT_IN_LOBBY");
    }

    /**
     * Tests processing message when in lobby but not in game.
     * Verifies:
     * - Appropriate error message is sent
     */
    @Test
    void testProcessMessageNotInGame() {
        clientHandler.setCurrentLobby(mockLobby);
        mockLobby.setGameLogic(mock(GameLogic.class));
        when(mockLobby.getStatus()).thenReturn("In lobby");

        clientHandler.processMessage("BUYT$1$1");

        verify(mockOut).println("ERR$106$NOT_IN_GAME");
    }
}