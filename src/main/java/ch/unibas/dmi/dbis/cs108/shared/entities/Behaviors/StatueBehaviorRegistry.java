package ch.unibas.dmi.dbis.cs108.shared.entities.Behaviors;

import ch.unibas.dmi.dbis.cs108.SETTINGS;
import ch.unibas.dmi.dbis.cs108.server.core.model.GameState;
import ch.unibas.dmi.dbis.cs108.shared.entities.EntityRegistry;
import ch.unibas.dmi.dbis.cs108.shared.entities.Findables.Artifact;
import ch.unibas.dmi.dbis.cs108.shared.entities.Findables.Monument;
import ch.unibas.dmi.dbis.cs108.shared.entities.GameEntity;
import ch.unibas.dmi.dbis.cs108.shared.entities.Purchasables.PurchasableEntity;
import ch.unibas.dmi.dbis.cs108.shared.entities.Purchasables.Statues.Statue;
import ch.unibas.dmi.dbis.cs108.shared.entities.Purchasables.Statues.StatueParameterRequirement;
import ch.unibas.dmi.dbis.cs108.shared.entities.Purchasables.Statues.StatueParameters;
import ch.unibas.dmi.dbis.cs108.shared.entities.Purchasables.Structure;
import ch.unibas.dmi.dbis.cs108.shared.game.Player;
import ch.unibas.dmi.dbis.cs108.shared.game.Tile;
import ch.unibas.dmi.dbis.cs108.shared.utils.RandomGenerator;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Registry for statue behaviors based on their name and effect type.
 * <p>
 * This registry manages different types of statue behaviors for each statue:
 * - Level 1: No effect
 * - Level 2: Deal effect
 * - Level 3: Blessing (high probability) or Curse (low probability)
 * </p>
 * <p>
 * Each effect has specific parameter requirements that are validated
 * before execution.
 * </p>
 */
public class StatueBehaviorRegistry {

    /** Map: Statue Name -> (Map: Effect Type -> Behavior) */
    private final Map<String, Map<StatueEffectType, StatueBehavior>> behaviors = new HashMap<>();
    /** Map: Statue Name -> (Map: Effect Type -> Parameter Requirements) */
    private final Map<String, Map<StatueEffectType, StatueParameterRequirement>> requirements = new HashMap<>();
    /** Object of type random */
    private final Random random = new Random();

    /**
     * Initializes the registry with default behaviors for all statues.
     */
    public StatueBehaviorRegistry() {
        initializeDefaultBehaviors();
    }

    /**
     * Determines which effect type to use based on statue level.
     * Level 1: NONE
     * Level 2: DEAL
     * Level 3: BLESSING (high probability) or CURSE (low probability)
     *
     * @param statue The statue being used
     * @return The effect type to apply
     */
    public StatueEffectType determineEffectType(Statue statue) {
        int level = statue.getLevel();

        if (level == 1) {
            return StatueEffectType.NONE;
        } else if (level == 2) {
            return StatueEffectType.DEAL;
        } else if (level == 3) {
            return random.nextDouble() < (1 - (double) SETTINGS.Config.CHANCE_FOR_CURSE.getValue() / 100) ? StatueEffectType.BLESSING : StatueEffectType.CURSE;
        }

        return StatueEffectType.NONE;
    }

    /**
     * Registers a behavior for a statue with a specific effect type.
     *
     * @param statueName  The name of the statue
     * @param effectType  The effect type (DEAL, BLESSING, CURSE)
     * @param behavior    The behavior implementation
     * @param requirement The parameter requirements for this behavior
     */
    public void registerBehavior(String statueName, StatueEffectType effectType,
                                 StatueBehavior behavior, StatueParameterRequirement requirement) {
        // Initialize maps if not present
        behaviors.computeIfAbsent(statueName, k -> new HashMap<>())
                .put(effectType, behavior);

        requirements.computeIfAbsent(statueName, k -> new HashMap<>())
                .put(effectType, requirement);
    }

