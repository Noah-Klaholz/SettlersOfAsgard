package ch.unibas.dmi.dbis.cs108.server.core.entities.statue;

public class StatueFactory {
    public Statue createStatue(StatueData data) {
        return switch (data.getName()) {
            case "Jörmungandr" -> new Joermungandr(data);
            case "Freyr" -> new Freyr(data);
            case "Dwarf" -> new Dwarf(data);
            case "Freyja" -> new Freyja(data);
            case "Hel" -> new Hel(data);
            case "Nidhöggr" -> new Nidhoeggr(data);
            case "Loki" -> new Loki(data);
            case "Surtr" -> new Surtr(data);
            default -> throw new IllegalArgumentException("Unknown statue type: " + data.getName());
        };
    }
}