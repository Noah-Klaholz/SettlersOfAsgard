package ch.unibas.dmi.dbis.cs108.client.core.state;

import ch.unibas.dmi.dbis.cs108.shared.entities.Findables.Monument;
import ch.unibas.dmi.dbis.cs108.shared.entities.GameEntity;
import ch.unibas.dmi.dbis.cs108.shared.entities.Purchasables.Statues.Statue;
import ch.unibas.dmi.dbis.cs108.shared.entities.Purchasables.Structure;
import ch.unibas.dmi.dbis.cs108.shared.game.Board;
import ch.unibas.dmi.dbis.cs108.shared.game.Player;
import ch.unibas.dmi.dbis.cs108.shared.game.Status;
import ch.unibas.dmi.dbis.cs108.shared.game.Tile;
import ch.unibas.dmi.dbis.cs108.shared.entities.Findables.Artifact;
import ch.unibas.dmi.dbis.cs108.shared.entities.Purchasables.PurchasableEntity;
import ch.unibas.dmi.dbis.cs108.shared.entities.EntityRegistry;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * This class is responsible for managing a gameState.
 * It will update the gameState based on a message from the server.
 */
public class GameStateManager {

    /** The gameState object managed by this class */
    private GameState gameState;
    /** Logger to log logging*/
    private static final Logger LOGGER = Logger.getLogger(GameStateManager.class.getName());

    /**
     * Creates a new object of this class
     *
     * @param gameState the gameState to manage
     */
    public GameStateManager(GameState gameState) {
        this.gameState = gameState;
    }

    /**
     * Gets the current gameState
     * @return the gameState
     */
    public GameState getGameState() {
        return gameState;
    }

    /**
     * Sets the current gameState
     *
     * @param gameState the gameState to set
     */
    public void setGameState(GameState gameState) {
        this.gameState = gameState;
    }

    /**
     * Updates the gameState by parsing the message from the server.
     *
     * @param message the message from the server.
     * @see ch.unibas.dmi.dbis.cs108.server.core.model.GameStateSerializer
     */
    public void updateGameState(String message) {
        LOGGER.info("Updating GameState");
        if (message == null || !message.startsWith("SYNC$")) {
            LOGGER.warning("Invalid game state message: " + (message == null ? "null" : message));
            return;
        }

        // Format: META|PLAYERS|BOARD
        String[] sections = message.split("\\|", 3);
        if (sections.length < 3) {
            LOGGER.warning("Invalid message format, expected 3 sections but got " + sections.length);
            return;
        }

        gameState.getStateLock().writeLock().lock();
        try {
            gameState.reset();
            // Log each section before parsing to help with debugging
            LOGGER.fine("META section: " + sections[0]);
            parseMetaSection(sections[0]);

            LOGGER.fine("PLAYERS section: " + sections[1]);
            parsePlayersSection(sections[1]);

            LOGGER.fine("BOARD section: " + sections[2]);
            parseBoardSection(sections[2]);

            LOGGER.info("GameState successfully updated");
        } catch (Exception e) {
            LOGGER.severe("Error updating game state: " + e);
            e.printStackTrace(); // Log full stack trace
        } finally {
            gameState.getStateLock().writeLock().unlock();
        }
    }

    /**
     * Parse the meta-section of the string
     *
     * @param metaSection the first section of the message that contains metadata
     */
    private void parseMetaSection(String metaSection) {
        // Format: SYNC$META:gameRound,playerRound,playerTurn
        String[] parts = metaSection.substring(10).split(","); // Remove "SYNC$META:"
        if (parts.length >= 3) {
            try {
                gameState.setGameRound(Integer.parseInt(parts[0]));
                gameState.setPlayerRound(Integer.parseInt(parts[1]));
                gameState.setPlayerTurn(parts[2]);
            } catch (NumberFormatException e) {
                LOGGER.warning("Invalid meta format: " + metaSection);
            }
        }
    }

