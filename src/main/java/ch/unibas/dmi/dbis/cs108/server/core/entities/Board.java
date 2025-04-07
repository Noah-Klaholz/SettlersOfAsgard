package ch.unibas.dmi.dbis.cs108.server.core.entities;

import java.util.ArrayList;
import java.util.List;

public class Board {
    private Tile[][] tiles;

    /*
     * Constructor for Board class
     * Initializes the board with a 2D array of tiles.
     * The size of the board is currently set to 7 rows and 8 columns.
     * This can be changed later if needed or changed in initBoard method.
     */
    public Board() {
        tiles = new Tile[7][8]; // 7 rows and 8 columns
    }

    // Initialize board with a specified number of tiles
    // usable for potential other maps with other sizes.
    public void initBoard(int x, int y) {
        tiles = new Tile[x][y];
    }

    public void setTiles(Tile[][] tiles) {
        this.tiles = tiles;
    }

    public Tile getTileByCoordinates(int x, int y) {
        if (x < 0 || x >= tiles.length || y < 0 || y >= tiles[0].length) {
            System.out.println("Coordinates out of bounds");
            return null; // Out of bounds
        }
        return tiles[x][y];
    }

    public void setTileByCoordinates(int x, int y, Tile tile) {
        if (x < 0 || x >= tiles.length || y < 0 || y >= tiles[0].length) {
            System.out.println("Coordinates out of bounds");
            return; // Out of bounds
        }
        tiles[x][y] = tile;
    }

    public void setTile(int x, int y, Tile tile) {
        if (x < 0 || x >= tiles.length || y < 0 || y >= tiles[0].length) {
            System.out.println("Coordinates out of bounds");
            return; // Out of bounds
        }
        tiles[x][y] = tile;
    }

    public Tile getTile(int id) {
        for (int i = 0; i < tiles.length; i++) {
            for (int j = 0; j < tiles[i].length; j++) {
                if (tiles[i][j] != null && tiles[i][j].getTileID() == id) {
                    return tiles[i][j];
                }
            }
        }
        return null;
    }

    /*
    * Getter for all tiles
    *
    * @return Tile[][]
    */
    public Tile[][] getTiles() {
        return tiles;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Board{");
        sb.append("tiles=[");
        for (int i = 0; i < tiles.length; i++) {
            sb.append("[");
            for (int j = 0; j < tiles[i].length; j++) {
                sb.append(tiles[i][j] != null ? tiles[i][j].toString() : "null");
                if (j < tiles[i].length - 1) sb.append(", ");
            }
            sb.append("]");
            if (i < tiles.length - 1) sb.append(", ");
        }
        sb.append("]");
        sb.append("}");
        return sb.toString();
    }
}
