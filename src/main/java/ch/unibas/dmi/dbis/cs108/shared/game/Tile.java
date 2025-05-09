package ch.unibas.dmi.dbis.cs108.shared.game;

import ch.unibas.dmi.dbis.cs108.shared.entities.Findables.Artifact;
import ch.unibas.dmi.dbis.cs108.shared.entities.GameEntity;

/**
 * Represents a tile on the game board.
 * Each tile can contain an entity, an artifact, and has various properties
 * such as ownership, price, and resource value.
 */
public class Tile {

    /** x-coordinate */
    private final int x;
    /** y-coordinate */
    private final int y;
    /** name of the owner */
    private String owner; //ownerID
    /** price of the tile */
    private int price;
    /** The entity on the tile */
    private GameEntity entity; // Not an artifact
    /** The artifact on the tile */
    private Artifact artefact;
    /** The world the tile is in */
    private String world;
    /** If the tile is purchased or not */
    private boolean purchased;
    /** The value of the tile */
    private int resourceValue; //Runes: bei spezifischen sind es energy: dort vermerkt
    /** If the tile has a river */
    private boolean hasRiver; //has a river = true
    /** the id of the tile */
    private int tileID;
    /** the status of the tile */
    private final Status status;

    /**
     * Constructor for Tile.
     *
     * @param builder The TileBuilder object containing the parameters for this tile
     */
    public Tile(TileBuilder builder) {
        this.x = builder.x;
        this.y = builder.y;
        this.owner = builder.owner;
        this.price = builder.price;
        this.entity = builder.entity;
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
     * setter for buff
     *
     * @param buffType the type of buff to set
     * @param effect   the effect of the buff
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
     * @param buff  the buff to add
     * @param value the value of the buff (positive for buff, negative for debuff)
     */
    public void addBuff(Status.BuffType buff, double value) {
        status.buff(buff, value);
    }

    /**
     * getter for the entity
     *
     * @return the entity
     */
    public GameEntity getEntity() {
        return entity;
    }

    /**
     * setter for hasEntity
     *
     * @param entity the entity to set
     */
    public void setEntity(GameEntity entity) {
        this.entity = entity;
    }

    /**
     * getter for the owner
     *
     * @return the owner
     */
    public String getOwner() {
        return owner;
    }

    /**
     * setter for the owner
     *
     * @param owner the owner to set
     */
    public void setOwner(String owner) {
        this.owner = owner;
    }

    /**
     * getter for the price
     *
     * @return the price
     */
    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    /**
     * getter for the artefact
     *
     * @return the artifact
     */
    public Artifact getArtifact() {
        return artefact;
    }

    /**
     * setter for the artefact
     *
     * @param artefact the artifact to set
     */
    public void setArtifact(Artifact artefact) {
        this.artefact = artefact;
    }

    /**
     * getter for the world
     *
     * @return the world
     */
    public String getWorld() {
        return world;
    }

    public void setWorld(String s) {
        this.world = s;
    }

    /**
     * getter for the purchased status
     *
     * @return true if the tile is purchased, false otherwise
     */
    public boolean isPurchased() {
        return purchased;
    }

    /**
     * setter for the purchased status
     *
     * @param purchased the value to set
     */
    public void setPurchased(boolean purchased) {
        this.purchased = purchased;
    }

    /**
     * getter for the resource value
     *
     * @return the value
     */
    public int getResourceValue() {
        return resourceValue;
    }

    /**
     * setter for the resource value
     *
     * @param resourceValue the value to set
     */
    public void setResourceValue(int resourceValue) {
        this.resourceValue = resourceValue;
    }

    /**
     * getter for the hasRiver status
     *
     * @return if the tile has a river
     */
    public boolean hasRiver() {
        return hasRiver;
    }

    /**
     * setter for the hasRiver status
     *
     * @param hasRiver sets if the tile has a river
     */
    public void setHasRiver(boolean hasRiver) {
        this.hasRiver = hasRiver;
    }

    /**
     * getter for the tileID
     *
     * @return the tileID
     */
    public int getTileID() {
        return tileID;
    }

    /**
     * Sets the tile ID.
     *
     * @param i the tileID.
     */
    public void setTileID(int i) {
        this.tileID = i;
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
        return entity;
    }

    /**
     * Checks if the tile has an owner.
     *
     * @return true if the tile has an owner, false otherwise.
     */
    public boolean hasOwner() {
        return owner != null;
    }

    /**
     * Sets the owner name
     *
     * @param newName the name to set
     */
    public void setOwnerName(String newName) {
        this.owner = newName;
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
        private String owner;
        private int price;
        private GameEntity entity;
        private Artifact artefact;
        private String world;

        /**
         * Creates a new TileBuilder.
         */
        public TileBuilder() {
            this.purchased = false;
            this.hasRiver = false;
            this.owner = null;
        }

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