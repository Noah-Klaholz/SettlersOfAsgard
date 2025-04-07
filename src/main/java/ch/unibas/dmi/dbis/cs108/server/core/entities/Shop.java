package ch.unibas.dmi.dbis.cs108.server.core.entities;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class Shop {
    private ArrayList<Structure> buyableStructures;
    private ArrayList<Statue> buyableStatues;
    private boolean statueInUse; //todo: blocks the list of statues so no new one can be build while one is in possession

    public Shop() {
        this.buyableStructures = new ArrayList<>();
        this.buyableStatues = new ArrayList<>();
        loadStructures();
        loadStatues();
    }

    private void loadStructures() {
        Gson gson = new Gson();
        InputStream inputStream = getClass().getResourceAsStream("/json/structures.json");
        if (inputStream == null) {
            System.err.println("Could not find structures.json");
            return;
        }
        InputStreamReader reader = new InputStreamReader(inputStream);
        Type listType = new TypeToken<List<JsonElement>>() {
        }.getType();
        List<JsonElement> elements = gson.fromJson(reader, listType);

        for (JsonElement elem : elements) {
            if (elem.getAsJsonObject().has("name")) {
                StructureData data = gson.fromJson(elem, StructureData.class);
                Structure structure = new Structure(data.id, data.name, data.description, data.useType, data.price);
                buyableStructures.add(structure);
            }
        }
    }

    private void loadStatues() {
        Gson gson = new Gson();
        InputStream inputStream = getClass().getResourceAsStream("/json/statues.json");
        if (inputStream == null) {
            System.err.println("Could not find statues.json");
            return;
        }
        InputStreamReader reader = new InputStreamReader(inputStream);
        Type listType = new TypeToken<List<JsonElement>>() {
        }.getType();
        List<JsonElement> elements = gson.fromJson(reader, listType);

        for (JsonElement elem : elements) {
            // Only process objects that have a "name" field.
            if (elem.getAsJsonObject().has("name")) {
                StatueData data = gson.fromJson(elem, StatueData.class);
                Statue statue = new Statue(data.id, data.name, data.description, data.useType);
                buyableStatues.add(statue);
            }
        }
    }

    public ArrayList<Structure> getBuyableStructures() {
        return buyableStructures;
    }

    public ArrayList<Statue> getBuyableStatues() {
        return buyableStatues;
    }

    public void removeStructure(Structure structure) {
        buyableStructures.remove(structure);
    }

    public void blockStatue() {
        statueInUse = true;
    }

    private static class StructureData {
        int id;
        String name;
        String description;
        String useType;
        int price;
    }

    private static class StatueData {
        int id;
        String name;
        String description;
        String useType;
        int price;
        int upgradePrice;
    }
}