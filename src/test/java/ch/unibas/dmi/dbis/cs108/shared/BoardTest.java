package ch.unibas.dmi.dbis.cs108.shared;

import ch.unibas.dmi.dbis.cs108.shared.entities.GameEntity;
import ch.unibas.dmi.dbis.cs108.shared.entities.Purchasables.PurchasableEntity;
import ch.unibas.dmi.dbis.cs108.shared.entities.Purchasables.Statues.Statue;
import ch.unibas.dmi.dbis.cs108.shared.entities.Purchasables.Structure;
import ch.unibas.dmi.dbis.cs108.shared.game.Board;
import ch.unibas.dmi.dbis.cs108.shared.game.Tile;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

public class BoardTest {

    private final int WIDTH = 10;
    private final int HEIGHT = 10;
    private Board board;

    @BeforeEach
    void setUp() {
        board = new Board();
        board.initBoard(WIDTH, HEIGHT);
    }

    @Test
    void testBoardInitialization() {
        assertNotNull(board);
        assertEquals(WIDTH, board.getTiles().length);
        assertEquals(HEIGHT, board.getTiles()[0].length);

        // Check that all tiles are initialized
        for (int x = 0; x < WIDTH; x++) {
            for (int y = 0; y < HEIGHT; y++) {
                Tile tile = board.getTileByCoordinates(x, y);
                assertNotNull(tile);
                assertNull(tile.getOwner());
            }
        }
    }

    @Test
    void testSetTileOwner() {
        String playerName = "TestPlayer";
        board.getTileByCoordinates(1, 1).setOwner(playerName);

        Tile tile = board.getTileByCoordinates(1, 1);
        assertEquals(playerName, tile.getOwner());
    }

    @Test
    void testSetTileEntity() {
        PurchasableEntity mockEntity = mock(Structure.class);
        board.getTileByCoordinates(2, 3).setEntity(mockEntity);

        Tile tile = board.getTileByCoordinates(2, 3);
        assertTrue(tile.hasEntity());
        assertEquals(mockEntity, tile.getEntity());
    }

    @Test
    void testRemoveTileEntity() {
        // First add an entity
        GameEntity mockEntity = mock(Structure.class);
        board.getTileByCoordinates(4, 5).setEntity(mockEntity);

        // Then remove it
        GameEntity removedEntity = board.getTileByCoordinates(4, 5).removeEntity();

        // Verify the entity was removed
        assertEquals(mockEntity, removedEntity);
        assertFalse(board.getTileByCoordinates(4, 5).hasEntity());
    }

    @Test
    void testOutOfBoundsTile() {
        // Test getting a tile outside the board boundaries
        assertNull(board.getTileByCoordinates(-1, 0));
        assertNull(board.getTileByCoordinates(0, -1));
        assertNull(board.getTileByCoordinates(WIDTH + 1, 0));
        assertNull(board.getTileByCoordinates(0, HEIGHT + 1));
    }

    @Test
    void testGetAdjacentTiles() {
        // Test getting adjacent tiles at center
        Tile[] adjacentCenter = board.getAdjacentTiles(5, 5);
        assertEquals(4, adjacentCenter.length);

        // Test getting adjacent tiles at corner (top-left)
        Tile[] adjacentCorner = board.getAdjacentTiles(0, 0);
        assertEquals(2, adjacentCorner.length);

        // Test getting adjacent tiles at edge
        Tile[] adjacentEdge = board.getAdjacentTiles(0, 5);
        assertEquals(3, adjacentEdge.length);
    }

    @Test
    void testMultipleEntitiesOnBoard() {
        PurchasableEntity structure = mock(Structure.class);
        PurchasableEntity statue = mock(Statue.class);

        board.getTileByCoordinates(1, 1).setEntity(structure);
        board.getTileByCoordinates(2, 2).setEntity(statue);

        assertEquals(structure, board.getTileByCoordinates(1, 1).getEntity());
        assertEquals(statue, board.getTileByCoordinates(2, 2).getEntity());
    }

    @Test
    void testTileOwnershipChange() {
        String player1 = "Player1";
        String player2 = "Player2";

        // Set initial owner
        board.getTileByCoordinates(3, 3).setOwner(player1);
        assertEquals(player1, board.getTileByCoordinates(3, 3).getOwner());

        // Change ownership
        board.getTileByCoordinates(3, 3).setOwner(player2);
        assertEquals(player2, board.getTileByCoordinates(3, 3).getOwner());
    }

    @Test
    void testRemoveNonExistentEntity() {
        // Try to remove an entity from a tile that doesn't have one
        GameEntity removed = board.getTileByCoordinates(6, 6).removeEntity();
        assertNull(removed);
    }

    @Test
    void testResetBoard() {
        // First setup some board state
        board.getTileByCoordinates(1, 1).setOwner("Player");
        board.getTileByCoordinates(2, 2).setEntity(mock(Structure.class));

        // Reset the board
        board.resetTiles();

        // Verify board is back to initial state
        for (int x = 0; x < WIDTH; x++) {
            for (int y = 0; y < HEIGHT; y++) {
                Tile tile = board.getTileByCoordinates(x, y);
                assertNull(tile.getOwner());
                assertFalse(tile.hasEntity());
            }
        }
    }
}