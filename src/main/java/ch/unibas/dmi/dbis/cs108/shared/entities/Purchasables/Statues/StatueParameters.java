package ch.unibas.dmi.dbis.cs108.shared.entities.Purchasables.Statues;

import ch.unibas.dmi.dbis.cs108.shared.entities.Findables.Artifact;
import ch.unibas.dmi.dbis.cs108.shared.entities.Purchasables.Structure;
import ch.unibas.dmi.dbis.cs108.shared.game.Player;

/**
 * Container for parameters used in statue effect execution.
 * Provides flexible storage and validation for different parameter types.
 */
public class StatueParameters {
    /** Player object */
    private Player targetPlayer;
    /** x-coordinate */
    private Integer x;
    /** y-coordinate */
    private Integer y;
    /** structure parameter object */
    private Structure structure;
    /** artifact parameter object */
    private Artifact artifact;

    /**
     * Default constructor.
     */
    public StatueParameters() {
    }

    /**
     * Creates parameters for targeting a player.
     *
     * @param targetPlayer The player to target
     * @return The parameter container
     */
    public static StatueParameters forPlayer(Player targetPlayer) {
        StatueParameters params = new StatueParameters();
        params.setTargetPlayer(targetPlayer);
        return params;
    }

    /**
     * Creates parameters for targeting a tile.
     *
     * @param x The X coordinate
     * @param y The Y coordinate
     * @return The parameter container
     */
    public static StatueParameters forTile(int x, int y) {
        StatueParameters params = new StatueParameters();
        params.setTileCoordinates(x, y);
        return params;
    }

    /**
     * Creates parameters for targeting a structure.
     *
     * @param structure The structure
     * @return The parameter container
     */
    public static StatueParameters forStructure(Structure structure) {
        StatueParameters params = new StatueParameters();
        params.setStructure(structure);
        return params;
    }

    /**
     * Gets the target player.
     *
     * @return The target player or null if not set
     */
    public Player getTargetPlayer() {
        return targetPlayer;
    }

    /**
     * Sets the target player.
     *
     * @param targetPlayer The player to target with the effect
     */
    public void setTargetPlayer(Player targetPlayer) {
        this.targetPlayer = targetPlayer;
    }

    /**
     * Gets the X coordinate.
     *
     * @return The X coordinate or null if not set
     */
    public Integer getX() {
        return x;
    }

    /**
     * Gets the Y coordinate.
     *
     * @return The Y coordinate or null if not set
     */
    public Integer getY() {
        return y;
    }

    /**
     * Sets the tile coordinates.
     *
     * @param x The X coordinate
     * @param y The Y coordinate
     */
    public void setTileCoordinates(int x, int y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Gets the structure.
     *
     * @return The structure or null if not set
     */
    public Structure getStructure() {
        return structure;
    }

    /**
     * Sets the structure.
     *
     * @param structure The structure to use in the effect
     */
    public void setStructure(Structure structure) {
        this.structure = structure;
    }

    /**
     * Gets the artifact.
     *
     * @return The artifact or null if not set
     */
    public Artifact getArtifact() {
        return artifact;
    }

    /**
     * Sets the artifact.
     *
     * @param artifact The artifact to use in the effect
     */
    public void setArtifact(Artifact artifact) {
        this.artifact = artifact;
    }

    /**
     * Checks if tile coordinates are set.
     *
     * @return true if both X and Y coordinates are set
     */
    public boolean hasTileCoordinates() {
        return x != null && y != null;
    }

    /**
     * Validates parameters against requirements.
     *
     * @param requirement The parameter requirements to validate against
     * @return true if all required parameters are present
     */
    public boolean satisfiesRequirements(StatueParameterRequirement requirement) {
        if (requirement.requires(StatueParameterRequirement.StatueParameterType.PLAYER) && targetPlayer == null) {
            return false;
        }
        if (requirement.requires(StatueParameterRequirement.StatueParameterType.TILE) && !hasTileCoordinates()) {
            return false;
        }
        if (requirement.requires(StatueParameterRequirement.StatueParameterType.STRUCTURE) && structure == null) {
            return false;
        }
        return !requirement.requires(StatueParameterRequirement.StatueParameterType.ARTIFACT) || artifact != null;
    }

    /**
     * Builder for creating parameter containers.
     */
    public static class Builder {
        private final StatueParameters params = new StatueParameters();

        /**
         * Adds a player parameter.
         *
         * @param player The target player
         * @return This builder
         */
        public Builder withPlayer(Player player) {
            params.setTargetPlayer(player);
            return this;
        }

        /**
         * Adds tile coordinates.
         *
         * @param x The X coordinate
         * @param y The Y coordinate
         * @return This builder
         */
        public Builder withTile(int x, int y) {
            params.setTileCoordinates(x, y);
            return this;
        }

        /**
         * Adds a structure parameter.
         *
         * @param structure The structure
         * @return This builder
         */
        public Builder withStructure(Structure structure) {
            params.setStructure(structure);
            return this;
        }

        /**
         * Adds an artifact parameter.
         *
         * @param artifact The artifact
         * @return This builder
         */
        public Builder withArtifact(Artifact artifact) {
            params.setArtifact(artifact);
            return this;
        }

        /**
         * Builds the parameter container.
         *
         * @return The completed StatueParameters object
         */
        public StatueParameters build() {
            return params;
        }
    }
}