    /**
     * Executes a statue's effect with the provided parameters.
     *
     * @param statue    The statue being used
     * @param gameState The current game state
     * @param player    The player using the statue
     * @param params    The parameters for the effect
     * @return true if execution was successful, false otherwise
     */
    public boolean executeStatue(Statue statue, GameState gameState, Player player, StatueParameters params) {
        String statueName = statue.getName();
        StatueEffectType effectType = determineEffectType(statue);

        // Level 1 statues have no effect and disabled statues cannot be used.
        if (effectType == StatueEffectType.NONE || statue.isDisabled()) {
            return false;
        }

        // Get behavior and requirements for this statue and effect type
        Map<StatueEffectType, StatueBehavior> statueBehaviors = behaviors.get(statueName);
        Map<StatueEffectType, StatueParameterRequirement> statueRequirements = requirements.get(statueName);

        if (statueBehaviors == null || statueRequirements == null) {
            return false;
        }

        StatueBehavior behavior = statueBehaviors.get(effectType);
        StatueParameterRequirement requirement = statueRequirements.get(effectType);

        if (behavior == null || requirement == null) {
            return false;
        }

        // Validate parameters against requirements
        if (!params.satisfiesRequirements(requirement)) {
            return false;
        }

        // Execute the behavior
        boolean success = behavior.execute(statue, gameState, player, params);

        // If the behavior is a curse, then the client should get a notification about it
        if (success && effectType == StatueEffectType.CURSE) {
            gameState.sendNotification(player.getName(), "CURSE$" + statueName);
        }

        return success;
    }

    /**
     * Gets the parameter requirements for a specific statue and effect type.
     *
     * @param statueName The name of the statue
     * @param effectType The effect type
     * @return The parameter requirements or null if not registered
     */
    public StatueParameterRequirement getRequirements(String statueName, StatueEffectType effectType) {
        Map<StatueEffectType, StatueParameterRequirement> statueRequirements = requirements.get(statueName);
        return statueRequirements != null ? statueRequirements.get(effectType) : null;
    }

