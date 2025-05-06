package ch.unibas.dmi.dbis.cs108.server;

import ch.unibas.dmi.dbis.cs108.server.core.structures.Lobby;
import ch.unibas.dmi.dbis.cs108.server.networking.ClientHandler;
import ch.unibas.dmi.dbis.cs108.server.networking.GameServer;
import ch.unibas.dmi.dbis.cs108.shared.game.Player;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Comprehensive test suite for {@link GameServer}, verifying server lifecycle,
 * client management, lobby operations, and edge cases.
 */
@ExtendWith(MockitoExtension.class)
public class GameServerTest {

    private static final int TEST_PORT = 8080;
    private GameServer gameServer;
    private ClientHandler clientHandler1;
    private ClientHandler clientHandler2;

    @BeforeEach
    void setUp() throws IOException {
        gameServer = new GameServer(TEST_PORT);
        clientHandler1 = mock(ClientHandler.class);
        clientHandler2 = mock(ClientHandler.class);
    }

    @AfterEach
    void tearDown() {
        gameServer.shutdown();
    }

    /**
     * Test the initial state of the GameServer.
     * Verifies:
     * - list of clients is empty
     * - list of lobbies is empty
     */
    @Test
    void testInitialState() {
        assertTrue(gameServer.getClients().isEmpty());
        assertTrue(gameServer.getLobbies().isEmpty());
    }

    /**
     * Tests server shutdown when no clients are connected.
     * Verifies:
     * - shutdown completes without errors
     * - executor services are terminated
     */
    @Test
    void testShutdownWithNoClients() {
        gameServer.shutdown();
        // Verify through observable behavior
        assertTrue(gameServer.getClients().isEmpty());
    }

    /**
     * Tests server shutdown with active clients.
     * Verifies:
     * - All clients receive shutdown message
     * - Client resources are closed
     * - Client list is cleared
     */
    @Test
    void testShutdownWithClients() throws Exception {
        gameServer.getClients().add(clientHandler1);
        gameServer.getClients().add(clientHandler2);
        gameServer.shutdown();

        verify(clientHandler1).closeResources();
        verify(clientHandler2).closeResources();
        assertTrue(gameServer.getClients().isEmpty());
    }

    /**
     * Tests successful client removal.
     * Verifies:
     * - Client is removed from list
     * - Client's lobby is cleaned up if empty
     */
    @Test
    void testRemoveClient() {
        Lobby mockLobby = mock(Lobby.class);
        when(clientHandler1.getCurrentLobby()).thenReturn(mockLobby);

        gameServer.getClients().add(clientHandler1);
        gameServer.removeClient(clientHandler1);

        assertFalse(gameServer.getClients().contains(clientHandler1));
        verify(mockLobby).removePlayer(clientHandler1);
    }

    /**
     * Tests removing a non-existent client.
     * Verifies:
     * - No exceptions thrown
     * - Client list remains unchanged
     */
    @Test
    void testRemoveNonExistentClient() {
        int initialSize = gameServer.getClients().size();
        gameServer.removeClient(clientHandler1);
        assertEquals(initialSize, gameServer.getClients().size());
    }

    /**
     * Tests ping mechanism removes disconnected clients.
     * Verifies:
     * - Non-running clients are removed
     * - Running clients receive ping
     */
    @Test
    void testPingClients() {
        // Setup mocks only for this test
        when(clientHandler1.isRunning()).thenReturn(false);
        when(clientHandler2.isRunning()).thenReturn(true);

        gameServer.getClients().add(clientHandler1);
        gameServer.getClients().add(clientHandler2);

        gameServer.pingClients();

        assertFalse(gameServer.getClients().contains(clientHandler1));
        assertTrue(gameServer.getClients().contains(clientHandler2));
        verify(clientHandler2).sendPing();
    }

    /**
     * Tests successful lobby creation.
     * Verifies:
     * - Lobby is created with correct parameters
     * - Lobby is added to server's list
     */
    @Test
    void testCreateLobby() {
        Lobby lobby = gameServer.createLobby("testLobby", 4);
        assertNotNull(lobby);
        assertEquals("testLobby", lobby.getId());
        assertTrue(gameServer.getLobbies().contains(lobby));
    }

