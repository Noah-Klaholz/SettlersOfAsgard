package ch.unibas.dmi.dbis.cs108.server.core.entities.statue;

public abstract class Statue {
    private final String name;
    private final String deal;
    private final String blessing;
    private final String curse;
    private final String description;
    private final String useType;
    private final int id;
    private final int cost;

    public Statue(StatueData data) {
        this.name = data.getName();
        this.deal = data.getDeal();
        this.blessing = data.getBlessing();
        this.curse = data.getCurse();
        this.description = data.getDescription();
        this.useType = data.getUseType();
        this.id = data.getId();
        this.cost = data.getCost();
    }
}
