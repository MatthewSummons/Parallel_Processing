package TwentyFourGame.Server;

import java.rmi.Remote;
import java.rmi.RemoteException;


public interface Authenticate extends Remote {
    LoginStatus login(String username, String passwordHash) throws RemoteException;
    UserData getUserData(String username) throws RemoteException;
    RegisterStatus register(String username, String passwordHash) throws RemoteException;
    LogoutStatus logout(String username) throws RemoteException;
}
