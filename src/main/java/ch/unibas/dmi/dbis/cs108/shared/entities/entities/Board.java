package ch.unibas.dmi.dbis.cs108.shared.entities.entities;

/**
 * Class representing a game board.
 * The board is represented as a 2D array of tiles.
 * Each tile can contain various entities such as players, artefacts, etc.
 */
public class Board {
    private Tile[][] tiles;

    /**
     * Constructor for Board class
     * Initializes the board with a 2D array of tiles.
     * The size of the board is currently set to 7 rows and 8 columns.
     * This can be changed later if needed or changed in initBoard method.
     */
    public Board() {
        tiles = new Tile[7][8]; // 7 rows and 8 columns
        initBoard(7, 8);
    }

    /**
     * Initialize board with a specified number of tiles
     * usable for potential other maps with other sizes.
     *
     * @param x The number of rows in the board.
     * @param y The number of columns in the board.
     */
    public void initBoard(int x, int y) {
        //tiles = new Tile[x][y];
        Tile.TileBuilder tilebuilder = new Tile.TileBuilder();
        tilebuilder.setPrice(10);
        for (int i=0; i<x; i++){
            for (int j=0; j<y; j++){
                tilebuilder.setX(i).setY(j);
                tiles[i][j] = new Tile(tilebuilder);
            }
        }
    }

    /**
     * Adds a tile to the board.
     *
     * @param tiles The 2D array of tiles to be added.
     */
    public void setTiles(Tile[][] tiles) {
        this.tiles = tiles;
    }

    /**
     * Gets a tile from the board by its coordinates.
     *
     * @param x The x-coordinate of the tile.
     * @param y The y-coordinate of the tile.
     * @return The tile at the specified coordinates, or null if out of bounds.
     */
    public Tile getTileByCoordinates(int x, int y) {
        if (x < 0 || x >= tiles.length || y < 0 || y >= tiles[0].length) {
            System.out.println("Coordinates out of bounds");
            return null; // Out of bounds
        }
        return tiles[x][y];
    }

    /**
     * Sets a tile at the specified coordinates.
     *
     * @param x    The x-coordinate of the tile.
     * @param y    The y-coordinate of the tile.
     * @param tile The tile to be set at the specified coordinates.
     */
    public void setTileByCoordinates(int x, int y, Tile tile) {
        if (x < 0 || x >= tiles.length || y < 0 || y >= tiles[0].length) {
            System.out.println("Coordinates out of bounds");
            return; // Out of bounds
        }
        tiles[x][y] = tile;
    }

    /**
     * Sets a tile at the specified coordinates.
     *
     * @param x    The x-coordinate of the tile.
     * @param y    The y-coordinate of the tile.
     * @param tile The tile to be set at the specified coordinates.
     */
    public void setTile(int x, int y, Tile tile) {
        if (x < 0 || x >= tiles.length || y < 0 || y >= tiles[0].length) {
            System.out.println("Coordinates out of bounds");
            return; // Out of bounds
        }
        tiles[x][y] = tile;
    }

    /**
     * Gets a tile from the board by its ID.
     *
     * @param id The ID of the tile.
     * @return The tile with the specified ID, or null if not found.
     */
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

    /**
    * Getter for all tiles
    *
    * @return Tile[][]
    */
    public Tile[][] getTiles() {
        return tiles;
    }

    /**
     * Gets all tiles on the board.
     *
     * @return A list of all tiles on the board.
     */
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

    /**
     * Resets all tiles on the board to their initial state.
     * This includes:
     * - Marking tiles as unpurchased
     * - Removing any structures
     * - Resetting any tile-specific state
     */
    public void resetTiles() {
        for (Tile[] tile : tiles) {
            for (Tile value : tile) {
                if (value != null) {
                    // Reset tile properties
                    value.setPurchased(false);
                    value.setHasStructure(false);
                    value.setStructure(null);

                    // If there are other properties to reset, add them here
                    // tiles[i][j].setSomeOtherProperty(defaultValue);
                }
            }
        }
    }
}
