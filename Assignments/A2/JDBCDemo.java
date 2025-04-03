import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Scanner;

public class JDBCDemo {
	private static final String DB_HOST = "sophia";
	private static final String DB_USER = "";
	private static final String DB_PASS = "";
	private static final String DB_NAME = "";
	
	public static void main(String[] args) {
		try {
			new JDBCDemo().go();
		} catch (InstantiationException | IllegalAccessException
				| ClassNotFoundException | SQLException e) {
			System.err.println("Connection failed: "+e);
		}
	}
	private Connection conn;
	public JDBCDemo() throws SQLException, InstantiationException, IllegalAccessException, ClassNotFoundException {
	}
	public void go() {
		
		Scanner keyboard = new Scanner(System.in);
		String line;
		System.out.print("> ");
		while(!(line = keyboard.next()).equals("exit")) {
			if(line.equals("create")) {
				insert(keyboard.next(), keyboard.next());
			} else if(line.equals("read")) {
				read(keyboard.next());
			} else if(line.equals("list")) {
				list();
			} else if(line.equals("update")) {
				update(keyboard.next(), keyboard.next());
			} else if(line.equals("delete")) {
				delete(keyboard.next());
			}
			System.out.print("> ");
		}
		keyboard.close();
		
	}
	private void insert(String name, String birthday) {
		
	}
	private void read(String name) {
		
	}
	private void list() {
		
	}
	private void update(String name, String birthday) {
		
	}
	private void delete(String name) {
		
	}
}
