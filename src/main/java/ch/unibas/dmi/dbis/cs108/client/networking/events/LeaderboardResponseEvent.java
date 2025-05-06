package ch.unibas.dmi.dbis.cs108.client.networking.events;

import ch.unibas.dmi.dbis.cs108.server.core.model.Leaderboard;

import java.time.Instant;

/**
 * LeaderboardResponseEvent is an event that represents a response from the
 * leaderboard service.
 */
public class LeaderboardResponseEvent implements Event {
    /**
     * The leaderboard object
     */
    private final Leaderboard leaderboard;

    /**
     * Constructor for LeaderboardResponseEvent.
     * Initializes the event with the given parameters.
     */
    public LeaderboardResponseEvent(String leaderboard) {
        this.leaderboard = Leaderboard.fromString(leaderboard);
    }

    /**
     * Getter for the timestamp of the event.
     *
     * @return The timestamp of the event.
     */
    @Override
    public Instant getTimestamp() {
        return null;
    }

    /**
     * Getter for the leaderboard.
     *
     * @return The leaderboard object.
     */
    public Leaderboard getLeaderboard() {
        return leaderboard;
    }
}
