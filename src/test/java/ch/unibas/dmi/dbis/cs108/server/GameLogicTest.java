package ch.unibas.dmi.dbis.cs108.server;

import ch.unibas.dmi.dbis.cs108.server.core.model.GameState;
import ch.unibas.dmi.dbis.cs108.server.core.structures.Lobby;
import ch.unibas.dmi.dbis.cs108.server.core.logic.GameLogic;
import ch.unibas.dmi.dbis.cs108.server.core.logic.TurnManager;
import ch.unibas.dmi.dbis.cs108.server.networking.ClientHandler;
import ch.unibas.dmi.dbis.cs108.shared.game.Player;
import ch.unibas.dmi.dbis.cs108.shared.entities.EntityRegistry;
import ch.unibas.dmi.dbis.cs108.shared.entities.Purchasables.Structure;

import java.util.List;

import ch.unibas.dmi.dbis.cs108.shared.entities.EntityRegistry;
import ch.unibas.dmi.dbis.cs108.shared.entities.Purchasables.Structure;
import ch.unibas.dmi.dbis.cs108.shared.game.Tile;
import org.junit.jupiter.api.AfterEach;
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
    private TurnManager turnManager;

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

    @AfterEach
    void tearDown() {
        // Shutdown the turn scheduler if the game is running
        if (lobby.getStatus().equals("In-Game")) {
            lobby.stopTurnScheduler();
        }
        if (gameLogic != null) {
            lobby.endGame();
        }
        lobby = null;
        gameLogic = null;
        gameState = null;
    }

    /**
     * This test checks the correct starting of the game. It verifies:
     * - Creation of GameLogic, GameState and TurnManager
     * - Player initialization
     * - Board initialization
     * - Metadata initialization
     */
    @Test
    void testStartGame() {
        // Verify gameLogic, gameState and turnManager are not null after setup
        assertNotNull(gameLogic);
        assertNotNull(gameState);
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

    /**
     * This test checks the turn progression. It verifies:
     * - Second Player is different from the first.
     * - Index of the new player is correct.
     * - After a complete round, the gameRound count is 1.
     */
    @Test
    void testTurnProgression() {
        List<Player> players = gameState.getPlayers();
        String firstPlayer = gameState.getPlayerTurn();
        // Advance turn
        lobby.manualEndTurn();
        String secondPlayer = gameState.getPlayerTurn();
        assertNotEquals(firstPlayer, secondPlayer);
        // Verify turn order is maintained
        int firstIndex = players.indexOf(gameState.findPlayerByName(firstPlayer));
        int secondIndex = players.indexOf(gameState.findPlayerByName(secondPlayer));
        assertEquals((firstIndex + 1) % players.size(), secondIndex);
        // Complete full rotation
        for (int i = 0; i < players.size() - 1; i++) {
            lobby.manualEndTurn();
        }
        // Verify game round increments
        assertEquals(1, gameState.getGameRound());
    }

    /**
     * This test checks the successful completion of a players request to buy a tile. It verifies:
     * - resource allocation
     * - tile properties
     * - player properties
     */
    @Test
    void testBuyTileAction() {
        int runesBefore = gameState.getPlayers().get(0).getRunes();
        Tile t = gameState.getBoardManager().getTile(0, 0);
        int price = t.getPrice();
        // Verify that the player can buy the tile
        assertTrue(gameLogic.buyTile(0, 0, "player1"));
        // Verify that the player has paid the resources
        int runesAfter = gameState.getPlayers().get(0).getRunes();
        assertEquals(runesBefore - price, runesAfter);
        // Verify that the tile has the correct owner
        String owner = gameState.getBoardManager().getBoard().getTiles()[0][0].getOwner();
        assertEquals("player1" ,owner);
        // Verify that the player owns this tile (and only this one)
        assertEquals(1, gameState.getPlayers().get(0).getOwnedTiles().size());
        assertEquals(t, gameState.getPlayers().get(0).getOwnedTiles().get(0));
    }

    /**
     * This test checks the successful completion of a players request to place a structure. It verifies:
     * - resource allocation
     * - tile properties
     * - player properties
     */
    @Test
    void testPlaceStructure() {
        Tile t = gameState.getBoardManager().getTile(0, 0);
        Structure s = EntityRegistry.getStructure(1);
        int runesBefore = gameState.getPlayers().get(0).getRunes();
        assert s != null;
        int price = t.getPrice() + s.getPrice();
        // Verify that the player is able to buy a tile and place a structure on it
        assertTrue(gameLogic.buyTile(0, 0, "player1"));
        assertTrue(gameLogic.placeStructure(0, 0, 1, "player1"));
        int runesAfter = gameState.getPlayers().get(0).getRunes();
        // Verify players has the correct amount of runes
        assertEquals(runesBefore - price, runesAfter);
        // Verify the tile has the correct entity
        assertTrue(t.hasEntity());
        assertEquals(1, t.getEntity().getId());
        assertEquals(1, gameState.getPlayers().get(0).getPurchasableEntities().size());
        assertEquals(gameState.getPlayers().get(0).getPurchasableEntities().get(0), s);
    }

}