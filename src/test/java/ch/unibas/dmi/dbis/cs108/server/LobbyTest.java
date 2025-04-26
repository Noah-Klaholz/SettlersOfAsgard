package ch.unibas.dmi.dbis.cs108.server;

import ch.unibas.dmi.dbis.cs108.server.core.model.GameState;
import ch.unibas.dmi.dbis.cs108.server.core.model.Leaderboard;
import ch.unibas.dmi.dbis.cs108.server.core.structures.Lobby;
import ch.unibas.dmi.dbis.cs108.server.core.logic.GameLogic;
import ch.unibas.dmi.dbis.cs108.server.core.logic.TurnManager;
import ch.unibas.dmi.dbis.cs108.server.networking.ClientHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Comprehensive test suite for {@link Lobby}, verifying player management,
 * game state transitions, turn handling, and message broadcasting.
 * Uses Mockito for mocking dependencies.
 */
@ExtendWith(MockitoExtension.class)
public class LobbyTest {

    private Lobby lobby;
    private ClientHandler player1;
    private ClientHandler player2;

    /**
     * Initializes test environment before each test:
     * - Creates a new lobby with max 2 players
     * - Initializes mock player objects
     */
    @BeforeEach
    void setUp() {
        lobby = new Lobby("testLobby", 2, mock(Leaderboard.class));
        player1 = mock(ClientHandler.class);
        player2 = mock(ClientHandler.class);
    }

    // Player Management Tests

    /**
     * Tests successful player addition to lobby.
     * Verifies:
     * - addPlayer returns true
     * - Player count increases
     * - Player list contains added player
     */
    @Test
    void testAddPlayer() {
        assertTrue(lobby.addPlayer(player1));
        assertEquals(1, lobby.getPlayers().size());
        assertTrue(lobby.getPlayers().contains(player1));
    }

    /**
     * Tests player addition when lobby is full.
     * Verifies:
     * - addPlayer returns false
     * - Player count remains at max capacity
     */
    @Test
    void testAddPlayerWhenFull() {
        lobby.addPlayer(player1);
        lobby.addPlayer(player2);
        assertFalse(lobby.addPlayer(mock(ClientHandler.class)));
        assertEquals(2, lobby.getPlayers().size());
    }

    /**
     * Tests successful game start with full lobby.
     * Verifies:
     * - startGame returns true
     * - Lobby status changes to IN_GAME
     */
    @Test
    void testStartGame() {
        lobby.addPlayer(player1);
        lobby.addPlayer(player2);
        assertTrue(lobby.startGame());
        assertEquals("In-Game", lobby.getStatus());
    }

    /**
     * Tests successful manual turn ending during active game.
     * Verifies:
     * - manualEndTurn returns true
     * - TurnManager.nextTurn() is invoked
     */
    @Test
    void testManualEndTurn_Success() {
        // Setup mocks only for this test
        GameLogic mockGameLogic = mock(GameLogic.class);
        TurnManager mockTurnManager = mock(TurnManager.class);
        GameState mockGameState = mock(GameState.class);
        when(mockGameLogic.getTurnManager()).thenReturn(mockTurnManager);
        when(player1.getPlayerName()).thenReturn("Player1"); // Stub only when needed
        when(player2.getPlayerName()).thenReturn("Player2");

        lobby.addPlayer(player1);
        lobby.addPlayer(player2);
        lobby.startGame();
        lobby.setGameLogic(mockGameLogic);
        when(mockGameLogic.getGameState()).thenReturn(mockGameState);

        assertTrue(lobby.manualEndTurn());
        verify(mockTurnManager).nextTurn();
    }

    /**
     * Tests player listing functionality.
     * Verifies:
     * - Contains all player names
     * - Names are properly delimited
     */
    @Test
    void testListPlayers() {
        when(player1.getPlayerName()).thenReturn("Player1"); // Stub only when needed
        when(player2.getPlayerName()).thenReturn("Player2");

        lobby.addPlayer(player1);
        lobby.addPlayer(player2);

        String players = lobby.listPlayers();
        assertTrue(players.contains("Player1"));
        assertTrue(players.contains("Player2"));
    }

