// Java
package ch.unibas.dmi.dbis.cs108.shared.entities;

public class Parameter {
    private String name;
    private double value;

    public Parameter(String name, double value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public double getValue() {
        return value;
    }
}