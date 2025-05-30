package ch.unibas.dmi.dbis.cs108.shared.game;

import ch.unibas.dmi.dbis.cs108.SETTINGS;
import ch.unibas.dmi.dbis.cs108.shared.entities.EntityRegistry;
import ch.unibas.dmi.dbis.cs108.shared.entities.Findables.Artifact;
import ch.unibas.dmi.dbis.cs108.shared.entities.Findables.Monument;
import ch.unibas.dmi.dbis.cs108.shared.entities.GameEntity;
import ch.unibas.dmi.dbis.cs108.shared.entities.Purchasables.PurchasableEntity;
import ch.unibas.dmi.dbis.cs108.shared.entities.Purchasables.Statues.Statue;
import ch.unibas.dmi.dbis.cs108.shared.entities.Purchasables.Structure;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

/**
 * Player class is responsible for creating a player object
 */
public class Player {
    /** The UUID of the player */
    private final UUID playerID;
    /** The name of the player */
    private String name;
    /** The amount of runes the player has */
    private int runes;
    /** The amount of energy the player has */
    private int energy;
    /** The list of owned tiles*/
    private List<Tile> ownedTiles = new ArrayList<>();
    /** The list of artifacts */
    private List<Artifact> artifacts = new ArrayList<>();
    /** The list of purchasable entities */
    private List<PurchasableEntity> purchasableEntities = new ArrayList<>();
    /** The list of monuments */
    private List<Monument> monuments = new ArrayList<>();
    /**  The status of the player */
    private Status status;
    /** The number of tiles the player has bought this round */
    private int roundBoughtTiles;

    /**
     * Constructor for Player class
     * <p>
     * //@param id   String
     *
     * @param name String
     */
    public Player(String name) {
        this.playerID = UUID.randomUUID();
        this.name = name;
        runes = SETTINGS.Config.START_RUNES.getValue(); //Startrunen
        energy = SETTINGS.Config.START_ENERGY.getValue(); //Startenergie
        status = new Status();
        roundBoughtTiles = 0;
    }

    /**
     * Method for buying anything
     * Removes runes from the player according to given price and taking into account the buffs
     *
     * @param price int
     * @return boolean
     */
    public boolean buy(int price) {
        if (price < 0) {
            return false;
        }

        double priceModifier = status.get(Status.BuffType.SHOP_PRICE);
        double adjusted = price / Math.max(priceModifier, 0.5); // Prevent divide-by-zero or negative scaling and ensure maximum price of 200% original
        int adjustedPrice = Math.max(0, (int) Math.round(adjusted)); // Ensure price is never negative

        if (runes >= adjustedPrice) {
            runes -= adjustedPrice;
            return true;
        }

        return false;
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
        if (!isDebuffable() && value < 0) {
            return; // Do not apply debuff if player is not debuffable
        }
        status.buff(buff, value);
    }

    /**
     * Checks if the player is debuffable.
     *
     * @return true if the player is debuffable.
     */
    public boolean isDebuffable() {
        return status.get(Status.BuffType.DEBUFFABLE) == 1.0;
    }

    /**
     * Getter for owned purchasableEntity
     *
     * @return the purchasableEntities
     */
    public List<PurchasableEntity> getPurchasableEntities() {
        return purchasableEntities;
    }

    /**
     * Setter for owned purchasableEntity
     *
     * @param purchasableEntities the purchasableEntities to set
     */
    public void setPurchasableEntities(List<PurchasableEntity> purchasableEntities) {
        this.purchasableEntities = purchasableEntities;
    }

    /**
     * adds a purchasableEntity to the player
     *
     * @param entity the purchasable entity to add
     */
    public void addPurchasableEntity(PurchasableEntity entity) {
        purchasableEntities.add(entity);
    }

    /**
     * removes a structure from the player
     *
     * @param entity Structure
     */
    public void removePurchasableEntity(PurchasableEntity entity) {
        purchasableEntities.remove(entity);
    }


    /**
     * Getter for owned monument
     *
     * @return the monuments
     */
    public List<Monument> getMonuments() {
        return monuments;
    }

    /**
     * Setter for owned monument
     *
     * @param monuments the monuments to set
     */
    public void setMonuments(List<Monument> monuments) {
        this.monuments = monuments;
    }

    /**
     * adds a monument to the player
     *
     * @param monument the monument to add
     */
    public void addOwnedMonument(Monument monument) {
        monuments.add(monument);
    }

    /**
     * removes a monument from the player
     *
     * @param monument monument
     */
    public void removeMonument(Monument monument) {
        monuments.remove(monument);
    }

    /**
     * Checks whether a player has a complete set of a certain monument
     *
     * @param monument the monument
     * @return true if yes, false, if not.
     */
    public boolean hasCompleteSet(Monument monument) {
        // Get all monuments from the registry that are part of the same set
        //TODO check if this actually works
        List<Integer> requiredSet = EntityRegistry.getAllMonuments().stream()
                .filter(m -> m.isSet() && m.equals(monument))
                .map(GameEntity::getId)
                .toList();

        List<Integer> playerSet = monuments.stream()
                .filter(m -> m.isSet() && m.equals(monument))
                .map(GameEntity::getId)
                .toList();
        // Check if the player's monuments contain all of them
        return new HashSet<>(playerSet).containsAll(requiredSet);
    }

    /**
     * Getter for runeBoughtTiles
     *
     * @return roundBoughtTiles the number of tiles already bought
     */
    public int getRoundBoughtTiles() {
        return roundBoughtTiles;
    }

    /**
     * Setter for runeBoughtTiles
     *
     * @param roundBoughtTiles the number of tiles already bought
     */
    public void setRoundBoughtTiles(int roundBoughtTiles) {
        this.roundBoughtTiles = roundBoughtTiles;
    }

