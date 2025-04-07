package ch.unibas.dmi.dbis.cs108.example.gui.swing;

import javax.swing.*;

/**
 * This is an example Swing Application
 */
public class SwingGUI {

    private static void createAndShowGUI() {
        // Make sure we have nice window decorations
        JFrame.setDefaultLookAndFeelDecorated(true);
        // Create and set up the window.
        JFrame frame = new JFrame("HelloWorldSwing");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // Add the ubiquitous "Hello World" label
        JLabel label = new JLabel("Hello World");
        frame.getContentPane().add(label);
        // Display the window
        frame.pack();
        frame.setVisible(true);
    }

    public static void main(String[] args) {
        // Schedule a job for the event-dispatching thread: // creating and showing this application's GUI
        javax.swing.SwingUtilities.invokeLater(() -> createAndShowGUI());
    }

}
