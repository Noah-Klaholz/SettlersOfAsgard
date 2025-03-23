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
     * Can differ between operating systems (macOS, Windows, Linux).
     * @param args
     */
    public static void main(String[] args) {
        String jarPath;
        try {
            // Get the current execution path
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

        // Adjust the path if running from IntelliJ (i.e., build/classes instead of build/libs)
        if (jarPath.contains("/build/classes/")) {
            jarPath = jarPath.replace("/build/classes/java/main", "/build/libs/settlersOfAsgard.jar");
        }

        System.out.println("Using jar path: " + jarPath);

        // Commands for server and client
        String serverCmd = "java -jar " + jarPath + " server 9000";
        String clientCmd = "java -jar " + jarPath + " client localhost:9000";

        try {
            // Detect OS
            String os = System.getProperty("os.name").toLowerCase();
            System.out.println("OS: " + os);

            if (os.contains("mac")) {
                System.out.println("Using Mac Terminal");
                // macOS: Use AppleScript with Terminal
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

                // Open new Terminal windows via AppleScript for client
                Runtime.getRuntime().exec(new String[]{
                        "osascript", "-e",
                        "tell application \"Terminal\" to do script \"" + clientCmd + "\""
                });
            } else if (os.contains("win")) {
                // Windows: Use cmd.exe to open new command windows
                Runtime.getRuntime().exec(new String[]{
                        "cmd.exe", "/c", "start", "cmd.exe", "/k", serverCmd
                });

                Runtime.getRuntime().exec(new String[]{
                        "cmd.exe", "/c", "start", "cmd.exe", "/k", clientCmd
                });
            } else if (os.contains("nix") || os.contains("nux") || os.contains("linux")) {
                // Linux: Use terminal emulators (adjust if needed)
                String terminal = findAvailableTerminal();
                if (terminal != null) {
                    Runtime.getRuntime().exec(new String[]{terminal, "-e", serverCmd});
                    Runtime.getRuntime().exec(new String[]{terminal, "-e", clientCmd});
                } else {
                    System.err.println("No compatible terminal emulator found!");
                }
            } else {
                System.err.println("Unsupported OS: " + os);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Finds an available terminal emulator for Linux.
     */
    private static String findAvailableTerminal() {
        String[] terminals = {"gnome-terminal", "konsole", "x-terminal-emulator", "xfce4-terminal", "lxterminal", "mate-terminal"};
        for (String terminal : terminals) {
            try {
                Process process = Runtime.getRuntime().exec(new String[]{"which", terminal});
                if (process.getInputStream().read() != -1) {
                    return terminal; // Return the first available terminal
                }
            } catch (IOException ignored) {}
        }
        return null; // No terminal found
    }


}