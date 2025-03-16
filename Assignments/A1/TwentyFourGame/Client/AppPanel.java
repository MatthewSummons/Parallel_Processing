package TwentyFourGame.Client;

import java.awt.*;
import java.awt.event.*;
import java.rmi.RemoteException;

import javax.swing.*;

import TwentyFourGame.Server.Authenticate;
import TwentyFourGame.Server.LogoutStatus;
import TwentyFourGame.Server.UserData;

public class AppPanel extends JPanel {

    private JFrame parentFrame;
    private Authenticate authHandler;
    private UserData userData;

    public AppPanel(JFrame parentFrame, Authenticate authHandler) {
        this.parentFrame = parentFrame;
        this.authHandler = authHandler;
        
        showLoginPanel();
    }
    
    public void showLoginPanel() {
        GridBagConstraints gridConstraints = new GridBagConstraints();
        gridConstraints.insets = new Insets(10, 10, 10, 10);
        
        this.setLayout(new GridBagLayout());
        
        // Login Text
        gridConstraints.gridx = 0;
        gridConstraints.gridy = 0;
        gridConstraints.anchor = GridBagConstraints.EAST;
        this.add(new JLabel("Username"), gridConstraints);

        gridConstraints.gridx = 1;
        gridConstraints.gridwidth = 2;
        gridConstraints.anchor = GridBagConstraints.WEST;
        gridConstraints.fill = GridBagConstraints.HORIZONTAL;
        JTextField usernameField = new JTextField(20);
        this.add(usernameField, gridConstraints);

        // Password Text
        gridConstraints.gridx = 0;
        gridConstraints.gridy = 1;
        gridConstraints.anchor = GridBagConstraints.EAST;
        this.add(new JLabel("Password"), gridConstraints);

        gridConstraints.gridx = 1;
        gridConstraints.gridwidth = 2;
        gridConstraints.anchor = GridBagConstraints.WEST;
        gridConstraints.fill = GridBagConstraints.HORIZONTAL;
        JPasswordField passwordField = new JPasswordField(20);
        this.add(passwordField, gridConstraints);

        // Register Button
        gridConstraints.gridx = 0;
        gridConstraints.gridy = 2;
        gridConstraints.gridwidth = 2;
        gridConstraints.anchor = GridBagConstraints.EAST;
        JButton registerButton = new JButton("Register");
        registerButton.addActionListener(
            new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    parentFrame.setVisible(false);
                    RegistrationManager registrationDialog = new RegistrationManager(parentFrame, authHandler);
                    registrationDialog.setVisible(true);
                    if (registrationDialog.isRegistered()) {
                        showGamePanel();
                    }
                }
            }
        );
        this.add(registerButton, gridConstraints);

        // Login Button
        gridConstraints.gridx = 2;
        gridConstraints.gridy = 2;
        gridConstraints.gridwidth = 2;
        gridConstraints.anchor = GridBagConstraints.WEST;
        JButton loginButton = new JButton("Login");
        loginButton.addActionListener(
            new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    LoginManager loginManager = new LoginManager(parentFrame, usernameField, passwordField);
                    loginManager.attemptLogin(authHandler);
                    if (loginManager.isLoggedIn()) {
                        userData = loginManager.getUserData();
                        showGamePanel();
                    }
                }
            }
        );
        this.add(loginButton, gridConstraints);
    }

    public void showGamePanel() {
        // Remove all existing components
        this.removeAll();

        // Create the tabbed pane
        JTabbedPane tabbedPane = new JTabbedPane();

        // User Profile tab
        JPanel userProfilePanel = new UserPanel();

        tabbedPane.addTab("User Profile", userProfilePanel);

        // TODO: Create Game Panel
        // Play Game tab
        JPanel playGamePanel = new JPanel();
        // Add your play game UI components here
        tabbedPane.addTab("Play Game", playGamePanel);

        // Leader Board tab
        JPanel leaderBoardPanel = new JPanel();
        // Add your leader board UI components here
        tabbedPane.addTab("Leader Board", leaderBoardPanel);

        // Logout tab
        JButton logoutBtn = new JButton("Logout");
        logoutBtn.addActionListener(
            new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    // RMI Call to Server for Logout
                    try {
                        LogoutStatus result = authHandler.logout(userData.username);
                        if (result == LogoutStatus.SERVER_ERROR) {
                            Notification.showError("Server error while logging out", parentFrame);
                        } // Successful Otherwise
                    } catch (RemoteException ex) {
                        Notification.showError("Unable to logout properly", null);
                        System.err.println("Error: " + ex);
                    } finally {
                        AppPanel.this.removeAll();
                        AppPanel.this.showLoginPanel();
                        AppPanel.this.repaint();
                        AppPanel.this.revalidate();
                    }
                }
            }
        );

        // Add your logout UI components here
        tabbedPane.addTab("Logout", logoutBtn);

        // Add the tabbed pane to the main panel
        this.setLayout(new BorderLayout());
        this.add(tabbedPane, BorderLayout.CENTER);

        this.repaint();
        this.revalidate();
        // When coming from registration, unhide the parent frame
        parentFrame.setVisible(true);
    }

    private class UserPanel extends JPanel {
        
        public UserPanel() {
            createUserProfilePage(userData);
        }

        private void createUserProfilePage(UserData userData) {
            assert(userData != null);
            
            this.setLayout(new GridBagLayout());

            GridBagConstraints constraints = new GridBagConstraints();
            constraints.insets = new Insets(20, 0, 5, 0);
            constraints.fill = GridBagConstraints.HORIZONTAL;
            constraints.weightx = 1.0;

            // Username
            constraints.gridx = 0;
            constraints.gridy = 0;
            constraints.gridwidth = 2;
            constraints.anchor = GridBagConstraints.CENTER;
            JLabel usernameLabel = new JLabel(userData.username, SwingConstants.CENTER);
            usernameLabel.setFont(new Font("Helvetica", Font.BOLD, 20));
            this.add(usernameLabel, constraints);

            // Wins
            constraints.gridx = 0;
            constraints.gridy = 1;
            constraints.gridwidth = 1;
            constraints.anchor = GridBagConstraints.CENTER;
            JLabel winsLabel = new JLabel("Wins (" + userData.wins + ")", SwingConstants.CENTER);
            winsLabel.setFont(new Font("Helvetica", Font.PLAIN, 14));
            this.add(winsLabel, constraints);

            // Games Played
            constraints.gridx = 0;
            constraints.gridy = 2;
            constraints.anchor = GridBagConstraints.CENTER;
            JLabel gamesPlayedLabel = new JLabel("Games Played (" + userData.games + ")", SwingConstants.CENTER);
            gamesPlayedLabel.setFont(new Font("Helvetica", Font.PLAIN, 14));
            this.add(gamesPlayedLabel, constraints);

            // Average Win Time
            constraints.gridx = 1;
            constraints.gridy = 1;
            constraints.anchor = GridBagConstraints.CENTER;
            JLabel avgWinTimeLabel = new JLabel("Avg. Win Time (" + userData.avgWinTime + "s)", SwingConstants.CENTER);
            avgWinTimeLabel.setFont(new Font("Helvetica", Font.PLAIN, 14));
            this.add(avgWinTimeLabel, constraints);

            // Rank
            constraints.gridx = 1;
            constraints.gridy = 2;
            constraints.anchor = GridBagConstraints.CENTER;
            JLabel rankLabel = new JLabel("Rank (" + userData.rank + ")", SwingConstants.CENTER);
            rankLabel.setFont(new Font("Helvetica", Font.PLAIN, 14));
            this.add(rankLabel, constraints);

            // Add a vertical glue to push the components to the top
            constraints.gridx = 0;
            constraints.gridy = 5;
            constraints.weighty = 1.0;
            this.add(Box.createVerticalGlue(), constraints);
        }
    }
}
