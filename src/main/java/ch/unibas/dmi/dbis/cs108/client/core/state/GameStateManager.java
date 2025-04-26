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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
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
        Logger.getGlobal().info("Updateing GameState");
        if (message == null || !message.startsWith("SYNC$")) {
            LOGGER.warning("Invalid game state message");
            return;
        }
        // Format: META|PLAYERS|BOARD
        String[] sections = message.split("\\|", 3); // Split into max 3 parts
        if (sections.length < 3) return;

        gameState.getStateLock().writeLock().lock();
        try {
            // 1. Parse Meta Section
            parseMetaSection(sections[0]);

            // 2. Parse Players Section
            parsePlayersSection(sections[1]);

            // 3. Parse Board Section
            parseBoardSection(sections[2]);

            Logger.getGlobal().info("GameState updated:");
        } catch (Exception e) {
            LOGGER.warning("Error updating game state: " + e.getMessage());
        } finally {
            gameState.getStateLock().writeLock().unlock();
        }
    }

    /**
     * Parse the meta section of the string
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
        // Format: PLAYERS:uuid1{...};uuid2{...};
        String[] playerEntries = playersSection.substring(8).split(";(?=\\w{8}-)"); // Split on ; followed by UUID
        Map<UUID, Player> existingPlayers = new HashMap<>();
        gameState.getPlayers().forEach(p -> existingPlayers.put(p.getPlayerID(), p));

        for (String entry : playerEntries) {
            if (entry.isEmpty()) continue;

            // Extract UUID and properties
            int braceOpen = entry.indexOf('{');
            UUID playerId = UUID.fromString(entry.substring(0, braceOpen));
            String props = entry.substring(braceOpen + 1, entry.length() - 1);

            // Get or create player
            Player player = existingPlayers.get(playerId);
            if (player == null) {
                player = new Player(""); // Temporary name
                gameState.getPlayers().add(player);
            }

            // Parse properties
            for (String prop : props.split(",(?=[A-Z]{1,2}:)")) { // Split on commas before property codes
                String[] keyValue = prop.split(":", 2);
                if (keyValue.length != 2) continue;

                switch (keyValue[0]) {
                    case "N": player.setName(keyValue[1]); break;
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

        // Remove players not in the update
        List<Player> toRemove = new ArrayList<>();
        for (Player p : gameState.getPlayers()) {
            if (!existingPlayers.containsKey(p.getPlayerID())) {
                toRemove.add(p);
            }
        }
        gameState.getPlayers().removeAll(toRemove);
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
                    case "AR":
                        if (!keyValue[1].equals("null")) {
                            tile.setArtifact(EntityRegistry.getArtifact(Integer.parseInt(keyValue[1])));
                        } else {
                            tile.setArtifact(null);
                        }
                        break;
                    case "PU": tile.setPurchased(keyValue[1].equals("1")); break;
                    case "RV": tile.setResourceValue(Integer.parseInt(keyValue[1])); break;
                    case "HR": tile.setHasRiver(keyValue[1].equals("1")); break;
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
        String[] parts = props.split("STA,")[1].split(","); // Split after "STA,"
        // Format: STA,id,DI<disabled>,AC<activated>,LV<level>
        int id = Integer.parseInt(parts[0]);
        int disabled = Integer.parseInt(parts[1].substring(2));
        boolean activated = parts[2].substring(2).equals("1");
        int level = Integer.parseInt(parts[3].substring(2));

        Statue statue = EntityRegistry.getStatue(id);
        if (statue != null) {
            statue.setDisabled(disabled);
            statue.setActivated(activated);
            statue.setLevel(level);
        }
        return statue;
    }

    /**
     * Handles the properties of a monument on a tile.
     *
     * @param props the properties of the monument.
     * @return the monument object
     */
    private Monument parseMonument(String props) {
        String[] parts = props.split("MON,")[1].split(","); // Split after "MON,"
        // Format: MON,id,DI<disabled>
        int id = Integer.parseInt(parts[0]);
        int disabled = Integer.parseInt(parts[1].substring(2));

        Monument monument = EntityRegistry.getMonument(id);
        if (monument != null) {
            monument.setDisabled(disabled);
        }
        return monument;
    }

    /**
     * Handles the properties of a Structure on a tile.
     *
     * @param props the properties of the Structure.
     * @return the Structure object
     */
    private Structure parseStructure(String props) {
        String[] parts = props.split("STR,")[1].split(","); // Split after "STR,"
        // Format: STR,id,DI<disabled>,AC<activated>
        int id = Integer.parseInt(parts[0]);
        int disabled = Integer.parseInt(parts[1].substring(2));
        boolean activated = parts[2].substring(2).equals("1");

        Structure structure = EntityRegistry.getStructure(id);
        if (structure != null) {
            structure.setDisabled(disabled);
            structure.setActivated(activated);
        }
        return structure;
    }
}
