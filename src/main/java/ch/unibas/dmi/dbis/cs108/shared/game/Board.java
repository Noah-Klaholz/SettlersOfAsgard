package ch.unibas.dmi.dbis.cs108.shared.game;

import ch.unibas.dmi.dbis.cs108.SETTINGS;
import ch.unibas.dmi.dbis.cs108.shared.entities.EntityRegistry;
import ch.unibas.dmi.dbis.cs108.shared.entities.Findables.Monument;
import ch.unibas.dmi.dbis.cs108.shared.utils.RandomGenerator;

import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;

/**
 * Class representing a game board.
 * The board is represented as a 2D array of tiles.
 * Each tile can contain various entities such as players, artifacts, etc.
 */
public class Board {

    /**
     * A 2D array representation of the worlds on the map
     */
    private static final String[][] WORLD_MAP = {
            // y = 0 (top row)
            {"Alfheim", "Alfheim", "Alfheim", "Asgard", "Asgard", "Asgard", "Muspelheim", "Muspelheim"},
            // y = 1
            {"Alfheim", "Alfheim", "Asgard", "Asgard", "Asgard", "Asgard", "Muspelheim", "Muspelheim"},
            // y = 2
            {"Vanaheim", "Vanaheim", "Vanaheim", "Asgard", "Midgard", "Midgard", "Muspelheim", "Muspelheim"},
            // y = 3
            {"Vanaheim", "Vanaheim", "Vanaheim", "Midgard", "Midgard", "Midgard", "Muspelheim", "Muspelheim"},
            // y = 4
            {"Vanaheim", "Jotunheim", "Vanaheim", "Vanaheim", "Nilfheim", "Helheim", "Svartalfheim", "Svartalfheim"},
            // y = 5
            {"Jotunheim", "Jotunheim", "Nilfheim", "Nilfheim", "Helheim", "Helheim", "Helheim", "Svartalfheim"},
            // y = 6 (bottom row)
            {"Jotunheim", "Jotunheim", "Nilfheim", "Nilfheim", "Nilfheim", "Helheim", "Helheim", "Helheim"}
    };
    /**
     * Logger to log logging
     */
    Logger logger = Logger.getLogger(Board.class.getName());
    /**
     * Two-dimensional array of Tiles. Represents the entire board. The first dimension is x.
     */
    private Tile[][] tiles;

    /**
     * Initialize board with a specified number of tiles
     * usable for potential other maps with other sizes.
     *
     * @param x The number of rows in the board.
     * @param y The number of columns in the board.
     */
    public void initBoard(int x, int y) {
        tiles = new Tile[x][y];

        for (int i = 0; i < x; i++) {
            for (int j = 0; j < y; j++) {
                Tile.TileBuilder tilebuilder = new Tile.TileBuilder();
                tilebuilder.setPrice(10);
                // Set world based on coordinates
                tilebuilder.setWorld(determineWorld(i, j));
                // Set monument based on coordinates
                tilebuilder.setEntity(determineMonument(i, j));
                // Set river based on coordinates
                tilebuilder.setHasRiver(determineRiver(i, j));
                // Optional artifact
                if (RandomGenerator.chance(SETTINGS.Config.ARTIFACT_CHANCE.getValue())) {
                    tilebuilder.setArtifact(EntityRegistry.getRandomArtifact());
                } else {
                    tilebuilder.setArtifact(null);
                }

                tilebuilder.setResourceValue(RandomGenerator.randomIntInRange(
                        SETTINGS.Config.MIN_RESSOURCE_VALUE.getValue(),
                        SETTINGS.Config.MAX_RESOURCE_VALUE.getValue()
                ));
                tilebuilder.setX(i).setY(j);
                tiles[i][j] = tilebuilder.build();
            }
        }
    }

    /**
     * Returns a new Monument object based on the coordinates of the Tile
     *
     * @param x the x coordinate of the tile
     * @param y the y coordinate of the tile
     * @return the Monument
     */
    private Monument determineMonument(int x, int y) {
        AtomicReference<Monument> monument = new AtomicReference<>();
        EntityRegistry.getAllMonuments().forEach(mon -> {
            if (mon.getTiles().contains(new Monument.Coordinates(x, y))) {
                monument.set(EntityRegistry.getMonument(mon.getId()));
            }
        });
        return monument.get();
    }

    /**
     * Returns a String of the world (name) based on the coordinates of the Tile.
     *
     * @param x the x coordinate of the tile
     * @param y the y coordinate of the tile
     * @return name of the world
     */
    private String determineWorld(int x, int y) {
        if (y >= 0 && y < WORLD_MAP.length && x >= 0 && x < WORLD_MAP[y].length) {
            return WORLD_MAP[y][x];
        }
        return "Unknown";
    }


    /**
     * Determines wethere a tile has a river (true) or not (false)
     *
     * @param x the x coordinate of the tile
     * @param y the y coordinate of the tile
     * @return if tile has a river (true) or not (false)
     */
    private boolean determineRiver(int x, int y) {
        if (x <= 2 && y == 2) return true;
        if (x <= 1 && y == 3) return true;
        return x == 0 && y == 4;
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
            logger.warning(" get Tile By Coordinates Coordinates out of bounds " + x + " " + y);
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
            logger.warning("Set Tile by coordinates Coordinates out of bounds " + x + " " + y);
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
            logger.warning("Set Tile Coordinates out of bounds " + x + " " + y);
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
     * Adds a tile to the board.
     *
     * @param tiles The 2D array of tiles to be added.
     */
    public void setTiles(Tile[][] tiles) {
        this.tiles = tiles;
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
                    value.setEntity(null);
                    value.setOwner(null);
                    value.setArtifact(null); // Some tiles start with an artifact but upon reset they should be null
                    value.setResourceValue(RandomGenerator.randomIntInRange(SETTINGS.Config.MIN_RESSOURCE_VALUE.getValue(), SETTINGS.Config.MAX_RESOURCE_VALUE.getValue()));
                    value.setHasRiver(false);
                    value.getStatus().reset(); // Reset price to default value

                    // If there are other properties to reset, add them here
                    // tiles[i][j].setSomeOtherProperty(defaultValue);
                }
            }
        }
    }

    /**
     * Gets the adjacent tiles for a given tile.
     *
     * @param x The x-coordinate of the tile.
     * @param y The y-coordinate of the tile.
     * @return An array of adjacent tiles.
     */
    public Tile[] getAdjacentTiles(int x, int y) {
        Tile[] adjacentTiles = new Tile[4]; // Up, Down, Left, Right
        int index = 0;

        // Up
        if (x > 0) {
            adjacentTiles[index++] = tiles[x - 1][y];
        }
        // Down
        if (x < tiles.length - 1) {
            adjacentTiles[index++] = tiles[x + 1][y];
        }
        // Left
        if (y > 0) {
            adjacentTiles[index++] = tiles[x][y - 1];
        }
        // Right
        if (y < tiles[0].length - 1) {
            adjacentTiles[index++] = tiles[x][y + 1];
        }

        // Resize the array to fit the number of adjacent tiles found
        Tile[] result = new Tile[index];
        System.arraycopy(adjacentTiles, 0, result, 0, index);
        return result;
    }
}