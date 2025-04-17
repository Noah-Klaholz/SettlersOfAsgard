package ch.unibas.dmi.dbis.cs108.client.ui.events.game;

import ch.unibas.dmi.dbis.cs108.client.ui.events.UIEvent;

public class UseStatueUIEvent implements UIEvent {
    private final int x;
    private final int y;
    private final int statueId;
    private final String useType;

    public UseStatueUIEvent(int x, int y, int statueId, String useType) {
        this.x = x;
        this.y = y;
        this.statueId = statueId;
        this.useType = useType;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getStatueId() {
        return statueId;
    }

    public String getUseType() {
        return useType;
    }

    @Override
    public String getType() {
        return "USESTATUE";
    }
}