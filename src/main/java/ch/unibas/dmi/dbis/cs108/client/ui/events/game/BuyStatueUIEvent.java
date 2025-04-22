package ch.unibas.dmi.dbis.cs108.client.ui.events.game;

import ch.unibas.dmi.dbis.cs108.client.ui.events.UIEvent;

public class BuyStatueUIEvent implements UIEvent {
    private final int statueId;

    public BuyStatueUIEvent(int statueId) {
        this.statueId = statueId;
    }

    public int getStatueId() {
        return statueId;
    }

    @Override
    public String getType() {
        return "BUY_STATUE";
    }
}
