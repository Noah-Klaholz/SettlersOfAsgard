package ch.unibas.dmi.dbis.cs108.client.ui.events.admin;

import ch.unibas.dmi.dbis.cs108.client.ui.events.UIEvent;
import java.util.List;
import java.util.Map;

/**
 * UIEvent triggered when leaderboard data is received from the server.
 */
public class LeaderboardResponseUIEvent implements UIEvent {
    private final List<Map<String, Object>> leaderboardEntries;

    /**
     * Constructs a new LeaderboardResponseUIEvent.
     *
     * @param leaderboardEntries A list of maps containing player data (name, score,
     *                           rank, etc.)
     */
    public LeaderboardResponseUIEvent(List<Map<String, Object>> leaderboardEntries) {
        this.leaderboardEntries = leaderboardEntries;
    }

    /**
     * Gets the leaderboard entries.
     *
     * @return A list of maps containing player data.
     */
    public List<Map<String, Object>> getLeaderboardEntries() {
        return leaderboardEntries;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getType() {
        return "LEADERBOARD_RESPONSE";
    }
}
