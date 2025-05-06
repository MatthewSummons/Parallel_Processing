package TwentyFourGame.Server;

// TODO: Remove later
import java.io.*;

import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import java.util.ArrayList;

import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;


public class AuthenticationManager extends UnicastRemoteObject implements Authenticate {
    // Crashes if DB cannot be initialized, connected to etc.
    private DatabaseManager DB = new DatabaseManager();
    
    // TODO: Refactor to use DB
    private static final String USER_INFO_FILE = "TwentyFourGame/Server/UserInfo.txt";

    // TODO: Remove
    private ReadWriteLock userInfoLock;

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

    public AuthenticationManager() throws RemoteException, IOException {
        super();
        
        // TODO: Also Remove
        this.userInfoLock = new ReentrantReadWriteLock();
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
    // TODO: Refactor to take in UserData and write to DB
    public ArrayList<String[]> getUserLeaderboard() throws RemoteException {
        userInfoLock.readLock().lock();
        try {
            ArrayList<String[]> leaderboard = new ArrayList<>();
            try (BufferedReader br = new BufferedReader(new FileReader(USER_INFO_FILE))) {
                String line;
                int counter = 1;
                while ((line = br.readLine()) != null) {
                    String[] parts = line.split(", ");
                    if (parts.length == 6) {
                        leaderboard.add(new String[]{
                            parts[0],
                            counter++ + "",
                            parts[2],
                            parts[3],
                            parts[4],
                            parts[5]
                        });
                    }
                }
            }
            leaderboard.sort((a, b) -> Integer.parseInt(a[5]) - Integer.parseInt(b[5]));
            return leaderboard;
        } catch (IOException e) {
            System.err.println("Error: " + e);
            return null;
        } finally {
            userInfoLock.readLock().unlock();
        }
    }
    

    @Override
    public LogoutStatus logout(String username) throws RemoteException {
        return DB.RemoveOnlineUser(username);
    }
}