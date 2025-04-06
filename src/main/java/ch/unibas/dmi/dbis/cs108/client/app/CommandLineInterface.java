package ch.unibas.dmi.dbis.cs108.client.app;

import ch.unibas.dmi.dbis.cs108.client.core.entities.Player;
import ch.unibas.dmi.dbis.cs108.client.networking.NetworkController;
import ch.unibas.dmi.dbis.cs108.client.networking.events.*;

import java.util.Scanner;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

/**
 * CommandLineInterface class
 * Handles terminal-based interaction with the network system.
 */
public class CommandLineInterface {
    private static final Logger logger = Logger.getLogger(CommandLineInterface.class.getName());
    private final NetworkController networkController;
    private final Player localPlayer;
    private final AtomicBoolean running;
    private final Scanner scanner;
    private final BlockingQueue<String> messageQueue;

    /**
     * Constructor for CommandLineInterface
     *
     * @param networkController NetworkController
     * @param localPlayer       Player
     */
    public CommandLineInterface(NetworkController networkController, Player localPlayer) {
        this.networkController = networkController;
        this.localPlayer = localPlayer;
        this.running = new AtomicBoolean(true);
        this.scanner = new Scanner(System.in);
        this.messageQueue = new LinkedBlockingQueue<>();
        setupEventListeners();
    }

    /**
     * Start the command line interface
     */
    public void start() {
        try {
            // Start message receiver thread
            Thread receiverThread = startMessageReceiverThread();

            logger.info("Connected. Type /help to view available commands");

            // Process user input
            while (running.get()) {
                processInput(scanner.nextLine().trim().toLowerCase());
            }

            receiverThread.join(1000);
        } catch (Exception e) {
            logger.warning("CLI error: " + e.getMessage());
        } finally {
            scanner.close();
        }
    }

    /**
     * Stop the command line interface
     */
    public void stop() {
        running.set(false);
    }

    /**
     * Set up event listeners
     */
    private void setupEventListeners() {
        EventDispatcher dispatcher = EventDispatcher.getInstance();

        // Add listener for chat messages
        dispatcher.registerListener(ChatMessageEvent.class, new EventDispatcher.EventListener<ChatMessageEvent>() {
            @Override
            public void onEvent(ChatMessageEvent event) {
                String prefix = "";
                switch (event.getType()) {
                    case GLOBAL:
                        prefix = "[GLOBAL] ";
                        break;
                    case LOBBY:
                        prefix = "[LOBBY] ";
                        break;
                    case PRIVATE:
                        prefix = "[PRIVATE] ";
                        break;
                }
                messageQueue.add(prefix + event.getSender() + ": " + event.getContent());
            }

            @Override
            public Class<ChatMessageEvent> getEventType() {
                return ChatMessageEvent.class;
            }
        });

        // Add listener for notifications
        dispatcher.registerListener(NotificationEvent.class, new EventDispatcher.EventListener<NotificationEvent>() {
            @Override
            public void onEvent(NotificationEvent event) {
                messageQueue.add("[NOTIFICATION] " + event.getMessage());
            }

            @Override
            public Class<NotificationEvent> getEventType() {
                return NotificationEvent.class;
            }
        });

        // Add listener for errors
        dispatcher.registerListener(ErrorEvent.class, new EventDispatcher.EventListener<ErrorEvent>() {
            @Override
            public void onEvent(ErrorEvent event) {
                messageQueue.add("[ERROR] " + event.getErrorCode() + ": " + event.getErrorMessage());
            }

            @Override
            public Class<ErrorEvent> getEventType() {
                return ErrorEvent.class;
            }
        });

        // Add listener for connection events
        dispatcher.registerListener(ConnectionEvent.class, new EventDispatcher.EventListener<ConnectionEvent>() {
            @Override
            public void onEvent(ConnectionEvent event) {
                messageQueue.add("[CONNECTION] " + event.getState() + ": " + event.getMessage());
            }

            @Override
            public Class<ConnectionEvent> getEventType() {
                return ConnectionEvent.class;
            }
        });

        // Add listener for lobby events
        dispatcher.registerListener(LobbyEvent.class, new EventDispatcher.EventListener<LobbyEvent>() {
            @Override
            public void onEvent(LobbyEvent event) {
                messageQueue.add("[LOBBY] " + event.getPlayerName() + " " + event.getAction() + " " + event.getLobbyName());
            }

            @Override
            public Class<LobbyEvent> getEventType() {
                return LobbyEvent.class;
            }
        });
    }