    /**
     * Parse the player section of the string
     *
     * @param playersSection the first section of the message that contains player data
     */
    private void parsePlayersSection(String playersSection) {
        // Format: PLAYERS:name1{...};name2{...};
        String[] playerEntries = playersSection.substring(8).split(";"); // Split on ; followed by name

        for (String entry : playerEntries) {
            if (entry.isEmpty()) continue;

            // Extract name and properties
            int braceOpen = entry.indexOf('{');
            if (braceOpen == -1) continue;

            String playerName = entry.substring(0, braceOpen);
            String props = entry.substring(braceOpen + 1, entry.length() - 1);

            // Create player
            Player player = new Player(playerName);
            gameState.addPlayer(player);

            // Parse properties
            for (String prop : props.split(",(?=[A-Z]{1,2}:)")) { // Split on commas before property codes
                String[] keyValue = prop.split(":", 2);
                if (keyValue.length != 2) continue;

                switch (keyValue[0]) {
                    case "R": player.setRunes(Integer.parseInt(keyValue[1])); break;
                    case "E": player.setEnergy(Integer.parseInt(keyValue[1])); break;
                    case "T":
                        List<Tile> ownedTiles = new ArrayList<>();
                        if (!keyValue[1].equals("[]")) {
                            for (String id : keyValue[1].substring(1, keyValue[1].length()-1).split(",")) {
                                Tile t = gameState.getBoardManager().getBoard().getTile(Integer.parseInt(id));
                                if (t != null) ownedTiles.add(t);
                            }
                        }
                        player.setOwnedTiles(ownedTiles);
                        break;
                    case "A":
                        List<Artifact> artifacts = new ArrayList<>();
                        if (!keyValue[1].equals("[]")) {
                            for (String id : keyValue[1].substring(1, keyValue[1].length()-1).split(",")) {
                                artifacts.add(EntityRegistry.getArtifact(Integer.parseInt(id)));
                            }
                        }
                        player.setArtifacts(artifacts);
                        break;
                    case "PE":
                        List<PurchasableEntity> entities = new ArrayList<>();
                        if (!keyValue[1].equals("[]")) {
                            for (String id : keyValue[1].substring(1, keyValue[1].length()-1).split(",")) {
                                entities.add(EntityRegistry.getPurchasableEntity(Integer.parseInt(id)));
                            }
                        }
                        player.setPurchasableEntities(entities);
                        break;
                    case "ST":
                        // Status buffs
                        String[] buffs = keyValue[1].substring(1, keyValue[1].length()-1).split(",");
                        for (String buff : buffs) {
                            String[] buffParts = buff.split(":");
                            double value = Double.parseDouble(buffParts[1]);
                            switch (buffParts[0]) {
                                case "RG": player.addBuff(Status.BuffType.RUNE_GENERATION, value - player.getStatus().get(Status.BuffType.RUNE_GENERATION)); break;
                                case "EG": player.addBuff(Status.BuffType.ENERGY_GENERATION, value - player.getStatus().get(Status.BuffType.ENERGY_GENERATION)); break;
                                case "RR": player.addBuff(Status.BuffType.RIVER_RUNE_GENERATION, value - player.getStatus().get(Status.BuffType.RIVER_RUNE_GENERATION)); break;
                                case "SP": player.addBuff(Status.BuffType.SHOP_PRICE, value - player.getStatus().get(Status.BuffType.SHOP_PRICE)); break;
                                case "AC": player.addBuff(Status.BuffType.ARTIFACT_CHANCE, value - player.getStatus().get(Status.BuffType.ARTIFACT_CHANCE)); break;
                                case "DB": player.addBuff(Status.BuffType.DEBUFFABLE, value > 0 ? 1 : -1); break;
                            }
                        }
                        break;
                }
            }
        }
    }

