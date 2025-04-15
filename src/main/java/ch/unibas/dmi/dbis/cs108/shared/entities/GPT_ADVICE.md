# Statue Implementation with Variable Selection Types

To implement statues with different selection requirements, I'll design a system that handles choosing tiles, players, or multiple targets. Here's a comprehensive approach:

## 1. Define a StatueSelectionType enum

```java
package ch.unibas.dmi.dbis.cs108.shared.entities;

public enum StatueSelectionType {
    NONE,       // No selection needed
    PLAYER,     // Select another player
    TILE,       // Select a tile
    STRUCTURE,  // Select a structure
    SMELTERY,   // Specifically for smeltery (could be a special case of STRUCTURE)
    MIXED       // Multiple selections needed
}
```

## 2. Enhance the Statue class

```java
package ch.unibas.dmi.dbis.cs108.shared.entities.Purchasables;

import ch.unibas.dmi.dbis.cs108.shared.entities.GameEntity;
import ch.unibas.dmi.dbis.cs108.shared.entities.StatueSelectionType;
import com.google.gson.JsonObject;

public class Statue extends GameEntity {
    private String deal;
    private String blessing;
    private String curse;
    private int price;
    private int upgradePrice;
    private StatueSelectionType selectionType;

    @Override
    protected void loadFromJson(JsonObject json) {
        super.loadFromJson(json);
        this.deal = json.get("deal").getAsString();
        this.blessing = json.get("blessing").getAsString();
        this.curse = json.get("curse").getAsString();
        this.price = json.get("price").getAsInt();
        this.upgradePrice = json.get("upgradePrice").getAsInt();
        
        // Parse selection type from useType
        if (json.has("useType")) {
            String useType = json.get("useType").getAsString();
            switch (useType) {
                case "Player": selectionType = StatueSelectionType.PLAYER; break;
                case "Tile": selectionType = StatueSelectionType.TILE; break;
                case "Smeltery": selectionType = StatueSelectionType.SMELTERY; break;
                case "mix": selectionType = StatueSelectionType.MIXED; break;
                default: selectionType = StatueSelectionType.NONE;
            }
        } else {
            selectionType = StatueSelectionType.NONE;
        }
    }

    public String getDeal() { return deal; }
    public String getBlessing() { return blessing; }
    public String getCurse() { return curse; }
    public int getPrice() { return price; }
    public int getUpgradePrice() { return upgradePrice; }
    public StatueSelectionType getSelectionType() { return selectionType; }
}
```

## 3. Create a StatueSelection class for networking

```java
package ch.unibas.dmi.dbis.cs108.shared.entities;

public class StatueSelection {
    private int statueId;
    private String playerName;    // Target player (if applicable)
    private String tileCoords;    // Target tile in format "x,y" (if applicable)
    private int structureId;      // Target structure ID (if applicable)
    
    // Static factory methods for different selection types
    public static StatueSelection forPlayer(int statueId, String playerName) {
        StatueSelection selection = new StatueSelection();
        selection.statueId = statueId;
        selection.playerName = playerName;
        return selection;
    }
    
    public static StatueSelection forTile(int statueId, int x, int y) {
        StatueSelection selection = new StatueSelection();
        selection.statueId = statueId;
        selection.tileCoords = x + "," + y;
        return selection;
    }
    
    // Serialize/deserialize for network transmission
    public String serialize() {
        StringBuilder sb = new StringBuilder(String.valueOf(statueId));
        
        if (playerName != null) sb.append(":PLAYER:").append(playerName);
        if (tileCoords != null) sb.append(":TILE:").append(tileCoords);
        if (structureId > 0) sb.append(":STRUCTURE:").append(structureId);
        
        return sb.toString();
    }
    
    public static StatueSelection deserialize(String data) {
        String[] parts = data.split(":");
        if (parts.length < 3) return null;
        
        int statueId = Integer.parseInt(parts[0]);
        StatueSelection selection = new StatueSelection();
        selection.statueId = statueId;
        
        for (int i = 1; i < parts.length; i += 2) {
            String type = parts[i];
            String value = parts[i+1];
            
            switch (type) {
                case "PLAYER": selection.playerName = value; break;
                case "TILE": selection.tileCoords = value; break;
                case "STRUCTURE": selection.structureId = Integer.parseInt(value); break;
            }
        }
        
        return selection;
    }
    
    // Getters
    public int getStatueId() { return statueId; }
    public String getPlayerName() { return playerName; }
    public String getTileCoords() { return tileCoords; }
    public int getStructureId() { return structureId; }
}
```

