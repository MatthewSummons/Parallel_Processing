package TwentyFourGame.Server;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class AuthenticationManager extends UnicastRemoteObject implements Authenticate {
    private static final String USER_INFO_FILE = "TwentyFourGame/Server/UserInfo.txt";
    private static final String ONLINE_USER_FILE = "TwentyFourGame/Server/OnlineUser.txt";

    private ReadWriteLock userInfoLock;
    private ReadWriteLock onlineUserLock;

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
        this.userInfoLock = new ReentrantReadWriteLock();
        this.onlineUserLock = new ReentrantReadWriteLock();

        // Check if the OnlineUser.txt file exists, and create or clear it if necessary
        File onlineUserFile = new File(ONLINE_USER_FILE);
        if (onlineUserFile.exists()) {
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(ONLINE_USER_FILE))) {
                // Clear the file by opening it in write mode
            } catch (IOException e) {
                throw new IOException("Error clearing online user file: " + e);
            }
        } else {
            try {
                if (!onlineUserFile.createNewFile()) {
                    throw new IOException("Failed to create OnlineUser.txt file");
                }
            } catch (IOException e) {
                throw new IOException("Error creating online user file: " + e);
            }
        }
    }

    @Override
    public LoginStatus login(String username, String passwordHash) throws RemoteException {
        userInfoLock.readLock().lock();
        try {
            String[] userInfo = readUserInfo(username);
            if (userInfo != null && userInfo[1].equals(passwordHash)) {
                onlineUserLock.writeLock().lock();
                try {
                    if (!isUserOnline(username)) {
                        addOnlineUser(username);
                        return LoginStatus.SUCCESS;
                    } else {
                        return LoginStatus.LOGGED_IN;
                    }
                } finally {
                    onlineUserLock.writeLock().unlock();
                }
            } else {
                return LoginStatus.INVALID_CREDENTIALS;
            }
        } catch (IOException e) {
            System.err.println("Error: " + e);
            return LoginStatus.SERVER_ERROR;
        } finally {
            userInfoLock.readLock().unlock();
        }
    }

    // FIXME: Implement getUserData
    @Override
    public UserData getUserData(String username) throws RemoteException {
        return new UserData(
            username, 12, 25, 23.4, 1
        );
    }    
    
    @Override
    public RegisterStatus register(String username, String passwordHash) throws RemoteException {
        userInfoLock.writeLock().lock();
        try {
            if (isUserRegistered(username)) {
                return RegisterStatus.USERNAME_TAKEN;
            } else {
                writeUserInfo(username, passwordHash);
                return RegisterStatus.SUCCESS;
            }
        } catch (IOException e) {
            System.err.println("Error: " + e);
            return RegisterStatus.SERVER_ERROR;
        } finally {
            userInfoLock.writeLock().unlock();
        }
    }

    @Override
    public LogoutStatus logout(String username) throws RemoteException {
        onlineUserLock.writeLock().lock();
        try {
            return removeOnlineUser(username);
        } finally {
            onlineUserLock.writeLock().unlock();
        }
    }

    private String[] readUserInfo(String username) throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader(USER_INFO_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(" ");
                if (parts.length == 2 && parts[0].equals(username)) {
                    return new String[] { parts[0], parts[1] };
                }
            }
        }
        return null;
    }

    private boolean isUserRegistered(String username) {
        try {
            return readUserInfo(username) != null;
        } catch (IOException e) {
            System.err.println("Error: " + e);
            return false;
        }
    }

    private void writeUserInfo(String username, String passwordHash) throws IOException {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(USER_INFO_FILE, true))) {
            bw.write(username + " " + passwordHash);
            bw.newLine();
        }
    }

    private boolean isUserOnline(String username) throws IOException{
        try (BufferedReader br = new BufferedReader(new FileReader(ONLINE_USER_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.equals(username)) {
                    return true;
                }
            }
        } return false;
    }

    private void addOnlineUser(String username) throws IOException {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(ONLINE_USER_FILE, true))) {
            bw.write(username);
            bw.newLine();
        }
    }

    private LogoutStatus removeOnlineUser(String username) {
        List<String> onlineUsers = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(ONLINE_USER_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (!line.equals(username)) {
                    onlineUsers.add(line);
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading online user file: " + e);
            return LogoutStatus.SERVER_ERROR;
        }

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(ONLINE_USER_FILE))) {
            for (String user : onlineUsers) {
                bw.write(user);
                bw.newLine();
            }
        } catch (IOException e) {
            System.err.println("Error writing to online user file: " + e);
            return LogoutStatus.SERVER_ERROR;
        } return LogoutStatus.SUCCESS;
    }
}