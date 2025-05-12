package ch.unibas.dmi.dbis.cs108.shared.entities.Purchasables.Statues;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

/**
 * Defines the parameter requirements for statue effects.
 * Used to validate if provided parameters satisfy what a statue effect needs.
 */
public class StatueParameterRequirement {

    private final Set<StatueParameterType> required;

    /**
     * Creates a requirement with specified parameter types.
     *
     * @param types The parameter types required for this effect
     */
    public StatueParameterRequirement(StatueParameterType... types) {
        this.required = EnumSet.noneOf(StatueParameterType.class);
        if (types != null) {
            Collections.addAll(this.required, types);
        }
    }

    /**
     * Checks if a specific parameter type is required.
     *
     * @param type The parameter type to check
     * @return true if the parameter type is required
     */
    public boolean requires(StatueParameterType type) {
        return required.contains(type);
    }

    /**
     * Gets all required parameter types.
     *
     * @return An unmodifiable set of required parameter types
     */
    public Set<StatueParameterType> getRequiredTypes() {
        return Collections.unmodifiableSet(required);
    }

    /**
     * Defines the types of parameters that can be used in statue effects.
     */
    public enum StatueParameterType {
        /*
         * The order of these types is important, as it is used to determine the
         * order in which parameters are passed to the statue effect.
         */
        PLAYER,    // Target player
        /*
         * The following types are used for the statue effect's target.
         * They are used to determine the type of target that the statue effect
         * can be applied to.
         */
        TILE,      // x,y coordinates
        /*
         * The following types are used for the statue effect's target.
         * They are used to determine the type of target that the statue effect
         * can be applied to.
         */
        STRUCTURE, // Structure ID
        /*
         * The following types are used for the statue effect's target.
         * They are used to determine the type of target that the statue effect
         * can be applied to.
         */
        ARTIFACT   // Artifact ID
    }

}