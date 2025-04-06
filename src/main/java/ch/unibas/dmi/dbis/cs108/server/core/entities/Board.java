package ch.unibas.dmi.dbis.cs108.server.core.entities;

import java.util.ArrayList;
import java.util.List;

public class Board {
    private Tile[][] tiles;

    public Board() {
        tiles = new Tile[7][8]; // 7 rows and 8 columns
    }

    //todo: 2d array statt list: 7x8

    // Initialize board with a specified number of tiles
    // usable for potential other maps with other sizes.
    public void initBoard(int x, int y) {
        tiles = new Tile[x][y];
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

    public Tile[][] getTiles() {
        return tiles;
    }
}
