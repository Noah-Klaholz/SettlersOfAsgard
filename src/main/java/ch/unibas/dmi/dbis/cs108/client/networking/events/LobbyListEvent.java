package ch.unibas.dmi.dbis.cs108.client.networking.events;

import ch.unibas.dmi.dbis.cs108.client.ui.controllers.LobbyScreenController;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LobbyListEvent implements Event {
    private final Instant timestamp = Instant.now();
    private final List<LobbyScreenController.GameLobby> lobbies = new ArrayList<>();

    public LobbyListEvent(String message) {
        String[] lobbies = message.split("%");
        Arrays.stream(lobbies).toList().forEach(lobby -> {
            String [] params = lobby.split(":");
            if (params.length >= 5) {
                LobbyScreenController.GameLobby lobbyObject = new LobbyScreenController.GameLobby(
                        params[0], // lobbyId
                        params[0], // name = id
                        Integer.parseInt(params[1]), // currentPlayers
                        Integer.parseInt(params[2]), // maxPlayers
                        params[3], // status
                        params[4] // hostName
                );
                this.lobbies.add(lobbyObject);
            } else {
                System.out.println("Invalid lobby data: " + lobby);
            }
        });
    }

    @Override
    public Instant getTimestamp() {
        return timestamp;
    }

    public List<LobbyScreenController.GameLobby> getLobbies() {
        return lobbies;
    }
}