package ch.unibas.dmi.dbis.cs108.client.core.events;

import ch.unibas.dmi.dbis.cs108.shared.entities.Player;
import ch.unibas.dmi.dbis.cs108.shared.entities.Tile;
import ch.unibas.dmi.dbis.cs108.shared.entities.Structure;

public class StructurePurchasedEvent implements GameEvent {
    private final Player player;
    private final Tile tile;
    private final Structure structure;

    public StructurePurchasedEvent(Player player, Tile tile, Structure structure) {
        this.player = player;
        this.tile = tile;
        this.structure = structure;
    }

    public Player getPlayer() {
        return player;
    }

    public Tile getTile() {
        return tile;
    }

    public Structure getStructure() {
        return structure;
    }
}
