package ch.unibas.dmi.dbis.cs108.server.core.entities.artefacts;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.List;

public class ArefactRepository {
    public static List<ArtefactData> loadArtefacts() {
        InputStream inputStream = ArefactRepository.class.getResourceAsStream("/json/artifacts.json");
        if (inputStream == null) {
            throw new RuntimeException("Artifacts JSON file not found");
        }
        InputStreamReader reader = new InputStreamReader(inputStream);
        Gson gson = new Gson();
        Type listType = new TypeToken<List<ArtefactData>>() {
        }.getType();
        return gson.fromJson(reader, listType);
    }
}