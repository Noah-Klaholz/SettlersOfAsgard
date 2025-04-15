package ch.unibas.dmi.dbis.cs108.shared.game;

/**
 * Status class represents the status of a player in the game.
 * It can be used to track various buffs and debuffs
 */
public class Status {

    public enum BuffType {
        RUNE_GENERATION,
        ENERGY_GENERATION,
        RIVER_RUNE_GENERATION,
        SHOP_PRICE,
        ARTIFACT_CHANCE
        // Add more buff types as needed
    }

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
     * Constructor for Status class.
     * Initializes the efficiency values to default.
     */
    public Status() {
        this.energyEfficiency = 1.0;
        this.runeEfficieny = 1.0;
        this.riverRuneEfficiency = 1.0;
        this.shopPriceEfficiency = 1.0;
        this.artifactChance = 1.0;
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
            default -> throw new IllegalArgumentException("Unknown buff type: " + type);
        };
    }


    /**
     * Buffs the player with a specific type and value.
     *
     * @param type the type of buff to apply
     * @param value the value of the buff (positive for buff, negative for debuff)
     */
    public void buff(BuffType type, double value) {
        switch (type) {
            case RUNE_GENERATION:
                runeEfficieny += value;
                break;
            case ENERGY_GENERATION:
                energyEfficiency += value;
                break;
            case RIVER_RUNE_GENERATION:
                riverRuneEfficiency += value;
                break;
            case SHOP_PRICE:
                shopPriceEfficiency += value;
                break;
            case ARTIFACT_CHANCE:
                artifactChance *= value;
                break;
        }
    }
}
