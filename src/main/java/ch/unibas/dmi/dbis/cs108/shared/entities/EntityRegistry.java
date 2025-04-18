package ch.unibas.dmi.dbis.cs108.shared.entities;

import ch.unibas.dmi.dbis.cs108.shared.entities.Findables.Artifact;
import ch.unibas.dmi.dbis.cs108.shared.entities.Findables.Monument;
import ch.unibas.dmi.dbis.cs108.shared.entities.Purchasables.PurchasableEntity;
import ch.unibas.dmi.dbis.cs108.shared.entities.Purchasables.Statues.Statue;
import ch.unibas.dmi.dbis.cs108.shared.entities.Purchasables.Structure;
import ch.unibas.dmi.dbis.cs108.shared.utils.RandomGenerator;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Central registry for all game entities.
 * Responsible for loading entity data from JSON files and providing
 * access to entities by ID or type.
 */
public class EntityRegistry {
    /**
     * Map of structure IDs to Structure objects.
     */
    private static final Map<Integer, Structure> structures = new HashMap<>();

    /**
     * Map of statue IDs to Statue objects.
     */
    private static final Map<Integer, Statue> statues = new HashMap<>();

    /**
     * Map of artifact IDs to Artifact objects.
     */
    private static final Map<Integer, Artifact> artifacts = new HashMap<>();

    /**
     * Map of monument IDs to Monument objects.
     */
    private static final Map<Integer, Monument> monuments = new HashMap<>();


    /**
     * Static initializer to load all entities when the class is first accessed.
     */
    static {
        loadEntities();
    }

    /**
     * Loads all entity types from their respective JSON files.
     */
    private static void loadEntities() {
        loadStructures();
        loadStatues();
        loadArtifacts();
        loadMonuments();
    }

    /**
     * Loads structure entities from the structures.json resource file.
     */
    private static void loadStructures() {
        Gson gson = new Gson();
        InputStream is = EntityRegistry.class.getResourceAsStream("/json/structures.json");
        if (is == null) {
            System.err.println("Could not find structures.json");
            return;
        }

        Type listType = new TypeToken<List<JsonElement>>(){}.getType();
        List<JsonElement> elements = gson.fromJson(new InputStreamReader(is), listType);

        for (JsonElement elem : elements) {
            if (elem.isJsonObject()) {
                JsonObject obj = elem.getAsJsonObject();
                if (obj.has("id")) {
                    Structure structure = Structure.fromJson(obj);
                    structures.put(structure.getId(), structure);
                }
            }
        }
    }

    /**
     * Loads statue entities from the statues.json resource file.
     */
    private static void loadStatues() {
        Gson gson = new Gson();
        InputStream is = EntityRegistry.class.getResourceAsStream("/json/statues.json");
        if (is == null) {
            System.err.println("Could not find statues.json");
            return;
        }

        Type listType = new TypeToken<List<JsonElement>>(){}.getType();
        List<JsonElement> elements = gson.fromJson(new InputStreamReader(is), listType);

        for (JsonElement elem : elements) {
            if (elem.isJsonObject()) {
                JsonObject obj = elem.getAsJsonObject();
                if (obj.has("id")) {
                    Statue statue = Statue.fromJson(obj);
                    statues.put(statue.getId(), statue);
                }
            }
        }
    }

    /**
     * Loads artifact entities from the artifacts.json resource file.
     */
    private static void loadArtifacts() {
        Gson gson = new Gson();
        InputStream is = EntityRegistry.class.getResourceAsStream("/json/artifacts.json");
        if (is == null) {
            System.err.println("Could not find artifacts.json");
            return;
        }

        Type listType = new TypeToken<List<JsonElement>>(){}.getType();
        List<JsonElement> elements = gson.fromJson(new InputStreamReader(is), listType);

        for (JsonElement elem : elements) {
            if (elem.isJsonObject()) {
                JsonObject obj = elem.getAsJsonObject();
                if (obj.has("name")) {
                    Artifact artifact = Artifact.fromJson(obj);
                    artifacts.put(artifact.getId(), artifact);
                }
            }
        }
    }

    /**
     * Loads monument entities from the monument.json resource file.
     */
    private static void loadMonuments() {
        Gson gson = new Gson();
        InputStream is = EntityRegistry.class.getResourceAsStream("/json/monuments.json");
        if (is == null) {
            System.err.println("Could not find monuments.json");
            return;
        }

        Type listType = new TypeToken<List<JsonElement>>(){}.getType();
        List<JsonElement> elements = gson.fromJson(new InputStreamReader(is), listType);

        for (JsonElement elem : elements) {
            if (elem.isJsonObject()) {
                JsonObject obj = elem.getAsJsonObject();
                if (obj.has("name")) {
                    Monument monument = Monument.fromJson(obj);
                    monuments.put(monument.getId(), monument);
                }
            }
        }
    }


    /**
     * Returns a PurchasableEntity by its ID.
     *
     * @param id The ID of the entity to retrieve
     * @return The PurchasableEntity object with the given ID, or null if not found
     */
    public static PurchasableEntity getPurchasableEntity(int id) {
        PurchasableEntity entity = structures.get(id);
        if (entity == null) {
            entity = statues.get(id);
        }
        return entity;
    }


    /**
     * Returns a structure by its ID.
     *
     * @param id The ID of the structure to retrieve
     * @return The Structure object with the given ID, or null if not found
     */
    public static Structure getStructure(int id) {
        return structures.get(id);
    }

    /**
     * Returns a statue by its ID.
     *
     * @param id The ID of the statue to retrieve
     * @return The Statue object with the given ID, or null if not found
     */
    public static Statue getStatue(int id) {
        return statues.get(id);
    }

    /**
     * Returns an artifact by its ID.
     *
     * @param id The ID of the artifact to retrieve
     * @return The Artifact object with the given ID, or null if not found
     */
    public static Artifact getArtifact(int id) {
        return artifacts.get(id);
    }

    /**
     * Returns a monument by its ID.
     *
     * @param id The ID of the monument to retrieve
     * @return The Monument object with the given ID, or null if not found
     */
    public static Monument getMonument(int id) {
        return monuments.get(id);
    }

    /**
     * Returns a collection of all available structures.
     *
     * @return Collection of all Structure objects
     */
    public static Collection<Structure> getAllStructures() {
        return structures.values();
    }

    /**
     * Returns a collection of all available statues.
     *
     * @return Collection of all Statue objects
     */
    public static Collection<Statue> getAllStatues() {
        return statues.values();
    }

    /**
     * Returns a collection of all available artifacts.
     *
     * @return Collection of all Artifact objects
     */
    public static Collection<Artifact> getAllArtifacts() {
        return artifacts.values();
    }

    /**
     * Returns a collection of all available monuments.
     *
     * @return Collection of all Monument objects
     */
    public static Collection<Monument> getAllMonuments() {
        return monuments.values();
    }

    /**
     * Returns a random artifact from the registry.
     *
     * @return A random Artifact object
     */
    public static Artifact getRandomArtifact() {
        return getArtifact(RandomGenerator.randomIntInRange(10,21));
    }
}