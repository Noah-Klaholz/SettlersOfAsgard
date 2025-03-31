package ch.unibas.dmi.dbis.cs108.client.core.entities;

public class ActiveTrap {
    //a trap SET by a player - not a findable one: that would be an artifact
    private final int trapID;
    private final int lostRunes;

    public ActiveTrap(int trapID, int lostRunes) {
        this.trapID = trapID;
        this.lostRunes = lostRunes;
    }
}
