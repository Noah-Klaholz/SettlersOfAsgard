package ch.unibas.dmi.dbis.cs108.shared;

import ch.unibas.dmi.dbis.cs108.shared.entities.Purchasables.PurchasableEntity;
import ch.unibas.dmi.dbis.cs108.shared.entities.Purchasables.Structure;
import ch.unibas.dmi.dbis.cs108.shared.entities.Purchasables.Statues.Statue;
import ch.unibas.dmi.dbis.cs108.shared.game.Board;
import ch.unibas.dmi.dbis.cs108.shared.game.Tile;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class BoardTest {

    private Board board;
    private final int WIDTH = 10;
    private final int HEIGHT = 10;

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
                assertFalse(tile.hasEntity());
            }
        }
    }

    @Test
    void testSetTileOwner() {
        String playerName = "TestPlayer";
        board.getTile(1).setOwner(playerName);

        Tile tile = board.getTileByCoordinates(1, 1);
        assertEquals(playerName, tile.getOwner());
    }

    @Test
    void testSetTileEntity() {
        PurchasableEntity mockEntity = mock(Structure.class);
        board.getTileByCoordinates(2,3).setEntity(mockEntity);

        Tile tile = board.getTileByCoordinates(2, 3);
        assertTrue(tile.hasEntity());
        assertEquals(mockEntity, tile.getEntity());
    }

    @Test
    void testRemoveTileEntity() {
        // First add an entity
        PurchasableEntity mockEntity = mock(Structure.class);
        board.getTileByCoordinates(4,5).setEntity(mockEntity);

        // Then remove it
        PurchasableEntity removedEntity = board.getTileByCoordinates(4,5).removeEntity();

        // Verify the entity was removed
        assertEquals(mockEntity, removedEntity);
        assertFalse(board.getTileByCoordinates(4, 5).hasEntity());
    }

    @Test
    void testOutOfBoundsTile() {
        // Test getting a tile outside the board boundaries
        assertNull(board.getTileByCoordinates(-1, 0));
        assertNull(board.getTileByCoordinates(0, -1));
        assertNull(board.getTileByCoordinates(WIDTH+1, 0));
        assertNull(board.getTileByCoordinates(0, HEIGHT+1));
    }

    @Test
    void testSetTileOwnerOutOfBounds() {
        // Should not throw exception, just do nothing
        board.getTileByCoordinates(-1,0).setOwner("Player");
        board.getTileByCoordinates(WIDTH+1,HEIGHT+1).setOwner("Player");

        // Verify no changes to valid tiles
        assertNull(board.getTileByCoordinates(0, 0).getOwner());
        assertNull(board.getTileByCoordinates(WIDTH, HEIGHT).getOwner());
    }

    @Test
    void testSetAndRemoveEntityOutOfBounds() {
        PurchasableEntity mockEntity = mock(Structure.class);

        // Should not throw exception for out of bounds
        board.getTileByCoordinates(-1,0).setEntity(mockEntity);
        PurchasableEntity removed = board.getTileByCoordinates(WIDTH, HEIGHT).removeEntity();

        // Nothing should be returned when removing from invalid coordinates
        assertNull(removed);
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
        board.getTileByCoordinates(2, 2).setEntity(structure);

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
        PurchasableEntity removed = board.getTileByCoordinates(6, 6).removeEntity();
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