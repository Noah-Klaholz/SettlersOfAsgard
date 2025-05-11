// Java
package ch.unibas.dmi.dbis.cs108.shared.entities.Behaviors;

/**
 * The Parameter class represents a parameter with a name and a value.
 * It is used to define various parameters in the game.
 */
public class Parameter {
    private final String name;
    private final double value;

    /**
     * Constructor to create a Parameter object.
     *
     * @param name  The name of the parameter.
     * @param value The value of the parameter.
     */
    public Parameter(String name, double value) {
        this.name = name;
        this.value = value;
    }

    /**
     * getName method to retrieve the name of the parameter.
     *
     * @return The name of the parameter.
     */
    public String getName() {
        return name;
    }

    /**
     * getValue method to retrieve the value of the parameter.
     *
     * @return The value of the parameter.
     */
    public double getValue() {
        return value;
    }
}