    /**
     * Tests message broadcasting to all players.
     * Verifies:
     * - Message is sent to each player exactly once
     * - Message content is preserved
     */
    @Test
    void testBroadcastMessage() {
        lobby.addPlayer(player1);
        lobby.addPlayer(player2);
        lobby.broadcastMessage("TEST_MSG");
        verify(player1).sendMessage("TEST_MSG");
        verify(player2).sendMessage("TEST_MSG");
    }

    /**
     * Tests removing a player from an empty lobby.
     * Verifies:
     * - removePlayer returns false
     * - Player count remains 0
     */
    @Test
    void testRemovePlayerWhenEmpty() {
        assertFalse(lobby.removePlayer(player1));
        assertEquals(0, lobby.getPlayers().size());
    }

    /**
     * Tests removing a non-existent player from a non-empty lobby.
     * Verifies:
     * - removePlayer returns false
     * - Player count remains unchanged
     */
    @Test
    void testRemoveNonExistentPlayer() {
        lobby.addPlayer(player1);
        assertFalse(lobby.removePlayer(player2));
        assertEquals(1, lobby.getPlayers().size());
    }

    /**
     * Tests adding a null player.
     * Verifies:
     * - addPlayer returns false
     * - Player count remains 0
     */
    @Test
    void testAddNullPlayer() {
        assertFalse(lobby.addPlayer(null));
        assertEquals(0, lobby.getPlayers().size());
    }

    /**
     * Tests removing a null player.
     * Verifies:
     * - removePlayer returns false
     */
    @Test
    void testRemoveNullPlayer() {
        assertFalse(lobby.removePlayer(null));
    }

    /**
     * Tests starting game when lobby is not full.
     * Verifies:
     * - startGame returns false
     * - Status remains IN_LOBBY
     */
    @Test
    void testStartGameWhenNotFull() {
        lobby.addPlayer(player1);
        assertFalse(lobby.startGame());
        assertEquals("In lobby", lobby.getStatus());
    }

    /**
     * Tests starting game when already in game.
     * Verifies:
     * - startGame returns false
     * - Status remains IN_GAME
     */
    @Test
    void testStartGameWhenAlreadyInGame() {
        lobby.addPlayer(player1);
        lobby.addPlayer(player2);
        assertTrue(lobby.startGame());
        assertEquals("In-Game", lobby.getStatus());

        // Try to start again
        assertFalse(lobby.startGame());
        assertEquals("In-Game", lobby.getStatus());
    }

    /**
     * Tests manual turn ending when not in game.
     * Verifies:
     * - manualEndTurn returns false
     */
    @Test
    void testManualEndTurnWhenNotInGame() {
        assertFalse(lobby.manualEndTurn());
    }

    /**
     * Tests listing players when lobby is empty.
     * Verifies:
     * - Returns "No available players" message
     */
    @Test
    void testListPlayersWhenEmpty() {
        assertEquals("No available players", lobby.listPlayers());
    }

    /**
     * Tests that turn scheduler is stopped when game ends.
     * Verifies:
     * - turnScheduler is shutdown after endGame
     */
    @Test
    void testTurnSchedulerStopsOnGameEnd() {
        lobby.addPlayer(player1);
        lobby.addPlayer(player2);
        lobby.startGame();
        lobby.endGame();
        // Verify scheduler was stopped
        assertTrue(lobby.getTurnScheduler().isShutdown());
    }

    /**
     * Tests broadcasting to empty lobby.
     * Verifies:
     * - No exceptions thrown when broadcasting to empty lobby
     */
    @Test
    void testBroadcastToEmptyLobby() {
        assertDoesNotThrow(() -> lobby.broadcastMessage("TEST"));
    }

    /**
     * Tests broadcasting null message.
     * Verifies:
     * - No exceptions thrown when broadcasting null
     */
    @Test
    void testBroadcastNullMessage() {
        lobby.addPlayer(player1);
        assertDoesNotThrow(() -> lobby.broadcastMessage(null));
        verify(player1).sendMessage(null);
    }
}