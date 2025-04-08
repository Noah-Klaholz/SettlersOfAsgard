package ch.unibas.dmi.dbis.cs108.shared.entities.entities.artefacts;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Repository class for loading artefacts from a JSON file.
 * Implements singleton pattern and caching for efficient access.
 */
public class ArtefactRepository {
    private static final Logger LOGGER = Logger.getLogger(ArtefactRepository.class.getName());
    private static final String ARTEFACTS_PATH = "/json/artifacts.json";
    private static ArtefactRepository instance;

    private List<Artefact> cachedArtefacts;
    private List<ArtefactData> cachedArtefactData;

    private ArtefactRepository() {
        // Private constructor for singleton pattern
    }

    /**
     * Gets the singleton instance of the repository.
     *
     * @return The ArtefactRepository instance
     */
    public static synchronized ArtefactRepository getInstance() {
        if (instance == null) {
            instance = new ArtefactRepository();
        }
        return instance;
    }

    /**
     * Loads artefact data from JSON file.
     *
     * @return List of artefact data objects
     */
    private List<ArtefactData> loadArtefactData() {
        if (cachedArtefactData != null) {
            return cachedArtefactData;
        }

        try {
            InputStream inputStream = getClass().getResourceAsStream(ARTEFACTS_PATH);
            if (inputStream == null) {
                LOGGER.severe("Artefacts JSON file not found: " + ARTEFACTS_PATH);
                return Collections.emptyList();
            }

            InputStreamReader reader = new InputStreamReader(inputStream);
            Gson gson = new Gson();
            Type listType = new TypeToken<List<ArtefactData>>() {}.getType();
            List<ArtefactData> dataList = gson.fromJson(reader, listType);

            if (dataList == null) {
                LOGGER.severe("Failed to parse artefacts JSON: null result");
                return Collections.emptyList();
            }

            cachedArtefactData = Collections.unmodifiableList(dataList);
            return cachedArtefactData;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to load artefact data", e);
            return Collections.emptyList();
        }
    }

    /**
     * Creates an Artefact instance from ArtefactData.
     *
     * @param data The artefact data to convert
     * @return A new Artefact instance
     */
    private Artefact createArtefact(ArtefactData data) {
        return new Artefact(data);
    }

    /**
     * Loads all artefacts from the JSON file.
     * Results are cached for subsequent calls.
     *
     * @return An unmodifiable list of Artefact objects
     */
    public List<Artefact> loadArtefacts() {
        if (cachedArtefacts != null) {
            return cachedArtefacts;
        }

        List<Artefact> artefacts = new ArrayList<>();
        List<ArtefactData> dataList = loadArtefactData();

        for (ArtefactData data : dataList) {
            try {
                if (data.getName() != null) {
                    artefacts.add(createArtefact(data));
                } else {
                    LOGGER.warning("Skipping artefact with null name");
                }
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Failed to create artefact: " + e.getMessage(), e);
            }
        }

        cachedArtefacts = Collections.unmodifiableList(artefacts);
        LOGGER.info("Loaded " + artefacts.size() + " artefacts");

        return cachedArtefacts;
    }

    /**
     * Gets an artefact by its ID.
     *
     * @param id The ID of the artefact to find
     * @return The artefact with the specified ID, or null if not found
     */
    public Artefact getArtefactById(int id) {
        List<Artefact> artefacts = loadArtefacts();
        for (Artefact artefact : artefacts) {
            if (artefact.artifactID == id) {
                return artefact;
            }
        }
        return null;
    }

    /**
     * Clears the cache, forcing reload on next access.
     * Primarily used for testing.
     */
    public void clearCache() {
        cachedArtefacts = null;
        cachedArtefactData = null;
    }
}