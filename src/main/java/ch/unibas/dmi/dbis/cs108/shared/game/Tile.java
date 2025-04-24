package ch.unibas.dmi.dbis.cs108.shared.game;

import ch.unibas.dmi.dbis.cs108.client.core.Game;
import ch.unibas.dmi.dbis.cs108.shared.entities.Findables.Artifact;
import ch.unibas.dmi.dbis.cs108.shared.entities.Findables.Monument;
import ch.unibas.dmi.dbis.cs108.shared.entities.GameEntity;
import ch.unibas.dmi.dbis.cs108.shared.entities.Purchasables.PurchasableEntity;

/**
 * Represents a tile on the game board.
 * Each tile can contain an entity, an artifact, and has various properties
 * such as ownership, price, and resource value.
 */
public class Tile {

    private final int x;
    private final int y;
    private boolean hasEntity;
    private String owner; //ownerID
    private final int price;
    private GameEntity entity; // Not an artifact
    private Artifact artefact;
    private final String world;
    private boolean purchased;
    private int resourceValue; //Runes: bei spezifischen sind es energy: dort vermerkt
    private boolean hasRiver; //has a river = true
    private final int tileID;
    private Status status;

    /**
     * Constructor for Tile.
     *
     * @param builder The TileBuilder object containing the parameters for this tile
     */
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
        status = new Status();
    }

    /**
     * getter for x coordinate
     */
    public int getX() {
        return x;
    }

    /**
     * getter for y coordinate
     */
    public int getY() {
        return y;
    }

    /**
     * Check if the tile has an entity
     *
     * @return true if the tile has an entity, false otherwise
     */
    public boolean hasEntity() {
        return this.entity != null;
    }

    /**
     * getter for hasEntity
     *
     * @return true if the tile has an entity, false otherwise
     */
    public boolean getHasEntity() {
        return hasEntity;
    }

    /**
     * setter for hasEntity
     *
     * @param entity the entity to set
     */
    public void setEntity(GameEntity entity) {
        this.entity = entity;
        this.hasEntity = true;
    }

    /**
     * setter for buff
     *
     * @param buffType the type of buff to set
     * @param effect the effect of the buff
     */
    public void setBuff(Status.BuffType buffType, int effect) {
        status.buff(buffType, effect);
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


    /**
     * getter for the entity
     * @return
     */
    public GameEntity getEntity() {
        return entity;
    }

    /**
     * setter for hasEntity
     * @param hasEntity
     */
    public void setHasEntity(boolean hasEntity) {
        this.hasEntity = hasEntity;
    }

    /**
     * getter for the owner
     * @return
     */
    public String getOwner() {
        return owner;
    }

    /**
     * setter for the owner
     * @param owner
     */
    public void setOwner(String owner) {
        this.owner = owner;
    }

    /**
     * getter for the price
     * @return
     */
    public int getPrice() {
        return price;
    }

    /**
     * getter for the artefact
     * @return
     */
    public Artifact getArtifact() {
        return artefact;
    }

    /**
     * setter for the artefact
     * @param artefact
     */
    public void setArtifact(Artifact artefact) {
        this.artefact = artefact;
    }

    /**
     * getter for the world
     * @return
     */
    public String getWorld() {
        return world;
    }

    /**
     * getter for the purchased status
     * @return
     */
    public boolean isPurchased() {
        return purchased;
    }

    /**
     * setter for the purchased status
     * @param purchased
     */
    public void setPurchased(boolean purchased) {
        this.purchased = purchased;
    }

    /**
     * getter for the resource value
     * @return
     */
    public int getResourceValue() {
        return resourceValue;
    }

    /**
     * setter for the resource value
     * @param resourceValue
     */
    public void setResourceValue(int resourceValue) {
        this.resourceValue = resourceValue;
    }

    /**
     * getter for the hasRiver status
     * @return
     */
    public boolean hasRiver() {
        return hasRiver;
    }

    /**
     * setter for the hasRiver status
     * @param hasRiver
     */
    public void setHasRiver(boolean hasRiver) {
        this.hasRiver = hasRiver;
    }

    /**
     * getter for the tileID
     * @return
     */
    public int getTileID() {
        return tileID;
    }

    /**
     * toString method for the Tile class
     *
     * @return A string representation of the Tile object
     */
    @Override
    public String toString() {
        return "Tile{" +
                "id=" + tileID +
                ", purchased=" + purchased +
                ", resourceValue=" + resourceValue +
                '}';
    }

    /**
     * Removes the entity from the tile and returns it.
     * This method sets the entity to null and updates the hasEntity flag.
     *
     * @return The removed entity, or null if no entity was present.
     */
    public GameEntity removeEntity() {
        GameEntity entity = this.entity;
        this.entity = null;
        this.hasEntity = false;
        return entity;
    }

    /**
     * Builder class for creating Tile instances.
     */
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
        private GameEntity entity;
        private Artifact artefact;
        private String world;

        /**
         * Constructor for TileBuilder.
         *
         * @param x The x-coordinate of the tile
         */
        public TileBuilder setX(int x) {
            this.x = x;
            return this;
        }

        /**
         * setter for y
         *
         * @param y The y-coordinate of the tile
         */
        public TileBuilder setY(int y) {
            this.y = y;
            return this;
        }

        /**
         * setter for hasEntity
         *
         * @param hasEntity true if the tile has an entity, false otherwise
         */
        public TileBuilder setHasEntity(boolean hasEntity) {
            this.hasEntity = hasEntity;
            return this;
        }

        /**
         * setter for owner
         *
         * @param owner The owner of the tile
         */
        public TileBuilder setOwner(String owner) {
            this.owner = owner;
            return this;
        }

        /**
         * setter for price
         *
         * @param price The price of the tile
         */
        public TileBuilder setPrice(int price) {
            this.price = price;
            return this;
        }

        /**
         * setter for entity
         *
         * @param entity The entity on the tile
         */
        public TileBuilder setEntity(GameEntity entity) {
            this.hasEntity = true;
            this.entity = entity;
            return this;
        }

        /**
         * setter for artefact
         *
         * @param artefact The artefact on the tile
         */
        public TileBuilder setArtifact(Artifact artefact) {
            this.artefact = artefact;
            return this;
        }

        /**
         * setter for world
         *
         * @param world The world of the tile
         */
        public TileBuilder setWorld(String world) {
            this.world = world;
            return this;
        }

        /**
         * setter for purchased
         *
         * @param purchased true if the tile is purchased, false otherwise
         */
        public TileBuilder setPurchased(boolean purchased) {
            this.purchased = purchased;
            return this;
        }

        /**
         * setter for resourceValue
         *
         * @param resourceValue The resource value of the tile
         */
        public TileBuilder setResourceValue(int resourceValue) {
            this.resourceValue = resourceValue;
            return this;
        }

        /**
         * setter for hasRiver
         *
         * @param hasRiver true if the tile has a river, false otherwise
         */
        public TileBuilder setHasRiver(boolean hasRiver) {
            this.hasRiver = hasRiver;
            return this;
        }

        /**
         * setter for tileID
         *
         * @param tileID The ID of the tile
         */
        public TileBuilder setTileID(int tileID) {
            this.tileID = tileID;
            return this;
        }

        /**
         * Builds the Tile object.
         *
         * @return The constructed Tile object
         */
        public Tile build() {
            return new Tile(this);
        }
    }
}