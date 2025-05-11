package ch.unibas.dmi.dbis.cs108.server;

import ch.unibas.dmi.dbis.cs108.server.core.logic.GameLogic;
import ch.unibas.dmi.dbis.cs108.server.core.logic.TurnManager;
import ch.unibas.dmi.dbis.cs108.server.core.model.GameState;
import ch.unibas.dmi.dbis.cs108.server.core.model.Leaderboard;
import ch.unibas.dmi.dbis.cs108.server.core.structures.Lobby;
import ch.unibas.dmi.dbis.cs108.server.networking.ClientHandler;
import ch.unibas.dmi.dbis.cs108.shared.entities.EntityRegistry;
import ch.unibas.dmi.dbis.cs108.shared.entities.Findables.Artifact;
import ch.unibas.dmi.dbis.cs108.shared.entities.Purchasables.Structure;
import ch.unibas.dmi.dbis.cs108.shared.game.Player;
import ch.unibas.dmi.dbis.cs108.shared.game.Status;
import ch.unibas.dmi.dbis.cs108.shared.game.Tile;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

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
    private GameLogic gameLogic;
    private GameState gameState;
    private GameState spyGameState;
    private TurnManager turnManager;

    /**
     * Initializes a test environment before each test:
     * - Creates a Lobby with maximal 4 players.
     * - Creates the 4 player objects, sets the names and adds them to the lobby
     * and starts the game.
     * - Gets the gameLogic, gameState and turnManager objects from the lobby/gameLogic.
     */
    @BeforeEach
    void setUp() {
        lobby = new Lobby("testLobby", 4, mock(Leaderboard.class));
        ClientHandler player1 = mock(ClientHandler.class);
        ClientHandler player2 = mock(ClientHandler.class);
        ClientHandler player3 = mock(ClientHandler.class);
        ClientHandler player4 = mock(ClientHandler.class);
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
        spyGameState = spy(gameState);
        turnManager = gameLogic.getTurnManager();
    }

    @AfterEach
    void tearDown() {
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
        // Verify board was initialized
        // assertEquals(8, gameState.getBoardManager().getBoard().getTiles().length);
        // assertEquals(7, gameState.getBoardManager().getBoard().getTiles()[0].length);
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
        assertEquals("player1", owner);
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
        Tile t = gameState.getBoardManager().getTile(0, 1);
        Structure s = EntityRegistry.getStructure(1);
        int runesBefore = gameState.getPlayers().get(0).getRunes();
        assert s != null;
        int price = t.getPrice() + s.getPrice();
        // Verify that the player is able to buy a tile and place a structure on it
        assertTrue(gameLogic.buyTile(t.getX(), t.getY(), "player1"));
        assertTrue(gameLogic.placeStructure(t.getX(), t.getY(), 1, "player1"));
        int runesAfter = gameState.getPlayers().get(0).getRunes();
        // Verify players has the correct amount of runes
        assertEquals(runesBefore - price, runesAfter);
        // Verify the tile has the correct entity
        assertTrue(t.hasEntity());
        assertEquals(1, t.getEntity().getId());
        assertEquals(1, gameState.getPlayers().get(0).getPurchasableEntities().size());
        assertEquals(s.getId(), gameState.getPlayers().get(0).getPurchasableEntities().get(0).getId());
    }

    /**
     * This test verifies the correct functionality of the rune table structure. (1)
     */
    @Test
    void TestUseStructureRuneTable() {
        Tile t = gameState.getBoardManager().getTile(0, 1);
        Structure s = EntityRegistry.getStructure(1);
        int runesBefore = gameState.getPlayers().get(0).getRunes();
        assert s != null;
        int price = t.getPrice() + s.getPrice();
        assertTrue(gameLogic.buyTile(t.getX(), t.getY(), "player1"));
        assertTrue(gameLogic.placeStructure(t.getX(), t.getY(), 1, "player1"));
        int runesCurrently = runesBefore - price;
        gameState.getPlayers().get(0).setEnergy(4); // manually set energy to 4 to test this structure
        assertTrue(gameLogic.useStructure(t.getX(), t.getY(), 1, "player1"));
        int runesAfter = gameState.getPlayers().get(0).getRunes();
        // Check rune and energy count
        assertEquals(runesCurrently + s.getParams().get(1).getValue(), runesAfter);
        int energyCount = gameState.getPlayers().get(0).getEnergy();
        assertEquals(4 + s.getParams().get(0).getValue(), energyCount);
    }

    /**
     * This test verifies the correct functionality of the mimisbrunnr structure. (2)
     */
    @Test
    void TestUseStructureMimisbrunnr() {
        Tile t = gameState.getBoardManager().getTile(0, 1);
        Structure s = EntityRegistry.getStructure(2);
        assert s != null;
        assertTrue(gameLogic.buyTile(t.getX(), t.getY(), "player1"));
        assertTrue(gameLogic.placeStructure(t.getX(), t.getY(), s.getId(), "player1"));
        assertTrue(gameLogic.useStructure(t.getX(), t.getY(), s.getId(), "player1"));
        assertEquals(1, gameState.getPlayers().get(0).getArtifacts().size());
    }

    /**
     * This test verifies the correct functionality of the Helgrindr structure. (3)
     */
    @Test
    void testUseStructureHelgrindr() {
        Tile t = gameState.getBoardManager().getTile(0, 1);
        Structure s = EntityRegistry.getStructure(3);
        assert s != null;
        gameState.getPlayers().get(0).addRunes(100);
        assertTrue(gameLogic.buyTile(t.getX(), t.getY(), "player1"));
        assertTrue(gameLogic.placeStructure(t.getX(), t.getY(), s.getId(), "player1"));
        assertTrue(gameLogic.useStructure(t.getX(), t.getY(), s.getId(), "player1"));
        assertEquals(0, gameState.getPlayers().get(0).getStatus().get(Status.BuffType.DEBUFFABLE));

    }

    /**
     * This test verifies the correct functionality of the huginn and muninn structure. (4)
     * Places an artifact on tile (1,1) with the artifact id 10.
     * the structure should reveal the position of the artifact.
     */
    @Test
    void testUseStructureHuginnAndMuninn() {
        // Setup - place an artifact at (1,1)
        Tile t1 = gameState.getBoardManager().getTile(1, 1);
        Artifact a = EntityRegistry.getArtifact(10);
        t1.setArtifact(a);

        // Clear any existing notifications
        gameState.getNotifications().clear();

        Tile t2 = gameState.getBoardManager().getTile(2, 2);
        Structure s = EntityRegistry.getStructure(4);
        assert s != null;

        // Player buys tile and places structure
        assertTrue(gameLogic.buyTile(t2.getX(), t2.getY(), "player1"));
        assertTrue(gameLogic.placeStructure(t2.getX(), t2.getY(), s.getId(), "player1"));

        // Use the structure
        assertTrue(gameLogic.useStructure(t2.getX(), t2.getY(), s.getId(), "player1"));

        // Verify energy was added (from structure params)
        assertEquals(2, gameState.getPlayers().get(0).getEnergy());

        // Verify notification was sent
        List<String> notifications = gameState.getNotifications();
        assertFalse(notifications.isEmpty());
        assertTrue(notifications.contains("10$1$1"));
    }

    /**
     * This test verifies the correct functionality of the rans hall structure. (5)
     */
    @Test
    void testUseStructureRansHall() {
        Tile t = gameState.getBoardManager().getTile(2, 2);
        Structure s = EntityRegistry.getStructure(5);
        assert s != null;
        assertTrue(gameLogic.buyTile(t.getX(), t.getY(), "player1"));
        assertTrue(gameLogic.placeStructure(t.getX(), t.getY(), s.getId(), "player1"));
        assertTrue(gameLogic.useStructure(t.getX(), t.getY(), s.getId(), "player1"));
        assertEquals(2, gameState.getPlayers().get(0).getEnergy());
    }

    /**
     * This test verifies the correct functionality of the tree structure. (7)
     */
    @Test
    void testUseStructureTree() {
        Tile t = gameState.getBoardManager().getTile(2, 2);
        Structure s = EntityRegistry.getStructure(7);
        assertNotNull(s, "Tree structure should exist");
        int runesBefore = gameState.getPlayers().get(0).getRunes();
        int price = t.getPrice();
        assertTrue(gameLogic.buyTile(t.getX(), t.getY(), "player1"));
        assertTrue(gameLogic.placeStructure(t.getX(), t.getY(), s.getId(), "player1"));
        assertTrue(gameLogic.useStructure(t.getX(), t.getY(), s.getId(), "player1"));
        assertEquals(runesBefore - price, gameState.getPlayers().get(0).getRunes());
    }

    /**
     * This test verifies the correct functionality of the activeTrap structure. (8)
     */
    @Test
    void TestUseStructureActiveTrap() {
        // ActiveTrap should be placed on an unowned tile first
        Tile t = gameState.getBoardManager().getTile(0, 1);
        Structure s = EntityRegistry.getStructure(8);
        assertNotNull(s, "ActiveTrap structure should exist");

        // place trap
        t.setEntity(s);

        // Verify trap was placed but not activated yet
        assertNotNull(t.getEntity());
        assertEquals(s.getId(), t.getEntity().getId());

        // Now when player buys the tile, the trap activates
        int initialRunes = gameState.getPlayers().get(0).getRunes();
        assertTrue(gameLogic.buyTile(t.getX(), t.getY(), "player1"));

        // Verify trap effect (should reduce runes)
        int newRunes = gameState.getPlayers().get(0).getRunes();
        assertTrue(newRunes < initialRunes, "Trap should have reduced player's runes");

        // Verify tile is now owned despite the trap
        assertEquals("player1", t.getOwner());
    }

    // ----- Edge Cases -----

    /**
     * This test verifies the correct handling for edge cases when placing a structure
     */
    @Test
    void testPlaceStructure_InvalidCases() {
        // Test placing on invalid coordinates
        assertFalse(gameLogic.placeStructure(-1, 0, 1, "player1"), "Should fail - negative X");
        assertFalse(gameLogic.placeStructure(0, -1, 1, "player1"), "Should fail - negative Y");
        assertFalse(gameLogic.placeStructure(100, 0, 1, "player1"), "Should fail - X out of bounds");
        assertFalse(gameLogic.placeStructure(0, 100, 1, "player1"), "Should fail - Y out of bounds");

        // Test invalid structure ID
        assertFalse(gameLogic.placeStructure(0, 0, -1, "player1"), "Should fail - invalid structure ID");
        assertFalse(gameLogic.placeStructure(0, 0, 999, "player1"), "Should fail - non-existent structure");

        // Test placing on unowned tile (for non-trap structures)
        assertFalse(gameLogic.placeStructure(1, 1, 1, "player1"), "Should fail - tile not owned");

        // Test placing on tile with existing entity
        gameLogic.buyTile(0, 0, "player1");
        gameLogic.placeStructure(0, 0, 1, "player1");
        assertFalse(gameLogic.placeStructure(0, 0, 2, "player1"), "Should fail - tile already has structure");
    }

    /**
     * This test verifies correct edge case handling when using a structure
     */
    @Test
    void testUseStructure_InvalidCases() {
        // Setup valid structure first
        gameLogic.buyTile(0, 0, "player1");
        gameLogic.placeStructure(0, 0, 1, "player1");

        // Test invalid coordinates
        assertFalse(gameLogic.useStructure(-1, 0, 1, "player1"), "Should fail - negative X");
        assertFalse(gameLogic.useStructure(0, -1, 1, "player1"), "Should fail - negative Y");

        // Test wrong structure ID
        assertFalse(gameLogic.useStructure(0, 0, 2, "player1"), "Should fail - wrong structure ID");

        // Test using another player's structure
        gameLogic.buyTile(1, 1, "player2");
        gameLogic.placeStructure(1, 1, 1, "player2");
        assertFalse(gameLogic.useStructure(1, 1, 1, "player1"), "Should fail - not owner");

        // Test using already activated structure
        gameLogic.useStructure(0, 0, 1, "player1");
        assertFalse(gameLogic.useStructure(0, 0, 1, "player1"), "Should fail - already used this turn");
    }

    /**
     * This test verifies correct trap edge cases
     */
    @Test
    void testActiveTrap_EdgeCases() {
        gameLogic.buyTile(0, 0, "player1");

        // Test trap activation by different player
        Tile t = gameState.getBoardManager().getTile(1, 1);
        Structure trap = EntityRegistry.getStructure(8);
        t.setEntity(trap);

        int player2InitialRunes = gameState.findPlayerByName("player2").getRunes();
        assertTrue(gameLogic.buyTile(1, 1, "player2"), "Should succeed - buying trapped tile");
        assertTrue(gameState.findPlayerByName("player2").getRunes() < player2InitialRunes,
                "Trap should have reduced player2's runes");
    }

    /**
     * This test checks for edge cases when the player has insufficient resources
     */
    @Test
    void testInsufficientResources() {
        // Setup - drain player's resources
        Player player = gameState.findPlayerByName("player1");
        player.setRunes(0);

        // Test buying tile with no runes
        assertFalse(gameLogic.buyTile(0, 0, "player1"), "Should fail - insufficient runes");

        // Test placing structure with no runes
        player.setRunes(10); // Enough for tile but not structure
        assertTrue(gameLogic.buyTile(0, 0, "player1"), "Should work - enough runes for tile");
        assertFalse(gameLogic.placeStructure(0, 0, 1, "player1"), "Should fail - insufficient runes for structure");
    }

    /**
     * This test verifies edge cases for buying tiles
     */
    @Test
    void testBuyTile_EdgeCases() {
        // Test buying invalid coordinates
        assertFalse(gameLogic.buyTile(-1, 0, "player1"), "Should fail - negative X");
        assertFalse(gameLogic.buyTile(0, -1, "player1"), "Should fail - negative Y");
        assertFalse(gameLogic.buyTile(100, 0, "player1"), "Should fail - X out of bounds");
        assertFalse(gameLogic.buyTile(0, 100, "player1"), "Should fail - Y out of bounds");

        // Test buying already owned tile
        assertTrue(gameLogic.buyTile(0, 0, "player1"), "Initial purchase should succeed");
        assertFalse(gameLogic.buyTile(0, 0, "player1"), "Should fail - already owned by same player");
        assertFalse(gameLogic.buyTile(0, 0, "player2"), "Should fail - already owned by different player");

        // Test buying with insufficient runes
        Player player = gameState.findPlayerByName("player1");
        int originalRunes = player.getRunes();
        Tile expensiveTile = gameState.getBoardManager().getTile(1, 1);
        expensiveTile.setPrice(originalRunes + 100); // Make tile unaffordable

        assertFalse(gameLogic.buyTile(1, 1, "player1"), "Should fail - insufficient runes");
        assertEquals(originalRunes, player.getRunes(), "Runes should not change after failed purchase");

        // Test buying tile with trap (should succeed but reduce runes)
        Tile trappedTile = gameState.getBoardManager().getTile(2, 2);
        Structure trap = EntityRegistry.getStructure(8);
        trappedTile.setEntity(trap);

        int preTrapRunes = player.getRunes();
        assertTrue(gameLogic.buyTile(2, 2, "player1"), "Should succeed - buying trapped tile");
        assertTrue(player.getRunes() < preTrapRunes, "Trap should have reduced player's runes");
        assertEquals("player1", trappedTile.getOwner(), "Tile should now be owned");
    }

}