package ch.unibas.dmi.dbis.cs108.server.core.entities.statue;

import ch.unibas.dmi.dbis.cs108.server.core.entities.structures.Structure;
import ch.unibas.dmi.dbis.cs108.server.core.entities.structures.StructureData;
import ch.unibas.dmi.dbis.cs108.server.core.entities.structures.StructureFactory;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class StatueRepository {

    private List<Structure> cachedStructures;

    public List<Structure> loadStructures() throws IOException {
        if (cachedStructures != null) {
            return cachedStructures;
        }
        Gson gson = new Gson();
        InputStream is = getClass().getResourceAsStream("/json/structures.json");
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
            Type listType = new TypeToken<List<StructureData>>() {
            }.getType();
            List<StructureData> dataList = gson.fromJson(reader, listType);
            List<Structure> structures = new ArrayList<>();
            for (StructureData data : dataList) {
                if (data.getName() != null) {
                    structures.add(StructureFactory.createStructure(data));
                }
            }
            cachedStructures = structures;
            return cachedStructures;
        }
    }
}