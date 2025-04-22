package ch.unibas.dmi.dbis.cs108.client.ui.events.game;

import ch.unibas.dmi.dbis.cs108.client.ui.events.UIEvent;
import java.util.List;
import java.util.Map;

/**
 * Indicates that the game has ended, containing information about the winner
 * and final scores.
 */
public class GameEndEvent implements UIEvent {

    private final String winnerName;
    private final List<Map<String, Object>> finalScores;

    /**
     * Constructs a GameEndEvent.
     *
     * @param winnerName  the name of the winner
     * @param finalScores list of player scores/ranks
     */
    public GameEndEvent(String winnerName, List<Map<String, Object>> finalScores) {
        this.winnerName = winnerName;
        this.finalScores = finalScores;
    }

    /**
     * @return the name of the winner
     */
    public String getWinnerName() {
        return winnerName;
    }

    /**
     * @return the list of final scores
     */
    public List<Map<String, Object>> getFinalScores() {
        return finalScores;
    }

    /**
     * @return the event type identifier
     */
    @Override
    public String getType() {
        return "GAME_END";
    }
}
