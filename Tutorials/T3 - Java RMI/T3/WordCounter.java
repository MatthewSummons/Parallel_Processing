import java.rmi.*;
import java.rmi.server.*;

public class WordCounter extends UnicastRemoteObject implements WordCount {
	public static void main(String[] args) {
		try {
			WordCounter app = new WordCounter();
			System.setSecurityManager(new SecurityManager());
			Naming.rebind("WordCounter", app);
			System.out.println("Service Registered");
		} catch(Exception e) {
			System.err.println("Exception thrown: " + e);
		}
	}
	
	public WordCounter() throws  RemoteException {}
	public int count(String message) throws RemoteException {
		System.out.println("Counting");	
		return message.split(" +").length;
	}
}