    /**
     * adds a bought tile for this turn
     */
    public void addBoughtTile() {
        roundBoughtTiles++;
    }

    /**
     * Getter for artifacts
     *
     * @return the artifacts
     */
    public List<Artifact> getArtifacts() {
        return artifacts;
    }

    /**
     * Setter for artifacts
     *
     * @param artifacts the artifacts
     */
    public void setArtifacts(List<Artifact> artifacts) {
        this.artifacts = artifacts;
    }

    /**
     * adds an artifact to the player
     *
     * @param artifact Artefact
     * @return true if the action was successful
     */
    public boolean addArtifact(Artifact artifact) {
        if (artifacts.size() < SETTINGS.Config.MAX_ARTIFACTS.getValue()) {
            artifacts.add(artifact);
            return true;
        }
        return false;
    }

    /**
     * removes an artifact from the player
     *
     * @param artifact Artefact
     */
    public void removeArtifact(Artifact artifact) {
        artifacts.remove(artifact);
    }

    /**
     * Getter for id
     *
     * @return String
     */
    public UUID getId() {
        return playerID;
    }

    /**
     * Getter for name
     *
     * @return String
     */
    public String getName() {
        return name;
    }

    /**
     * Setter for name
     *
     * @param name String
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Getter for player id
     *
     * @return player_id
     */
    public UUID getPlayer_id() {
        return playerID;
    }

    /**
     * Getter for runes
     *
     * @return int
     */
    public int getRunes() {
        return runes;
    }

    /**
     * Setter for runes
     *
     * @param runes int
     */
    public void setRunes(int runes) {
        this.runes = runes;
    }

    /**
     * Getter for energy
     *
     * @return int
     */
    public int getEnergy() {
        return energy;
    }

    /**
     * Setter for energy
     *
     * @param energy int
     */
    public void setEnergy(int energy) {
        this.energy = energy;
    }

    /**
     * Getter for player id
     *
     * @return player_id
     */
    public UUID getPlayerID() {
        return playerID;
    }

    /**
     * Getter for owned tiles
     *
     * @return ownedTiles
     */
    public List<Tile> getOwnedTiles() {
        return ownedTiles;
    }

    /**
     * Setter for owned tiles
     *
     * @param ownedTiles List Tile
     */
    public void setOwnedTiles(List<Tile> ownedTiles) {
        this.ownedTiles = ownedTiles;
    }

    /**
     * Adds runes to the player, runes should never be negative
     *
     * @param amount int
     *   @return int
     */
    public int addRunes(int amount) {
        runes += amount;
        if (runes < 0) {
            amount += runes;
            runes = 0;
        }
        return amount;
    }

    /**
     * Adds energy to the player.
     * Negative values are allowed to remove energy.
     * If the energy exceeds 4, it is capped at 4.
     * If the energy goes below 0, it is capped at 0.
     *
     * @param amount int
     */
    public void addEnergy(int amount) {
        if (energy + amount > SETTINGS.Config.MAX_ENERGY.getValue()) {
            energy = 4;
            return;
        } else if (energy + amount < 0) {
            energy = 0;
            return;
        }
        this.energy += amount;
    }

    /**
     * Adds a tile to the player
     * Tile is only added if it is not already owned and not null
     *
     * @param tile Tile
     */
    public void addOwnedTile(Tile tile) {
        if (!ownedTiles.contains(tile) && tile != null) {
            ownedTiles.add(tile);
            tile.setOwner(name);
        }
    }

    /**
     * toString method for Player class
     *
     * @return String including name, runes and energy
     */
    @Override
    public String toString() {
        return "Player{" +
                "name='" + name + '\'' +
                ", runes=" + runes +
                ", energy=" + energy +
                '}';


    }

    /**
     * Cleanup the resources at the end of the game.
     */
    public void reset() {
        this.ownedTiles.clear();
        this.purchasableEntities.clear();
        this.monuments.clear();
        this.status = new Status();
        this.roundBoughtTiles = 0;
        this.name = "";
        this.artifacts.clear();
        this.runes = 0;
        this.energy = 0;
    }

    /**
     * Checks is the player already owns a statue
     *
     * @return true if he does false otherwise
     */
    public boolean hasStatue() {
        for (PurchasableEntity purchasableEntity : purchasableEntities) {
            if (purchasableEntity instanceof Statue) {
                return true;
            }
        }
        return false;
    }

    /**
     * Removes an owned Tile
     *
     * @param removeTile the tile to remove
     */
    public void removeOwnedTile(Tile removeTile) {
        for (Tile tile : ownedTiles) {
            if (tile.getX() == removeTile.getX() && tile.getY() == removeTile.getY()) {
                ownedTiles.remove(tile);
                break;
            }
        }
    }

    /**
     * Returns all owned structures as a list
     *
     * @return structures the list of structures
     */
    public List<Structure> getStructures() {
        List<Structure> structures = new ArrayList<>();
        for (PurchasableEntity purchasableEntity : purchasableEntities) {
            if (purchasableEntity instanceof Structure) {
                structures.add((Structure) purchasableEntity);
            }
        }
        return structures;
    }

    /**
     * Gets all tiles, that hold structures as a List
     *
     * @return the List of Tiles with Structures
     */
    public List<Tile> getTilesWithStructures() {
        List<Tile> tilesWithStructures = new ArrayList<>();
        for (Tile tile : ownedTiles) {
            if (tile.hasEntity() && tile.getEntity() instanceof Structure) {
                tilesWithStructures.add(tile);
            }
        }
        return tilesWithStructures;
    }

}