package ch.unibas.dmi.dbis.cs108.shared.game;

import ch.unibas.dmi.dbis.cs108.shared.entities.Findables.Artifact;
import ch.unibas.dmi.dbis.cs108.shared.entities.Purchasables.PurchasableEntity;

public class Tile {

    private final int x;
    private final int y;
    private boolean hasEntity;
    private String owner; //ownerID
    private final int price;
    private PurchasableEntity entity;
    private Artifact artefact;
    private final String world;
    private boolean purchased;
    private int resourceValue; //Runes: bei spezifischen sind es energy: dort vermerkt
    private boolean hasRiver; //has a river = true
    private final int tileID;
    private Status status;

    public Tile(TileBuilder builder) {
        this.x = builder.x;
        this.y = builder.y;
        this.hasEntity = builder.hasEntity;
        this.owner = builder.owner;
        this.price = builder.price;
        this.entity = builder.hasEntity ? builder.entity : null;
        this.artefact = builder.artefact;
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

    public boolean hasEntity() {
        return this.entity != null;
    }

    public boolean getHasEntity() {
        return hasEntity;
    }

    public void setEntity(PurchasableEntity entity) {
        this.entity = entity;
        this.hasEntity = true;
    }

    public void setBuff(Status.BuffType buffType, int effect) {
    }

    /**
     * Getter for player status
     *
     * @return Status
     */
    public Status getStatus() {
        return status;
    }

    /**
     * Add a new buff or debuff to the player
     *
     * @param buff the buff to add
     * @param value the value of the buff (positive for buff, negative for debuff)
     */
    public void addBuff(Status.BuffType buff, double value) {
        status.buff(buff, value);
    }


    public PurchasableEntity getEntity() {
        return entity;
    }

    public void setHasEntity(boolean hasEntity) {
        this.hasEntity = hasEntity;
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

    public Artifact getArtifact() {
        return artefact;
    }

    public void setArtifact(Artifact artefact) {
        this.artefact = artefact;
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
        private boolean hasEntity;
        private String owner;
        private int price;
        private PurchasableEntity entity;
        private Artifact artefact;
        private String world;

        public TileBuilder setX(int x) {
            this.x = x;
            return this;
        }

        public TileBuilder setY(int y) {
            this.y = y;
            return this;
        }

        public TileBuilder setHasEntity(boolean hasEntity) {
            this.hasEntity = hasEntity;
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

        public TileBuilder setEntity(PurchasableEntity entity) {
            this.hasEntity = true;
            this.entity = entity;
            return this;
        }

        public TileBuilder setArtifact(Artifact artefact) {
            this.artefact = artefact;
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