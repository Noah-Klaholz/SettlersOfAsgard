package ch.unibas.dmi.dbis.cs108.server.core.entities.statue;

public class StatueData {
    private String name;
    private String deal;
    private String blessing;
    private String curse;
    private String description;
    private String useType;
    private int id;
    private int cost;

    // Getters and setters
    public String getName() {
        return name;
    }

    public String getDeal() {
        return deal;
    }

    public String getBlessing() {
        return blessing;
    }

    public String getCurse() {
        return curse;
    }

    public String getDescription() {
        return description;
    }

    public String getUseType() {
        return useType;
    }

    public int getId() {
        return id;
    }

    public int getCost() {
        return cost;
    }
}