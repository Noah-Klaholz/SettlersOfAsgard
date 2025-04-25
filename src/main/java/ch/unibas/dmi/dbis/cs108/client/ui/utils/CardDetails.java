package ch.unibas.dmi.dbis.cs108.client.ui.utils;

public class CardDetails {
    final String title;
    final String description;
    final String lore;

    public CardDetails(String title, String description, String lore) {
        this.title = title;
        this.description = description;
        this.lore = lore;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getLore() {
        return lore;
    }
}