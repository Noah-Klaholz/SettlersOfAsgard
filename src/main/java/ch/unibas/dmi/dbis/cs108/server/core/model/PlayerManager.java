package ch.unibas.dmi.dbis.cs108.server.core.model;

import ch.unibas.dmi.dbis.cs108.shared.entities.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.logging.Logger;

/**
 * Responsible for managing players in the game
 */
public class PlayerManager {
    private static final Logger LOGGER = Logger.getLogger(PlayerManager.class.getName());

    private final ReadWriteLock stateLock;
    private final StateObserverManager observerManager;
    private final List<Player> players;

    public PlayerManager(ReadWriteLock stateLock, StateObserverManager observerManager) {
        this.stateLock = stateLock;
        this.observerManager = observerManager;
        this.players = new ArrayList<>();
    }

    /**
     * Get an unmodifiable list of players
     */
    public List<Player> getPlayers() {
        stateLock.readLock().lock();
        try {
            return Collections.unmodifiableList(players);
        } finally {
            stateLock.readLock().unlock();
        }
    }

    /**
     * Set the players list
     */
    public void setPlayers(String[] playerNames) {
        stateLock.writeLock().lock();
        try {
            if (playerNames == null) {
                LOGGER.warning("No players provided");
                return;
            }

            if (playerNames.length >= 9) {
                LOGGER.warning("Too many players: " + playerNames.length);
                return;
            }

            players.clear();
            for (String name : playerNames) {
                players.add(new Player(name));
            }

            observerManager.notifyObservers(null);
        } finally {
            stateLock.writeLock().unlock();
        }
    }

    /**
     * Add a player to the game
     */
    public void addPlayer(String playerName) {
        stateLock.writeLock().lock();
        try {
            if (players.size() >= 8) {
                LOGGER.warning("Maximum players reached");
                return;
            }

            players.add(new Player(playerName));
            observerManager.notifyObservers(null);
        } finally {
            stateLock.writeLock().unlock();
        }
    }

    /**
     * Find player by name
     */
    public Player findPlayerByName(String name) {
        stateLock.readLock().lock();
        try {
            return players.stream()
                    .filter(p -> p.getName().equals(name))
                    .findFirst()
                    .orElse(null);
        } finally {
            stateLock.readLock().unlock();
        }
    }

    /**
     * Set runes for a specific player
     */
    public boolean setRunes(int runes, String playerName) {
        stateLock.writeLock().lock();
        try {
            if (runes < 0) {
                LOGGER.warning("Cannot set negative runes");
                return false;
            }

            Player player = findPlayerByName(playerName);
            if (player == null) {
                return false;
            }

            player.setRunes(runes);
            observerManager.notifyObservers(null);
            return true;
        } finally {
            stateLock.writeLock().unlock();
        }
    }

    /**
     * Add runes to a player's collection
     */
    public boolean addRunes(int runes, String playerName) {
        stateLock.writeLock().lock();
        try {
            if (runes < 0) {
                LOGGER.warning("Cannot add negative runes");
                return false;
            }

            Player player = findPlayerByName(playerName);
            if (player == null) {
                return false;
            }

            player.addRunes(runes);
            observerManager.notifyObservers(null);
            return true;
        } finally {
            stateLock.writeLock().unlock();
        }
    }

    /**
     * Remove runes from a player's collection
     */
    public boolean removeRunes(int runes, String playerName) {
        stateLock.writeLock().lock();
        try {
            if (runes < 0) {
                LOGGER.warning("Cannot remove negative runes");
                return false;
            }

            Player player = findPlayerByName(playerName);
            if (player == null) {
                return false;
            }

            int currentRunes = player.getRunes();
            if (currentRunes < runes) {
                player.setRunes(0);
            } else {
                player.removeRunes(runes);
            }

            observerManager.notifyObservers(null);
            return true;
        } finally {
            stateLock.writeLock().unlock();
        }
    }

    /**
     * Reset all players
     */
    public void reset() {
        stateLock.writeLock().lock();
        try {
            for (Player player : players) {
                player.reset();
            }
        } finally {
            stateLock.writeLock().unlock();
        }
    }
}