package ch.unibas.dmi.dbis.cs108.server.core.model;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * This class manages a Leaderboard. It handles loading, updating, saving and sending
 * of the leaderboard.txt file (@see leaderboard/leaderboard.txt)
 */
public class Leaderboard {
    /**
     * Logger to log logging
     */
    private static final Logger LOGGER = Logger.getLogger(Leaderboard.class.getName());
    /**
     * Map for the leaderboard (name -> points (runes)
     */
    private final Map<String, Integer> leaderboard = new ConcurrentHashMap<>();
    /**
     * The path to the leaderboard
     */
    private final Path leaderboardPath;
    /**
     * ReadWriteLock to ensure thread-safe handling
     */
    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    /**
     * Instantiates a Leaderboard object. Creates the correct path.
     */
    public Leaderboard() {
        // Try to use the Git-tracked leaderboard first, then fall back to user home
        Path gitTrackedPath = tryGetGitTrackedLeaderboardPath();
        if (gitTrackedPath != null && Files.exists(gitTrackedPath)) {
            this.leaderboardPath = gitTrackedPath;
        } else {
            this.leaderboardPath = createUserHomeLeaderboardPath();
        }
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
     * Tries to get the Git-tracked leaderboard path.
     *
     * @return the path to the Git-tracked leaderboard, or null if not found
     */
    private static Path tryGetGitTrackedLeaderboardPath() {
        try {
            // First try relative to current directory (where JAR is run)
            Path projectRelative = Paths.get("../../leaderboard", "leaderboard.txt");
            if (Files.exists(projectRelative)) {
                return projectRelative;
            }
            return null;
        } catch (Exception e) {
            LOGGER.log(Level.INFO, "Could not locate Git-tracked leaderboard", e);
            return null;
        }
    }

    /**
     * Creates the path to the leaderboard in the user's home directory.
     *
     * @return the path to the leaderboard file
     */
    private static Path createUserHomeLeaderboardPath() {
        String userHome = System.getProperty("user.home");
        Path appDataPath = Paths.get(userHome, ".settlersOfAsgard");
        return appDataPath.resolve("leaderboard.txt");
    }

    /**
     * Creates a Leaderboard object from its string representation.
     *
     * @param leaderboardString the string representation of a leaderboard
     * @return a new Leaderboard object populated with the parsed data
     */
    public static Leaderboard fromString(String leaderboardString) {
        Leaderboard leaderboard = new Leaderboard();

        // Remove the curly braces
        String content = leaderboardString.substring(1, leaderboardString.length() - 1);

        // Skip if empty
        if (content.trim().isEmpty()) {
            return leaderboard;
        }

        // Split by comma and space
        String[] entries = content.split(", ");

        for (String entry : entries) {
            String[] parts = entry.split("=");
            if (parts.length == 2) {
                String playerName = parts[0];
                try {
                    int points = Integer.parseInt(parts[1]);
                    leaderboard.update(playerName, points);
                } catch (NumberFormatException e) {
                    LOGGER.log(Level.WARNING, "Failed to parse points for player: " + playerName, e);
                }
            }
        }

        return leaderboard;
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
     * @param points     the points of the player
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
     * @param entry  the entry of the Map to write
     */
    private void writeEntry(BufferedWriter writer, Map.Entry<String, Integer> entry) {
        try {
            writer.write(entry.getKey() + ": " + entry.getValue() + "\n");
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to write leaderboard entry", e);
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
    public String toString() {
        lock.readLock().lock();
        try {
            return getLeaderboard().toString();
        } finally {
            lock.readLock().unlock();
        }
    }
}