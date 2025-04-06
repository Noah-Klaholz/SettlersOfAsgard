package ch.unibas.dmi.dbis.cs108.server.core.entities;

public class Artifact {
    private final int artifactID;
    private final String name;
    private final String description;
    private final String useType;

    public Artifact(int artifactID, String name, String description, String useType) {
        this.artifactID = artifactID;
        this.name = name;
        this.description = description;
        this.useType = useType;
    }

    public int getArtifactID() {
        return artifactID;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getUseType() {
        return useType;
    }


}
