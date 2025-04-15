package ch.unibas.dmi.dbis.cs108.shared.entities;

import ch.unibas.dmi.dbis.cs108.shared.entities.Findables.Artifact;
import ch.unibas.dmi.dbis.cs108.shared.entities.Findables.Trap;
import ch.unibas.dmi.dbis.cs108.shared.entities.Purchasables.Statue;
import ch.unibas.dmi.dbis.cs108.shared.entities.Purchasables.Structure;
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
     * Map of trap IDs to Trap objects.
     */
    private static final Map<Integer, Trap> traps = new HashMap<>();

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
        loadTraps();
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
     * Loads trap entities from the traps.json resource file.
     */
    private static void loadTraps() {
        Gson gson = new Gson();
        InputStream is = EntityRegistry.class.getResourceAsStream("/json/traps.json");
        if (is == null) {
            System.err.println("Could not find traps.json");
            return;
        }

        Type listType = new TypeToken<List<JsonElement>>(){}.getType();
        List<JsonElement> elements = gson.fromJson(new InputStreamReader(is), listType);

        for (JsonElement elem : elements) {
            if (elem.isJsonObject()) {
                JsonObject obj = elem.getAsJsonObject();
                if (obj.has("name")) {
                    Trap trap = Trap.fromJson(obj);
                    traps.put(trap.getId(), trap);
                }
            }
        }
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
     * Returns a trap by its ID.
     *
     * @param id The ID of the trap to retrieve
     * @return The Trap object with the given ID, or null if not found
     */
    public static Trap getTrap(int id) {
        return traps.get(id);
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
     * Returns a collection of all available traps.
     *
     * @return Collection of all Trap objects
     */
    public static Collection<Trap> getAllTraps() {
        return traps.values();
    }
}