package ch.unibas.dmi.dbis.cs108.server;

import ch.unibas.dmi.dbis.cs108.server.core.model.GameState;
import ch.unibas.dmi.dbis.cs108.server.core.structures.Lobby;
import ch.unibas.dmi.dbis.cs108.server.core.logic.GameLogic;
import ch.unibas.dmi.dbis.cs108.server.core.logic.TurnManager;
import ch.unibas.dmi.dbis.cs108.server.networking.ClientHandler;
import ch.unibas.dmi.dbis.cs108.shared.game.Player;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Comprehensive test suite for {@link GameLogic}, verifying correct
 * handling of players and actions.
 * Uses Mockito for mocking dependencies.
 */
@ExtendWith(MockitoExtension.class)
public class GameLogicTest {

    private Lobby lobby;
    private ClientHandler player1, player2, player3, player4;
    private GameLogic gameLogic;
    private GameState gameState;
    TurnManager turnManager;

    /**
     * Initializes a test environment before each test:
     * - Creates a Lobby with maximal 4 players.
     * - Creates the 4 player objects, sets the names and adds them to the lobby
     *   and starts the game.
     * - Gets the gameLogic, gameState and turnManager objects from the lobby/gameLogic.
     */
    @BeforeEach
    void setUp(){
        lobby = new Lobby("testLobby", 4);
        player1 = mock(ClientHandler.class);
        player2 = mock(ClientHandler.class);
        player3 = mock(ClientHandler.class);
        player4 = mock(ClientHandler.class);
        when(player1.getPlayerName()).thenReturn("player1");
        when(player2.getPlayerName()).thenReturn("player2");
        when(player3.getPlayerName()).thenReturn("player3");
        when(player4.getPlayerName()).thenReturn("player4");
        lobby.addPlayer(player1);
        lobby.addPlayer(player2);
        lobby.addPlayer(player3);
        lobby.addPlayer(player4);
        lobby.startGame();
        gameLogic = lobby.getGameLogic();
        gameState = gameLogic.getGameState();
        turnManager = gameLogic.getTurnManager();
    }

    @Test
    void testStartGame() {
        // Verify gameLogic is not null after setup
        assertNotNull(gameLogic);
        // Verify gameState is not null after setup
        assertNotNull(gameState);
        // Verify turn manager is not null after setup
        assertNotNull(turnManager);
        // Verify players were properly set
        List<Player> players = gameState.getPlayers();
        assertEquals(4, players.size());
        String[] playerNames = players.stream().map(Player::getName).toArray(String[]::new);
        assertEquals("player1", playerNames[0]);
        assertEquals("player2", playerNames[1]);
        assertEquals("player3", playerNames[2]);
        assertEquals("player4", playerNames[3]);
        // Verify board was initialized (8x7 as per startGame implementation)
        assertEquals(8, gameState.getBoardManager().getBoard().getTiles().length);
        assertEquals(7, gameState.getBoardManager().getBoard().getTiles()[0].length);
        // Verify correct metadata initialization
        assertEquals(0, gameState.getGameRound());
        assertEquals(0, gameState.getPlayerRound());
        assertEquals("player1", gameState.getPlayerTurn());
    }

}