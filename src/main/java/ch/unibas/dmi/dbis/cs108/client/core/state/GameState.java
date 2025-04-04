package ch.unibas.dmi.dbis.cs108.client.core.state;

import ch.unibas.dmi.dbis.cs108.client.core.entities.GameRound;
import ch.unibas.dmi.dbis.cs108.client.core.entities.PlayerRound;

public class GameState {

    private GameRound gameRound;
    private PlayerRound playerRound;

    public GameState(){
        this.gameRound = new GameRound();
        this.playerRound = new PlayerRound();
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