package ch.unibas.dmi.dbis.cs108.client.core.events;

import ch.unibas.dmi.dbis.cs108.client.core.entities.Player;
import ch.unibas.dmi.dbis.cs108.client.core.entities.Structure;

public class StructureUpgradedEvent implements GameEvent {
    private final Player player;
    private final Structure structure;
    private final int previousLevel;
    private final int newLevel;

    public StructureUpgradedEvent(Player player, Structure structure, int previousLevel, int newLevel) {
        this.player = player;
        this.structure = structure;
        this.previousLevel = previousLevel;
        this.newLevel = newLevel;
    }

    public Player getPlayer() {
        return player;
    }

    public Structure getStructure() {
        return structure;
    }

    public int getPreviousLevel() {
        return previousLevel;
    }

    public int getNewLevel() {
        return newLevel;
    }
}

