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

public class Leaderboard {
    private static final Logger LOGGER = Logger.getLogger(Leaderboard.class.getName());

    private final Map<String, Integer> leaderboard = new ConcurrentHashMap<>();
    private final Path leaderboardPath;
    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    public Leaderboard() {
        this(Paths.get("leaderboard", "Leaderboard.txt"));
    }

    public Leaderboard(Path customPath) {
        this.leaderboardPath = customPath.toAbsolutePath();
        ensureDirectoryExists();
        load();
    }

    private void ensureDirectoryExists() {
        try {
            Files.createDirectories(leaderboardPath.getParent());
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Could not create leaderboard directory", e);
        }
    }

    public void update(String playerName, int points) {
        lock.writeLock().lock();
        try {
            leaderboard.merge(playerName, points, Integer::sum);
            save();
        } finally {
            lock.writeLock().unlock();
        }
    }

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

    private void writeEntry(BufferedWriter writer, Map.Entry<String, Integer> entry) {
        try {
            writer.write(entry.getKey() + ": " + entry.getValue() + "\n");
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Failed to write leaderboard entry", e);
        }
    }

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
}