    /**
     * Parse the board section of the string
     *
     * @param boardSection the third section of the message that contains board data
     */
    private void parseBoardSection(String boardSection) {
        Board board = gameState.getBoardManager().getBoard();
        String[] tileEntries = boardSection.substring(6).split(";(?=\\d+,\\d+\\{)"); // Split on ; before coordinates

        for (String entry : tileEntries) {
            if (entry.isEmpty()) continue;

            // Extract coordinates and properties
            int braceOpen = entry.indexOf('{');
            String[] coords = entry.substring(0, braceOpen).split(",");
            int x = Integer.parseInt(coords[0]);
            int y = Integer.parseInt(coords[1]);
            String props = entry.substring(braceOpen + 1, entry.length() - 1);

            Tile tile = board.getTileByCoordinates(x, y);
            if (tile == null) continue;

            // First: handle entity-specific properties
            GameEntity entity = null;
            if (props.contains("STA")) {
                entity = parseStatue(props);
            }
            else if (props.contains("MON")) {
                entity = parseMonument(props);
            }
            else if (props.contains("STR")) {
                entity = parseStructure(props);
            }

            if (entity != null) {
                tile.setEntity(entity);
                tile.setHasEntity(true);
            }

            // Second: handle regular properties
            for (String prop : props.split(",(?=[A-Z]{1,2}:)")) {
                String[] keyValue = prop.split(":", 2);
                if (keyValue.length != 2) continue;

                switch (keyValue[0]) {
                    case "HE": tile.setHasEntity(keyValue[1].equals("1")); break;
                    case "O": tile.setOwner(keyValue[1].equals("null") ? null : keyValue[1]); break;
                    case "P": tile.setPrice(Integer.parseInt(keyValue[1])); break;
                    case "AR":
                        if (!keyValue[1].equals("null")) {
                            tile.setArtifact(EntityRegistry.getArtifact(Integer.parseInt(keyValue[1])));
                        } else {
                            tile.setArtifact(null);
                        }
                        break;
                    case "W": tile.setWorld(keyValue[1]); break;
                    case "PU": tile.setPurchased(keyValue[1].equals("1")); break;
                    case "RV": tile.setResourceValue(Integer.parseInt(keyValue[1])); break;
                    case "HR": tile.setHasRiver(keyValue[1].equals("1")); break;
                    case "ID": tile.setTileID(Integer.parseInt(keyValue[1])); break;
                    case "ST":
                        // Tile status buffs
                        String[] buffs = keyValue[1].substring(1, keyValue[1].length()-1).split(",");
                        for (String buff : buffs) {
                            String[] buffParts = buff.split(":");
                            double value = Double.parseDouble(buffParts[1]);
                            switch (buffParts[0]) {
                                case "RG": tile.addBuff(Status.BuffType.RUNE_GENERATION, value - tile.getStatus().get(Status.BuffType.RUNE_GENERATION)); break;
                                case "EG": tile.addBuff(Status.BuffType.ENERGY_GENERATION, value - tile.getStatus().get(Status.BuffType.ENERGY_GENERATION)); break;
                                case "RR": tile.addBuff(Status.BuffType.RIVER_RUNE_GENERATION, value - tile.getStatus().get(Status.BuffType.RIVER_RUNE_GENERATION)); break;
                                case "SP": tile.addBuff(Status.BuffType.SHOP_PRICE, value - tile.getStatus().get(Status.BuffType.SHOP_PRICE)); break;
                                case "AC": tile.addBuff(Status.BuffType.ARTIFACT_CHANCE, value - tile.getStatus().get(Status.BuffType.ARTIFACT_CHANCE)); break;
                                case "DB": tile.addBuff(Status.BuffType.DEBUFFABLE, value > 0 ? 1 : -1); break;
                            }
                        }
                        break;
                }
            }
        }
    }

