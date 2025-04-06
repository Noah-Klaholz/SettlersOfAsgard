package ch.unibas.dmi.dbis.cs108.server.core.State;

import ch.unibas.dmi.dbis.cs108.server.core.entities.Artefact;
import ch.unibas.dmi.dbis.cs108.server.core.entities.Player;

import java.util.ArrayList;
import java.util.UUID;

/**
 * The GameState class is responsible for storing and managing the current state
 * of a game session. It tracks information such as:
 * <ul>
 *     <li>The player whose turn it is</li>
 *     <li>The current game round (1-5)</li>
 *     <li>The game field represented as a two-dimensional array</li>
 *     <li>List of players in the game</li>
 *     <li>Each player's artifacts and rune count</li>
 * </ul>
 * This class provides getter and setter methods for all game state properties.
 */
public class GameState {
    private int playerRound;
    private int gameRound;
    private UUID activePlayerID;
    private ArrayList<Player> players;

    /**
     * Gets the name of the player whose turn it currently is.
     *
     * @return The name of the player whose turn it is, or null if not set
     */
    public String getPlayerTurn() {
        for (Player p : players) {
            if (p.getPlayerID() == activePlayerID) {
                return p.getName();
            }
        }
        System.out.println("no active player found");
        return null;
    }

    /**
     * Sets the player whose turn it currently is.
     *
     * @param player The name of the player whose turn it is
     */
    public void setPlayerTurn(String player) {
        for (Player p : players) {
            if (p.getName().equals(player)) {
                activePlayerID = p.getPlayerID();
                return;
            }
        }
        System.out.println("no player found");
    }

    /**
     * Gets the current game round.
     *
     * @return The current game round as an int (0 means no round set)
     */
    public int getGameRound() {
        return gameRound;
    }

    /**
     * Sets the current game round.
     *
     * @param round The game round number (1-5)
     *              should be checked in GameLogic
     */
    public void setGameRound(int round) {
        if (round >= 1 && round <= 5) {
            gameRound = round;
        } else {
            System.out.println("Invalid game round: " + round);
        }
    }

    /**
     * Gets the list of players in the game.
     *
     * @return A string representation of the players, or null if not set
     */
    public String getPlayers() {
        if (players == null) {
            System.out.println("no players found");
            return null;
        }
        String playersString = "";
        for (Player p : players) {
            playersString += p.getName() + ", ";
        }
        return playersString;
    }

    /**
     * Sets the list of players in the game.
     *
     * @param players An array of player names
     */
    public void setPlayers(String[] players) {
        this.players = new ArrayList<>();
        for (String playerName : players) {
            Player player = new Player(playerName);
            this.players.add(player);
        }
    }

    /**
     * Adds a player to the game.
     *
     * @param player The name of the player to add
     */
    public void addPlayer(String player) {
        Player newPlayer = new Player(player);
        players.add(newPlayer);
    }

    /**
     * Gets the artifacts owned by a specific player.
     *
     * @param player The name of the player
     * @return An array of artifacts owned by the player, or null if not set
     */
    public Artefact[] getArtifacts(String player) {
        if(players == null) {
            System.out.println("no players found");
            return null;
        }
        for (Player p : players) {
            if (p.getName().equals(player)) {
                if(p.getArtifacts() == null) {
                    System.out.println("no artifacts found");
                    return null;
                }
                Artefact[] artefacts = new Artefact[p.getArtifacts().size()];
                for (int i = 0; i < p.getArtifacts().size(); i++) {
                    artefacts[i] = p.getArtifacts().get(i);
                }
            }
        }
        return null;
    }

    /**
     * Sets the artifacts for a specific player.
     *
     * @param artifactsList The list of artifacts to set
     * @param player    The name of the player
     */
    public void setArtifacts(Artefact[] artifactsList, String player) {
        for (Player p : players) {
            if (p.getName().equals(player)) {
                ArrayList<Artefact> artifacts = new ArrayList<>();
                for (Artefact artifact : artifactsList) {
                    artifacts.add(artifact);
                }
                p.setArtifacts(artifacts);
            }
        }
    }

    /**
     * Adds artifacts to a player's collection.
     *
     * @param artifactsList The artifacts to add
     * @param player    The name of the player
     */
    public void addArtifact(Artefact[] artifactsList, String player) {
        for (Player p : players) {
            if (p.getName().equals(player)) {
                for (Artefact artifact : artifactsList) {
                    p.addArtifact(artifact);
                }
            }
        }
    }

    /**
     * Removes artifacts from a player's collection.
     *
     * @param artifactsList The artifacts to remove
     * @param player    The name of the player
     */
    public void removeArtifact(Artefact[] artifactsList, String player) {
        for (Player p : players) {
            if (p.getName().equals(player)) {
                for (Artefact artifact : artifactsList) {
                    p.removeArtifact(artifact);
                }
            }
        }
    }

    /**
     * Gets the number of runes a player has.
     *
     * @param player The name of the player
     * @return The number of runes the player has
     */
    public int getRunes(String player) {
        return 0;
    }

    /**
     * Sets the number of runes for a player.
     *
     * @param runes  The number of runes
     * @param player The name of the player
     */
    public void setRunes(int runes, String player) {
    }

    /**
     * Adds runes to a player's collection.
     *
     * @param runes  The number of runes to add
     * @param player The name of the player
     */
    public void addRunes(int runes, String player) {
    }

    /**
     * Removes runes from a player's collection.
     *
     * @param runes  The number of runes to remove
     * @param player The name of the player
     */
    public void removeRunes(String[] runes, String player) {
    }

    /**
     * Gets the entire game field.
     *
     * @return A two-dimensional array representing the game field, or null if not set
     */
    public int[][] getGameField() {
        return null;
    }

    /**
     * Sets the entire game field.
     *
     * @param gameField A two-dimensional array representing the game field
     */
    public void setGameField(int[][] gameField) {
    }

    /**
     * Gets the value at a specific position on the game field.
     *
     * @param x The x-coordinate on the game field
     * @param y The y-coordinate on the game field
     * @return The value at the specified position
     */
    public int getGameField(int x, int y) {
        return 0;
    }

    /**
     * Sets the value at a specific position on the game field.
     *
     * @param x     The x-coordinate on the game field
     * @param y     The y-coordinate on the game field
     * @param value The value to set at the specified position
     */
    public void setGameField(int x, int y, int value) {
    }
}