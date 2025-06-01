package TwentyFourGame.Server;

import java.rmi.Remote;
import java.rmi.RemoteException;

import java.util.ArrayList;


public interface Authenticate extends Remote {
    RegisterStatus register(String username, String passwordHash) throws RemoteException;
    UserData getUserData(String username) throws RemoteException;
    LoginStatus login(String username, String passwordHash) throws RemoteException;
    ArrayList<UserData> getUserLeaderboard() throws RemoteException;
    LogoutStatus logout(String username) throws RemoteException;
}