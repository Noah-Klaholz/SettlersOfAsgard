package ch.unibas.dmi.dbis.cs108.shared.entities.entities.structures;

public class StructureFactory {
    public static Structure createStructure(StructureData data) {
        return switch (data.getName()) {
            case "Rune Table" -> new RuneTable(data);
            case "Mimisbrunnr" -> new Mimisbrunnr(data);
            case "Helgrindr" -> new Helgrindr(data);
            case "Huginn and Muninn" -> new HuginnAndMuninn(data);
            case "Ran's Hall" -> new RansHall(data);
            case "Surtur's Smeltery" -> new SurtursSmeltery(data);
            case "Tree" -> new Tree(data);
            default -> throw new IllegalArgumentException("Unknown structure: " + data.getName());
        };
    }
}