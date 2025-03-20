package ch.unibas.dmi.dbis.cs108.client.app;

import ch.unibas.dmi.dbis.cs108.client.core.entities.Player;
import ch.unibas.dmi.dbis.cs108.client.networking.GameClient;

import java.util.Scanner;
import java.util.concurrent.atomic.AtomicBoolean;

public class Main {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        GameClient client = null;
        AtomicBoolean running = new AtomicBoolean(true);

        try {
            String systemName = System.getProperty("user.name");
            Player localPlayer = new Player(systemName, "InitialName");

            System.out.println("Connecting to server at localhost:9999...");

            client = new GameClient("localhost", 9999, localPlayer);

            if (checkClient(client)) return;

            Thread receiverThread = startMessageReceiverThread(client, running);

            System.out.println("Connected. Type /changeName <name>, /ping, /exit, or your chat message.");

            processInput(running, scanner, client);

            receiverThread.join(1000);

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (client != null && client.isConnected()) {
                client.disconnect(); // Add this method to GameClient
            }
            scanner.close();
            System.out.println("Client terminated.");
        }

        System.out.println("message");
    }


    //after implementing GameClient

    private static boolean checkClient(GameClient client){
        return false;
    }


    private static void processInput(AtomicBoolean running, Scanner scanner, GameClient client){

    }

    private static Thread startMessageReceiverThread(GameClient client, AtomicBoolean running){
        return null;
    }


}
