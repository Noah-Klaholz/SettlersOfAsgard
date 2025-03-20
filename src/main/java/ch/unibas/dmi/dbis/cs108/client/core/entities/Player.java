package ch.unibas.dmi.dbis.cs108.client.core.entities;

public class Player {
    private String id;
    private String name;

    public Player(String id, String name) {
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
