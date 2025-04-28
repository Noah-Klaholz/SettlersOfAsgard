package ch.unibas.dmi.dbis.cs108.server.core.model;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.*;
import java.util.logging.*;
import java.util.stream.Collectors;

/**
 * This class manages a Leaderboard. It handles loading, updating, saving and sending
 * of the leaderboard.txt file (@see leaderboard/leaderboard.txt)
 */
public class Leaderboard {
    /** Logger to log logging */
    private static final Logger LOGGER = Logger.getLogger(Leaderboard.class.getName());
    /** Map for the leaderboard (name -> points (runes) */
    private final Map<String, Integer> leaderboard = new ConcurrentHashMap<>();
    /** The path to the leaderboard */
    private final Path leaderboardPath;
    /** ReadWriteLock to ensure thread-safe handling */
    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    /**
     * Instantiates a Leaderboard object. Initializes the Path field from the current file.
     * Ensures the directory exists and loads the file.
     */
    public Leaderboard() {
        this(Paths.get("leaderboard", "Leaderboard.txt").toAbsolutePath());
        ensureDirectoryExists();
        load();
    }

    /**
     * Instantiates a Leaderboard object. Initializes the Path field from the parameter.
     * Ensures the directory exists and loads the file.
     *
     * @param customPath the path to the file
     */
    public Leaderboard(Path customPath) {
        this.leaderboardPath = customPath;
        ensureDirectoryExists();
        load();
    }

    /**
     * Ensures the leaderboard directory exists.
     */
    private void ensureDirectoryExists() {
        try {
            Files.createDirectories(leaderboardPath.getParent());
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Could not create leaderboard directory", e);
        }
    }

    /**
     * Updates the leaderboard by adding a key and value to the map.
     *
     * @param playerName the name of the player
     * @param points the points of the player
     */
    public void update(String playerName, int points) {
        lock.writeLock().lock();
        try {
            leaderboard.merge(playerName, points, Integer::sum);
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Saves the current leaderboard.
     */
    public void save() {
        lock.readLock().lock();
        try (BufferedWriter writer = Files.newBufferedWriter(leaderboardPath,
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {

            leaderboard.entrySet().stream()
                    .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                    .forEach(entry -> writeEntry(writer, entry));

        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Could not save leaderboard", e);
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Writes an entry of the map to the BufferedWriter. Helper Method for save().
     *
     * @param writer the BufferedWriter to write to
     * @param entry the entry of the Map to write
     */
    private void writeEntry(BufferedWriter writer, Map.Entry<String, Integer> entry) {
        try {
            writer.write(entry.getKey() + ": " + entry.getValue() + "\n");
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Failed to write leaderboard entry", e);
        }
    }

    /**
     * Loads the leaderboard map from the file.
     */
    public void load() {
        lock.writeLock().lock();
        try {
            if (!Files.exists(leaderboardPath)) return;

            try (BufferedReader reader = Files.newBufferedReader(leaderboardPath)) {
                reader.lines()
                        .map(line -> line.split(": "))
                        .filter(parts -> parts.length == 2)
                        .forEach(parts -> leaderboard.put(parts[0], Integer.parseInt(parts[1])));
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Could not load leaderboard", e);
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Gets the leaderboard map (sorted from highest to lowest).
     *
     * @return the leaderboard.
     */
    public Map<String, Integer> getLeaderboard() {
        lock.readLock().lock();
        try {
            return leaderboard.entrySet().stream()
                    .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                    .collect(Collectors.toMap(
                            Map.Entry::getKey,
                            Map.Entry::getValue,
                            (e1, e2) -> e1,
                            LinkedHashMap::new
                    ));
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Returns the leaderboard as a String. Needed to send it to clients.
     *
     * @return the leaderboard as a String.
     */
    @Override
    public String toString(){
        lock.readLock().lock();
        try {
            return getLeaderboard().toString();
        } finally {
            lock.readLock().unlock();
        }
    }
}