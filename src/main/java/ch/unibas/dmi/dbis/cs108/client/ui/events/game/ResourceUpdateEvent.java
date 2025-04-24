package ch.unibas.dmi.dbis.cs108.client.ui.events.game;

import ch.unibas.dmi.dbis.cs108.client.ui.events.UIEvent;
import java.util.Map;

/**
 * Indicates an update to a player's resources.
 */
public class ResourceUpdateEvent implements UIEvent {

    private final String playerName;
    private final Map<String, Integer> updatedResources;

    /**
     * Constructs a ResourceUpdateEvent.
     *
     * @param playerName       the player whose resources were updated
     * @param updatedResources map of resource names to their new total amounts
     */
    public ResourceUpdateEvent(String playerName, Map<String, Integer> updatedResources) {
        this.playerName = playerName;
        this.updatedResources = updatedResources;
    }

    /**
     * @return the name of the player whose resources were updated
     */
    public String getPlayerName() {
        return playerName;
    }

    /**
     * @return the updated resources map
     */
    public Map<String, Integer> getUpdatedResources() {
        return updatedResources;
    }

    /**
     * @return the event type identifier
     */
    @Override
    public String getType() {
        return "RESOURCE_UPDATE";
    }
}
