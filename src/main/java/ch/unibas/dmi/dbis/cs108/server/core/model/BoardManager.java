package ch.unibas.dmi.dbis.cs108.server.core.model;

import ch.unibas.dmi.dbis.cs108.shared.entities.Board;
import ch.unibas.dmi.dbis.cs108.shared.entities.Tile;

import java.util.concurrent.locks.ReadWriteLock;
import java.util.logging.Logger;

/**
 * Responsible for managing the game board
 */
public class BoardManager {
    private static final Logger LOGGER = Logger.getLogger(BoardManager.class.getName());

    private final ReadWriteLock stateLock;
    private final StateObserverManager observerManager;
    private final Board board;

    public BoardManager(ReadWriteLock stateLock, StateObserverManager observerManager) {
        this.stateLock = stateLock;
        this.observerManager = observerManager;
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
            // Create and set tiles with default values
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    Tile.TileBuilder tilebuilder = new Tile.TileBuilder();
                    tilebuilder.setX(x);
                    tilebuilder.setY(y);
                    Tile newTile = new Tile(tilebuilder);
                    board.setTileByCoordinates(x, y, newTile);
                }
            }
            observerManager.notifyObservers(null);
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
            observerManager.notifyObservers(null);
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
}