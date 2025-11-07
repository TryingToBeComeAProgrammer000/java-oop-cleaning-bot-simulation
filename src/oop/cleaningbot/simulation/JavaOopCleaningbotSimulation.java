package oop.cleaningbot.simulation;

import view.MainFrame;
import javax.swing.*;

/**
 * Main entry point for the Robot Cleaning Simulator
 */
public class JavaOopCleaningbotSimulation {
    public static void main(String[] args) {
        // Set system look and feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            System.err.println("Could not set system look and feel: " + e.getMessage());
        }
        
        // Launch GUI on Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            MainFrame frame = new MainFrame();
            frame.setVisible(true);
        });
    }
}