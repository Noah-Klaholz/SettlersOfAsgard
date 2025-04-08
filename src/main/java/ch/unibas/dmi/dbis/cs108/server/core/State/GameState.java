package ch.unibas.dmi.dbis.cs108.server.core.State;

import ch.unibas.dmi.dbis.cs108.shared.entities.entities.Artefact;
import ch.unibas.dmi.dbis.cs108.shared.entities.entities.Board;
import ch.unibas.dmi.dbis.cs108.shared.entities.entities.Player;
import ch.unibas.dmi.dbis.cs108.shared.entities.entities.Tile;

import java.util.ArrayList;

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
    private Player activePlayer;
    private ArrayList<Player> players;
    private Board board;

    public GameState() {
        this.playerRound = 0; //0-Anzahl Spieler
        this.gameRound = 0; //1-5
        this.activePlayer = null;
        this.players = new ArrayList<>(); //determines order
        this.board = new Board();
    }

    /**
     * Gets the name of the player whose turn it currently is.
     *
     * @return The name of the player whose turn it is, or null if not set
     */
    public String getPlayerTurn() {
        for (Player p : players) {
            if (p.getName() == activePlayer.getName()) {
                return p.getName();
            }
        }
        System.out.println("no active player found");
        return null;
    }

    public Board getBoard() {return board;}

    public void setActivePlayer(String name){
        for(Player p : players) {
            if (p.getName().equals(name)) {
                activePlayer = p;
                return;
            }
        }
        System.out.println("player not found");
    }

    /**
     * Sets the player whose turn it currently is.
     *
     * @param player The name of the player whose turn it is
     */
    public void setPlayerTurn(String player) {
        for (Player p : players) {
            if (p.getName().equals(player)) {
                activePlayer = p;
                return;
            }
        }
        System.out.println("no player found");
    }

    /**
     * Gets the current player round.
     *
     * @return The current player round as an int (0 means no player set)
     */
    public int getPlayerRound() {
        return playerRound;
    }

    /**
     * Sets the current playerRound
     *
     * @param playerRound the player to set
     */
    public void setPlayerRound(int playerRound) {
        this.playerRound = playerRound;
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
        if (round >= 0 && round <= 5) {
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

    public ArrayList<Player> getPlayerList() {
        return players;
    }

    /**
     * Sets the list of players in the game.
     *
     * @param players An array of player names
     */
    public void setPlayers(String[] players) {
        if (players == null) {
            System.out.println("no players found");
            return;
        }
        if(players.length >= 9){
            System.out.println("too many players");
            return;
        }
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
        if(artifactsList == null) {
            System.out.println("no artifacts found");
            return;
        }
        if(artifactsList.length >= 4){
            System.out.println("too many artifacts");
            return;
        }
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
        if(artifactsList == null) {
            System.out.println("no artifacts found");
            return;
        }
        if(artifactsList.length + players.get(0).getArtifacts().size() >= 4){
            System.out.println("too many artifacts");
            return;
        }
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
        for (Player p : players) {
            if (p.getName().equals(player)) {
                return p.getRunes();
            }
        }
        System.out.println("no player found");
        return -1;
    }

    /**
     * Sets the number of runes for a player.
     *
     * @param runes  The number of runes
     * @param player The name of the player
     */
    public void setRunes(int runes, String player) {
        if(runes < 0) {
            System.out.println("runes cannot be negative");
            return;
        }
        for (Player p : players) {
            if (p.getName().equals(player)) {
                p.setRunes(runes);
                break;
            }
        }
        System.out.println("no player found");
    }

    /**
     * Adds runes to a player's collection.
     *
     * @param runes  The number of runes to add
     * @param player The name of the player
     */
    public void addRunes(int runes, String player) {
        for (Player p : players) {
            if (p.getName().equals(player)) {
                p.addRunes(runes);
                break;
            }
        }
        System.out.println("no player found");
    }

    /**
     * Removes runes from a player's collection.
     * //falls player runes kleiner remove runes, set to 0
     *
     * @param runes  The number of runes to remove
     * @param player The name of the player
     */
    public void removeRunes(int runes, String player) {
        for (Player p : players) {
            if (p.getName().equals(player)) {
                if(p.getRunes() < runes) {
                    p.setRunes(0);
                }
                p.removeRunes(runes);
            }
        }
    }

    /**
     * Gets the entire game field.
     *
     * @return A two-dimensional array representing the game field, or null if not set
     */
    public Tile[][] getGameField() {
        if(board == null) {
            System.out.println("no board found");
            return null;
        }
        if(board.getTiles() == null) {
            System.out.println("no game field found");
            return null;
        }
        return board.getTiles();
    }

    /**
     * Sets the entire game field.
     *
     * @param gameField A two-dimensional array representing the game field
     */
    //since set game field is so far 7x8, it is implemented that way here
    // for future updates, another method shall be written (for example initBoard) that uses board.initBoard(x,y)
    public void setGameField(Tile[][] gameField) {
        if (gameField == null) {
            System.out.println("game field is null");
            return;
        }
        if(board == null) {
            System.out.println("board is null");
            return;
        }
        if (gameField.length != board.getTiles().length) {
            System.out.println("game field is not 7x8");
            return;
        }
        board.setTiles(gameField);
    }

    /**
     * Gets the Rune value at a specific position on the game field.
     *
     * @param x The x-coordinate on the game field
     * @param y The y-coordinate on the game field
     * @return The Rune value at the specified position
     */
    public int getGameFieldValue(int x, int y) {
        if (board == null) {
            System.out.println("no board found");
            return -1;
        }
        if (board.getTiles() == null) {
            System.out.println("no game field found");
            return -1;
        }
        if (x < 0 || x >= board.getTiles().length || y < 0 || y >= board.getTiles()[0].length) {
            System.out.println("invalid coordinates");
            return -1;
        }
        return board.getTileByCoordinates(x, y).getResourceValue();
    }

    /**
     * Sets the Rune value at a specific position on the game field.
     *
     * @param x     The x-coordinate on the game field
     * @param y     The y-coordinate on the game field
     * @param value The value to set at the specified position
     */
    public void setGameFieldValue(int x, int y, int value) {
        if (board == null) {
            System.out.println("no board found");
            return;
        }
        if (board.getTiles() == null) {
            System.out.println("no game field found");
            return;
        }
        if (x < 0 || x >= board.getTiles().length || y < 0 || y >= board.getTiles()[0].length) {
            System.out.println("invalid coordinates");
            return;
        }
        board.getTileByCoordinates(x, y).setResourceValue(value);
    }

    /**
     * Gets a specific tile on the game field.
     *
     * @param x The x-coordinate on the game field
     * @param y The y-coordinate on the game field
     * @return The tile at the specified position, or null if not found
     */
    public Tile getTile(int x, int y) {
        if (board == null) {
            System.out.println("no board found");
            return null;
        }
        if (board.getTiles() == null) {
            System.out.println("no game tiles found");
            return null;
        }
        return board.getTileByCoordinates(x, y);
    }

    /**
     * Sets a specific tile on the game field.
     *
     * @param x   The x-coordinate on the game field
     * @param y   The y-coordinate on the game field
     * @param tile The tile to set at the specified position
     */
    public void setTile(int x, int y, Tile tile) {
        if (board == null) {
            System.out.println("no board found");
            return;
        }
        if (board.getTiles() == null) {
            System.out.println("no game tiles found");
            return;
        }
        if (x < 0 || x >= board.getTiles().length || y < 0 || y >= board.getTiles()[0].length) {
            System.out.println("invalid coordinates");
            return;
        }
        board.setTileByCoordinates(x, y, tile);
    }

    @Override
    public String toString() {
        return "GameState{" +
                "playerRound=" + playerRound +
                ", gameRound=" + gameRound +
                ", activePlayer=" + activePlayer.getName() +
                ", players=" + players +
                ", board=" + board +
                '}';
    }
}