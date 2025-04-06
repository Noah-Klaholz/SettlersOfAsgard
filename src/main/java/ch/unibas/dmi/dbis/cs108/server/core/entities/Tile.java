package ch.unibas.dmi.dbis.cs108.server.core.entities;

public class Tile {

    private final int x;
    private final int y;
    private boolean hasStructure;
    private String owner; //ownerID
    private final int price;
    private Structure structure;
    private Statue statue;
    private Artefact artefact;
    private ActiveTrap trap;
    private final String world;
    private boolean purchased;
    private int resourceValue; //Runes: bei spezifischen sind es energy: dort vermerkt
    private boolean hasRiver; //has a river = true
    private final int tileID;

    public Tile(TileBuilder builder) {
        this.x = builder.x;
        this.y = builder.y;
        this.hasStructure = builder.hasStructure;
        this.owner = builder.owner;
        this.price = builder.price;
        this.structure = builder.structure;
        this.statue = builder.statue;
        this.artefact = builder.artefact;
        this.trap = builder.trap;
        this.world = builder.world;
        this.purchased = builder.purchased;
        this.resourceValue = builder.resourceValue;
        this.hasRiver = builder.hasRiver;
        this.tileID = builder.tileID;
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

    public Artefact getArtifact() {
        return artefact;
    }

    public void setArtifact(Artefact artefact) {
        this.artefact = artefact;
    }

    public ActiveTrap getTrap() {
        return trap;
    }

    public void setTrap(ActiveTrap trap) {
        this.trap = trap;
    }

    public String getWorld() {
        return world;
    }

    public boolean isPurchased() {
        return purchased;
    }

    public void setPurchased(boolean purchased) {
        this.purchased = purchased;
    }

    public int getResourceValue() {
        return resourceValue;
    }

    public void setResourceValue(int resourceValue) {
        this.resourceValue = resourceValue;
    }

    public boolean hasRiver() {
        return hasRiver;
    }

    public void setHasRiver(boolean hasRiver) {
        this.hasRiver = hasRiver;
    }

    public int getTileID() {
        return tileID;
    }

    @Override
    public String toString() {
        return "Tile{" +
                "id=" + tileID +
                ", purchased=" + purchased +
                ", resourceValue=" + resourceValue +
                '}';
    }

    public static class TileBuilder {
        public boolean purchased;
        public int resourceValue;
        public boolean hasRiver;
        public int tileID;
        private int x;
        private int y;
        private boolean hasStructure;
        private String owner;
        private int price;
        private Structure structure;
        private Statue statue;
        private Artefact artefact;
        private ActiveTrap trap;
        private String world;

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

        public TileBuilder setArtifact(Artefact artefact) {
            this.artefact = artefact;
            return this;
        }

        public TileBuilder setTrap(ActiveTrap trap) {
            this.trap = trap;
            return this;
        }

        public TileBuilder setWorld(String world) {
            this.world = world;
            return this;
        }

        public TileBuilder setPurchased(boolean purchased) {
            this.purchased = purchased;
            return this;
        }

        public TileBuilder setResourceValue(int resourceValue) {
            this.resourceValue = resourceValue;
            return this;
        }

        public TileBuilder setHasRiver(boolean hasRiver) {
            this.hasRiver = hasRiver;
            return this;
        }

        public TileBuilder setTileID(int tileID) {
            this.tileID = tileID;
            return this;
        }

        public Tile build() {
            return new Tile(this);
        }
    }
}