package ch.unibas.dmi.dbis.cs108.client.ui.events.game;

import ch.unibas.dmi.dbis.cs108.client.ui.events.UIEvent;

public class UseStructureUIEvent implements UIEvent {
    private final int x;
    private final int y;
    private final int structureId;
    private final String useType;

    public UseStructureUIEvent(int x, int y, int structureId, String useType) {
        this.x = x;
        this.y = y;
        this.structureId = structureId;
        this.useType = useType;
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

    public String getUseType() {
        return useType;
    }

    @Override
    public String getType() {
        return "USESTRUCTURE";
    }
}