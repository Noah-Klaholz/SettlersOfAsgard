package ch.unibas.dmi.dbis.cs108.server.core.model;

import ch.unibas.dmi.dbis.cs108.shared.game.Board;
import ch.unibas.dmi.dbis.cs108.shared.game.Tile;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.logging.Logger;

/**
 * Responsible for managing the game board
 */
public class BoardManager {
    private static final Logger LOGGER = Logger.getLogger(BoardManager.class.getName());

    private final ReadWriteLock stateLock;
    private final Board board;

    public BoardManager(ReadWriteLock stateLock) {
        this.stateLock = stateLock;
        this.board = new Board();
    }

    /**
     * Get the game board
     */
    public Board getBoard() {
        return board;
    }

    /**
     * Initialize the board
     */
    public void initializeBoard(int width, int height) {
        stateLock.writeLock().lock();
        try {
            board.initBoard(width, height);
        } finally {
            stateLock.writeLock().unlock();
        }
    }

    /**
     * Get a specific tile on the board
     */
    public Tile getTile(int x, int y) {
        stateLock.readLock().lock();
        try {
            return board.getTileByCoordinates(x, y);
        } finally {
            stateLock.readLock().unlock();
        }
    }

    /**
     * Set a specific tile on the board
     */
    public boolean setTile(int x, int y, Tile tile) {
        stateLock.writeLock().lock();
        try {
            if (tile == null) {
                LOGGER.warning("Cannot set null tile");
                return false;
            }

            board.setTileByCoordinates(x, y, tile);
            return true;
        } finally {
            stateLock.writeLock().unlock();
        }
    }

    /**
     * Reset the board
     */
    public void reset() {
        stateLock.writeLock().lock();
        try {
            board.resetTiles();
        } finally {
            stateLock.writeLock().unlock();
        }
    }

    /**
     * Get adjacent tiles for a specific tile
     *
     * @param x The x-coordinate of the tile
     * @param y The y-coordinate of the tile
     *
     * @return An array of adjacent tiles
     */
    public Tile[] getAdjacentTiles(int x, int y) {
        stateLock.readLock().lock();
        try {
            return board.getAdjacentTiles(x, y);
        } finally {
            stateLock.readLock().unlock();
        }
    }

    /**
     * Gets all tiles that hold a river as a List
     *
     * @return list of tiles
     */
    public List<Tile> getRiverTiles() {
        stateLock.readLock().lock();
        List<Tile> result = new ArrayList<>();
        try {
            for (Tile[] tile : board.getTiles()) {
                for (Tile t : tile) {
                    if (t != null && t.hasRiver()) {
                        result.add(t);
                    }
                }
            }
            return result;
        } finally {
            stateLock.readLock().unlock();
        }
    }
}