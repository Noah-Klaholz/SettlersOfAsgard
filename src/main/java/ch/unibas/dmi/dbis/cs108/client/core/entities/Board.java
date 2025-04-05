package ch.unibas.dmi.dbis.cs108.client.core.entities;

import java.util.ArrayList;
import java.util.List;

public class Board {
    private List<Tile> tiles;

    public Board() {
        tiles = new ArrayList<>();
    }

    //todo: 2d array statt list

    // Initialize board with a specified number of fields.
    public void initBoard(int numberOfTiles) {
//        for (int i = 0; i < numberOfTiles; i++) {
//            // For demo purposes, resourceValue is simply (i+1)
//            tiles.add(new Tile(i, i + 1));
//        }
    }

    public Tile getTile(int id) {
        for (Tile tile : tiles) {
            if (tile.getTileID() == id) {
                return tile;
            }
        }
        return null;
    }

    public List<Tile> getTiles() {
        return tiles;
    }
}
