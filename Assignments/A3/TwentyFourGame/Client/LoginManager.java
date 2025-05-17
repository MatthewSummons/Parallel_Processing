package TwentyFourGame.Client;

import TwentyFourGame.Server.*;
import java.util.Arrays;
import javax.swing.*;
import java.rmi.RemoteException;



public class LoginManager {
    
    private boolean isLoggedIn = false;
    private UserData userData;
    
    private JFrame parentFrame;
    private JTextField usernameField;
    private JPasswordField passwordField;

    public LoginManager(JFrame parent, JTextField usernameField, JPasswordField passwordField) {
        this.parentFrame = parent;
        this.usernameField = usernameField;
        this.passwordField = passwordField;
    }

    // TODO: Trigger event on `Enter` key press
    public void attemptLogin(Authenticate authHandler) {
        String username = usernameField.getText();
        char[] password = passwordField.getPassword();
        String passwordHash = HashUtil.SHA_256Hash(password);
        Arrays.fill(password, '0');

        if (username.isEmpty() || password.length == 0) {
            Notification.showError("Username and Password cannot be empty!", parentFrame);
            return;
        } else if (username.length() > UserData.MAX_USRERNAME_LENGTH) {
            Notification.showError(
                "Login Name cannot be longer than 80 characters!", parentFrame
            );return;
        }

        RMI_login(username, passwordHash, authHandler);
    }

    public void RMI_login(String username, String passwordHash, Authenticate authHandler) {
        // RMI Call to Server for Login
        try {
            LoginStatus status = authHandler.login(username, passwordHash);
            
            if (status == LoginStatus.INVALID_CREDENTIALS) {
                Notification.showError("Invalid Credentials!", parentFrame);
                return;
            } else if (status == LoginStatus.ALREADY_LOGGED_IN) {
                Notification.showError("User Already Logged In!", parentFrame);
                return;
            } else if (status == LoginStatus.SERVER_ERROR) {
                Notification.showError("Server Error!", parentFrame);
                return;
            }
        
            userData = authHandler.getUserData(username);
        
        } catch (RemoteException e) {
            System.err.println("Error: " + e);
            Notification.showError("Server Disconnected!", parentFrame);
            return;
        }

        isLoggedIn = true;
    }

    public boolean isLoggedIn() {
        return isLoggedIn;
    }

    public UserData getUserData() {
        return userData;
    }
}