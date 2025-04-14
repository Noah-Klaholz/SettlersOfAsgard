package ch.unibas.dmi.dbis.cs108.client.core.state;

import ch.unibas.dmi.dbis.cs108.shared.game.Board;
import ch.unibas.dmi.dbis.cs108.shared.game.Player;
import ch.unibas.dmi.dbis.cs108.shared.game.Tile;

import java.util.ArrayList;
import java.util.List;

public class GameState {

    private GameRound gameRound;
    private PlayerRound playerRound;
    private List<Player> players;
    private Board board;

    public GameState(){
        this.gameRound = new GameRound();
        this.playerRound = new PlayerRound();
        players = new ArrayList<Player>();
        board = new Board();
    }

    public void addPlayer(Player player) {
        players.add(player);
    }

    public void removePlayer(Player player) {
        players.remove(player);
    }

    public List<Player> getPlayers() {
        return players;
    }

    public Board getBoard() {
        return board;
    }

    public void setBoard(Board board) {
        this.board = board;
    }

    public int getCurrentGameRoundNumber() {
        return gameRound.getNumber();
    }

    public int getCurrentPlayerRoundNumber() {
        return playerRound.getNumber();
    }

    public GameRound getCurrentGameRound() {
        return gameRound;
    }

    public PlayerRound getCurrentPlayerRound() {
        return playerRound;
    }

    public void setCurrentGameRoundNumber(int number) {
        gameRound.setNumber(number);
    }

    public void setCurrentPlayerRoundNumber(int number) {
        playerRound.setNumber(number);
    }

    public void nextTurn(){
        if (playerRound.getNumber() >= players.size()) {
            playerRound.setNumber(0);
            if(gameRound.getNumber() < 5){
                gameRound.nextRound();
            }
            else{
                System.out.println("Game Over");
                //todo: end game
            }
        }
        else {
            playerRound.nextRound();
        }
    }

    //todo: change so that GameRound has a list of PlayerRounds
    public void buyTile(Player player, Tile tile) {
        if(tile.isPurchased()){
            System.out.println("Tile already purchased");
        } else {
            if(player.getRunes() >= tile.getPrice()){
                player.setRunes(player.getRunes() - tile.getPrice());
                tile.setPurchased(true);
                player.addOwnedTile(tile);
                System.out.println("Player " + player.getPlayerID() + " purchased tile " + tile.getTileID());
            } else {
                System.out.println("Not enough runes");
            }
        }

    }

    public GameRound getGameRound() {
        return gameRound;
    }
    public PlayerRound getPlayerRound() {
        return playerRound;
    }
    public void setGameRound(GameRound gameRound) {
        this.gameRound = gameRound;
    }
    public void setPlayerRound(PlayerRound playerRound) {
        this.playerRound = playerRound;
    }
}