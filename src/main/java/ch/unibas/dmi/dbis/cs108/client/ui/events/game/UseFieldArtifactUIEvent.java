package ch.unibas.dmi.dbis.cs108.client.ui.events.game;

import ch.unibas.dmi.dbis.cs108.client.ui.events.UIEvent;

public class UseFieldArtifactUIEvent implements UIEvent {
    private final int x;
    private final int y;
    private final int artifactId;
    private final String useType;

    public UseFieldArtifactUIEvent(int x, int y, int artifactId, String useType) {
        this.x = x;
        this.y = y;
        this.artifactId = artifactId;
        this.useType = useType;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getArtifactId() {
        return artifactId;
    }

    public String getUseType() {
        return useType;
    }

    @Override
    public String getType() {
        return "USE_FIELD_ARTIFACT";
    }
}
