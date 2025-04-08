package ch.unibas.dmi.dbis.cs108.shared.entities.entities;

public class Statue {

    private final int statueID;
    private final String name;
    private final String description;
    private final String useType;
    private final int price;
    private final int upgradePrice;
    private int level;

    public Statue(int statueID, String name, String description, String useType) {
        this.statueID = statueID;
        this.name = name;
        this.description = description;
        this.useType = useType;
        this.price = 200;
        this.upgradePrice = 100;
        this.level = 1; // Default level
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

    public int getUpgradePrice() {
        return upgradePrice;
    }

    public int getPrice() {
        return price;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public boolean upgrade() {
        if (level < 3) { // max level is 3
            level++;
            return true;
        } else {
            System.out.println("Statue is already at max level.");
        }
        return false;
    }

    public void use() {
        //todo: later implement: these are all different per statue
    }

    //todo: later implement: instead of use -> useDeal and useBlessinglvlcheck
}