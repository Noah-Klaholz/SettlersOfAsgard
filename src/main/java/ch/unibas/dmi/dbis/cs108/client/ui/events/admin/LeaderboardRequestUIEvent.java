package ch.unibas.dmi.dbis.cs108.client.ui.events.admin;

import ch.unibas.dmi.dbis.cs108.client.ui.events.UIEvent;

/**
 * UIEvent triggered when a user requests to view the leaderboard.
 */
public class LeaderboardRequestUIEvent implements UIEvent {

    /**
     * {@inheritDoc}
     */
    @Override
    public String getType() {
        return "LEADERBOARD_REQUEST";
    }
}
