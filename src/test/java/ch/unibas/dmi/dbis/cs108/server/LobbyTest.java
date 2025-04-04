package ch.unibas.dmi.dbis.cs108.server;

import ch.unibas.dmi.dbis.cs108.server.core.structures.Lobby;
import ch.unibas.dmi.dbis.cs108.server.networking.ClientHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

/**
 * Test class for the Lobby class.
 */
@ExtendWith(MockitoExtension.class)
public class LobbyTest {

    private Lobby lobby;
    private ClientHandler player1;
    private ClientHandler player2;

    /**
     * Set up the test environment.
     */
    @BeforeEach
    void setUp() {
        lobby = new Lobby("lobby2", 2);
        player1 = mock(ClientHandler.class);
        player2 = mock(ClientHandler.class);
    }

    /**
     * Tests the addPlayer method.
     */
    @Test
    void testAddPlayer() {
        // Test adding a player to the lobby
        assertTrue(lobby.addPlayer(player1));
        assertEquals(1, lobby.getPlayers().size());
        assertTrue(lobby.getPlayers().contains(player1));
    }

    /**
     * Tests the addPlayer method when the lobby is full.
     * Lobby should not add the player-
     */
    @Test
    void testAddPlayerWhenFull() {
        // Test adding a player when the lobby is full
        lobby.addPlayer(player1);
        lobby.addPlayer(player2);
        ClientHandler player3 = mock(ClientHandler.class);
        assertFalse(lobby.addPlayer(player3)); // Lobby is full
        assertEquals(2, lobby.getPlayers().size());
    }

    /**
     * Tests the removePlayer method.
     */
    @Test
    void testRemovePlayer() {
        // Test removing a player from the lobby
        lobby.addPlayer(player1);
        lobby.removePlayer(player1);
        assertEquals(0, lobby.getPlayers().size());
        assertFalse(lobby.getPlayers().contains(player1));
    }

    /**
     * Tests the removePlayer method when the lobby is empty.
     * Should not remove anything from the lobby and not throw an exception
     */
    @Test
    void testRemovePlayerWhenEmpty() {
        // Test removing a player from the lobby
        assertFalse(lobby.removePlayer(player1));
        assertTrue(lobby.getPlayers().isEmpty());
    }

    /**
     * Tests the startGame method.
     */
    @Test
    void testStartGame() {
        // Test starting the game in the lobby
        lobby.addPlayer(player1);
        lobby.addPlayer(player2);
        assertTrue(lobby.startGame());
    }

    /**
     * Tests the toString method.
     * Should return a string representation of the lobby, including the id, number of players, max players, and game status.
     */
    @Test
    void testToString() {
        // Test the toString method
        lobby.addPlayer(player1);
        String expected = "Lobby{id='lobby2', players=1, maxPlayers=2, status=In lobby}";
        assertEquals(expected, lobby.toString());
    }
}