    /**
     * Handles the properties of a statue on a tile.
     *
     * @param props the properties of the statue.
     * @return the Statue object
     */
    private Statue parseStatue(String props) {
        // Match format "STAid,DI<disabled>,AC<activated>,LV<level>"
        String staPattern = "STA\\d+,";
        if (props.contains("STA,")) {
            // Handle format with comma: "STA,id,DI<disabled>,AC<activated>,LV<level>"
            String[] parts = props.split("STA,")[1].split(",");
            int id = Integer.parseInt(parts[0]);
            int disabled = Integer.parseInt(parts[1].substring(2));
            boolean activated = Boolean.parseBoolean(parts[2].substring(2));
            int level = Integer.parseInt(parts[3].substring(2));

            Statue statue = EntityRegistry.getStatue(id);
            if (statue != null) {
                statue.setDisabled(disabled);
                statue.setActivated(activated);
                statue.setLevel(level);
            }
            return statue;
        } else {
            // Handle format without comma: "STAid,DI<disabled>,AC<activated>,LV<level>"
            String idStr = props.substring(props.indexOf("STA") + 3, props.indexOf(",DI"));
            int id = Integer.parseInt(idStr);

            // Extract other properties
            String[] segments = props.split(",");
            int disabled = 0;
            boolean activated = false;
            int level = 1;

            for (String segment : segments) {
                if (segment.startsWith("DI")) {
                    disabled = Integer.parseInt(segment.substring(2));
                } else if (segment.startsWith("AC")) {
                    activated = segment.substring(2).equalsIgnoreCase("true") || segment.substring(2).equals("1");
                } else if (segment.startsWith("LV")) {
                    level = Integer.parseInt(segment.substring(2));
                }
            }

            Statue statue = EntityRegistry.getStatue(id);
            if (statue != null) {
                statue.setDisabled(disabled);
                statue.setActivated(activated);
                statue.setLevel(level);
            }
            return statue;
        }
    }

    /**
     * Handles the properties of a monument on a tile.
     *
     * @param props the properties of the monument.
     * @return the monument object
     */
    private Monument parseMonument(String props) {
        if (props.contains("MON,")) {
            // Format with comma: "MON,id,DI<disabled>"
            String[] parts = props.split("MON,")[1].split(",");
            int id = Integer.parseInt(parts[0]);
            int disabled = Integer.parseInt(parts[1].substring(2));

            Monument monument = EntityRegistry.getMonument(id);
            if (monument != null) {
                monument.setDisabled(disabled);
            }
            return monument;
        } else {
            // Format without comma: "MONid,DI<disabled>"
            String idStr = props.substring(props.indexOf("MON") + 3, props.indexOf(",DI"));
            int id = Integer.parseInt(idStr);

            // Extract disabled property
            String disabledStr = props.substring(props.indexOf("DI") + 2);
            if (disabledStr.contains(",")) {
                disabledStr = disabledStr.substring(0, disabledStr.indexOf(","));
            }
            int disabled = Integer.parseInt(disabledStr);

            Monument monument = EntityRegistry.getMonument(id);
            if (monument != null) {
                monument.setDisabled(disabled);
            }
            return monument;
        }
    }

    /**
     * Handles the properties of a Structure on a tile.
     *
     * @param props the properties of the Structure.
     * @return the Structure object
     */
    private Structure parseStructure(String props) {
        if (props.contains("STR,")) {
            // Format with comma: "STR,id,DI<disabled>,AC<activated>"
            String[] parts = props.split("STR,")[1].split(",");
            int id = Integer.parseInt(parts[0]);
            int disabled = Integer.parseInt(parts[1].substring(2));
            boolean activated = parts[2].substring(2).equalsIgnoreCase("true") || parts[2].substring(2).equals("1");

            Structure structure = EntityRegistry.getStructure(id);
            if (structure != null) {
                structure.setDisabled(disabled);
                structure.setActivated(activated);
            }
            return structure;
        } else {
            // Format without comma: "STRid,DI<disabled>,AC<activated>"
            String idStr = props.substring(props.indexOf("STR") + 3, props.indexOf(",DI"));
            int id = Integer.parseInt(idStr);

            // Extract other properties
            String[] segments = props.split(",");
            boolean disabled = false;
            boolean activated = false;

            for (String segment : segments) {
                if (segment.startsWith("DI")) {
                    disabled = Boolean.parseBoolean(segment.substring(2));
                } else if (segment.startsWith("AC")) {
                    activated = segment.substring(2).equalsIgnoreCase("true") || segment.substring(2).equals("1");
                }
            }

            Structure structure = EntityRegistry.getStructure(id);
            if (structure != null) {
                structure.setDisabled(disabled ? 1 : 0);
                structure.setActivated(activated);
            }
            return structure;
        }
    }
}