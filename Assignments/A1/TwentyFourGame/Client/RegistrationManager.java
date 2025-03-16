package TwentyFourGame.Client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.rmi.RemoteException;
import java.util.Arrays;
import TwentyFourGame.Server.Authenticate;
import TwentyFourGame.Server.RegisterStatus;
import TwentyFourGame.Server.UserData;
import TwentyFourGame.Server.HashUtil;


public class RegistrationManager extends JDialog {

    private JFrame parentFrame;
    
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JPasswordField confirmPasswordField;

    
    private Authenticate authHandler;
    private boolean isRegistered = false;
    private boolean isLoggedIn = false;
    private UserData userData;

    // TODO: Clean up the UI
    public RegistrationManager(JFrame parent, Authenticate authHandler) {
        super(parent, "Register", true);
        this.parentFrame = parent;
        this.authHandler = authHandler;

        // Set layout and size
        setLayout(new GridLayout(5, 2));
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(300, 200);
        setLocationRelativeTo(parent);

        // Add components
        add(new JLabel("Login Name:"));
        usernameField = new JTextField();
        add(usernameField);

        add(new JLabel("Password:"));
        passwordField = new JPasswordField();
        add(passwordField);

        add(new JLabel("Confirm Password:"));
        confirmPasswordField = new JPasswordField();
        add(confirmPasswordField);

        // Register button
        JButton registerButton = new JButton("Register");
        registerButton.addActionListener(new RegistrationHandler()); 
        add(registerButton);

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
        add(cancelButton);
    }

    // Handles RMI Call To The Server
    private class RegistrationHandler implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String userName = usernameField.getText();

            if (userName.isEmpty()) {
                Notification.showError("Login Name cannot be empty!", parentFrame);
                return;
            } else if (userName.contains(" ")) {
                Notification.showError("Login Name cannot contain spaces!", parentFrame);
                return;
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
        assert isRegistered;
        assert isLoggedIn;
        return userData;
    }
}