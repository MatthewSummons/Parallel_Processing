package TwentyFourGame.Server;

import java.rmi.Remote;
import java.rmi.RemoteException;

import java.util.List;


public interface Authenticate extends Remote {
    LoginStatus login(String username, String passwordHash) throws RemoteException;
    UserData getUserData(String username) throws RemoteException;
    List<String[]> getUserLeaderboard() throws RemoteException;
    RegisterStatus register(String username, String passwordHash) throws RemoteException;
    LogoutStatus logout(String username) throws RemoteException;
}