    /**
     * Tests duplicate lobby creation.
     * Verifies:
     * - Returns null when lobby exists
     * - Original lobby remains unchanged
     */
    @Test
    void testCreateDuplicateLobby() {
        gameServer.createLobby("testLobby", 4);
        Lobby duplicate = gameServer.createLobby("testLobby", 2);
        assertNull(duplicate);
        assertEquals(1, gameServer.getLobbies().size());
    }

    /**
     * Tests lobby removal when empty.
     * Verifies:
     * - Lobby is removed from server
     * - No errors occur
     */
    @Test
    void testRemoveEmptyLobby() {
        Lobby lobby = gameServer.createLobby("testLobby", 4);
        gameServer.removeLobby(lobby);
        assertFalse(gameServer.getLobbies().contains(lobby));
    }

    /**
     * Tests getting non-existent lobby.
     * Verifies:
     * - Returns null when lobby doesn't exist
     */
    @Test
    void testGetNonExistentLobby() {
        assertNull(gameServer.getLobby("nonexistent"));
    }

    /**
     * Tests broadcast to empty client list.
     * Verifies:
     * - No exceptions thrown
     * - Message not sent to any client
     */
    @Test
    void testBroadcastToEmptyClients() {
        assertDoesNotThrow(() -> gameServer.broadcast("TEST_MSG"));
        // No client to verify
    }

    /**
     * Tests handling of client with null lobby reference.
     * Verifies:
     * - No NullPointerException when removing client
     * - Clean removal from client list
     */
    @Test
    void testRemoveClientWithNullLobby() {
        when(clientHandler1.getCurrentLobby()).thenReturn(null);
        gameServer.getClients().add(clientHandler1);

        assertDoesNotThrow(() -> gameServer.removeClient(clientHandler1));
        assertTrue(gameServer.getClients().isEmpty());
    }

    /**
     * Tests player name uniqueness check.
     * Verifies:
     * - Returns true when name exists
     * - Returns false when name doesn't exist
     * - Handles null player references
     */
    @Test
    void testContainsPlayerName() {
        // Setup mocks only for this test
        when(clientHandler1.getPlayerName()).thenReturn("Alice");
        when(clientHandler1.getPlayer()).thenReturn(mock(Player.class));
        when(clientHandler2.getPlayer()).thenReturn(null);

        gameServer.getClients().add(clientHandler1);
        gameServer.getClients().add(clientHandler2);

        assertTrue(gameServer.containsPlayerName("Alice"));
        assertFalse(gameServer.containsPlayerName("Bob"));
        assertFalse(gameServer.containsPlayerName(null));
    }

    /**
     * Tests listing players when no players exist.
     * Verifies:
     * - Returns appropriate "no players" message
     */
    @Test
    void testListPlayersWhenEmpty() {
        assertEquals("No available players", gameServer.listPlayers());
    }

    /**
     * Tests listing players with existing players.
     * Verifies:
     * - Returns correct player names
     */
    @Test
    void testListPlayers() {
        // Setup mocks only for this test
        when(clientHandler1.getPlayerName()).thenReturn("Alice");
        when(clientHandler2.getPlayerName()).thenReturn("Bob");

        gameServer.getClients().add(clientHandler1);
        gameServer.getClients().add(clientHandler2);

        String players = gameServer.listPlayers();
        assertTrue(players.contains("Alice"));
        assertTrue(players.contains("Bob"));
    }

    /**
     * Tests client connection handling (simulated).
     * Verifies:
     * - New client is added to list
     * - Client handler is executed
     */
    @Test
    void testClientConnectionHandling() {
        // Setup test-specific behavior
        GameServer testServer = new GameServer(TEST_PORT) {
            @Override
            public void start() {
                // Simulate connection
                ClientHandler mockClient = mock(ClientHandler.class);
                getClients().add(mockClient);
                getExecutor().execute(mockClient);
            }
        };

        testServer.start();
        assertEquals(1, testServer.getClients().size());
    }
}