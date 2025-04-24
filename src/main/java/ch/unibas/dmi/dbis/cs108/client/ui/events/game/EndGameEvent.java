package ch.unibas.dmi.dbis.cs108.client.ui.events.game;

import ch.unibas.dmi.dbis.cs108.client.ui.events.UIEvent;

import java.time.Instant;
import java.util.Arrays;
import java.util.HashMap;

/**
 * Represents an event sent from the server to the client when the game ends.
 */
public class EndGameEvent implements UIEvent {
    /**
     * The leaderboard of the game.
     */
    private final HashMap<String, Integer> leaderboard;

    public EndGameEvent(HashMap<String, Integer> leaderboard) {
        this.leaderboard = leaderboard;
    }

    /**
     * Getter for the leaderboard of the event.
     *
     * @return The leaderboard of the event.
     */
    public HashMap<String, Integer> getLeaderboard() {
        return leaderboard;
    }

    /**
     * Returns the event type string.
     *
     * @return event type string
     */
    @Override
    public String getType() {
        return "";
    }
}
