package ch.unibas.dmi.dbis.cs108.server;

import org.junit.jupiter.api.*;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * The {@code ServerTest} class is a JUnit test class for testing the {@link GameServer} and its interaction
 * with a {@link GameClient}. It ensures that the server can start, accept client connections, process
 * messages, and shut down gracefully.
 */
class ServerTest {

    private GameServer server;
    private Thread serverThread;

    /**
     * Sets up the test environment by starting the {@link GameServer} in a separate thread.
     * This method is executed before each test method.
     */
    @BeforeEach
    public void setUp() {
        // Start the server in a new thread
        CountDownLatch serverReady = new CountDownLatch(1);
        serverThread = new Thread(() -> {
            server = new GameServer(9000);
            server.start();
            serverReady.countDown(); // Signal that the server is ready
        });
        serverThread.start();

        // Wait for the server to start
        try {
            serverReady.await(2, TimeUnit.SECONDS); // Wait up to 2 seconds for the server to start
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Server setup interrupted", e);
        }
    }

    /**
     * Tears down the test environment by shutting down the {@link GameServer} and interrupting
     * the server thread if it is still running. This method is executed after each test method.
     */
    @AfterEach
    public void tearDown() {
        // Shut down the server gracefully
        if (server != null) {
            server.shutdown();
        }
        if (serverThread != null && serverThread.isAlive()) {
            serverThread.interrupt(); // Interrupt the server thread if it's still running
        }
    }

    /**
     * Tests the interaction between the {@link GameServer} and a {@link GameClient}. This test:
     * Starts the server.
     * Connects a client to the server.
     * Sends a test message from the client to the server.
     * Waits for the server to process the message.
     * Disconnects the client.
     * @throws IOException if an I/O error occurs during client-server communication.
     * @throws InterruptedException if the test thread is interrupted while waiting.
     */
    @Test
    public void testServer() throws IOException, InterruptedException {
        System.out.println("Testing");

        // Start the client
        GameClient client = new GameClient("127.0.0.1", 9000);
        client.connect();

        // Send a test command
        client.sendMessage("Test:arg1,arg2,arg3");

        // Wait for the server to process the message
        Thread.sleep(1000); // Adjust the delay as needed

        // Disconnect the client
        client.disconnect();

        System.out.println("Test complete.");
    }

    /**
     * Tests the connection loss handling of the {@link GameServer}. This test:
     * Starts the server.
     * Connects a client to the server.
     * Simulates a client disconnect.
     * Verifies that the server handles the disconnect properly.
     * @throws IOException if an I/O error occurs during client-server communication.
     * @throws InterruptedException if the test thread is interrupted while waiting.
     */
    @Test
    public void testConnectionLossHandling() throws IOException, InterruptedException {
        System.out.println("Testing connection loss handling");

        // Start the client
        GameClient client = new GameClient("127.0.0.1", 9000);
        client.connect();

        // Simulate client disconnect by closing the socket
        client.disconnect();

        // Wait for the server to handle the disconnect
        Thread.sleep(1000); // Adjust the delay as needed

        // Verify that the server has removed the client
        Assertions.assertTrue(server.getClients().isEmpty(), "Server should have removed the disconnected client");

        System.out.println("Connection loss handling test complete.");
    }
}