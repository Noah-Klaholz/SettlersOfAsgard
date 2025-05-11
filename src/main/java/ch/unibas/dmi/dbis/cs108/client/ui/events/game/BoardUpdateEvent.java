package ch.unibas.dmi.dbis.cs108.client.ui.events.game;

import ch.unibas.dmi.dbis.cs108.client.ui.events.UIEvent;

import java.util.List;
import java.util.Map;

/**
 * Indicates that one or more elements on the game board have changed,
 * such as tile ownership, structure placement/upgrade, or artifact placement.
 */
public class BoardUpdateEvent implements UIEvent {

    /**
     * A list of updates, where each update is represented as a map.
     * The map contains key-value pairs representing the details of the update.
     */
    private final List<Map<String, Object>> updates;

    /**
     * Constructs a BoardUpdateEvent.
     *
     * @param updates a list of updates, each represented as a map
     */
    public BoardUpdateEvent(List<Map<String, Object>> updates) {
        this.updates = updates;
    }

    /**
     * @return the list of updates
     */
    public List<Map<String, Object>> getUpdates() {
        return updates;
    }

    /**
     * @return the event type identifier
     */
    @Override
    public String getType() {
        return "BOARD_UPDATE";
    }
}
