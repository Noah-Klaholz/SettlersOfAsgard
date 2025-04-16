package ch.unibas.dmi.dbis.cs108.client.networking.protocol;

import java.util.Map;

public class StatueCommandBuilder {
    /**
     * Creates a command to use a statue on a tile.
     */
    public static String useStatueOnTile(int statueId, int x, int y) {
        return "USTA " + statueId + " TILE:" + x + "," + y;
    }

    /**
     * Creates a command to use a statue on a player.
     */
    public static String useStatueOnPlayer(int statueId, String playerName) {
        return "USTA " + statueId + " PLAYER:" + playerName;
    }

    /**
     * Creates a command to use a statue with a structure.
     */
    public static String useStatueWithStructure(int statueId, int structureId) {
        return "USTA " + statueId + " STRUCTURE:" + structureId;
    }

    /**
     * Creates a command to use a statue with an artifact.
     */
    public static String useStatueWithArtifact(int statueId, int artifactId) {
        return "USTA " + statueId + " ARTIFACT:" + artifactId;
    }

    /**
     * Creates a command to use a statue with complex parameters.
     */
    public static String useStatueComplex(int statueId, Map<String, String> params) {
        StringBuilder cmd = new StringBuilder("USTA " + statueId);

        for (Map.Entry<String, String> param : params.entrySet()) {
            cmd.append(" ").append(param.getKey()).append(":").append(param.getValue());
        }

        return cmd.toString();
    }

    public static String useStatue(int x, int y, int structureID) {
        return "";
    }
}