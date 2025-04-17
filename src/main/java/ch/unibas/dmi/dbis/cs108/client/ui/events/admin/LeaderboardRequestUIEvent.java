package ch.unibas.dmi.dbis.cs108.client.ui.events.admin;

import ch.unibas.dmi.dbis.cs108.client.ui.events.UIEvent;

/**
 * UIEvent triggered when a user requests to view the leaderboard.
 * This event is published by the UI and handled by the CommunicationMediator.
 */
public class LeaderboardRequestUIEvent implements UIEvent {
    
    /**
     * Gets the type of this event.
     *
     * @return The event type as a string.
     */
    @Override
    public String getType() {
        return "LEADERBOARD_REQUEST";
    }
}