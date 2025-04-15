package ch.unibas.dmi.dbis.cs108.shared.game;

/**
 * PlayerStatus class represents the status of a player in the game.
 * It can be used to track various buffs and debuffs
 */
public class PlayerStatus {

    public enum BuffType {
        RUNE_GENERATION,
        ENERGY_GENERATION,
        RIVER_RUNE_GENERATION
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
     * Constructor for PlayerStatus class.
     * Initializes the efficiency values to default.
     */
    public PlayerStatus() {
        this.energyEfficiency = 1.0;
        this.runeEfficieny = 1.0;
    }

    public double get(BuffType type) {
        return switch (type) {
            case RUNE_GENERATION -> runeEfficieny;
            case ENERGY_GENERATION -> energyEfficiency;
            case RIVER_RUNE_GENERATION -> riverRuneEfficiency;
            default -> throw new IllegalArgumentException("Unknown buff type: " + type);
        };
    }


    /**
     * Buffs the player with a specific type and value.
     *
     * @param type
     * @param value
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
        }
    }
}
