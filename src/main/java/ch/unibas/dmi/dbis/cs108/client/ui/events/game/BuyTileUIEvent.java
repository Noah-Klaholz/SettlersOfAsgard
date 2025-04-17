package ch.unibas.dmi.dbis.cs108.client.ui.events.game;

import ch.unibas.dmi.dbis.cs108.client.ui.events.UIEvent;

public class BuyTileUIEvent implements UIEvent {
    private final int x;
    private final int y;

    public BuyTileUIEvent(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    @Override
    public String getType() {
        return "BUYTILE";
    }
}