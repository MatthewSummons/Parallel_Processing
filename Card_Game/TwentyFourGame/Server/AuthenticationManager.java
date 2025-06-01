package TwentyFourGame.Server;

import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import java.util.ArrayList;
import java.util.Queue;

import javax.jms.*;
import javax.naming.Context;
import javax.naming.InitialContext;

import TwentyFourGame.Common.Authenticate;
import TwentyFourGame.Common.LoginStatus;
import TwentyFourGame.Common.LogoutStatus;
import TwentyFourGame.Common.RegisterStatus;
import TwentyFourGame.Common.UserData;


public class AuthenticationManager extends UnicastRemoteObject implements Authenticate {
    // Crashes if DB cannot be initialized, connected to etc.
    private DatabaseManager DB = new DatabaseManager();
    private GameQueueListener gameQueueListener;
    private GamePublisher gamePublisher;


    public static void main(String[] args) {
        
        try {
            AuthenticationManager app = new AuthenticationManager();
            System.setSecurityManager(new SecurityManager());
            Naming.rebind("AuthenticationManager", app);
            System.out.println("AuthenticationManager service registered");
        } catch (Exception e) {
            System.err.println("Exception thrown: " + e);
        }
    }

    public AuthenticationManager() throws RemoteException {
        super();
        try {
            gameQueueListener = new GameQueueListener(DB);
            gamePublisher = new GamePublisher(gameQueueListener.session);
            gameQueueListener.setGamePublisher(gamePublisher);
            gameQueueListener.startListening();
            System.out.println("JMS GameQueueListener started inside AuthenticationManager.");
        } catch (Exception e) {
            System.err.println("Failed to start JMS GameQueueListener: " + e);
        }
    }

    
    @Override
    public RegisterStatus register(String username, String passwordHash) throws RemoteException {
        return DB.register(username, passwordHash);
    }

    @Override
    public UserData getUserData(String username) throws RemoteException {
        return DB.readUserInfo(username);
    }
        
    @Override
    public LoginStatus login(String username, String passwordHash) throws RemoteException {
        return DB.login(username, passwordHash);
    }

    @Override
    public ArrayList<UserData> getUserLeaderboard() throws RemoteException {
        return DB.fetchUserLeaderboard();
    }

    @Override
    public LogoutStatus logout(String username) throws RemoteException {
        return DB.removeOnlineUser(username);
    }
}