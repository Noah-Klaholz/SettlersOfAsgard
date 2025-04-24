package ch.unibas.dmi.dbis.cs108.client.networking.events;

import java.time.Instant;
import java.util.Arrays;
import java.util.HashMap;

/**
 * Represents an event sent from the server to the client when the game ends.
 */
public class EndGameEvent implements Event{
    /**
     * The timestamp of the event.
     */
    private final Instant timestamp = Instant.now();
    /**
     * The leaderboard of the game.
     */
    private final HashMap<String, Integer> leaderboard;

    public EndGameEvent(String leaderboardStr) {
        this.leaderboard = new HashMap<>();

        // Handle empty or null input
        if (leaderboardStr == null || leaderboardStr.isEmpty()) {
            return;
        }

        // Parse the leaderboard string: name$runes$name$runes$...
        String[] parts = leaderboardStr.split("\\$");

        // Process pairs of player name and score
        for (int i = 0; i < parts.length - 1; i += 2) {
            String playerName = parts[i];
            try {
                int score = Integer.parseInt(parts[i + 1]);
                leaderboard.put(playerName, score);
            } catch (NumberFormatException e) {
                // Skip invalid entries
            }
        }
    }

    /**
     * Getter for the timestamp of the event.
     *
     * @return The timestamp of the event.
     */
    @Override
    public Instant getTimestamp() {
        return timestamp;
    }

    /**
     * Getter for the leaderboard of the event.
     *
     * @return The leaderboard of the event.
     */
    public HashMap<String, Integer> getLeaderboard() {
        return leaderboard;
    }
}