    /**
     * Process user input
     */
    private void processInput(String input) {
            input = input.replaceAll("[$]", "").trim();
            if (input.equals("/exit")) {
                running.set(false);
            } else if (input.startsWith("/changename ")) {
                String newName = input.replace("/changename ", "").trim();
                networkController.changeName(newName);
            } else if (input.equals("/ping")) {
                // Ping is automatically handled by NetworkController
                messageQueue.add("Sending ping...");
            } else if (input.startsWith("/join ")) {
                String lobbyId = input.replace("/join ", "").trim();
                networkController.joinLobby(lobbyId);
            } else if (input.startsWith("/leave")) {
                networkController.leaveLobby();
            } else if (input.startsWith("/create ")) {
                String lobbyName = input.replace("/create ", "").trim();
                networkController.createLobby(lobbyName);
            } else if (input.startsWith("/start")) {
                networkController.startGame();
            } else if (input.startsWith("/listlobbies")) {
                networkController.listLobbies();
            } else if (input.startsWith("@")) {
                String[] parts = input.substring(1).split(" ", 2);
                if (parts.length == 2) {
                    networkController.sendPrivateChat(parts[0], parts[1]);
                } else {
                    messageQueue.add("Usage: @<username> <message>");
                }
            } else if (input.startsWith("/global ")) {
                String message = input.replace("/global ", "").trim();
                networkController.sendGlobalChat(message);
            } else if (input.startsWith("/help")) {
                logger.info("Available commands: /changeName <name>, /ping, /exit, /join <lobbyId>, /leave, /create <lobbyName>, " +
                        "/start, /listLobbies, /help. Use @<playerName> <message> to whisper and /global <message> for global chat. " +
                        "Typing any non-command sends a lobby chat message if you're in a lobby, or global chat if not.");
            } else if (input.startsWith("/end ")) {
                networkController.endTurn();
            } else if (input.startsWith("/buytile")) {
                String[] coords = input.replace("/buytile ", "").trim().split(" ");
                if (coords.length == 2) {
                    networkController.buyTile(Integer.parseInt(coords[0]), Integer.parseInt(coords[1]));
                } else {
                    messageQueue.add("Usage: /buytile <x> <y>");
                }
            } else if (input.startsWith("/placestructure")) {
                String[] args = input.replace("/placestructure ", "").trim().split(" ");
                if (args.length == 3) {
                    networkController.placeStructure(Integer.parseInt(args[0]), Integer.parseInt(args[1]), args[2]);
                } else {
                    messageQueue.add("Usage: /placestructure <x> <y> <structureId>");
                }
            } else if (input.startsWith("/usestructure")) {
                String[] args = input.replace("/usestructure ", "").trim().split(" ");
                if (args.length == 3) {
                    networkController.placeStructure(Integer.parseInt(args[0]), Integer.parseInt(args[1]), args[2]);
                } else {
                    messageQueue.add("Usage: /usestructure <x> <y> <structureId>");
                }
            } else if (input.startsWith("/upgradestatue")) {
                String[] args = input.replace("/upgradestatue ", "").trim().split(" ");
                if (args.length == 3) {
                    networkController.upgradeStatue(Integer.parseInt(args[0]), Integer.parseInt(args[1]), args[2]);
                } else {
                    messageQueue.add("Usage: /upgradestatue <x> <y> <statueId>");
                }
            } else if (input.startsWith("/usestatue")) {
                String[] args = input.replace("/usestatue ", "").trim().split(" ");
                if (args.length == 4) {
                    networkController.useStatue(Integer.parseInt(args[0]), Integer.parseInt(args[1]), args[2], args[3]);
                } else {
                    messageQueue.add("Usage: /usestatue <x> <y> <statueId> <useType>");
                }
            } else if (input.startsWith("/usefieldartifact")) {
                String[] args = input.replace("/usefieldartifact ", "").trim().split(" ");
                if (args.length == 4) {
                    networkController.useStatue(Integer.parseInt(args[0]), Integer.parseInt(args[1]), args[2], args[3]);
                } else {
                    messageQueue.add("Usage: /usefieldartifact <x> <y> <artifactId> <useType>");
                }
            } else if (input.startsWith("/useplayerartifact")) {
                String[] args = input.replace("/useplayerartifact ", "").trim().split(" ");
                if (args.length == 4) {
                    networkController.useStatue(Integer.parseInt(args[0]), Integer.parseInt(args[1]), args[2], args[3]);
                } else {
                    messageQueue.add("Usage: /useplayerartifact <x> <y> <artifactId> <useType>");
                }
            } else if (input.startsWith("/status")) { // Should return the current gameState (turn, round, available runes, etc.)
                networkController.getGameState();
            } else if (input.startsWith("/prices")) { // Should return prices for available actions
                networkController.getPrices();
            } else {
                networkController.sendLobbyChat(input);
            }
    }

    /**
     * Start a message receiver thread to display messages from the message queue
     *
     * @return Thread
     */
    private Thread startMessageReceiverThread() {
        Thread thread = new Thread(() -> {
            try {
                while (running.get()) {
                    String message = messageQueue.poll();
                    if (message != null) {
                        System.out.println(message);
                        if (message.contains("Server has shut down")) {
                            running.set(false);
                            break;
                        }
                    }
                    Thread.sleep(50); // Small delay to prevent CPU hogging
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        thread.setDaemon(true);
        thread.start();
        return thread;
    }
}