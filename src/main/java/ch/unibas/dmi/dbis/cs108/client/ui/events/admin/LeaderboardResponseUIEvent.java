package ch.unibas.dmi.dbis.cs108.client.ui.events.admin;

import ch.unibas.dmi.dbis.cs108.client.ui.events.UIEvent;
import ch.unibas.dmi.dbis.cs108.server.core.model.Leaderboard;

/**
 * UIEvent triggered when leaderboard data is received from the server.
 */
public class LeaderboardResponseUIEvent implements UIEvent {
    /**
     * The leaderboard data received from the server.
     */
    private final Leaderboard leaderboard;

    /**
     * Constructor for LeaderboardResponseUIEvent.
     *
     * @param leaderboard The leaderboard data received from the server.
     */
    public LeaderboardResponseUIEvent(Leaderboard leaderboard) {
        this.leaderboard = leaderboard;
    }

    /**
     * Gets the leaderboard entries.
     *
     * @return A list of maps containing player data.
     */
    public Leaderboard getLeaderboard() {
        return leaderboard;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getType() {
        return "LEADERBOARD_RESPONSE";
    }
}
