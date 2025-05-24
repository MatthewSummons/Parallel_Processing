package TwentyFourGame.Client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.rmi.RemoteException;

import java.util.Arrays;

import TwentyFourGame.Common.Authenticate;
import TwentyFourGame.Common.RegisterStatus;
import TwentyFourGame.Common.UserData;
import TwentyFourGame.Server.HashUtil;


public class RegistrationManager extends JDialog {

    private JFrame parentFrame;
    
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JPasswordField confirmPasswordField;

    
    private Authenticate authHandler;
    private boolean isRegistered = false;
    private boolean isLoggedIn   = false;
    private UserData userData;

    public RegistrationManager(JFrame parent, Authenticate authHandler) {
        super(parent, "Register", true);
        this.parentFrame = parent;
        this.authHandler = authHandler;

        // Set layout and size
        setLayout(new GridBagLayout());
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(600, 450);
        setLocationRelativeTo(parent);
        setResizable(true);

        // Add components
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.insets = new Insets(15, 20, 15, 20);
        constraints.fill = GridBagConstraints.HORIZONTAL;

        // Username
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.anchor = GridBagConstraints.EAST;
        add(new JLabel("Login Name:"), constraints);

        constraints.gridx = 1;
        constraints.anchor = GridBagConstraints.WEST;
        usernameField = new JTextField(25);
        add(usernameField, constraints);

        // Password
        constraints.gridx = 0;
        constraints.gridy = 1;
        constraints.anchor = GridBagConstraints.EAST;
        add(new JLabel("Password:"), constraints);

        constraints.gridx = 1;
        constraints.anchor = GridBagConstraints.WEST;
        passwordField = new JPasswordField(25);
        add(passwordField, constraints);

        // Confirm Password
        constraints.gridx = 0;
        constraints.gridy = 2;
        constraints.anchor = GridBagConstraints.EAST;
        add(new JLabel("Confirm Password:"), constraints);

        constraints.gridx = 1;
        constraints.anchor = GridBagConstraints.WEST;
        confirmPasswordField = new JPasswordField(25);
        add(confirmPasswordField, constraints);

        // Buttons
        constraints.gridx = 0;
        constraints.gridy = 3;
        constraints.gridwidth = 2;
        constraints.anchor = GridBagConstraints.CENTER;

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));

        // Register button
        JButton registerButton = new JButton("Register");
        registerButton.addActionListener(new RegistrationHandler());
        buttonPanel.add(registerButton);

        // Cancel button
        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Open login Panel again
                parentFrame.setVisible(true);
                dispose();
            }
        });
        buttonPanel.add(cancelButton);

        add(buttonPanel, constraints);
    }

    // Handles RMI Call To The Server
    private class RegistrationHandler implements ActionListener {
        @Override // Called when the register button is clicked
        public void actionPerformed(ActionEvent e) {
            String userName = usernameField.getText();

            if (userName.isEmpty()) {
                Notification.showError("Login Name cannot be empty!", parentFrame);
                return;
            } else if (userName.contains(" ")) {
                Notification.showError("Login Name cannot contain spaces!", parentFrame);
                return;
            } else if (userName.length() > UserData.MAX_USRERNAME_LENGTH) {
                Notification.showError(
                    "Login Name cannot be longer than 80 characters!", parentFrame
                ); return;
            }

            // Grab the passwords and obtain their hashes
            char [] password = passwordField.getPassword();
            String passwordHash = HashUtil.SHA_256Hash(password);
            Arrays.fill(password, '0');

            char[] confirmPassword = confirmPasswordField.getPassword();
            String confirmPasswordHash = HashUtil.SHA_256Hash(confirmPassword);
            Arrays.fill(confirmPassword, '0');


            // Even after zeroing the arrays, paasword length remains
            if (password.length == 0 || confirmPassword.length == 0) {
                Notification.showError("Password cannot be empty!", parentFrame);
                return;
            }

            if (passwordHash.equals(confirmPasswordHash)) {

                // Registration RMI Call
                try {
                    RegisterStatus result = authHandler.register(userName, passwordHash);
                    if (result == RegisterStatus.USERNAME_TAKEN) {
                        Notification.showError("Username already taken!", parentFrame);
                        return;
                    } else if (result == RegisterStatus.SERVER_ERROR) {
                        Notification.showError("Server Error!", parentFrame);
                        return;
                    } else if (result == RegisterStatus.SUCCESS) {
                        Notification.showConfirm("Registration Successful!", parentFrame);
                        dispose();

                        isRegistered = true;
                        LoginManager loginManager = new LoginManager(parentFrame, null, null);
                        loginManager.RMI_login(userName, passwordHash, authHandler);
                        isLoggedIn = loginManager.isLoggedIn();
                        if (isLoggedIn) {
                            userData = loginManager.getUserData();
                        }
                    }

                } catch (RemoteException ex) {
                    System.out.println("Error: " + ex);
                    Notification.showError("Server is offline!", parentFrame);
                    return;
                }
            } else {
                Notification.showError("Passwords do not match!", parentFrame);
            }
        }
    }

    public boolean isRegistered() {
        return isRegistered;
    }

    public boolean isLoggedIn() {
        return isLoggedIn;
    }

    public UserData getUserData() {
        if (isRegistered && isLoggedIn) {
            return userData;
        } Notification.showError("User is not registered or logged in!", parentFrame);
        throw new IllegalStateException("User is not registered or logged in.");
    }
}