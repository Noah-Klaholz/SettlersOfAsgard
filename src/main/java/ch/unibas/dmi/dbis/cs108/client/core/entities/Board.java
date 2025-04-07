package ch.unibas.dmi.dbis.cs108.client.core.entities;

import java.util.ArrayList;
import java.util.List;

/**
 * Class representing the game board.
 * The board consists of a list of tiles.
 */
public class Board {
    private List<Tile> tiles;

    /**
     * Constructor for the Board class.
     * Initializes the board with an empty list of tiles.
     */
    public Board() {
        tiles = new ArrayList<>();
    }

    //todo: 2d array statt list

    /**
     * Initializes the board with a specified number of tiles.
     * For demo purposes, the resource value of each tile is simply (i+1).
     *
     * @param numberOfTiles The number of tiles to initialize on the board.
     */
    public void initBoard(int numberOfTiles) {
//        for (int i = 0; i < numberOfTiles; i++) {
//            // For demo purposes, resourceValue is simply (i+1)
//            tiles.add(new Tile(i, i + 1));
//        }
    }

    /**
     * Adds a tile to the board.
     *
     * @param id The id of the tile to be added.
     * @return tile The tile to be added.
     */
    public Tile getTile(int id) {
        for (Tile tile : tiles) {
            if (tile.getTileID() == id) {
                return tile;
            }
        }
        return null;
    }

    /**
     * Adds a tile to the board.
     *
     * @return tile The tile to be added.
     */
    public List<Tile> getTiles() {
        return tiles;
    }
}
