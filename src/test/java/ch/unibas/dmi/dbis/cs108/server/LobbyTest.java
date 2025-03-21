package ch.unibas.dmi.dbis.cs108.server;

import ch.unibas.dmi.dbis.cs108.server.core.structures.Lobby;
import ch.unibas.dmi.dbis.cs108.server.networking.ClientHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class LobbyTest {

    private Lobby lobby;
    private ClientHandler player1;
    private ClientHandler player2;

    @BeforeEach
    void setUp() {
        lobby = new Lobby("lobby2", 2);
        player1 = mock(ClientHandler.class);
        player2 = mock(ClientHandler.class);
    }

    @Test
    void testAddPlayer() {
        // Test adding a player to the lobby
        assertTrue(lobby.addPlayer(player1));
        assertEquals(1, lobby.getPlayers().size());
        assertTrue(lobby.getPlayers().contains(player1));
    }

    @Test
    void testAddPlayerWhenFull() {
        // Test adding a player when the lobby is full
        lobby.addPlayer(player1);
        lobby.addPlayer(player2);
        ClientHandler player3 = mock(ClientHandler.class);
        assertFalse(lobby.addPlayer(player3)); // Lobby is full
        assertEquals(2, lobby.getPlayers().size());
    }

    @Test
    void testRemovePlayer() {
        // Test removing a player from the lobby
        lobby.addPlayer(player1);
        lobby.removePlayer(player1);
        assertEquals(0, lobby.getPlayers().size());
        assertFalse(lobby.getPlayers().contains(player1));
    }

    @Test
    void testStartGame() {
        // Test starting the game in the lobby
        lobby.addPlayer(player1);
        lobby.addPlayer(player2);
        assertTrue(lobby.startGame());
    }

    @Test
    void testToString() {
        // Test the toString method
        lobby.addPlayer(player1);
        String expected = "Lobby{id='lobby2', players=1, maxPlayers=2, isGameStarted=false}";
        assertEquals(expected, lobby.toString());
    }
}