package ch.unibas.dmi.dbis.cs108.client.ui.events.game;

import ch.unibas.dmi.dbis.cs108.client.ui.events.UIEvent;

public class PlaceStructureUIEvent implements UIEvent {
    private final int x;
    private final int y;
    private final int structureId;

    public PlaceStructureUIEvent(int x, int y, int structureId) {
        this.x = x;
        this.y = y;
        this.structureId = structureId;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getStructureId() {
        return structureId;
    }

    @Override
    public String getType() {
        return "PLACESTRUCTURE";
    }
}