## 4. Create a StatueBehaviorRegistry

```java
package ch.unibas.dmi.dbis.cs108.shared.entities;

import ch.unibas.dmi.dbis.cs108.server.core.logic.GameLogic;
import ch.unibas.dmi.dbis.cs108.shared.entities.Purchasables.Statue;
import ch.unibas.dmi.dbis.cs108.shared.game.GameState;
import ch.unibas.dmi.dbis.cs108.shared.game.Player;
import ch.unibas.dmi.dbis.cs108.shared.game.Tile;
import java.util.HashMap;
import java.util.Map;

public class StatueBehaviorRegistry {
    private final Map<String, StatueBehavior> behaviors = new HashMap<>();
    
    public StatueBehaviorRegistry() {
        initializeDefaultBehaviors();
    }
    
    private void initializeDefaultBehaviors() {
        // Register behaviors for each statue
        registerBehavior("Jörmungandr", (statue, gameState, player, selection) -> {
            // Implementation for Jörmungandr
            if (selection.getPlayerName() == null) return false;
            
            Player targetPlayer = gameState.getPlayerByName(selection.getPlayerName());
            if (targetPlayer == null) return false;
            
            // Destroy 1 random structure of target player
            // Sacrifice 1 structure of your own
            return true;
        });
        
        registerBehavior("Freyr", (statue, gameState, player, selection) -> {
            // Implementation for Freyr
            if (selection.getTileCoords() == null) return false;
            
            // Parse tile coordinates
            String[] coords = selection.getTileCoords().split(",");
            int x = Integer.parseInt(coords[0]);
            int y = Integer.parseInt(coords[1]);
            Tile tile = gameState.getTileAt(x, y);
            
            if (tile == null || !tile.isRiver()) return false;
            
            // Grow tree implementation
            // Use player's energy
            return true;
        });
        
        // Add more statue behaviors...
    }
    
    public void registerBehavior(String statueName, StatueBehavior behavior) {
        behaviors.put(statueName, behavior);
    }
    
    public boolean execute(Statue statue, GameState gameState, Player player, StatueSelection selection) {
        StatueBehavior behavior = behaviors.get(statue.getName());
        if (behavior != null) {
            return behavior.execute(statue, gameState, player, selection);
        }
        return false;
    }
    
    @FunctionalInterface
    public interface StatueBehavior {
        boolean execute(Statue statue, GameState gameState, Player player, StatueSelection selection);
    }
}
```

## 5. Network Protocol

Add these methods to your network handling class:

```java
public void handleStatueUse(String playerName, String selectionData) {
    StatueSelection selection = StatueSelection.deserialize(selectionData);
    if (selection == null) return;
    
    Player player = gameState.getPlayerByName(playerName);
    Statue statue = EntityRegistry.getStatue(selection.getStatueId());
    
    if (player != null && statue != null) {
        statueBehaviorRegistry.execute(statue, gameState, player, selection);
    }
}

// Client sends: "STATUE_USE:<selectionData>"
// Where selectionData is created with StatueSelection.serialize()
```

## 6. Client-Side UI Logic

```java
// Display different UI based on statue selection type
public void showStatueSelectionUI(Statue statue) {
    switch (statue.getSelectionType()) {
        case PLAYER:
            displayPlayerSelectionPanel();
            break;
        case TILE:
            enableTileSelectionMode();
            break;
        case MIXED:
            startMixedSelectionSequence();
            break;
        case SMELTERY:
            highlightSmelteryStructures();
            break;
        default:
            // No selection needed, use directly
            sendStatueUseCommand(statue.getId(), null);
    }
}
```

This implementation provides a flexible system for various statue behaviors while managing different selection requirements through a typed selection system and string-based networking.