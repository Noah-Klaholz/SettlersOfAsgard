package ch.unibas.dmi.dbis.cs108.shared.game;

import javafx.fxml.FXML;

/**
 * Status class represents the status of a player in the game.
 * It can be used to track various buffs and debuffs
 */
public class Status {

    /**
     * This value determines the efficiency of rune generation.
     */
    private double runeEfficieny;
    /**
     * This value determines the efficiency of energy generation.
     */
    private double energyEfficiency;
    /**
     * This value determines the efficiency of rune generation for river tiles.
     */
    private double riverRuneEfficiency;
    /**
     * This value determines the efficiency of shop prices.
     */
    private double shopPriceEfficiency;
    /**
     * This value determines the artifact chance.
     */
    private double artifactChance;
    /**
     * This value determines wether the player is debuffable or not.
     */
    private boolean debuffable;
    /**
     * Constructor for Status class.
     * Initializes the efficiency values to default.
     */
    public Status() {
        this.energyEfficiency = 1.0;
        this.runeEfficieny = 1.0;
        this.riverRuneEfficiency = 1.0;
        this.shopPriceEfficiency = 1.0;
        this.artifactChance = 1.0;
        this.debuffable = true; // Default to debuffable
    }

    /**
     * Gets the efficiency value for a specific buff type.
     *
     * @param type the type of buff
     * @return the efficiency value for the specified buff type
     */
    public double get(BuffType type) {
        return switch (type) {
            case RUNE_GENERATION -> runeEfficieny;
            case ENERGY_GENERATION -> energyEfficiency;
            case RIVER_RUNE_GENERATION -> riverRuneEfficiency;
            case SHOP_PRICE -> shopPriceEfficiency;
            case ARTIFACT_CHANCE -> artifactChance;
            case DEBUFFABLE -> debuffable ? 1.0 : 0.0; // Return 1.0 if debuffable, else 0.0
            default -> throw new IllegalArgumentException("Unknown buff type: " + type);
        };
    }

    /**
     * Buffs the player with a specific type and value.
     *
     * @param type  the type of buff to apply
     * @param value the value of the buff (positive for buff, negative for debuff)
     */
    public void buff(BuffType type, double value) {
        value = Math.round(value * 100) / 100.0; // Round to 2 decimal places
        switch (type) {
            case RUNE_GENERATION:
                runeEfficieny = Math.round((runeEfficieny + value) * 100) / 100.0;
                if (runeEfficieny < 0) runeEfficieny = 0;
                break;
            case ENERGY_GENERATION:
                energyEfficiency = Math.round((energyEfficiency + value) * 100) / 100.0;
                if (energyEfficiency < 0) energyEfficiency = 0;
                break;
            case RIVER_RUNE_GENERATION:
                riverRuneEfficiency = Math.round((riverRuneEfficiency + value) * 100) / 100.0;
                if (riverRuneEfficiency < 0) riverRuneEfficiency = 0;
                break;
            case SHOP_PRICE:
                shopPriceEfficiency = Math.round((shopPriceEfficiency + value) * 100) / 100.0;
                if (shopPriceEfficiency < 0.5) shopPriceEfficiency = 0.5;
                break;
            case ARTIFACT_CHANCE:
                artifactChance = Math.round((artifactChance + value) * 100) / 100.0;
                if (artifactChance < 0) artifactChance = 0;
                break;
            case DEBUFFABLE:
                debuffable = value > 0;
                break;
        }
    }

    /**
     * Resets the status to default values.
     */
    public void reset() {
        this.energyEfficiency = 1.0;
        this.runeEfficieny = 1.0;
        this.riverRuneEfficiency = 1.0;
        this.shopPriceEfficiency = 1.0;
        this.artifactChance = 1.0;
        this.debuffable = true; // Default to debuffable
    }

    /**
     * Sets the efficiency value for a specific buff type.
     *
     * @param buffType the type of buff
     * @param value    the efficiency value to set
     */
    public void set(BuffType buffType, double value) {
        switch (buffType) {
            case RUNE_GENERATION -> runeEfficieny = value;
            case ENERGY_GENERATION -> energyEfficiency = value;
            case RIVER_RUNE_GENERATION -> riverRuneEfficiency = value;
            case SHOP_PRICE -> shopPriceEfficiency = value;
            case ARTIFACT_CHANCE -> artifactChance = value;
            case DEBUFFABLE -> debuffable = value > 0; // Return 1.0 if debuffable, else 0.0
            default -> throw new IllegalArgumentException("Unknown buff type: " + buffType);
        };
    }

    /**
     * Enum representing different types of buffs.
     */
    public enum BuffType {
        RUNE_GENERATION,
        ENERGY_GENERATION,
        RIVER_RUNE_GENERATION,
        SHOP_PRICE,
        ARTIFACT_CHANCE,
        DEBUFFABLE
        // Add more buff types as needed
    }

    /**
     * Returns a string representation of the Status object.
     *
     * @return a string representation of the Status object
     */
    @Override
    public String toString() {
        return "Status{" +
                "runeEfficieny=" + runeEfficieny +
                ", energyEfficiency=" + energyEfficiency +
                ", riverRuneEfficiency=" + riverRuneEfficiency +
                ", shopPriceEfficiency=" + shopPriceEfficiency +
                ", artifactChance=" + artifactChance +
                ", debuffable=" + debuffable +
                '}';
    }
}
