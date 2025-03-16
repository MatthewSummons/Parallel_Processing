import java.io.*;
import java.net.*;
import java.util.*;


public class ChatClient {
	
	public static void main(String[] args) {
		new ChatClient().go();
	}
	private PrintWriter out;
	private BufferedReader in;
	public void go() {
		try {
			Socket s = new Socket("localhost", 10000);
			System.out.println("Connection established");
			out = new PrintWriter(s.getOutputStream());
			in = new BufferedReader(new InputStreamReader(s.getInputStream()));
			System.out.println("I/O streams prepared");
			new Thread(new ResponseHandler()).start();
			
			Scanner scanner = new Scanner(System.in);
			
			boolean loop = true;
			while(loop) {
				String line = scanner.nextLine();
				if(line == null || line.isEmpty()) {
					loop = false;
				} else {
					out.println(line);
					out.flush();
				}
			}
			scanner.close();
			s.close();
		} catch(IOException e){
			System.err.println("Connection closed");
		}
	}
	private class ResponseHandler implements Runnable {
		
		public void run() {
			try {
				while(true) {
					System.out.println(in.readLine());
				}
			} catch(IOException e) {
			}
		}
	}
}
