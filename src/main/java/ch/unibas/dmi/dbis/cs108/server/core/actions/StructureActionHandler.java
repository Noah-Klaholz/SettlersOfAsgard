package ch.unibas.dmi.dbis.cs108.server.core.actions;

import ch.unibas.dmi.dbis.cs108.server.core.model.GameState;
import ch.unibas.dmi.dbis.cs108.shared.game.Player;
import ch.unibas.dmi.dbis.cs108.shared.game.Shop;
import ch.unibas.dmi.dbis.cs108.shared.entities.Purchasables.Structure;

import java.util.concurrent.locks.ReadWriteLock;

public class StructureActionHandler {
    private final GameState gameState;
    private final ReadWriteLock gameLock;

    public StructureActionHandler(GameState gameState, ReadWriteLock gameLock) {
        this.gameState = gameState;
        this.gameLock = gameLock;
    }

    public boolean buyStructure(int structureID, String playerName) {
        Player player = gameState.findPlayerByName(playerName);
        Shop shop = player.getShop();
        Structure targetstructure;
        for (Structure s : shop.getBuyableStructures()) {

        }






        return false;
    }

    public boolean placeStructure(int x, int y, int structureID, String playerName) {
        // Implementation will be added later
        return false;
    }

    public boolean useStructure(int x, int y, int structureID, String playerName) {
        // Implementation will be added later
        return false;
    }
}