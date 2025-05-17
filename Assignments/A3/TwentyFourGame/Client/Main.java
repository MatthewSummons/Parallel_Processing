package TwentyFourGame.Client;

import java.awt.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import javax.swing.*;

import TwentyFourGame.Server.Authenticate;

// TODO: Add Icon on application window and app-icon
public final class Main {    
    private Authenticate authHandler;
    
    
    /**
     * Create the GUI and show it. For thread safety,
     * this method should be invoked from the
     * event dispatch thread.
     */
    private void createAndShowGUI() {
        // Create and set up the window.
        JFrame frame = new JFrame("24 Games");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setMinimumSize(new Dimension(400, 200));
        frame.setLocationRelativeTo(null);

        AppPanel panel = new AppPanel(frame, authHandler);   // Pass parent frame down to hide it later
        frame.add(panel);

        // Display the window.
        frame.pack();
        frame.setVisible(true);
    }
    
    public Main(String rmiHost) {
        try {
            Registry registry = LocateRegistry.getRegistry(rmiHost);
            authHandler = (Authenticate) registry.lookup("AuthenticationManager");
        } catch (Exception e) {
            System.err.println("Exception thrown: " + e);
            Notification.showError("Server is offline!", null);
        }
    }
    
    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("Usage: java TwentyFourGame.Client.Main <rmiHost>");
            System.exit(1);
        }
        
        Main application = new Main(args[0]);
        
                
        // Schedule a job for the event dispatch thread: creating and showing this application's GUI.
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                application.createAndShowGUI();
            }
        });
    }
    
}
