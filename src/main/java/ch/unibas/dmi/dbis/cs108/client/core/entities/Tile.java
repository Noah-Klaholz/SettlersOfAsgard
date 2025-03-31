package ch.unibas.dmi.dbis.cs108.client.core.entities;

public class Tile {

    private final int x;
    private final int y;
    private boolean hasStructure;
    private String owner;
    private final int price;
    private Structure structure;
    private Statue statue;
    private Artifact artifact;
    private ActiveTrap trap;

    private Tile(TileBuilder builder) {
        this.x = builder.x;
        this.y = builder.y;
        this.hasStructure = builder.hasStructure;
        this.owner = builder.owner;
        this.price = builder.price;
        this.structure = builder.structure;
        this.statue = builder.statue;
        this.artifact = builder.artifact;
        this.trap = builder.trap;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public boolean getHasStructure() {
        return hasStructure;
    }

    public void setHasStructure(boolean hasStructure) {
        this.hasStructure = hasStructure;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public int getPrice() {
        return price;
    }

    public Structure getStructure() {
        return structure;
    }

    public void setStructure(Structure structure) {
        this.structure = structure;
    }

    public Statue getStatue() {
        return statue;
    }

    public void setStatue(Statue statue) {
        this.statue = statue;
    }

    public Artifact getArtifact() {
        return artifact;
    }

    public void setArtifact(Artifact artifact) {
        this.artifact = artifact;
    }

    public ActiveTrap getTrap() {
        return trap;
    }

    public void setTrap(ActiveTrap trap) {
        this.trap = trap;
    }

    public static class TileBuilder {
        private int x;
        private int y;
        private boolean hasStructure;
        private String owner;
        private int price;
        private Structure structure;
        private Statue statue;
        private Artifact artifact;
        private ActiveTrap trap;

        public TileBuilder setX(int x) {
            this.x = x;
            return this;
        }

        public TileBuilder setY(int y) {
            this.y = y;
            return this;
        }

        public TileBuilder setHasStructure(boolean hasStructure) {
            this.hasStructure = hasStructure;
            return this;
        }

        public TileBuilder setOwner(String owner) {
            this.owner = owner;
            return this;
        }

        public TileBuilder setPrice(int price) {
            this.price = price;
            return this;
        }

        public TileBuilder setStructure(Structure structure) {
            this.structure = structure;
            return this;
        }

        public TileBuilder setStatue(Statue statue) {
            this.statue = statue;
            return this;
        }

        public TileBuilder setArtifact(Artifact artifact) {
            this.artifact = artifact;
            return this;
        }

        public TileBuilder setTrap(ActiveTrap trap) {
            this.trap = trap;
            return this;
        }

        public Tile build() {
            return new Tile(this);
        }
    }
}