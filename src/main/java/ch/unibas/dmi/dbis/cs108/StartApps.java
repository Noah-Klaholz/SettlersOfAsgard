package ch.unibas.dmi.dbis.cs108;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

/**
 * Class starting a local server and a client in a different terminal for easier testing.
 */
public class StartApps {

    /**
     * Main method starting a local server and a client in a different terminal for easier testing.
     * The server is started on port 9000 and the client connects to it.
     * @param args
     */
    public static void main(String[] args) {
        String jarPath;
        try {
            // Get the current code source location
            jarPath = new File(
                    Main.class.getProtectionDomain()
                            .getCodeSource()
                            .getLocation()
                            .toURI()
            ).getAbsolutePath();
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return;
        }

        // If running from the IDE, adjust the path to point to the jar in build/libs.
        if (jarPath.contains("/build/classes/")) {
            // Modify this according to your project's specific directory structure and jar naming
            jarPath = jarPath.replace("/build/classes/java/main", "/build/libs/settlersOfAsgard.jar");
        }

        System.out.println("Using jar path: " + jarPath);

        // Build the commands for server and client
        String serverCmd = "java -jar " + jarPath + " server 9000";
        String clientCmd = "java -jar " + jarPath + " client localhost:9000";

        try {
            // Open new Terminal windows via AppleScript for server
            Runtime.getRuntime().exec(new String[]{
                    "osascript", "-e",
                    "tell application \"Terminal\" to do script \"" + serverCmd + "\""
            });

            // Open new Terminal windows via AppleScript for client
            Runtime.getRuntime().exec(new String[]{
                    "osascript", "-e",
                    "tell application \"Terminal\" to do script \"" + clientCmd + "\""
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}