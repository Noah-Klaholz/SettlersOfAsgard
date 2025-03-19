package ch.unibas.dmi.dbis.cs108.server;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Logger;

public class Lobby {

    private String id;
    private List<ClientHandler> players;
    private int maxPlayers;
    private boolean isGameStarted;
    private static final Logger logger = Logger.getLogger(Lobby.class.getName());

    public Lobby(String id, int maxPlayers) {
        this.id = id;
        this.maxPlayers = maxPlayers;
        this.players = new CopyOnWriteArrayList<>();
        this.isGameStarted = false;
    }

    public String getId() {
        return id;
    }

    public List<ClientHandler> getPlayers() {
        return players;
    }

    public boolean addPlayer(ClientHandler player) {
        if(players.size() < maxPlayers && !isGameStarted) {
            players.add(player);
            logger.info(player.toString() + " has joined Lobby: " + id);
            return true;
        }
        logger.warning(player.toString() + " could not join Lobby: " + id);
        return false;
    }

    public boolean removePlayer(ClientHandler player) {
        if(!players.isEmpty()){
            players.remove(player);
            logger.info(player.toString() + " has been removed from Lobby: " + id);
            return true;
        }
        logger.warning(player.toString() + " was not removed from Lobby: " + id);
        return false;
    }

    public boolean isGameStarted(){
        return isGameStarted;
    }

    public boolean isFull(){
        return players.size() == maxPlayers;
    }

    @Override
    public String toString() {
        return "Lobby{" +
                "id='" + id + '\'' +
                ", players=" + players.size() +
                ", maxPlayers=" + maxPlayers +
                ", isGameStarted=" + isGameStarted +
                '}';
    }


}