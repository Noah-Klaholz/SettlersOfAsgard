package ch.unibas.dmi.dbis.cs108.client.core.entities;

public class Statue {

    private final int statueID;
    private final String name;
    private final String description;
    private final String useType;
    private final int price;
    private final int upGradePrice; //todo: later have 2 different prices for lvl 2 and 3

    public Statue(int statueID, String name, String description, String useType, int price, int upGradePrice) {
        this.statueID = statueID;
        this.name = name;
        this.description = description;
        this.useType = useType;
        this.price = price;
        this.upGradePrice = upGradePrice;
    }

    public int getPrice() {
        return price;
    }

    public int getUpgradePrice() {
        return upGradePrice;
    }

    public int getStatueID() {
        return statueID;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getUseType() {
        return useType;
    }
}