    /**
     * Initializes default behaviors for all statues based on statues.json.
     */
    private void initializeDefaultBehaviors() {
        // Jörmungandr
        registerBehavior("Jörmungandr", StatueEffectType.DEAL,
                (statue, gameState, player, params) -> {
                    // Destroys 1 random structure of a chosen player: sacrifices 1 structure of your own
                    Player targetPlayer = params.getTargetPlayer();
                    int x = params.getX();
                    int y = params.getY();
                    Tile tile = gameState.getBoardManager().getTile(x, y);
                    if (targetPlayer == null || tile == null) return false;
                    if (!tile.hasEntity() || tile.getEntity().isStructure()) return false;
                    Structure structure = (Structure) tile.getEntity();

                    player.removePurchasableEntity(structure);
                    tile.setEntity(null);

                    Tile targetTile = RandomGenerator.pickRandomElement(targetPlayer.getTilesWithStructures());

                    targetPlayer.removePurchasableEntity((PurchasableEntity) targetTile.getEntity());
                    targetTile.setEntity(null);
                    return true;
                },
                new StatueParameterRequirement(StatueParameterRequirement.StatueParameterType.PLAYER, StatueParameterRequirement.StatueParameterType.TILE)
        );

        // Freyr
        registerBehavior("Freyr", StatueEffectType.DEAL,
                (statue, gameState, player, params) -> {
                    // Grows 1 Tree on a River Tile: costs all available Energy (min 1 Energy)
                    int x = params.getX();
                    int y = params.getY();
                    Tile tile = gameState.getBoardManager().getTile(x, y);
                    if (tile == null || player.getEnergy() == 0) return false;

                    Structure structure = EntityRegistry.getStructure(7); // Tree has id 7
                    tile.setEntity(structure); // places the tree

                    player.addPurchasableEntity(structure);

                    return true;
                },
                new StatueParameterRequirement(StatueParameterRequirement.StatueParameterType.TILE)
        );

        registerBehavior("Freyr", StatueEffectType.BLESSING,
                (statue, gameState, player, params) -> {
                    // Grows Trees on all River Tiles
                    List<Tile> tiles = gameState.getBoardManager().getRiverTiles();
                    for (Tile tile : tiles) {
                        Structure structure = EntityRegistry.getStructure(7); // Tree has id 7
                        tile.setEntity(structure);
                        if (player.getOwnedTiles().contains(tile)) {
                            player.addPurchasableEntity(structure); // If the player owns the tile, he should also own the placed tree
                        }
                    }
                    return true;
                },
                new StatueParameterRequirement(StatueParameterRequirement.StatueParameterType.TILE)
        );

        registerBehavior("Freyr", StatueEffectType.CURSE,
                (statue, gameState, player, params) -> {
                    // Overgrows the Freyr Statue so it cannot be used anymore and blocks the tile
                    Tile tile = player.getOwnedTiles().stream().
                            filter(t -> t.getEntity() != null && t.getEntity().getName().equals("Freyr"))
                            .findFirst().orElse(null); // Finds the Freyr Statue in the players Owned Tiles

                    if (tile == null) return false;
                    tile.setEntity(EntityRegistry.getStructure(8));
                    tile.setResourceValue(0);
                    player.removePurchasableEntity(statue);

                    return true;
                },
                new StatueParameterRequirement()
        );

        // Dwarf
        registerBehavior("Dwarf", StatueEffectType.DEAL,
                (statue, gameState, player, params) -> {
                    // Smeltery: produces +1 buff next Round: 1 random Structure does not produce Runes next round
                    int x = params.getX();
                    int y = params.getY();
                    Tile tile = gameState.getBoardManager().getTile(x, y);
                    Structure structure = (Structure) tile.getEntity();
                    if (structure == null || !"Surtur's Smeltery".equals(structure.getName())) return false;

                    structure.setParam(0, structure.getParams().get(0).getValue() + 1); // Increase number of buffs by 1

                    Structure targetStructure = RandomGenerator.pickRandomElement(player.getStructures());
                    targetStructure.setRessourceValue(0);
                    return true;
                },
                new StatueParameterRequirement(StatueParameterRequirement.StatueParameterType.TILE)
        );

        registerBehavior("Dwarf", StatueEffectType.BLESSING,
                (statue, gameState, player, params) -> {
                    // Next Artifact from Smeltery debuffs all Players
                    int x = params.getX();
                    int y = params.getY();
                    Tile tile = gameState.getBoardManager().getTile(x, y);
                    Structure structure = (Structure) tile.getEntity();
                    if (structure == null || !"Surtur's Smeltery".equals(structure.getName())) return false;

                    structure.setParam(1, 1); // Set debuffOtherPlayers to 1.0 = true

                    return true;
                },
                new StatueParameterRequirement(StatueParameterRequirement.StatueParameterType.TILE)
        );

        registerBehavior("Dwarf", StatueEffectType.CURSE,
                (statue, gameState, player, params) -> {
                    // Destroys the Smeltery
                    Tile tile = player.getOwnedTiles().stream().
                            filter(t -> t.getEntity() != null && t.getEntity().getName().equals("Surtur's Smeltery"))
                            .findFirst().orElse(null); // Finds the Freyr Statue in the players Owned Tiles

                    if (!player.getOwnedTiles().contains(tile) || tile == null) return false;
                    Structure structure = (Structure) tile.getEntity();
                    if (structure == null || !"Surtur's Smeltery".equals(structure.getName())) return false;

                    tile.removeEntity();
                    player.removePurchasableEntity(structure);
                    return true;
                },
                new StatueParameterRequirement()
        );

        // Freyja
        registerBehavior("Freyja", StatueEffectType.DEAL,
                (statue, gameState, player, params) -> {
                    // Gives 1 random Artifact: costs (a lot of) Runes
                    if (!player.buy((int) statue.getParams().get(0).getValue())) return false;

                    player.addArtifact(EntityRegistry.getRandomArtifact());
                    return true;
                },
                new StatueParameterRequirement()
        );

        registerBehavior("Freyja", StatueEffectType.BLESSING,
                (statue, gameState, player, params) -> {
                    // 1 Tile of choice for free
                    int x = params.getX();
                    int y = params.getY();
                    Tile tile = gameState.getBoardManager().getTile(x, y);
                    if (tile == null || tile.isPurchased()) return false;

                    player.addOwnedTile(tile);

                    // Implementation of effect
                    return true;
                },
                new StatueParameterRequirement(StatueParameterRequirement.StatueParameterType.TILE)
        );

        registerBehavior("Freyja", StatueEffectType.CURSE,
                (statue, gameState, player, params) -> {
                    // Conquers a Tile in possession and makes it unusable
                    Tile tile = player.getOwnedTiles().stream().
                            filter(t -> t.getEntity() == null)
                            .findFirst().orElse(null); // Finds the Freyr Statue in the players Owned Tiles

                    if (tile == null) return false;
                    tile.setEntity(EntityRegistry.getStructure(8));
                    tile.setResourceValue(0);
                    player.removePurchasableEntity(statue);

                    return true;
                },
                new StatueParameterRequirement(StatueParameterRequirement.StatueParameterType.TILE)
        );

        // Hel
        registerBehavior("Hel", StatueEffectType.DEAL,
                (statue, gameState, player, params) -> {
                    // Blocks the Statue of a chosen Player next Round: blocks a random Structure of your own next Round
                    Player targetPlayer = params.getTargetPlayer();

                    Tile tile = targetPlayer.getOwnedTiles().stream()
                            .filter(t -> t.getEntity() != null && t.getEntity().isStatue())
                            .findFirst().orElse(null);

                    if (tile == null) return false;

                    Statue statue1 = (Statue) tile.getEntity();
                    statue1.disable(1); // Block statue for the next turn

                    PurchasableEntity purchasableEntity = RandomGenerator.pickRandomElement(player.getPurchasableEntities());
                    purchasableEntity.disable(2); // Block random entity for current and the next turn

                    return true;
                },
                new StatueParameterRequirement(StatueParameterRequirement.StatueParameterType.PLAYER)
        );

        registerBehavior("Hel", StatueEffectType.BLESSING,
                (statue, gameState, player, params) -> {
                    // Destroys the statue of a random Player
                    Player targetPlayer = player;
                    while (targetPlayer == player) { // Make sure the player does not destroy his own statue
                        targetPlayer = RandomGenerator.pickRandomElement(gameState.getPlayers());
                    }

                    Tile tile = targetPlayer.getOwnedTiles().stream().filter(t -> t.hasEntity() && t.getEntity().isStatue()).findFirst().orElse(null);
                    if (tile == null) return false;
                    Statue targetStatue = (Statue) tile.getEntity();

                    targetPlayer.removePurchasableEntity(targetStatue);
                    tile.setEntity(null);
                    return true;
                },
                new StatueParameterRequirement()
        );

        registerBehavior("Hel", StatueEffectType.CURSE,
                (statue, gameState, player, params) -> {
                    // Goes back to Helheim and the Statue gets destroyed
                    Tile tile = player.getOwnedTiles().stream().filter(t -> t.hasEntity() && t.getEntity().isStatue()).findFirst().orElse(null);
                    if (tile == null) return false;
                    Statue targetStatue = (Statue) tile.getEntity();

                    player.removePurchasableEntity(targetStatue);
                    tile.setEntity(null);
                    return true;
                },
                new StatueParameterRequirement()
        );

        // Nidhöggr
        registerBehavior("Nidhöggr", StatueEffectType.DEAL,
                (statue, gameState, player, params) -> {
                    // Devours a random Tree on the Board: needs to be fed 1 Artifacts
                    Tile target = RandomGenerator.pickRandomElement(gameState.getBoardManager().getRiverTiles().stream().filter(t -> t.hasEntity() && t.getEntity().getId() == 7).toArray(Tile[]::new));

                    if (target == null) return false;

                    Structure tree = (Structure) target.getEntity();
                    Player targetPlayer = gameState.getPlayers().stream().filter(p -> Objects.equals(p.getName(), target.getOwner())).findFirst().orElse(null);
                    if (targetPlayer != null) {
                        target.setEntity(null);
                        player.removePurchasableEntity(tree);
                    }
                    player.removeArtifact(params.getArtifact());
                    return true;
                },
                new StatueParameterRequirement(StatueParameterRequirement.StatueParameterType.ARTIFACT)
        );

        registerBehavior("Nidhöggr", StatueEffectType.BLESSING,
                (statue, gameState, player, params) -> {
                    // Devours all Trees on the Board
                    gameState.getBoardManager().getRiverTiles().forEach(tile -> {
                        if (tile.getEntity() != null && tile.getEntity().isStructure() && tile.getEntity().getId() == 7) { // check if tile holds a tree
                            Structure tree = (Structure) tile.getEntity();
                            Player target = gameState.getPlayers().stream().filter(p -> Objects.equals(p.getName(), tile.getOwner())).findFirst().orElse(null);
                            if (target != null) {
                                tile.setEntity(null);
                                player.removePurchasableEntity(tree);
                            }
                        }
                    });
                    return true;
                },
                new StatueParameterRequirement()
        );

        registerBehavior("Nidhöggr", StatueEffectType.CURSE,
                (statue, gameState, player, params) -> {
                    // Devours 2 structures of yours randomly
                    Tile[] tiles = player.getOwnedTiles().stream().filter(t -> t.hasEntity() && t.getEntity().isStructure()).toArray(Tile[]::new);
                    Tile tile1 = RandomGenerator.pickRandomElement(tiles);
                    if (tile1 == null) return false;
                    player.removePurchasableEntity((Structure) tile1.getEntity());
                    Tile tile2 = RandomGenerator.pickRandomElement(tiles);
                    if (tile2 == null) return false;
                    player.removePurchasableEntity((Structure) tile2.getEntity());
                    return true;
                },
                new StatueParameterRequirement()
        );

        // Loki
        registerBehavior("Loki", StatueEffectType.DEAL,
                (statue, gameState, player, params) -> {
                    // Sets 1 Trap for a chosen Player: steals 1 Artefact in return
                    Player targetPlayer = params.getTargetPlayer();
                    if (targetPlayer == null) return false;
                    Artifact targetArtifact = RandomGenerator.pickRandomElement(targetPlayer.getArtifacts());
                    Artifact yourArtifact = RandomGenerator.pickRandomElement(player.getArtifacts());
                    if (yourArtifact != null) {
                        player.removeArtifact(yourArtifact);
                    }
                    if (targetArtifact != null) {
                        targetPlayer.removeArtifact(targetArtifact);
                        player.addArtifact(targetArtifact);
                    }
                    if (yourArtifact != null) {
                        targetPlayer.addArtifact(yourArtifact);
                    }
                    return true;
                },
                new StatueParameterRequirement(
                        StatueParameterRequirement.StatueParameterType.PLAYER
                )
        );

        registerBehavior("Loki", StatueEffectType.BLESSING,
                (statue, gameState, player, params) -> {
                    // Steals a set amount of Runes from another Player and gives them to you
                    Player targetPlayer = params.getTargetPlayer();
                    int runes = (int) statue.getParams().get(0).getValue();
                    int removedRunes = targetPlayer.addRunes(runes);
                    player.addRunes(removedRunes);
                    return true;
                },
                new StatueParameterRequirement(StatueParameterRequirement.StatueParameterType.PLAYER)
        );

        registerBehavior("Loki", StatueEffectType.CURSE,
                (statue, gameState, player, params) -> {
                    // Sets 2 traps for yourself
                    AtomicInteger i = new AtomicInteger((int) statue.getParams().get(1).getValue());
                    player.getOwnedTiles().forEach(tile -> {
                        if (tile.getEntity() == null && i.get() > 0) {
                            tile.setEntity(EntityRegistry.getStructure(8)); // Place an active trap on an empty tile twice
                            i.getAndDecrement();
                        }
                    });
                    return true;
                },
                new StatueParameterRequirement()
        );

        // Surtr
        registerBehavior("Surtr", StatueEffectType.DEAL,
                (statue, gameState, player, params) -> {
                    // Destroys 1 random Structure or Statue of a chosen Player: consumes the Flaming Sword Structure

                    Player targetPlayer = params.getTargetPlayer();
                    Tile target = RandomGenerator.pickRandomElement(targetPlayer.getOwnedTiles().stream().filter(t -> t.hasEntity() && (t.getEntity().isStructure() || t.getEntity().isStatue())).toArray(Tile[]::new));
                    assert target != null;
                    GameEntity entity = target.getEntity();
                    target.setEntity(null);
                    targetPlayer.removePurchasableEntity((PurchasableEntity) entity);

                    // Check if player has Flaming Sword
                    Tile sword = player.getOwnedTiles().stream().filter(t -> t.getEntity() != null && t.getEntity().isMonument() && t.getWorld().equals("Muspelheim")).findFirst().orElse(null);
                    assert sword != null;
                    Monument swordMonument = (Monument) sword.getEntity();

                    // Consume flaming sword from player, should still exist as an entity, since tile should still be blocked
                    player.removeMonument(swordMonument);

                    return true;
                },
                new StatueParameterRequirement(StatueParameterRequirement.StatueParameterType.PLAYER)
        );

        registerBehavior("Surtr", StatueEffectType.BLESSING,
                (statue, gameState, player, params) -> {
                    // Gifts 2 Tiles of Muspelheim for free
                    int i = 0;
                    Tile[][] boardTiles = gameState.getBoardManager().getBoard().getTiles();
                    for (Tile[] tile : boardTiles) {
                        for (Tile t : tile) {
                            if (i == 2) break;
                            if (t.getOwner() == null && t.getWorld().equals("Muspelheim")) player.addOwnedTile(t);
                        }
                    }
                    return true;
                },
                new StatueParameterRequirement()
        );

        registerBehavior("Surtr", StatueEffectType.CURSE,
                (statue, gameState, player, params) -> {
                    // Destroys 1 random Structure or Statue of your own
                    Tile target = RandomGenerator.pickRandomElement(player.getOwnedTiles().stream().filter(t -> t.hasEntity() && (t.getEntity().isStructure() || t.getEntity().isStatue())).toArray(Tile[]::new));
                    assert target != null;
                    GameEntity entity = target.getEntity();
                    target.setEntity(null);
                    player.removePurchasableEntity((PurchasableEntity) entity);
                    return true;
                },
                new StatueParameterRequirement()
        );
    }

    /**
     * Effect types for statues based on their level.
     */
    public enum StatueEffectType {
        /** No effect */
        NONE,      // Level 1: no effect
        /** Deal effect */
        DEAL,      // Level 2: deal effect
        /** Blessing effect */
        BLESSING,  // Level 3: positive effect (high probability)
        /** Curse effect */
        CURSE      // Level 3: negative effect (low probability)
    }

    /**
     * Functional interface for statue behaviors.
     */
    @FunctionalInterface
    public interface StatueBehavior {
        /**
         * Executes the statue effect.
         *
         * @param statue    The statue being used
         * @param gameState The current game state
         * @param player    The player using the statue
         * @param params    Parameters for the effect
         * @return true if execution was successful, false otherwise
         */
        boolean execute(Statue statue, GameState gameState, Player player, StatueParameters params);
    }
}