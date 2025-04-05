package ch.unibas.dmi.dbis.cs108.client.core.rules;
import ch.unibas.dmi.dbis.cs108.client.core.entities.Player;
import ch.unibas.dmi.dbis.cs108.client.core.rules.Rule;
import ch.unibas.dmi.dbis.cs108.client.core.state.GameState;

public class ResourceGenerationRule implements Rule {
    @Override
    public boolean isApplicable(GameState gameState) {
        // For demo, always applicable at the end of a round.
        return true;
    }

    @Override
    public void apply(GameState gameState) {
        for (Player player : gameState.getPlayers()) {
            // Generate runes based on the number of fields owned.
            int generatedRunes = player.getOwnedTiles().size() * 5;
            player.addRunes(generatedRunes);
            System.out.println(player.getName() + " generated " + generatedRunes + " runes.");
        }
    }
}