package TwentyFourGame.Client;

import java.awt.*;
import java.awt.event.*;
import java.rmi.RemoteException;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;

import TwentyFourGame.Common.Authenticate;
import TwentyFourGame.Common.LogoutStatus;
import TwentyFourGame.Common.UserData;

import java.util.ArrayList;
import java.util.Arrays;



public class AppPanel extends JPanel {

    private JFrame parentFrame;
    private Authenticate authHandler;

    private String username;

    public AppPanel(JFrame parentFrame, Authenticate authHandler) {
        this.parentFrame = parentFrame;
        this.authHandler = authHandler;
        
        showLoginPanel();

        // Logout on kill signal
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
            try {
                if (username != null) {
                    authHandler.logout(username);
                }
            } catch (RemoteException ex) {
                System.err.println("Error logging out: " + ex);
            }
            }
        });
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
        registerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                parentFrame.setVisible(false);
                RegistrationManager registrationDialog = new RegistrationManager(
                    parentFrame, authHandler
                ); registrationDialog.setVisible(true);
                if (registrationDialog.isRegistered() && registrationDialog.isLoggedIn()) {
                    UserData userData = registrationDialog.getUserData();
                    username = userData.username; 
                    showMainPanel(userData);
                }
            }
            }
        ); this.add(registerButton, gridConstraints);

        // Login Button
        gridConstraints.gridx = 2;
        gridConstraints.gridy = 2;
        gridConstraints.gridwidth = 2;
        gridConstraints.anchor = GridBagConstraints.WEST;
        JButton loginButton = new JButton("Login");
        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                LoginManager loginManager = new LoginManager(parentFrame, usernameField, passwordField);
                loginManager.attemptLogin(authHandler);
                if (loginManager.isLoggedIn()) {
                    UserData userData = loginManager.getUserData();
                    username = userData.username;
                    showMainPanel(userData);
                }
            }
            }
        ); this.add(loginButton, gridConstraints);
    }

    private void showMainPanel(UserData userdata) {
        // Remove all existing components
        this.removeAll();

        // Create the tabbed pane
        JTabbedPane tabbedPane = new JTabbedPane();

        // User Profile tab
        JPanel userProfilePanel = new UserPanel(userdata);

        tabbedPane.addTab("User Profile", userProfilePanel);

        // Play Game tab
        JButton playGamePanel = new WaitingButton(tabbedPane);
        tabbedPane.addTab("Play Game", playGamePanel);

        // Leader Board tab
        JPanel leaderBoardPanel = new LeaderboardPanel();
        tabbedPane.addTab("Leader Board", leaderBoardPanel);


        // Logout tab
        JButton logoutBtn = new JButton("Logout");
        logoutBtn.addActionListener(
            new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    // RMI Call to Server for Logout
                    try {
                        assert (username != null);
                        LogoutStatus result = authHandler.logout(username);
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
        
        public UserPanel(UserData userdata) {
            createUserProfilePage(userdata);
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

    private class LeaderboardPanel extends JPanel {
        public LeaderboardPanel() {
            setLayout(new BorderLayout());
            createLeaderboardTable();
        }

        private void createLeaderboardTable() {
            // Create the table model
            DefaultTableModel tableModel = new DefaultTableModel(
                    new Object[]{"Rank", "Player", "Games won", "Games played", "Avg. winning time"}, 0
            );

            // Populate the table model with data from the server
            try {
                ArrayList<UserData> leaderboardData = authHandler.getUserLeaderboard();
                for (UserData row : leaderboardData) {
                    tableModel.addRow(
                        new Object[]{ row.rank, row.username, row.wins, row.games, row.avgWinTime }
                    );
                }
            } catch (RemoteException e) {
                System.err.println("Error retrieving leaderboard data: " + e);
                return;
            }

            // Create the JTable and add it to the panel
            JTable leaderboardTable = new JTable(tableModel);
            leaderboardTable.setAutoCreateRowSorter(true);
            leaderboardTable.setFillsViewportHeight(true);
            leaderboardTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

            JScrollPane scrollPane = new JScrollPane(leaderboardTable);
            add(scrollPane, BorderLayout.CENTER);
        }
    }

    private class WaitingButton extends JButton {
        public JTabbedPane parent;
        public WaitingButton(JTabbedPane parent) {
            super("Play Game");
            this.parent = parent;
            this.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                   
                   // TODO: Business Logic here
                    TransitionToGamePanel();
                }
            });
        }

        private void TransitionToGamePanel() {
            // Find the parent tabbed pane
            Component component = WaitingButton.this.parent;
            while (!(component instanceof JTabbedPane) && component != null) {
                component = component.getParent();
            }

            if (component != null) {
                // Replace this panel with game panel
                JTabbedPane tabbedPane = (JTabbedPane) component;
                int index = tabbedPane.indexOfComponent(WaitingButton.this);
                // FIXME: Remove Boilerplate seeding{
                String[] cards = {"A♠", "K♣", "2♦", "3♥"};
                String[][] players = {
                    { "Kevin", "Win: 0/0 avg: 0.0s" },
                    { "Kevin2", "Win: 0/0 avg: 0.0s" },
                    { "Kevin3", "Win: 0/0 avg: 0.0s" },
                    { "Kevin4", "Win: 0/0 avg: 0.0s" }
                };
                tabbedPane.setComponentAt(index, new GamePanel(cards, players));
            }
        }
    }

    private class GamePanel extends JPanel {
        public GamePanel(String[] cards, String[][] players) {
            setLayout(new BorderLayout(10, 10));
            setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

            // Card display (4 cards)
            JPanel cardsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
            cardsPanel.setBorder(BorderFactory.createEmptyBorder(20, 10, 20, 10));

            for (String card : cards) {
                JPanel cardPanel = createPlayingCard(card);
                cardsPanel.add(cardPanel);
            } add(cardsPanel, BorderLayout.CENTER);

            // Player list with styled boxes
            JPanel playersPanel = new JPanel();
            playersPanel.setLayout(new BoxLayout(playersPanel, BoxLayout.Y_AXIS));
            playersPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

            for (String[] player : players) {
                JPanel playerBox = new JPanel();
                playerBox.setLayout(new BoxLayout(playerBox, BoxLayout.Y_AXIS));
                playerBox.setBorder(BorderFactory.createLineBorder(Color.GRAY));
                playerBox.setBackground(new Color(240, 240, 240));
                playerBox.setMaximumSize(new Dimension(200, 60));
                playerBox.setPreferredSize(new Dimension(150, 60));

                JLabel nameLabel = new JLabel(player[0]);
                nameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
                nameLabel.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));

                JLabel statsLabel = new JLabel(player[1]);
                statsLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
                statsLabel.setBorder(BorderFactory.createEmptyBorder(2, 0, 5, 0));

                playerBox.add(nameLabel);
                playerBox.add(statsLabel);

                playersPanel.add(playerBox);
                playersPanel.add(Box.createRigidArea(new Dimension(0, 10)));
            }
            
            add(playersPanel, BorderLayout.EAST);

            // Input field with result label
            JPanel expressionPanel = new JPanel(new BorderLayout(5, 0));
            expressionPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

            JLabel resultLabel = new JLabel();
            resultLabel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
            expressionPanel.add(resultLabel, BorderLayout.EAST);
            
            JTextField expressionField = new JTextField();
            expressionField.addActionListener(new ExpressionEvaluator(expressionField, resultLabel, cards));    
            expressionPanel.add(expressionField, BorderLayout.CENTER);
            add(expressionPanel, BorderLayout.SOUTH);
        }

        private JPanel createPlayingCard(String cardValue) {
            // Define fixed card dimensions
            final int cardWidth = 120;
            final int cardHeight = 160;
            
            // Create a fixed-size container panel with null layout
            JPanel fixedSizeContainer = new JPanel(null);
            fixedSizeContainer.setPreferredSize(new Dimension(cardWidth, cardHeight));
            
            // Create the actual card panel
            JPanel cardOuter = new JPanel(new BorderLayout());
            cardOuter.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
            cardOuter.setBounds(0, 0, cardWidth, cardHeight);
            
            JPanel cardPanel = new JPanel(new BorderLayout());
            cardPanel.setBackground(Color.WHITE);
            cardPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

            // Parse card value
            // String rank = cardValue.substring(0, cardValue.length() - 1);
            String suit = cardValue.substring(cardValue.length() - 1);

            // Determine color based on suit
            Color textColor = 
                (suit.equals("♥") || suit.equals("♦")) ? Color.RED : Color.BLACK;

            // Top-left rank and suit
            JLabel topLeftLabel = new JLabel(cardValue);
            topLeftLabel.setFont(new Font("Arial", Font.BOLD, 16));
            topLeftLabel.setForeground(textColor);
            cardPanel.add(topLeftLabel, BorderLayout.NORTH);

            // Center suit/image (larger)
            JLabel centerLabel = new JLabel(suit, SwingConstants.CENTER);
            centerLabel.setFont(new Font("Arial", Font.BOLD, 50));
            centerLabel.setForeground(textColor);
            cardPanel.add(centerLabel, BorderLayout.CENTER);

            // Bottom-right rank and suit (upside down)
            JLabel bottomRightLabel = new JLabel(cardValue, SwingConstants.RIGHT);
            bottomRightLabel.setFont(new Font("Arial", Font.BOLD, 16));
            bottomRightLabel.setForeground(textColor);
            cardPanel.add(bottomRightLabel, BorderLayout.SOUTH);

            cardOuter.add(cardPanel);
            fixedSizeContainer.add(cardOuter);
            
            return fixedSizeContainer;
        }

        
    }
}